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
package org.dllearner.algorithms.qtl.operations.lcs;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes the Least Common Subsumer for given rooted RDF graphs. 
 * @author Lorenz Buehmann
 *
 */
public class LCS {
	
	private Map<Set<Node>, RootedRDFGraph> cache = new HashMap<>();
	
	public RootedRDFGraph computeLCS(RootedRDFGraph g1, RootedRDFGraph g2) {
		
		Set<Node> pair = Sets.newHashSet(g1.getRoot(), g2.getRoot());
		
		if(cache.containsKey(pair)) {
			return cache.get(pair);
		} else {
			Node x;
			
			if(g1.getRoot().equals(g2.getRoot())) {
//				x = g1.getRoot();
				return g1;
			} else {
				// create fresh blank node
				x = NodeFactory.createBlankNode();
				
				// a new set of triples
				Set<Triple> triples = new HashSet<>();
				
				// add to result to avoid recomputation
				RootedRDFGraph g = new RootedRDFGraph(x, triples);
				cache.put(pair, g);
				
				for (Triple t1 : g1.getTriples()) {
					for (Triple t2 : g2.getTriples()) {
//						Node y = computeLCS(t1.getPredicate(), t2.getPredicate());
//						Node z = computeLCS(t1.getObject(), t2.getObject());
//						
//						// add triple <x,y,z>
//						Triple t = Triple.create(x, y, z);
//						triples.add(t);
					}
				}
				return g;
			}
		}
		
		
	}
	
	private Set<Triple> connectedTriples(Node node, Set<Triple> triples) {
		Set<Triple> connectedTriples = new HashSet<>();
		
		for (Triple triple : triples) {
			if(isRDFConnected(node, triple, triples)) {
				connectedTriples.add(triple);
			}
		}
		
		return connectedTriples;
	}

	/**
	 * Check if there is an RDF-path from source to target.
	 *
	 * @param source  the source node
	 * @param target  the target node
	 * @param triples the set of triples in the graph
	 * @return whether both nodes are RDF-connected by the given set of triples, i.e. if there is an RDF-path from
	 * source to target.
	 */
	public static boolean isRDFConnected(Node source, Node target, Set<Triple> triples) {
		// trivial case: node is always RDF-connected to itself
		if(source.equals(target)) {
			return true;
		}
		
		// other case: 
		for (Triple t : triples) {
			if(t.subjectMatches(source) && 
					(isRDFConnected(t.getPredicate(), target, triples) ||
					 isRDFConnected(t.getObject(), target, triples))
					) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Check if there is a path from s to the triple, i.e. 
	 */
	private boolean isRDFConnected(Node node, Triple triple, Set<Triple> triples) {
		return isRDFConnected(node, triple.getPredicate(), triples) ||  
				isRDFConnected(node, triple.getObject(), triples);
	}
	
	
	/**
	 * A set of triples with a given node (IRI or blank node) declared to be the root in the 
	 * corresponding RDF graph.
	 * @author Lorenz Buehmann
	 *
	 */
	static class RootedRDFGraph {
		private Set<Triple> triples;
		private Node root;
		
		public RootedRDFGraph(Node root, Set<Triple> triples) {
			this.root = root;
			this.triples = triples;
		}
		
		public Node getRoot() {
			return root;
		}
		
		public Set<Triple> getTriples() {
			return triples;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return root + "" + triples;
		}
	}

}
