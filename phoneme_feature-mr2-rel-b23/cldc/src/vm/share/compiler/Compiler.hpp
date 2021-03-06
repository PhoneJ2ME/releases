/*
 *   
 *
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

#if ENABLE_COMPILER

// The compiler translates bytecodes into native instructions.

class CompilerStatePointers {
public:
  #define FIELD( type, name ) \
    type##Desc* _##name;              \
    inline type* name ( void ) const { return (type*) &_##name; } \
    inline void set_##name ( type* val ) { _##name = (type##Desc*) val->obj(); }

#if defined(ARM) || defined(HITACHI_SH) || ENABLE_THUMB_COMPILER
  FIELD( Oop, first_literal             )
  FIELD( Oop, first_unbound_literal     )
  FIELD( Oop, last_literal              )
#endif

#if ENABLE_THUMB_COMPILER
  FIELD( Oop, first_unbound_branch_literal  )
  FIELD( Oop, last_unbound_branch_literal   )
#endif

#undef FIELD

  static int pointer_count( void ) {
    return sizeof(CompilerStatePointers) / sizeof(OopDesc*);
  }
};

class CompilerState: public CompilerStatePointers  {
public:
  #define FIELD( type, name ) \
    type _##name;             \
    inline type name ( void ) const { return _##name; } \
    inline void set_##name ( const type val ) { _##name = val; }

  FIELD( int,  code_size                 )
  FIELD( int,  current_relocation_offset )
  FIELD( int,  current_code_offset       )
  FIELD( int,  current_oop_relocation_offset )
  FIELD( int,  current_oop_code_offset       )
#if ENABLE_ISOLATES
  // The ID of the task that started this compilation. Compilation uses
  // information that are specific to a task's context -- for example, 
  // class_ids. Hence, a compilation must be resumed under the correct 
  // task context.
  FIELD( int, task_id                   )
#endif

#if defined(ARM) || defined(HITACHI_SH) || ENABLE_THUMB_COMPILER
  FIELD( int, unbound_literal_count                     )
  FIELD( int, code_offset_to_force_literals             )
  FIELD( int, code_offset_to_desperately_force_literals )
#endif

#if ENABLE_THUMB_COMPILER
  FIELD( int, unbound_branch_literal_count              )
#endif

#undef FIELD

  bool _valid;

  bool valid ( void ) const { return _valid; }
  void allocate ( void ) { _valid = true;   }
  void dispose  ( void ) { _valid = false;  }

  void oops_do( void do_oop(OopDesc**) );
};

#define COMPILER_INSTANCE_HANDLES  \
  FIELD( Method,                  method                  ) \
  FIELD( CompilationQueueElement, compilation_queue       ) \
  FIELD( CompilationQueueElement, current_element         ) \
  ARRAY( ObjArray,                entry_table             ) \
  ARRAY( TypeArray,               entry_counts_table      ) \
  ARRAY( TypeArray,               bci_flags_table         ) 

#if ENABLE_INTERNAL_CODE_OPTIMIZER && ENABLE_NPCE
//address of all the null point related LDR(STR)
//this will be used to update the abort_point bitmap during 
//code scheduling
#define  SCHEDULER_HANDLES \
  ARRAY( TypeArray,               null_point_exception_ins_table )
#else
#define  SCHEDULER_HANDLES
#endif

#define COMPILER_CONTEXT_HANDLES \
      COMPILER_INSTANCE_HANDLES \
      SCHEDULER_HANDLES

class CompilerContextPointers {
  #define ARRAY( type, name ) DEF( Array, type, name )
  #define FIELD( type, name ) DEF( type,  type, name )
  #define DEF( repr, type, name ) \
    repr##Desc* _##name;    \
    type* name ( void ) const           { return (type*) &_##name;     }  \
    void clear_##name ( void )          { _##name = NULL;              }  \
    void set_##name ( OopDesc* val )    { _##name = (repr##Desc*) val; }  \
    void set_##name ( const type* val ) { set_##name( val->obj() );    }

public:
  COMPILER_CONTEXT_HANDLES

  #undef ARRAY
  #undef FIELD
  #undef DEF

  static int pointer_count( void ) {
    return sizeof(CompilerContextPointers) / sizeof(OopDesc*);
  }
};

#define GENERIC_COMPILER_CONTEXT_FIELDS_DO(template) \
        template( Compiler*, parent                        )   \
        template( bool,      in_loop                       )   \
        template( bool,      has_loops                     )   \
        template( int,       saved_bci                     )   \
        template( int,       saved_num_stack_lock_words    )   \
        template( int,       local_base                    )

#if ENABLE_INLINE
#define INLINER_COMPILER_CONTEXT_FIELDS_DO(template)  \
        template( int, inline_return_label_encoding )
#else
#define INLINER_COMPILER_CONTEXT_FIELDS_DO(template)
#endif

#if ENABLE_CODE_OPTIMIZER && ENABLE_NPCE
#define SCHEDULER_COMPILER_CONTEXT_FIELDS_DO(template) \
        template( int, codes_can_throw_null_point_exception ) \
        template( int, null_point_record_counter)
#else
#define SCHEDULER_COMPILER_CONTEXT_FIELDS_DO(template) 
#endif

#define COMPILER_CONTEXT_FIELDS_DO(template) \
        GENERIC_COMPILER_CONTEXT_FIELDS_DO(template) \
        INLINER_COMPILER_CONTEXT_FIELDS_DO(template) \
        SCHEDULER_COMPILER_CONTEXT_FIELDS_DO(template) 

class CompilerContext: public CompilerContextPointers {
public:
  #define FIELD( type, name ) \
    type _##name;             \
    type name         ( void )     { return _##name; } \
    void set_##name   ( type val ) { _##name = val;  }

  COMPILER_CONTEXT_FIELDS_DO(FIELD)

  #undef FIELD

  bool valid ( void ) const { return method()->not_null(); }

  void oops_do( void do_oop(OopDesc**) );
  void cleanup( void );

#if USE_DEBUG_PRINTING
  void print_on(Stream *st);
#endif
};

#define COMPILER_STATIC_HANDLES  \
  FIELD( CompiledMethod,          current_compiled_method ) \
  FIELD( VirtualStackFrame,       frame                   ) \
  FIELD( VirtualStackFrame,       conforming_frame        ) \
  FIELD( VirtualStackFrame,       cached_preserved_frame  )

class CompilerStaticPointers {
public:
  #define FIELD( type, name )  \
    type##Desc* _##name;       \
    type* name        ( void )            { return (type*)&_##name;     } \
    void clear_##name ( void )            { _##name = NULL;             } \
    void set_##name   ( OopDesc* val )    { _##name = (type##Desc*)val; } \
    void set_##name   ( const type* val ) { set_##name( val->obj() );   }

  COMPILER_STATIC_HANDLES

  #undef FIELD

  OopDesc* _rte_handlers[ThrowExceptionStub::number_of_runtime_exceptions];

  static int pointer_count( void ) {
    return sizeof(CompilerStaticPointers) / sizeof(OopDesc*);
  }

  bool valid ( void ) const { return _current_compiled_method != NULL; }

  void oops_do( void do_oop(OopDesc**) );
  void cleanup( void );
};

#define COMPILER_STATIC_FIELDS_DO(template)  \
        template( Compiler*, root                    ) \
        template( Compiler*, current                 ) \
        template( bool,      omit_stack_frame        )

class CompilerStatic: public CompilerStaticPointers {
public:
  #define DECLARE_FIELD( type, name ) \
          type _##name;
  COMPILER_STATIC_FIELDS_DO(DECLARE_FIELD)

#if USE_DEBUG_PRINTING
  void print_on(Stream *st);
#endif
};

class Compiler: public StackObj {
 private:
  // The compiler state.
  static CompilerStatic _state;

  // Info used to tune down compilation for smoother animation.
  static jlong          _estimated_frame_time;
  static jlong          _last_frame_time_stamp;

 public:
  #define DEFINE_ACCESSOR( type, name ) \
    static type name       ( void )          { return _state._##name;    } \
    static void set_##name ( type name )     { _state._##name = name;    }
  COMPILER_STATIC_FIELDS_DO(DEFINE_ACCESSOR)

  #undef DEFINE_ACCESSOR

  #define FIELD( type, name ) \
    static type* name       ( void )         { return _state.name();     } \
    static void clear_##name( void )         { _state.clear_##name();    } \
    static void set_##name( OopDesc* val )   { _state.set_##name( val ); } \
    static void set_##name( const type* val ){ _state.set_##name( val ); }

  COMPILER_STATIC_HANDLES

  #undef FIELD

  #define DEFINE_ACCESSOR( type, name ) \
    type name                  ( void )      { return _context.name();   } \
    void set_##name            ( type name ) { _context.set_##name(name);} \
    static type current_##name ( void )      { return current()->name(); }
  COMPILER_CONTEXT_FIELDS_DO(DEFINE_ACCESSOR)

  #undef DEFINE_ACCESSOR

  #define ARRAY( type, name ) FIELD( type, name )
  #define FIELD( type, name ) \
    type* name       ( void )            { return _context.name();     } \
    void clear_##name( void )            { _context.clear_##name();    } \
    void set_##name  ( OopDesc* val )    { _context.set_##name( val ); } \
    void set_##name  ( const type* val ) { _context.set_##name( val ); } \
    static type* current_##name ( void ) { return current()->name();   }

  COMPILER_CONTEXT_HANDLES

  #undef FIELD

  // Constructor and deconstructor.
  Compiler( Method* method, const int active_bci );

#ifndef PRODUCT
  // Create a dummy compiler that does nothing, but just make is_active()
  // to true.
  Compiler();
#endif
  ~Compiler();

  // Called during VM start-up
  static void initialize();

  // Compiles the method and returns the result.
  // ^CompiledMethod
  static ReturnOop compile(Method* method, int active_bci JVM_TRAPS);

  // Resume a compilation that has been suspended.
  static ReturnOop resume_compilation(Method* method JVM_TRAPS);

  // Abort current suspended compilation.
  static void abort_suspended_compilation( void );

  // Abort the current active compilation. This method must be called
  // when compilation is actually taking place. It's usually used
  // during the development of the compiler to stop compilation when
  // an unimplemented feature is used.
  static void abort_active_compilation(bool is_permanent JVM_TRAPS);

  static CodeGenerator* code_generator( void ) {
    return jvm_fast_globals.compiler_code_generator;
  }

  void set_code_generator(CodeGenerator* value) {
    _closure.set_code_generator(value);
    jvm_fast_globals.compiler_code_generator = value;
  }

  static BytecodeCompileClosure* closure( void ) {
    return jvm_fast_globals.compiler_closure;
  }

  static ReturnOop get_cached_preserved_frame( void ) {
    const ReturnOop result = cached_preserved_frame()->obj();
    clear_cached_preserved_frame();
    return result;
  }

  // Accessors for the compilation queue.
  ReturnOop current_compilation_queue_element( void ) {
    ReturnOop p = (CompilationQueueElementDesc*)current_element()->obj();
    if( !p ) {
      p = compilation_queue()->obj();
      set_compilation_queue( ((CompilationQueueElementDesc*)p)->_next );
      set_current_element( p );
    }
    return p;
  }

  CompilerContext* context() {
    return &_context;
  }

#if ENABLE_NPCE
  //get the exception stub whose's entry label is still unset.
  //the function will find the stub and let the store_to_add_xx() to fill the 
  //address in LDR instr  in it.
  CompilationQueueElementDesc* get_unlinked_exception_stub(jint bci){
    CompilationQueueElement::Raw next_element = compilation_queue()->obj();
    while( ! next_element().is_null()){
      //This function will iterator the new created stub and
      //Try to found the unlinked null exception stub.
      if( next_element().bci() == bci && 
          next_element().type() == 
          CompilationQueueElement::throw_exception_stub &&
          ((ThrowExceptionStub) next_element()).get_rte() ==
          ThrowExceptionStub::rte_null_pointer
          ){
              return (CompilationQueueElementDesc *) next_element().obj();
      }
      next_element = next_element().next();
    };

    return NULL;
  }

#if ENABLE_INTERNAL_CODE_OPTIMIZER
  //Null pointer exception accessor
  void record_null_point_exception_inst( int offset ) {
    int index = null_point_record_counter();
    null_point_exception_ins_table()->int_at_put( index++, offset); 
    set_null_point_record_counter(index);     
  }

  //return the null point related instr indexed by index parameter
  int null_point_exception_abort_point( int index) {
    return null_point_exception_ins_table()->int_at( index);
  }

  //record the instr offset of those null point stubs into a npe_table.
  //we will track the offset changing of those instr during scheduling and
  //update the entry label of those instr after scheduling.
  void record_instr_offset_of_null_point_stubs(int start_offset);

  //update the entry lable of those null point stubs base on the information
  //of npe_table after scheduling
  void update_null_check_stubs();
#endif // ENABLE_INTERNAL_CODE_OPTIMIZER 
#endif // ENABLE_NPCE 

#if ENABLE_LOOP_OPTIMIZATION && ARM
  //get the first compilation queue item.
  ReturnOop get_first_compilation_queue_element() {
    return compilation_queue()->obj();
  }
  
  //get the next element in the current compilation queue. 
  ReturnOop get_next_compilation_queue_element(CompilationQueueElement *
                                               current_elem) {
    return current_elem->next();
  }
#endif //#if ENABLE_LOOP_OPTIMIZATION && ARM

  void insert_compilation_queue_element(CompilationQueueElement* value) {
    value->set_next( compilation_queue() );
    set_compilation_queue( value );
  }

  bool is_compilation_queue_empty( void ) {
    return compilation_queue()->is_null() && current_element()->is_null();
  }

  // Entry counts accessor.
  int entry_count_for(const jint bci)  {
    return entry_counts_table()->ubyte_at(bci);
  }

  bool exception_has_osr_entry(const jint bci) {
    return (bci_flags_table()->byte_at(bci) & 
            Method::bci_exception_has_osr_entry) != 0;
  }

  void set_exception_has_osr_entry(const jint bci) {
    bci_flags_table()->byte_at_put(bci,
      (jbyte) (bci_flags_table()->byte_at(bci) | 
               Method::bci_exception_has_osr_entry));
  }

  bool is_branch_taken(const jint bci) {
    return (bci_flags_table()->byte_at(bci) & 
            Method::bci_branch_taken) != 0;
  }

  void set_branch_taken(const jint bci) {
    bci_flags_table()->byte_at_put(bci,
      (jbyte) (bci_flags_table()->byte_at(bci) | 
               Method::bci_branch_taken));
  }

  // Entry accessor.
  ReturnOop entry_for(const jint bci)  {
    return entry_table()->obj_at(bci);
  }
  void set_entry_for(const jint bci, Entry* entry) {
    entry_table()->obj_at_put(bci, entry);
  }
  bool has_entry_for(const jint bci)  {
    return entry_for(bci) != NULL;
  }

  bool method_aborted_for_exception_at(const int bci) {
    const AccessFlags flags = method()->access_flags();
    return !( flags.is_synchronized()
           || flags.has_monitor_bytecodes()
           || method()->exception_handler_exists_for(bci)
           || _debugger_active );
  }

  // Tells whether is compiler is active
  static bool is_active( void ) { return current() != NULL; }

  // Support for sharing of exception thrower stubs.
  typedef ThrowExceptionStub::RuntimeException RuntimeException;
  static ReturnOop rte_handler(const RuntimeException rte) {
    return _state._rte_handlers[rte];
  }
  static void set_rte_handler(const RuntimeException rte, OopDesc* value) {
    _state._rte_handlers[rte] = value;
  }
#if ENABLE_INLINE
  void internal_compile_inlined( Method::Attributes& attributes JVM_TRAPS );

  BinaryAssembler::Label inline_return_label() {
    BinaryAssembler::Label label;
    label._encoding = inline_return_label_encoding();
    return label;
  }

  void set_inline_return_label(BinaryAssembler::Label& label) {
    set_inline_return_label_encoding(label._encoding);
  }

 private:
  ReturnOop parent_frame() {
    GUARANTEE(is_inlining(), "Can only be called during inlining");
    Compiler* parent_compiler = parent();
    GUARANTEE(parent_compiler != NULL, "Cannot be null when inlining");
    GUARANTEE(parent_compiler != this, "Sanity");
    CompilationQueueElement::Raw parent_element = 
      parent_compiler->current_element();
    GUARANTEE(parent_element.not_null(), "Cannot be null when inlining");
    return parent_element().frame();
  }

  void set_parent_frame(VirtualStackFrame* frame) {
    GUARANTEE(is_inlining(), "Can only be called during inlining");
    Compiler* parent_compiler = parent();
    GUARANTEE(parent_compiler != NULL, "Cannot be null when inlining");
    GUARANTEE(parent_compiler != this, "Sanity");
    CompilationQueueElement::Raw parent_element = 
      parent_compiler->current_element();
    GUARANTEE(parent_element.not_null(), "Cannot be null when inlining");
    parent_element().set_frame(frame);
  }

  void clear_parent_frame() {
    VirtualStackFrame::Raw null_frame;
    set_parent_frame(&null_frame);
  }
 public:
#endif
  static int bci( void ) {
    return jvm_fast_globals.compiler_bci;
  }
  static void set_bci( int bci ) {
    jvm_fast_globals.compiler_bci = bci;
  }

  static int  num_stack_lock_words(void) {
    return jvm_fast_globals.num_stack_lock_words;
  }
  static void set_num_stack_lock_words(int num_lock_words) {
    jvm_fast_globals.num_stack_lock_words = num_lock_words;
  }

  static bool is_in_loop           ( void ) { 
    return Compiler::current()->in_loop();     
  }
  static void mark_as_in_loop      ( void ) { 
    Compiler::current()->set_in_loop( true );  
  }
  static void mark_as_outside_loop ( void ) { 
    Compiler::current()->set_in_loop( false ); 
  }

  static bool is_inlining( void ) { 
    return current() != root();
  }

  enum CompilationFailure {
    none,
    reservation_failed,
    out_of_time,
    out_of_memory,
    out_of_stack
  };

  static CompilationFailure _failure;
  static void print_compilation_history() PRODUCT_RETURN;

 private:
  static CompilerState _suspended_compiler_state;
  static CompilerContext _suspended_compiler_context;
 public:
  static void on_timer_tick(bool is_real_time_tick JVM_TRAPS);
  static void process_interpretation_log();
  static void set_hint(int hint);

  static CompilerState* suspended_compiler_state( void ) {
    return &_suspended_compiler_state;
  }
  static CompilerContext* suspended_compiler_context( void ) {
    return &_suspended_compiler_context;
  }

  static bool is_suspended ( void ) {
    return suspended_compiler_state()->valid();
  }

  static void oops_do( void do_oop(OopDesc**) );

#if ENABLE_PERFORMANCE_COUNTERS && ENABLE_DETAILED_PERFORMANCE_COUNTERS
  static void print_detailed_performance_counters();
#else 
  static void print_detailed_performance_counters() {}
#endif
 private:
#if ENABLE_INTERNAL_CODE_OPTIMIZER
  InternalCodeOptimizer _internal_code_optimizer;
  FastOopInStackObj    __must_be_first_item__;
#endif

  // The compiler closure.
  BytecodeCompileClosure _closure;

  // The compiler context.
  CompilerContext        _context;

#if ENABLE_INTERNAL_CODE_OPTIMIZER && ARM &&ENABLE_CODE_OPTIMIZER
  CompilationQueueElement::Fast _next_element;
  CompilationQueueElement::Fast _cur_element;
  BinaryAssembler::LiteralPoolElement::Fast  _next_bound_literal;

  InternalCodeOptimizer* optimizer() {
    return &_internal_code_optimizer;
  }

  void prepare_for_scheduling_of_current_cc(CompiledMethod* cm) {
#if ENABLE_NPCE
    //reset counter before compilation of cc.
    set_null_point_record_counter(0);
#endif
    _internal_code_optimizer.prepare_for_scheduling_of_current_cc(
              cm, code_generator()->code_size());
  }
  
  bool  schedule_current_cc(CompiledMethod* cm JVM_TRAPS) {
#if ENABLE_NPCE
       //should be GUARANTEE(Compiler::current()->null_point_record_counter() <= 
       //                     Compiler::null_point_exception_ins_table()->length(), "there're more npe related ins");
       //if the table is smaller than the real number of npe ins, we don't scheduling code
       //since we have no enough information to maintain the npe relationship during scheduling.
       //null_point_record_counter is the real number of npe ins appeared in current cc.
        if (Compiler::current()->null_point_record_counter() > 
                      Compiler::null_point_exception_ins_table()->length()) {
          return false;
        }             
#endif
      
    return  _internal_code_optimizer.schedule_current_cc(
      cm, code_generator()->code_size() JVM_NO_CHECK_AT_BOTTOM);
  }

  //get the number of unbound literal count
  //if the count>0, scheduler should track the 
  //literal access instruction during scheduling
  int get_unbound_literal_count() {
    return this->code_generator()->unbound_literal_count();
  }

  //get the next branch instruction of the chain 
  //pointed to the same unbind index check stub
  int next_schedulable_branch(int* begin, 
      int& next, BinaryAssembler::Label* label) {

    if (label->is_unused()) {
      //only entered for the first call of Compiler::next_scheduable_branch()
      //in a loop.
              
      CompilationQueueElement::Raw stub = this->rte_handler(
        ThrowExceptionStub::rte_array_index_out_of_bounds);

      // There's no array boundary checking code in current emitted code.       
      if (stub().is_null()) {
        return BinaryAssembler::stop_searching;
      }

      (*label) = stub().entry_label();

      // Stub is emitted. is_unused should be replace by assertion.
      if ( label->is_unused() || label->is_bound()) {
        return BinaryAssembler::stop_searching;
      }
    }

    // Stub isn't emitted.     we are going to find each branch
    // in the chain.
    return code_generator()->next_schedulable_branch(
      *label, (address)begin, next);
 
  }

  //get the offset of bound index check stub
  int index_check_stub_offset() {
   CompilationQueueElement::Raw stub = 
      this->rte_handler(ThrowExceptionStub::rte_array_index_out_of_bounds);
   if (stub().is_null() || !stub().entry_label().is_bound()) {
     return -1;
   }
   return stub().entry_label().position();

  }

  //for the ldr ins accessing  the same literal, we find out the first one, if the chain 
  //start from current CC. Otherwise, we  
  //return the last literal access LDR of previous CC
  //if the chain starts from there.
  //please refer: 
  // "Figure.3.8.2.2.2 algorithm for maintaining the literal accessing chain"
  //of optimization document.
  void get_first_literal_ldrs(int* begin_offset_of_block) {
    AllocationDisabler allocation_not_allowed_in_this_function;
      
    BinaryAssembler::LiteralPoolElement::Raw literal;
    BinaryAssembler::Label label;
    int offset;
    literal = this->code_generator()->_first_unbound_literal.obj();
    
    for (; !literal.is_null(); literal = literal().next()) { 
      label._encoding = literal().label()._encoding;
      offset = this->code_generator()->first_instr_of_literal_loading(label, 
               (address)begin_offset_of_block);
      //if offset is -1, means the this literal is not used 
      //in current compilation continuals
      if (offset > BinaryAssembler::literal_not_used_in_current_cc) {
        VERBOSE_SCHEDULING_AS_YOU_GO(("\t\t[%d] =>literal[%d] ", offset, index));
      //record the offset into table  
        _internal_code_optimizer.record_offset_of_unbound_literal_access_ins(offset);
      }
    }
  }

  //modify the lables of literal pool element based on the scheduling result
  //of literal access instructions
  void patch_unbound_literal_elements(int begin_offset_of_block) {
    AllocationDisabler allocation_not_allowed_in_this_function;
      
    BinaryAssembler::LiteralPoolElement::Raw literal;
    BinaryAssembler::Label tmp;
    int index = 0; 
    int new_offset;
    
    literal = this->code_generator()->_first_unbound_literal.obj();
    
    for (; !literal.is_null(); literal = literal().next()) { 
      tmp._encoding = literal().label()._encoding;
      if(tmp.position() < begin_offset_of_block){
          continue;
      }
      new_offset = _internal_code_optimizer.offset_of_scheduled_unbound_literal_access_ins(index); 
      if(new_offset !=0 ){
        tmp.link_to( new_offset);
        literal().set_label(tmp);
      }
      index++;   
    }
  }

  //update the entry label of a unbind index check stub.
  //the entry label point to the tail of a chain of branch.
  void update_shared_index_check_stub(int position) {
    CompilationQueueElement::Raw stub = rte_handler(
              ThrowExceptionStub::rte_array_index_out_of_bounds);
    if (!stub().is_null() && !stub().entry_label().is_bound() ) {
      BinaryAssembler::Label label;
      label.link_to(position);
      stub().set_entry_label(label);
    }
  }

  //two methods for getting the jitted code offset recorded 
  //in uncompiled OSRStub or
  //EntryFrame.We won't schedule those instruction since 
  //other code will jump to those places later.
  void begin_pinned_entry_search();
  BinaryAssembler::Label get_next_pinned_entry();

  //two method for getting the jittted code offset of the
  //literal who has been written
  //out. We won't unpack those place during scheduling
  void begin_bound_literal_search();
  BinaryAssembler::Label get_next_bound_literal();

  friend class CodeOptimizer;
  friend class InternalCodeOptimizer;
#endif

  friend class BytecodeCompileClosure;
  friend class CodeGenerator;
  friend class CompilationQueueElement;
  friend class OSRStub;

  bool check_if_stack_frame_may_be_omitted();
  static ReturnOop try_to_compile(Method* method, const int active_bci,
                                  const int compiled_code_factor JVM_TRAPS);

  ReturnOop allocate_and_compile( const int compiled_code_factor JVM_TRAPS );

  inline void check_free_space        ( JVM_SINGLE_ARG_TRAPS ) const;
  void internal_compile        ( JVM_SINGLE_ARG_TRAPS );
  void begin_compile           ( JVM_SINGLE_ARG_TRAPS );
  void suspend                 ( void );
  void restore_and_compile     ( JVM_SINGLE_ARG_TRAPS );
  void optimize_code           ( JVM_SINGLE_ARG_TRAPS );
  void setup_for_compile       ( Method::Attributes& attributes JVM_TRAPS );

  void process_compilation_queue ( JVM_SINGLE_ARG_TRAPS );
  static void terminate ( OopDesc* result );
  bool reserve_compiler_area(size_t compiled_method_size);

  void handle_out_of_memory( void );
  static void set_impossible_to_compile(Method *method, const char why[]);
#if ENABLE_PERFORMANCE_COUNTERS
  void init_performance_counters(bool is_resume);
  void update_performance_counters(bool is_resume, OopDesc* result);

  jlong _start_time;
  int   _mem_before_compile;
  int   _gc_before_compile;
#else
  void init_performance_counters(bool /*is_resume*/) {}
  void update_performance_counters(bool /*is_resume*/, OopDesc* /*result*/) {}
