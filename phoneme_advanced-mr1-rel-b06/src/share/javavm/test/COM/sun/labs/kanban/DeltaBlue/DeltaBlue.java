/*
 * Copyright 1990-2006 Sun Microsystems, Inc. All Rights Reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 only,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 or visit www.sun.com if you need additional information or have
 * any questions.
 */
/*

  This is a Java implemention of the DeltaBlue algorithm described in:
    "The DeltaBlue Algorithm: An Incremental Constraint Hierarchy Solver"
    by Bjorn N. Freeman-Benson and John Maloney
    January 1990 Communications of the ACM,
    also available as University of Washington TR 89-08-06.

  This implementation by Mario Wolczko, Sun Microsystems, Sep 1996,
  based on the Smalltalk implementation by John Maloney.

*/

package COM.sun.labs.kanban.DeltaBlue;

import java.util.Vector;

import Benchmark;



/* 
Strengths are used to measure the relative importance of constraints.
New strengths may be inserted in the strength hierarchy without
disrupting current constraints.  Strengths cannot be created outside
this class, so pointer comparison can be used for value comparison.
*/

class Strength {

  private int strengthValue;
  private String name;

  private Strength(int strengthValue, String name)
  {
    this.strengthValue= strengthValue;
    this.name= name;
  }

  public static boolean stronger(Strength s1, Strength s2)
  {
    return s1.strengthValue < s2.strengthValue;
  }

  public static boolean weaker(Strength s1, Strength s2)
  {
    return s1.strengthValue > s2.strengthValue;
  }

  public static Strength weakestOf(Strength s1, Strength s2)
  {
    return weaker(s1, s2) ? s1 : s2;
  }

  public static Strength strongest(Strength s1, Strength s2)
  {
    return stronger(s1, s2) ? s1 : s2;
  }

  // for iteration
  public Strength nextWeaker()
  {
    switch (strengthValue) {
    case 0: return weakest;
    case 1: return weakDefault;
    case 2: return normal;
    case 3: return strongDefault;
    case 4: return preferred;
    case 5: return strongPreferred;

    case 6: 
    default:
      System.err.println("Invalid call to nextStrength()!");
      System.exit(1);
      return null;
    }
  }

  // Strength constants
  public final static Strength required       = new Strength(0, "required");
  public final static Strength strongPreferred= new Strength(1, "strongPreferred");
  public final static Strength preferred      = new Strength(2, "preferred");
  public final static Strength strongDefault  = new Strength(3, "strongDefault");
  public final static Strength normal	      = new Strength(4, "normal");
  public final static Strength weakDefault    = new Strength(5, "weakDefault");
  public final static Strength weakest	      = new Strength(6, "weakest");

  public void print()
  {
    System.out.print("strength[" + Integer.toString(strengthValue) + "]");
  }
}




//------------------------------ variables ------------------------------

// I represent a constrained variable. In addition to my value, I
// maintain the structure of the constraint graph, the current
// dataflow graph, and various parameters of interest to the DeltaBlue
// incremental constraint solver.

class Variable {

  public int value;               // my value; changed by constraints
  public Vector constraints;      // normal constraints that reference me
  public Constraint determinedBy; // the constraint that currently determines
                                  // my value (or null if there isn't one)
  public int mark;                // used by the planner to mark constraints
  public Strength walkStrength;   // my walkabout strength
  public boolean stay;            // true if I am a planning-time constant
  public String	name;             // a symbolic name for reporting purposes


  private Variable(String name, int initialValue, Strength walkStrength,
		   int nconstraints)
  {
    value= initialValue;
    constraints= new Vector(nconstraints);
    determinedBy= null;
    mark= 0;
    this.walkStrength= walkStrength;
    stay= true;
    this.name= name;
  }

  public Variable(String name, int value)
  {
    this(name, value, Strength.weakest, 2);
  }

  public Variable(String name)
  {
    this(name, 0, Strength.weakest, 2);
  }

