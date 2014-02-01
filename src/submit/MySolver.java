package submit;

// some useful things to import. add any additional imports you need.
import joeq.Compiler.Quad.*;
import flow.Flow;
import java.util.Iterator;
import java.util.HashSet;

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

		//QuadIterator cfgItr = new QuadIterator(cfg);
		
		// Set all the OUTs to NULL.
		/*while (cfgItr.hasNext()) {
			Quad q = cfgItr.next();
			analysis.setOut(q, new Flow.DataflowObject());
		}*/
		// entry = analysis.getEntry(cfg);
		
		boolean repeatLoop = true;
		int loopCounter = 1;
		HashSet<Quad> exitIns;
		while (repeatLoop == true){
			repeatLoop = false;
			//System.out.println("# loops: "+loopCounter);
			exitIns = new HashSet<Quad>();
			QuadIterator cfgItr = new QuadIterator(cfg);
			Quad q;
			Flow.DataflowObject in = analysis.getEntry();
			Flow.DataflowObject out = analysis.getExit();
			while (cfgItr.hasNext()) {
				// TODO: take the union of ALL the predecessors.
				// TODO: Ask about multiple branches.
				q = cfgItr.next();
				Flow.DataflowObject prevIn = analysis.getIn(q);
				Flow.DataflowObject prevOut = analysis.getOut(q);
				Iterator predecessors = cfgItr.predecessors();
				Quad pred = (Quad)predecessors.next();
				//predecessors.remove();
				//int numPreds = 1;
				Flow.DataflowObject ins;
				if (pred != null) {
					//System.out.println("pred is NOT NULL");
					ins = analysis.getOut(pred);
					while (predecessors.hasNext()) {
						pred = (Quad)predecessors.next();
						ins.meetWith(analysis.getOut(pred));
					}
				} else {
					//System.out.println("pred is NULL");
					//ins.meetWtih(analysis.getEntry());
					ins = in;
				}				
				if (!ins.equals(prevIn)) {
					repeatLoop = true;
				}
				
				Iterator successorsItr = cfgItr.successors();
				while (successorsItr.hasNext()) {
					Quad suc = (Quad)successorsItr.next();
					if (suc == null) {
						exitIns.add(q);
					}
				}
				
				analysis.setIn(q, ins);
				analysis.processQuad(q);
				out = analysis.getOut(q);
				if (!out.equals(prevOut)) {
					repeatLoop = true;
				}
				in = out;
				
				//prevIns = out;
			}
			loopCounter++;
			Iterator exitItr = exitIns.iterator();
			Flow.DataflowObject exitVal = analysis.getOut((Quad)exitItr.next());
			while (exitItr.hasNext()) {
				exitVal.meetWith(analysis.getOut((Quad)exitItr.next()));
			}
			analysis.setExit(exitVal);
		}
		
		
		
		
        // this needs to come last.
        analysis.postprocess(cfg);
    }
}
