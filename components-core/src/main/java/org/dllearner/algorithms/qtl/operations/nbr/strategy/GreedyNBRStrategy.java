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
package org.dllearner.algorithms.qtl.operations.nbr.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import org.apache.jena.graph.Node;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class GreedyNBRStrategy implements NBRStrategy{
	
	private static final Logger logger = Logger.getLogger(GreedyNBRStrategy.class);
	
	private int maxEqualEdgesFromRoot = 3;
	
	private Random random;
	
	private boolean useWeakGeneralisation = true;
	
	public GreedyNBRStrategy(){
		random = new Random();
	}

	@Override
	public RDFResourceTree computeNBR(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees) {
		if(logger.isInfoEnabled()){
			logger.info("Making NBR...");
		}
		logger.info("LGG:\n" + QueryTreeUtils.toSPARQLQueryString(posExampleTree));
		Monitor mon = MonitorFactory.getTimeMonitor("NBR");
		mon.start();
		
		RDFResourceTree nbr = new RDFResourceTree(posExampleTree);
		Map<RDFResourceTree, List<Integer>> matrix = new HashMap<>();
		
		for(int i = 0; i < negExampleTrees.size(); i++){
			checkTree(matrix, nbr, negExampleTrees.get(i), i);
		}
		
		
		int negTreeSize = negExampleTrees.size();
		Map<RDFResourceTree, Double> rowValues = new HashMap<>();
		double value;
		for(Entry<RDFResourceTree, List<Integer>> entry : matrix.entrySet()){
			value = (sum(entry.getValue())+1.0)/(negTreeSize+2.0);
			rowValues.put(entry.getKey(), value);
		}

		
		List<RDFResourceTree> candidates2Remove = new ArrayList<>();
		if(useWeakGeneralisation){
			for(Entry<RDFResourceTree, Double> entry : rowValues.entrySet()){
				if(random.nextDouble() < entry.getValue()){
					candidates2Remove.add(entry.getKey());
				}
			}
			useWeakGeneralisation = false;
		} else {
			for(Entry<RDFResourceTree, Double> entry : rowValues.entrySet()){
				if(random.nextDouble() < (1 - entry.getValue())){
					candidates2Remove.add(entry.getKey());
				}
			}
		}
		
//		RDFResourceTree parent;
//		for(RDFResourceTree leaf : new ArrayList<RDFResourceTree>(nbr.getLeafs())){
//			parent = leaf.getParent();
//			if(candidates2Remove.contains(leaf)){
//				if(logger.isInfoEnabled()){
//					logger.info("Removing edge [" + 
//							leaf.getParent().getUserObject() + "--" + leaf.getParent().getEdge(leaf) + "-->" + leaf.getUserObject() + "]");
//				}
//				leaf.getParent().removeChild((QueryTreeImpl<N>) leaf);
//				if(logger.isInfoEnabled()){
//					logger.info("Checking if removal leads to cover a negative tree...");
//				}
//				if(coversNegativeTree(nbr, negExampleTrees)){
//					parent.addChild((QueryTreeImpl<N>) leaf);
//					if(logger.isInfoEnabled()){
//						logger.info("Removal of the edge leads to cover a negative tree. Undoing removal.");
//					}
//				}
//			}
//		}
		RDFResourceTree parent;
		for(RDFResourceTree candidate : candidates2Remove){
			if(candidate.isRoot())continue;
			parent = candidate.getParent();
			parent.removeChild(candidate);
			if(logger.isInfoEnabled()){
				logger.info("Checking if removal leads to coverage of a negative tree...");
			}
			if(coversNegativeTree(nbr, negExampleTrees)){
				parent.addChild(candidate);
				if(logger.isInfoEnabled()){
					logger.info("Removal of the edge leads to coverage of a negative tree. Undoing removal.");
				}
			}
		}
		
//		removeLeafs(nbr, candidates2Remove);
		removeEqualEdgesFromRoot(nbr);
		
		mon.stop();
		
		return nbr;
	}
	
	private void generaliseWeak(){
		
	}
	
	private void generaliseStrong(){
		
	}
	
	private boolean coversNegativeTree(RDFResourceTree posTree, List<RDFResourceTree> negTrees){
		for(RDFResourceTree negTree : negTrees){
			if(QueryTreeUtils.isSubsumedBy(negTree, posTree)){
				return true;
			}
		}
		return false;
	}
	
	private void removeLeafs(RDFResourceTree nbr, List<RDFResourceTree> candidates2Remove){
		for(RDFResourceTree leaf : new ArrayList<>(nbr.getLeafs())){
			if(candidates2Remove.contains(leaf)){
				logger.info("REMOVE " + leaf);
				leaf.getParent().removeChild(leaf);
			}
		}
	}
	
	private void removeEqualEdgesFromRoot(RDFResourceTree tree){
		List<RDFResourceTree> children;
		int childCount = 1;
		for(Node edge : tree.getEdges()){
			children = tree.getChildren(edge);
			childCount = children.size();
			while(childCount > maxEqualEdgesFromRoot){
				tree.removeChild(children.get(childCount-1));
				childCount--;
			}
		}
		
	}
	
	
	private String printTreeWithValues(RDFResourceTree tree, Map<RDFResourceTree, List<Integer>> matrix){
		int depth = QueryTreeUtils.getDepth(tree);
        StringBuilder sb = new StringBuilder();
        if(tree.isRoot()){
        	sb.append("TREE\n\n");
        }
//        ren = ren.replace("\n", "\n" + sb);
        sb.append(tree.getData()).append("(").append(matrix.get(tree)).append(")");
        sb.append("\n");
        for (RDFResourceTree child : tree.getChildren()) {
            for (int i = 0; i < depth; i++) {
                sb.append("\t");
            }
            Node edge = tree.getEdgeToChild(child);
            if (edge != null) {
            	sb.append("  ");
            	sb.append(edge);
            	sb.append(" ---> ");
            }
            sb.append(printTreeWithValues(child, matrix));
        }
        return sb.toString();
	}
	
	private int sum(List<Integer> list){
		int sum = 0;
		for(Integer i : list){
			sum += i;
		}
		return sum;
	}

	@Override
	public List<RDFResourceTree> computeNBRs(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees) {
		return Collections.singletonList(computeNBR(posExampleTree, negExampleTrees));
	}
	
//	private void checkTree(Map<RDFResourceTree, List<Integer>> matrix, RDFResourceTree posTree, RDFResourceTree negTree, int index){
//		int entry;
//		if(!posTree.getUserObject().equals("?") && !posTree.getUserObject().equals(negTree.getUserObject())){
//			entry = 1;
//		} else {
//			entry = 1;
//			for(Object edge : posTree.getEdges()){
//				for(RDFResourceTree child1 : posTree.getChildren(edge)){
//					for(RDFResourceTree child2 : negTree.getChildren(edge)){
//						if(!posTree.getUserObject().equals("?") && child1.getUserObject().equals(child2.getUserObject())){
//							entry = 0;break;
//						}
//						if(posTree.getUserObject().equals("?")){
//							checkTree(matrix, child1, child2, index);
//						}
//					}
//				}
//			}
//			Object edge;
//			for(RDFResourceTree child1 : posTree.getChildren()){
//	    		edge = posTree.getEdge(child1);
//	    		for(RDFResourceTree child2 : negTree.getChildren(edge)){
//	    			
//	    		}
//	    		
//	    	}
//		}
//		setMatrixEntry(matrix, posTree, index, entry);
//		if(entry == 1){
//			for(RDFResourceTree child : posTree.getChildrenClosure()){
//				setMatrixEntry(matrix, child, index, 0);
//			}
//		}
//	}
	
	private void checkTree(Map<RDFResourceTree, List<Integer>> matrix, RDFResourceTree posTree, RDFResourceTree negTree, int index){
		int entry = 1;
		for(RDFResourceTree child1 : posTree.getChildren()){
			entry = 1;
    		Node edge = posTree.getEdgeToChild(child1);
    		for(RDFResourceTree child2 : negTree.getChildren(edge)){
    			if(!child1.isVarNode() && child1.getData().equals(child2.getData())){
    				entry = 0;
    				checkTree(matrix, child1, child2, index);
    			} else if(child1.isVarNode()){
    				entry = 0;
    				checkTree(matrix, child1, child2, index);
    			}
    		}
    		setMatrixEntry(matrix, child1, index, entry);
    		if(entry == 1){
    			for(RDFResourceTree child : QueryTreeUtils.getNodes(posTree)) {
    				setMatrixEntry(matrix, child, index, 0);
    			}
    		}
		}
		
	}
	
	private void setMatrixEntry(Map<RDFResourceTree, List<Integer>> matrix, RDFResourceTree row, int column, int entry){
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

//	@Override
//	public RDFResourceTree computeNBR(RDFResourceTree posExampleTree,
//			List<RDFResourceTree> negExampleTrees) {
//		Map<RDFResourceTree, Integer> map = new HashMap<RDFResourceTree, Integer>();
//		for(RDFResourceTree negExampleTree : negExampleTrees){
//			checkSubsumptionBreadthFirst(posExampleTree, negExampleTree, map);
//		}
//		
//		Map<RDFResourceTree, Integer> sortedMap = sortByValues(map);
//		System.out.println(sortedMap);
//		return null;
//	}
//
//	@Override
//	public List<RDFResourceTree> computeNBRs(RDFResourceTree posExampleTree,
//			List<RDFResourceTree> negExampleTrees) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	private void checkSubsumptionBreadthFirst(RDFResourceTree tree1, RDFResourceTree tree2, Map<RDFResourceTree, Integer> map){
//		Object edge;
//		for(RDFResourceTree child1 : tree1.getChildren()){
//			edge = tree1.getEdge(child1);
//			for(RDFResourceTree child2 : tree2.getChildren(edge)){
//				if(child1.getUserObject().equals("?") || child2.getUserObject().equals(child1.getUserObject())){
//					Integer i = map.get(child1);
//					if(i == null){
//						i = Integer.valueOf(0);
//					}
//					map.put(child1, Integer.valueOf(i.intValue() +1));
//					checkSubsumptionBreadthFirst(child1, child2, map);
//				} else {
//					Integer i = map.get(child1);
//					if(i == null){
//						map.put(child1, Integer.valueOf(0));
//					}
//					
//				}
//			}
//		}
//	}
//	
//	private <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
//		Comparator<K> valueComparator =  new Comparator<K>() {
//		    public int compare(K k1, K k2) {
//		        int compare = map.get(k2).compareTo(map.get(k1));
//		        if (compare == 0) return 1;
//		        else return compare;
//		    }
//		};
//		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
//		sortedByValues.putAll(map);
//		return sortedByValues;
//	}
//	
//	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
//			Map<K, V> map) {
//		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
//				map.entrySet());
//		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
//			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
//				return (o1.getValue()).compareTo(o2.getValue());
//			}
//		});
//
//		Map<K, V> result = new LinkedHashMap<K, V>();
//		for (Map.Entry<K, V> entry : list) {
//			result.put(entry.getKey(), entry.getValue());
//		}
//		return result;
//	}
	
	

}
