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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
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

@ComponentAnn(name="objectproperty domain axiom learner", shortName="opldomain", version=0.1)
public class ObjectPropertyDomainAxiomLearner2 extends AbstractAxiomLearningAlgorithm<OWLObjectPropertyDomainAxiom, OWLIndividual> {
	
	private static final Logger logger = LoggerFactory.getLogger(ObjectPropertyDomainAxiomLearner2.class);
	
	private Map<OWLIndividual, SortedSet<OWLClassExpression>> individual2Types;
	
	private OWLObjectProperty propertyToDescribe;
	
	public ObjectPropertyDomainAxiomLearner2(SparqlEndpointKS ks){
		this.ks = ks;
		super.posExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s a ?type}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString("SELECT DISTINCT ?s WHERE {?s ?p ?o. FILTER NOT EXISTS{?s a ?type}}");
	
	}
	
	public OWLObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(OWLObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
//		negExamplesQueryTemplate.clearParams();
//		posExamplesQueryTemplate.clearParams();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		OWLClassExpression existingDomain = reasoner.getDomain(propertyToDescribe);
		if(existingDomain != null){
			existingAxioms.add(df.getOWLObjectPropertyDomainAxiom(propertyToDescribe, existingDomain));
			if(reasoner.isPrepared()){
				if(reasoner.getClassHierarchy().contains(existingDomain)){
					for(OWLClassExpression sup : reasoner.getClassHierarchy().getSuperClasses(existingDomain)){
						existingAxioms.add(df.getOWLObjectPropertyDomainAxiom(propertyToDescribe, existingDomain));
						logger.info("Existing domain(inferred): " + sup);
					}
				}
				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		runSPARQL1_0_Mode();
	}
	
	private void buildSampleFragment(){
		workingModel = ModelFactory.createDefaultModel();
		int limit = 10000;
		int offset = 0;
		String filter = "";
		for (String ns : allowedNamespaces) {
			filter += "FILTER(STRSTARTS(STR(?type), '" + ns + "'))";
		}
		ParameterizedSparqlString queryTemplate = new ParameterizedSparqlString("CONSTRUCT {?s a ?type.} WHERE {?s ?p ?o. ?s a ?type. " + filter + "}");
		queryTemplate.setIri("p", propertyToDescribe.toStringID());
		Query query =  queryTemplate.asQuery();
		query.setLimit(limit);
		Model tmp = executeConstructQuery(query.toString());
		workingModel.add(tmp);
		while(!tmp.isEmpty() && !terminationCriteriaSatisfied()){
			//increase offset by limit
			offset += limit;
			query.setOffset(offset);
			//run query
			tmp = executeConstructQuery(query.toString());
			workingModel.add(tmp);
		}
	}
	
	private void run(){
		//extract sample fragment from KB
		buildSampleFragment();
		
		//generate a set of axiom candidates
		computeAxiomCandidates();
		
		//compute evidence score on the whole KB
		List<OWLObjectPropertyDomainAxiom> axioms = getCurrentlyBestAxioms();
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom<OWLObjectPropertyDomainAxiom>>();
		//get total number of instances of A
		int cntA = reasoner.getPopularity(propertyToDescribe);
		OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
		for (OWLObjectPropertyDomainAxiom axiom : axioms) {
			//get total number of instances of B
			OWLClass domain = axiom.getDomain().asOWLClass();
			int cntB = reasoner.getPopularity(domain);
			
			//get number of instances of (A AND B)
			Query query = converter.asCountQuery(df.getOWLObjectIntersectionOf(domain, df.getOWLObjectSomeValuesFrom(propertyToDescribe, df.getOWLThing())));
//			System.out.println(query);
			int cntAB = executeSelectQuery(query.toString()).next().getLiteral("cnt").getInt();
			
			//precision (A AND B)/B
			double precision = Heuristics.getConfidenceInterval95WaldAverage(cntB, cntAB);
			
			//recall (A AND B)/A
			double recall = Heuristics.getConfidenceInterval95WaldAverage(cntA, cntAB);
			
			//beta
			double beta = 3.0;
			
			//F score
			double fscore = Heuristics.getFScore(recall, precision, beta);
			System.out.println(axiom + ":" + fscore + "(P=" + precision + "|R=" + recall + ")");
			currentlyBestAxioms.add(new EvaluatedAxiom<OWLObjectPropertyDomainAxiom>(axiom, new AxiomScore(fscore)));
//			System.out.println(new EvaluatedAxiom(axiom, new AxiomScore(fscore)));
		}
	}
	
	private void computeAxiomCandidates() {
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom<OWLObjectPropertyDomainAxiom>>();
		// get number of distinct subjects
		String query = "SELECT (COUNT(DISTINCT ?s) AS ?all) WHERE {?s a ?type.}";
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
			while (rs.hasNext()) {
				qs = rs.next();
				Resource type = qs.get("type").asResource();
				// omit owl:Thing as trivial domain
				if (type.equals(OWL.Thing)) {
					continue;
				}
				currentlyBestAxioms.add(new EvaluatedAxiom<OWLObjectPropertyDomainAxiom>(
						df.getOWLObjectPropertyDomainAxiom(propertyToDescribe, df.getOWLClass(IRI.create(type.getURI()))),
								computeScore(all, qs.get("cnt").asLiteral().getInt())));
			}
		}
	}
	
	private void computeLocalScore(){
		
	}
	
	private void computeScore(Set<OWLObjectPropertyDomainAxiom> axioms){
		OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
		for (OWLObjectPropertyDomainAxiom axiom : axioms) {
			OWLSubClassOfAxiom sub = axiom.asOWLSubClassOfAxiom();
			String subClassQuery = converter.convert("?s", sub.getSubClass());
		}
	}
	
	private void runSPARQL1_0_Mode() {
		workingModel = ModelFactory.createDefaultModel();
		int limit = 10000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s a ?type.} WHERE {?s <%s> ?o. ?s a ?type.} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
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
				all = qs.getLiteral("all").getInt();System.out.println(all);
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
					currentlyBestAxioms.add(new EvaluatedAxiom<OWLObjectPropertyDomainAxiom>(
							df.getOWLObjectPropertyDomainAxiom(propertyToDescribe, df.getOWLClass(IRI.create(type.getURI()))),
							computeScore(all, qs.get("cnt").asLiteral().getInt())));
				}
				
			}
			offset += limit;
			query = String.format(baseQuery, propertyToDescribe.toStringID(), limit, offset);
			newModel = executeConstructQuery(query);
			fillWithInference(newModel);
		}
	}
	
	private void fillWithInference(Model model){
		Model additionalModel = ModelFactory.createDefaultModel();
		if(reasoner.isPrepared()){
			for(StmtIterator iter = model.listStatements(null, RDF.type, (RDFNode)null); iter.hasNext();){
				Statement st = iter.next();
				OWLClass cls = df.getOWLClass(IRI.create(st.getObject().asResource().getURI()));
				if(reasoner.getClassHierarchy().contains(cls)){
					for(OWLClassExpression sup : reasoner.getClassHierarchy().getSuperClasses(cls)){
						additionalModel.add(st.getSubject(), st.getPredicate(), model.createResource(sup.toString()));
					}
				}
			}
		}
		model.add(additionalModel);
	}
	
	@Override
	public Set<OWLIndividual> getPositiveExamples(EvaluatedAxiom<OWLObjectPropertyDomainAxiom> evAxiom) {
		OWLObjectPropertyDomainAxiom axiom = evAxiom.getAxiom();
		posExamplesQueryTemplate.setIri("type", axiom.getDomain().toString());
		return super.getPositiveExamples(evAxiom);
	}
	
	@Override
	public Set<OWLIndividual> getNegativeExamples(EvaluatedAxiom<OWLObjectPropertyDomainAxiom> evAxiom) {
		OWLObjectPropertyDomainAxiom axiom = evAxiom.getAxiom();
		negExamplesQueryTemplate.setIri("type", axiom.getDomain().toString());
		return super.getNegativeExamples(evAxiom);
	}
	
	public static void main(String[] args) throws Exception{
		org.apache.log4j.Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
		org.apache.log4j.Logger.getLogger(DataPropertyDomainAxiomLearner.class).setLevel(Level.INFO);		
		
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia(), "cache");
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks, "cache");
		reasoner.prepareSubsumptionHierarchy();
		OWLDataFactory df = new OWLDataFactoryImpl();
		
		ObjectPropertyDomainAxiomLearner2 l = new ObjectPropertyDomainAxiomLearner2(ks);
		l.setReasoner(reasoner);
		l.setPropertyToDescribe(df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/birthPlace")));
		l.setMaxExecutionTimeInSeconds(20);
		l.addFilterNamespace("http://dbpedia.org/ontology/");
		l.init();
		l.start();
//		l.run();
		System.out.println(l.getBestEvaluatedAxiom());
		
		ObjectPropertyDomainAxiomLearner l2 = new ObjectPropertyDomainAxiomLearner(ks);
		l2.setReasoner(reasoner);
		l2.setPropertyToDescribe(df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/birthPlace")));
		l2.setMaxExecutionTimeInSeconds(10);
		l2.addFilterNamespace("http://dbpedia.org/ontology/");
		l2.init();
		l2.start();
		System.out.println(l2.getCurrentlyBestEvaluatedAxioms(0.2));
		System.out.println(l2.getBestEvaluatedAxiom());
//		for (OWLObjectProperty p : reasoner.getOWLObjectProperties("http://dbpedia.org/ontology/")) {
//			System.out.println(p);
//			l.setPropertyToDescribe(p);
//			l.setMaxExecutionTimeInSeconds(10);
//			l.addFilterNamespace("http://dbpedia.org/ontology/");
////			l.setReturnOnlyNewAxioms(true);
//			l.init();
////			l.start();
//			l.run();
//			List<EvaluatedAxiom> axioms = l.getCurrentlyBestEvaluatedAxioms(10, 0.5);
////			System.out.println(axioms);
//			System.out.println(l.getBestEvaluatedAxiom());
//		}
		
		
//		for(EvaluatedAxiom axiom : axioms){
//			printSubset(l.getPositiveExamples(axiom), 10);
//			printSubset(l.getNegativeExamples(axiom), 10);
//			l.explainScore(axiom);
//		}
	}
	
}
