/*
 *
 * Portions Copyright  2003-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 *
 *!c<
 * Copyright 2006 Intel Corporation. All rights reserved.
 *!c>
 */

#include "incls/_precompiled.incl"
#include "incls/_Compiler.cpp.incl"

#if ENABLE_COMPILER

PRODUCT_ONLY(inline)
CodeGenerator::CodeGenerator(Compiler* compiler)
  : BinaryAssembler(compiler->current_compiled_method())
{
  compiler->set_code_generator(this);
}

PRODUCT_ONLY(inline)
CodeGenerator::CodeGenerator(Compiler* compiler,
                                    CompilerState* compiler_state)
  : BinaryAssembler(compiler_state, compiler->current_compiled_method())
{
  compiler->set_code_generator(this);
}

CompilerStatic Compiler::_state;
CompilerState  Compiler::_suspended_compiler_state;

inline void CompilerState::oops_do( void do_oop(OopDesc**) ) {
  if( valid() ) {
    OopDesc** p = (OopDesc**) this;
    for( int i = pointer_count(); --i >= 0; p++ ) {
      do_oop( p );
    }
  }
}

inline void CompilerStaticPointers::oops_do( void do_oop(OopDesc**) ) {
  if( valid() ) {
    OopDesc** p = (OopDesc**) this;
    for( int i = pointer_count(); --i >= 0; p++ ) {
      do_oop( p );
    }
  }
}

inline void CompilerStaticPointers::cleanup( void ) {
  if( valid() ) {
    jvm_memset( this, 0, sizeof *this );
  }
}

jlong                           Compiler::_estimated_frame_time;
jlong                           Compiler::_last_frame_time_stamp;
Compiler::CompilationFailure    Compiler::_failure;
#ifndef PRODUCT
Compiler::CompilationHistory*   Compiler::_history_head;
Compiler::CompilationHistory*   Compiler::_history_tail;
#endif

#if ENABLE_APPENDED_CALLINFO
CallInfoWriter Compiler::_callinfo_writer;
#endif

Compiler::Compiler( Method* method, const int active_bci ) {
  set_method( method );
  _failure = reservation_failed;
  _closure.initialize(this, method, active_bci);

  GUARANTEE(!is_active(), "Only one compiler at a time");
  _current = this;
#if ENABLE_CSE
  VirtualStackFrame::clear_cse_status();
  RegisterAllocator::init_notation();
#endif
  jvm_fast_globals.compiler_method         = _state.method();
  jvm_fast_globals.compiler_frame          = _state.frame();
  jvm_fast_globals.compiler_closure        = &_closure;
}

// Called during VM start-up
void Compiler::initialize() {
  jvm_memset(&_state, 0, sizeof(_state));
  jvm_memset(&_suspended_compiler_state, 0, sizeof(_suspended_compiler_state));
#if ENABLE_PERFORMANCE_COUNTERS && ENABLE_DETAILED_PERFORMANCE_COUNTERS
  jvm_memset(&comp_perf_counts, 0, sizeof(comp_perf_counts));
#endif
  _estimated_frame_time = 30;
  _last_frame_time_stamp = Os::java_time_millis();
}

void Compiler::set_hint(int hint) {
  switch (hint) {
  case JVM_HINT_VISUAL_OUTPUT:
    _estimated_frame_time = 300;
    _last_frame_time_stamp = Os::java_time_millis();
    break;
  case JVM_HINT_END_STARTUP_PHASE:
    break;
  }
}

void Compiler::on_timer_tick(bool is_real_time_tick JVM_TRAPS) {
  if( !UseCompiler || !Universe::is_compilation_allowed() ) {
    return;
  }

  if( TestCompiler ) {
    // We are testing the compiler (which may the ARM compiler running
    // inside an x86-hosted romgen). At this point we're initializing
    // the test classes and get a timer tick. Don't compile, or else
    // we will soon be executing JIT code of the wrong architecture!
    return;
  }

#if ENABLE_INTERPRETATION_LOG
  if (is_real_time_tick) {
    process_interpretation_log();
  }
#endif

  JavaFrame frame(Thread::current());

  // We don't instrument backward branches to update _method_execution_sensor,
  // so it is necessary to supplement the detection of execution of
  // loop-intensive compiled methods by sampling on timer ticks
  if( frame.is_compiled_frame() ) {
    const OopDesc* cm = frame.as_JavaFrame().compiled_method();
    if( !ROM::system_contains( cm ) ) {
      _method_execution_sensor[((CompiledMethodDesc*)cm)->get_cache_index()] = 0;
    }
  }

  CompiledMethodCache::on_timer_tick();
  if( --Universe::_compilation_abstinence_ticks >= 0 ) {
    return;
  }

  const jint bci = frame.bci();
#if ENABLE_JAVA_DEBUGGER
    // If we are single stepping in this thread, and we are at bci == 0
    // we might be stepping into this method.  So don't compile it.
    // The debugger code will mark it as impossible_to_compile if it
    // actually steps into it.

    if (Thread::current()->is_stepping() && bci == 0) {
      return;
    }
#endif

  const bool resume = Compiler::is_suspended();

  UsingFastOops fast_oops;
  Method::Fast current_compiling;
  if( resume ) {
    // IMPL_NOTE: (1) don't resume compilation if we have spent too many ticks
    //            on it already.
    //        (2) if current_compiling is the current method, set active_bci
    //            so that it can be more easily OSR'ed.
    current_compiling = Compiler::method();
  } else {
    if (!frame.is_compiled_frame()) {
      current_compiling = frame.method();
      Symbol::Raw name = current_compiling().name();
      if (name.equals(Symbols::class_initializer_name()) && bci == 0) {
        // Don't compile <clinit> methods on-entry. If this method 
        // contains a hot loop, it will eventually be discovered later and
        // we will compile it then. But in most cases, <clinit> methods
        // aren't worth compiling.
        return;
      }
    }
  }
  if( current_compiling.not_null() ) {
#ifndef PRODUCT
    if( TraceCompiledMethodCache ) {
      tty->print_cr("Current method ");
      current_compiling().print_name_on( tty );
      if( current_compiling().is_impossible_to_compile() ) {
        tty->print( " - impossible to compile" );
      }
      else if( current_compiling().has_compiled_code() ) {
        tty->print( " - already compiled" );
      }
      tty->cr();
    }
#endif
    const bool is_compiled =
      current_compiling().compile(bci, resume JVM_MUST_SUCCEED);
    if( is_compiled && InstallCompiledCode && !frame.is_compiled_frame()
        && current_compiling.obj() == frame.method() ) {
      frame.osr_replace_frame(bci);
    }
    GUARANTEE( Compiler::is_suspended() ==
               Compiler::current_compiled_method()->not_null(), "sanity");
  }
}

