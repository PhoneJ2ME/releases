/*
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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
 */

// Interface to com.sun.cldc.isolate.Isolate objects

class IsolateObj : public Instance {
public:
  HANDLE_DEFINITION(IsolateObj, Instance);

  // To avoid endless lists of friends the static offset computation
  // routines are all public.
  #define FIELD(name, index, kind, inner, outer)                              \
    static int name##_offset() { return header_size() + index * sizeof(jint);}\
    inner name(void) const { return kind##_field(name##_offset()); }          \
    void set_##name(outer value) { kind##_field_put(name##_offset(), value);}

  #define INT_FIELD(name, index)      FIELD(name, index, int, jint, const jint)
  #define OBJ_FIELD(name, index, type) FIELD(name, index, obj, ReturnOop, type*)
  #define STATIC_FIELD(name, index, kind, inner, outer)                       \
    static int static_##name##_offset() {                                     \
         return TaskMirror::static_field_start() + index * sizeof(jint);      \
    }                                                                         \
    inner name(void) const {                                                  \
      return static_##kind##_field(static_##name##_offset());                 \
    }                                                                         \
    void set_##name(outer value) {                                            \
        static_##kind##_field_put(static_##name##_offset(), value);           \
    }

  #define STATIC_INT_FIELD(name, index) \
     STATIC_FIELD(name, index, int, jint, const jint)


  INT_FIELD( priority,          0 )
  OBJ_FIELD( next,              1, IsolateObj )
  OBJ_FIELD( unique_id,         2, String     )
  INT_FIELD( is_terminated,     3 )
  INT_FIELD( saved_exit_code,   4 )
  OBJ_FIELD( main_class,        5, ObjArray   )
  OBJ_FIELD( main_args,         6, ObjArray   )
  OBJ_FIELD( classpath,         7, ObjArray   )
  INT_FIELD( memory_reserve,    8 )
  INT_FIELD( memory_limit,      9 )
  INT_FIELD( api_access_init,  10 )
  INT_FIELD( connect_debugger, 11)
  INT_FIELD( use_verifier,     12)
  INT_FIELD( profile_id,       13 )  

  STATIC_INT_FIELD( api_access, 0 )

  #undef FIELD
  #undef INT_FIELD
  #undef OBJ_FIELD

  jint exit_code( void ) const;
  jint status   ( void ) const;
  jint task_id  ( void ) const;

  jint static_int_field(int offset) const;
  void static_int_field_put(int offset, const jint value);

  void notify_all_waiters(JVM_SINGLE_ARG_TRAPS);

  bool is_equivalent_to(const IsolateObj *other) const {
    return unique_id() == other->unique_id();
  }

  void mark_equivalent_isolates_as_terminated(JVM_SINGLE_ARG_TRAPS);

  // Returns the task represented by this IsolateObj. Returns NULL if this
  // IsolateObj represents a task that has not been started or has been
  // terminated.
  ReturnOop task( void ) const;

  bool represents(const Task *task) const;

  ReturnOop duplicate(JVM_SINGLE_ARG_TRAPS) const;
  static void verify_fields( void ) PRODUCT_RETURN;
};
