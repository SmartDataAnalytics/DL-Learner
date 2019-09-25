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

import org.apache.jena.query.*;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.*;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A learning algorithm for object property hierarchy axioms.
 * @author Lorenz Buehmann
 *
 */
public abstract class ObjectPropertyHierarchyAxiomLearner<T extends OWLObjectPropertyAxiom> extends ObjectPropertyAxiomLearner<T> {
	
	protected static final ParameterizedSparqlString PROPERTY_OVERLAP_QUERY = new ParameterizedSparqlString(
			"SELECT ?p_other (COUNT(*) AS ?overlap) WHERE {"
			+ "?s ?p ?o; ?p_other ?o . "
			+ "?p_other a <http://www.w3.org/2002/07/owl#ObjectProperty> . FILTER(?p != ?p_other)}"
			+ " GROUP BY ?p_other");

	protected static final ParameterizedSparqlString PROPERTY_OVERLAP_WITH_RANGE_QUERY = new ParameterizedSparqlString(
			"SELECT ?p_other (COUNT(*) AS ?overlap) WHERE {"
			+ "?s ?p ?o; ?p_other ?o . "
			+ "?p_other a <http://www.w3.org/2002/07/owl#ObjectProperty> ; rdfs:range ?range . FILTER(?p != ?p_other)}"
			+ " GROUP BY ?p_other");
	
