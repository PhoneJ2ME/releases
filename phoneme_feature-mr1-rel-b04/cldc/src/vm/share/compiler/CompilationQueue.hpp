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

#if ENABLE_COMPILER

class CompilationQueueElement: public MixedOop {
 public:
  // To avoid endless lists of friends the static offset computation routines
  // are all public. 
  static jint type_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _type);
  }
  static jint bci_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _bci);
  }
  static jint frame_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _frame);
  }
  static jint entry_label_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _entry_label);
  }
  static jint return_label_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _return_label);
  }
  static jint register_0_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _register_0);
  }
  static jint register_1_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _register_1);
  }
  static jint info_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _info);
  }
  static jint is_suspended_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _is_suspended);
  }
  static jint code_size_before_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _code_size_before);
  }
  static jint entry_has_been_bound_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _entry_has_been_bound);
  }
  static jint next_offset() {
    return FIELD_OFFSET(CompilationQueueElementDesc, _next);
  }

 public:
  HANDLE_DEFINITION(CompilationQueueElement, MixedOop);

  enum CompilationQueueElementType {
    compilation_continuation,
    throw_exception_stub,
    type_check_stub,
    check_cast_stub,
    instance_of_stub,
    osr_stub,
    stack_overflow_stub,
    timer_tick_stub,
    quick_catch_stub
#if ENABLE_INTERNAL_CODE_OPTIMIZER && ARM
    , entry_stub
#endif

  };

#if ENABLE_LOOP_OPTIMIZATION && ARM
  enum {
    max_search_length   = 3
  };
#endif

#ifndef PRODUCT
  static void iterate_oopmaps(oopmaps_doer do_map, void *param);
  void iterate(OopVisitor* visitor);
#endif


 protected:
  void generic_compile(address addr JVM_TRAPS);
  void generic_compile(address addr,
                       const Assembler::Register stack_ptr JVM_TRAPS);

public:
  BinaryAssembler::Label entry_label() {
    BinaryAssembler::Label L; 
    L._encoding = int_field(entry_label_offset()); 
    return L;
  }
  void  set_entry_label(BinaryAssembler::Label& value) { 
    int_field_put(entry_label_offset(), value._encoding);
  }

  void clear_entry_label() {
    int_field_put(entry_label_offset(), 0);
  }

  BinaryAssembler::Label return_label() {
    BinaryAssembler::Label L;
    L._encoding = int_field(return_label_offset());
    return L;
  }
  void set_return_label(BinaryAssembler::Label& value) {
    int_field_put(return_label_offset(), value._encoding);
  }
 
  void clear_return_label() {
    int_field_put(return_label_offset(), 0);
  }

 private:
  // Pool of recycled elements, MUST BE RESET BEFORE GC
  static CompilationQueueElementDesc* _pool;
#if ENABLE_INLINE && ARM
  static CompilationQueueElementDesc* saved_pool;
#endif
 public:
#if ENABLE_INLINE && ARM
  static void save_pool ( void ) { saved_pool = _pool; }
  static void load_pool (void ) { _pool = saved_pool;}
#endif
  static void reset_pool ( void ) { _pool = NULL; }

  // Recycled elements are kept live in the pool until
  // the end of current compilation or a GC
  inline void free( void );

  // CompilationQueueElement
  static ReturnOop allocate(CompilationQueueElementType type, int bci JVM_TRAPS);

  void insert();

  // Compile this CompilationQueueElement.
  // Returns true if the element finished compilation, false if it
  // has been suspended and needs to be resumed in the future.
  bool compile(JVM_SINGLE_ARG_TRAPS);

  // Field accessors.
  CompilationQueueElementType type() {
    return (CompilationQueueElementType) int_field(type_offset());
  }
  void set_type(CompilationQueueElementType value) {
    int_field_put(type_offset(), value);
  }

  jint bci()               { return int_field(bci_offset()); }
  void set_bci(jint value) { int_field_put(bci_offset(), value);
  }

  // ^VirtualStackFrame
  ReturnOop frame()  { return obj_field(frame_offset()); }
  void set_frame(VirtualStackFrame* value) {
      obj_field_put(frame_offset(), value);
  }

  Assembler::Register register_0(){
    return (Assembler::Register) int_field(register_0_offset());
  }
  void set_register_0(Assembler::Register value) {
    int_field_put(register_0_offset(), value);
  }

  Assembler::Register register_1() {
    return (Assembler::Register) int_field(register_1_offset());
  }
  void set_register_1(Assembler::Register value) {
    int_field_put(register_1_offset(), value);
  }

  jint info() {
    return (Assembler::Register) int_field(info_offset());
  }
  void set_info(jint value) {
    int_field_put(info_offset(), value);
  }
  bool is_suspended() {
    return bool_field(is_suspended_offset());
  }
  void set_is_suspended(bool value) {
    bool_field_put(is_suspended_offset(), CAST_TO_JBOOLEAN(value));
  }
  bool entry_has_been_bound() {
    return bool_field(entry_has_been_bound_offset());
  }
  void set_entry_has_been_bound(bool value) {
    bool_field_put(entry_has_been_bound_offset(), CAST_TO_JBOOLEAN(value));
  }
  jint code_size_before() {
    return int_field(code_size_before_offset());
  }
  void set_code_size_before(jint value) {
    int_field_put(code_size_before_offset(), value);
  }

  ReturnOop next() { return obj_field(next_offset()); }

  void set_next(CompilationQueueElement* value) {
    obj_field_put(next_offset(), value);
  }
};

