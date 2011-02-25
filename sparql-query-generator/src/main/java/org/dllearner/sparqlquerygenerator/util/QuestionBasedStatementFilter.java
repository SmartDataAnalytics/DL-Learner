package org.dllearner.sparqlquerygenerator.util;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.Statement;

public class QuestionBasedStatementFilter implements Selector {
	
	private String question;
	private AbstractStringMetric metric;
	private double threshold = 0.7;
	
	public QuestionBasedStatementFilter(String question){
		this.question = question;
		metric = new QGramsDistance();
		
	}

	@Override
	public boolean test(Statement s) {
		String predicate = s.getPredicate().getURI().substring(s.getPredicate().getURI().lastIndexOf("/"));
		String object;
		if(s.getObject().isURIResource()){
			object = s.getObject().asResource().getURI();
			object = object.substring(object.lastIndexOf("/"));
		} else if(s.getObject().isLiteral()){
			object = s.getObject().asLiteral().getLexicalForm();
		}
		
		return false;
	}
	
	private boolean areSimiliar(String s1, String s2){
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
