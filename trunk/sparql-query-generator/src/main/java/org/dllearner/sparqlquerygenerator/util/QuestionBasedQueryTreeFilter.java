package org.dllearner.sparqlquerygenerator.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import com.hp.hpl.jena.rdf.model.Statement;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

public class QuestionBasedQueryTreeFilter {
	
private Set<String> questionWords;
	
	private AbstractStringMetric qGramMetric;
	private AbstractStringMetric levensteinMetric;
	private I_Sub substringMetric;
	
	private double threshold = 0.4;
	private int topK = 3;
	private double topKSumThreshold = 0.8;
	
	public QuestionBasedQueryTreeFilter(Set<String> questionWords){
		this.questionWords = questionWords;
		qGramMetric = new QGramsDistance();
		levensteinMetric = new Levenshtein();
		substringMetric = new I_Sub();
	}
	
	public QueryTree<String> getFilteredQueryTree(QueryTree<String> tree){
		QueryTree<String> copy = new QueryTreeImpl<String>(tree);
		filterTree(copy);
		return copy;
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
		SortedSet<Double> values = new TreeSet<Double>(Collections.reverseOrder());
		for(String word : questionWords){
			double v = substringMetric.score(word, s, true);
			if(v >= threshold){
				return true;
			} else {
				values.add(Double.valueOf(v));
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
		Set<Double> top = new HashSet<Double>();
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
