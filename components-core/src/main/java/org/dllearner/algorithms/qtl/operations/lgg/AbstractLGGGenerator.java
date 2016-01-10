package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.List;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.core.AbstractReasonerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractLGGGenerator implements LGGGenerator {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private Monitor mon = MonitorFactory.getTimeMonitor("lgg");
	
	protected int subCalls;
	
	protected Entailment entailment = Entailment.SIMPLE;
	protected AbstractReasonerComponent reasoner;
	

	private void reset() {
		subCalls = 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator2#getLGG(org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree, org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree)
	 */
	@Override
	public RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2) {
		return getLGG(tree1, tree2, false);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator2#getLGG(org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree, org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree, boolean)
	 */
	@Override
	public RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters) {
		reset();
		
		// apply some pre-processing
		preProcess(tree1);
		
		// compute the LGG
		mon.start();
		RDFResourceTree lgg = computeLGG(tree1, tree2, learnFilters);
		mon.stop();
		
		// apply some post-processing
		postProcess(lgg);
		
		return lgg;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator2#getLGG(java.util.List)
	 */
	@Override
	public RDFResourceTree getLGG(List<RDFResourceTree> trees) {
		return getLGG(trees, false);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator2#getLGG(java.util.List, boolean)
	 */
	@Override
	public RDFResourceTree getLGG(List<RDFResourceTree> trees, boolean learnFilters) {
		// if there is only 1 tree return it
		if (trees.size() == 1) {
			return trees.get(0);
		}

		// lgg(t_1, t_n)
		mon.start();
		RDFResourceTree lgg = trees.get(0);
		for (int i = 1; i < trees.size(); i++) {
			lgg = getLGG(lgg, trees.get(i), learnFilters);
		}
		mon.stop();

		addNumbering(0, lgg);

		return lgg;
	}
	
	protected void postProcess(RDFResourceTree tree) {
		// prune the tree according to the given entailment
		QueryTreeUtils.prune(tree, reasoner, entailment);
		
		addNumbering(0, tree);
	}
	
	protected void preProcess(RDFResourceTree tree) {
		
	}
	

	private void addNumbering(int nodeId, RDFResourceTree tree){
//		tree.setId(nodeId);
		for(RDFResourceTree child : tree.getChildren()){
			addNumbering(nodeId++, child);
		}
	}
	
	protected abstract RDFResourceTree computeLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters);

}
