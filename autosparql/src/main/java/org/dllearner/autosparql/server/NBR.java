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
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.server.QueryTreeChange.ChangeType;
import org.dllearner.autosparql.server.exception.TimeOutException;
import org.dllearner.autosparql.server.util.SPARQLEndpointEx;
import org.dllearner.autosparql.server.util.TreeHelper;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.sparqlquerygenerator.cache.ModelCache;
import org.dllearner.sparqlquerygenerator.cache.QueryTreeCache;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

public class NBR<N> {
	
	private boolean generalizeSortedByNegatives = false;
	
	private volatile boolean stop = false;
	private boolean isRunning;
	private int maxExecutionTimeInSeconds = 1000;
	private long startTime;
	
	private ExtractionDBCache selectCache;
	private ExtractionDBCache constructCache;
	private SPARQLEndpointEx endpoint;
	private ModelGenerator modelGen;
	private ModelCache modelCache;
	private QueryTreeCache treeCache;
	
	private String query;
	private int limit;
	
	private int nodeId;
	private QueryTree<N> lgg;
	private QueryTree<N> postLGG;
	private List<QueryTree<N>> negTrees;
	private List<Integer> determiningNodeIds;
	
	
	
	private LastQueryTreeChangeComparator comparator = new LastQueryTreeChangeComparator();
	
	private static final Logger logger = Logger.getLogger(NBR.class);
	
	public NBR(SPARQLEndpointEx endpoint, ExtractionDBCache selectCache, ExtractionDBCache constructCache){
		this.endpoint = endpoint;
		this.selectCache = selectCache;
		this.constructCache = constructCache;
		
		modelGen = new ModelGenerator(endpoint, new HashSet<String>(((SPARQLEndpointEx)endpoint).getPredicateFilters()), constructCache);
		modelCache = new ModelCache(modelGen);
		treeCache = new QueryTreeCache();
	}
	
