/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.util.filters;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.semanticweb.owlapi.vocab.DublinCoreVocabulary;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * A query tree filter that removes edges whose existence is supposed to be
 * semantically meaningless from user perspective.
 *
 * @author Lorenz Buehmann
 *
 */
public class PredicateExistenceFilter implements TreeFilter<RDFResourceTree>{
	

	private Set<Node> existentialMeaninglessProperties = new HashSet<>();
	
	public PredicateExistenceFilter() {
		existentialMeaninglessProperties.addAll(Arrays.stream(SKOSVocabulary.values()).map(v -> NodeFactory.createURI(v.getIRI().toString())).collect(
				Collectors.toSet()));
		existentialMeaninglessProperties.addAll(Arrays.stream(
				DublinCoreVocabulary.values()).map(v -> NodeFactory.createURI(v.getIRI().toString())).collect(
				Collectors.toSet()));
		existentialMeaninglessProperties.addAll(Arrays.stream(
				OWLRDFVocabulary.values()).map(v -> NodeFactory.createURI(v.getIRI().toString())).collect(
				Collectors.toSet()));
	}

	public PredicateExistenceFilter(Set<Node> existentialMeaninglessProperties) {
		this();
		this.existentialMeaninglessProperties.addAll(existentialMeaninglessProperties);
	}
	
	/**
	 * @param existentialMeaninglessProperties the existential meaningless properties
	 */
	public void setExistentialMeaninglessProperties(Set<Node> existentialMeaninglessProperties) {
		this.existentialMeaninglessProperties.addAll(existentialMeaninglessProperties);
	}
	
	@Override
	public RDFResourceTree apply(RDFResourceTree tree) {
		RDFResourceTree newTree;
		if(tree.isLiteralNode() && !tree.isLiteralValueNode()) {
			newTree = new RDFResourceTree(tree.getDatatype());
		} else {
			newTree = new RDFResourceTree(0, tree.getData());
		}

		for(Node edge : tree.getEdges()) {
			Node e = edge;
			// if the edge is meaningless
			if(e instanceof NodeInv) {
				e = ((NodeInv) e).getNode();
			}
			if(existentialMeaninglessProperties.contains(e)) {
				// process all children
				for (RDFResourceTree child : tree.getChildren(edge)) {
					// if child is URI or literal
					if(child.isResourceNode() || child.isLiteralValueNode()) {
						RDFResourceTree newChild = apply(child);
						newTree.addChild(newChild, edge);
					} else {// if child is variable
						RDFResourceTree newChild = apply(child);
						SortedSet<Node> childEdges = newChild.getEdges();
						if(!childEdges.isEmpty() && !(childEdges.size() == 1 && childEdges.contains(RDF.type.asNode()))) {
							newTree.addChild(newChild, edge);
						}
					}
				}
			} else {
				for (RDFResourceTree child : tree.getChildren(edge)) {
					RDFResourceTree newChild = apply(child);
					newTree.addChild(newChild, edge);
				}
			}
		}

		// we have to run the subsumption check one more time to prune the tree
//		QueryTreeUtils.prune(newTree, null, Entailment.RDFS);
		return newTree;
	}
}
