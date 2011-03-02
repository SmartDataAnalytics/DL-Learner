package org.dllearner.sparqlquerygenerator.util;

import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

public class QuestionBasedQueryTreeFilter {
	
private Set<String> questionWords;
	
	private AbstractStringMetric qGramMetric;
	private AbstractStringMetric levensteinMetric;
	private AbstractStringMetric jaroWinklerMetric;
	
	private double threshold = 0.4;
	
	public QuestionBasedQueryTreeFilter(Set<String> questionWords){
		this.questionWords = questionWords;
		qGramMetric = new QGramsDistance();
		levensteinMetric = new Levenshtein();
		jaroWinklerMetric = new JaroWinkler();
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
		return false;
	}
	
	private String getFragment(String uri){
		int i = uri.lastIndexOf("#");
		if(i > 0){
			return uri.substring(i+1);
		} else {
			return uri.substring(uri.lastIndexOf("/")+1);
		}
	}
	
	private boolean areSimiliar(String s1, String s2){//cnt++;System.out.println(cnt);
		if(s1.toLowerCase().contains(s2.toLowerCase()) || s2.toLowerCase().contains(s1.toLowerCase())){
			return true;
		}
		float qSim = qGramMetric.getSimilarity(s1, s2);
		float lSim = levensteinMetric.getSimilarity(s1, s2);
//		float jSim = jaroWinklerMetric.getSimilarity(s1, s2);
		float sim = Math.max(qSim, lSim);
//		sim = Math.max(sim, jSim);
		return sim >= threshold;
	}

}
