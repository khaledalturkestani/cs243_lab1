package submit;

// some useful things to import. add any additional imports you need.
import joeq.Compiler.Quad.*;
import flow.Flow;
import java.util.Iterator;

/**
 * Skeleton class for implementing the Flow.Solver interface.
 */
public class MySolver implements Flow.Solver {

    protected Flow.Analysis analysis;

    /**
     * Sets the analysis.  When visitCFG is called, it will
     * perform this analysis on a given CFG.
     *
     * @param analyzer The analysis to run
     */
    public void registerAnalysis(Flow.Analysis analyzer) {
        this.analysis = analyzer;
    }

    /**
     * Runs the solver over a given control flow graph.  Prior
     * to calling this, an analysis must be registered using
     * registerAnalysis
     *
     * @param cfg The control flow graph to analyze.
     */
    public void visitCFG(ControlFlowGraph cfg) {

        // this needs to come first.
        analysis.preprocess(cfg);

		QuadIterator cfgItr = new QuadIterator(cfg);
		
		// Set all the OUTs to NULL.
		/*while (cfgItr.hasNext()) {
			Quad q = cfgItr.next();
			analysis.setOut(q, new Flow.DataflowObject());
		}*/
		
		// entry = analysis.getEntry(cfg);
		
		
		Quad q;
		Flow.DataflowObject in = analysis.getEntry();
		Flow.DataflowObject out = analysis.getExit();
		boolean repeatLoop = true;
		while (cfgItr.hasNext() && repeatLoop == true){
			// TODO: take the union of ALL the predecessors.
			// TODO: Ask about multiple branches.
			repeatLoop = false;
			q = cfgItr.next();
			Flow.DataflowObject prevOut = analysis.getOut(q);
			
			Iterator predecessors = cfgItr.predecessors();
			Quad pred = (Quad)predecessors.next();
			Flow.DataflowObject ins;
			if (pred != null) {
				ins = analysis.getOut(pred);
				while (predecessors.hasNext()) {
					pred = (Quad)predecessors.next();
					ins.meetWith(analysis.getOut(pred));
				}
			} else {
				ins = in;
			}
			
			analysis.setIn(q, ins);
			//analysis.setIn(q, in);
			analysis.processQuad(q);
			out = analysis.getOut(q);
			if (!out.equals(prevOut)) {
				repeatLoop = true;
				System.out.println("TRUE!!!!!!");
			}
			in = out;
			//prevIns = out;
		}
		
        // this needs to come last.
        analysis.postprocess(cfg);
    }
}