  public void print()
  {
    System.out.print(name + "(");
    walkStrength.print();
    System.out.print("," + value + ")");
  }
  
  // Add the given constraint to the set of all constraints that refer to me.
  public void addConstraint(Constraint c)
  {
    constraints.addElement(c);
  }

  // Remove all traces of c from this variable.
  public void removeConstraint(Constraint c)
  {
    constraints.removeElement(c);
    if (determinedBy == c) determinedBy= null;
  }

  // Attempt to assign the given value to me using the given strength.
  public void setValue(int value, Strength strength)
  {
    EditConstraint e= new EditConstraint(this, strength);
    if (e.isSatisfied()) {
      this.value= value;
      DeltaBlue.planner.propagateFrom(this);
    }
    e.destroyConstraint();
  }

}




//------------------------ constraints ------------------------------------

// I am an abstract class representing a system-maintainable
// relationship (or "constraint") between a set of variables. I supply
// a strength instance variable; concrete subclasses provide a means
// of storing the constrained variables and other information required
// to represent a constraint.

abstract class Constraint {

  public Strength strength; // the strength of this constraint

  protected Constraint() {} // this has to be here because of
                            // Java's constructor idiocy.

  protected Constraint(Strength strength)
  {
    this.strength= strength;
  }

  // Answer true if this constraint is satisfied in the current solution.
  public abstract boolean isSatisfied();

  // Record the fact that I am unsatisfied.
  public abstract void markUnsatisfied();

  // Normal constraints are not input constraints. An input constraint
  // is one that depends on external state, such as the mouse, the
  // keyboard, a clock, or some arbitrary piece of imperative code.
  public boolean isInput() { return false; }

  // Activate this constraint and attempt to satisfy it.
  protected void addConstraint()
  {
    addToGraph();
    DeltaBlue.planner.incrementalAdd(this);
  }

  // Deactivate this constraint, remove it from the constraint graph,
  // possibly causing other constraints to be satisfied, and destroy
  // it.
  public void destroyConstraint()
  {
    if (isSatisfied()) DeltaBlue.planner.incrementalRemove(this);
    removeFromGraph();
  }

  // Add myself to the constraint graph.
  public abstract void addToGraph();

  // Remove myself from the constraint graph.
  public abstract void removeFromGraph();

  // Decide if I can be satisfied and record that decision. The output
  // of the choosen method must not have the given mark and must have
  // a walkabout strength less than that of this constraint.
  protected abstract void chooseMethod(int mark);

  // Set the mark of all input from the given mark.
  protected abstract void markInputs(int mark);

  // Assume that I am satisfied. Answer true if all my current inputs
  // are known. A variable is known if either a) it is 'stay' (i.e. it
  // is a constant at plan execution time), b) it has the given mark
  // (indicating that it has been computed by a constraint appearing
  // earlier in the plan), or c) it is not determined by any
  // constraint.
  public abstract boolean inputsKnown(int mark);

  // Answer my current output variable. Raise an error if I am not
  // currently satisfied.
  public abstract Variable output();

  // Attempt to find a way to enforce this constraint. If successful,
  // record the solution, perhaps modifying the current dataflow
  // graph. Answer the constraint that this constraint overrides, if
  // there is one, or nil, if there isn't.
  // Assume: I am not already satisfied.
  //
  public Constraint satisfy(int mark)
  {
    chooseMethod(mark);
    if (!isSatisfied()) {
      if (strength == Strength.required) {
	DeltaBlue.error("Could not satisfy a required constraint");
      }
      return null;
    }
    // constraint can be satisfied
    // mark inputs to allow cycle detection in addPropagate
    markInputs(mark);
    Variable out= output();
    Constraint overridden= out.determinedBy;
    if (overridden != null) overridden.markUnsatisfied();
    out.determinedBy= this;
    if (!DeltaBlue.planner.addPropagate(this, mark)) {
      System.out.println("Cycle encountered");
      return null;
    }
    out.mark= mark;
    return overridden;
  }

