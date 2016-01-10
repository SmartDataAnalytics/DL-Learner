package org.dllearner.algorithms.qtl.operations.nbr;

import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

/**
 * Negative based reduction of query trees.
 * @author Lorenz Bühmann
 *
 */
public interface NBRGenerator {
	
	RDFResourceTree getNBR(RDFResourceTree posExampleTree, RDFResourceTree negExampleTree);
	
	RDFResourceTree getNBR(RDFResourceTree posExampleTree, List<RDFResourceTree> negExampleTrees);
	
	List<RDFResourceTree> getNBRs(RDFResourceTree posExampleTree, RDFResourceTree negExampleTree);
	
	List<RDFResourceTree> getNBRs(RDFResourceTree posExampleTree, List<RDFResourceTree> negExampleTrees);
	
	List<RDFResourceTree> getNBRs(RDFResourceTree posExampleTree, RDFResourceTree negExampleTree, int limit);
	
	List<RDFResourceTree> getNBRs(RDFResourceTree posExampleTree, List<RDFResourceTree> negExampleTrees, int limit);
	

}