#endif

#ifndef PRODUCT
  class CompilationHistory {
  public:
    // IMPL_NOTE: add more info such as start/stop time, etc.
    CompilationHistory *_next;
    char _method_name[256];
  };

  static CompilationHistory *_history_head;
  static CompilationHistory *_history_tail;
#endif

  void append_compilation_history() PRODUCT_RETURN;

#if USE_DEBUG_PRINTING
  void print_on(Stream *st);
  static void p();
#endif

private:
#if ENABLE_APPENDED_CALLINFO
  static CallInfoWriter _callinfo_writer;

  static CallInfoWriter* callinfo_writer() { return &_callinfo_writer; }
#endif

#if ENABLE_CODE_PATCHING
public:
  static void update_checkpoints_table(CompiledMethod* cm);
  static void patch_checkpoints(address current_pc);
  static void unpatch_checkpoints();
  static bool is_undoing_patching() {
    return _is_undoing_patching;
  }
  static bool can_patch(int bci_from, int bci_to);

private:
  static void patch_compiled_method(CompiledMethod* cm);
  static void unpatch_compiled_method(CompiledMethod* cm);

  static CompiledMethodDesc* get_current_compiled_method(address current_pc);

  static bool _is_undoing_patching;
#endif
};

#if ENABLE_PERFORMANCE_COUNTERS && ENABLE_DETAILED_PERFORMANCE_COUNTERS

