/**
 * 
 */
package org.dllearner.algorithms.properties;

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

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.OWL2;

/**
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
	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		super.setPropertyToDescribe(propertyToDescribe);
		
		POS_FREQUENCY_QUERY.setIri("p", propertyToDescribe.toStringID());
		ALREADY_DECLARED_QUERY.setIri("p", propertyToDescribe.toStringID());
		
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
		} else {
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
			existingAxioms.add(getAxiom(propertyToDescribe));
			logger.info("Property is already declared as asymmetric in knowledge base.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyAxiomLearner#run()
	 */
	@Override
	protected void run() {
		runSPARQL1_1_Mode();
	}
	
	protected abstract T getAxiom(OWLObjectProperty property);
	
	private void runSPARQL1_0_Mode() {
		boolean declared = !existingAxioms.isEmpty();
		
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery = "CONSTRUCT {?s <%s> ?o.} WHERE {?s <%s> ?o} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(),
				limit, offset);
		Model newModel = executeConstructQuery(query);
		while (!terminationCriteriaSatisfied() && newModel.size() != 0) {
			workingModel.add(newModel);
			
			// get number of instances of s with <s p o>
//			query = "SELECT (COUNT(*) AS ?total) WHERE {?s <%s> ?o.}";
//			query = query.replace("%s", propertyToDescribe.toStringID());
//			ResultSet rs = executeSelectQuery(query, workingModel);
//			QuerySolution qs;
//			int total = 0;
//			while (rs.hasNext()) {
//				qs = rs.next();
//				total = qs.getLiteral("total").getInt();
//			}
			int total = (int) workingModel.size();
			
			// get number of pos examples
			ResultSet rs = executeSelectQuery(POS_FREQUENCY_QUERY.toString(), workingModel);
			int frequency = rs.next().getLiteral("cnt").getInt();

			if (total > 0) {
				currentlyBestAxioms.clear();
				currentlyBestAxioms.add(new EvaluatedAxiom<T>(
						getAxiom(propertyToDescribe), 
						computeScore(popularity, frequency),
						declared));
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit,
					offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	private void runSPARQL1_1_Mode() {
		boolean declared = !existingAxioms.isEmpty();
		
		ResultSet rs = executeSelectQuery(POS_FREQUENCY_QUERY.toString());
		int frequency = rs.next().getLiteral("cnt").getInt();

		currentlyBestAxioms.add(new EvaluatedAxiom<T>(
				getAxiom(propertyToDescribe), 
				computeScore(popularity, frequency),
				declared));
	}
	
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<T> evAxiom) {
		T axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", axiom.getProperty().asOWLObjectProperty().toStringID());

		Set<OWLObjectPropertyAssertionAxiom> posExamples = new TreeSet<OWLObjectPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString());
		}

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			posExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return posExamples;
	}

	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<T> evaluatedAxiom) {
		T axiom = evaluatedAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", axiom.getProperty().asOWLObjectProperty().toStringID());

		Set<OWLObjectPropertyAssertionAxiom> negExamples = new TreeSet<OWLObjectPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString());
		}

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return negExamples;
	}

}
