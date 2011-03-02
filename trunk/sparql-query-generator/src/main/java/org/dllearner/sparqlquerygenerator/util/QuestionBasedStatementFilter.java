package org.dllearner.sparqlquerygenerator.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
	private I_Sub substringMetric;
	
	private double threshold = 0.4;
	
	private Map<Statement, Double> statement2Similarity = new HashMap<Statement, Double>();
	
	int cnt = 0;
	
	public QuestionBasedStatementFilter(Set<String> questionWords){
		this.questionWords = questionWords;
		qGramMetric = new QGramsDistance();
		levensteinMetric = new Levenshtein();
		jaroWinklerMetric = new JaroWinkler();
		substringMetric = new I_Sub();
		
	}

	private boolean isSimiliar2QuestionWord(String s, Statement st){
		for(String word : questionWords){
			if(areSimiliar(word, s, st)){
				return true;
			}
		}
		return false;
	}
	
	private boolean areSimiliar(String s1, String s2, Statement st){//cnt++;System.out.println(cnt);
		float qSim = qGramMetric.getSimilarity(s1, s2);
		float lSim = levensteinMetric.getSimilarity(s1, s2);
//		float jSim = jaroWinklerMetric.getSimilarity(s1, s2);
		double subSim = substringMetric.score(s1, s2, true);
		float sim = Math.max(qSim, lSim);
		sim = Math.max(sim, Double.valueOf(subSim).floatValue());
		
//		sim = Math.max(sim, jSim);
		if(sim >= threshold){
			statement2Similarity.put(st, Double.valueOf(sim));
			return true;
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

	@Override
	public boolean accept(Statement s) {
		String predicate = s.getPredicate().getURI().substring(s.getPredicate().getURI().lastIndexOf("/"));
		String object = null;
		if(s.getObject().isURIResource()){
			object = s.getObject().asResource().getURI();
			object = getFragment(s.getObject().asResource().getURI());
		} else if(s.getObject().isLiteral()){
			object = s.getObject().asLiteral().getLexicalForm();
		}
		if(isSimiliar2QuestionWord(object, s) || isSimiliar2QuestionWord(predicate, s)){
			return true;
		}
		
		return false;
	}
	
	public void setThreshold(double threshold){
		this.threshold = threshold;
	}
	
	public double getThreshold(){
		return threshold;
	}
	
	public Set<Statement> getStatementsBelowThreshold(double threshold){
		Set<Statement> statements = new HashSet<Statement>();
		for(Entry<Statement, Double> entry : statement2Similarity.entrySet()){
			if(entry.getValue().doubleValue() < threshold){
				statements.add(entry.getKey());
			}
		}
		return statements;
	}

}
