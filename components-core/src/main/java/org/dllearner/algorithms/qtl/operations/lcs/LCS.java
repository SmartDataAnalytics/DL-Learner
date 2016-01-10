package org.dllearner.algorithms.qtl.operations.lcs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.dllearner.exceptions.LCSException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

/**
 * Computes the Least Common Subsumer for given rooted RDF graphs. 
 * @author Lorenz Buehmann
 *
 */
public class LCS {
	
	private Map<Set<Node>, RootedRDFGraph> cache = new HashMap<>();
	
	public RootedRDFGraph computeLCS(RootedRDFGraph g1, RootedRDFGraph g2) throws LCSException{
		
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
				x = NodeFactory.createAnon();
				
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
	 * @param source the source node
	 * @param target the target node
	 * @param triples the set of triples in the graph
	 * @return
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
	class RootedRDFGraph {
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