  // Enforce this constraint. Assume that it is satisfied.
  public abstract void execute();

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  public abstract void recalculate();

  protected abstract void printInputs();

  protected void printOutput() { output().print(); }

  public void print()
  {
    int i, outIndex;

    if (!isSatisfied()) {
      System.out.print("Unsatisfied");
    } else {
      System.out.print("Satisfied(");
      printInputs();
      System.out.print(" -> ");
      printOutput();
      System.out.print(")");
    }
    System.out.print("\n");
  }

}



//-------------unary constraints-------------------------------------------

// I am an abstract superclass for constraints having a single
// possible output variable.
//
abstract class UnaryConstraint extends Constraint {

  protected Variable myOutput; // possible output variable
  protected boolean satisfied; // true if I am currently satisfied

  protected UnaryConstraint(Variable v, Strength strength)
  {
    super(strength);
    myOutput= v;
    satisfied= false;
    addConstraint();
  }

  // Answer true if this constraint is satisfied in the current solution.
  public boolean isSatisfied() { return satisfied; }

  // Record the fact that I am unsatisfied.
  public void markUnsatisfied() { satisfied= false; }

  // Answer my current output variable.
  public Variable output() { return myOutput; }

  // Add myself to the constraint graph.
  public void addToGraph()
  {
    myOutput.addConstraint(this);
    satisfied= false;
  }

  // Remove myself from the constraint graph.
  public void removeFromGraph()
  {
    if (myOutput != null) myOutput.removeConstraint(this);
    satisfied= false;
  }

  // Decide if I can be satisfied and record that decision.
  protected void chooseMethod(int mark)
  {
    satisfied=    myOutput.mark != mark
               && Strength.stronger(strength, myOutput.walkStrength);
  }

  protected void markInputs(int mark) {}   // I have no inputs

  public boolean inputsKnown(int mark) { return true; }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied."
  public void recalculate()
  {
    myOutput.walkStrength= strength;
    myOutput.stay= !isInput();
    if (myOutput.stay) execute(); // stay optimization
  }

  protected void printInputs() {} // I have no inputs

}


// I am a unary input constraint used to mark a variable that the
// client wishes to change.
//
class EditConstraint extends UnaryConstraint {

  public EditConstraint(Variable v, Strength str) { super(v, str); }

  // I indicate that a variable is to be changed by imperative code.
  public boolean isInput() { return true; }

  public void execute() {} // Edit constraints do nothing.

}

// I mark variables that should, with some level of preference, stay
// the same. I have one method with zero inputs and one output, which
// does nothing. Planners may exploit the fact that, if I am
// satisfied, my output will not change during plan execution. This is
// called "stay optimization".
//
class StayConstraint extends UnaryConstraint {

  // Install a stay constraint with the given strength on the given variable.
  public StayConstraint(Variable v, Strength str) { super(v, str); }

  public void execute() {} // Stay constraints do nothing.

}




//-------------binary constraints-------------------------------------------


// I am an abstract superclass for constraints having two possible
// output variables.
//
abstract class BinaryConstraint extends Constraint {

  protected Variable v1, v2; // possible output variables
  protected byte direction; // one of the following...
  protected static byte backward= -1;    // v1 is output
  protected static byte nodirection= 0;  // not satisfied
  protected static byte forward= 1;      // v2 is output

  protected BinaryConstraint() {} // this has to be here because of
                                  // Java's constructor idiocy.

  protected BinaryConstraint(Variable var1, Variable var2, Strength strength) {
    super(strength);
    v1= var1;
    v2= var2;
    direction= nodirection;
    addConstraint();
  }

  // Answer true if this constraint is satisfied in the current solution.
  public boolean isSatisfied() { return direction != nodirection; }

  // Add myself to the constraint graph.
  public void addToGraph()
  {
    v1.addConstraint(this);
    v2.addConstraint(this);
    direction= nodirection;
  }

