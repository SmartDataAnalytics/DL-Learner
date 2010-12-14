/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
 *
 */
package org.dllearner.sparqlquerygenerator.operations.nbr.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class GreedyNBRStrategy<N> implements NBRStrategy<N>{
	
	private static final Logger logger = Logger.getLogger(GreedyNBRStrategy.class);
	
	private int maxEqualEdgesFromRoot = 3;

	@Override
	public QueryTree<N> computeNBR(QueryTree<N> posExampleTree,
			List<QueryTree<N>> negExampleTrees) {
		Monitor mon = MonitorFactory.getTimeMonitor("NBR");
		mon.start();
		
		QueryTree<N> nbr = new QueryTreeImpl<N>(posExampleTree);
		Map<QueryTree<N>, List<Integer>> matrix = new HashMap<QueryTree<N>, List<Integer>>();
		
		for(int i = 0; i < negExampleTrees.size(); i++){
			checkTree(matrix, nbr, negExampleTrees.get(i), i);
		}
		
		if(logger.isInfoEnabled()){
			logger.info(printTreeWithValues(nbr, matrix));
		}
		
		List<QueryTree<N>> candidates2Remove = new ArrayList<QueryTree<N>>();
		for(Entry<QueryTree<N>, List<Integer>> entry : matrix.entrySet()){
			if(sum(entry.getValue()) < negExampleTrees.size()/2.0){
				candidates2Remove.add(entry.getKey());
			}
		}
		removeLeafs(nbr, candidates2Remove);
		removeEqualEdgesFromRoot(nbr);
		
		mon.stop();
		
		return nbr;
	}
	
	private void removeLeafs(QueryTree<N> nbr, List<QueryTree<N>> candidates2Remove){
		for(QueryTree<N> leaf : new ArrayList<QueryTree<N>>(nbr.getLeafs())){
			if(candidates2Remove.contains(leaf)){
				logger.info("REMOVE " + leaf);
				leaf.getParent().removeChild((QueryTreeImpl<N>) leaf);
			}
		}
	}
	
	private void removeEqualEdgesFromRoot(QueryTree<N> tree){
		List<QueryTree<N>> children;
		int childCount = 1;
		for(Object edge : tree.getEdges()){
			children = tree.getChildren(edge);
			childCount = children.size();
			while(childCount > maxEqualEdgesFromRoot){
				tree.removeChild((QueryTreeImpl<N>) children.get(childCount-1));
				childCount--;
			}
		}
		
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
	
	private int sum(List<Integer> list){
		int sum = 0;
		for(Integer i : list){
			sum += i;
		}
		return sum;
	}

	@Override
	public List<QueryTree<N>> computeNBRs(QueryTree<N> posExampleTree,
			List<QueryTree<N>> negExampleTrees) {
		return Collections.singletonList(computeNBR(posExampleTree, negExampleTrees));
	}
	
//	private void checkTree(Map<QueryTree<N>, List<Integer>> matrix, QueryTree<N> posTree, QueryTree<N> negTree, int index){
//		int entry;
//		if(!posTree.getUserObject().equals("?") && !posTree.getUserObject().equals(negTree.getUserObject())){
//			entry = 1;
//		} else {
//			entry = 1;
//			for(Object edge : posTree.getEdges()){
//				for(QueryTree<N> child1 : posTree.getChildren(edge)){
//					for(QueryTree<N> child2 : negTree.getChildren(edge)){
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
//			for(QueryTree<N> child1 : posTree.getChildren()){
//	    		edge = posTree.getEdge(child1);
//	    		for(QueryTree<N> child2 : negTree.getChildren(edge)){
//	    			
//	    		}
//	    		
//	    	}
//		}
//		setMatrixEntry(matrix, posTree, index, entry);
//		if(entry == 1){
//			for(QueryTree<N> child : posTree.getChildrenClosure()){
//				setMatrixEntry(matrix, child, index, 0);
//			}
//		}
//	}
	
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
    			for(QueryTree<N> child : posTree.getChildrenClosure()){
    				setMatrixEntry(matrix, child, index, 0);
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

//	@Override
//	public QueryTree<N> computeNBR(QueryTree<N> posExampleTree,
//			List<QueryTree<N>> negExampleTrees) {
//		Map<QueryTree<N>, Integer> map = new HashMap<QueryTree<N>, Integer>();
//		for(QueryTree<N> negExampleTree : negExampleTrees){
//			checkSubsumptionBreadthFirst(posExampleTree, negExampleTree, map);
//		}
//		
//		Map<QueryTree<N>, Integer> sortedMap = sortByValues(map);
//		System.out.println(sortedMap);
//		return null;
//	}
//
//	@Override
//	public List<QueryTree<N>> computeNBRs(QueryTree<N> posExampleTree,
//			List<QueryTree<N>> negExampleTrees) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	private void checkSubsumptionBreadthFirst(QueryTree<N> tree1, QueryTree<N> tree2, Map<QueryTree<N>, Integer> map){
//		Object edge;
//		for(QueryTree<N> child1 : tree1.getChildren()){
//			edge = tree1.getEdge(child1);
//			for(QueryTree<N> child2 : tree2.getChildren(edge)){
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
