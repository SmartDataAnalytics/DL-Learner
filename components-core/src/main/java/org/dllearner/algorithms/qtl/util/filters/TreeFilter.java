package org.dllearner.algorithms.qtl.util.filters;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;

/**
 * A filter for trees.
 *
 * @author Lorenz Buehmann
 */
public interface TreeFilter<T extends GenericTree> {

    /**
     * Apply the filter and return a new filtered tree.
     * @param tree the input tree
     * @return a new filtered tree
     */
    T apply(T tree);
}
