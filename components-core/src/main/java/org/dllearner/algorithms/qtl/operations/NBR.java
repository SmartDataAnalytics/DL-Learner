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
package org.dllearner.algorithms.qtl.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.http.HTTPException;

import org.apache.jena.graph.NodeFactory;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.GeneralisedQueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeChange;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeChange.ChangeType;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.exception.TimeOutException;
import org.dllearner.algorithms.qtl.util.SPARQLEndpointEx;
import org.dllearner.algorithms.qtl.util.TreeHelper;
import org.dllearner.kb.sparql.SparqlEndpoint;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.RDF;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class NBR<N> {
	
	private boolean generalizeSortedByNegatives = false;
	
	private volatile boolean stop = false;
	private boolean isRunning;
	private int maxExecutionTimeInSeconds = 100;
	private long startTime;
	
	private SparqlEndpoint endpoint;
	private Model model;
	private org.aksw.jena_sparql_api.core.QueryExecutionFactory qef;
	
	private String query;
	private int limit;
	
	private int nodeId;
	private QueryTree<N> lgg;
	private QueryTree<N> postLGG;
	private List<QueryTree<N>> negTrees;
	private List<Integer> determiningNodeIds;
	
	private List<List<QueryTreeChange>> noSequences;
	private List<QueryTreeChange> lastSequence;
	private int negExamplesCount = -1;
	private Set<String> lggInstances;
	
	private LastQueryTreeChangeComparator comparator = new LastQueryTreeChangeComparator();
	
	private Monitor mon = MonitorFactory.getTimeMonitor("NBR");
	
	private static final Logger logger = Logger.getLogger(NBR.class);
	
	public NBR(SparqlEndpoint endpoint){
		this(endpoint, null);
	}
	
	public NBR(SparqlEndpoint endpoint, String cacheDirectory){
		this.endpoint = endpoint;
		
		noSequences = new ArrayList<>();
		
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cacheDirectory != null){
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend(cacheDirectory, true, timeToLive);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		}
	}
	
	public NBR(Model model){
		this.model = model;
		
		noSequences = new ArrayList<>();
		
		qef = new QueryExecutionFactoryModel(model);
	}
	
	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds){
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}
	
	
	private Map<QueryTree<N>, List<Integer>> createMatrix(QueryTree<N> tree, List<QueryTree<N>> negTrees){
		Map<QueryTree<N>, List<Integer>> matrix = new HashMap<>();
		for(int i = 0; i < negTrees.size(); i++){
			checkTree(matrix, tree, negTrees.get(i), i);
		}
		return matrix;
	}
	
	private List<Integer> getDeterminingNodeIds(QueryTree<N> lgg, List<QueryTree<N>> trees){
		List<Integer> nodeIds = new ArrayList<>();
		
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
		List<QueryTree<N>> leafs = new ArrayList<>();
		
		SortedMap<Integer, List<QueryTree<N>>> map = new TreeMap<>();
		int rowSum;
		List<QueryTree<N>> treeList;
		for(Entry<QueryTree<N>, List<Integer>> entry : matrix.entrySet()){
			rowSum = sum(entry.getValue());
			treeList = map.get(rowSum);
			if(treeList == null){
				treeList = new ArrayList<>();
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
			list = new ArrayList<>();
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
        sb.append(tree.getUserObject()).append("(").append(matrix.get(tree)).append(")");
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
//		return computeQuestionOptimized(lgg, negTrees, knownResources);
		mon.start();
		String question = computeQuestionBetterPerformance(lgg, negTrees, knownResources);
		mon.stop();
		return question;
	}
	
	public void setLGGInstances(Set<String> instances){
		this.lggInstances = instances;
	}
	
	
	private String computeQuestionBetterPerformance(QueryTree<N> lgg, List<QueryTree<N>> negTrees, List<String> knownResources) throws TimeOutException{
		startTime = System.currentTimeMillis();
		this.lgg = lgg;
		this.negTrees = negTrees;
		if(userAnsweredWithNo()){
			noSequences.add(lastSequence);
		}
		negExamplesCount = negTrees.size();
		determiningNodeIds = getDeterminingNodeIds(lgg, negTrees);
		logger.debug("Computing next question...");
		postLGG = getFilteredTree(lgg);
		PostLGG<N> postGen;
		if(endpoint != null){
			postGen = new PostLGG<>(endpoint);
		} else {
			postGen = new PostLGG<>();
		}
		
		postGen.simplifyTree(postLGG, negTrees);
		if(logger.isDebugEnabled()){
			String treeString;
			if(endpoint instanceof SPARQLEndpointEx){
				treeString = TreeHelper.getAbbreviatedTreeRepresentation(
						postLGG, ((SPARQLEndpointEx)endpoint).getBaseURI(), ((SPARQLEndpointEx)endpoint).getPrefixes());
			} else {
				treeString = postLGG.getStringRepresentation();
			}
			logger.debug("Post LGG(Tree): \n" + treeString);
			logger.debug("Post LGG(Query):\n" + postLGG.toSPARQLQueryString());
			logger.debug("Post LGG(#Instances):\n" + getAllResources(postLGG.toSPARQLQueryString()).size());
		}
		
		limit = knownResources.size();
		
		List<GeneralisedQueryTree<N>> queue = null;
		if(generalizeSortedByNegatives){
			queue = getAllowedGeneralisationsSortedByMatrix(new GeneralisedQueryTree<>(postLGG), negTrees);
		} else {
			queue = getAllowedGeneralisationsSorted2(new GeneralisedQueryTree<>(postLGG));
		}
		logger.debug(getQueueLogInfo(queue));
		
		GeneralisedQueryTree<N> tree1;
		GeneralisedQueryTree<N> tree2;
		GeneralisedQueryTree<N> tmp;
		List<GeneralisedQueryTree<N>> gens;
		List<GeneralisedQueryTree<N>> neededGeneralisations;
		while(!queue.isEmpty()){
			neededGeneralisations = new ArrayList<>();
			logger.debug("Selecting first tree from queue");
//			tree1 = queue.remove(0);
			tree1 = getGeneralisedQueryTreeNotContainingNoSequence(queue);
			tmp = tree1;
			
			if(logger.isDebugEnabled()){
				logger.debug("Changes: " + tmp.getChanges());
			}
			boolean coversNegTree = coversNegativeTree(tmp.getQueryTree(), negTrees);
			neededGeneralisations.add(tmp);
			logger.debug("covers negative tree: " + coversNegTree);
			while(!coversNegTree){
				if(generalizeSortedByNegatives){
					gens = getAllowedGeneralisationsSortedByMatrix(tmp, negTrees);
				} else {
					gens = getAllowedGeneralisationsSorted2(tmp);
				}
				if(gens.isEmpty()){
					if(logger.isDebugEnabled()){
						logger.debug("Couldn't create a generalisation which covers a negative tree.");
					}
					break;
				}
//				tmp = gens.remove(0);
				tmp = getGeneralisedQueryTreeNotContainingNoSequence(gens);
				
				if(logger.isDebugEnabled()){
					logger.debug("Changes: " + tmp.getChanges());
				}
				queue.addAll(0, gens);
				logger.debug(getQueueLogInfo(queue));
				coversNegTree = coversNegativeTree(tmp.getQueryTree(), negTrees);
				if(coversNegTree) {
					logger.debug("covers negative tree by changes " + tmp.getChanges());
				} else {
					neededGeneralisations.add(tmp);
				}
			}
		
			int index = neededGeneralisations.size()-1;
			if(coversNegTree){
				if(index == -1){
					tree2 = tmp;
				}
				tree2 = neededGeneralisations.get(index--);
			} else {
				tree2 = tmp;
			}
			
//			QueryTree<N> newTree = getNewResource(tree2, knownResources);
			if(logger.isDebugEnabled()){
				logger.debug("Testing tree\n" + tree2.getQueryTree().getStringRepresentation());
			}
			String newResource = getNewResource2(fSparql(lgg, tree2.getChanges()), knownResources);
			if(isTerminationCriteriaReached()){
				throw new TimeOutException(maxExecutionTimeInSeconds);
			}
			logger.debug("New resource before binary search: " + newResource);
			if(!(newResource == null)){
				logger.debug("binary search for most specific query returning a resource - start");
				List<QueryTreeChange> firstChanges = new ArrayList<>(neededGeneralisations.get(0).getChanges());
				while(firstChanges.size() > 1){
					firstChanges.remove(firstChanges.size()-1);
					neededGeneralisations.add(0, new GeneralisedQueryTree<>(getTreeByChanges(lgg, firstChanges), firstChanges));
					firstChanges = new ArrayList<>(firstChanges);
				}
				newResource = findMostSpecificResourceTree2(neededGeneralisations, knownResources, 0, neededGeneralisations.size()-1);
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
	
	private GeneralisedQueryTree<N> getGeneralisedQueryTreeNotContainingNoSequence(List<GeneralisedQueryTree<N>> queue){
		GeneralisedQueryTree<N> genTree;
		for(int i = 0; i < queue.size(); i++){
			genTree = queue.get(i);
			boolean containsNoSequence = false;
			for(List<QueryTreeChange> seq : noSequences){
				if(genTree.getChanges().containsAll(seq)){
					logger.info("Skipping sequence from queue " + genTree.getChanges() + " because it contains NO sequence" + seq);
					containsNoSequence = true;
					break;
				}
			}
			if(!containsNoSequence){
				return queue.remove(i);
			}
		}
		return queue.remove(0);
	}
	
	private boolean userAnsweredWithNo(){
		return (negExamplesCount != -1) && (negTrees.size() > negExamplesCount);
	}
	
	private SortedSet<String> getAllResources(String query){
		SortedSet<String> resources = new TreeSet<>();
		query = query + " LIMIT 1000";
		
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			resources.add(qs.getResource("x0").getURI());
		}
		qe.close();
		return resources;
	}
	
	private String findMostSpecificResourceTree2(List<GeneralisedQueryTree<N>> trees, List<String> knownResources, int low, int high) throws TimeOutException {
		
//		if(low==high) {
//			return low;
//		}
		int testIndex = low + (high-low)/2;
		// perform SPARQL query
		
//		QueryTree<N> t = getNewResource(trees.get(testIndex), knownResources);
		String t = null;
		GeneralisedQueryTree<N> genTree = trees.get(testIndex);
		if(logger.isDebugEnabled()){
			logger.debug("Binary search: Testing tree\n" + genTree.getQueryTree().getStringRepresentation());
		}
		try {
			t = getNewResource2(fSparql(lgg, genTree.getChanges()), knownResources);
		} catch (HTTPException e) {
			throw new TimeOutException(maxExecutionTimeInSeconds);
		}
		if(isTerminationCriteriaReached()){
			throw new TimeOutException(maxExecutionTimeInSeconds);
		}
		if(testIndex == high){
			lastSequence = trees.get(testIndex).getChanges();
			return t;
		}
		if(t == null) {
			return findMostSpecificResourceTree2(trees,knownResources,testIndex+1,high);
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("Binary search: Found new resource \"" + t + "\"");
			}
			return findMostSpecificResourceTree2(trees,knownResources,low,testIndex);
		}
	}
	
	public List<GeneralisedQueryTree<N>> getAllowedGeneralisations(GeneralisedQueryTree<N> tree){
		logger.debug("Computing allowed generalisations...");
		List<GeneralisedQueryTree<N>> gens = new LinkedList<>();
		gens.addAll(computeAllowedGeneralisations(tree, tree.getLastChange()));
		return gens;
	}
	
	private List<QueryTree<N>> getPossibleNodes2Change(QueryTree<N> tree){
		List<QueryTree<N>> nodes = new ArrayList<>();
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
		List<GeneralisedQueryTree<N>> gens = new ArrayList<>();
		if(logger.isDebugEnabled()){
			String treeString;
			if(endpoint instanceof SPARQLEndpointEx){
				treeString = TreeHelper.getAbbreviatedTreeRepresentation(
						negTrees.get(0), ((SPARQLEndpointEx)endpoint).getBaseURI(), ((SPARQLEndpointEx)endpoint).getPrefixes());
			} else {
				treeString = negTrees.get(0).getStringRepresentation();
			}
			logger.debug(treeString);
		}
		
		
		Map<GeneralisedQueryTree<N>, Integer> genTree2Sum = new HashMap<>();
		
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
					genTree = new GeneralisedQueryTree<>(new QueryTreeImpl<>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(node.getId(), ChangeType.REMOVE_NODE));
					genTree2Sum.put(genTree, sum(matrix.get(node)));
					parent.addChild((QueryTreeImpl<N>) node, edge, pos);
				}
			} else {
				if(node.getUserObject().equals("?")){
					int pos = parent.removeChild((QueryTreeImpl<N>) node);
					genTree = new GeneralisedQueryTree<>(new QueryTreeImpl<>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(node.getId(), ChangeType.REMOVE_NODE));
					genTree2Sum.put(genTree, sum(matrix.get(node)));
					parent.addChild((QueryTreeImpl<N>) node, edge, pos);
				} else if(lastChange.getNodeId() < node.getId()){
					node.setUserObject((N) "?");
					node.setVarNode(true);
					genTree = new GeneralisedQueryTree<>(new QueryTreeImpl<>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(node.getId(), ChangeType.REPLACE_LABEL));
					genTree2Sum.put(genTree, sum(matrix.get(node)));
					node.setUserObject(label);
					node.setIsLiteralNode(isLiteralNode);
					node.setIsResourceNode(!isLiteralNode);
				}
			}
		}
		List<Entry<GeneralisedQueryTree<N>, Integer>> entries = new ArrayList<>(genTree2Sum.entrySet());
		Collections.sort(entries, new NegativeTreeOccurenceComparator());
		for(Entry<GeneralisedQueryTree<N>, Integer> entry : entries){
			gens.add(entry.getKey());
		}
		return gens;
	}
	
	private List<GeneralisedQueryTree<N>> getAllowedGeneralisationsSorted2(GeneralisedQueryTree<N> tree){
		List<GeneralisedQueryTree<N>> gens = getAllowedGeneralisations(tree);
		Iterator<GeneralisedQueryTree<N>> it = gens.iterator();
		GeneralisedQueryTree<N> t;
		while(it.hasNext()){
			t = it.next();
			for(List<QueryTreeChange> changes : noSequences){
				if(t.getChanges().containsAll(changes)){
					it.remove();
					break;
				}
			}
		}
		Collections.sort(gens, comparator);	
		return gens;
	}
	
	/**
	 * Computing the allowed generalisations, i.e. we traverse the tree from the root depths first. For the current considered node n 
	 * if the label of the parent node is a "?" and n is a resource node, we can replace it with "?", and if the current node n is a "?"
	 * and a leaf node, it can be removed. 
	 */
	private List<GeneralisedQueryTree<N>> computeAllowedGeneralisations(GeneralisedQueryTree<N> tree, QueryTreeChange lastChange){
		List<GeneralisedQueryTree<N>> gens = new LinkedList<>();
		
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
					genTree = new GeneralisedQueryTree<>(new QueryTreeImpl<>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REPLACE_LABEL));
					gens.add(genTree);
					parent.addChild((QueryTreeImpl<N>) child, edge, pos);
				} else {
					Map<Integer, N> node2Label = new HashMap<>();
					for(QueryTree<N> c : child.getChildren()){
						if(determiningNodeIds.contains(c.getId())){
							node2Label.put(c.getId(), c.getUserObject());
							c.setUserObject((N)"?");
						}
					}
					child.setUserObject((N) "?");
					child.setVarNode(true);
					genTree = new GeneralisedQueryTree<>(new QueryTreeImpl<>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REPLACE_LABEL));
					gens.add(genTree);
					child.setUserObject(label);
					child.setIsLiteralNode(isLiteralNode);
					child.setIsResourceNode(!isLiteralNode);
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
					genTree = new GeneralisedQueryTree<>(new QueryTreeImpl<>(queryTree));
					genTree.addChanges(changes);
					genTree.addChange(new QueryTreeChange(child.getId(), ChangeType.REMOVE_NODE));
					gens.add(genTree);
					parent.addChild((QueryTreeImpl<N>) child, edge, pos);
				} else {
					int pos = parent.removeChild((QueryTreeImpl<N>) child);
					for(GeneralisedQueryTree<N> subTree : computeAllowedGeneralisations(new GeneralisedQueryTree<>(child), tree.getLastChange())){
						parent.addChild((QueryTreeImpl<N>) subTree.getQueryTree(), edge, pos);
						genTree = new GeneralisedQueryTree<>(new QueryTreeImpl<>(queryTree));
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
			nodes = getNodesByPath(tree, new ArrayList<>(path));
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
		List<QueryTree<N>> nodes = new ArrayList<>();
		for(QueryTree<N> child : tree.getChildren(path.remove(0))){
			if(path.isEmpty()){
				nodes.add(child);
			} else {
				nodes.addAll(getNodesByPath(child, new ArrayList<>(path)));
			}
		}
		return nodes;
	}
	
	private List<Object> getPathFromRootToNode(QueryTree<N> node){
		List<Object> path = new ArrayList<>();
		QueryTree<N> parent = node.getParent();
		path.add(parent.getEdge(node));
		if(!parent.isRoot()){
			path.addAll(getPathFromRootToNode(parent));
		}
		Collections.reverse(path);
		return path;
	}
	
	private SortedSet<String> getResources(String query, int limit, int offset){
		SortedSet<String> resources = new TreeSet<>();
		this.query = query;
		if(logger.isDebugEnabled()){
			logger.debug("Testing query\n" + getLimitedQuery(query, limit, offset) + "\n");
		}
		
		QueryExecution qe = qef.createQueryExecution(getLimitedQuery(query, limit, offset));
		ResultSet rs = qe.execSelect();
		
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			resources.add(qs.getResource("x0").getURI());
		}
		qe.close();
		
		return resources;
	}
	
	private String getNewResource2(String query, List<String> knownResources){
		SortedSet<String> foundResources;
//		int i = 0;
//		int chunkSize = 40;
//		QueryTree<N> newTree;
//		int foundSize;
//		do{
//			foundResources = getResources(query, chunkSize, chunkSize * i);
//			foundSize = foundResources.size();
//			foundResources.removeAll(knownResources);
//			for(String resource : foundResources){System.err.println(resource);
//				newTree = getQueryTree(resource);
//				if(!newTree.isSubsumedBy(lgg)){mon.stop();System.err.println(mon.getLastValue());
//					return resource;
//				}
//			}
//			i++;
//		} while(foundSize == chunkSize);
		foundResources = getResources(query, lggInstances.size()+1, 0);
		foundResources.removeAll(knownResources);
		foundResources.removeAll(lggInstances);
		if(!foundResources.isEmpty()){
//			System.err.println(foundResources.first());
			return foundResources.first();
		}
		if(logger.isDebugEnabled()){
			logger.debug("Found no resource which would modify the LGG");
		}
		return null;
	}
	
	private String getLimitedQuery(String query, int limit, int offset){
		return query + " LIMIT " + limit + " OFFSET " + offset;
	}
	
	private QueryTree<N> getFilteredTree(QueryTree<N> tree){
		nodeId = 0;
		QueryTree<N> filteredTree = createFilteredTree(tree);
		return tree;//filteredTree;
	}
	
	private QueryTree<N> createFilteredTree(QueryTree<N> tree){
		QueryTree<N> filteredTree = new QueryTreeImpl<>(tree.getUserObject());
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
    		subTree.setIsLiteralNode(child.isLiteralNode());
    		subTree.setIsResourceNode(child.isResourceNode());
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
						
						if(nodeId1 != nodeId2){
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
    	Set<QueryTree<N>> parents = new HashSet<>();
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
    
    private String fSparql(QueryTree<N> tree, List<QueryTreeChange> changes){
    	logger.debug("fSparql uses:" + changes);
    	QueryTree<N> copy = new QueryTreeImpl<>(tree);
    	StringBuilder query = new StringBuilder();
    	StringBuilder triples = new StringBuilder();
    	List<String> optionals = new ArrayList<>();
    	List<String> filters = new ArrayList<>();
    	query.append("SELECT DISTINCT ?x0 WHERE{\n");
    	buildSPARQLQueryString(copy, changes, triples, optionals, filters);
    	if(triples.toString().isEmpty()){
    		triples.append("?x0 ?p ?o.\n");
    	}
    	query.append(triples.toString());
    	for(String optional : optionals){
    		query.append("OPTIONAL{").append(optional).append("}\n");
    	}
    	if(filters.size() > 0){
    		query.append("FILTER(");
    		for(int i = 0; i < filters.size()-1; i++){
    			query.append("(").append(filters.get(i)).append(") || ");
    		}
    		query.append("(").append(filters.get(filters.size()-1)).append(")");
    		query.append(")\n");
    	}
    	query.append("}");
//    	if(logger.isDebugEnabled()){
//    		logger.debug("fsparql: generated query: \n" + query.toString());
//    	}
    	return query.toString();
    	
    }
    
    private String fSparql2(QueryTree<N> tree, List<QueryTreeChange> changes){
    	logger.debug("fSparql uses:" + changes);//getSPARQLQueries(tree, changes);
    	QueryTree<N> copy = new QueryTreeImpl<>(tree);
    	StringBuilder query = new StringBuilder();
    	StringBuilder triples = new StringBuilder();
    	List<String> optionals = new ArrayList<>();
    	Map<String, String> filters = new HashMap<>();
    	List<String> bounds = new ArrayList<>();
    	query.append("SELECT DISTINCT ?x0 WHERE{\n");
    	buildSPARQLQueryString2(copy, changes, triples, optionals, filters, bounds);
    	if(triples.toString().isEmpty()){
    		triples.append("?x0 ?p ?o.\n");
    	}
    	query.append(triples.toString());
    	for(String optional : optionals){
    		query.append("OPTIONAL{").append(optional).append("}\n");
    	}
    	List<String> filterParts = new ArrayList<>();
    	filterParts.addAll(filters.keySet());
    	filterParts.addAll(bounds);
    	if(filterParts.size() > 0){
    		query.append("FILTER(\n");
    		String currentPart = null;
    		for(int i = 0; i < filterParts.size(); i++){
    			int cnt = 1;
    			query.append("(");
    			currentPart = filterParts.get(i);
    			if(filters.get(currentPart) != null){
    				currentPart = currentPart + "!=" + filters.get(currentPart);
    				cnt++;
    				query.append(currentPart);
    				if(filters.keySet().size() > 1){
    					query.append(" && ");
    				}
    			} else if(bounds.contains(currentPart)){
    				currentPart = "!BOUND(" + currentPart + ")";
    				query.append(currentPart);
    				if(!filters.keySet().isEmpty()){
    					query.append(" && ");
    				}
    			}
    			for(String f : filters.keySet()){
    				if(!filterParts.get(i).equals(f)){
    					query.append(f).append("=").append(filters.get(f));
    					if(cnt < filters.keySet().size()){
    						query.append(" && ");
    					}
    					cnt++;
    				} else {
//    					cnt++;
    				}
    				
    				
        		}
    			query.append(")");
    			if(i < filterParts.size()-1){
    				query.append("\n||\n");
				}
    			
    			
    		}
    		query.append("\n)\n");
    	}
    	query.append("}");
    	if(logger.isDebugEnabled()){
    		logger.debug("fsparql: generated query: \n" + query.toString());
    	}
    	return query.toString();
    	
    }
    
    
    private void buildSPARQLQueryString(QueryTree<N> tree, List<QueryTreeChange> changes, StringBuilder triples, List<String> optionals, List<String> filters){
    	Object subject = null;
    	if(tree.getUserObject().equals("?")){
    		subject = "?x" + tree.getId();
    	} else {
    		subject = "<" + tree.getUserObject() + ">";
    	}
    	Object predicate;
    	Object object;
    	if(!tree.isLeaf()){
    		for(QueryTree<N> child : tree.getChildren()){
        		predicate = tree.getEdge(child);
        		object = child.getUserObject();
        		
        		boolean addFilter = false;
        		boolean removed = false;
        		String uri = null;
        		QueryTreeChange c = getChange(changes, child.getId());
    			if(c != null){
    				if(c.getType() == ChangeType.REPLACE_LABEL){
    					uri = (String) object;
    					if(((String)child.getUserObject()).contains("^^") || ((String)child.getUserObject()).contains("@")){
    						filters.add("?x" + child.getId() + "!=" + uri);
    					} else {
    						filters.add("?x" + child.getId() + "!=<" + uri + ">");
    					}
    					
        				child.setUserObject((N)"?");
    				} else {
    					removed = true;
    					if(!predicate.equals(RDF.type.toString())){
	    					optionals.add(subject + " <" + predicate + "> ?x" + child.getId());
//	    					triples.append("OPTIONAL{").append(subject).
//	    					append(" <").append(predicate).append("> ").append("?x").append(child.getId()).append("}\n");
	    					filters.add("!BOUND(?x" + child.getId() + ")");
    					}
    					child.getParent().removeChild((QueryTreeImpl<N>) child);
    				}
    				
    			}
    			object = child.getUserObject();
    			boolean objectIsResource = !object.equals("?");
        		if(!objectIsResource){
        			object = "?x" + child.getId();
        		} else if(((String)object).startsWith("http://")){
        			object = "<" + object + ">";
        		}
        		if(!removed){
        			triples.append(subject).append(" <").append(predicate).append("> ").append(object).append(".\n");
        		}
        		if(!objectIsResource){
        			buildSPARQLQueryString(child, changes, triples, optionals, filters);
        		}
        	}
    	}
    }
    
    private void buildSPARQLQueryString2(QueryTree<N> tree, List<QueryTreeChange> changes, StringBuilder triples, List<String> optionals, Map<String, String> filters, List<String> bounds){
    	Object subject = null;
    	if(tree.getUserObject().equals("?")){
    		subject = "?x" + tree.getId();
    	} else {
    		subject = "<" + tree.getUserObject() + ">";
    	}
    	Object predicate;
    	Object object;
    	if(!tree.isLeaf()){
    		for(QueryTree<N> child : tree.getChildren()){
        		predicate = tree.getEdge(child);
        		object = child.getUserObject();
        		
        		boolean addFilter = false;
        		boolean removed = false;
        		String uri = null;
        		QueryTreeChange c = getChange(changes, child.getId());
    			if(c != null){
    				if(c.getType() == ChangeType.REPLACE_LABEL){
    					uri = (String) object;
    					if(((String)child.getUserObject()).contains("^^") || ((String)child.getUserObject()).contains("@")){
//    						filters.add("?x" + child.getId() + "!=" + uri);
    						filters.put("?x" + child.getId(), uri);
    					} else {
//    						filters.add("?x" + child.getId() + "!=<" + uri + ">");
    						filters.put("?x" + child.getId(), "<" + uri + ">");
    					}
    					
        				child.setUserObject((N)"?");
    				} else {
    					removed = true;
    					if(!predicate.equals(RDF.type.toString())){
	    					optionals.add(subject + " <" + predicate + "> ?x" + child.getId());
	//    					triples.append("OPTIONAL{").append(subject).
	//    					append(" <").append(predicate).append("> ").append("?x").append(child.getId()).append("}\n");
//	    					filters.add("!BOUND(?x" + child.getId() + ")");
	    					bounds.add("?x" + child.getId());
    					}
    					child.getParent().removeChild((QueryTreeImpl<N>) child);
    				}
    				
    			}
    			object = child.getUserObject();
    			boolean objectIsResource = !object.equals("?");
        		if(!objectIsResource){
        			object = "?x" + child.getId();
        		} else if(((String)object).startsWith("http://")){
        			object = "<" + object + ">";
        		}
        		if(!removed){
        			triples.append(subject).append(" <").append(predicate).append("> ").append(object).append(".\n");
        		}
        		if(!objectIsResource){
        			buildSPARQLQueryString2(child, changes, triples, optionals, filters, bounds);
        		}
        	}
    	}
    }
    
    private List<String> getSPARQLQueries(QueryTree<N> tree, List<QueryTreeChange> changes){
    	List<String> queries = new ArrayList<>();
    	
    	String originalQuery = tree.toSPARQLQueryString();
    	
    	for(QueryTree<N> leaf : tree.getLeafs()){
    		QueryTreeChange c = getChange(changes, leaf.getId());
    		if(c != null){
    			if(c.getType() == ChangeType.REPLACE_LABEL){
    				System.out.println("JENA:\n " + getSPARQLQuery(originalQuery, nodeId, (String) leaf.getUserObject()));
    			} else if(c.getType() == ChangeType.REMOVE_NODE){
    				System.out.println("JENA:\n " + getSPARQLQuery2(tree.toQuery(), nodeId));
    			}
    		}
    	}
    	
    	
    	
    	return queries;
    }
    
    private String getSPARQLQuery2(Query originalQuery, int nodeId){
		Query query = QueryFactory.create(originalQuery);
		Element elt = query.getQueryPattern();
		if ( elt instanceof ElementGroup ){
			Node node = null;
			Triple optional = null;
			for (Element el : ((ElementGroup) elt).getElements()) {
				if (el instanceof ElementTriplesBlock) {
					Triple current;
					int position = 1;
					for (Iterator<Triple> iter = ((ElementTriplesBlock) el).getPattern().iterator(); iter.hasNext();) {
						current = iter.next();
						if (current.getObject().isVariable() && current.getObject().getName().equals("x" + nodeId)) {
							node = current.getObject();
							position = ((ElementTriplesBlock) el).getPattern().getList().indexOf(current);
							optional = current;
							iter.remove();
						}
					}
				}
			}
			
			if(optional != null){
				ElementTriplesBlock optionalTriplesBlock = new ElementTriplesBlock();
				optionalTriplesBlock.addTriple(optional);
				((ElementGroup) elt).addElement(new ElementOptional(optionalTriplesBlock));
				
				
				ElementFilter filter = new ElementFilter(new E_LogicalNot(new ExprVar(optional.getObject().getName())));
            	((ElementGroup)elt).addElementFilter(filter);
			}
		}
		return query.toString();
	}
    
    private String getSPARQLQuery(String queryString, int nodeId, String label){
		Query query = QueryFactory.create(queryString);
		Element elt = query.getQueryPattern();
		if ( elt instanceof ElementGroup ){
			Node node = null;
			boolean addFilter = false;
			for(Element el : ((ElementGroup)elt).getElements()){
				if ( el instanceof ElementTriplesBlock )
	            {	Triple add = null;
	            Triple current;
	            int position = 1;
	                for(Iterator<Triple> iter = ((ElementTriplesBlock) el).getPattern().iterator() ; iter.hasNext() ; ){
	                	current = iter.next();
	                	if(current.getObject().toString().equals(label)){
	                		node = current.getObject();
	                		position = ((ElementTriplesBlock) el).getPattern().getList().indexOf(current);
	                		add = Triple.create(current.getSubject(), current.getPredicate(), NodeFactory.createVariable("x" + nodeId));
	                		iter.remove();
	                	}
	                }
	                if(add != null){
	                	((ElementTriplesBlock) el).addTriple(position, add);
	                	addFilter = true;
	                }
	            }
			}
			if(addFilter){
				ElementFilter filter = new ElementFilter(new E_Equals(new ExprVar(Integer.toString(nodeId)), new NodeValueNode(node)));
            	((ElementGroup)elt).addElementFilter(filter);
			}
		}
		return query.toString();
	}
    
//    private void buildSPARQLQueryString(QueryTree<N> tree, List<QueryTreeChange> changes, StringBuilder triples, List<String> filters){
//    	Object subject = null;
//    	if(tree.getUserObject().equals("?")){
//    		subject = "?x" + tree.getId();
//    	} else {
//    		subject = "<" + tree.getUserObject() + ">";
//    	}
//    	Object predicate;
//    	Object object;
//    	if(!tree.isLeaf()){
//    		for(QueryTree<N> child : tree.getChildren()){
//        		predicate = tree.getEdge(child);
//        		object = child.getUserObject();
//        		boolean objectIsResource = !object.equals("?");
//        		boolean addFilter = false;
//        		boolean removed = false;
//        		String uri = null;
//        		if(!objectIsResource){
//        			object = "?x" + child.getId();
//        		} else if(((String)object).startsWith("http://")){
//        			QueryTreeChange c = getChange(changes, child.getId());
//        			if(c != null){
//        				if(c.getType() == ChangeType.REPLACE_LABEL){
//        					uri = (String) object;
//            				child.setUserObject((N)"?");
//            				object = "?x" + child.getId();
//            				addFilter = true;
//        				} else {
//        					removed = true;
//        					triples.append("OPTIONAL{").append(subject).
//        					append(" <").append(predicate).append("> ").append("?x").append(child.getId()).append("}\n");
//        					filters.add("!BOUND(?x" + child.getId() + ")");
//        					child.getParent().removeChild((QueryTreeImpl<N>) child);
//        				}
//        				
//        			} else {
//        				object = "<" + object + ">";
//        			}
//        			
//        		}
//        		if(!removed){
//        			triples.append(subject).append(" <").append(predicate).append("> ").append(object).append(".\n");
//        		}
//        		if(addFilter){
//        			filters.add("?x" + child.getId() + "!=<" + uri + ">");
//        		}
//        		if(!objectIsResource){
//        			buildSPARQLQueryString(child, changes, triples, filters);
//        		}
//        	}
//    	}
//    }
    
    private QueryTree<N> getTreeByChanges(QueryTree<N> originalTree, List<QueryTreeChange> changes){
    	QueryTree<N> copy = new QueryTreeImpl<>(originalTree);
    	QueryTree<N> node;
    	for(QueryTreeChange change : changes){
    		node = copy.getNodeById(change.getNodeId());
    		if(change.getType() == ChangeType.REPLACE_LABEL){
    			node.setUserObject((N)"?");
    		} else {
    			node.getParent().removeChild((QueryTreeImpl<N>) node);
    		}
    	}
    	return copy;
    }
    
    private QueryTreeChange getChange(List<QueryTreeChange> changes, int nodeId){
    	QueryTreeChange change = null;
    	for(QueryTreeChange c : changes){
    		if(c.getNodeId() == nodeId){
    			if(c.getType() == ChangeType.REMOVE_NODE){
    				return c;
    			} else {
    				change = c;
    			}
    		}
    	}
    	return change;
    }
    
    private ResultSet executeSelectQuery(String query){
    	ResultSet rs;
    	if(model == null){
    		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
    		queryExecution.setTimeout(maxExecutionTimeInSeconds * 1000);
    		for (String dgu : endpoint.getDefaultGraphURIs()) {
    			queryExecution.addDefaultGraph(dgu);
    		}
    		for (String ngu : endpoint.getNamedGraphURIs()) {
    			queryExecution.addNamedGraph(ngu);
    		}			
    		rs = queryExecution.execSelect();
    	} else {
    		rs = QueryExecutionFactory.create(query, model).execSelect();
    	}
    	
		return rs;
    }

}