class CompilationContinuation: public CompilationQueueElement {
 public:
  HANDLE_DEFINITION(CompilationContinuation, CompilationQueueElement);

  static ReturnOop insert(jint bci,
                          BinaryAssembler::Label& entry_label JVM_TRAPS);

  // Compile this continuation.
  // Returns true if the element finished compilation, false if it
  // has been suspended and needs to be resumed in the future.
  bool compile(JVM_SINGLE_ARG_TRAPS);

  static CompilationContinuation* cast(CompilationQueueElement* value) { 
    GUARANTEE(value->type() == compilation_continuation, "Type check");
    return (CompilationContinuation*) value;
  }

  enum { 
     cc_flag_need_osr_entry        = 1,
     cc_flag_is_exception_handler  = 2,
     cc_flag_run_immediately       = 4,
     cc_flag_forward_branch_target = 8
  };

  void set_need_osr_entry() { set_info(info() | cc_flag_need_osr_entry); }
  bool need_osr_entry()     { return (info() & cc_flag_need_osr_entry) != 0; }

  void set_is_exception_handler() { 
    set_info(info() | cc_flag_is_exception_handler);
  }
  bool is_exception_handler() {
    return (info() & cc_flag_is_exception_handler) != 0;
  }

  void set_run_immediately() { set_info(info() | cc_flag_run_immediately); }
  bool run_immediately()     { return (info() & cc_flag_run_immediately) != 0;}

  void set_forward_branch_target() { 
    set_info(info() | cc_flag_forward_branch_target); 
  }
  bool forward_branch_target()     { 
    return (info() & cc_flag_forward_branch_target) != 0;
  }

private:
  void begin_compile(JVM_SINGLE_ARG_TRAPS);
  bool compile_bytecodes(JVM_SINGLE_ARG_TRAPS);
  void end_compile( void );
};


class ThrowExceptionStub: public CompilationQueueElement {
 public:
  // Runtime exceptions that can be thrown by compiled code
  enum RuntimeException { 
    rte_null_pointer                 = 0,
    rte_array_index_out_of_bounds    = 1,
    rte_illegal_monitor_state        = 2,
    rte_division_by_zero             = 3,
    rte_incompatible_class_change    = 4,
    number_of_runtime_exceptions
  };
  static address exception_allocator(int rte);
  static ReturnOop exception_class(int rte);

  HANDLE_DEFINITION(ThrowExceptionStub, CompilationQueueElement);

private:
  // ^ThrowExceptionStub
  static ReturnOop allocate(RuntimeException rte, int bci JVM_TRAPS);
protected:
  static ReturnOop allocate_or_share(RuntimeException rte JVM_TRAPS);

private:
  address exception_thrower();

  void set_rte(RuntimeException rte) {
    GUARANTEE(info() == 0, "Only set rte once");
    set_info((jint)rte);
  }
  RuntimeException get_rte()  {
    return (RuntimeException)(info() & ~0x80000000);
  }

public:
  void set_is_persistent() { set_info(info() | 0x80000000); }
  bool is_persistent()  { return info() < 0; }

  // Throw an exception.
  void compile(JVM_SINGLE_ARG_TRAPS);

  static ThrowExceptionStub* cast(CompilationQueueElement* value) {
    GUARANTEE(value->type() == throw_exception_stub, "Type check");
    return (ThrowExceptionStub*) value;
  }
#if ENABLE_NPCE  
#if ENABLE_INTERNAL_CODE_OPTIMIZER && ARM
  static int npe_count;
#endif
  friend class CodeGenerator;
#endif  
#if ENABLE_INTERNAL_CODE_OPTIMIZER || ENABLE_NPCE
  friend class Compiler;
#endif  
};

inline void CompilationQueueElement::free( void ) {
  if( !( type() == CompilationQueueElement::throw_exception_stub &&
         ThrowExceptionStub::cast(this)->is_persistent() ) ) {
    CompilationQueueElementDesc* p = (CompilationQueueElementDesc*) obj();
    set_next((CompilationQueueElement*)&_pool);
    _pool = p;
  }
}

