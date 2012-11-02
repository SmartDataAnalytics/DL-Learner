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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.owl.EquivalentObjectPropertiesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KBElement;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name="equivalent objectproperty axiom learner", shortName="oplequiv", version=0.1)
public class EquivalentObjectPropertyAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(EquivalentObjectPropertyAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	
	public EquivalentObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p ?o}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s ?o WHERE {?s ?p1 ?o. FILTER NOT EXISTS{?s ?p ?o}}");
	
	}
	
	public ObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(ObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}

	@Override
	public void start() {
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom>();
		
		if(returnOnlyNewAxioms){
			//get existing domains
			SortedSet<ObjectProperty> existingEquivalentProperties = reasoner.getEquivalentProperties(propertyToDescribe);
			if(existingEquivalentProperties != null && !existingEquivalentProperties.isEmpty()){
				for(ObjectProperty supProp : existingEquivalentProperties){
					existingAxioms.add(new EquivalentObjectPropertiesAxiom(propertyToDescribe, supProp));
				}
			}
		}
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSingleQueryMode();
		} else {
			runSPARQL1_0_Mode();
		}
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSingleQueryMode(){
		int total = reasoner.getPopularity(propertyToDescribe);
		
		if(total > 0){
			String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?s ?p ?o.} GROUP BY ?p", propertyToDescribe.getName());
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				ObjectProperty prop = new ObjectProperty(qs.getResource("p").getURI());
				int cnt = qs.getLiteral("cnt").getInt();
				if(!prop.equals(propertyToDescribe)){
					currentlyBestAxioms.add(new EvaluatedAxiom(new EquivalentObjectPropertiesAxiom(propertyToDescribe, prop), computeScore(total, cnt)));
				}
			}
		}
	}
	
	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s ?p ?o.} WHERE {?s <%s> ?o. ?s ?p ?o.} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
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
				ObjectProperty prop;
				while(rs.hasNext()){
					qs = rs.next();
					prop = new ObjectProperty(qs.get("p").asResource().getURI());
					//omit property to describe as it is trivial
					if(prop.equals(propertyToDescribe)){
						continue;
					}
					currentlyBestAxioms.add(new EvaluatedAxiom(
							new EquivalentObjectPropertiesAxiom(propertyToDescribe, prop),
							computeScore(all, qs.get("cnt").asLiteral().getInt())));
				}
				
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
	}
	
	@Override
	public Set<KBElement> getPositiveExamples(EvaluatedAxiom evAxiom) {
		EquivalentObjectPropertiesAxiom axiom = (EquivalentObjectPropertiesAxiom) evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("p", axiom.getEquivalentProperties().iterator().next().toString());
		if(workingModel != null){
			Set<KBElement> posExamples = new HashSet<KBElement>();
			
			ResultSet rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
			Individual subject;
			Individual object;
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				subject = new Individual(qs.getResource("s").getURI());
				object = new Individual(qs.getResource("o").getURI());
				posExamples.add(new ObjectPropertyAssertion(propertyToDescribe, subject, object));
			}
			
			return posExamples;
		} else {
			throw new UnsupportedOperationException("Getting positive examples is not possible.");
		}
	}
	
	@Override
	public Set<KBElement> getNegativeExamples(EvaluatedAxiom evAxiom) {
		EquivalentObjectPropertiesAxiom axiom = (EquivalentObjectPropertiesAxiom) evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("p", axiom.getEquivalentProperties().iterator().next().toString());
		if(workingModel != null){
			Set<KBElement> negExamples = new TreeSet<KBElement>();
			
			ResultSet rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
			Individual subject;
			Individual object;
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				subject = new Individual(qs.getResource("s").getURI());
				object = new Individual(qs.getResource("o").getURI());
				negExamples.add(new ObjectPropertyAssertion(propertyToDescribe, subject, object));
			}
			
			return negExamples;
		} else {
			throw new UnsupportedOperationException("Getting positive examples is not possible.");
		}
	}
	
	public static void main(String[] args) throws Exception{
		EquivalentObjectPropertyAxiomLearner l = new EquivalentObjectPropertyAxiomLearner(new SparqlEndpointKS(new SparqlEndpoint(
				new URL("http://dbpedia.aksw.org:8902/sparql"), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList())));//.getEndpointDBpediaLiveAKSW()));
		l.setPropertyToDescribe(new ObjectProperty("http://dbpedia.org/ontology/nationality"));
		l.setMaxExecutionTimeInSeconds(10);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5, 0.75));
	}
	

}
