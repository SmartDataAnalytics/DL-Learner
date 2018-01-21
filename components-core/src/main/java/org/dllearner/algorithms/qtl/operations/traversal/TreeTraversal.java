package org.dllearner.algorithms.qtl.operations.traversal;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.util.Iterator;

/**
 * An iterator for traversal on the nodes in the query tree.
 *
 * @author Lorenz Buehmann
 */
public interface TreeTraversal<T extends GenericTree> extends Iterator<T> {
}