class UnlockExceptionStub: public ThrowExceptionStub {
 public:
  static ReturnOop
  allocate_or_share(JVM_SINGLE_ARG_TRAPS) {
    return ThrowExceptionStub::allocate_or_share(rte_illegal_monitor_state
                                                 JVM_NO_CHECK_AT_BOTTOM);
  }
};

class IndexCheckStub: public ThrowExceptionStub {
 public:
  static ReturnOop
  allocate_or_share(JVM_SINGLE_ARG_TRAPS) {
    return ThrowExceptionStub::allocate_or_share(rte_array_index_out_of_bounds
                                                 JVM_NO_CHECK_AT_BOTTOM);
  }
};

class NullCheckStub: public ThrowExceptionStub {
 public:
  HANDLE_DEFINITION(NullCheckStub, ThrowExceptionStub);
  static ReturnOop
  allocate_or_share(JVM_SINGLE_ARG_TRAPS) {
    return ThrowExceptionStub::allocate_or_share(rte_null_pointer
                                                 JVM_NO_CHECK_AT_BOTTOM);
  }
#if ENABLE_NPCE
#if ENABLE_CODE_OPTIMIZER && !ENABLE_THUMB_COMPILER
  bool is_two_words() {
    return int_field(return_label_offset()) != 0;
  }
  jint high_word() {
    return int_field(return_label_offset());
  }
  void set_two_words(int offset = 1) {
    int_field_put(return_label_offset(), offset);
#if ENABLE_INTERNAL_CODE_OPTIMIZER
    ThrowExceptionStub::npe_count++;
#endif
  }


#endif
#endif
};

class ZeroDivisorCheckStub: public ThrowExceptionStub {
 public:
  HANDLE_DEFINITION(ZeroDivisorCheckStub, ThrowExceptionStub);
  static ReturnOop
  allocate_or_share(JVM_SINGLE_ARG_TRAPS) {
    return ThrowExceptionStub::allocate_or_share(rte_division_by_zero
                                                 JVM_NO_CHECK_AT_BOTTOM);
  }
};

class IncompatibleClassChangeStub: public ThrowExceptionStub {
 public:
  HANDLE_DEFINITION(IncompatibleClassChangeStub, ThrowExceptionStub);
  static ReturnOop
  allocate_or_share(JVM_SINGLE_ARG_TRAPS) {
    return ThrowExceptionStub::allocate_or_share(rte_incompatible_class_change
                                                 JVM_NO_CHECK_AT_BOTTOM);
  }
};

class TypeCheckStub: public CompilationQueueElement {
 public:
  HANDLE_DEFINITION(TypeCheckStub, CompilationQueueElement);

  // ^TypeCheckStub
  static ReturnOop allocate(jint bci,
                            BinaryAssembler::Label& entry_label,
                            BinaryAssembler::Label& return_label JVM_TRAPS) {
    TypeCheckStub::Raw stub =
      CompilationQueueElement::allocate(type_check_stub, bci
                                        JVM_NO_CHECK);
    if (stub.not_null()) {
      stub().set_entry_label(entry_label); 
      stub().set_return_label(return_label);
    }
    return stub;
  }

  void compile(JVM_SINGLE_ARG_TRAPS);
  
  static TypeCheckStub* cast(CompilationQueueElement* value) {
    GUARANTEE(value->type() == type_check_stub, "Type check");
    return (TypeCheckStub*) value;
  }

 private:
};

class InstanceOfStub: public CompilationQueueElement {
 private:
  BinaryAssembler::Register return_register() {
    return register_0();
  }
  int class_id() {
    return info();
  }
  void set_return_register(BinaryAssembler::Register value) {
    set_register_0(value);
  }
  void set_class_id(int class_id) {
    set_info(class_id);
  }

 public:
  HANDLE_DEFINITION(InstanceOfStub, CompilationQueueElement);

  // ^InstanceOfStub
  static ReturnOop allocate(jint bci, jint class_id,
                            BinaryAssembler::Label& entry_label,
                            BinaryAssembler::Label& return_label, 
                            BinaryAssembler::Register return_register
                            JVM_TRAPS) {
    InstanceOfStub::Raw stub =
        CompilationQueueElement::allocate(instance_of_stub, bci
                                          JVM_NO_CHECK);
    if (stub.not_null()) {
      stub().set_entry_label(entry_label); 
      stub().set_return_label(return_label);
      stub().set_return_register(return_register);
      stub().set_class_id(class_id);
    }
    return stub;
  }

  void compile(JVM_SINGLE_ARG_TRAPS);

  static InstanceOfStub* cast(CompilationQueueElement* value) {
    GUARANTEE(value->type() == instance_of_stub, "Type check");
    return (InstanceOfStub*) value;
  }

};

