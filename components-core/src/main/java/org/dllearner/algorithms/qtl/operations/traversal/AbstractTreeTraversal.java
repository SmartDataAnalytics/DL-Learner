package org.dllearner.algorithms.qtl.operations.traversal;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;

import java.util.Objects;

/**
 * @author Lorenz Buehmann
 */
public abstract class AbstractTreeTraversal<T extends GenericTree> implements TreeTraversal<T> {

    protected final T tree;

    public AbstractTreeTraversal(T tree) {
        this.tree = Objects.requireNonNull(tree, "tree must not be null");
    }

    public T getTree() {
        return tree;
    }
}
