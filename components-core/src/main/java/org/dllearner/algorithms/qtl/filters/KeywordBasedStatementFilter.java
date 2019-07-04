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

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class KeywordBasedStatementFilter implements Predicate<Statement> {
	
	private Set<String> questionWords;
	
	private AbstractStringMetric qGramMetric;
	private AbstractStringMetric levensteinMetric;
	private AbstractStringMetric jaroWinklerMetric;
	private I_Sub substringMetric;
	
	private double threshold = 0.4;
	
	private int topK = 3;
	private double topKSumThreshold = 0.8;
	
	private Map<Statement, Double> statement2Similarity = new HashMap<>();
	
	private Map<RDFNode, Boolean> cache = new HashMap<>();
	
	int cnt = 0;
	
	public KeywordBasedStatementFilter(Set<String> questionWords){
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
		return isSimlarWithSubstringMetrik(s);
	}
	
	private boolean areSimiliar(String s1, String s2, Statement st){
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

	@Override
	public boolean test(Statement s) {
		Boolean similarPredicate = cache.get(s.getPredicate());
		Boolean similarObject = cache.get(s.getObject());
		if(similarPredicate != null && similarObject != null){
			return similarPredicate || similarObject;
		} else if(similarPredicate == null && similarObject != null){
			if(similarObject){
				return true;
			} else {
				String predicate = s.getPredicate().getURI().substring(s.getPredicate().getURI().lastIndexOf("/"));
				if (isSimiliar2QuestionWord(predicate, s)){
					cache.put(s.getPredicate(), true);
					return true;
				} else {
					cache.put(s.getPredicate(), false);
					return false;
				}
			}
		} else if(similarPredicate != null && similarObject == null){
			if(similarPredicate){
				return true;
			} else {
				String object = null;
				if(s.getObject().isURIResource()){
					object = s.getObject().asResource().getURI();
					object = getFragment(s.getObject().asResource().getURI());
				} else if(s.getObject().isLiteral()){
					object = s.getObject().asLiteral().getLexicalForm();
				}
				if(isSimiliar2QuestionWord(object, s)){
					cache.put(s.getObject(), true);
					return true;
				} else {
					cache.put(s.getObject(), false);
					return false;
				}
			}
		} else {
			String predicate = s.getPredicate().getURI().substring(s.getPredicate().getURI().lastIndexOf("/"));
			if (isSimiliar2QuestionWord(predicate, s)){
				cache.put(s.getPredicate(), true);
				return true;
			} else {
				cache.put(s.getPredicate(), false);
			}
			String object = null;
			if(s.getObject().isURIResource()){
				object = s.getObject().asResource().getURI();
				object = getFragment(s.getObject().asResource().getURI());
			} else if(s.getObject().isLiteral()){
				object = s.getObject().asLiteral().getLexicalForm();
			}
			if(isSimiliar2QuestionWord(object, s)){
				cache.put(s.getObject(), true);
				return true;
			} else {
				cache.put(s.getObject(), false);
			}
			return false;
		}
	}
	
//	@Override
//	public boolean accept(Statement s) {
//		String predicate = s.getPredicate().getURI().substring(s.getPredicate().getURI().lastIndexOf("/"));
//		String object = null;
//		if(s.getObject().isURIResource()){
//			object = s.getObject().asResource().getURI();
//			object = getFragment(s.getObject().asResource().getURI());
//		} else if(s.getObject().isLiteral()){
//			object = s.getObject().asLiteral().getLexicalForm();
//		}
//		return isSimiliar2QuestionWord(predicate, s) || isSimiliar2QuestionWord(object, s);
//	}
	
	public void setThreshold(double threshold){
		this.threshold = threshold;
	}
	
	public double getThreshold(){
		return threshold;
	}
	
	public Set<Statement> getStatementsBelowThreshold(double threshold){
		Set<Statement> statements = new HashSet<>();
		for(Entry<Statement, Double> entry : statement2Similarity.entrySet()){
			if(entry.getValue() < threshold){
				statements.add(entry.getKey());
			}
		}
		return statements;
	}

}