  // Remove myself from the constraint graph.
  public void removeFromGraph()
  {
    if (v1 != null) v1.removeConstraint(this);
    if (v2 != null) v2.removeConstraint(this);
    direction= nodirection;
  }

  // Decide if I can be satisfied and which way I should flow based on
  // the relative strength of the variables I relate, and record that
  // decision.
  //
  protected void chooseMethod(int mark)
  {
    if (v1.mark == mark) 
      direction=
	v2.mark != mark && Strength.stronger(strength, v2.walkStrength)
	  ? forward : nodirection;

    if (v2.mark == mark) 
      direction=
	v1.mark != mark && Strength.stronger(strength, v1.walkStrength)
	  ? backward : nodirection;

    // If we get here, neither variable is marked, so we have a choice.
    if (Strength.weaker(v1.walkStrength, v2.walkStrength))
      direction=
	Strength.stronger(strength, v1.walkStrength) ? backward : nodirection;
    else
      direction=
	Strength.stronger(strength, v2.walkStrength) ? forward : nodirection;
  }

  // Record the fact that I am unsatisfied.
  public void markUnsatisfied() { direction= nodirection; }

  // Mark the input variable with the given mark.
  protected void markInputs(int mark)
  {
    input().mark= mark;
  }
  
  public boolean inputsKnown(int mark)
  {
    Variable i= input();
    return i.mark == mark || i.stay || i.determinedBy == null;
  }
  
  // Answer my current output variable.
  public Variable output() { return direction==forward ? v2 : v1; }

  // Answer my current input variable
  public Variable input() { return direction==forward ? v1 : v2; }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  //
  public void recalculate()
  {
    Variable in= input(), out= output();
    out.walkStrength= Strength.weakestOf(strength, in.walkStrength);
    out.stay= in.stay;
    if (out.stay) execute();
  }

  protected void printInputs()
  {
    input().print();
  }

}


// I constrain two variables to have the same value: "v1 = v2".
//
class EqualityConstraint extends BinaryConstraint {

  // Install a constraint with the given strength equating the given variables.
  public EqualityConstraint(Variable var1, Variable var2, Strength strength)
  {
    super(var1, var2, strength);
  }

  // Enforce this constraint. Assume that it is satisfied.
  public void execute() {
    output().value= input().value;
  }

}


// I relate two variables by the linear scaling relationship: "v2 =
// (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
// this relationship but the scale factor and offset are considered
// read-only.
//
class ScaleConstraint extends BinaryConstraint {

  protected Variable scale; // scale factor input variable
  protected Variable offset; // offset input variable

  // Install a scale constraint with the given strength on the given variables.
  public ScaleConstraint(Variable src, Variable scale, Variable offset,
		         Variable dest, Strength strength)
  {
    // Curse this wretched language for insisting that constructor invocation
    // must be the first thing in a method...
    // ..because of that, we must copy the code from the inherited
    // constructors.
    this.strength= strength;
    v1= src;
    v2= dest;
    direction= nodirection;
    this.scale= scale;
    this.offset= offset;
    addConstraint();
  }

  // Add myself to the constraint graph.
  public void addToGraph()
  {
    super.addToGraph();
    scale.addConstraint(this);
    offset.addConstraint(this);
  }

  // Remove myself from the constraint graph.
  public void removeFromGraph()
  {
    super.removeFromGraph();
    if (scale != null) scale.removeConstraint(this);
    if (offset != null) offset.removeConstraint(this);
  }

  // Mark the inputs from the given mark.
  protected void markInputs(int mark)
  {
    super.markInputs(mark);
    scale.mark= offset.mark= mark;
  }

  // Enforce this constraint. Assume that it is satisfied.
  public void execute()
  {
    if (direction == forward) 
      v2.value= v1.value * scale.value + offset.value;
    else
      v1.value= (v2.value - offset.value) / scale.value;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  public void recalculate()
  {
    Variable in= input(), out= output();
    out.walkStrength= Strength.weakestOf(strength, in.walkStrength);
    out.stay= in.stay && scale.stay && offset.stay;
    if (out.stay) execute(); // stay optimization
  }
}

    
// ------------------------------------------------------------


// A Plan is an ordered list of constraints to be executed in sequence
// to resatisfy all currently satisfiable constraints in the face of
// one or more changing inputs.

class Plan {

