/**
 * 
 */
package org.dllearner.algorithms.properties;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.pagination.core.PaginationUtils;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
		GET_SAMPLE_QUERY.setIri("p", propertyToDescribe.toStringID());
		
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
			existingAxioms.add(getAxiom(propertyToDescribe));
			logger.info("Property is already declared as asymmetric in knowledge base.");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyAxiomLearner#run()
	 */
	@Override
	protected void run() {
//		runSPARQL1_0_Mode();
		runSPARQL1_1_Mode();
	}
	
	protected abstract T getAxiom(OWLObjectProperty property);
	
	private void runSPARQL1_0_Mode() {
		boolean declared = !existingAxioms.isEmpty();
		
		workingModel = ModelFactory.createDefaultModel();
		
		//TODO determine page size in super class or even better in the KB object
		int DEFAULT_PAGE_SIZE = 10000;
		long limit = DEFAULT_PAGE_SIZE; //PaginationUtils.adjustPageSize(qef, DEFAULT_PAGE_SIZE);
		long offset = 0;
		
		Query query = GET_SAMPLE_QUERY.asQuery();
		query.setLimit(limit);
		Model newModel = executeConstructQuery(query.toString());
		
		while (!terminationCriteriaSatisfied() && newModel.size() != 0) {
			workingModel.add(newModel);
			
			popularity = getPropertyPopularity(workingModel);
			
			// get number of pos examples
			int frequency = getPositiveExamplesFrequency(workingModel);

			if (popularity > 0) {
				currentlyBestAxioms.clear();
				currentlyBestAxioms.add(new EvaluatedAxiom<T>(
						getAxiom(propertyToDescribe), 
						computeScore(popularity, frequency, useSample),
						declared));
			}
			offset += limit;
			query.setOffset(offset);
			newModel = executeConstructQuery(query.toString());
		}
	}
	
	private void runSPARQL1_1_Mode() {
		boolean declared = !existingAxioms.isEmpty();
		
		int frequency = getPositiveExamplesFrequency();

		currentlyBestAxioms.add(new EvaluatedAxiom<T>(
				getAxiom(propertyToDescribe), 
				computeScore(popularity, frequency, false),
				declared));
	}
	
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

		List<String> vars = rs.getResultVars();
		boolean onlySubject = vars.size() == 1;
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource(onlySubject ? "s" : "o").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return negExamples;
	}

}