	protected static final ParameterizedSparqlString GIVEN_PROPERTY_OVERLAP_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(*) AS ?overlap) WHERE {?s ?p ?o; ?p_other ?o . }");
	
	private static final ParameterizedSparqlString SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s ?p ?o . ?s ?p1 ?o . ?p1 a <http://www.w3.org/2002/07/owl#ObjectProperty> .} "
			+ "WHERE {?s ?p ?o . OPTIONAL{?s ?p1 ?o . FILTER(?p != ?p1)} }");

	protected static final ParameterizedSparqlString PROPERTY_OVERLAP_WITH_POPULARITY_BATCH_QUERY = new ParameterizedSparqlString(
			"SELECT ?p_other (COUNT(*) AS ?overlap) WHERE {"
					+ "?s ?p ?o; ?p_other ?o . "
					+ "?p_other a <http://www.w3.org/2002/07/owl#ObjectProperty> ; rdfs:range ?range . FILTER(?p != ?p_other)}"
					+ " GROUP BY ?p_other");
	
	
	// set strict mode, i.e. if for the property explicit domain and range is given
	// we only consider properties with same range and domain
	@ConfigOption(defaultValue = "false")
	protected boolean strictMode = false;

	@ConfigOption(defaultValue = "1.0", description = "the beta value for the F-score calculation")
	protected double beta = 1.0;

	@ConfigOption(defaultValue = "false", description = "compute everything in a single SPARQL query")
	protected boolean batchMode = false;

	
	public ObjectPropertyHierarchyAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o ; ?p_other ?o}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s ?p_other ?o}}");
	}
	
	@Override
	public void setEntityToDescribe(OWLObjectProperty entityToDescribe) {
		super.setEntityToDescribe(entityToDescribe);
		
		GIVEN_PROPERTY_OVERLAP_QUERY.setIri("p", entityToDescribe.toStringID());
		PROPERTY_OVERLAP_QUERY.setIri("p", entityToDescribe.toStringID());
		PROPERTY_OVERLAP_WITH_RANGE_QUERY.setIri("p", entityToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.PropertyAxiomLearner#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return SAMPLE_QUERY;
	}
	
	@Override
	protected void run() {
		if(batchMode) {
			runBatched();
		} else {
			runIterative();
		}
	}

	protected void runIterative() {
		// get the candidates
		SortedSet<OWLObjectProperty> candidates = getCandidates();

		// check for each candidate if an overlap exist
		int i = 1;
		for (OWLObjectProperty p : candidates) {
			logger.debug("processing candidate property {}...", p);
			progressMonitor.learningProgressChanged(axiomType, i++, candidates.size());

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
			AxiomScore score = computeScore(candidatePopularity, popularity, overlap);

			currentlyBestAxioms.add(new EvaluatedAxiom<>(getAxiom(entityToDescribe, p), score));
		}
	}
	
	/**
	 * In this method we try to compute the overlap with each property in one single SPARQL query.
	 * This method might be much slower as the query is much more complex.
	 *
	 * There are two options:
	 * 1) compute the overlap in a single query, but the popularity for each overlapping property separately
	 * 2) compute overlap and popularity in a single query
	 */
	protected void runBatched() {
		
		String query;
		if(strictMode){
			// get rdfs:range of the property
			OWLClassExpression range = reasoner.getRange(entityToDescribe);
			
			if(range != null && !range.isAnonymous() && !range.isOWLThing()){
				PROPERTY_OVERLAP_WITH_RANGE_QUERY.setIri("range", range.asOWLClass().toStringID());
				query = PROPERTY_OVERLAP_WITH_RANGE_QUERY.toString();
			} else {
				query = PROPERTY_OVERLAP_QUERY.toString();
			}
		} else {
			query = PROPERTY_OVERLAP_QUERY.toString();
		}

		// compute the property candidates p_i that have at least one (s,o) in common with the target property p
		ResultSet rs = executeSelectQuery(query);
		ResultSetRewindable rsrw = ResultSetFactory.copyResults(rs);
	    int size = rsrw.size();
	    rs = rsrw;
		while (rs.hasNext()) {
			QuerySolution qs = rsrw.next();

			progressMonitor.learningProgressChanged(axiomType, rs.getRowNumber(), size);
			
			OWLObjectProperty candidate = df.getOWLObjectProperty(IRI.create(qs.getResource("p_other").getURI()));
			
			// get the popularity of the candidate
			int candidatePopularity = reasoner.getPopularity(candidate);
			
			// get the number of overlapping triples, i.e. triples with the same subject and object
			int overlap = qs.getLiteral("overlap").getInt();
			
			// compute the score
			AxiomScore score = computeScore(candidatePopularity, popularity, overlap);

			currentlyBestAxioms.add(new EvaluatedAxiom<>(getAxiom(entityToDescribe, candidate), score));
		}
	}

	public abstract T getAxiom(OWLObjectProperty property, OWLObjectProperty otherProperty);
	
	/**
	 * Returns the candidate properties for comparison.
	 * @return the candidate properties
	 */
	protected SortedSet<OWLObjectProperty> getCandidates(){
		// get the candidates
		SortedSet<OWLObjectProperty> candidates = new TreeSet<>();

		if (strictMode) { // that have the same domain and range 
			// get rdfs:domain of the property
			OWLClassExpression domain = reasoner.getDomain(entityToDescribe);

			// get rdfs:range of the property
			OWLClassExpression range = reasoner.getRange(entityToDescribe);

			String query = "SELECT ?p WHERE {?p a owl:ObjectProperty .";
			if (domain != null && !domain.isAnonymous() && !domain.isOWLThing()) {
				query += "?p rdfs:domain <" + domain.asOWLClass().toStringID() + "> .";
			}

			if (range != null && !range.isAnonymous() && !range.isOWLThing()) {
				query += "?p rdfs:range <" + range.asOWLClass().toStringID() + "> .";
			}
			query += "}";

			ResultSet rs = executeSelectQuery(query);
			while (rs.hasNext()) {
				OWLObjectProperty p = df.getOWLObjectProperty(IRI.create(rs.next().getResource("p").getURI()));
				candidates.add(p);
			}

		} else {// we have to check all other properties
			candidates = reasoner.getOWLObjectProperties();
		}
		candidates.remove(entityToDescribe);
		
		return candidates;
	}
	
	@Override
	protected Set<OWLObjectPropertyAssertionAxiom> getExamples(ParameterizedSparqlString queryTemplate,
															   EvaluatedAxiom<T> evAxiom) {
		T axiom = evAxiom.getAxiom();
		queryTemplate.setIri("p", entityToDescribe.toStringID());

		OWLObjectProperty otherProperty;
		if(axiom instanceof OWLNaryPropertyAxiom){// we assume a single atomic property
			otherProperty = ((OWLNaryPropertyAxiom<OWLObjectPropertyExpression>) axiom).getPropertiesMinus(entityToDescribe).iterator().next()
					.asOWLObjectProperty();
		} else {
			otherProperty = ((OWLSubObjectPropertyOfAxiom) axiom).getSuperProperty().asOWLObjectProperty();
		}
		queryTemplate.setIri("p_other", otherProperty.toStringID());

		Set<OWLObjectPropertyAssertionAxiom> examples = new TreeSet<>();

		ResultSet rs = executeSelectQuery(queryTemplate.toString());

		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			OWLIndividual subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			OWLIndividual object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			examples.add(df.getOWLObjectPropertyAssertionAxiom(entityToDescribe, subject, object));
		}

		return examples;
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
	public void setStrictMode(boolean strictMode) {
		this.strictMode = strictMode;
	}

	public boolean isStrictMode() {
		return strictMode;
	}

	public double getBeta() {
		return beta;
	}

	/**
	 * If <code>true</code>, batch mode is used and only a single query will be used to compute the result. Otherwise,
	 * iteration over all properties in the ontology is done, i.e. at lots of queries - but simpler ones - will
	 * be executed.
	 *
	 * @param batchMode
	 */
	public void setBatchMode(boolean batchMode) {
		this.batchMode = batchMode;
	}

	public boolean isBatchMode() {
		return batchMode;
	}
}
