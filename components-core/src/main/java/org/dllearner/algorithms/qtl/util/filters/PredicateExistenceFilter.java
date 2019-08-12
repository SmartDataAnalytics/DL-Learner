/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 * <p>
 * This file is part of DL-Learner.
 * <p>
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.util.filters;

import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.Entailment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * A query tree filter that removes edges whose existence is supposed to be
 * semantically meaningless from user perspective.
 *
 * @author Lorenz Buehmann
 *
 */
public class PredicateExistenceFilter extends AbstractTreeFilter<RDFResourceTree> {

    private Set<Node> existentialMeaninglessProperties = new HashSet<>();

    public PredicateExistenceFilter() {}

    public PredicateExistenceFilter(Set<Node> existentialMeaninglessProperties) {
        this.existentialMeaninglessProperties = existentialMeaninglessProperties;
    }

    /**
     * @param existentialMeaninglessProperties the existential meaningless properties
     */
    public void setExistentialMeaninglessProperties(Set<Node> existentialMeaninglessProperties) {
        this.existentialMeaninglessProperties = existentialMeaninglessProperties;
    }

    public boolean isMeaningless(Node predicate) {
        return existentialMeaninglessProperties.contains(predicate);
    }

    @Override
    public RDFResourceTree apply(RDFResourceTree tree) {
        RDFResourceTree newTree = new RDFResourceTree(tree, false);

//        if (tree.isLiteralNode() && !tree.isLiteralValueNode()) {
//            newTree = new RDFResourceTree(tree.getDatatype());
//        } else {
//            newTree = new RDFResourceTree(0);
//            newTree.setData(tree.getData());
//        }
//        newTree.setAnchorVar(tree.getAnchorVar());

        for (Node edge : tree.getEdges()) {
            // get the label if it's an incoming edge
            Node edgeLabel = edge instanceof NodeInv ? ((NodeInv) edge).getNode() : edge;

            // properties that are marked as "meaningless"
            if (isMeaningless(edgeLabel)) {
                // if the edge is meaningless
                // 1. process all children
                for (RDFResourceTree child : tree.getChildren(edge)) {
                    // add edge if child is resource, literal or a nodes that has to be kept
                    if (child.isResourceNode() || child.isLiteralValueNode() || nodes2Keep.contains(child.getAnchorVar())) {
                        RDFResourceTree newChild = apply(child);
                        newTree.addChild(newChild, edge);
                    } else {
                        // else recursive call and then check if there is no more child attached, i.e. it's just a leaf with a variable as label
                        RDFResourceTree newChild = apply(child);
                        SortedSet<Node> childEdges = newChild.getEdges();
                        if (!childEdges.isEmpty() && !(childEdges.size() == 1 && childEdges.contains(RDF.type.asNode()))) {
                            newTree.addChild(newChild, edge);
                        }
                    }
                }
            } else {
                // all other properties
                for (RDFResourceTree child : tree.getChildren(edge)) {
                    RDFResourceTree newChild = apply(child);
                    newTree.addChild(newChild, edge);
                }
            }
        }

        // we have to run the subsumption check one more time to prune the tree
        QueryTreeUtils.prune(newTree, null, Entailment.RDFS);
        return newTree;
    }
}
