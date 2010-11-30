package org.dllearner.autosparql.server;

import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;

public class Generalisation<N> {
	
	private static final Logger logger = Logger.getLogger(Generalisation.class);
	
	public QueryTree<N> generalise(QueryTree<N> queryTree){
		QueryTree<N> copy = new QueryTreeImpl<N>(queryTree);
		
		copy.setUserObject((N)"?");
		removeStatementsWithProperty(copy, OWL.sameAs.getURI());
		
//		retainTypeEdges(copy);
		pruneTree(copy, 0.5);
		
		return copy;
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
		logger.info(tree.getStringRepresentation());
		int childCountBefore = tree.getChildCount();
		
		for(QueryTree<N> child : tree.getChildren()){
			logger.info("Removing child: " + child);
			tree.removeChild((QueryTreeImpl<N>) child);
			if((double)tree.getChildCount()/childCountBefore <= limit){
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
