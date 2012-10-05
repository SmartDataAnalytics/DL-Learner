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

import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DataPropertyEditor;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyDomainAxiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.KBElement;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

@ComponentAnn(name="dataproperty domain axiom learner", shortName="dpldomain", version=0.1)
public class DataPropertyDomainAxiomLearner extends AbstractAxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(DataPropertyDomainAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=DataPropertyEditor.class)
	private DatatypeProperty propertyToDescribe;
	
	public DataPropertyDomainAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s a ?type}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s ?p ?o. FILTER NOT EXISTS{?s a ?type}}");
	}
	
	public DatatypeProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(DatatypeProperty propertyToDescribe) {
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
			Description existingDomain = reasoner.getDomain(propertyToDescribe);
			if(existingDomain != null){
				existingAxioms.add(new DatatypePropertyDomainAxiom(propertyToDescribe, existingDomain));
				if(reasoner.isPrepared()){
					if(reasoner.getClassHierarchy().contains(existingDomain)){
						for(Description sup : reasoner.getClassHierarchy().getSuperClasses(existingDomain)){
							existingAxioms.add(new DatatypePropertyDomainAxiom(propertyToDescribe, existingDomain));
							logger.info("Existing domain(inferred): " + sup);
						}
					}
					
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
		
		String query = String.format("SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s <%s> ?o.}", propertyToDescribe.getName());
		ResultSet rs = executeSelectQuery(query);
		int nrOfSubjects = rs.next().getLiteral("cnt").getInt();
		
		query = String.format("SELECT ?type (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s <%s> ?o. ?s a ?type.} GROUP BY ?type", propertyToDescribe.getName());
		rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			NamedClass domain = new NamedClass(qs.getResource("type").getURI());
			int cnt = qs.getLiteral("cnt").getInt();
			if(!domain.getURI().equals(Thing.uri)){
				currentlyBestAxioms.add(new EvaluatedAxiom(new DatatypePropertyDomainAxiom(propertyToDescribe, domain), computeScore(nrOfSubjects, cnt)));
			}
		}
	}
	
	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s a ?type.} WHERE {?s <%s> ?o. ?s a ?type.} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
		Model newModel = executeConstructQuery(query);
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			workingModel.add(newModel);
			// get number of distinct subjects
			query = "SELECT (COUNT(DISTINCT ?s) AS ?all) WHERE {?s a ?type.}";
			ResultSet rs = executeSelectQuery(query, workingModel);
			QuerySolution qs;
			int all = 1;
			while (rs.hasNext()) {
				qs = rs.next();
				all = qs.getLiteral("all").getInt();
			}
			
			// get class and number of instances
			query = "SELECT ?type (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a ?type.} GROUP BY ?type ORDER BY DESC(?cnt)";
			rs = executeSelectQuery(query, workingModel);
			
			if (all > 0) {
				currentlyBestAxioms.clear();
				while(rs.hasNext()){
					qs = rs.next();
					Resource type = qs.get("type").asResource();
					//omit owl:Thing as trivial domain
					if(type.equals(OWL.Thing)){
						continue;
					}
					currentlyBestAxioms.add(new EvaluatedAxiom(
							new DatatypePropertyDomainAxiom(propertyToDescribe, new NamedClass(type.getURI())),
							computeScore(all, qs.get("cnt").asLiteral().getInt())));
				}
				
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
			fillWithInference(newModel);
		}
	}
	
	private void fillWithInference(Model model){
		Model additionalModel = ModelFactory.createDefaultModel();
		if(reasoner.isPrepared()){
			for(StmtIterator iter = model.listStatements(null, RDF.type, (RDFNode)null); iter.hasNext();){
				Statement st = iter.next();
				Description cls = new NamedClass(st.getObject().asResource().getURI());
				if(reasoner.getClassHierarchy().contains(cls)){
					for(Description sup : reasoner.getClassHierarchy().getSuperClasses(cls)){
						additionalModel.add(st.getSubject(), st.getPredicate(), model.createResource(sup.toString()));
					}
				}
			}
		}
		model.add(additionalModel);
	}
	
	@Override
	public Set<KBElement> getPositiveExamples(EvaluatedAxiom evAxiom) {
		DatatypePropertyDomainAxiom axiom = (DatatypePropertyDomainAxiom) evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("type", axiom.getDomain().toString());
		return super.getPositiveExamples(evAxiom);
	}
	
	@Override
	public Set<KBElement> getNegativeExamples(EvaluatedAxiom evAxiom) {
		DatatypePropertyDomainAxiom axiom = (DatatypePropertyDomainAxiom) evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("type", axiom.getDomain().toString());
		return super.getNegativeExamples(evAxiom);
	}
	
	public static void main(String[] args) throws Exception{
		org.apache.log4j.Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
		org.apache.log4j.Logger.getLogger(DataPropertyDomainAxiomLearner.class).setLevel(Level.INFO);		
		
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		reasoner.prepareSubsumptionHierarchy();
		
		DataPropertyDomainAxiomLearner l = new DataPropertyDomainAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		l.setReasoner(reasoner);
		l.setPropertyToDescribe(new DatatypeProperty("http://dbpedia.org/ontology/AutomobileEngine/height"));
		l.setMaxExecutionTimeInSeconds(10);
		l.addFilterNamespace("http://dbpedia.org/ontology/");
//		l.setReturnOnlyNewAxioms(true);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(5));
	}
	

}