  private Vector v;

  public Plan() { v= new Vector(); }

  public void addConstraint(Constraint c) { v.addElement(c); }

  public int size() { return v.size(); }

  public Constraint constraintAt(int index) {
    return (Constraint) v.elementAt(index); }

  public void execute()
  {
    for (int i= 0; i < size(); ++i) {
      Constraint c= (Constraint) constraintAt(i);
      c.execute();
    }
  }

}


// ------------------------------------------------------------

// The DeltaBlue planner

class Planner {

  int currentMark= 0;

  // Select a previously unused mark value.
  private int newMark() { return ++currentMark; }

  public Planner()
  {
    currentMark= 0;
  }

  // Attempt to satisfy the given constraint and, if successful,
  // incrementally update the dataflow graph.  Details: If satifying
  // the constraint is successful, it may override a weaker constraint
  // on its output. The algorithm attempts to resatisfy that
  // constraint using some other method. This process is repeated
  // until either a) it reaches a variable that was not previously
  // determined by any constraint or b) it reaches a constraint that
  // is too weak to be satisfied using any of its methods. The
  // variables of constraints that have been processed are marked with
  // a unique mark value so that we know where we've been. This allows
  // the algorithm to avoid getting into an infinite loop even if the
  // constraint graph has an inadvertent cycle.
  //
  public void incrementalAdd(Constraint c)
  {
    int mark= newMark();
    Constraint overridden= c.satisfy(mark);
    while (overridden != null) {
      overridden= overridden.satisfy(mark);
    }
  }


  // Entry point for retracting a constraint. Remove the given
  // constraint and incrementally update the dataflow graph.
  // Details: Retracting the given constraint may allow some currently
  // unsatisfiable downstream constraint to be satisfied. We therefore collect
  // a list of unsatisfied downstream constraints and attempt to
  // satisfy each one in turn. This list is traversed by constraint
  // strength, strongest first, as a heuristic for avoiding
  // unnecessarily adding and then overriding weak constraints.
  // Assume: c is satisfied.
  //
  public void incrementalRemove(Constraint c)
  {
    Variable out= c.output();
    c.markUnsatisfied();
    c.removeFromGraph();
    Vector unsatisfied= removePropagateFrom(out);
    Strength strength= Strength.required;
    do {
      for (int i= 0; i < unsatisfied.size(); ++i) {
	Constraint u= (Constraint)unsatisfied.elementAt(i);
	if (u.strength == strength)
	  incrementalAdd(u);
      }
      strength= strength.nextWeaker();
    } while (strength != Strength.weakest);
  }

  // Recompute the walkabout strengths and stay flags of all variables
  // downstream of the given constraint and recompute the actual
  // values of all variables whose stay flag is true. If a cycle is
  // detected, remove the given constraint and answer
  // false. Otherwise, answer true.
  // Details: Cycles are detected when a marked variable is
  // encountered downstream of the given constraint. The sender is
  // assumed to have marked the inputs of the given constraint with
  // the given mark. Thus, encountering a marked node downstream of
  // the output constraint means that there is a path from the
  // constraint's output to one of its inputs.
  //
  public boolean addPropagate(Constraint c, int mark)
  {
    Vector todo= new Vector();
    todo.addElement(c);
    while (!todo.isEmpty()) {
      Constraint d= (Constraint)todo.elementAt(0);
      todo.removeElementAt(0);
      if (d.output().mark == mark) {
	incrementalRemove(c);
	return false;
      }
      d.recalculate();
      addConstraintsConsumingTo(d.output(), todo);
    }
    return true;
  }


