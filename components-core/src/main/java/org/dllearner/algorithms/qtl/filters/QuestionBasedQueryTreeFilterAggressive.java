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
package org.dllearner.algorithms.qtl.filters;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

public class QuestionBasedQueryTreeFilterAggressive implements QueryTreeFilter{
	
private Set<String> questionWords;
	
	private AbstractStringMetric qGramMetric;
	private AbstractStringMetric levensteinMetric;
	private I_Sub substringMetric;
	
	private double threshold = 0.4;
	private int topK = 3;
	private double topKSumThreshold = 0.8;
	
	private Set<Integer> numbers = new HashSet<>();
	
	public QuestionBasedQueryTreeFilterAggressive(Set<String> questionWords){
		this.questionWords = questionWords;
		qGramMetric = new QGramsDistance();
		levensteinMetric = new Levenshtein();
		substringMetric = new I_Sub();
		extractNumbers();
		
	}
	
	@Override
	public QueryTree<String> getFilteredQueryTree(QueryTree<String> tree){
		if(tree.getChildren().isEmpty()){
			return tree;
		}
		QueryTree<String> copy = new QueryTreeImpl<>(tree);
		filterTree(copy);
		return copy;
	}
	
	public void setThreshold(double threshold){
		this.threshold = threshold;
	}
	
	private void filterTree(QueryTree<String> tree){
		List<QueryTree<String>> leafs = tree.getLeafs();
		QueryTree<String> parent = leafs.get(0).getParent();
		String edge = (String) parent.getEdge(leafs.get(0));
		String label;
		for(QueryTree<String> leaf : leafs){
			if(!leaf.getParent().getEdge(leaf).equals(edge) || leaf.getParent()!= parent){
				removeUnnecessaryEdges(parent, edge);
				parent = leaf.getParent();
				edge = (String) parent.getEdge(leaf);
			}
			label = leaf.getUserObject();
			edge = (String) leaf.getParent().getEdge(leaf);
			boolean replace = false;
			if(leaf.isLiteralNode()){
				replace = !literalIsSimiliar2QuestionWord(label);
			} else {
				replace = !resourceIsSimilar2QuestionWord(label);
			}
			if(replace){
				leaf.setUserObject("?");
			}
			
		}
	}
	
	private void removeUnnecessaryEdges(QueryTree<String> node, String edge){
		List<QueryTree<String>> children = node.getChildren(edge);
		if(children.size() >= 2){
			int removed = 0;
			for(QueryTree<String> child : children){
				if(child.getUserObject().equals("?") && removed < children.size()){
					node.removeChild((QueryTreeImpl<String>) child);
					removed++;
				}
			}
		}
		
	}
	
	private boolean resourceIsSimilar2QuestionWord(String resource){
		String label = getFragment(resource);
		for(String word : questionWords){
			if(areSimiliar(word, label)){
				return true;
			}
		} 
		return isSimlarWithSubstringMetrik(label);
	}
	
	private boolean literalIsSimiliar2QuestionWord(String literal){
		String value = extractLiteralValue(literal);
		if(isNumber(value)){
			if(numbers.isEmpty()){
				return false;
			} else {
				int i = Integer.parseInt(value);
				return numbers.contains(i);
			}
		}
		for(String word : questionWords){
			if(areSimiliar(word, value)){
				return true;
			}
		} 
		return isSimlarWithSubstringMetrik(value);
	}
	
	private String extractLiteralValue(String literal){
		String value = literal;
		int index = literal.indexOf("^^");
		if(index != -1){
			value = literal.substring(1, index-1);
		} else {
			index = literal.indexOf("@");
			if(index != -1){
				value = literal.substring(1, index-1);
			}
		}
		return value;
		
	}
	
	private void extractNumbers(){
		for(String word : questionWords){
			if(isNumber(word)){
				numbers.add(Integer.valueOf(word));
			}
		}
	}
	
	private boolean isNumber(String s){
		for (int i = 0; i < s.length(); i++) {
			if(!Character.isDigit(s.charAt(i))){
				return false;
			}
		}
		return true;
		     
	}
	
	private boolean areSimiliar(String s1, String s2){
		return (qGramMetric.getSimilarity(s1, s2) >= threshold) || 
		(levensteinMetric.getSimilarity(s1, s2) >= threshold);
	}
	
	private boolean isSimlarWithSubstringMetrik(String s){
		SortedSet<Double> values = new TreeSet<>(Collections.reverseOrder());
		for(String word : questionWords){
			double v = substringMetric.score(word, s, true);
			if(v >= threshold){
				return true;
			} else {
				values.add(v);
			}
		} 
		double sum = 0;
		for(Double v : getTopK(values)){
			if(v >= 0){
				sum += v;
			}
			
		}
		return sum >= topKSumThreshold;
	}
	
	private Set<Double> getTopK(SortedSet<Double> values){
		Set<Double> top = new HashSet<>();
		int k = 0;
		for(Double v : values){
			if(k == topK){
				break;
			}
			top.add(v);
			k++;
		}
		return top;
	}
	
	private String getFragment(String uri){
		int i = uri.lastIndexOf("#");
		if(i > 0){
			return uri.substring(i+1);
		} else {
			return uri.substring(uri.lastIndexOf("/")+1);
		}
	}
	

}
