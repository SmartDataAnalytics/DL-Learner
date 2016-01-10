package org.dllearner.algorithms.properties;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.store.NRTCachingDirectory;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

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
