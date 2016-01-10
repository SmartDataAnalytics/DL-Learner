package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public interface LGGGenerator {
	
	/**
	 * Returns the Least General Generalization of two RDF resource trees.
	 * @param tree1 the first tree
	 * @param tree2 the second tree
	 * @return the Least General Generalization
	 */
	RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2);
	
	/**
	 * Returns the Least General Generalization of two RDF resource trees.
	 * @param tree1 the first tree
	 * @param tree2 the second tree
	 * @return the Least General Generalization
	 */
	RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters);
	
	/**
	 * Returns the Least General Generalization of a list of RDF resource trees.
	 * @param trees the trees
	 * @return the Least General Generalization
	 */
	RDFResourceTree getLGG(List<RDFResourceTree> trees);
	
	/**
	 * Returns the Least General Generalization of a list of RDF resource trees. It can be forced to learn filters
	 * on literal values.
	 *
	 * @param trees the trees
	 * @return the Least General Generalization
	 *///todo add better explanation
	RDFResourceTree getLGG(List<RDFResourceTree> trees, boolean learnFilters);

}