#if ENABLE_INTERPRETATION_LOG
void Compiler::process_interpretation_log() {
  jlong now = Os::java_time_millis();
  if (now < _last_frame_time_stamp + _estimated_frame_time) {
    Universe::reset_interpretation_log();
    return;
  }

  if( TraceCompiledMethodCache ) {
    TTY_TRACE_CR(( "Interpretation log scan beg" ));
  }

#if ENABLE_PERFORMANCE_COUNTERS
  if( InterpretationLogSize < INTERP_LOG_SIZE ) {
    // Clear interpretation log tail
    jvm_memset(&_interpretation_log[InterpretationLogSize], 0,
               (INTERP_LOG_SIZE - InterpretationLogSize) * 
               sizeof(_interpretation_log[0]));
  }
#endif


  // Mark all the recently interpreted methods to be
  // compiled-on-invocation
  unsigned possible_to_compile_count = 0;
  ForInterpretationLog( p ) {
    Method::Raw m = *p;

#ifndef PRODUCT
    if( TraceCompiledMethodCache ) {
      m().print_name_on( tty );
      if( m().is_impossible_to_compile() ) {
        tty->print( " - impossible to compile" );
      } else if( m().has_compiled_code() ) {
        tty->print( " - already compiled" );
      }
      tty->cr();
    }
#endif

    if( m().can_be_compiled() ) {
      m().set_execution_entry((address) shared_invoke_compiler);
      possible_to_compile_count++;
    }
    *p = NULL;
  }
  _interpretation_log_idx = 0;

  if( TraceCompiledMethodCache ) {
    TTY_TRACE_CR(( "Interpretation log scan end, "
                   "scanned %d entries, scheduled %d",
                   p - _interpretation_log, possible_to_compile_count ));
  }

  enum { InterpreterFeedbackThreshold = 3 };
  if( possible_to_compile_count > InterpreterFeedbackThreshold ) {
      CompiledMethodCache::degrade();
  }
}
#endif // ENABLE_INTERPRETATION_LOG


void Compiler::oops_do( void do_oop(OopDesc**) ) {
  _state.oops_do( do_oop );
  _suspended_compiler_state.oops_do( do_oop );
}

void Compiler::terminate ( OopDesc* result ) {
#ifndef PRODUCT
  if (PrintCompilation) {
    if (result != NULL) {
      TTY_TRACE_CR(("<done %d>", ((CompiledMethodDesc*)result)->object_size()));
    } else {
      TTY_TRACE_CR(("<done>"));
    }
  }
#endif
#if ENABLE_INLINE && ARM  && !CROSS_GENERATOR
 if (_failure != none || result == NULL)
  {
      for( int i = Universe::inlined_count - 1; i >=  0; i--) {
       if ((Universe::inlined_class_ids[i] & METHOD_INDEX_MASK)== CompiledMethodCache::get_upb() + 1) 
       {
          Universe::inlined_count--;
       }
       else
      { 
        break;
      }
      }
  }
#endif
#if ENABLE_TRAMPOLINE && !CROSS_GENERATOR
  if (_failure != none || result == NULL)
  {
    BranchTable::revoke((address) Compiler::current_compiled_method()->obj());
  }
#endif  
  _suspended_compiler_state.dispose();
  _state.cleanup();
  ObjectHeap::update_compiler_area_top( result );
}

void Compiler::set_impossible_to_compile(Method *method, const char why[]) {
  if (!method->is_impossible_to_compile()) {
    (void)why;
#if ENABLE_TTY_TRACE
    if (PrintCompilation || TraceFailedCompilation) {
      tty->print("[impossible to compile: ");
      method->print_name_on(tty);
      tty->print_cr("] - %s", why);
    }
#endif
#if ENABLE_PERFORMANCE_COUNTERS
    jvm_perf_count.num_of_compilations_failed ++;
#endif
  }

  method->set_impossible_to_compile();
}

inline void Compiler::check_free_space( JVM_SINGLE_ARG_TRAPS ) const {
  code_generator()->check_free_space( JVM_SINGLE_ARG_NO_CHECK );
}

inline
ReturnOop Compiler::allocate_and_compile(const int compiled_code_factor
                                         JVM_TRAPS) {
#ifndef PRODUCT
  if( TraceCompiledMethodCache ) {
    tty->print( "\n*** Compiler::allocate_and_compile( " );
    method()->print_name_on( tty );
    tty->print_cr( ") beg ***" );
  }
#endif

  const jint size = align_allocation_size(1024 + (method()->code_size() *
                                            compiled_code_factor));

  if (!reserve_compiler_area((size_t)size)) {
    // We don't have enough space (yet) to compile this method. We'll try to
    // compile it later.
    return NULL;
  }

  if( TraceCompiledMethodCache ) {
    TTY_TRACE_CR(( "allocation size: %d, CompiledCodeFactor: %d",
        size, compiled_code_factor ));
  };

  if( CompiledMethodCache::alloc() == CompiledMethodCache::UndefIndex ) {
    if( TraceCompiledMethodCache ) {
      TTY_TRACE_CR(("out of cache indices"));
    };
    return NULL;
  }

  set_bci(0);

  // Allocate a compiled method.
  UsingFastOops fast_oops;
  CompiledMethod::Fast result =Universe::new_compiled_method(size JVM_NO_CHECK);
  if (CURRENT_HAS_PENDING_EXCEPTION) {
    if( TraceCompiledMethodCache ) {
      TTY_TRACE_CR(( "Compilation FAILED - out of memory" ));
    }
    return NULL;
  }
  *current_compiled_method() = result;
  result().set_method(method());
#if ENABLE_APPENDED_CALLINFO
  _callinfo_writer.initialize(current_compiled_method());
#endif

#if USE_COMPILER_COMMENTS && ENABLE_PERFORMANCE_COUNTERS
  if (PrintCompilation || PrintCompiledCode || PrintCompiledCodeAsYouGo) {
    if (VerbosePointers) {
      tty->print("#%d: ", jvm_perf_count.num_of_compilations);
    }
    tty->print("(bci=%d) Compiling ", 
               closure()->active_bci());
    method()->print_name_on(tty);
    tty->cr();
  }
#endif

  // Instantiate a code generator.
  CodeGenerator code_generator(this);
  internal_compile( JVM_SINGLE_ARG_NO_CHECK );
  switch( _failure ) {
    default:
      // Zap the unused contents in order not to confuse ObjectHeap::verify()
      result().shrink(0, 0);
      if( TraceCompiledMethodCache ) {
        TTY_TRACE_CR(( "Compilation failed - exception thrown" ));
      };
      // Intentionally no break; here
    case out_of_time:
      result.set_null();
    case none:
      break;
  }

  if (PrintCompilationAtExit) {
    append_compilation_history();
  }
  return result;
}

