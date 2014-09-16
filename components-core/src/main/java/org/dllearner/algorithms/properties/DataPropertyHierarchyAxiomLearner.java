/**
 * 
 */
package org.dllearner.algorithms.properties;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNaryPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class DataPropertyHierarchyAxiomLearner<T extends OWLDataPropertyAxiom> extends DataPropertyAxiomLearner<T> {
	
	protected static final ParameterizedSparqlString PROPERTY_OVERLAP_QUERY = new ParameterizedSparqlString(
			"SELECT ?p_other (COUNT(*) AS ?overlap) WHERE {"
			+ "?s ?p ?o; ?p_other ?o . "
			+ "?p_other a <http://www.w3.org/2002/07/owl#DatatypeProperty> . FILTER(?p != ?p_other)}"
			+ " GROUP BY ?p_other");

	protected static final ParameterizedSparqlString PROPERTY_OVERLAP_WITH_RANGE_QUERY = new ParameterizedSparqlString(
			"SELECT ?p_other (COUNT(*) AS ?overlap) WHERE {"
			+ "?s ?p ?o; ?p_other ?o . "
			+ "?p_other a <http://www.w3.org/2002/07/owl#DatatypeProperty> ; rdfs:range ?range . FILTER(?p != ?p_other)}"
			+ " GROUP BY ?p_other");
	
	protected static final ParameterizedSparqlString GIVEN_PROPERTY_OVERLAP_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(*) AS ?overlap) WHERE {?s ?p ?o; ?p_other ?o . FILTER(?p != ?p_other)}");
	
	
	
	// set strict mode, i.e. if for the property explicit domain and range is given
	// we only consider properties with same range and domain
	protected boolean strictMode = false;

	protected double beta = 1.0;
	
	public DataPropertyHierarchyAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o ; ?p_other ?o}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s ?p_other ?o}}");
	}
	
	public void setPropertyToDescribe(OWLDataProperty propertyToDescribe) {
		super.setPropertyToDescribe(propertyToDescribe);
		
		GIVEN_PROPERTY_OVERLAP_QUERY.setIri("p", propertyToDescribe.toStringID());
		PROPERTY_OVERLAP_QUERY.setIri("p", propertyToDescribe.toStringID());
		PROPERTY_OVERLAP_WITH_RANGE_QUERY.setIri("p", propertyToDescribe.toStringID());
	}
	
	protected void run() {
		
		// get the candidates
		SortedSet<OWLDataProperty> candidates = getCandidates();

		// check for each candidate if an overlap exist
		int i = 1;
		for (OWLDataProperty p : candidates) {
			progressMonitor.learningProgressChanged(i++, candidates.size());
			
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(p);
			
			if(candidatePopularity == 0){// skip empty properties
				logger.debug("Cannot compute equivalence statements for empty candidate property " + p);
				continue;
			}
			
			// get the number of overlapping triples, i.e. triples with the same subject and object
			GIVEN_PROPERTY_OVERLAP_QUERY.setIri("p_other", p.toStringID());
			ResultSet rs = executeSelectQuery(GIVEN_PROPERTY_OVERLAP_QUERY.toString());
			int overlap = rs.next().getLiteral("overlap").getInt();
			
			// compute the score
			double score = computeScore(candidatePopularity, popularity, overlap);
			
			currentlyBestAxioms.add(
					new EvaluatedAxiom<T>(
							getAxiom(propertyToDescribe, p), 
							new AxiomScore(score, useSample)));
		}
	}
	
	/**
	 * In this method we try to compute the overlap with each property in one single SPARQL query.
	 * This method might be much slower as the query is much more complex.
	 */
	protected void runBatched() {
		
		String query;
		if(strictMode){
			// get rdfs:range of the property
			OWLDataRange range = reasoner.getRange(propertyToDescribe);
			
			if(range != null && range.isDatatype() && !range.isTopDatatype()){
				PROPERTY_OVERLAP_WITH_RANGE_QUERY.setIri("range", range.asOWLDatatype().toStringID());
				query = PROPERTY_OVERLAP_WITH_RANGE_QUERY.toString();
			} else {
				query = PROPERTY_OVERLAP_QUERY.toString();
			}
		} else {
			query = PROPERTY_OVERLAP_QUERY.toString();
		}
		
		ResultSet rs = executeSelectQuery(query);
		ResultSetRewindable rsrw = ResultSetFactory.copyResults(rs);
	    int size = rsrw.size();
	    rs = rsrw;
		while (rs.hasNext()) {
			QuerySolution qs = rsrw.next();
			progressMonitor.learningProgressChanged(rs.getRowNumber(), size);
			
			OWLDataProperty candidate = df.getOWLDataProperty(IRI.create(qs.getResource("p_other").getURI()));
			
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(candidate);
			
			// get the number of overlapping triples, i.e. triples with the same subject and object
			int overlap = qs.getLiteral("overlap").getInt();
			
			// compute the score
			double score = computeScore(candidatePopularity, popularity, overlap);

			currentlyBestAxioms.add(
					new EvaluatedAxiom<T>(
							getAxiom(propertyToDescribe, candidate), 
							new AxiomScore(score)));
		}
	}
	
	public double computeScore(int candidatePopularity, int popularity, int overlap){
		// compute the estimated precision
		double precision = Heuristics.getConfidenceInterval95WaldAverage(candidatePopularity, overlap);

		// compute the estimated recall
		double recall = Heuristics.getConfidenceInterval95WaldAverage(popularity, overlap);

		// compute the final score
		double score = Heuristics.getFScore(recall, precision, beta);
		
		return score;
	}
	
	public abstract T getAxiom(OWLDataProperty property, OWLDataProperty otherProperty);
	
	/**
	 * Returns the candidate properties for comparison.
	 * @return
	 */
	protected SortedSet<OWLDataProperty> getCandidates(){
		// get the candidates
		SortedSet<OWLDataProperty> candidates = new TreeSet<OWLDataProperty>();

		if (strictMode) { // that have the same domain and range 
			// get rdfs:domain of the property
			OWLClassExpression domain = reasoner.getDomain(propertyToDescribe);

			// get rdfs:range of the property
			OWLDataRange range = reasoner.getRange(propertyToDescribe);

			String query = "SELECT ?p WHERE {?p a owl:ObjectProperty .";
			if (domain != null && !domain.isAnonymous() && !domain.isOWLThing()) {
				query += "?p rdfs:domain <" + domain.asOWLClass().toStringID() + "> .";
			}

			if (range != null && range.isDatatype() && !range.isTopDatatype()) {
				query += "?p rdfs:range <" + range.asOWLDatatype().toStringID() + "> .";
			}
			query += "}";

			ResultSet rs = executeSelectQuery(query);
			while (rs.hasNext()) {
				OWLDataProperty p = df.getOWLDataProperty(IRI.create(rs.next().getResource("p").getURI()));
				candidates.add(p);
			}

		} else {// we have to check all other properties
			candidates = reasoner.getOWLDataProperties();
		}
		candidates.remove(propertyToDescribe);
		
		return candidates;
	}
	
	@Override
	public Set<OWLDataPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<T> evAxiom) {
		T axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		
		OWLDataProperty otherProperty;
		if(axiom instanceof OWLNaryPropertyAxiom){// we assume a single atomic property
			otherProperty = ((OWLNaryPropertyAxiom<OWLDataPropertyExpression>) axiom).getPropertiesMinus(propertyToDescribe).iterator().next()
					.asOWLDataProperty();
		} else {
			otherProperty = ((OWLSubDataPropertyOfAxiom) axiom).getSuperProperty().asOWLDataProperty();
		}
		posExamplesQueryTemplate.setIri("p_other", otherProperty.toStringID());

		Set<OWLDataPropertyAssertionAxiom> posExamples = new TreeSet<OWLDataPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString());
		}

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLLiteral object = convertLiteral(qs.getLiteral("o"));
			posExamples.add(df.getOWLDataPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return posExamples;
	}

	@Override
	public Set<OWLDataPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<T> evAxiom) {
		T axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		
		OWLDataProperty otherProperty;
		if(axiom instanceof OWLNaryPropertyAxiom){// we assume a single atomic property
			otherProperty = ((OWLNaryPropertyAxiom<OWLDataPropertyExpression>) axiom).getPropertiesMinus(propertyToDescribe).iterator().next()
					.asOWLDataProperty();
		} else {
			otherProperty = ((OWLSubDataPropertyOfAxiom) axiom).getSuperProperty().asOWLDataProperty();
		}
		negExamplesQueryTemplate.setIri("p_other", otherProperty.toStringID());

		Set<OWLDataPropertyAssertionAxiom> negExamples = new TreeSet<OWLDataPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString());
		}

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLLiteral object = convertLiteral(qs.getLiteral("o"));
			negExamples.add(df.getOWLDataPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return negExamples;
	}
	
	/**
	 * @param beta the beta to set
	 */
	public void setBeta(double beta) {
		this.beta = beta;
	}
	
	/**
	 * @param strictMode the strictMode to set
	 */
	public void setUseStrictMode(boolean strictMode) {
		this.strictMode = strictMode;
	}

}
