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
package org.dllearner.algorithms.properties;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

/**
 * A learning algorithm for object property characteristic axioms. 
 * @author Lorenz Buehmann
 *
 */
public abstract class ObjectPropertyCharacteristicsAxiomLearner<T extends OWLObjectPropertyCharacteristicAxiom> extends ObjectPropertyAxiomLearner<T>{

	protected final ParameterizedSparqlString ALREADY_DECLARED_QUERY = new ParameterizedSparqlString("ASK {?p a ?type .}");
	
	protected ParameterizedSparqlString POS_FREQUENCY_QUERY = null;
	
	protected boolean declared;
	
	public ObjectPropertyCharacteristicsAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyAxiomLearner#setPropertyToDescribe(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public void setEntityToDescribe(OWLObjectProperty entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		POS_FREQUENCY_QUERY.setIri("p", entityToDescribe.toStringID());
		ALREADY_DECLARED_QUERY.setIri("p", entityToDescribe.toStringID());
		
		IRI type;
		if(axiomType.equals(AxiomType.SYMMETRIC_OBJECT_PROPERTY)){
			type = OWLRDFVocabulary.OWL_SYMMETRIC_PROPERTY.getIRI();
		} else if(axiomType.equals(AxiomType.ASYMMETRIC_OBJECT_PROPERTY)){
			type = OWLRDFVocabulary.OWL_ASYMMETRIC_PROPERTY.getIRI();
		} else if(axiomType.equals(AxiomType.FUNCTIONAL_OBJECT_PROPERTY)){
			type = OWLRDFVocabulary.OWL_FUNCTIONAL_PROPERTY.getIRI();
		} else if(axiomType.equals(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY)){
			type = OWLRDFVocabulary.OWL_INVERSE_FUNCTIONAL_PROPERTY.getIRI();
		} else if(axiomType.equals(AxiomType.REFLEXIVE_OBJECT_PROPERTY)){
			type = OWLRDFVocabulary.OWL_REFLEXIVE_PROPERTY.getIRI();
		} else if(axiomType.equals(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY)){
			type = OWLRDFVocabulary.OWL_IRREFLEXIVE_PROPERTY.getIRI();
		} else if(axiomType.equals(AxiomType.TRANSITIVE_OBJECT_PROPERTY)){
			type = OWLRDFVocabulary.OWL_TRANSITIVE_PROPERTY.getIRI();
		}else {
			throw new IllegalArgumentException("Axiom type cannot be " + axiomType);
		}
		ALREADY_DECLARED_QUERY.setIri("type", type.toString()); 
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		// check if property is already declared as asymmetric in knowledge base
		declared = executeAskQuery(ALREADY_DECLARED_QUERY.toString());
		if (declared) {
			existingAxioms.add(getAxiom(entityToDescribe));
			logger.info("Property is already declared as asymmetric in knowledge base.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyAxiomLearner#run()
	 */
	@Override
	protected void run() {
		boolean declared = !existingAxioms.isEmpty();
		
		int frequency = getPositiveExamplesFrequency();

		currentlyBestAxioms.add(new EvaluatedAxiom<>(
				getAxiom(entityToDescribe),
				computeScore(popularity, frequency, useSampling),
				declared));
	}
	
	protected abstract T getAxiom(OWLObjectProperty property);
	
	protected int getPositiveExamplesFrequency(){
		return getCountValue(POS_FREQUENCY_QUERY.toString());
	}
	
	protected int getPositiveExamplesFrequency(Model model){
		return getCountValue(POS_FREQUENCY_QUERY.toString(), model);
	}
	
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<T> evAxiom) {
		T axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", axiom.getProperty().asOWLObjectProperty().toStringID());

		Set<OWLObjectPropertyAssertionAxiom> posExamples = new TreeSet<>();

		ResultSet rs = executeSelectQuery(posExamplesQueryTemplate.toString());

		List<String> vars = rs.getResultVars();
		boolean onlySubject = vars.size() == 1;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource(onlySubject ? "s" : "o").getURI()));
			posExamples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, subject, object));
		}

		return posExamples;
	}

	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<T> evaluatedAxiom) {
		T axiom = evaluatedAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", axiom.getProperty().asOWLObjectProperty().toStringID());

		Set<OWLObjectPropertyAssertionAxiom> negExamples = new TreeSet<>();

		ResultSet rs = executeSelectQuery(negExamplesQueryTemplate.toString());

		List<String> vars = rs.getResultVars();
		boolean onlySubject = vars.size() == 1;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource(onlySubject ? "s" : "o").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, subject, object));
		}

		return negExamples;
	}

}