  // The given variable has changed. Propagate new values downstream.
  public void propagateFrom(Variable v)
  {
    Vector todo= new Vector();
    addConstraintsConsumingTo(v, todo);
    while (!todo.isEmpty()) {
      Constraint c= (Constraint)todo.elementAt(0);
      todo.removeElementAt(0);
      c.execute();
      addConstraintsConsumingTo(c.output(), todo);
    }
  }

  // Update the walkabout strengths and stay flags of all variables
  // downstream of the given constraint. Answer a collection of
  // unsatisfied constraints sorted in order of decreasing strength.
  //
  protected Vector removePropagateFrom(Variable out)
  {
    out.determinedBy= null;
    out.walkStrength= Strength.weakest;
    out.stay= true;
    Vector unsatisfied= new Vector();
    Vector todo= new Vector();
    todo.addElement(out);
    while (!todo.isEmpty()) {
      Variable v= (Variable)todo.elementAt(0);
      todo.removeElementAt(0);
      for (int i= 0; i < v.constraints.size(); ++i) {
	Constraint c= (Constraint) v.constraints.elementAt(i);
	if (!c.isSatisfied())
	  unsatisfied.addElement(c);
      }
      Constraint determiningC= v.determinedBy;
      for (int i= 0; i < v.constraints.size(); ++i) {
	Constraint nextC= (Constraint) v.constraints.elementAt(i);
	if (nextC != determiningC && nextC.isSatisfied()) {
	  nextC.recalculate();
	  todo.addElement(nextC.output());
	}
      }
    }
    return unsatisfied;
  }

  // Extract a plan for resatisfaction starting from the outputs of
  // the given constraints, usually a set of input constraints.
  //
  protected Plan extractPlanFromConstraints(Vector constraints)
  {
    Vector sources= new Vector();
    for (int i= 0; i < constraints.size(); ++i) {
      Constraint c= (Constraint)constraints.elementAt(i);
      if (c.isInput() && c.isSatisfied())
	sources.addElement(c);
    }
    return makePlan(sources);
  }

  // Extract a plan for resatisfaction starting from the given source
  // constraints, usually a set of input constraints. This method
  // assumes that stay optimization is desired; the plan will contain
  // only constraints whose output variables are not stay. Constraints
  // that do no computation, such as stay and edit constraints, are
  // not included in the plan.
  // Details: The outputs of a constraint are marked when it is added
  // to the plan under construction. A constraint may be appended to
  // the plan when all its input variables are known. A variable is
  // known if either a) the variable is marked (indicating that has
  // been computed by a constraint appearing earlier in the plan), b)
  // the variable is 'stay' (i.e. it is a constant at plan execution
  // time), or c) the variable is not determined by any
  // constraint. The last provision is for past states of history
  // variables, which are not stay but which are also not computed by
  // any constraint.
  // Assume: sources are all satisfied.
  //
  protected Plan makePlan(Vector sources)
  {
    int mark= newMark();
    Plan plan= new Plan();
    Vector todo= sources;
    while (!todo.isEmpty()) {
      Constraint c= (Constraint)todo.elementAt(0);
      todo.removeElementAt(0);
      if (c.output().mark != mark && c.inputsKnown(mark)) {
	// not in plan already and eligible for inclusion
	plan.addConstraint(c);
	c.output().mark= mark;
	addConstraintsConsumingTo(c.output(), todo);
      }
    }
    return plan;
  }

  protected void addConstraintsConsumingTo(Variable v, Vector coll)
  {
    Constraint determiningC= v.determinedBy;
    Vector cc= v.constraints;
    for (int i= 0; i < cc.size(); ++i) {
      Constraint c= (Constraint) cc.elementAt(i);
      if (c != determiningC && c.isSatisfied())
	coll.addElement(c);
    }
  }

}

//------------------------------------------------------------

public class DeltaBlue implements Benchmark {

  private long total_ms;
  public long getRunTime() { return total_ms; }

  public static Planner planner;

  public static void main(String[] args) {
    (new DeltaBlue()).inst_main(args);
  }

