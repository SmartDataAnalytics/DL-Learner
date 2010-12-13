package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import com.hp.hpl.jena.vocabulary.RDF;

public class Generalisation<N> {
	
	private static final Logger logger = Logger.getLogger(Generalisation.class);
	
	private int maxEdgeCount = 15;
	public double pruningFactor = 0.5;
	
	boolean invert = false;
	
	public QueryTree<N> generalise(QueryTree<N> queryTree){
		QueryTree<N> copy = new QueryTreeImpl<N>(queryTree);
		
		copy.setUserObject((N)"?");
//		removeStatementsWithProperty(copy, OWL.sameAs.getURI());
		
//		retainTypeEdges(copy);
		return pruneTree(copy, pruningFactor);
		
//		return copy;
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
	
	private QueryTree<N> pruneTree(QueryTree<N> tree, double limit){
		if(logger.isInfoEnabled()){
			logger.info("Pruning tree:");
//			logger.info(tree.getStringRepresentation());
			logger.info("Number of triple pattern: " + ((QueryTreeImpl<N>)tree).getTriplePatternCount());
//			logger.info(((QueryTreeImpl<N>)tree).getSPARQLQueryTree().getStringRepresentation());
		}
		
		tree = ((QueryTreeImpl<N>)tree).getSPARQLQueryTree();
		
		int edgeCount = tree.getChildrenClosure().size()-1;
		if( edgeCount > maxEdgeCount){
			removeLeafs(tree, edgeCount-maxEdgeCount);
		} else {
			if(edgeCount == 1){
				removeLeafs(tree, 1);
			} else {
				removeLeafs(tree, edgeCount/2);
			}
			
		}
		return tree;
		
		
//		List<QueryTree<N>> children = new ArrayList<QueryTree<N>>(tree.getChildren());
//		QueryTree<N> child;
//		for(Iterator<QueryTree<N>> iter = children.iterator(); iter.hasNext(); ){
//			child = iter.next();
//			if(logger.isInfoEnabled()){
//				logger.info("Removing child: " + child);
//			}
//			tree.removeChild((QueryTreeImpl<N>) child);
//			int newEdgeCount = tree.getUserObjectClosure().size()-1;
//			if( newEdgeCount <= maxEdgeCount
//					&& (double)newEdgeCount/edgeCount <= limit){
//				break;
//			}
//		}
//		return tree;
	}
	
	private void removeLeafs(QueryTree<N> tree, int cnt){
		int level = tree.getMaxDepth();
		
		while(cnt > 0){
			for(QueryTree<N> leaf : getLeafsAtLevel(tree, level)){
				if(logger.isInfoEnabled()){
					logger.info("Removing edge [" + 
							leaf.getParent().getUserObject() + "--" + leaf.getParent().getEdge(leaf) + "-->" + leaf.getUserObject() + "]");
				}
				leaf.getParent().removeChild((QueryTreeImpl<N>) leaf);
				cnt--;
				if(cnt == 0){
					break;
				}
			}
			level--;
		}
		if(logger.isInfoEnabled()){
			logger.info(tree.getStringRepresentation());
		}
	}
	
	private List<QueryTree<N>> getLeafsAtLevel(QueryTree<N> tree, int level){
		List<QueryTree<N>> leafs = new ArrayList<QueryTree<N>>();
		for(QueryTree<N> leaf : tree.getLeafs()){
			if(leaf.getPathToRoot().size() == level){
				leafs.add(leaf);
			}
		}
			
		return leafs;
	}
	
	private void retainTypeEdges(QueryTree<N> tree){
		for(QueryTree<N> child : tree.getChildren()){
			if(!tree.getEdge(child).equals(RDF.type.toString())){
				tree.removeChild((QueryTreeImpl<N>) child);
			}
		}
	}
	

}