/*
 * The chain of command:
 *       Compiler::compile()
 *  calls Compiler::try_to_compile()
 *   calls Compiler::allocate_and_compile()
 *    calls Compiler::internal_compile()
 *     calls - begin_compile()
 *           - process_compilation_queue()
 *           - end_compile()
 */
ReturnOop Compiler::compile(Method* method, int active_bci JVM_TRAPS) {
  // Bail out if the method is impossible to compile
  if (method->is_impossible_to_compile()) {
    return NULL;
  }

  EnforceCompilerJavaStackDirection enfore_java_stack_direction;

  EventLogger::log(EventLogger::COMPILE_START);

  // We need a bigger compiled code factor if any of these are set.
  // The following values are justs guesses.  They may need to be fixed.
  int compiled_code_factor = CompiledCodeFactor * (1 + 10 * (
    int(GenerateCompilerComments) +
    int(Deterministic) +
    int(TraceBytecodesCompiler)));
  if( method->is_double_size() ) {
    compiled_code_factor <<= 1;
  }

  CompiledMethod::Raw result =
    try_to_compile( method, active_bci, compiled_code_factor JVM_MUST_SUCCEED);
#if ENABLE_COMPILATION_RETRY
  if( _failure == out_of_compiled_code_buffer && !method->is_double_size() ) {
    method->set_double_size();
    // compiler_area will automatically adjusts compiled_code_buffer size,
    // so there's no need to retry the compilation
  }
#endif

  if( _failure > out_of_time ) {
    if( Verbose && _failure == out_of_stack ) {
      TTY_TRACE_CR(("Compilation of method %s failed due to out of stack "
                      "condition", method->name()));
    }
    set_impossible_to_compile(method, "out of memory" );
  }

  EventLogger::log(EventLogger::COMPILE_END);
  return result();
}

#if ENABLE_INTERPRETER_GENERATOR || USE_SOURCE_IMAGE_GENERATOR
// This is a table of bits -- each bit indicate whether the corresponding 
// bytecode may be used by a compiled method without a callframe.
// For more details on the rules of omitting leaf method frames see 
// internal_doc/omit_leaf_method_frames.txt
void Compiler::generate_omit_frame_table(Stream *output) {
  jubyte table[256 / 8];
  int i;

  jvm_memset(table, 0, sizeof(table));
  for (i=0; i<Bytecodes::number_of_java_codes; i++) {
    Bytecodes::Code code = (Bytecodes::Code)i;
    bool always_ok = false;

    switch (code) {
    // All floating point bytecodes (or other bytecodes that need to
    // call a runtime function from compiled code) require a call frame.
    // The exceptions are fconst, dload, etc, that are done without
    // calling runtime functions.
    case Bytecodes::_nop:
    case Bytecodes::_aconst_null:
    case Bytecodes::_iconst_m1:
    case Bytecodes::_iconst_0:
    case Bytecodes::_iconst_1:
    case Bytecodes::_iconst_2:
    case Bytecodes::_iconst_3:
    case Bytecodes::_iconst_4:
    case Bytecodes::_iconst_5:
    case Bytecodes::_lconst_0:
    case Bytecodes::_lconst_1:
    case Bytecodes::_fconst_0:
    case Bytecodes::_fconst_1:
    case Bytecodes::_fconst_2:
    case Bytecodes::_dconst_0:
    case Bytecodes::_dconst_1:
    case Bytecodes::_bipush:
    case Bytecodes::_sipush:
    case Bytecodes::_iload:
    case Bytecodes::_lload:
    case Bytecodes::_fload:
    case Bytecodes::_dload:
    case Bytecodes::_aload:
    case Bytecodes::_iload_0:
    case Bytecodes::_iload_1:
    case Bytecodes::_iload_2:
    case Bytecodes::_iload_3:
    case Bytecodes::_lload_0:
    case Bytecodes::_lload_1:
    case Bytecodes::_lload_2:
    case Bytecodes::_lload_3:
    case Bytecodes::_fload_0:
    case Bytecodes::_fload_1:
    case Bytecodes::_fload_2:
    case Bytecodes::_fload_3:
    case Bytecodes::_dload_0:
    case Bytecodes::_dload_1:
    case Bytecodes::_dload_2:
    case Bytecodes::_dload_3:
    case Bytecodes::_aload_0:
    case Bytecodes::_aload_1:
    case Bytecodes::_aload_2:
    case Bytecodes::_aload_3:
    case Bytecodes::_istore:
    case Bytecodes::_lstore:
    case Bytecodes::_fstore:
    case Bytecodes::_dstore:
    case Bytecodes::_astore:
    case Bytecodes::_istore_0:
    case Bytecodes::_istore_1:
    case Bytecodes::_istore_2:
    case Bytecodes::_istore_3:
    case Bytecodes::_lstore_0:
    case Bytecodes::_lstore_1:
    case Bytecodes::_lstore_2:
    case Bytecodes::_lstore_3:
    case Bytecodes::_fstore_0:
    case Bytecodes::_fstore_1:
    case Bytecodes::_fstore_2:
    case Bytecodes::_fstore_3:
    case Bytecodes::_dstore_0:
    case Bytecodes::_dstore_1:
    case Bytecodes::_dstore_2:
    case Bytecodes::_dstore_3:
    case Bytecodes::_astore_0:
    case Bytecodes::_astore_1:
    case Bytecodes::_astore_2:
    case Bytecodes::_astore_3:
    case Bytecodes::_pop:
    case Bytecodes::_pop2:
    case Bytecodes::_dup:
    case Bytecodes::_dup_x1:
    case Bytecodes::_dup_x2:
    case Bytecodes::_dup2:
    case Bytecodes::_dup2_x1:
    case Bytecodes::_dup2_x2:
    case Bytecodes::_swap:
    case Bytecodes::_iadd:
    case Bytecodes::_ladd:
    case Bytecodes::_isub:
    case Bytecodes::_lsub:
    case Bytecodes::_imul:
    case Bytecodes::_lmul:
    case Bytecodes::_ineg:
    case Bytecodes::_lneg:
    case Bytecodes::_fneg:
    case Bytecodes::_dneg:
    case Bytecodes::_ishl:
    case Bytecodes::_lshl:
    case Bytecodes::_ishr:
    case Bytecodes::_lshr:
    case Bytecodes::_iushr:
    case Bytecodes::_lushr:
    case Bytecodes::_iand:
    case Bytecodes::_land:
    case Bytecodes::_ior:
    case Bytecodes::_lor:
    case Bytecodes::_ixor:
    case Bytecodes::_lxor:
    case Bytecodes::_iinc:
    case Bytecodes::_i2l:
    case Bytecodes::_l2i:
    case Bytecodes::_i2b:
    case Bytecodes::_i2c:
    case Bytecodes::_i2s:
    case Bytecodes::_lcmp:
    case Bytecodes::_ireturn:
    case Bytecodes::_lreturn:
    case Bytecodes::_freturn:
    case Bytecodes::_dreturn:
    case Bytecodes::_areturn:
    case Bytecodes::_return:
    case Bytecodes::_wide:
      // These bytecodes are OK
      always_ok = true;
      break;
    }

    if (always_ok) {
      int index = i / 8;
      jubyte value = (jubyte)( ((juint)table[index]) | ((juint) 1 << (i % 8)) );
      table[index] = value;
    }
  }

  output->cr();
  output->print_cr("extern const unsigned char omit_frame_table[];");
  output->print   ("       const unsigned char omit_frame_table[] = {");

  for (i=0; i<256/8; i++) {
    if ((i % 8) == 0) {
      output->cr();
      output->print("    ");
    }
    output->print("0x%02x, ", table[i]);
  }
  output->cr();
  output->print_cr("};");
}
#endif // PRODUCT


