package org.dllearner.autosparql.server;

import java.util.List;

import org.dllearner.autosparql.client.model.Example;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;

public class NBR<N> {
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	
	public NBR(SparqlEndpoint endpoint, ExtractionDBCache cache){
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	public Example makeNBR(List<String> resources, QueryTree<N> lgg, List<QueryTree<N>> negTrees){
		QueryTree<N> relevantTree = ((QueryTreeImpl<N>)lgg).getSPARQLQueryTree();
		for(QueryTree<N> leaf : relevantTree.getLeafs()){
			if(leaf.getUserObject().equals("?")){
				QueryTree<N> parent = leaf.getParent();
				Object edge = parent.getEdge(leaf);
				parent.removeChild((QueryTreeImpl<N>) leaf);
				if(coversNegativeTree(lgg, negTrees)){
					parent.addChild((QueryTreeImpl<N>) leaf);
				} else {
					String query = getLimitedEdgeCountQuery(relevantTree);//.toSPARQLQueryString();
					System.out.println(query);
					String result = cache.executeSelectQuery(endpoint, query);
					ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
					String uri;
					QuerySolution qs;
					while(rs.hasNext()){
						qs = rs.next();
						uri = qs.getResource("x0").getURI();
						if(!resources.contains(uri)){
							return new Example(uri, null, null, null);
						}
						
					}
				}
				
			} else {
				N oldLabel = leaf.getUserObject();
				leaf.setUserObject((N) "?");
				if(coversNegativeTree(lgg, negTrees)){
					leaf.setUserObject(oldLabel);
				} else {
					String query = getLimitedEdgeCountQuery(relevantTree);//.toSPARQLQueryString();
					System.out.println(query);
					String result = cache.executeSelectQuery(endpoint, query);
					ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
					String uri;
					QuerySolution qs;
					while(rs.hasNext()){
						qs = rs.next();
						uri = qs.getResource("x0").getURI();
						if(!resources.contains(uri)){
							return new Example(uri, null, null, null);
						}
					}
				}
				
			}
		}
		return makeNBR(resources, relevantTree, negTrees);
		//We only consider here the tree which corresponds to the SPARQL-Query
		
	}
	
	private boolean coversNegativeTree(QueryTree<N> lgg, List<QueryTree<N>> negTrees){
		for(QueryTree<N> negTree : negTrees){
			if(negTree.isSubsumedBy(lgg)){
				return true;
			}
		}
		return false;
	}
	
	private String getLimitedEdgeCountQuery(QueryTree<N> tree){
		List<QueryTree<N>> children;
		int childCount = 1;
		for(Object edge : tree.getEdges()){
			children = tree.getChildren(edge);
			childCount = children.size();
			while(childCount > 1){
				tree.removeChild((QueryTreeImpl<N>) children.get(childCount-1));
				childCount--;
			}
		}
		return tree.toSPARQLQueryString();
	}
	

}
