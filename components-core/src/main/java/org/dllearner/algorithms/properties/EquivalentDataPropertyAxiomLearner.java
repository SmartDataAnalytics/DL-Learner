/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name="equivalent dataproperty axiom learner", shortName="dplequiv", version=0.1)
public class EquivalentDataPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm<OWLEquivalentDataPropertiesAxiom, OWLDataPropertyAssertionAxiom> {
	
	private static final Logger logger = LoggerFactory.getLogger(EquivalentDataPropertyAxiomLearner.class);
	
	private OWLDataProperty propertyToDescribe;
	
	public EquivalentDataPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p ?o}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s ?p1 ?o}}");
		super.existingAxiomsTemplate = new ParameterizedSparqlString("SELECT ?p WHERE {?p owl:equivalentProperty ?p_eq .}");
	}
	
	public OWLDataProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLDataProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		
		existingAxiomsTemplate.setIri("p", propertyToDescribe.toStringID());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		ResultSet rs = executeSelectQuery(existingAxiomsTemplate.toString());
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			OWLDataProperty equivalentProperty = df.getOWLDataProperty(IRI.create(qs.getResource("p_eq").getURI()));
			existingAxioms.add(df.getOWLEquivalentDataPropertiesAxiom(propertyToDescribe, equivalentProperty));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSingleQueryMode();
		} else {
			runSPARQL1_0_Mode();
		}
	}
	
	private void runSingleQueryMode(){
		int total = reasoner.getPopularity(propertyToDescribe);
		
		if(total > 0){
			String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?s ?p ?o.} GROUP BY ?p", propertyToDescribe.toStringID());
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				OWLDataProperty prop = df.getOWLDataProperty(IRI.create(qs.getResource("p").getURI()));
				int cnt = qs.getLiteral("cnt").getInt();
				if(!prop.equals(propertyToDescribe)){
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLEquivalentDataPropertiesAxiom>(df.getOWLEquivalentDataPropertiesAxiom(propertyToDescribe, prop), computeScore(total, cnt)));
					
				}
			}
		}
	}
	
	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s ?p ?o.} WHERE {?s <%s> ?o. ?s ?p ?o.} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			// get number of triples
			int all = (int)workingModel.size();
			
			if (all > 0) {
				// get class and number of instances
				query = "SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s ?p ?o.} GROUP BY ?p ORDER BY DESC(?cnt)";
				ResultSet rs = executeSelectQuery(query, workingModel);
				
				currentlyBestAxioms.clear();
				QuerySolution qs;
				OWLDataProperty prop;
				while(rs.hasNext()){
					qs = rs.next();
					prop = df.getOWLDataProperty(IRI.create(qs.get("p").asResource().getURI()));
					//omit property to describe as it is trivial
					if(prop.equals(propertyToDescribe)){
						continue;
					}
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLEquivalentDataPropertiesAxiom>(
							df.getOWLEquivalentDataPropertiesAxiom(propertyToDescribe, prop),
							computeScore(all, qs.get("cnt").asLiteral().getInt())));
				}
				
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	@Override
	public Set<OWLDataPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<OWLEquivalentDataPropertiesAxiom> evAxiom) {
		OWLEquivalentDataPropertiesAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		// we assume a single atomic property
		OWLDataProperty disjointProperty = axiom.getPropertiesMinus(propertyToDescribe).iterator().next()
				.asOWLDataProperty();
		posExamplesQueryTemplate.setIri("p_eq", disjointProperty.toStringID());

		Set<OWLDataPropertyAssertionAxiom> posExamples = new TreeSet<OWLDataPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString());
		}

		OWLIndividual subject;
		OWLLiteral object;
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			object = convertLiteral(qs.getLiteral("o"));
			posExamples.add(df.getOWLDataPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return posExamples;
	}
	
	@Override
	public Set<OWLDataPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<OWLEquivalentDataPropertiesAxiom> evAxiom) {
		OWLEquivalentDataPropertiesAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		// we assume a single atomic property
		OWLDataProperty disjointProperty = axiom.getPropertiesMinus(propertyToDescribe).iterator().next()
				.asOWLDataProperty();
		negExamplesQueryTemplate.setIri("p_eq", disjointProperty.toStringID());

		Set<OWLDataPropertyAssertionAxiom> negExamples = new TreeSet<OWLDataPropertyAssertionAxiom>();

		ResultSet rs;
		if (workingModel != null) {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString());
		}

		OWLIndividual subject;
		OWLLiteral object;
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			object = convertLiteral(qs.getLiteral("o"));
			negExamples.add(df.getOWLDataPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}

		return negExamples;
	}
}