bool Compiler::check_if_stack_frame_may_be_omitted() {

  if (ENABLE_WTK_PROFILER && !TestCompiler) {
    // it's a quick solution. Profiler doesn't work without stack frames.
    return false;
  }

  Method* m = method(); 

  if (has_loops() || m->access_flags().is_synchronized()
      || m->access_flags().has_monitor_bytecodes()) {
    // For simplicity, don't omit stack frame for any methods that have loops,
    // or need synchronization.
    return false;
  }

  if ((m->max_execution_stack_count()) > 4) {
    // Methods that have a lot of locals/stacks don't benefit too much 
    // from omitting stack frames.
    return false;
  }

  const int codesize = m->code_size();
  if (codesize > 20) {
    // Methods that are non-trivial don't benefit too much from omitting 
    // stack frames.
    return false;
  }

  const jubyte *table = (const jubyte*)omit_frame_table;

  for (int bci=0; bci<codesize; ) {
    Bytecodes::Code code = m->bytecode_at(bci);

    int index = ((jint)code) / 8;
    int mask  = 1 << (((jint)code) % 8);
    if ( (((jint)(table[index])) & mask) == 0 ) {
      // We treat these bytecodes as if they would need a stack frame.
      return false;
    }

    bci += Bytecodes::length_for(m, bci);
  }

  // We have a simple leaf method. Let's omit stack frames.
  return true;
}

void Compiler::begin_compile( JVM_SINGLE_ARG_TRAPS ) {
  // Mark the compiler as being outside any loops.
  mark_as_outside_loop();
  
  { // Compute the entry count for each bytecode index.
    bool has_loops;
    OopDesc* p = BasicBlock::compute_entry_counts(method(), has_loops
                                                  JVM_CHECK);
    set_entry_counts_table( p );
    set_has_loops( has_loops );
  }

  if (OmitLeafMethodFrames) {
    set_omit_stack_frame( check_if_stack_frame_may_be_omitted() );
  } else {
    set_omit_stack_frame( false );
  }

  const int code_size = method()->code_size();

  { // Allocate object array for the entry table
    OopDesc* p = Universe::new_obj_array_in_compiler_area(code_size JVM_CHECK);
    set_entry_table( p );
  }
  { // Allocate table describing various flags
    OopDesc* p = Universe::new_byte_array_in_compiler_area(code_size JVM_CHECK);
    set_bci_flags_table( p );
  }
  { // Instantiate a virtual stack frame for this method.    
    OopDesc* p = VirtualStackFrame::create(method() JVM_CHECK);
    set_frame( p );
  }

  // A different allocation table may be used depending on frame omission.
  RegisterAllocator::initialize();

  {
    COMPILER_PERFORMANCE_COUNTER_IN_BLOCK(method_entry);
    // Compile the method entry.
    code_generator()->method_entry(method() JVM_CHECK);
  }

  
#if ENABLE_NPCE && ENABLE_INTERNAL_CODE_OPTIMIZER
  {
    OopDesc* p = Universe::new_int_array_in_compiler_area(
                              npe_bytecode_count() JVM_CHECK);
    set_npe_table(p);
  }
#endif

#if ENABLE_REMEMBER_ARRAY_LENGTH & ARM
  // Preload the parameter of array type
  // also preload the array length
  code_generator()->preload_parameter(method());
#endif

  {    
    // Add the initial compilation continuation (bci = 0) to the
    // compilation queue.
    BinaryAssembler::Label unused;
    CompilationContinuation::Raw stub =
      CompilationContinuation::insert(0, unused JVM_CHECK);

    // A simple method that has no loops and no invocation will return
    // quickly. Let's save space by no generating the OSR entry.
    // In comp mode with OmitLeafMethodFrames off OSR entry is always generated
    bool need_osr_entry = has_loops() || !method()->is_leaf() ||
                          !(MixedMode || OmitLeafMethodFrames);

    if (GenerateROMImage) {
      // No need for OSR entry at first compilation continuation if we're
      // doing AOT compilation.
      need_osr_entry = false;
    }
    if (need_osr_entry) {
      stub().set_need_osr_entry();
    }
  }
  check_free_space(JVM_SINGLE_ARG_CHECK);
}

