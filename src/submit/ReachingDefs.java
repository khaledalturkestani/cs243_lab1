package submit;

// some useful things to import. add any additional imports you need.
import joeq.Compiler.Quad.*;
import flow.Flow;
import joeq.Compiler.Quad.Operand.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.List;
	
/**
 * Skeleton class for implementing a reaching definition analysis
 * using the Flow.Analysis interface.
 */
public class ReachingDefs implements Flow.Analysis {

    /**
     * Class for the dataflow objects in the ReachingDefs analysis.
     * You are free to change this class or move it to another file.
     */
    public class MyDataflowObject implements Flow.DataflowObject {
		private HashMap<String, Integer> defs;
		//private int state; // 0 = udef;
        /**
         * Methods from the Flow.DataflowObject interface.
         * See Flow.java for the meaning of these methods.
         * These need to be filled in.
         */
		public MyDataflowObject() {
			defs = new HashMap<String, Integer>();
		}
		public void setToTop() {
			//setUndef();
		}
        public void setToBottom() {
        	//setNAC();
        }
        public void meetWith (Flow.DataflowObject o) {
			Iterator tmpItr = defs.keySet().iterator();
			//System.out.println("Meeting with: "+((MyDataflowObject)o).toString());
			String tmpStr = "[";
			while (tmpItr.hasNext()) {
				String v = (String)tmpItr.next();
				tmpStr = tmpStr + v + ", ";
			}
			tmpStr += "]";
			//System.out.println(tmpStr);
			
			//System.out.println("Before: "+this.toString());
			HashMap<String, Integer> defsToMeetWith = ((MyDataflowObject)o).getDefs();
			Iterator itr = defsToMeetWith.keySet().iterator();
			String str = "[";
			while (itr.hasNext()) {
				String definedVar = (String)itr.next();
				str = str + definedVar + ", ";
				//if (!defs.containsKey(definedVar))
				defs.put(definedVar, defsToMeetWith.get(definedVar));
			}
			str += "]";
			//System.out.println(str);
			//System.out.println("After: "+this.toString());
        	//defs.putAll(defsToMeetWith);
        }
        public void copy (Flow.DataflowObject o) {
			defs.clear();
        	defs.putAll(((MyDataflowObject)o).getDefs());
        }
			
		public HashMap<String, Integer> getDefs() {
			return new HashMap(defs);
		}
		
		public void setDefs(HashMap<String, Integer> d) {
			defs.clear();
			defs.putAll(d);
		}
		
        @Override
        public boolean equals (Object o) {
            if (o instanceof MyDataflowObject) {
                MyDataflowObject a = (MyDataflowObject) o;
                if (defs.equals(a.getDefs())) {
                    return true;
				}
				return false;
            }
            return false;
        }

        /**
         * toString() method for the dataflow objects which is used
         * by postprocess() below.  The format of this method must
         * be of the form "[ID0, ID1, ID2, ...]", where each ID is
         * the identifier of a quad defining some register, and the
         * list of IDs must be sorted.  See src/test/test.rd.out
         * for example output of the analysis.  The output format of
         * your reaching definitions analysis must match this exactly.
         */
        @Override
        public String toString() { 
			TreeSet<Integer> sortedDefs = new TreeSet<Integer>();
			Iterator defsItr = defs.keySet().iterator();
			while (defsItr.hasNext()) {
				String definedVar = (String)defsItr.next();
				Integer defNum = defs.get(definedVar);
				sortedDefs.add(defNum);
			}
			String str = "[";
			Iterator sortedDefsItr = sortedDefs.iterator();
			while (sortedDefsItr.hasNext()) {
				Integer defNum = (Integer)sortedDefsItr.next();
				str += defNum.intValue();
				if (sortedDefsItr.hasNext())
					str += ", ";
			}
			str += "]";
			return str; 
		}
    }

    /**
     * Dataflow objects for the interior and entry/exit points
     * of the CFG. in[ID] and out[ID] store the entry and exit
     * state for the input and output of the quad with identifier ID.
     *
     * You are free to modify these fields, just make sure to
     * preserve the data printed by postprocess(), which relies on these.
     */
    private MyDataflowObject[] in, out;
    private MyDataflowObject entry, exit;

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
        in = new MyDataflowObject[max];
        out = new MyDataflowObject[max];

        // initialize the contents of in and out.
        qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            int id = qit.next().getID();
            in[id] = new MyDataflowObject();
            out[id] = new MyDataflowObject();
        }

        // initialize the entry and exit points.
        entry = new MyDataflowObject();
        exit = new MyDataflowObject();

        /************************************************
         * Your remaining initialization code goes here *
         ************************************************/
		/*
		QuadIterator quadItr = new QuadIterator(cfg);
		while (quadItr.hasNext()) {
			Quad q = quadItr.next();
			int id = q.getID();
			System.out.println("ID: "+id);
            for (RegisterOperand def : q.getDefinedRegisters()) {
                System.out.println(def.getRegister().toString());
            }
		}	*/
			
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
        for (int i=0; i<in.length; i++) {
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
    public boolean isForward () { return true; }
    public Flow.DataflowObject getEntry() { return entry; }
    public Flow.DataflowObject getExit() { return exit; }
    public void setEntry(Flow.DataflowObject value) { entry.copy(value); }
    public void setExit(Flow.DataflowObject value) { exit.copy(value); }
    public Flow.DataflowObject getIn(Quad q) { return in[q.getID()]; }
    public Flow.DataflowObject getOut(Quad q) { return out[q.getID()]; }
    public void setIn(Quad q, Flow.DataflowObject value) {
    	//in[q.getID()].clear();
		in[q.getID()].copy(value);
    }
    public void setOut(Quad q, Flow.DataflowObject value) {
    	//out[q.getID()].clear();
		out[q.getID()].copy(value);
    }
    public Flow.DataflowObject newTempVar() { return new MyDataflowObject(); }
    public void processQuad(Quad q) {
		MyDataflowObject outDefs = new MyDataflowObject();
		outDefs.copy(in[q.getID()]);
    	HashMap<String, Integer> defs = outDefs.getDefs();
        for (RegisterOperand def : q.getDefinedRegisters()) {
			//System.out.println("def reg: "+ def.toString());
			defs.put(def.getRegister().toString(), new Integer(q.getID()));
        }
		
		/*List<RegisterOperand> regs  = q.getDefinedRegisters();
		if (regs.size() > 0)
			defs.put(regs.get(0).toString(), new Integer(q.getID()));
		*/
		outDefs.setDefs(defs);
		out[q.getID()].copy(outDefs);
    }
}
