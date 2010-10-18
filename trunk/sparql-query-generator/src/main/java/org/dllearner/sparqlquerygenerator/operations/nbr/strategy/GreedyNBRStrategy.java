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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class GreedyNBRStrategy<N> implements NBRStrategy<N>{

	@Override
	public QueryTree<N> computeNBR(QueryTree<N> posExampleTree,
			Set<QueryTree<N>> negExampleTrees) {
		Map<QueryTree<N>, Integer> map = new HashMap<QueryTree<N>, Integer>();
		for(QueryTree<N> negExampleTree : negExampleTrees){
			checkSubsumptionBreadthFirst(posExampleTree, negExampleTree, map);
		}
		
		Map<QueryTree<N>, Integer> sortedMap = sortByValues(map);
		System.out.println(sortedMap);
		return null;
	}

	@Override
	public List<QueryTree<N>> computeNBRs(QueryTree<N> posExampleTree,
			Set<QueryTree<N>> negExampleTrees) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void checkSubsumptionBreadthFirst(QueryTree<N> tree1, QueryTree<N> tree2, Map<QueryTree<N>, Integer> map){
		Object edge;
		for(QueryTree<N> child1 : tree1.getChildren()){
			edge = tree1.getEdge(child1);
			for(QueryTree<N> child2 : tree2.getChildren(edge)){
				if(child1.getUserObject().equals("?") || child2.getUserObject().equals(child1.getUserObject())){
					Integer i = map.get(child1);
					if(i == null){
						i = Integer.valueOf(0);
					}
					map.put(child1, Integer.valueOf(i.intValue() +1));
					checkSubsumptionBreadthFirst(child1, child2, map);
				} else {
					Integer i = map.get(child1);
					if(i == null){
						map.put(child1, Integer.valueOf(0));
					}
					
				}
			}
		}
	}
	
	private <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
		Comparator<K> valueComparator =  new Comparator<K>() {
		    public int compare(K k1, K k2) {
		        int compare = map.get(k2).compareTo(map.get(k1));
		        if (compare == 0) return 1;
		        else return compare;
		    }
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
