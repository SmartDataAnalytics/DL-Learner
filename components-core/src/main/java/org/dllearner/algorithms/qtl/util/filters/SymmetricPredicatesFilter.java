package org.dllearner.algorithms.qtl.util.filters;

import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.operations.traversal.PostOrderTreeTraversal;
import org.dllearner.algorithms.qtl.operations.traversal.PostOrderTreeTraversal2;
import org.dllearner.algorithms.qtl.operations.traversal.PreOrderTreeTraversal;
import org.dllearner.algorithms.qtl.operations.traversal.TreeTraversal;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 * Drops edges v1 -p-> v2 if
 * p is symmetric and
 * there is an edge v -p-> v1 with l(v)=l(v2)
 *
 * @author Lorenz Buehmann
 */
public class SymmetricPredicatesFilter extends AbstractTreeFilter<RDFResourceTree> {

    private final Set<Node> symmetricPredicates;

    public SymmetricPredicatesFilter(Set<Node> symmetricPredicates) {
        this.symmetricPredicates = symmetricPredicates;
    }

    public boolean isSymmetric(Node edge) {
        return symmetricPredicates != null && symmetricPredicates.contains(edge);
    }

    @Override
    public RDFResourceTree apply(RDFResourceTree tree) {
        RDFResourceTree newTree = new RDFResourceTree(tree, true);

        TreeTraversal<RDFResourceTree> it = new PostOrderTreeTraversal2<>(tree);

        while(it.hasNext()) {
            RDFResourceTree child = it.next();

            if(!child.isRoot()) {
                Node edge = child.getEdgeToParent();

                boolean incoming = edge instanceof NodeInv;

                if(incoming) {
                    edge = ((NodeInv) edge).getNode();
                }

                if(isSymmetric(edge)) {
                    RDFResourceTree parent = child.getParent();
                    System.out.println(parent + "(" + parent.getID() + ") --" + edge + "--" + child + "(" + child.getID() + ")");

                    List<RDFResourceTree> children;
                    if(incoming) {
                        children = parent.getChildren(edge);
                    } else {
                        children = parent.getChildren(new NodeInv(edge));
                    }

                    boolean subsumed = children != null && children.stream().anyMatch(otherChild -> QueryTreeUtils.isSubsumedBy(child, otherChild));

                    if(children != null) {
                        for (RDFResourceTree otherChild : children) {
                            System.out.println(otherChild + " (" + otherChild.getID() + "):" + QueryTreeUtils.isSubsumedBy(child, otherChild));
                        }
                    }

                    if(subsumed) {
                        it.remove();
                    }
                }
            }
        }

        return newTree;
    }
}
