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

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

@ComponentAnn(name="objectproperty range learner", shortName="oplrange", version=0.1)
public class ObjectPropertyRangeAxiomLearner extends AbstractAxiomLearningAlgorithm<OWLObjectPropertyRangeAxiom, OWLIndividual> {
	
private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyRangeAxiomLearner.class);
	
	private OWLObjectProperty propertyToDescribe;
	
	public ObjectPropertyRangeAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o ?p ?s. ?s a ?type .}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT ?s WHERE {?o ?p ?s. FILTER NOT EXISTS {?s a ?type}}");
	
	}
	
	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		OWLClassExpression existingRange = reasoner.getRange(propertyToDescribe);
		if(existingRange != null){
			existingAxioms.add(df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, existingRange));
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
		
		String query = String.format("SELECT (COUNT(DISTINCT ?o) AS ?cnt) WHERE {?s <%s> ?o.}", propertyToDescribe.toStringID());
		ResultSet rs = executeSelectQuery(query);
		int nrOfSubjects = rs.next().getLiteral("cnt").getInt();
		
		query = String.format("SELECT ?type (COUNT(DISTINCT ?o) AS ?cnt) WHERE {?s <%s> ?o . ?o a ?type . ?type a owl:Class} GROUP BY ?type", propertyToDescribe.toStringID());
		rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("type") != null){
				OWLClass range = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
				int cnt = qs.getLiteral("cnt").getInt();
				currentlyBestAxioms.add(new EvaluatedAxiom<OWLObjectPropertyRangeAxiom>(
						df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, range), computeScore(nrOfSubjects, cnt)));
			} 
		}
	}

	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s <%s> ?o . ?o a ?type .} WHERE {?s <%s> ?o. ?o a ?type .} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			// get number of distinct subjects
			query = "SELECT (COUNT(?o) AS ?all) WHERE {?s ?p ?o.}";
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int all = 1;
			while (rs.hasNext()) {
				qs = rs.next();
				all = qs.getLiteral("all").getInt();
			}
			
			// get class and number of instances
//			query = "SELECT (DATATYPE(?o) AS ?dt) (COUNT(?o) AS ?cnt) WHERE{?s ?p ?o} GROUP BY DATATYPE(?o) ORDER BY DESC(?cnt)";
			query = "SELECT ?type (COUNT(?o) AS ?cnt) " +
					"WHERE {?s ?p ?o . ?o a ?type .} GROUP BY ?type";
			rs = executeSelectQuery(query, workingModel);
			
			if (all > 0) {
				currentlyBestAxioms.clear();
				while(rs.hasNext()){
					qs = rs.next();
					Resource type = qs.get("type").asResource();
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLObjectPropertyRangeAxiom>(
							df.getOWLObjectPropertyRangeAxiom(propertyToDescribe, df.getOWLClass(IRI.create(type.getURI()))),
							computeScore(all, qs.get("cnt").asLiteral().getInt())));
				}
				
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), propertyToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	@Override
	public Set<OWLIndividual> getPositiveExamples(EvaluatedAxiom<OWLObjectPropertyRangeAxiom> evAxiom) {
		OWLObjectPropertyRangeAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("type", axiom.getRange().toString());
		return super.getPositiveExamples(evAxiom);
	}
	
	@Override
	public Set<OWLIndividual> getNegativeExamples(EvaluatedAxiom<OWLObjectPropertyRangeAxiom> evAxiom) {
		OWLObjectPropertyRangeAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("type", axiom.getRange().toString());
		return super.getNegativeExamples(evAxiom);
	}
	
	public static void main(String[] args) throws Exception {
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia(), "cache");
		
		OWLDataFactory df = new OWLDataFactoryImpl();
		
		ObjectPropertyRangeAxiomLearner l = new ObjectPropertyRangeAxiomLearner(ks);
		l.setPropertyToDescribe(df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/birthPlace")));
		l.setMaxExecutionTimeInSeconds(20);
		l.addFilterNamespace("http://dbpedia.org/ontology/");
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(0.5));
	}

}
