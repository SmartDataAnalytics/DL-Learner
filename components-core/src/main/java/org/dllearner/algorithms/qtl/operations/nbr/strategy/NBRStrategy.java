package org.dllearner.algorithms.qtl.operations.nbr.strategy;

import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;


/**
 * A strategy used to apply negative-based reduction on query trees.
 * @author Lorenz Bühmann
 *
 */
public interface NBRStrategy {
	
	RDFResourceTree computeNBR(RDFResourceTree posExampleTree, List<RDFResourceTree> negExampleTrees);
	
	List<RDFResourceTree> computeNBRs(RDFResourceTree posExampleTree, List<RDFResourceTree> negExampleTrees);

}
