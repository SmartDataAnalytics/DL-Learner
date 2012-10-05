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

import java.util.ArrayList;
import java.util.HashMap;
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
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.NamedClassEditor;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Learns sub classes using SPARQL queries.
 * 
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "simple subclass learner", shortName = "clsub", version = 0.1)
public class SimpleSubclassLearner extends AbstractAxiomLearningAlgorithm implements ClassExpressionLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleSubclassLearner.class);
	
	@ConfigOption(name="classToDescribe", required=true, description="", propertyEditorClass=NamedClassEditor.class)
	private NamedClass classToDescribe;
	
	private List<EvaluatedDescription> currentlyBestEvaluatedDescriptions;
	
	public SimpleSubclassLearner(SparqlEndpointKS ks) {
		this.ks = ks;
	}

	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return getCurrentlyBestEvaluatedAxioms(currentlyBestEvaluatedDescriptions.size());
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
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		currentlyBestAxioms = new ArrayList<EvaluatedAxiom>();
		for(EvaluatedDescription ed : getCurrentlyBestEvaluatedDescriptions(nrOfAxioms)){
			currentlyBestAxioms.add(new EvaluatedAxiom(new SubClassAxiom(classToDescribe, ed.getDescription()), new AxiomScore(ed.getAccuracy())));
		}
		return currentlyBestAxioms;
	}

	@Override
	public void start() {
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		fetchedRows = 0;
		currentlyBestEvaluatedDescriptions = new ArrayList<EvaluatedDescription>();
		
		//get existing super classes
		SortedSet<Description> existingSuperClasses = reasoner.getSuperClasses(classToDescribe);
		if(!existingSuperClasses.isEmpty()){
			SortedSet<Description> inferredSuperClasses = new TreeSet<Description>();
			for(Description assertedSup : existingSuperClasses){
				if(reasoner.isPrepared()){
					if(reasoner.getClassHierarchy().contains(assertedSup)){
						for(Description inferredSup : reasoner.getClassHierarchy().getSuperClasses(assertedSup, false)){
							inferredSuperClasses.add(inferredSup);
						}
					}
				} else {
					inferredSuperClasses.add(assertedSup);
				}
			}
			existingSuperClasses.addAll(inferredSuperClasses);
			logger.info("Existing super classes: " + existingSuperClasses);
			for(Description sup : existingSuperClasses){
				existingAxioms.add(new SubClassAxiom(classToDescribe, sup));
			}
		}
		
		if(!forceSPARQL_1_0_Mode && ks.supportsSPARQL_1_1()){
			runSingleQueryMode();
		} else {
			runSPARQL1_0_Mode();
		}
		
		logger.info("...finished in {}ms. (Got {} rows)", (System.currentTimeMillis()-startTime), fetchedRows);
	}
	
	private void runSPARQL1_0_Mode(){
		Map<Individual, SortedSet<Description>> ind2Types = new HashMap<Individual, SortedSet<Description>>();
		int limit = 1000;
		boolean repeat = true;
		while(!terminationCriteriaSatisfied() && repeat){
			repeat = addIndividualsWithTypes(ind2Types, limit, fetchedRows);
			createEvaluatedDescriptions(ind2Types);
			fetchedRows += 1000;
		}
	}
	
	private void runSingleQueryMode(){
		int total = reasoner.getPopularity(classToDescribe);
		
		if(total > 0){
			String query = String.format("SELECT ?type (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a <%s>. ?s a ?type} GROUP BY ?type ORDER BY DESC(?cnt)", classToDescribe.getName());
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				if(!qs.get("type").isAnon()){
					NamedClass sup = new NamedClass(qs.getResource("type").getURI());
					int overlap = qs.get("cnt").asLiteral().getInt();
					if(!sup.getURI().equals(Thing.uri) && ! classToDescribe.equals(sup)){//omit owl:Thing and the class to describe itself
						currentlyBestEvaluatedDescriptions.add(new EvaluatedDescription(sup, computeScore(total, overlap)));
					}
				}
			}
		}
	}
	
	public NamedClass getClassToDescribe() {
		return classToDescribe;
	}

	public void setClassToDescribe(NamedClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}
	
	private boolean addIndividualsWithTypes(Map<Individual, SortedSet<Description>> ind2Types, int limit, int offset){
		boolean notEmpty = false;
		String query;
		if(ks.supportsSPARQL_1_1()){
			query = String.format("PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT DISTINCT ?ind ?type WHERE {?ind a ?type.?type a owl:Class. {SELECT ?ind {?ind a <%s>} LIMIT %d OFFSET %d}}", classToDescribe.getName(), limit, offset);
		} else {
			query = String.format("SELECT DISTINCT ?ind ?type WHERE {?ind a <%s>. ?ind a ?type} LIMIT %d OFFSET %d", classToDescribe.getName(), limit, offset);
		}
		ResultSet rs = executeSelectQuery(query);
		Individual ind;
		Description newType;
		QuerySolution qs;
		SortedSet<Description> types;
		while(rs.hasNext()){
			qs = rs.next();
			ind = new Individual(qs.getResource("ind").getURI());
			newType = new NamedClass(qs.getResource("type").getURI());
			types = ind2Types.get(ind);
			if(types == null){
				types = new TreeSet<Description>();
				ind2Types.put(ind, types);
			}
			types.add(newType);
			Set<Description> superClasses;
			if(reasoner.isPrepared()){
				if(reasoner.getClassHierarchy().contains(newType)){
					superClasses = reasoner.getClassHierarchy().getSuperClasses(newType);
					types.addAll(superClasses);
				}
				
			}
			
			notEmpty = true;
		}
		return notEmpty;
	}
	
	private void createEvaluatedDescriptions(Map<Individual, SortedSet<Description>> individual2Types){
		currentlyBestEvaluatedDescriptions.clear();
		
		Map<Description, Integer> result = new HashMap<Description, Integer>();
		for(Entry<Individual, SortedSet<Description>> entry : individual2Types.entrySet()){
			for(Description nc : entry.getValue()){
				Integer cnt = result.get(nc);
				if(cnt == null){
					cnt = Integer.valueOf(1);
				} else {
					cnt = Integer.valueOf(cnt + 1);
				}
				result.put(nc, cnt);
			}
		}
		
		//omit owl:Thing and classToDescribe
		result.remove(new NamedClass(Thing.instance.getURI()));
		result.remove(classToDescribe);
		
		EvaluatedDescription evalDesc;
		int total = individual2Types.keySet().size();
		for(Entry<Description, Integer> entry : sortByValues(result, true)){
			evalDesc = new EvaluatedDescription(entry.getKey(),
					computeScore(total, entry.getValue()));
			currentlyBestEvaluatedDescriptions.add(evalDesc);
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		reasoner.prepareSubsumptionHierarchy();
		
		SimpleSubclassLearner l = new SimpleSubclassLearner(ks);
		l.setReasoner(reasoner);
		l.setReturnOnlyNewAxioms(true);
		
		ConfigHelper.configure(l, "maxExecutionTimeInSeconds", 50);
		l.setClassToDescribe(new NamedClass("http://dbpedia.org/ontology/SoccerClub"));
		l.init();
		l.start();
		
		for(EvaluatedAxiom e : l.getCurrentlyBestEvaluatedAxioms(Integer.MAX_VALUE, 0.75)){
			System.out.println(e);
		}
	}

}
