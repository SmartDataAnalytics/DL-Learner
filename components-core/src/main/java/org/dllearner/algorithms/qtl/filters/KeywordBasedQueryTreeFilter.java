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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

public class KeywordBasedQueryTreeFilter implements QueryTreeFilter{
	
	private Collection<String> questionWords;
	
	private AbstractStringMetric qGramMetric;
	private AbstractStringMetric levensteinMetric;
	private I_Sub substringMetric;
	
	private double threshold = 0.4;
	private int topK = 3;
	private double topKSumThreshold = 0.8;
	
	public KeywordBasedQueryTreeFilter(Collection<String> questionWords){
		this.questionWords = questionWords;
		qGramMetric = new QGramsDistance();
		levensteinMetric = new Levenshtein();
		substringMetric = new I_Sub();
	}
	
	@Override
	public QueryTree<String> getFilteredQueryTree(QueryTree<String> tree){
		QueryTree<String> copy = new QueryTreeImpl<>(tree);
		filterTree(copy);
		return copy;
	}
	
	public Collection<String> getQuestionWords(){
		return questionWords;
	}
	
	public void setThreshold(double threshold){
		this.threshold = threshold;
	}
	
	private void filterTree(QueryTree<String> tree){
		String edge;
		for(QueryTree<String> child : tree.getChildren()){
			if(child.getUserObject().equals("?")){
				edge = (String) tree.getEdge(child);
				if(!isSimiliar2QuestionWord(getFragment(edge))){
					child.getParent().removeChild((QueryTreeImpl<String>) child);
				}
			} else {
				filterTree(child);
			}
		}
	}
	
	private boolean isSimiliar2QuestionWord(String s){
		for(String word : questionWords){
			if(areSimiliar(word, s)){
				return true;
			}
		} 
		return isSimlarWithSubstringMetrik(s);
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