#define FOR_ALL_COMPILER_PERFORMANCE_COUNTERS(template) \
  template(method_entry, 0)                             \
  template(begin_compile, 0)                            \
  template(end_compile, 0)                              \
  template(conformance_entry, 0)                        \
  template(conform_to_entry, 0)                         \
  template(jmp_to_entry, 0)                             \
                                                        \
  template(bytecode_compile, 0)                         \
  template(bytecode_prolog, 0)                          \
  template(bytecode_epilog, 0)                          \
                                                        \
  template(push_int, 0)                                 \
  template(push_long, 0)                                \
  template(push_obj, 0)                                 \
  template(push_float, 0)                               \
  template(push_double, 0)                              \
                                                        \
  template(load_local, 0)                               \
  template(store_local, 0)                              \
  template(increment_local_int, 0)                      \
                                                        \
  template(array_length, 0)                             \
  template(load_array, 0)                               \
  template(store_array, 0)                              \
                                                        \
  template(binary, 1)                                   \
  template(unary, 0)                                    \
  template(convert, 0)                                  \
                                                        \
  template(pop, 0)                                      \
  template(pop_and_npe_if_null, 0)                      \
  template(pop2, 0)                                     \
  template(dup, 0)                                      \
  template(dup2, 0)                                     \
  template(dup_x1, 0)                                   \
  template(dup2_x1, 0)                                  \
  template(dup_x2, 0)                                   \
  template(dup2_x2, 0)                                  \
  template(swap, 0)                                     \
                                                        \
  template(branch, 0)                                   \
  template(branch_if, 0)                                \
  template(branch_if_icmp, 0)                           \
  template(branch_if_acmp, 0)                           \
                                                        \
  template(compare, 0)                                  \
                                                        \
  template(check_cast, 0)                               \
  template(instance_of, 0)                              \
  template(throw_exception, 0)                          \
  template(return_op, 0)                                \
  template(table_switch, 0)                             \
  template(lookup_switch, 0)                            \
                                                        \
  template(get_field, 0)                                \
  template(put_field, 0)                                \
  template(fast_get_field, 0)                           \
  template(fast_put_field, 0)                           \
                                                        \
  template(get_static, 0)                               \
  template(put_static, 0)                               \
                                                        \
  template(new_object, 0)                               \
  template(new_basic_array, 0)                          \
  template(new_object_array, 0)                         \
  template(new_multi_array, 0)                          \
                                                        \
  template(monitor_enter, 0)                            \
  template(monitor_exit, 0)                             \
                                                        \
  template(invoke_static, 0)                            \
  template(invoke_interface, 0)                         \
  template(fast_invoke_virtual, 0)                      \
  template(fast_invoke_virtual_final, 0)                \
  template(fast_invoke_special, 0)                      \
  template(invoke_native, 0)                            \
                                                        \
  template(invoke_special, 0)                           \
  template(invoke_virtual, 0)                           \
                                                        \
  template(throw_exception_stub, 0)                     \
  template(check_cast_stub, 0)                          \
  template(osr_stub, 0)                                 \
                                                        \
  template(instance_of_stub, 0)                         \
  template(type_check_stub, 0)                          \
  template(stack_overflow_stub, 0)                      \
  template(timer_tick_stub, 0)                          \
                                                        \
  template(new_object_stub, 0)                          \
  template(new_type_array_stub, 0)                      \
                                                        \
  template(generic_compile, 1)                          \
  template(custom_compile, 1)                           \
                                                        \
  template(sentinel, 0)                                 \
  template(relocation, 0)                               \
  template(init_static_array, 0)

