package org.dllearner.sparqlquerygenerator.util;

import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

public class QuestionBasedStatementFilter extends Filter<Statement> {
	
	private Set<String> questionWords;
	private AbstractStringMetric metric;
	private double threshold = 0.5;
	
	int cnt = 0;
	
	public QuestionBasedStatementFilter(Set<String> questionWords){
		this.questionWords = questionWords;
		metric = new QGramsDistance();
		
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
		float sim = metric.getSimilarity(s1, s2);
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

}