  public void inst_main(String args[])
  {
    System.out.println("DeltaBlue benchmark starting...");
    int iterations= 100;
    long startTime= System.currentTimeMillis();
    for (int j= 0; j < iterations; ++j) {
      chainTest(100);
      projectionTest(100);
    }
    long endTime= System.currentTimeMillis();
    total_ms= endTime - startTime;
    System.out.println("Total time for " + iterations
		       + " iterations of chain and projection tests: "
		       + total_ms + " ms");
    System.out.println("Average time per iteration: "
		       + ((double)total_ms / iterations) + " ms");

  }



  //  This is the standard DeltaBlue benchmark. A long chain of
  //  equality constraints is constructed with a stay constraint on
  //  one end. An edit constraint is then added to the opposite end
  //  and the time is measured for adding and removing this
  //  constraint, and extracting and executing a constraint
  //  satisfaction plan. There are two cases. In case 1, the added
  //  constraint is stronger than the stay constraint and values must
  //  propagate down the entire length of the chain. In case 2, the
  //  added constraint is weaker than the stay constraint so it cannot
  //  be accomodated. The cost in this case is, of course, very
  //  low. Typical situations lie somewhere between these two
  //  extremes.
  //
  private void chainTest(int n)
  {
    planner= new Planner();

    Variable prev= null, first= null, last= null;

    // Build chain of n equality constraints
    for (int i= 0; i <= n; i++) {
      String name= "v" + Integer.toString(i);
      Variable v= new Variable(name);
      if (prev != null)
	new EqualityConstraint(prev, v, Strength.required);
      if (i == 0) first= v;
      if (i == n) last= v;
      prev= v;
    }

    new StayConstraint(last, Strength.strongDefault);
    Constraint editC= new EditConstraint(first, Strength.preferred);
    Vector editV= new Vector();
    editV.addElement(editC);
    Plan plan= planner.extractPlanFromConstraints(editV);
    for (int i= 0; i < 100; i++) {
      first.value= i;
      plan.execute();
      if (last.value != i)
	error("Chain test failed!");
    }
    editC.destroyConstraint();
  }


  // This test constructs a two sets of variables related to each
  // other by a simple linear transformation (scale and offset). The
  // time is measured to change a variable on either side of the
  // mapping and to change the scale and offset factors.
  //
  private void projectionTest(int n)
  {
    planner= new Planner();

    Variable scale= new Variable("scale", 10);
    Variable offset= new Variable("offset", 1000);
    Variable src= null, dst= null;

    Vector dests= new Vector();

    for (int i= 0; i < n; ++i) {
      src= new Variable("src" + Integer.toString(i), i);
      dst= new Variable("dst" + Integer.toString(i), i);
      dests.addElement(dst);
      new StayConstraint(src, Strength.normal);
      new ScaleConstraint(src, scale, offset, dst, Strength.required);
    }

    change(src, 17);
    if (dst.value != 1170) error("Projection test 1 failed!");

    change(dst, 1050);
    if (src.value != 5) error("Projection test 2 failed!");

    change(scale, 5);
    for (int i= 0; i < n - 1; ++i) {
      if (((Variable)dests.elementAt(i)).value != i * 5 + 1000)
	error("Projection test 3 failed!");
    }

    change(offset, 2000);
    for (int i= 0; i < n - 1; ++i) {
      if (((Variable)dests.elementAt(i)).value != i * 5 + 2000)
	error("Projection test 4 failed!");
    }
  }

  private void change(Variable var, int newValue)
  {
    EditConstraint editC= new EditConstraint(var, Strength.preferred);
    Vector editV= new Vector();
    editV.addElement(editC);
    Plan plan= planner.extractPlanFromConstraints(editV);
    for (int i= 0; i < 10; i++) {
      var.value= newValue;
      plan.execute();
    }
    editC.destroyConstraint();
  }

  public static void error(String s)
  {
    System.err.println(s);
    System.exit(1);
  }

}