#define DEFINE_COUNTER_FIELDS(name, counter_level) \
  julong name ## _time;                            \
  jint   name ## _size;                            \
  jint   name ## _count;

struct Compiler_PerformanceCounters {
  int level;
  FOR_ALL_COMPILER_PERFORMANCE_COUNTERS(DEFINE_COUNTER_FIELDS)
};

#undef DEFINE_COUNTER_FIELDS

extern Compiler_PerformanceCounters comp_perf_counts;

#define DEFINE_COUNTER_WRAPPER_CLASS(name, counter_level)                  \
class counter_ ## name {                                                   \
public:                                                                    \
  counter_ ## name() {                                                     \
    GUARANTEE(comp_perf_counts.level <= counter_level, "Sanity");          \
    _level = comp_perf_counts.level;                                       \
    comp_perf_counts.level = counter_level + 1;                            \
                                                                           \
    _start_offset = Compiler::current()->code_generator()->code_size();    \
    _start_time = Os::elapsed_counter();                                   \
  }                                                                        \
                                                                           \
  ~counter_ ## name() {                                                    \
    comp_perf_counts.name ## _time +=                                      \
      Os::elapsed_counter() - _start_time;                                 \
    comp_perf_counts.name ## _size +=                                      \
      Compiler::current()->code_generator()->code_size() - _start_offset;  \
    comp_perf_counts.name ## _count++;                                     \
                                                                           \
    GUARANTEE(comp_perf_counts.level > counter_level, "Sanity");           \
    comp_perf_counts.level = _level;                                       \
  }                                                                        \
