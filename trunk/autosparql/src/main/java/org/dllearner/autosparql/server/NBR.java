package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.QueryTreeChange.ChangeType;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.util.Filter;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;

public class NBR<N> {
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	private String query;
	private int limit;
	
	private int nodeId;
	
	private static final Logger logger = Logger.getLogger(NBR.class);
	
	public NBR(SparqlEndpoint endpoint, ExtractionDBCache cache){
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	public Example makeNBR(List<String> resources, QueryTree<N> lgg, List<QueryTree<N>> negTrees){
		//We only consider here the tree which corresponds to the SPARQL-Query
		QueryTree<N> relevantTree = ((QueryTreeImpl<N>)lgg).getSPARQLQueryTree();
		
		QueryTree<N> nbr = new QueryTreeImpl<N>(relevantTree);
		Map<QueryTree<N>, List<Integer>> matrix = new HashMap<QueryTree<N>, List<Integer>>();
		
		for(int i = 0; i < negTrees.size(); i++){
			checkTree(matrix, nbr, negTrees.get(i), i);
		}
		
//		System.err.println(printTreeWithValues(nbr, matrix));
		List<QueryTree<N>> orderedLeafs = getLeafsOrderedByRowSum(nbr, matrix);
//		System.err.println(orderedLeafs);
		
		for(QueryTree<N> leaf : orderedLeafs){
			if(leaf.getUserObject().equals("?")){
				logger.info("Removing edge [" + 
						leaf.getParent().getUserObject() + "--" + leaf.getParent().getEdge(leaf) + "-->" + leaf.getUserObject() + "]");
				QueryTree<N> parent = leaf.getParent();
				Object edge = parent.getEdge(leaf);
				parent.removeChild((QueryTreeImpl<N>) leaf);
//				if(coversNegativeTree(nbr, negTrees)){
//					parent.addChild((QueryTreeImpl<N>) leaf);
//					logger.info("Undoing change because a negative tree would be covered.");
//				} else {
					query = getLimitedEdgeCountQuery(nbr);//.toSPARQLQueryString();
					logger.info("Testing query\n" + query);
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
//				}
				
			} else {
				logger.info("Replacing label for node " + leaf.getUserObject());
				N oldLabel = leaf.getUserObject();
				leaf.setUserObject((N) "?");
//				if(coversNegativeTree(nbr, negTrees)){
//					leaf.setUserObject(oldLabel);
//					logger.info("Undoing change because a negative tree would be covered.");
//				} else {
					query = getLimitedEdgeCountQuery(nbr);//.toSPARQLQueryString();
					logger.info("Testing query\n" + query);
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
//				}
				
			}
		}
		return makeNBR(resources, relevantTree, negTrees);
		
	}
	
	public String getQuery(){
		return query;
	}
	
	private List<QueryTree<N>> getLeafsOrderedByRowSum(QueryTree<N> tree, Map<QueryTree<N>, List<Integer>> matrix){
		List<QueryTree<N>> leafs = new ArrayList<QueryTree<N>>();
		
		SortedMap<Integer, List<QueryTree<N>>> map = new TreeMap<Integer, List<QueryTree<N>>>();
		int rowSum;
		List<QueryTree<N>> treeList;
		for(Entry<QueryTree<N>, List<Integer>> entry : matrix.entrySet()){
			rowSum = sum(entry.getValue());
			treeList = map.get(rowSum);
			if(treeList == null){
				treeList = new ArrayList<QueryTree<N>>();
				map.put(rowSum, treeList);
			}
			treeList.add(entry.getKey());
		}
		
		for(List<QueryTree<N>> trees : map.values()){
			leafs.addAll(trees);
		}
		Collections.reverse(leafs);
		
		return leafs;
	}
	
	private int sum(List<Integer> list){
		int sum = 0;
		for(Integer i : list){
			sum += i;
		}
		return sum;
	}
	
	private boolean coversNegativeTree(QueryTree<N> lgg, List<QueryTree<N>> negTrees){
		for(QueryTree<N> negTree : negTrees){
			if(negTree.isSubsumedBy(lgg)){
				return true;
			}
		}
		return false;
	}
	
	private void checkTree(Map<QueryTree<N>, List<Integer>> matrix, QueryTree<N> posTree, QueryTree<N> negTree, int index){
		int entry = 1;
		Object edge;
		for(QueryTree<N> child1 : posTree.getChildren()){
			entry = 1;
    		edge = posTree.getEdge(child1);
    		for(QueryTree<N> child2 : negTree.getChildren(edge)){
    			if(!child1.getUserObject().equals("?") && child1.getUserObject().equals(child2.getUserObject())){
    				entry = 0;checkTree(matrix, child1, child2, index);
    			} else if(child1.getUserObject().equals("?")){
    				entry = 0;
    				checkTree(matrix, child1, child2, index);
    			}
    		}
    		setMatrixEntry(matrix, child1, index, entry);
    		if(entry == 1){
    			for(QueryTree<N> child : child1.getChildrenClosure()){
    				if(!child1.equals(child)){
    					setMatrixEntry(matrix, child, index, 0);
    				}
    			}
    		}
		}
		
	}
	
	private void setMatrixEntry(Map<QueryTree<N>, List<Integer>> matrix, QueryTree<N> row, int column, int entry){
		List<Integer> list = matrix.get(row);
		if(list == null){
			list = new ArrayList<Integer>();
			matrix.put(row, list);
		}
		try {
			list.set(column, entry);
		} catch (IndexOutOfBoundsException e) {
			list.add(entry);
		}
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
	
	private String printTreeWithValues(QueryTree<N> tree, Map<QueryTree<N>, List<Integer>> matrix){
		int depth = tree.getPathToRoot().size();
        StringBuilder sb = new StringBuilder();
        if(tree.isRoot()){
        	sb.append("TREE\n\n");
        }
//        ren = ren.replace("\n", "\n" + sb);
        sb.append(tree.getUserObject() + "(" +matrix.get(tree) +  ")");
        sb.append("\n");
        for (QueryTree<N> child : tree.getChildren()) {
            for (int i = 0; i < depth; i++) {
                sb.append("\t");
            }
            Object edge = tree.getEdge(child);
            if (edge != null) {
            	sb.append("  ");
            	sb.append(edge);
            	sb.append(" ---> ");
            }
            sb.append(printTreeWithValues(child, matrix));
        }
        return sb.toString();
	}
	
	
	public Example getQuestion(QueryTree<N> lgg, List<QueryTree<N>> negTrees, List<String> knownResources){
		lgg = getFilteredTree(lgg);
		logger.info(lgg.getStringRepresentation());
		limit = knownResources.size();
		List<GeneralisedQueryTree<N>> gens = getAllowedGeneralisations(new GeneralisedQueryTree<N>(lgg));
		
		GeneralisedQueryTree<N> genTree;
		QueryTree<N> queryTree;
		while(!gens.isEmpty()){
			genTree = gens.remove(0);
			logger.info("Changes: " + genTree.getChanges());
			queryTree = genTree.getQueryTree();
			if(!coversNegativeTree(queryTree, negTrees)){
				SortedSet<String> foundResources = getResources(queryTree);
				foundResources.removeAll(knownResources);
				if(!foundResources.isEmpty()){
					return new Example(foundResources.first(), null, null, null);
				} else {
					logger.info("Found no new resources");
					gens.addAll(0, getAllowedGeneralisations(genTree));
				}
			} else {
				logger.info("Covers negative tree");
			}
		}
		return null;
	}
	
//	private Queue<QueryTree<N>> gen(QueryTree<N> tree){
//		Queue<QueryTree<N>> gens = new LinkedList<QueryTree<N>>();
//		
//		QueryTree<N> genTree;
//		GeneralisedQueryTree<N> gTree;
//		N label;
//		N parentLabel;
//		Object edge;
//		QueryTree<N> parent;
//		for(QueryTree<N> child : tree.getChildren()){
//			label = child.getUserObject();
//			parentLabel = child.getParent().getUserObject();
//			if(!label.equals("?") && parentLabel.equals("?")){
//				child.setUserObject((N) "?");
//				genTree = new QueryTreeImpl<N>(tree);
//				gTree = new GeneralisedQueryTree<N>(genTree);
//				gTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REPLACE_LABEL));
//				gens.add(genTree);
//				child.setUserObject(label);
//			} else if(label.equals("?")){
//				edge = tree.getEdge(child);
//				parent = child.getParent();
//				if(child.isLeaf()){
//					int pos = parent.removeChild((QueryTreeImpl<N>) child);
//					genTree = new QueryTreeImpl<N>(tree);
//					gens.add(genTree);
//					parent.addChild((QueryTreeImpl<N>) child, edge, pos);
//				} else {
//					int pos = parent.removeChild((QueryTreeImpl<N>) child);
//					for(QueryTree<N> subTree : gen(child)){
//						parent.addChild((QueryTreeImpl<N>) subTree, edge, pos);
//						genTree = new QueryTreeImpl<N>(tree);
//						System.err.println(getSPARQLQuery(genTree));
//						gens.add(genTree);
//						parent.removeChild((QueryTreeImpl<N>) subTree);
//					}
//					parent.addChild((QueryTreeImpl<N>) child, edge, pos);
//				}
//			}
//		}
//		
//		return gens;
//	}
	
	private List<GeneralisedQueryTree<N>> getAllowedGeneralisations(GeneralisedQueryTree<N> tree){
		logger.info("Computing allowed generalisations...");
		List<GeneralisedQueryTree<N>> gens = new LinkedList<GeneralisedQueryTree<N>>();
		gens.addAll(computeAllowedGeneralisations(tree));
		
		return gens;
	}
	
	private List<GeneralisedQueryTree<N>> computeAllowedGeneralisations(GeneralisedQueryTree<N> tree){
		List<GeneralisedQueryTree<N>> gens = new LinkedList<GeneralisedQueryTree<N>>();
		
		QueryTree<N> queryTree = tree.getQueryTree();
		List<QueryTreeChange> changes = tree.getChanges();
		GeneralisedQueryTree<N> genTree;
		N label;
		N parentLabel;
		Object edge;
		QueryTree<N> parent;
		boolean isLiteralNode;
		for(QueryTree<N> child : queryTree.getChildren()){
			label = child.getUserObject();
			isLiteralNode = child.isLiteralNode();
			parentLabel = child.getParent().getUserObject();
			if(!label.equals("?") && parentLabel.equals("?")){
				child.setUserObject((N) "?");
				child.setVarNode(true);
				genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
				genTree.addChanges(changes);
				genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REPLACE_LABEL));
				gens.add(genTree);
				child.setUserObject(label);
				child.setLiteralNode(isLiteralNode);
				child.setResourceNode(!isLiteralNode);
			} else if(label.equals("?")){
				edge = queryTree.getEdge(child);
				parent = child.getParent();
				if(child.isLeaf()){
					int pos = parent.removeChild((QueryTreeImpl<N>) child);
					genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REMOVE_NODE));
					gens.add(genTree);
					parent.addChild((QueryTreeImpl<N>) child, edge, pos);
				} else {
					int pos = parent.removeChild((QueryTreeImpl<N>) child);
					for(GeneralisedQueryTree<N> subTree : computeAllowedGeneralisations(new GeneralisedQueryTree<N>(child))){
						parent.addChild((QueryTreeImpl<N>) subTree.getQueryTree(), edge, pos);
						genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
						genTree.addChanges(changes);
						genTree.addChanges(subTree.getChanges());
//						System.out.println(genTree.getChanges());
//						System.err.println(getSPARQLQuery(genTree.getQueryTree()));
						gens.add(genTree);
						parent.removeChild((QueryTreeImpl<N>) subTree.getQueryTree());
					}
					parent.addChild((QueryTreeImpl<N>) child, edge, pos);
				}
			}
		}
		
		return gens;
	}
	
	private SortedSet<String> getResources(QueryTree<N> tree){
		SortedSet<String> resources = new TreeSet<String>();
		
		query = getSPARQLQuery(tree);//tree.toSPARQLQueryString();
		query = getLimitedQuery(query);
		logger.info("Testing query\n" + query);
		String result = cache.executeSelectQuery(endpoint, query);
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(result);
		String uri;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("x0").getURI();
			resources.add(uri);
		}
		
		return resources;
	}
	
	
	
	private void applyGen(){
		
	}
	
	private String getLimitedQuery(String query){
		query = "SELECT DISTINCT " + query.substring(7);
		return query + " LIMIT " + (limit+1);
	}
	
	private QueryTree<N> getFilteredTree(QueryTree<N> tree){
		nodeId = 0;
		QueryTree<N> filteredTree = createFilteredTree(tree);
		return filteredTree;
	}
	
	private QueryTree<N> createFilteredTree(QueryTree<N> tree){
		QueryTree<N> filteredTree = new QueryTreeImpl<N>(tree.getUserObject());
		filteredTree.setId(nodeId);
		QueryTree<N> subTree;
		Object predicate;
    	for(QueryTree<N> child : tree.getChildren()){
    		if(child.isLiteralNode()){
    			continue;
    		}
    		predicate = tree.getEdge(child);
    		if(((String)predicate).startsWith("http://dbpedia.org/property")){
    			continue;
    		}
    		this.nodeId++;
    		subTree = createFilteredTree(child);
    		subTree.setLiteralNode(child.isLiteralNode());
    		subTree.setResourceNode(child.isResourceNode());
    		filteredTree.addChild((QueryTreeImpl<N>)subTree, tree.getEdge(child));
    	}
    	return filteredTree;
	}
	
    public String getSPARQLQuery(QueryTree<N> tree) {
    	if(tree.getChildren().isEmpty()){
    		return "SELECT ?x0 WHERE {?x0 ?y ?z.}";
    	}
    	int cnt = 0;
    	StringBuilder sb = new StringBuilder();
    	sb.append("SELECT ?x0 WHERE {\n");
    	buildSPARQLQueryString(tree, sb, false, cnt);
    	sb.append("}");
    	return sb.toString();
    }
    
    private void buildSPARQLQueryString(QueryTree<N> tree, StringBuilder sb, boolean filtered, int cnt){
    	Object subject = null;
    	if(tree.getUserObject().equals("?")){
    		subject = "?x" + cnt++;
    	} else {
    		subject = "<" + tree.getUserObject() + ">";
    	}
    	Object predicate;
    	Object object;
    	if(!tree.isLeaf()){
    		for(QueryTree<N> child : tree.getChildren()){
    			if(child.isLiteralNode()){
    				continue;
    			}
        		predicate = tree.getEdge(child);
        		if(((String)predicate).startsWith("http://dbpedia.org/property")){
        			continue;
        		}
        		if(filtered){
        			if(Filter.getAllFilterProperties().contains(predicate.toString())){
        				continue;
        			}
        		}
        		object = child.getUserObject();
        		boolean objectIsResource = !object.equals("?");
        		if(!objectIsResource){
        			object = "?x" + cnt;
        		} else if(((String)object).startsWith("http://")){
        			object = "<" + object + ">";
        		}
        		sb.append(subject).append(" <").append(predicate).append("> ").append(object).append(".\n");
        		if(!objectIsResource){
        			buildSPARQLQueryString(child, sb, filtered, cnt);
        		}
        	}
    	} 
    }

}
