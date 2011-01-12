package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.QueryTreeChange.ChangeType;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;
import org.dllearner.sparqlquerygenerator.util.Filter;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator.Strategy;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;

public class NBR<N> {
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	private QueryTreeFactory<String> treeFactory;
	private ModelGenerator modelGen;
	private String query;
	private int limit;
	
	private int nodeId;
	private QueryTree<N> lgg;
	private QueryTree<N> postLGG;
	
	private LastQueryTreeChangeComparator comparator = new LastQueryTreeChangeComparator();
	
	private static final Logger logger = Logger.getLogger(NBR.class);
	
	public NBR(SparqlEndpoint endpoint, ExtractionDBCache cache){
		this.endpoint = endpoint;
		this.cache = cache;
		
		modelGen = new ModelGenerator(endpoint, new HashSet<String>(((SPARQLEndpointEx)endpoint).getPredicateFilters()), cache);
		treeFactory = new QueryTreeFactoryImpl();
	}
	
	public Example makeNBR(List<String> resources, QueryTree<N> lgg, List<QueryTree<N>> negTrees){
		//We only consider here the tree which corresponds to the SPARQL-Query
		QueryTree<N> relevantTree = ((QueryTreeImpl<N>)lgg).getSPARQLQueryTree();
		
		QueryTree<N> nbr = new QueryTreeImpl<N>(relevantTree);
		Map<QueryTree<N>, List<Integer>> matrix = createMatrix(nbr, negTrees);
		
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
	
	private Map<QueryTree<N>, List<Integer>> createMatrix(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		Map<QueryTree<N>, List<Integer>> matrix = new HashMap<QueryTree<N>, List<Integer>>();
		for(int i = 0; i < negTrees.size(); i++){
			checkTree(matrix, tree, negTrees.get(i), i);
		}
		return matrix;
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
	
	public Example getQuestionOptimised(QueryTree<N> lgg, List<QueryTree<N>> negTrees, List<String> knownResources){
		this.lgg = lgg;
		logger.info("Computing next question...");
		postLGG = getFilteredTree(lgg);
		PostLGG<N> postGen = new PostLGG<N>();
		postGen.simplifyTree(postLGG, negTrees);
//		logger.debug("Starting generalisation with tree:\n" + postLGG.getStringRepresentation());
		limit = knownResources.size();
		List<GeneralisedQueryTree<N>> queue = getAllowedGeneralisations(new GeneralisedQueryTree<N>(postLGG));
		logger.debug(getQueueLogInfo(queue));
		
		GeneralisedQueryTree<N> tree1;
		QueryTree<N> tree2;
		GeneralisedQueryTree<N> tmp;
		QueryTree<N> queryTree;
		List<GeneralisedQueryTree<N>> gens;
		List<QueryTree<N>> neededGeneralisations;
		while(!queue.isEmpty()){
			neededGeneralisations = new ArrayList<QueryTree<N>>();
			logger.debug("Selecting first tree from queue");
			tree1 = queue.remove(0);
			tmp = tree1;
			if(logger.isDebugEnabled()){
				logger.debug("Changes: " + tmp.getChanges());
			}
			queryTree = tmp.getQueryTree();
			boolean coversNegTree = coversNegativeTree(tmp.getQueryTree(), negTrees);
			logger.debug("covers negative tree: " + coversNegTree);
			while(!coversNegTree){
				gens = getAllowedGeneralisationsSorted(tmp);
//				for(GeneralisedQueryTree<N> t : gens){
//					logger.info(t.getChanges());
//				}
				if(gens.isEmpty()){
					if(logger.isDebugEnabled()){
						logger.debug("Couldn't create a generalisation which covers a negative tree.");
					}
					break;
				}
				tmp = gens.remove(0);
				neededGeneralisations.add(tmp.getQueryTree());
				if(logger.isDebugEnabled()){
					logger.debug("Changes: " + tmp.getChanges());
				}
				queue.addAll(0, gens);
				logger.debug(getQueueLogInfo(queue));
				coversNegTree = coversNegativeTree(tmp.getQueryTree(), negTrees);
				if(coversNegTree) {
					logger.debug("covers negative tree");
				}
			}
		
//			List<QueryTreeChange> sequence = genSequence(tree1, tmp);
//			if(coversNegTree){
//				sequence.remove(sequence.size()-1);
//			} 
//			tree2 = applyGen(tree1.getQueryTree(), sequence);
			int index = neededGeneralisations.size()-1;
			if(coversNegTree){
				tree2 = neededGeneralisations.get(index--);
			} else {
				tree2 = tmp.getQueryTree();
			}
			
//			QueryTree<N> newTree = getNewResource(tree2, knownResources);
			String newResource = getNewResource(tree2, knownResources);
			if(!(newResource == null)){
				logger.debug("binary search for most specific query returning a resource - start");
				newResource = findMostSpecificResourceTree(neededGeneralisations, knownResources, 0, neededGeneralisations.size()-1);
				logger.debug("binary search for most specific query returning a resource - completed");
				// TODO: probably the corresponding tree, which resulted in the resource, should also be returned
				return new Example(newResource, null, null, null);
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("Query result contains no new resources. Trying next tree from queue...");
				}
			}
		}
		return null;
	}
	
	private QueryTree<N> getQueryTree(String resource){
		Model model = modelGen.createModel(resource, Strategy.CHUNKS, 2);
		QueryTree<String> tree = treeFactory.getQueryTree(resource, model);
		return getFilteredTree((QueryTree<N>) tree);
	}
	
	// uses binary search to find most specific tree containing new resource
	// invoke with low = 0 and high = listsize-1
	private String findMostSpecificResourceTree(List<QueryTree<N>> trees, List<String> knownResources, int low, int high) {
//		if(low==high) {
//			return low;
//		}
		int testIndex = low + (high-low)/2;
		// perform SPARQL query
		
//		QueryTree<N> t = getNewResource(trees.get(testIndex), knownResources);
		String t = getNewResource(trees.get(testIndex), knownResources);
		if(testIndex == high){
			return t;
		}
		if(t == null) {
			return findMostSpecificResourceTree(trees,knownResources,testIndex+1,high);
		} else {
			return findMostSpecificResourceTree(trees,knownResources,low,testIndex);
		}
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
	
	public List<GeneralisedQueryTree<N>> getAllowedGeneralisations(GeneralisedQueryTree<N> tree){
		logger.debug("Computing allowed generalisations...");
		List<GeneralisedQueryTree<N>> gens = new LinkedList<GeneralisedQueryTree<N>>();
//		QueryTreeChange initChange = new QueryTreeChange(0, ChangeType.REPLACE_LABEL);
		gens.addAll(computeAllowedGeneralisations(tree, tree.getLastChange()));
		
		return gens;
	}
	
	private List<QueryTree<N>> getPossibleNodes2Change(QueryTree<N> tree){
		List<QueryTree<N>> nodes = new ArrayList<QueryTree<N>>();
		for(QueryTree<N> child : tree.getChildren()){
			if(child.isLeaf()){
				nodes.add(child);
			} else {
				if(child.getParent().getUserObject().equals("?")){
					if(child.getUserObject().equals("?")){
						nodes.addAll(getPossibleNodes2Change(child));
					} else {
						nodes.add(child);
					}
				}
			}
		}
		return nodes;
	}
	
	private List<GeneralisedQueryTree<N>> getAllowedGeneralisationsSortedByMatrix(GeneralisedQueryTree<N> tree){
		List<QueryTreeChange> changes = new ArrayList<QueryTreeChange>();
		System.err.println(tree.getQueryTree().getStringRepresentation());
		QueryTreeChange lastChange = tree.getLastChange();
		for(QueryTree<N> node : getPossibleNodes2Change(tree.getQueryTree())){
			if(lastChange.getType() == ChangeType.REMOVE_NODE){
				if(node.getUserObject().equals("?") && node.getId() < lastChange.getNodeId()){
					changes.add(new QueryTreeChange(node.getId(), ChangeType.REMOVE_NODE));
				}
			} else {
				if(node.getUserObject().equals("?")){
					changes.add(new QueryTreeChange(node.getId(), ChangeType.REMOVE_NODE));
				} else {
					changes.add(new QueryTreeChange(node.getId(), ChangeType.REPLACE_LABEL));
				}
			}
		}
		System.out.println();
		List<GeneralisedQueryTree<N>> gens = getAllowedGeneralisations(tree);
		Collections.sort(gens, comparator);	
		return gens;
	}
	
	private List<GeneralisedQueryTree<N>> getAllowedGeneralisationsSorted(GeneralisedQueryTree<N> tree){
		List<GeneralisedQueryTree<N>> gens = getAllowedGeneralisations(tree);
		Collections.sort(gens, comparator);	
		return gens;
	}
	
	private List<GeneralisedQueryTree<N>> computeAllowedGeneralisations(GeneralisedQueryTree<N> tree, QueryTreeChange lastChange){
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
			parent = child.getParent();
			parentLabel = parent.getUserObject();
			edge = parent.getEdge(child);
			if(!label.equals("?") && parentLabel.equals("?")){
				if(lastChange.getNodeId() >= child.getId() || lastChange.getType()==ChangeType.REMOVE_NODE){
					continue;
				}
				if(parent.getChildren(edge).size() >= 2){
					int pos = parent.removeChild((QueryTreeImpl<N>) child);
					genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REPLACE_LABEL));
					gens.add(genTree);
					parent.addChild((QueryTreeImpl<N>) child, edge, pos);
				} else {
					child.setUserObject((N) "?");
					child.setVarNode(true);
					genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REPLACE_LABEL));
					gens.add(genTree);
					child.setUserObject(label);
					child.setLiteralNode(isLiteralNode);
					child.setResourceNode(!isLiteralNode);
				}
//				child.setUserObject((N) "?");
//				child.setVarNode(true);
//				genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
//				genTree.addChanges(changes);
//				genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REPLACE_LABEL));
//				gens.add(genTree);
//				child.setUserObject(label);
//				child.setLiteralNode(isLiteralNode);
//				child.setResourceNode(!isLiteralNode);
			} else if(label.equals("?")){
				edge = queryTree.getEdge(child);
				parent = child.getParent();
				if(child.isLeaf()){
					if(lastChange.getNodeId() < child.getId() ){
						continue;
					}
					int pos = parent.removeChild((QueryTreeImpl<N>) child);
					genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REMOVE_NODE));
					gens.add(genTree);
					parent.addChild((QueryTreeImpl<N>) child, edge, pos);
				} else {
					int pos = parent.removeChild((QueryTreeImpl<N>) child);
					for(GeneralisedQueryTree<N> subTree : computeAllowedGeneralisations(new GeneralisedQueryTree<N>(child), tree.getLastChange())){
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
		
		query = tree.toSPARQLQueryString();
		query = getDistinctQuery(query);
		if(logger.isDebugEnabled()){
			logger.debug("Testing query\n" + getLimitedQuery(query));
		}
		String result = cache.executeSelectQuery(endpoint, getLimitedQuery(query));
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
	
	private SortedSet<String> getResources(QueryTree<N> tree, int limit, int offset){
		SortedSet<String> resources = new TreeSet<String>();
		
		query = tree.toSPARQLQueryString();
		query = getDistinctQuery(query);
		if(logger.isDebugEnabled()){
			logger.debug("Testing query\n" + getLimitedQuery(query, limit, offset));
		}
		String result = cache.executeSelectQuery(endpoint, getLimitedQuery(query, limit, offset));
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
	
	private String getNewResource(QueryTree<N> tree, List<String> knownResources){
		int i = 0;
		int chunkSize = 10;
		SortedSet<String> foundResources = getResources(tree, 10, chunkSize * i);
		foundResources.removeAll(knownResources);
		QueryTree<N> newTree;
		while(!foundResources.isEmpty()){
			for(String resource : foundResources){
				newTree = getQueryTree(resource);
				if(!newTree.isSubsumedBy(lgg)){
//					return newTree;
					return resource;
				}
			}
			i++;
			foundResources = getResources(tree, 10, chunkSize * i);
		}
		logger.debug("Found no resource which would modify the LGG");
		return null;
	}
	
	private QueryTree<N> applyGen(QueryTree<N> tree, List<QueryTreeChange> changes){
		QueryTree<N> genTree = new QueryTreeImpl<N>(tree);
		
		QueryTree<N> node;
		QueryTree<N> parentNode;
		for(QueryTreeChange change : changes){
			node = tree.getNodeById(change.getNodeId());
			if(change.getType() == ChangeType.REMOVE_NODE){
				parentNode = node.getParent();
				parentNode.removeChild((QueryTreeImpl<N>) node);
			} else {
				node.setUserObject((N) "?");
			}
		}
		
		return genTree;
	}
	
	private List<QueryTreeChange> genSequence(GeneralisedQueryTree<N> treeBefore, GeneralisedQueryTree<N> treeAfter){
		List<QueryTreeChange> changes = new ArrayList<QueryTreeChange>(treeAfter.getChanges());
		changes.removeAll(treeBefore.getChanges());
		return changes;
	}
	
	private String getLimitedQuery(String query){
		return query + " LIMIT " + (limit+1);
	}
	
	private String getLimitedQuery(String query, int limit, int offset){
		return query + " LIMIT " + limit + " OFFSET " + offset;
	}
	
	private String getLimitedQuery(QueryTree<N> tree, int limit){
		String query = tree.toSPARQLQueryString();
		return query + " LIMIT " + (limit);
	}
	
	private String getLimitedQuery(QueryTree<N> tree, int limit, int offset){
		String query = tree.toSPARQLQueryString();
		return query + " LIMIT " + limit + " OFFSET " + offset;
	}
	
	private String getDistinctQuery(String query){
		return "SELECT DISTINCT " + query.substring(7);
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
	
	private void shrinkTree(QueryTree<N> tree, int limit){
		int cnt = 1;
		for(QueryTree<N> leaf : tree.getLeafs()){
			
		}
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
    
    public QueryTree<N> getPostLGG(){
    	return postLGG;
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
    
    private String getQueueLogInfo(List<GeneralisedQueryTree<N>> queue) {
    	int displayElements = 3;
    	int max = Math.min(displayElements, queue.size());
    	String str = "queue (size " + queue.size() + "): ";
    	for(int i=0; i<max; i++) {
    		str += queue.get(i).getChanges().toString() + ", ";
    	}
    	if(queue.size()>displayElements) {
    		str += " ... ";
    	}
    	return str;
    }
    
    class GeneralisedQueryTreeComparator implements Comparator<GeneralisedQueryTree<N>>{

    	@Override
		public int compare(GeneralisedQueryTree<N> tree1, GeneralisedQueryTree<N> tree2) {
			int aCount1 = 0;
			int aCount2 = 0;
			int bCount1 = 0;
			int bCount2 = 0;
			List<QueryTreeChange> changes1 = tree1.getChanges();
			List<QueryTreeChange> changes2 = tree2.getChanges();
			for(QueryTreeChange change : tree1.getChanges()){
				if(change.getType() == ChangeType.REPLACE_LABEL){
					aCount1++;
				} else {
					bCount1++;
				}
			}
			for(QueryTreeChange change : tree2.getChanges()){
				if(change.getType() == ChangeType.REPLACE_LABEL){
					aCount2++;
				} else {
					bCount2++;
				}
			}
			int nodeId1;
			int nodeId2;
			
			if(aCount1 < aCount2){
				return 1;
			} else if(aCount1 > aCount2){
				return -1;
			} else {
				if(bCount1 < bCount2){
					return -1;
				} else if(bCount1 > bCount2){
					return 1;
				} else {
					for(int i = 0; i < changes1.size(); i++){
						nodeId1 = changes1.get(i).getNodeId();
						nodeId2 = changes2.get(i).getNodeId();
						
						if(nodeId1 == nodeId2){
							continue;
						} else {
							return nodeId1-nodeId2;
						}
					}
					return 0;
				}
				
			}

			
		}
    	
    }
    
    class LastQueryTreeChangeComparator implements Comparator<GeneralisedQueryTree<N>>{

    	@Override
		public int compare(GeneralisedQueryTree<N> tree1, GeneralisedQueryTree<N> tree2) {
			QueryTreeChange change1 = tree1.getLastChange();
			QueryTreeChange change2 = tree2.getLastChange();
			if(change1.getType()==ChangeType.REPLACE_LABEL){
				if(change2.getType()==ChangeType.REPLACE_LABEL){
					return change1.getNodeId() - change2.getNodeId();
				} else {
					return 1;
				}
			} else {
				if(change2.getType()==ChangeType.REPLACE_LABEL){
					return 1;
				} else {
					return change2.getNodeId() - change1.getNodeId();
				}
			}
			
		}
    	
    }

}