inline void Compiler::internal_compile( JVM_SINGLE_ARG_TRAPS ) {
  begin_compile( JVM_SINGLE_ARG_NO_CHECK );
  if (CURRENT_HAS_PENDING_EXCEPTION) {    
    // IMPL_NOTE: Not a good reason to mark the method as impossible_to_compile
    set_impossible_to_compile(method(),
      "not enough memory to start compilation");
    return;
  }
  process_compilation_queue( JVM_SINGLE_ARG_NO_CHECK );
}

inline void Compiler::suspend( void ) {
  CompilerState* suspended_state = &_suspended_compiler_state;
  suspended_state->allocate();
#if ENABLE_ISOLATES
  suspended_state->set_task_id(Task::current_id());
#endif
  code_generator()->save_state( suspended_state );
}

#if  ENABLE_INTERNAL_CODE_OPTIMIZER && ARM && ENABLE_CODE_OPTIMIZER
void  Compiler::begin_bound_literal_search() {
  _next_bound_literal  =  this->code_generator()->_first_literal;
}

BinaryAssembler::Label Compiler::get_next_bound_literal() {
  BinaryAssembler::Label label;
  for (; _next_bound_literal.obj();
    _next_bound_literal.set_obj( _next_bound_literal().next())) {
    if (_next_bound_literal().is_bound()) {
      label =  _next_bound_literal().label();
      _next_bound_literal.set_obj(_next_bound_literal().next());
      break;
    }
  }
  return label;
}


void Compiler::begin_pinned_entry_search() {
  _next_element = compilation_queue();
}

BinaryAssembler::Label Compiler::get_next_pinned_entry() {
  BinaryAssembler::Label label;
  while (!_next_element().is_null() && 
       (_next_element().type() !=CompilationQueueElement::osr_stub &&
       _next_element().type() !=CompilationQueueElement::entry_stub 
        ) ) {
    _next_element = _next_element().next();
  }
  if (_next_element().is_null() ) { 
    return label;
  }
  label =_next_element().entry_label(); 
  _next_element = _next_element().next();
  return label;
}
  
#if ENABLE_NPCE
void Compiler::get_npe_instructions(int start_offset) {
  UsingFastOops fast_oops;
  CompilationQueueElement::Fast ptr;
  ptr = compilation_queue();
  int tmp;
  while (!ptr().is_null() ) {
    if (ptr().type() == CompilationQueueElement::throw_exception_stub &&
       ( (ThrowExceptionStub) ptr()).get_rte() == 
           ThrowExceptionStub::rte_null_pointer &&
       !((ThrowExceptionStub) ptr()).is_persistent()) {
      tmp = ptr().entry_label().position();
      if (tmp >= start_offset) {
        _internal_code_optimizer.record_npe_ldrs( tmp>>2 ,0);
        
        if (( (NullCheckStub)ptr()).is_two_words()) {
          jint high_word = ((NullCheckStub) ptr()).high_word();
          _internal_code_optimizer.record_npe_ldrs(((tmp>>2) + high_word), 0);
        }
          
      }
#ifndef PRODUCT
      if (OptimizeCompiledCodeVerboseInternal) {
        tty->print_cr("\t\t[%d: is a NPE instruction]", 
          ptr().entry_label().position());
      }
#endif
    }
#ifndef PRODUCT
      if (OptimizeCompiledCodeVerboseInternal) {
        tty->print_cr("\t\t[%d: is a NOT a NPE instruction]", 
          ptr().entry_label().position());
      }
#endif
    
    ptr = ptr().next();
  }
}

void Compiler::patch_null_check_stubs() {
  UsingFastOops fast_oops;
  CompilationQueueElement::Fast ptr;
  int index;
  int old_offset;
  short new_offset;
  BinaryAssembler::Label label;
#ifndef PRODUCT  
  if (OptimizeCompiledCodeVerboseInternal) {
    tty->print_cr("\tenter patch_null_check_stubs ");
    tty->print_cr("\tNPE record count is %d", 
      _internal_code_optimizer.get_npe_record_count());
  }
#endif  
  for (index = 0; index < 
     _internal_code_optimizer.get_npe_record_count(); index++) {
    new_offset = _internal_code_optimizer.get_npe_scheduled_address(index);

    old_offset = _internal_code_optimizer.get_npe_cur_address( index);
    ptr = compilation_queue();
    while (!ptr().is_null()) {
#ifndef PRODUCT  
      if (OptimizeCompiledCodeVerboseInternal) {
        TTY_TRACE_CR(("\tCurrent CC label is %d",
          ptr().entry_label().position()));
      }
#endif  
      if (ptr().type() == CompilationQueueElement::throw_exception_stub  &&
        ((ThrowExceptionStub) ptr()).get_rte() == 
               ThrowExceptionStub::rte_null_pointer &&
        !((ThrowExceptionStub) ptr()).is_persistent()) {
        if (ptr().entry_label().position() == old_offset << 2) { 
          //fix for double and long
          if (((NullCheckStub)ptr()).is_two_words()) {
            int next_new_offset = 0;
            next_new_offset = 
              _internal_code_optimizer.get_npe_scheduled_address(index + 1);
            if (next_new_offset == 0) {
              next_new_offset = 
                _internal_code_optimizer.get_npe_cur_address(index + 1);
            }
            if ((new_offset == 0 && next_new_offset  < old_offset) ||
                 next_new_offset < new_offset) {
              new_offset = next_new_offset;
            }
            index++;
          }
          if (new_offset == 0) {  
            break;
          }
          label = ptr().entry_label();
          label.link_to(new_offset << 2);
          ptr().set_entry_label(label);
#ifndef PRODUCT  
          if (OptimizeCompiledCodeVerboseInternal) {
            tty->print("\tUpdate NPCE stub label old position is %d ", old_offset << 2);
            tty->print_cr("new position is %d", new_offset << 2);
          }
#endif  
          break;
        }
      }
      ptr = ptr().next();
    }
  }
}
#endif //ENABLE_NPCE
#endif //ENABLE_INTERNAL_CODE_OPTIMIZER

