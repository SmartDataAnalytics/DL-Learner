package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class Generalisation<N> {
	
	private static final Logger logger = Logger.getLogger(Generalisation.class);
	
	private int maxEdgeCount = 10;
	public double pruningFactor = 0.5;
	
	boolean invert = false;
	
	private List<QueryTree<N>> rest;
	
	public QueryTree<N> generalise(QueryTree<N> queryTree){
		QueryTree<N> copy = new QueryTreeImpl<N>(queryTree);
		
		copy.setUserObject((N)"?");
//		removeStatementsWithProperty(copy, OWL.sameAs.getURI());
		
//		retainTypeEdges(copy);
		pruneTree(copy, pruningFactor);
		
		return copy;
	}
	
	public void setMaxEdgeCount(int maxEdgeCount){
		this.maxEdgeCount = maxEdgeCount;
	}
	
	public void setPruningFactor(double pruningFactor){
		this.pruningFactor = pruningFactor;
	}
	
	private void removeStatementsWithProperty(QueryTree<N> tree, String property){
		logger.info("Removing edges with property: " + property);
		
		for(QueryTree<N> child : tree.getChildren()){
			if(tree.getEdge(child).equals(property)){
				logger.info("Remove edge to child: " + child);
				tree.removeChild((QueryTreeImpl<N>) child);
			}
		}
	}
	
	private void replaceAllLeafs(QueryTree<N> queryTree){
		for(QueryTree<N> leaf : queryTree.getLeafs()){
			leaf.setUserObject((N)"?");
		}
		
	}
	
	private void pruneTree(QueryTree<N> tree, double limit){
		logger.info("Pruning tree:");
//		logger.info(tree.getStringRepresentation());
		logger.info("Number of triple pattern: " + ((QueryTreeImpl<N>)tree).getTriplePatternCount());
//		logger.info(((QueryTreeImpl<N>)tree).getSPARQLQueryTree().getStringRepresentation());
		int childCountBefore = tree.getChildCount();
		
		List<QueryTree<N>> children = new ArrayList<QueryTree<N>>(tree.getChildren());
//		Collections.shuffle(children);
		QueryTree<N> child;
		for(Iterator<QueryTree<N>> iter = children.iterator(); iter.hasNext(); ){
			child = iter.next();
			logger.info("Removing child: " + child);
			tree.removeChild((QueryTreeImpl<N>) child);
			if( (tree.getChildCount()) <= maxEdgeCount
					&& (double)tree.getChildCount()/childCountBefore <= limit){
				break;
			}
		}
		
		
	}
	
	private void retainTypeEdges(QueryTree<N> tree){
		for(QueryTree<N> child : tree.getChildren()){
			if(!tree.getEdge(child).equals(RDF.type.toString())){
				tree.removeChild((QueryTreeImpl<N>) child);
			}
		}
	}
	

}
