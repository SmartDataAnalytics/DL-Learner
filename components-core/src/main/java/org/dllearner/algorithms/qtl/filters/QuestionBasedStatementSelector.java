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

import java.util.Set;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.Statement;

public class QuestionBasedStatementSelector implements Selector {
	
	private Set<String> questionWords;
	private AbstractStringMetric metric;
	private double threshold = 0.5;
	int cnt = 0;
	
	public QuestionBasedStatementSelector(Set<String> questionWords){
		this.questionWords = questionWords;
		metric = new QGramsDistance();
		
	}

	@Override
	public boolean test(Statement s) {
		String predicate = s.getPredicate().getURI().substring(s.getPredicate().getURI().lastIndexOf("/"));
		String object = null;
		if(s.getObject().isURIResource()){
			object = s.getObject().asResource().getURI();
			object = object.substring(object.lastIndexOf("/")+1);
		} else if(s.getObject().isLiteral()){
			object = s.getObject().asLiteral().getLexicalForm();
		}
		return isSimiliar2QuestionWord(object) || isSimiliar2QuestionWord(predicate);

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
	public boolean isSimple() {
		return false;
	}

	@Override
	public Resource getSubject() {
		return null;
	}

	@Override
	public Property getPredicate() {
		return null;
	}

	@Override
	public RDFNode getObject() {
		return null;
	}

}