void Compiler::process_compilation_queue( JVM_SINGLE_ARG_TRAPS ) {
  // Prevent spending run time on compilation
  // for a specified number of subsequent ticks:
  Universe::_compilation_abstinence_ticks = CompilationAbstinenceTicks;
  _failure = out_of_memory;

  // Tell OS to interrupt compilation after a platform-specific amount
  // of time. This avoid long compilation pauses when compiling big methods.
  Os::start_compiler_timer();
  UsingFastOops fast_oops;
  CompilationQueueElement::Fast element;

  // Compile until the continuation queue is empty
  while (!is_compilation_queue_empty()) {
    // Get and compile the first element from the compile queue.
    element = current_compilation_queue_element();

    const bool finished_one_element = element().compile(JVM_SINGLE_ARG_CHECK);
    if (finished_one_element) {
      RegisterAllocator::guarantee_all_free();
      clear_current_element();
      element().free();
    } else {
      // We will resume compilation of the current element in the next
      // call to Compiler::resume()
    }

    check_free_space(JVM_SINGLE_ARG_CHECK);

#if ENABLE_INLINE && ARM
    if ( is_inline() <= 0) {
      if (!GenerateROMImage) {
        if ((Os::check_compiler_timer() || ExcessiveSuspendCompilation)
            && !is_compilation_queue_empty()) {
          _failure = out_of_time;
          suspend();
          return;
        }
      }
    }
#else
    if (!GenerateROMImage) {
      if ((Os::check_compiler_timer() || ExcessiveSuspendCompilation)
          && !is_compilation_queue_empty()) {
        _failure = out_of_time;
        suspend();
        return;
      }
    }
#endif
  }
#if ENABLE_INLINE && ARM
  if (is_inline() > 0) return;
#endif
  {
    COMPILER_PERFORMANCE_COUNTER_IN_BLOCK(sentinel);
    // Insert terminating sentinel.
    code_generator()->generate_sentinel();
  }

  check_free_space( JVM_SINGLE_ARG_CHECK );

#if ENABLE_APPENDED_CALLINFO
  _callinfo_writer.commit_table();
#endif

  // Shrink compiled method object to correct size
  const jint code_size = code_generator()->code_size();
  const jint relocation_size = code_generator()->relocation_size();

  INCREMENT_COMPILER_PERFORMANCE_COUNTER(relocation, relocation_size);
  CompiledMethod::Fast result = current_compiled_method();
  result().shrink(code_size, relocation_size);
#ifndef PRODUCT
  if( Verbose || PrintCompilation ) {
    if (VerbosePointers) {
      TTY_TRACE_CR((" [compiled method (0x%x) size=%d, code=%d, reloc=%d, "
                    "entry=0x%x]",
        result().obj(), result().object_size(), code_size, relocation_size,
        result().entry() ));
    } else {
      TTY_TRACE_CR((" [compiled method size=%d, code=%d, reloc=%d]",
        result().object_size(), code_size, relocation_size ));
    }
  }

  if( PrintCompiledCode && OptimizeCompiledCodeVerbose ) {
    tty->print_cr("Before Optimization:");
    result().print_code_on(tty);
  }
#endif

#if !ENABLE_INTERNAL_CODE_OPTIMIZER
  optimize_code(JVM_SINGLE_ARG_CHECK);
#endif

#ifdef FUNNY_CROSS_COMPILER
  // This is a fix used only for testing new ports.
  // We create a compiler that generates code for the wrong platform!
  // This lets us compile the code and print it, but we can't possibly run it.
  set_impossible_to_compile( method(), "funny cross-compiler" );
  return;
#endif

  if( !GenerateROMImage ) {
    CompiledMethodCache::insert( (CompiledMethodDesc*) result().obj() );
  }

#ifndef PRODUCT
  if( PrintCompiledCode ) {
    if ( OptimizeCompiledCodeVerbose ) {
      tty->print_cr("After Optimization:");
    }
    result().print_code_on(tty);
  }
#endif

#if ENABLE_JVMPI_PROFILE 
  // Set the actual generated code size.
  if ( result.not_null() ) {
   ((CompiledMethodDesc*)result().obj())->jvmpi_set_code_size(
                                          code_generator()->code_size());
  }
  
  // Send the compiled method load event.
  if(UseJvmpiProfiler && 
    JVMPIProfile::VMjvmpiEventCompiledMethodLoadIsEnabled() &&
    result.not_null()) {
    JVMPIProfile::VMjvmpiPostCompiledMethodLoadEvent(result);
  }
#endif

  if (InstallCompiledCode) {   
    result().flush_icache();
    method()->set_compiled_execution_entry(result().entry());
    GUARANTEE(method()->has_compiled_code(), "check bit");
  } else {
    method()->set_default_interpreter_entry();
  }

  _failure = none;
}

