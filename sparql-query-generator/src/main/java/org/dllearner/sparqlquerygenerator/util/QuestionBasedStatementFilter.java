package org.dllearner.sparqlquerygenerator.util;

import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

public class QuestionBasedStatementFilter extends Filter<Statement> {
	
	private Set<String> questionWords;
	
	private AbstractStringMetric qGramMetric;
	private AbstractStringMetric levensteinMetric;
	private AbstractStringMetric jaroWinklerMetric;
	
	private double threshold = 0.3;
	
	int cnt = 0;
	
	public QuestionBasedStatementFilter(Set<String> questionWords){
		this.questionWords = questionWords;
		qGramMetric = new QGramsDistance();
		levensteinMetric = new Levenshtein();
		jaroWinklerMetric = new JaroWinkler();
		
	}

	private boolean isSimiliar2QuestionWord(String s){
		for(String word : questionWords){
			if(areSimiliar(word, s)){
				return true;
			}
		}
		return false;
	}
	
	private boolean areSimiliar(String s1, String s2){//cnt++;System.out.println(cnt);
		float qSim = qGramMetric.getSimilarity(s1, s2);
		float lSim = levensteinMetric.getSimilarity(s1, s2);
		float jSim = jaroWinklerMetric.getSimilarity(s1, s2);
		float sim = Math.max(Math.max(qSim, lSim), jSim);
		return sim >= threshold;
	}

	@Override
	public boolean accept(Statement s) {
		String predicate = s.getPredicate().getURI().substring(s.getPredicate().getURI().lastIndexOf("/"));
		String object = null;
		if(s.getObject().isURIResource()){
			object = s.getObject().asResource().getURI();
			object = object.substring(object.lastIndexOf("/")+1);
		} else if(s.getObject().isLiteral()){
			object = s.getObject().asLiteral().getLexicalForm();
		}
		if(isSimiliar2QuestionWord(object) || isSimiliar2QuestionWord(predicate)){
			return true;
		}
		
		return false;
	}
	
	public void setThreshold(double threshold){
		this.threshold = threshold;
	}

}
