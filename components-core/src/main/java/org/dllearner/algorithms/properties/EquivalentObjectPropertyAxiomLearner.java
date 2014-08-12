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

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name="equivalent objectproperty axiom learner", shortName="oplequiv", version=0.1)
public class EquivalentObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm<OWLEquivalentObjectPropertiesAxiom, OWLObjectPropertyAssertionAxiom> {
	
	private static final Logger logger = LoggerFactory.getLogger(EquivalentObjectPropertyAxiomLearner.class);
	
	private OWLObjectProperty propertyToDescribe;
	
	public EquivalentObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p ?o ; ?p_eq ?o}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s ?p_eq ?o}}");
	
	}
	
	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
		posExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
		negExamplesQueryTemplate.setIri("p", propertyToDescribe.toStringID());
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLObjectProperty> existingEquivalentProperties = reasoner.getEquivalentProperties(propertyToDescribe);
		if(existingEquivalentProperties != null && !existingEquivalentProperties.isEmpty()){
			for(OWLObjectProperty supProp : existingEquivalentProperties){
				existingAxioms.add(df.getOWLEquivalentObjectPropertiesAxiom(propertyToDescribe, supProp));
			}
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
				OWLObjectProperty prop = df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI()));
				int cnt = qs.getLiteral("cnt").getInt();
				if(!prop.equals(propertyToDescribe)){
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLEquivalentObjectPropertiesAxiom>(df.getOWLEquivalentObjectPropertiesAxiom(propertyToDescribe, prop), computeScore(total, cnt)));
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
				OWLObjectProperty prop;
				while(rs.hasNext()){
					qs = rs.next();
					prop = df.getOWLObjectProperty(IRI.create(qs.get("p").asResource().getURI()));
					//omit property to describe as it is trivial
					if(prop.equals(propertyToDescribe)){
						continue;
					}
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLEquivalentObjectPropertiesAxiom>(
							df.getOWLEquivalentObjectPropertiesAxiom(propertyToDescribe, prop),
							computeScore(all, qs.get("cnt").asLiteral().getInt())));
				}
				
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getPositiveExamples(EvaluatedAxiom<OWLEquivalentObjectPropertiesAxiom> evAxiom) {
		OWLEquivalentObjectPropertiesAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p_eq", axiom.getPropertiesMinus(propertyToDescribe).iterator().next().asOWLObjectProperty().toStringID());
		
		ResultSet rs;
		if(workingModel != null){
			rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(posExamplesQueryTemplate.toString());
		}
		
		Set<OWLObjectPropertyAssertionAxiom> posExamples = new HashSet<OWLObjectPropertyAssertionAxiom>();
		
		OWLIndividual subject;
		OWLIndividual object;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			posExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}
		
		return posExamples;
	}
	
	@Override
	public Set<OWLObjectPropertyAssertionAxiom> getNegativeExamples(EvaluatedAxiom<OWLEquivalentObjectPropertiesAxiom> evAxiom) {
		OWLEquivalentObjectPropertiesAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p_eq", axiom.getPropertiesMinus(propertyToDescribe).iterator().next().asOWLObjectProperty().toStringID());
		
		ResultSet rs;
		if(workingModel != null){
			rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
		} else {
			rs = executeSelectQuery(negExamplesQueryTemplate.toString());
		}
		
		Set<OWLObjectPropertyAssertionAxiom> negExamples = new HashSet<OWLObjectPropertyAssertionAxiom>();
		
		OWLIndividual subject;
		OWLIndividual object;
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subject = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			object = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			negExamples.add(df.getOWLObjectPropertyAssertionAxiom(propertyToDescribe, subject, object));
		}
		
		return negExamples;
	}
	
	public static void main(String[] args) throws Exception{
		EquivalentObjectPropertyAxiomLearner l = new EquivalentObjectPropertyAxiomLearner(new SparqlEndpointKS(new SparqlEndpoint(
				new URL("http://dbpedia.aksw.org:8902/sparql"), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList())));//.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new OWLDataFactoryImpl().getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/nationality")));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5, 0.75));
	}
	

}
