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
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.ClassExpressionLearningAlgorithm;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerEditor;
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
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	private int maxFetchedRows = 0;
	
	private List<EvaluatedDescription> currentlyBestEvaluatedDescriptions;
	private long startTime;
	private int fetchedRows;
	
	public SimpleSubclassLearner(SparqlEndpointKS ks) {
		this.ks = ks;
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
		List<EvaluatedAxiom> axioms = new ArrayList<EvaluatedAxiom>();
		for(EvaluatedDescription ed : getCurrentlyBestEvaluatedDescriptions(nrOfAxioms)){
			axioms.add(new EvaluatedAxiom(new SubClassAxiom(classToDescribe, ed.getDescription()), new AxiomScore(ed.getAccuracy())));
		}
		return axioms;
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
			logger.info("Existing super classes: " + existingSuperClasses);
		}
		
		
		Map<Individual, SortedSet<NamedClass>> ind2Types = new HashMap<Individual, SortedSet<NamedClass>>();
		int limit = 1000;
		while(!terminationCriteriaSatisfied()){
			addIndividualsWithTypes(ind2Types, limit, fetchedRows);
			createEvaluatedDescriptions(ind2Types);
			fetchedRows += 1000;
		}

		
		logger.info("...finished in {}ms. (Got {} rows)", (System.currentTimeMillis()-startTime), fetchedRows);
	}

	public NamedClass getClassToDescribe() {
		return classToDescribe;
	}

	public void setClassToDescribe(NamedClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}
	
	public int getMaxFetchedRows() {
		return maxFetchedRows;
	}

	public void setMaxFetchedRows(int maxFetchedRows) {
		this.maxFetchedRows = maxFetchedRows;
	}
	
	private void addIndividualsWithTypes(Map<Individual, SortedSet<NamedClass>> ind2Types, int limit, int offset){
//		String query = String.format("SELECT DISTINCT ?ind ?type WHERE {?ind a <%s>. ?ind a ?type} LIMIT %d OFFSET %d", classToDescribe.getName(), limit, offset);
		
		String query = String.format("SELECT DISTINCT ?ind ?type WHERE {?ind a ?type. {SELECT ?ind {?ind a <%s>} LIMIT %d OFFSET %d}}", classToDescribe.getName(), limit, offset);
		
		ResultSet rs = executeSelectQuery(query);
		Individual ind;
		NamedClass newType;
		QuerySolution qs;
		SortedSet<NamedClass> types;
		while(rs.hasNext()){
			qs = rs.next();
			ind = new Individual(qs.getResource("ind").getURI());
			newType = new NamedClass(qs.getResource("type").getURI());
			types = ind2Types.get(ind);
			if(types == null){
				types = new TreeSet<NamedClass>();
				ind2Types.put(ind, types);
			}
			types.add(newType);
		}
		
	}
	
	private void createEvaluatedDescriptions(Map<Individual, SortedSet<NamedClass>> individual2Types){
		currentlyBestEvaluatedDescriptions.clear();
		
		Map<NamedClass, Integer> result = new HashMap<NamedClass, Integer>();
		for(Entry<Individual, SortedSet<NamedClass>> entry : individual2Types.entrySet()){
			for(NamedClass nc : entry.getValue()){
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
		for(Entry<NamedClass, Integer> entry : sortByValues(result)){
			evalDesc = new EvaluatedDescription(entry.getKey(),
					computeScore(total, entry.getValue()));
			currentlyBestEvaluatedDescriptions.add(evalDesc);
		}
		
	}
	
	private boolean terminationCriteriaSatisfied(){
		boolean timeLimitExceeded = maxExecutionTimeInSeconds == 0 ? false : (System.currentTimeMillis() - startTime) >= maxExecutionTimeInSeconds * 1000;
		boolean resultLimitExceeded = maxFetchedRows == 0 ? false : fetchedRows >= maxFetchedRows;
		return  timeLimitExceeded || resultLimitExceeded; 
	}
	
	public static void main(String[] args) throws Exception{
		SimpleSubclassLearner l = new SimpleSubclassLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveOpenLink()));
		ConfigHelper.configure(l, "maxExecutionTimeInSeconds", 10);
		l.setClassToDescribe(new NamedClass("http://dbpedia.org/ontology/Criminal"));
		l.init();
		l.start();
		
		System.out.println(l.getCurrentlyBestEvaluatedDescriptions(5));
	}

}
