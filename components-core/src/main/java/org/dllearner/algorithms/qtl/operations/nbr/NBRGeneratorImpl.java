package org.dllearner.algorithms.qtl.operations.nbr;

import java.util.Collections;
import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.operations.nbr.strategy.BruteForceNBRStrategy;
import org.dllearner.algorithms.qtl.operations.nbr.strategy.NBRStrategy;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class NBRGeneratorImpl implements NBRGenerator{
	
	NBRStrategy strategy;
	
	public NBRGeneratorImpl(){
		this.strategy = new BruteForceNBRStrategy();
	}
	
	public NBRGeneratorImpl(NBRStrategy strategy){
		this.strategy = strategy;
	}

	@Override
	public RDFResourceTree getNBR(RDFResourceTree posExampleTree,
			RDFResourceTree negExampleTree) {
		return strategy.computeNBR(posExampleTree, Collections.singletonList(negExampleTree));
	}

	@Override
	public RDFResourceTree getNBR(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees) {
		return strategy.computeNBR(posExampleTree, negExampleTrees);
	}

	@Override
	public List<RDFResourceTree> getNBRs(RDFResourceTree posExampleTree,
			RDFResourceTree negExampleTree) {
		return strategy.computeNBRs(posExampleTree, Collections.singletonList(negExampleTree));
	}

	@Override
	public List<RDFResourceTree> getNBRs(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees) {
		return strategy.computeNBRs(posExampleTree, negExampleTrees);
	}

	@Override
	public List<RDFResourceTree> getNBRs(RDFResourceTree posExampleTree,
			RDFResourceTree negExampleTree, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RDFResourceTree> getNBRs(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees, int limit) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