#if ENABLE_INLINE && ARM
void Compiler::restore_fast_globals() {
  jvm_fast_globals.compiler_method = _state.method();
  jvm_fast_globals.compiler_frame = _state.frame();
  jvm_fast_globals.compiler_closure = &_closure;
}
void Compiler::compile_method_for_inline(jint old_code_offset, bool is_static,
                                   VirtualStackFrame* frame, 
                                   CompiledMethod* result, CompilerState* caller_state  JVM_TRAPS) {
  // Mark the compiler as being outside any loops.
  mark_as_outside_loop();

  { 
    // Compute the entry count for each bytecode index.
    bool has_loops;
    OopDesc* p = BasicBlock::compute_entry_counts(method(), has_loops
                                                  JVM_CHECK);
    set_entry_counts_table( p );
    set_has_loops( has_loops );
  }

  const int code_size = method()->code_size();

  {
    // Allocate object array for the entry table
    OopDesc* p = Universe::new_obj_array_in_compiler_area(code_size JVM_CHECK);
    set_entry_table( p );
  }
  
  { 
    // Allocate table describing various flags
    OopDesc* p = Universe::new_byte_array_in_compiler_area(code_size JVM_CHECK);
    set_bci_flags_table( p );
  }
  {
     UsingFastOops fast_oops;
     VirtualStackFrame::Fast frame_clone = frame->clone(JVM_SINGLE_ARG_CHECK);

     set_frame(&frame_clone);
  }
  CodeGenerator code_generator(this);
  set_code_generator(&code_generator);
  code_generator.set_compiled_method(result);
  code_generator.set_code_size(old_code_offset);


  
  set_caller_virtual_stack_pointer(frame->virtual_stack_pointer());
  {    
    // Add the initial compilation continuation (bci = 0) to the
    // compilation queue.
    BinaryAssembler::Label unused;
    CompilationContinuation::Raw stub =
      CompilationContinuation::insert(0, unused JVM_CHECK);
    
  }

  code_generator.restore_state(caller_state);
  
  process_compilation_queue(JVM_SINGLE_ARG_CHECK);
  if (is_static == true) {
    BinaryAssembler::Label inline_label = get_inline_return_label();
    if (!inline_label.is_unused()) {
      code_generator.bind(inline_label);
    }
  }
  code_generator.save_state(caller_state);
}
#endif  

ReturnOop Compiler::try_to_compile(Method* method, const int active_bci,
                                   const int compiled_code_factor
                                   JVM_TRAPS) {
  OopDesc* result;
  CompilationQueueElement::reset_pool();
  ObjectHeap::save_compiler_area_top();
  {
    Compiler compiler( method, active_bci );
    compiler.init_performance_counters(false);
    result = compiler.allocate_and_compile(compiled_code_factor         
                                           JVM_NO_CHECK);
    compiler.update_performance_counters(false, result);
    Thread::clear_current_pending_exception();
  }

  if (!is_suspended()) {
    // At this point, no handles and no heap objects should point to
    // any space higher than <result> (where temporary objects such as
    // CompilationQueueElements were allocated during
    // compilation). The following call will update
    // ObjectHeap::_compiler_area_top and effectively discard all such
    // temporary objects. If the compilation failed (result=0)
    // _compiler_area_top will be restored to the value before the
    // last compilation started.
    terminate( result );
  }
  return result;
}

inline bool Compiler::reserve_compiler_area(size_t compiled_method_size) {
  // We need about 75 bytes of compiler data buffer per byte of Java
  // bytecode for small methods (less than ~1000 bytes). The factor
  // for larger methods is typically smaller because we can re-use
  // CompilationQueueElements.
  const size_t factor = 75;
  size_t temp_data_size = factor * method()->code_size();
  size_t max_temp_data = HeapCapacity * CompilerAreaPercentage / 100 / 4;
  if (temp_data_size > max_temp_data) {
    // Don't use more than a quarter of the compiler area to compile any
    // method. This means very big methods would fail to compile and
    // would be marked as impossible to compile.
    temp_data_size = max_temp_data;
  }
  const size_t needed = compiled_method_size + temp_data_size;

  // IMPL_NOTE: make sure that we don't thrash with compiler_area_collect if
  // we're interpreting a large method but we can't collect enough space
  // at every timer tick.
  return ObjectHeap::compiler_area_soft_collect(needed) >= needed;
}

#if !ENABLE_INTERNAL_CODE_OPTIMIZER      
inline void Compiler::optimize_code( JVM_SINGLE_ARG_TRAPS ) {
#if ENABLE_CODE_OPTIMIZER
  if( OptimizeCompiledCode ) {
#if ENABLE_PERFORMANCE_COUNTERS
    JvmTimer opt_timer;
    opt_timer.start();
#endif
    UsingFastOops fast_oops;
    CompiledMethod::Fast result = current_compiled_method();

    int* code_begin = (int*)result().entry();
    int* code_end = (int*)(result().entry() + code_generator()->code_size());
      
    CodeOptimizer optimizer(&result, code_begin, code_end);
    optimizer.optimize_code(JVM_SINGLE_ARG_NO_CHECK); 
    if(CURRENT_HAS_PENDING_EXCEPTION) {
      Thread::clear_current_pending_exception();
      if( OptimizeCompiledCodeVerbose ) {
        TTY_TRACE_CR(( "Code optimization FAILED - out of memory" ));
      }
    }
#if ENABLE_PERFORMANCE_COUNTERS
    opt_timer.stop();
    if( result().not_null() ) {
      if( PrintCompilation ) {
        tty->print_cr(" [optimized method size=%d, code=%d "
                      "(%.1fms = %.0f bytes/sec)]",
                      result().size(), code_generator()->code_size(),
                      jvm_f2d(jvm_fmul(jvm_d2f(opt_timer.seconds()), 1000.0f)),
                      jvm_f2d(jvm_fdiv(jvm_i2f(method()->code_size()),
                                     jvm_d2f(opt_timer.seconds()))));
      }
    }
#endif
  }
#else 
  JVM_IGNORE_TRAPS;
#endif /* ENABLE_CODE_OPTIMIZER*/
}
#endif

void Compiler::abort_suspended_compilation( void ) {
  if( is_suspended() ) {
    GUARANTEE(!is_active(), "sanity");
    _failure = none;
    terminate( NULL );
  }
}

void Compiler::abort_active_compilation(bool is_permanent JVM_TRAPS) {
  GUARANTEE(is_active(), "sanity");
  if (is_permanent) {
    set_impossible_to_compile(method(), "compilation aborted permanently");
  }
  Throw::out_of_memory_error(JVM_SINGLE_ARG_THROW);
}

inline void Compiler::restore_and_compile( JVM_SINGLE_ARG_TRAPS ) {
  CodeGenerator code_generator(this, suspended_compiler_state() );
  suspended_compiler_state()->dispose();
  process_compilation_queue( JVM_SINGLE_ARG_NO_CHECK );
}

