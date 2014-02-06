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
		if (analysis.isForward()) {
			processForward(cfg);
		} else {
			processBackward(cfg);
		}
       
    }
	
	private void processForward(ControlFlowGraph cfg) {
        // this needs to come first.
        analysis.preprocess(cfg);
		boolean repeatLoop = true;
		HashSet<Quad> exitIns;
		//int counter = 1;
		while (repeatLoop == true){
			//System.out.println("counter: "+counter++);
			repeatLoop = false;
			exitIns = new HashSet<Quad>();
			QuadIterator cfgItr = new QuadIterator(cfg);
			Quad q;
			Flow.DataflowObject in = analysis.getEntry();
			Flow.DataflowObject out = analysis.getExit();
			
			while (cfgItr.hasNext()) {
				//System.out.println("got here");
				q = cfgItr.next();
				Flow.DataflowObject prevIn = analysis.getIn(q);
				Flow.DataflowObject prevOut = analysis.getOut(q);
				Iterator predecessors = cfgItr.predecessors();
				Quad pred = (Quad)predecessors.next();
				Flow.DataflowObject ins;
				if (pred != null) {
					ins = analysis.getOut(pred);
					while (predecessors.hasNext()) {
						pred = (Quad)predecessors.next();
						//if (pred == null)
						//	System.out.println("pred is NULL");
						//else System.out.println("pred is NOT NULL");
						//System.out.println("Quad in: "+pred.getID());
						ins.meetWith(analysis.getOut(pred));
					}
				} else {
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
			}
			Iterator exitItr = exitIns.iterator();
			Flow.DataflowObject exitVal = analysis.getOut((Quad)exitItr.next());
			while (exitItr.hasNext()) {
				exitVal.meetWith(analysis.getOut((Quad)exitItr.next()));
			}
			analysis.setExit(exitVal);
			//System.out.println("\n\n\n\n");	
		}
        // this needs to come last.
        analysis.postprocess(cfg);
	}
	
	private void processBackward(ControlFlowGraph cfg) {
        // this needs to come first.
        analysis.preprocess(cfg);
		boolean repeatLoop = true;
		HashSet<Quad> entryOuts;
		while (repeatLoop == true){
			repeatLoop = false;
			entryOuts = new HashSet<Quad>();
			QuadIterator cfgItr = new QuadIterator(cfg, false);
			Quad q;
			Flow.DataflowObject in = analysis.getEntry();
			Flow.DataflowObject out = analysis.getExit();
			
			while (cfgItr.hasPrevious()) {
				//System.out.println("got here");
				q = cfgItr.previous();
				Flow.DataflowObject prevIn = analysis.getIn(q);
				Flow.DataflowObject prevOut = analysis.getOut(q);
				
				Iterator successorsItr = cfgItr.successors();
				Quad suc = (Quad)successorsItr.next();
				Flow.DataflowObject outs;
				if (suc != null) {
					outs = analysis.getIn(suc);
					//while (successorsItr.hasNext()) {
					while (successorsItr.hasNext()) {
						//System.out.println(successorsItr.hasNext());
						suc = (Quad)successorsItr.next();
						if (suc != null)
							outs.meetWith(analysis.getIn(suc));
					}
				} else {
					outs = out;
				}				
				if (!outs.equals(prevOut)) {
					repeatLoop = true;
				}
				
				Iterator predecessorsItr = cfgItr.predecessors();
				while (predecessorsItr.hasNext()) {
					Quad pred = (Quad)predecessorsItr.next();
					if (pred == null) {
						entryOuts.add(q);
					}
				}
				
				analysis.setOut(q, outs);
				analysis.processQuad(q);
				in = analysis.getIn(q);
				if (!in.equals(prevIn)) {
					repeatLoop = true;
				}
				out = in;
			
			
			}
			Iterator entryItr = entryOuts.iterator();
			Flow.DataflowObject entryVal = analysis.getIn((Quad)entryItr.next());
			while (entryItr.hasNext()) {
				//System.out.println("GOT HERE");
				entryVal.meetWith(analysis.getIn((Quad)entryItr.next()));
			}
			analysis.setEntry(entryVal);
		}
        // this needs to come last.
        analysis.postprocess(cfg);
	}
}
