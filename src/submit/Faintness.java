package submit;
// blah
// some useful things to import. add any additional imports you need.
import joeq.Compiler.Quad.*;
import flow.Flow;
import java.util.*;
import joeq.Compiler.Quad.Operand.*;

/**
 * Skeleton class for implementing a faint variable analysis
 * using the Flow.Analysis interface.
 */
public class Faintness implements Flow.Analysis {

    /**
     * Class for the dataflow objects in the Faintness analysis.
     * You are free to change this class or move it to another file.
     */
    public static class VarSet implements Flow.DataflowObject {
        private Set<String> set;
        public static Set<String> universalSet;
        public VarSet() { set = new TreeSet<String>(); }
        /**
         * Methods from the Flow.DataflowObject interface.
         * See Flow.java for the meaning of these methods.
         * These need to be filled in.
         */
        public void setToTop() { set = new TreeSet<String>(universalSet); }
        public void setToBottom() { set = new TreeSet<String>(); }
        public void meetWith (Flow.DataflowObject o) { 

            VarSet a = (VarSet)o;
            set.retainAll(a.set);

        }

        public boolean contains(String register) {

            return set.contains(register);
       
        }

        public void copy (Flow.DataflowObject o) {

            VarSet a = (VarSet) o;
            set = new TreeSet<String>(a.set);


        }

        /**
         * toString() method for the dataflow objects which is used
         * by postprocess() below.  The format of this method must
         * be of the form "[REG0, REG1, REG2, ...]", where each REG is
         * the identifier of a register, and the list of REGs must be sorted.
         * See src/test/TestFaintness.out for example output of the analysis.
         * The output format of your reaching definitions analysis must
         * match this exactly.
         */
        @Override
        public String toString() { return set.toString(); }

        @Override
        public int hashCode() {
            return set.hashCode();
        }

        @Override
        public boolean equals(Object o) 
        {
            if (o instanceof VarSet) 
            {
                VarSet a = (VarSet) o;
                return set.equals(a.set);
            }
            return false;
        }

        public void genVar(String v) {set.add(v);}
        public void killVar(String v) {set.remove(v);}
    }

    /**
     * Dataflow objects for the interior and entry/exit points
     * of the CFG. in[ID] and out[ID] store the entry and exit
     * state for the input and output of the quad with identifier ID.
     *
     * You are free to modify these fields, just make sure to
     * preserve the data printed by postprocess(), which relies on these.
     */
    private VarSet[] in, out;
    private VarSet entry, exit;

    /**
     * This method initializes the datflow framework.
     *
     * @param cfg  The control flow graph we are going to process.
     */
    public void preprocess(ControlFlowGraph cfg) {
        // this line must come first.
        System.out.println("Method: "+cfg.getMethod().getName().toString());

        // get the amount of space we need to allocate for the in/out arrays.
        QuadIterator qit = new QuadIterator(cfg);
        int max = 0;
        while (qit.hasNext()) {
            int id = qit.next().getID();
            if (id > max) 
                max = id;
        }
        max += 1;

        // allocate the in and out arrays.
        in = new VarSet[max];
        out = new VarSet[max];
        qit = new QuadIterator(cfg);

        Set<String> s = new TreeSet<String>();
        VarSet.universalSet = s;

        /* Arguments are always there. */
        int numargs = cfg.getMethod().getParamTypes().length;
        for (int i = 0; i < numargs; i++) {
            s.add("R"+i);
        }

        while (qit.hasNext()) {
            Quad q = qit.next();
            for (RegisterOperand def : q.getDefinedRegisters()) {
                s.add(def.getRegister().toString());
            }
            for (RegisterOperand use : q.getUsedRegisters()) {
                s.add(use.getRegister().toString());
            }
        }

        // initialize the entry and exit points.
        entry = new VarSet();
        exit = new VarSet();
        exit.setToTop();
       
        transferfn.val = new VarSet();

        // initialize the contents of in and out.
        qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            int id = qit.next().getID();
            in[id] = new VarSet();
            in[id].setToTop();
            out[id] = new VarSet();
        }

        System.out.println("Initialization completed.");


        /************************************************
         * Your remaining initialization code goes here *
         ************************************************/
    }

    /**
     * This method is called after the fixpoint is reached.
     * It must print out the dataflow objects associated with
     * the entry, exit, and all interior points of the CFG.
     * Unless you modify in, out, entry, or exit you shouldn't
     * need to change this method.
     *
     * @param cfg  Unused.
     */
    public void postprocess (ControlFlowGraph cfg) {
        System.out.println("entry: " + entry.toString());
        for (int i=1; i<in.length; i++) {
            if (in[i] != null) {
                System.out.println(i + " in:  " + in[i].toString());
                System.out.println(i + " out: " + out[i].toString());
            }
        }
        System.out.println("exit: " + exit.toString());
    }

    /**
     * Other methods from the Flow.Analysis interface.
     * See Flow.java for the meaning of these methods.
     * These need to be filled in.
     */
    public boolean isForward () { return false; }

    public Flow.DataflowObject getEntry() { 

        Flow.DataflowObject result = newTempVar();
        result.copy(entry); 
        return result;

    }

    public Flow.DataflowObject getExit() { 

        Flow.DataflowObject result = newTempVar();
        result.copy(exit); 
        return result;

    }

    public void setEntry(Flow.DataflowObject value) { entry.copy(value); }

    public void setExit(Flow.DataflowObject value) { exit.copy(value); }

    public Flow.DataflowObject getIn(Quad q) { 

        Flow.DataflowObject result = newTempVar();
        result.copy(in[q.getID()]); 
        return result;

    }

    public Flow.DataflowObject getOut(Quad q) { 

        Flow.DataflowObject result = newTempVar();
        result.copy(out[q.getID()]); 
        return result;

    }

    public void setIn(Quad q, Flow.DataflowObject value) { in[q.getID()].copy(value); }
    
    public void setOut(Quad q, Flow.DataflowObject value) { out[q.getID()].copy(value); }

    public Flow.DataflowObject newTempVar() { return new VarSet(); }

    /* Actually perform the transfer operation on the relevant
     * quad. */
    
    private TransferFunction transferfn = new TransferFunction ();

    public void processQuad(Quad q) {
        transferfn.val.copy(out[q.getID()]);
        transferfn.visitQuad(q);
        in[q.getID()].copy(transferfn.val);
    }

    /* The QuadVisitor that actually does the computation */
    public static class TransferFunction extends QuadVisitor.EmptyVisitor {
        VarSet val;
        @Override
        public void visitQuad(Quad q) {

            Operator opr = q.getOperator();

            String dest = "";
			//System.out.println(dest);
            for (RegisterOperand def : q.getDefinedRegisters()) {

                dest = def.getRegister().toString();               

            } 
			
            // Faintness propagation to variables in used registers
            // if operator is Move or Binary
            if (!val.contains(dest) || !((opr instanceof Operator.Move) || (opr instanceof Operator.Binary))) {           
                for (RegisterOperand use : q.getUsedRegisters()) {
                    val.killVar(use.getRegister().toString());
			    } 
   		    } 

            // Variables in defined registers are dead (faint)
            for (RegisterOperand def : q.getDefinedRegisters()) {
				val.genVar(def.getRegister().toString());
			} 			
        }
    }

}