class CheckCastStub: public CompilationQueueElement {
 public:
  HANDLE_DEFINITION(CheckCastStub, CompilationQueueElement);

  void compile(JVM_SINGLE_ARG_TRAPS);

  static void insert(int bci, int class_id,
                     BinaryAssembler::Label& entry_label,
                     BinaryAssembler::Label& return_label JVM_TRAPS);

  static CheckCastStub* cast(CompilationQueueElement* value) {
    GUARANTEE(value->type() == check_cast_stub, "Type check");
    return (CheckCastStub*) value;
  }

 private:
  int get_class_id() {
    return register_0();
  }
};

class StackOverflowStub: public CompilationQueueElement {
 public:
  HANDLE_DEFINITION(StackOverflowStub, CompilationQueueElement);

  void compile(JVM_SINGLE_ARG_TRAPS);

  // ^StackOverflowStub
  static ReturnOop allocate(BinaryAssembler::Label& entry_label,
                            BinaryAssembler::Label& return_label,
                            const Assembler::Register stack_pointer,
                            const Assembler::Register method_pointer JVM_TRAPS) {
    StackOverflowStub::Raw stub =
      CompilationQueueElement::allocate(stack_overflow_stub, 0 JVM_NO_CHECK);
    if (stub.not_null()) {
      stub().set_entry_label(entry_label); 
      stub().set_return_label(return_label);
      stub().set_register_1(method_pointer);
      stub().set_register_0(stack_pointer);
    }
    return stub;
  }

  static StackOverflowStub* cast(CompilationQueueElement* value) {
    GUARANTEE(value->type() == stack_overflow_stub, "Type check");
    return (StackOverflowStub*) value;
  }
 
 private:
};

class TimerTickStub: public CompilationQueueElement {
 public:
  HANDLE_DEFINITION(TimerTickStub, CompilationQueueElement);

  void compile(JVM_SINGLE_ARG_TRAPS);

  // ^TimerTickStub
  static ReturnOop allocate(jint bci,
                            BinaryAssembler::Label& entry_label,
                            BinaryAssembler::Label& return_label JVM_TRAPS) {
    TimerTickStub::Raw stub =
        CompilationQueueElement::allocate(timer_tick_stub, bci
                                          JVM_NO_CHECK);
    if (stub.not_null()) {
      stub().set_entry_label(entry_label); 
      stub().set_return_label(return_label);
    }
    return stub;
  }

  static TimerTickStub* cast(CompilationQueueElement* value) {
    GUARANTEE(value->type() == timer_tick_stub, "Type check");
    return (TimerTickStub*) value;
  }
 
 private:
};

class QuickCatchStub: public CompilationQueueElement {
 public:
  HANDLE_DEFINITION(QuickCatchStub, CompilationQueueElement);

  void compile(JVM_SINGLE_ARG_TRAPS);

  // ^QuickCatchStub
  static ReturnOop allocate(jint bci,
                            const Value &value, jint handler_bci,
                            BinaryAssembler::Label& entry_label JVM_TRAPS);

  static QuickCatchStub* cast(CompilationQueueElement* value) {
    GUARANTEE(value->type() == quick_catch_stub, "Type check");
    return (QuickCatchStub*) value;
  }
};

class OSRStub: public CompilationQueueElement {
 private:
  void emit_osr_entry_and_callinfo(CodeGenerator *gen JVM_TRAPS);

 public:
  HANDLE_DEFINITION(OSRStub, CompilationQueueElement);

  void compile(JVM_SINGLE_ARG_TRAPS);

  // ^OSRStub
  static ReturnOop allocate(jint bci,
                            BinaryAssembler::Label& entry_label
                            JVM_TRAPS) {
    OSRStub::Raw stub =
        CompilationQueueElement::allocate(osr_stub, bci
                                          JVM_NO_CHECK);
    if (stub.not_null()) {
      stub().set_entry_label(entry_label);
    }
    return stub;
  }

  static OSRStub* cast(CompilationQueueElement* value) {
    GUARANTEE(value->type() == osr_stub, "Type check");
    return (OSRStub*) value;
  }
 
};

#if ENABLE_INTERNAL_CODE_OPTIMIZER && ARM
class EntryStub:  public CompilationQueueElement  {
 public:

  HANDLE_DEFINITION(EntryStub, CompilationQueueElement);

  static ReturnOop allocate(jint bci, 
                          BinaryAssembler::Label& entry_label JVM_TRAPS) {
    EntryStub::Raw stub =
          CompilationQueueElement::allocate(entry_stub, bci 
                                                             JVM_NO_CHECK);
    if (stub.not_null()) {
      stub().set_entry_label(entry_label);
    }
    return stub;
  }

};
#endif
#endif /* ENABLE_COMPILER */
