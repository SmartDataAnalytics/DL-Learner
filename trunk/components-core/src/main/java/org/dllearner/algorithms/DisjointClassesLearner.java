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

package org.dllearner.algorithms;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ClassExpressionLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.NamedClassEditor;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DisjointClassesAxiom;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Learns disjoint classes using SPARQL queries.
 * 
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "disjoint classes learner", shortName = "cldisjoint", version = 0.1)
public class DisjointClassesLearner extends AbstractAxiomLearningAlgorithm implements ClassExpressionLearningAlgorithm {
	
	
	private static final Logger logger = LoggerFactory.getLogger(DisjointClassesLearner.class);
	
	@ConfigOption(name="classToDescribe", description="", propertyEditorClass=NamedClassEditor.class)
	private NamedClass classToDescribe;
	
	private List<EvaluatedDescription> currentlyBestEvaluatedDescriptions;
	private SortedSet<Description> subClasses;
	
	private boolean useWordNetDistance = false;
	private boolean suggestMostGeneralClasses = true;
	private boolean useClassPopularity = true;
	
	private Set<NamedClass> allClasses;
	
	private int popularity;
	
	public DisjointClassesLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}

	public NamedClass getClassToDescribe() {
		return classToDescribe;
	}

	public void setClassToDescribe(NamedClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}

	public boolean isUseWordNetDistance() {
		return useWordNetDistance;
	}

	public void setUseWordNetDistance(boolean useWordNetDistance) {
		this.useWordNetDistance = useWordNetDistance;
	}

	public boolean isSuggestMostGeneralClasses() {
		return suggestMostGeneralClasses;
	}

	public void setSuggestMostGeneralClasses(boolean suggestMostGeneralClasses) {
		this.suggestMostGeneralClasses = suggestMostGeneralClasses;
	}

	@Override
	public void start() {
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestEvaluatedDescriptions = new ArrayList<EvaluatedDescription>();
		
		//we return here if the class contains no instances
		popularity = reasoner.getPopularity(classToDescribe);
		if(popularity == 0){
			return;
		}
		
		//at first get all existing classes in knowledge base
		allClasses = getAllClasses();
		allClasses.remove(classToDescribe);
		
		//get the subclasses
		if(ks.isRemote()){
			if(reasoner.isPrepared()){
				subClasses = reasoner.getClassHierarchy().getSubClasses(classToDescribe, false);
			} else {
				subClasses = reasoner.getSubClasses(classToDescribe, true);
			}
		} else {
			subClasses = new TreeSet<Description>();
			OntModel ontModel = ((LocalModelBasedSparqlEndpointKS)ks).getModel();
			OntClass cls = ontModel.getOntClass(classToDescribe.getName());
			for(OntClass sub : cls.listSubClasses(false).toSet()){
				if(!sub.isAnon()){
					subClasses.add(new NamedClass(sub.getURI()));
				}
			}
			for(OntClass sup : cls.listSuperClasses().toSet()){
				if(!sup.isAnon()){
					subClasses.add(new NamedClass(sup.getURI()));
				}
			}
		}
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
//			runSPARQL1_1_Mode();
			runSingleQueryMode();
		} else {
			runSPARQL1_0_Mode();
		}
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
	}
	
	private void runSingleQueryMode(){
		//compute the overlap if exist
		Map<NamedClass, Integer> class2Overlap = new HashMap<NamedClass, Integer>(); 
		String query = String.format("SELECT ?type (COUNT(*) AS ?cnt) WHERE {?s a <%s>. ?s a ?type.} GROUP BY ?type", classToDescribe.getName());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			NamedClass cls = new NamedClass(qs.getResource("type").getURI());
			int cnt = qs.getLiteral("cnt").getInt();
			class2Overlap.put(cls, cnt);
		}
		//for each class in knowledge base
		for(NamedClass cls : allClasses){if(!cls.toString().equals("http://dbpedia.org/ontology/MotorcycleRider"))continue;
			//get the popularity
			int otherPopularity = reasoner.getPopularity(cls);
			if(otherPopularity == 0){//skip empty properties
				continue;
			}System.out.println(cls);
			//get the overlap
			int overlap = class2Overlap.containsKey(cls) ? class2Overlap.get(cls) : 0;
			//compute the estimated precision
			double precision = accuracy(otherPopularity, overlap);
			//compute the estimated recall
			double recall = accuracy(popularity, overlap);
			//compute the final score
			double score = 1 - fMEasure(precision, recall);
			
			currentlyBestEvaluatedDescriptions.add(new EvaluatedDescription(cls, new AxiomScore(score)));
		}
	}
	
	private void runSPARQL1_0_Mode(){
		Model model = ModelFactory.createDefaultModel();
		int limit = 1000;
		int offset = 0;
		String baseQuery  = "CONSTRUCT {?s a <%s>. ?s a ?type.} WHERE {?s a <%s>. ?s a ?type.} LIMIT %d OFFSET %d";
		String query = String.format(baseQuery, classToDescribe.getName(), classToDescribe.getName(), limit, offset);
		Model newModel = executeConstructQuery(query);
		Map<NamedClass, Integer> result = new HashMap<NamedClass, Integer>();
		NamedClass cls;
		while(!terminationCriteriaSatisfied() && newModel.size() != 0){
			model.add(newModel);
			//get total number of distinct instances
			query = "SELECT (COUNT(DISTINCT ?s) AS ?count) WHERE {?s a ?type.}";
			ResultSet rs = executeSelectQuery(query, model);
			int total = rs.next().getLiteral("count").getInt();
			
			// get number of instances of s with <s p o>
			query = "SELECT ?type (COUNT(?s) AS ?count) WHERE {?s a ?type.}" +
					" GROUP BY ?type";
			rs = executeSelectQuery(query, model);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				if(qs.getResource("type") != null && !qs.getResource("type").isAnon()){
					cls = new NamedClass(qs.getResource("type").getURI());
					int newCnt = qs.getLiteral("count").getInt();
					result.put(cls, newCnt);
				}
				
			}
			
			if(!result.isEmpty()){
				currentlyBestEvaluatedDescriptions = buildEvaluatedClassDescriptions(result, allClasses, total);
			}
			
			offset += limit;
			query = String.format(baseQuery, classToDescribe.getName(), classToDescribe.getName(), limit, offset);
			newModel = executeConstructQuery(query);
		}
		
	}
	
	private void runSPARQL1_1_Mode(){
		int limit = 1000;
		int offset = 0;
		String queryTemplate = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?type (COUNT(?s) AS ?count) WHERE {?s a ?type. ?type a owl:Class" +
		"{SELECT ?s WHERE {?s a <%s>.} LIMIT %d OFFSET %d} " +
		"} GROUP BY ?type";
		String query;
		Map<NamedClass, Integer> result = new HashMap<NamedClass, Integer>();
		NamedClass cls;
		Integer oldCnt;
		boolean repeat = true;
		
		while(!terminationCriteriaSatisfied() && repeat){
			query = String.format(queryTemplate, classToDescribe, limit, offset);System.out.println(query);
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			repeat = false;
			Resource res;
			while(rs.hasNext()){
				qs = rs.next();
				res = qs.getResource("type");
				if(res != null && !res.isAnon()){
					cls = new NamedClass(qs.getResource("type").getURI());
					int newCnt = qs.getLiteral("count").getInt();
					oldCnt = result.get(cls);
					if(oldCnt == null){
						oldCnt = Integer.valueOf(newCnt);
					} else {
						oldCnt += newCnt;
					}
					
					result.put(cls, oldCnt);
					repeat = true;
				}
				
			}
			if(!result.isEmpty()){
				currentlyBestEvaluatedDescriptions = buildEvaluatedClassDescriptions(result, allClasses, result.get(classToDescribe));
				offset += 1000;
			}
		}
	}

	@Override
	public List<Description> getCurrentlyBestDescriptions(int nrOfDescriptions) {
		List<Description> bestDescriptions = new ArrayList<Description>();
		for(EvaluatedDescription evDesc : getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions)){
			bestDescriptions.add(evDesc.getDescription());
		}
		return bestDescriptions;
	}

	@Override
	public List<? extends EvaluatedDescription> getCurrentlyBestEvaluatedDescriptions(
			int nrOfDescriptions) {
		int max = Math.min(currentlyBestEvaluatedDescriptions.size(), nrOfDescriptions);
		return currentlyBestEvaluatedDescriptions.subList(0, max);
	}
	
	@Override
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		List<Axiom> bestAxioms = new ArrayList<Axiom>();
		
		for(EvaluatedAxiom evAx : getCurrentlyBestEvaluatedAxioms(nrOfAxioms)){
			bestAxioms.add(evAx.getAxiom());
		}
		
		return bestAxioms;
	}

	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return getCurrentlyBestEvaluatedAxioms(currentlyBestEvaluatedDescriptions.size());
	}	
	
	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		List<Description> descriptions;
		for(EvaluatedDescription ed : getCurrentlyBestEvaluatedDescriptions(nrOfAxioms)){
			descriptions = new ArrayList<Description>();
			descriptions.add(classToDescribe);
			descriptions.add(ed.getDescription());
			axioms.add(new EvaluatedAxiom(new DisjointClassesAxiom(descriptions), new AxiomScore(ed.getAccuracy())));
		}
		return axioms;
	}
	
	private List<EvaluatedDescription> buildEvaluatedClassDescriptions(Map<NamedClass, Integer> class2Count, Set<NamedClass> allClasses, int total){
		List<EvaluatedDescription> evalDescs = new ArrayList<EvaluatedDescription>();
		
		//Remove temporarily classToDescribe but keep track of their count
//				Integer all = class2Count.get(classToDescribe);
				class2Count.remove(classToDescribe);
				
		//get complete disjoint classes
		Set<NamedClass> completeDisjointclasses = new TreeSet<NamedClass>(allClasses);
		completeDisjointclasses.removeAll(class2Count.keySet());
		
		// we remove the asserted subclasses here
		completeDisjointclasses.removeAll(subClasses);
		for (Description subClass : subClasses) {
			class2Count.remove(subClass);
		}
		
		//drop all classes which have a super class in this set
		if(suggestMostGeneralClasses){
			keepMostGeneralClasses(completeDisjointclasses);
		}
		
		
		
		
		EvaluatedDescription evalDesc;
		//firstly, create disjoint classexpressions which do not occur and give score of 1
		for(NamedClass cls : completeDisjointclasses){
			if(useClassPopularity){
				int overlap = 0;
				int pop;
				if(ks.isRemote()){
					pop = reasoner.getPopularity(cls);
				} else {
					pop = ((LocalModelBasedSparqlEndpointKS)ks).getModel().getOntClass(cls.getName()).listInstances().toSet().size();
				}
				//we skip classes with no instances
				if(pop == 0) continue;
				
				//we compute the estimated precision
				double precision = accuracy(pop, overlap);
				//we compute the estimated recall
				double recall = accuracy(popularity, overlap);
				//compute the overall score
				double score = 1 - fMEasure(precision, recall);
				
				evalDesc = new EvaluatedDescription(cls, new AxiomScore(score));
			} else {
				evalDesc = new EvaluatedDescription(cls, new AxiomScore(1));
			}
			
			evalDescs.add(evalDesc);
		}
		
		//secondly, create disjoint classexpressions with score 1 - (#occurence/#all)
		NamedClass cls;
		for (Entry<NamedClass, Integer> entry : sortByValues(class2Count)) {
			cls = entry.getKey();
			// drop classes from OWL and RDF namespace
			if (cls.getName().startsWith(OWL2.getURI()) || cls.getName().startsWith(RDF.getURI()))
				continue;
			if (useClassPopularity) {
				int overlap = entry.getValue();
				int pop;
				if (ks.isRemote()) {
					pop = reasoner.getPopularity(cls);
				} else {
					pop = ((LocalModelBasedSparqlEndpointKS) ks).getModel()
							.getOntClass(cls.getName()).listInstances().toSet()
							.size();
				}
				// we skip classes with no instances
				if (pop == 0)
					continue;

				// we compute the estimated precision
				double precision = accuracy(pop, overlap);
				// we compute the estimated recall
				double recall = accuracy(popularity, overlap);
				// compute the overall score
				double score = 1 - fMEasure(precision, recall);

				evalDesc = new EvaluatedDescription(cls, new AxiomScore(score));
			} else {
				evalDesc = new EvaluatedDescription(cls, new AxiomScore(1));
			}
			evalDescs.add(evalDesc);
		}
		
		class2Count.put(classToDescribe, total);
		return evalDescs;
	}
	
	private void keepMostGeneralClasses(Set<NamedClass> classes){
		if(ks.isRemote()){
			if(reasoner.isPrepared()){
				ClassHierarchy h = reasoner.getClassHierarchy();
				for(NamedClass nc : new HashSet<NamedClass>(classes)){
					classes.removeAll(h.getSubClasses(nc));
				}
			}
		} else {
			OntModel model = ((LocalModelBasedSparqlEndpointKS)ks).getModel();
			
//			Set<NamedClass> topClasses = new HashSet<NamedClass>();
//			for(OntClass cls : model.listNamedClasses().toSet()){
//				Set<OntClass> superClasses = cls.listSuperClasses().toSet();
//				if(superClasses.isEmpty() || 
//						(superClasses.size() == 1 && superClasses.contains(model.getOntClass(com.hp.hpl.jena.vocabulary.OWL.Thing.getURI())))){
//					topClasses.add(new NamedClass(cls.getURI()));
//				}
//				
//			}
//			classes.retainAll(topClasses);
			for(NamedClass nc : new HashSet<NamedClass>(classes)){//System.out.print(nc + "::");
				for(OntClass cls : model.getOntClass(nc.getName()).listSubClasses().toSet()){//System.out.print(cls + "|");
					classes.remove(new NamedClass(cls.getURI()));
				}
//				System.out.println();
			}
			
		}
	}
	
	private void computeAllDisjointClassAxiomsOptimized(){
		//get number of instances of A
		int instanceCountA = reasoner.getPopularity(classToDescribe);
		
		//firstly, we compute the disjointness to all sibling classes
		Set<EvaluatedDescription> disjointessOfSiblings = computeDisjointessOfSiblings(classToDescribe);
		System.out.println(disjointessOfSiblings);
		
		//we go the hierarchy up
		SortedSet<Description> superClasses = reasoner.getSuperClasses(classToDescribe);
		for (Description sup : superClasses) {
			Set<EvaluatedDescription> disjointessOfSuperClass = computeDisjointessOfSiblings(sup.asNamedClass());
			System.out.println(disjointessOfSuperClass);
		}
	}
	
	private Set<EvaluatedDescription> computeDisjointessOfSiblings(NamedClass cls){
		Set<EvaluatedDescription> evaluatedDescriptions = new HashSet<EvaluatedDescription>();
		
		//get number of instances of A
		int instanceCountA = reasoner.getPopularity(cls);
		
		if(instanceCountA > 0){
			//we compute the disjointness to all sibling classes
			Set<NamedClass> siblingClasses = reasoner.getSiblingClasses(cls);
			
			for (NamedClass sib : siblingClasses) {
				//get number of instances of B
				int instanceCountB = reasoner.getPopularity(sib);
				
				if(instanceCountB > 0){
					//get number of instances of (A and B)
					int instanceCountAB = reasoner.getPopularity(new Intersection(cls, sib));

					//we compute the estimated precision
					double precision = accuracy(instanceCountB, instanceCountAB);
					//we compute the estimated recall
					double recall = accuracy(instanceCountA, instanceCountAB);
					//compute the overall score
					double score = 1 - fMEasure(precision, recall);
					
					EvaluatedDescription evalDesc = new EvaluatedDescription(sib, new AxiomScore(score));
					evaluatedDescriptions.add(evalDesc);
				}
			}
		}
		
		return evaluatedDescriptions;
	}
	
	public EvaluatedAxiom computeDisjointess(NamedClass clsA, NamedClass clsB){
		logger.debug("Computing disjointness between " + clsA + " and " + clsB + "...");
		
		//if clsA = clsB
		if(clsA.equals(clsB)){
			return new EvaluatedAxiom(new DisjointClassesAxiom(clsA, clsB), new AxiomScore(0d, 1d));
		};
		
		//if the classes are connected via subsumption we assume that they are not disjoint 
		if(reasoner.isSuperClassOf(clsA, clsB) || reasoner.isSuperClassOf(clsB, clsA)){
			return new EvaluatedAxiom(new DisjointClassesAxiom(clsA, clsB), new AxiomScore(0d, 1d));
		};
		
		double scoreValue = 0;
		
		//get number of instances of A
		int instanceCountA = reasoner.getPopularity(clsA);
		
		//get number of instances of B
		int instanceCountB = reasoner.getPopularity(clsB);
		
		if(instanceCountA > 0 && instanceCountB > 0){
			//get number of instances of (A and B)
			int instanceCountAB = reasoner.getPopularity(new Intersection(clsA, clsB));
			
			//we compute the estimated precision
			double precision = accuracy(instanceCountB, instanceCountAB);
			
			//we compute the estimated recall
			double recall = accuracy(instanceCountA, instanceCountAB);
			
			//compute the overall score
			scoreValue = 1 - fMEasure(precision, recall);
			
		}
		
		AxiomScore score = new AxiomScore(scoreValue);
		
		return new EvaluatedAxiom(new DisjointClassesAxiom(clsA, clsB), score);
	}
	
	public Set<EvaluatedAxiom> computeSchemaDisjointness(){
		Set<EvaluatedAxiom> axioms = new HashSet<EvaluatedAxiom>();
		
		Set<NamedClass> classes = reasoner.getOWLClasses("http://dbpedia.org/ontology/");
		computeDisjointness(classes);
		
		//start from the top level classes, i.e. the classes whose direct super class is owl:Thing
		SortedSet<Description> topLevelClasses = reasoner.getMostGeneralClasses();
		axioms.addAll(computeDisjointness(asNamedClasses(topLevelClasses)));
		
		for (Description cls : topLevelClasses) {
			
		}
		
		return axioms;
	}
	
	public Set<EvaluatedAxiom> computeDisjointness(Set<NamedClass> classes){
		Set<EvaluatedAxiom> axioms = new HashSet<EvaluatedAxiom>();
		
		for (NamedClass clsA : classes) {
			for (NamedClass clsB : classes) {
				if(!clsA.equals(clsB)){
					axioms.add(computeDisjointess(clsA, clsB));
				}
			}
		}
		
		return axioms;
	}
	
	public static Set<NamedClass> asNamedClasses(Set<Description> descriptions){
		Set<NamedClass> classes = new TreeSet<NamedClass>();
		for (Description description : descriptions) {
			if(description.isNamedClass()){
				classes.add(description.asNamedClass());
			}
		}
		return classes;
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		ks = new LocalModelBasedSparqlEndpointKS(new URL("http://dl-learner.svn.sourceforge.net/viewvc/dl-learner/trunk/examples/swore/swore.rdf?revision=2217"));
		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		DisjointClassesLearner l = new DisjointClassesLearner(ks);
		SPARQLReasoner sparqlReasoner = new SPARQLReasoner(ks, "cache");
		sparqlReasoner.prepareSubsumptionHierarchy();
		sparqlReasoner.precomputeClassPopularity();
		l.setReasoner(sparqlReasoner);
		l.setClassToDescribe(new NamedClass("http://dbpedia.org/ontology/Actor"));
		l.setMaxExecutionTimeInSeconds(60);
		l.init();
		l.computeSchemaDisjointness();
		
//		System.out.println(l.getReasoner().getClassHierarchy().getSubClasses(new NamedClass("http://dbpedia.org/ontology/Athlete"), false));System.exit(0);
		l.start();
		
		for(EvaluatedAxiom e : l.getCurrentlyBestEvaluatedAxioms(Integer.MAX_VALUE, 0.0)){
			System.out.println(e);
		}
		
	}


}