	public void setStatementFilter(Filter<Statement> filter){
		treeCache.setStatementFilter(filter);
	}
	
	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds){
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
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
					String result = selectCache.executeSelectQuery(endpoint, query);
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
					String result = selectCache.executeSelectQuery(endpoint, query);
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
	
	private List<Integer> getDeterminingNodeIds(QueryTree<N> lgg, List<QueryTree<N>> trees){
		List<Integer> nodeIds = new ArrayList<Integer>();
		
		boolean parentIsResource = false;
		boolean childIsResource = false;
		for(QueryTree<N> child : lgg.getChildren()){
			parentIsResource = !child.getParent().getUserObject().equals("?");
			childIsResource = !child.getUserObject().equals("?");
			if(parentIsResource && childIsResource && hasAlwaysSameParent(child, trees)){
				nodeIds.add(child.getId());
			}
			if(!child.isLeaf()){
				nodeIds.addAll(getDeterminingNodeIds(child, trees));
			}
		}
		
		return nodeIds;
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
		int entry;
		Object edge;
		for(QueryTree<N> child1 : posTree.getChildren()){
			entry = 0;
    		edge = posTree.getEdge(child1);
    		for(QueryTree<N> child2 : negTree.getChildren(edge)){
    			if(child1.getUserObject().equals("?") ||
    			(!child1.getUserObject().equals("?") && child1.getUserObject().equals(child2.getUserObject()))){
    				entry = 1;
    				checkTree(matrix, child1, child2, index);
    			}
    		}
    		setMatrixEntry(matrix, child1, index, entry);
    		if(entry == 0){
    			for(QueryTree<N> child : child1.getChildrenClosure()){
    				if(!child1.equals(child)){
    					setMatrixEntry(matrix, child, index, 1);
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
	
	
	public String getQuestion(QueryTree<N> lgg, List<QueryTree<N>> negTrees, List<String> knownResources) throws TimeOutException{
		return computeQuestionOptimized(lgg, negTrees, knownResources);
	}
	
	private Example computeQuestion(QueryTree<N> lgg, List<QueryTree<N>> negTrees, List<String> knownResources){
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
	
	private String computeQuestionOptimized(QueryTree<N> lgg, List<QueryTree<N>> negTrees, List<String> knownResources) throws TimeOutException{
		startTime = System.currentTimeMillis();
		this.lgg = lgg;
		this.negTrees = negTrees;
		determiningNodeIds = getDeterminingNodeIds(lgg, negTrees);
//		System.err.println(negTrees.get(0).getStringRepresentation());
		logger.info("Computing next question...");
		postLGG = getFilteredTree(lgg);
		PostLGG<N> postGen = new PostLGG<N>((SPARQLEndpointEx) endpoint);
		postGen.simplifyTree(postLGG, negTrees);
//		logger.debug("Starting generalisation with tree:\n" + postLGG.getStringRepresentation());
		limit = knownResources.size();
		
		List<GeneralisedQueryTree<N>> queue = null;
		if(generalizeSortedByNegatives){
			queue = getAllowedGeneralisationsSortedByMatrix(new GeneralisedQueryTree<N>(postLGG), negTrees);
		} else {
			queue = getAllowedGeneralisationsSorted(new GeneralisedQueryTree<N>(postLGG));
		}
		logger.debug(getQueueLogInfo(queue));
		
		GeneralisedQueryTree<N> tree1;
		QueryTree<N> tree2;
		GeneralisedQueryTree<N> tmp;
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
			boolean coversNegTree = coversNegativeTree(tmp.getQueryTree(), negTrees);
			neededGeneralisations.add(tmp.getQueryTree());
			logger.debug("covers negative tree: " + coversNegTree);
			while(!coversNegTree){
				if(generalizeSortedByNegatives){
					gens = getAllowedGeneralisationsSortedByMatrix(tmp, negTrees);
				} else {
					gens = getAllowedGeneralisationsSorted(tmp);
				}
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
		
			int index = neededGeneralisations.size()-1;
			if(coversNegTree){
				tree2 = neededGeneralisations.get(index--);
			} else {
				tree2 = tmp.getQueryTree();
			}
			
//			QueryTree<N> newTree = getNewResource(tree2, knownResources);
			String newResource = getNewResource(tree2, knownResources);
			if(isTerminationCriteriaReached()){
				throw new TimeOutException(maxExecutionTimeInSeconds);
			}
			logger.debug("New resource before binary search: " + newResource);
			if(!(newResource == null)){
				logger.debug("binary search for most specific query returning a resource - start");
				newResource = findMostSpecificResourceTree(neededGeneralisations, knownResources, 0, neededGeneralisations.size()-1);
				logger.debug("binary search for most specific query returning a resource - completed");
				// TODO: probably the corresponding tree, which resulted in the resource, should also be returned
				return newResource;
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("Query result contains no new resources. Trying next tree from queue...");
				}
			}
		}
		return null;
	}
	
	
	private QueryTree<N> getQueryTree(String resource){
		Model model = modelCache.getModel(resource);
		QueryTree<String> tree = treeCache.getQueryTree(resource, model);
		return getFilteredTree((QueryTree<N>) tree);
	}
	
	// uses binary search to find most specific tree containing new resource
	// invoke with low = 0 and high = listsize-1
	private String findMostSpecificResourceTree(List<QueryTree<N>> trees, List<String> knownResources, int low, int high) throws TimeOutException {
//		if(low==high) {
//			return low;
//		}
		int testIndex = low + (high-low)/2;
		// perform SPARQL query
		
//		QueryTree<N> t = getNewResource(trees.get(testIndex), knownResources);
		String t = null;
		try {
			t = getNewResource(trees.get(testIndex), knownResources);
		} catch (HTTPException e) {
			throw new TimeOutException(maxExecutionTimeInSeconds);
		}
		if(isTerminationCriteriaReached()){
			throw new TimeOutException(maxExecutionTimeInSeconds);
		}
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
	
	private List<GeneralisedQueryTree<N>> getAllowedGeneralisationsSortedByMatrix(GeneralisedQueryTree<N> tree, List<QueryTree<N>> negTrees){
		Map<QueryTree<N>, List<Integer>> matrix = createMatrix(tree.getQueryTree(), negTrees);
		logger.debug("Matrix:");
		for(Entry<QueryTree<N>, List<Integer>> entry : matrix.entrySet()){
			logger.debug(entry.getKey().getId() + ": " + entry.getValue());
		}
		List<GeneralisedQueryTree<N>> gens = new ArrayList<GeneralisedQueryTree<N>>();
		logger.debug(TreeHelper.getAbbreviatedTreeRepresentation(negTrees.get(0), endpoint.getBaseURI(), endpoint.getPrefixes()));
		
		Map<GeneralisedQueryTree<N>, Integer> genTree2Sum = new HashMap<GeneralisedQueryTree<N>, Integer>();
		
		QueryTree<N> queryTree = tree.getQueryTree();
		QueryTreeChange lastChange = tree.getLastChange();
		List<QueryTreeChange> changes = tree.getChanges();
		GeneralisedQueryTree<N> genTree;
		N label;
		Object edge;
		QueryTree<N> parent;
		boolean isLiteralNode;
		for(QueryTree<N> node : getPossibleNodes2Change(tree.getQueryTree())){
			label = node.getUserObject();
			parent = node.getParent();
			isLiteralNode = node.isLiteralNode();
			edge = parent.getEdge(node);
			if(lastChange.getType() == ChangeType.REMOVE_NODE){
				if(node.getUserObject().equals("?") && node.getId() < lastChange.getNodeId()){
					int pos = parent.removeChild((QueryTreeImpl<N>) node);
					genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(node.getId(), ChangeType.REMOVE_NODE));
					genTree2Sum.put(genTree, sum(matrix.get(node)));
					parent.addChild((QueryTreeImpl<N>) node, edge, pos);
				}
			} else {
				if(node.getUserObject().equals("?")){
					int pos = parent.removeChild((QueryTreeImpl<N>) node);
					genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(node.getId(), ChangeType.REMOVE_NODE));
					genTree2Sum.put(genTree, sum(matrix.get(node)));
					parent.addChild((QueryTreeImpl<N>) node, edge, pos);
				} else if(lastChange.getNodeId() < node.getId()){
					node.setUserObject((N) "?");
					node.setVarNode(true);
					genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(node.getId(), ChangeType.REPLACE_LABEL));
					genTree2Sum.put(genTree, sum(matrix.get(node)));
					node.setUserObject(label);
					node.setLiteralNode(isLiteralNode);
					node.setResourceNode(!isLiteralNode);
				}
			}
		}
		List<Entry<GeneralisedQueryTree<N>, Integer>> entries = new ArrayList<Entry<GeneralisedQueryTree<N>,Integer>>(genTree2Sum.entrySet());
		Collections.sort(entries, new NegativeTreeOccurenceComparator());
		for(Entry<GeneralisedQueryTree<N>, Integer> entry : entries){
			gens.add(entry.getKey());
		}
		return gens;
	}
	
	private List<GeneralisedQueryTree<N>> getAllowedGeneralisationsSorted(GeneralisedQueryTree<N> tree){
		List<GeneralisedQueryTree<N>> gens = getAllowedGeneralisations(tree);
		Collections.sort(gens, comparator);	
		return gens;
	}
	
	/**
	 * Computing the allowed generalisations, i.e. we traverse the tree from the root depths first. For the current considered node n 
	 * if the label of the parent node is a "?" and n is a resource node, we can replace it with "?", and if the current node n is a "?"
	 * and a leaf node, it can be removed. 
	 */
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
					Map<Integer, N> node2Label = new HashMap<Integer, N>(); 
					for(QueryTree<N> c : child.getChildren()){
						if(determiningNodeIds.contains(c.getId())){
							node2Label.put(Integer.valueOf(c.getId()), c.getUserObject());
							c.setUserObject((N)"?");
						}
					}
					child.setUserObject((N) "?");
					child.setVarNode(true);
					genTree = new GeneralisedQueryTree<N>(new QueryTreeImpl<N>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REPLACE_LABEL));
					gens.add(genTree);
					child.setUserObject(label);
					child.setLiteralNode(isLiteralNode);
					child.setResourceNode(!isLiteralNode);
					for(QueryTree<N> c : child.getChildren()){
						N oldLabel = node2Label.get(c.getId());
						if(oldLabel != null){
							c.setUserObject(oldLabel);
						}
					}
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
					if(lastChange.getNodeId() < child.getId() && lastChange.getType() == ChangeType.REMOVE_NODE){
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
	
	private boolean hasAlwaysSameParent(QueryTree<N> node, List<QueryTree<N>> trees){
		N parentLabel = node.getParent().getUserObject();
		N nodeLabel = node.getUserObject();
		List<Object> path = getPathFromRootToNode(node);
		List<QueryTree<N>> nodes;
		for(QueryTree<N> tree : trees){
			nodes = getNodesByPath(tree, new ArrayList<Object>(path));
			if(!nodes.isEmpty()){
				for(QueryTree<N> otherNode : nodes){
					if(nodeLabel.equals(otherNode.getUserObject()) && !otherNode.getParent().getUserObject().equals(parentLabel)){
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private List<QueryTree<N>> getNodesByPath(QueryTree<N> tree, List<Object> path){
		List<QueryTree<N>> nodes = new ArrayList<QueryTree<N>>();
		for(QueryTree<N> child : tree.getChildren(path.remove(0))){
			if(path.isEmpty()){
				nodes.add(child);
			} else {
				nodes.addAll(getNodesByPath(child, new ArrayList<Object>(path)));
			}
		}
		return nodes;
	}
	
	private List<Object> getPathFromRootToNode(QueryTree<N> node){
		List<Object> path = new ArrayList<Object>();
		QueryTree<N> parent = node.getParent();
		path.add(parent.getEdge(node));
		if(!parent.isRoot()){
			path.addAll(getPathFromRootToNode(parent));
		}
		Collections.reverse(path);
		return path;
	}
	
	private SortedSet<String> getResources(QueryTree<N> tree){
		SortedSet<String> resources = new TreeSet<String>();
		
		query = tree.toSPARQLQueryString();
		query = getDistinctQuery(query);
		if(logger.isDebugEnabled()){
			logger.debug("Testing query\n" + getLimitedQuery(query));
		}
		String result = selectCache.executeSelectQuery(endpoint, getLimitedQuery(query));
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
		limitEqualEdgesToLeafs(tree, 1);
		query = tree.toSPARQLQueryString();
		query = getDistinctQuery(query);
		if(logger.isDebugEnabled()){
			logger.debug("Testing query\n" + getLimitedQuery(query, limit, offset));
		}
		String result = selectCache.executeSelectQuery(endpoint, getLimitedQuery(query, limit, offset));
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
		int chunkSize = 40;
		SortedSet<String> foundResources;
		QueryTree<N> newTree;
		int foundSize;
		do{
			foundResources = getResources(tree, chunkSize, chunkSize * i);
			foundSize = foundResources.size();
			foundResources.removeAll(knownResources);
			for(String resource : foundResources){
				newTree = getQueryTree(resource);
				if(!newTree.isSubsumedBy(lgg)){
					return resource;
				}
			}
			i++;
		} while(foundSize == chunkSize);
		logger.debug("Found no resource which would modify the LGG");
		return null;
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
//    		if(child.isLiteralNode()){
//    			continue;
//    		}
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
	
	
    
    public QueryTree<N> getPostLGG(){
    	return postLGG;
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
					return -1;
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
    
    class NegativeTreeOccurenceComparator implements Comparator<Entry<GeneralisedQueryTree<N>,Integer>>{

		@Override
		public int compare(Entry<GeneralisedQueryTree<N>, Integer> entry1,
				Entry<GeneralisedQueryTree<N>, Integer> entry2) {
			int sum1 = entry1.getValue();
			int sum2 = entry2.getValue();
			if(sum1 == sum2){
				QueryTreeChange change1 = entry1.getKey().getLastChange();
				QueryTreeChange change2 = entry2.getKey().getLastChange();
				if(change1.getType()==ChangeType.REPLACE_LABEL){
					if(change2.getType()==ChangeType.REPLACE_LABEL){
						return change1.getNodeId() - change2.getNodeId();
					} else {
						return -1;
					}
				} else {
					if(change2.getType()==ChangeType.REPLACE_LABEL){
						return 1;
					} else {
						return change2.getNodeId() - change1.getNodeId();
					}
				}
			} else {
				return sum2-sum1;
			}
		}
    	
    }
    
    private void limitEqualEdgesToLeafs(QueryTree<N> tree, int maxEqualEdgeCount){
    	if(tree.getChildren().isEmpty()){
    		return;
    	}
    	Set<QueryTree<N>> parents = new HashSet<QueryTree<N>>();
    	for(QueryTree<N> leaf : tree.getLeafs()){
    		if(leaf.getUserObject().equals("?")){
    			parents.add(leaf.getParent());
    		}
    	}
    	for(QueryTree<N> parent : parents){
    		for(Object edge : parent.getEdges()){
    			int cnt = 0;
    			boolean existsResourceChild = false;
    			for(QueryTree<N> child : parent.getChildren(edge)){
    				if(!child.getUserObject().equals("?")){
    					existsResourceChild = true;
    					break;
    				}
    			}
    			for(QueryTree<N> child : parent.getChildren(edge)){
    				if(child.getUserObject().equals("?")){
    					if(child.isLeaf()){
    						cnt++;
    						if(existsResourceChild || cnt>maxEqualEdgeCount){
    							parent.removeChild((QueryTreeImpl<N>) child);
    						}
    					}
    				}
    			}
    		}
    	}
    	
    }
    
    public void stop(){
    	stop = true;
    }
    
    public boolean isRunning(){
    	return isRunning;
    }
    
    private boolean isTerminationCriteriaReached(){
    	if(stop){
    		return true;
    	}
    	boolean result = false;
    	long totalTimeNeeded = System.currentTimeMillis() - this.startTime;
		long maxMilliSeconds = maxExecutionTimeInSeconds * 1000;
    	result = totalTimeNeeded >= maxMilliSeconds;
    	return result;
    }

}