private:                                                                   \
  jlong _start_time;                                                       \
  jint  _start_offset;                                                     \
  int   _level;                                                            \
};

FOR_ALL_COMPILER_PERFORMANCE_COUNTERS(DEFINE_COUNTER_WRAPPER_CLASS)

#undef DEFINE_COUNTER_WRAPPER_CLASS

#define DEFINE_COUNTER_LEVEL(name, counter_level) \
const int name ## _level = counter_level;

FOR_ALL_COMPILER_PERFORMANCE_COUNTERS(DEFINE_COUNTER_LEVEL)

#undef DEFINE_COUNTER_LEVEL

#define COMPILER_PERFORMANCE_COUNTER_ACTIVE() (comp_perf_counts.level > 0)

#define COMPILER_PERFORMANCE_COUNTER_START(name)                   \
    GUARANTEE(comp_perf_counts.level <= name ## _level, "Sanity"); \
    int _level = comp_perf_counts.level;                           \
    comp_perf_counts.level = name ## _level + 1;                   \
                                                                   \
    jint  __start_offset =                                         \
      Compiler::current()->code_generator()->code_size();          \
    jlong __start_time = Os::elapsed_counter()

#define COMPILER_PERFORMANCE_COUNTER_END(name)                             \
    comp_perf_counts.name ## _time +=                                      \
      Os::elapsed_counter() - __start_time;                                \
    comp_perf_counts.name ## _size +=                                      \
      Compiler::current()->code_generator()->code_size() - __start_offset; \
    comp_perf_counts.name ## _count++;                                     \
                                                                           \
    GUARANTEE(comp_perf_counts.level > name ## _level, "Sanity");          \
    GUARANTEE(_level <= name ## _level, "Sanity");                         \
    comp_perf_counts.level = _level                           

#define INCREMENT_COMPILER_PERFORMANCE_COUNTER(name, value) \
    comp_perf_counts.name ## _size += (value);              \
    comp_perf_counts.name ## _count++;

#define COMPILER_PERFORMANCE_COUNTER_IN_BLOCK(name) \
  counter_ ## name __counter_ ## name;

#else
#define COMPILER_PERFORMANCE_COUNTER_ACTIVE() false

#define COMPILER_PERFORMANCE_COUNTER_START()
#define COMPILER_PERFORMANCE_COUNTER_END(name)
#define INCREMENT_COMPILER_PERFORMANCE_COUNTER(name, value)
#define COMPILER_PERFORMANCE_COUNTER_IN_BLOCK(name) 
#endif

#else
// !ENABLE_COMPILER
class Compiler: public StackObj {
public:
  static bool is_active() {
    return false;
  }
  static void abort_suspended_compilation() {}
  static void on_timer_tick() {}
};
#endif