// Resume a compilation that has been suspended.
ReturnOop Compiler::resume_compilation(Method *method JVM_TRAPS) {
  EventLogger::log(EventLogger::COMPILE_START);

  OopDesc* result;

  if (PrintCompilation) {
    TTY_TRACE(("<c>"));
  }
  {
    // Put the handles in a separate scope, so when we call
    // ObjectHeap::update_compiler_area_top() we have no more handles pointing
    // into unused space in compiler_area.
    Compiler compiler(method, 0);
    compiler.init_performance_counters(true);
    compiler.restore_and_compile( JVM_SINGLE_ARG_NO_CHECK );
    Thread::clear_current_pending_exception();
    result = _failure == none ? current_compiled_method()->obj() : NULL;
    compiler.update_performance_counters(true, result);
  }
  switch( _failure ) {
    default:
      // IMPL_NOTE: The method may successfully compile later
      // when more memory will be available
      set_impossible_to_compile(method, "compiler area 2");
      // intentionally no break; here
    case none:
      terminate( result );
    case out_of_time:
      break;
  }

  EventLogger::log(EventLogger::COMPILE_END);

  return result;
}

#ifndef PRODUCT

void Compiler::append_compilation_history() {
  CompilationHistory *h = 
      (CompilationHistory*)OsMemory_allocate(sizeof(CompilationHistory));
  if (_history_head == NULL) {
    _history_head = _history_tail = h;
  } else {
    _history_tail->_next = h;
    _history_tail = h;
  }
  h->_next = NULL;
  method()->print_name_to(h->_method_name, sizeof(h->_method_name));
}

void Compiler::print_compilation_history( void ) {
  int i = 0;
  CompilationHistory* h = _history_head;
  while( h ) {
    tty->print_cr("Compiled: #%d %s", i++, h->_method_name);
    CompilationHistory* next = h->_next;
    OsMemory_free( h );
    h = next;
  }
  _history_head = _history_tail = h;
}

#endif // PRODUCT

#if USE_DEBUG_PRINTING
void CompilerStatic::print_on(Stream *st) {
  #define COMPILER_STATIC_FIELDS_PRINT( type, name ) \
          st->print_cr("%-30s = %d", STR(name), _##name);
  COMPILER_STATIC_FIELDS_DO(COMPILER_STATIC_FIELDS_PRINT);
}

void Compiler::print_on(Stream *st) {
  st->print_cr("Compiler::_current = 0x%08x", _current);
  _state.print_on(st);
  st->print_cr("Compiler::_closure = 0x%08x", &_closure);
  _closure.print_on(st);
}

void Compiler::p() {
  _current->print_on(tty);
}
#endif


#if ENABLE_PERFORMANCE_COUNTERS
void Compiler::init_performance_counters(bool is_resume) {
  _start_time = Os::elapsed_counter();
  _mem_before_compile = 
      ObjectHeap::used_memory() + jvm_perf_count.total_bytes_collected;
  _gc_before_compile = jvm_perf_count.num_of_gc;

  if (is_resume) {
    jvm_perf_count.compilation_resume_count ++;
  }
}

void Compiler::update_performance_counters(bool /*is_resume*/, OopDesc *result)
  const
{
  {
    const jlong elapsed = Os::elapsed_counter() - _start_time;
    jvm_perf_count.total_compile_hrticks += elapsed;
    if (jvm_perf_count.max_compile_hrticks < elapsed) {
      jvm_perf_count.max_compile_hrticks = elapsed;
      //Symbol n = method()->name();
      //tty->print("<<<%d (%d)", (jint)(elapsed), is_resume);
      //n.print_symbol_on(tty);
      //tty->print(">>>");
    }
  }

  jvm_perf_count.num_of_compilations ++;
  {
    const int mem_used = ObjectHeap::used_memory() +
      jvm_perf_count.total_bytes_collected - _mem_before_compile;
    jvm_perf_count.total_compile_mem += mem_used;
    if (jvm_perf_count.max_compile_mem < mem_used) {
      jvm_perf_count.max_compile_mem = mem_used;
    }
  }

  if( result ) {
    {
      int code_size = ((CompiledMethodDesc*)result)->object_size();
      jvm_perf_count.total_compiled_methods += code_size;
      if (jvm_perf_count.max_compiled_method < code_size) {
        jvm_perf_count.max_compiled_method = code_size;
      }
    }
    {
      const int code_size = method()->code_size();
      jvm_perf_count.total_compiled_bytecodes += code_size;
      if (jvm_perf_count.max_compiled_bytecodes < code_size) {
        jvm_perf_count.max_compiled_bytecodes = code_size;
      }
    }
    jvm_perf_count.num_of_compilations_finished ++;
  }

  jvm_perf_count.num_of_compiler_gc +=
    jvm_perf_count.num_of_gc - _gc_before_compile;
}

#if ENABLE_DETAILED_PERFORMANCE_COUNTERS
Compiler_PerformanceCounters comp_perf_counts;

#define PRINT_COMPILER_SIZE_COUNTER(name, level)                    \
  JVM::P_INT(true, #name " size", comp_perf_counts. name ## _size); \
  JVM::P_INT(true, #name " avg ",                                   \
             comp_perf_counts. name ## _count ?                     \
             comp_perf_counts. name ## _size /                      \
             comp_perf_counts. name ## _count : 0); 

#define PRINT_COMPILER_COUNT_COUNTER(name, level) \
  JVM::P_INT(true, #name " count", comp_perf_counts. name ## _count);

#define PRINT_COMPILER_TIME_COUNTER(name, level) \
  JVM::P_HRT(true, #name " time", comp_perf_counts. name ## _time);

void Compiler::print_detailed_performance_counters() {
  tty->cr();

  FOR_ALL_COMPILER_PERFORMANCE_COUNTERS(PRINT_COMPILER_SIZE_COUNTER)

  tty->cr();

  FOR_ALL_COMPILER_PERFORMANCE_COUNTERS(PRINT_COMPILER_COUNT_COUNTER)

  tty->cr();

  FOR_ALL_COMPILER_PERFORMANCE_COUNTERS(PRINT_COMPILER_TIME_COUNTER)
}

#undef PRINT_COMPILER_SIZE_COUNTER
#undef PRINT_COMPILER_COUNT_COUNTER
#undef PRINT_COMPILER_TIME_COUNTER

#endif // ENABLE_DETAILED_PERFORMANCE_COUNTERS

#endif // ENABLE_PERFORMANCE_COUNTERS

#endif // ENABLE_COMPILER
