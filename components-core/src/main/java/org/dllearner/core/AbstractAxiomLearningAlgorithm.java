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

package org.dllearner.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.owl.Axiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtendedQueryEngineHTTP;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 */
public abstract class AbstractAxiomLearningAlgorithm extends AbstractComponent implements AxiomLearningAlgorithm{
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractAxiomLearningAlgorithm.class);
	
	@ConfigOption(name="maxExecutionTimeInSeconds", defaultValue="10", description="", propertyEditorClass=IntegerEditor.class)
	protected int maxExecutionTimeInSeconds = 10;
	
	protected SparqlEndpointKS ks;
	protected SPARQLReasoner reasoner;
	
	ExtendedQueryEngineHTTP queryExecution;
	
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	@Override
	public void start() {
	}

	@Override
	public void init() throws ComponentInitException {
		reasoner = new SPARQLReasoner(ks);
	}

	@Override
	public List<Axiom> getCurrentlyBestAxioms() {
		return null;
	}
	
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		return getCurrentlyBestAxioms(nrOfAxioms, 0.0);
	}
	
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms,
			double accuracyThreshold) {
		List<Axiom> bestAxioms = new ArrayList<Axiom>();
		for(EvaluatedAxiom evAx : getCurrentlyBestEvaluatedAxioms(nrOfAxioms, accuracyThreshold)){
			bestAxioms.add(evAx.getAxiom());
		}
		return bestAxioms;
	}

	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		return getCurrentlyBestEvaluatedAxioms(nrOfAxioms, 0.0);
	}

	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms,
			double accuracyThreshold) {
		List<EvaluatedAxiom> returnList = new ArrayList<EvaluatedAxiom>();
		
		//get the currently best evaluated axioms
		List<EvaluatedAxiom> currentlyBestEvAxioms = getCurrentlyBestEvaluatedAxioms();
		
		for(EvaluatedAxiom evAx : currentlyBestEvAxioms){
			if(evAx.getScore().getAccuracy() >= accuracyThreshold && returnList.size() < nrOfAxioms){
				returnList.add(evAx);
			}
		}
		
		return returnList;
	}
	
	protected ResultSet executeSelectQuery(String query) {
		logger.info("Sending query\n{} ...", query);
		queryExecution = new ExtendedQueryEngineHTTP(ks.getEndpoint().getURL().toString(),
				query);
		queryExecution.setTimeout(maxExecutionTimeInSeconds * 1000);
		queryExecution.setDefaultGraphURIs(ks.getEndpoint().getDefaultGraphURIs());
		queryExecution.setNamedGraphURIs(ks.getEndpoint().getNamedGraphURIs());
		
		ResultSet resultSet = queryExecution.execSelect();
		
		return resultSet;
	}
	
	protected void close() {
		queryExecution.close();
	}
	
	protected boolean executeAskQuery(String query){
		logger.info("Sending query\n{} ...", query);
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
		for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		boolean result = queryExecution.execAsk();
		return result;
	}
	
	protected <K, V extends Comparable<V>> List<Entry<K, V>> sortByValues(Map<K, V> map){
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
        Collections.sort(entries, new Comparator<Entry<K, V>>() {

			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
        return entries;
	}
	
	protected Score computeScore(int total, int success){
		double[] confidenceInterval = Heuristics.getConfidenceInterval95Wald(total, success);
		
		double accuracy = (confidenceInterval[0] + confidenceInterval[1]) / 2;
	
		double confidence = confidenceInterval[1] - confidenceInterval[0];
		
		return new AxiomScore(accuracy, confidence);
	}
	
	

}
