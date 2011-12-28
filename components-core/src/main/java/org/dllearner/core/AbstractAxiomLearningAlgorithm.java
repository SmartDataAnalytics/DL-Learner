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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.config.BooleanEditor;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtendedQueryEngineHTTP;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.owl.AxiomComparator;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 */
public abstract class AbstractAxiomLearningAlgorithm extends AbstractComponent implements AxiomLearningAlgorithm{
	
	protected LearningProblem learningProblem;
	private static final Logger logger = LoggerFactory.getLogger(AbstractAxiomLearningAlgorithm.class);
	
	@ConfigOption(name="maxExecutionTimeInSeconds", defaultValue="10", description="", propertyEditorClass=IntegerEditor.class)
	protected int maxExecutionTimeInSeconds = 10;
	@ConfigOption(name="returnOnlyNewAxioms", defaultValue="false", description="", propertyEditorClass=BooleanEditor.class)
	protected boolean returnOnlyNewAxioms;
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	protected int maxFetchedRows;
	
	protected SparqlEndpointKS ks;
	protected SPARQLReasoner reasoner;
	
	protected List<EvaluatedAxiom> currentlyBestAxioms;
	protected SortedSet<Axiom> existingAxioms;
	protected int fetchedRows;
	
	protected long startTime;
	
	public AbstractAxiomLearningAlgorithm() {
		existingAxioms = new TreeSet<Axiom>(new AxiomComparator());
	}
	
	@Override
    public LearningProblem getLearningProblem() {
        return learningProblem;
    }

    @Autowired
    @Override
    public void setLearningProblem(LearningProblem learningProblem) {
        this.learningProblem = learningProblem;
    }	
	
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public SPARQLReasoner getReasoner() {
		return reasoner;
	}

	public void setReasoner(SPARQLReasoner reasoner) {
		this.reasoner = reasoner;
	}

	public boolean isReturnOnlyNewAxioms() {
		return returnOnlyNewAxioms;
	}

	public void setReturnOnlyNewAxioms(boolean returnOnlyNewAxioms) {
		this.returnOnlyNewAxioms = returnOnlyNewAxioms;
	}
	
	public int getMaxFetchedRows() {
		return maxFetchedRows;
	}

	public void setMaxFetchedRows(int maxFetchedRows) {
		this.maxFetchedRows = maxFetchedRows;
	}

	@Override
	public void start() {
	}

	@Override
	public void init() throws ComponentInitException {
		ks.init();
		if(reasoner == null){
			reasoner = new SPARQLReasoner((SparqlEndpointKS) ks);
		}
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
	
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return currentlyBestAxioms;
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
				if(returnOnlyNewAxioms){
					if(!existingAxioms.contains(evAx.getAxiom())){
						returnList.add(evAx);
					}
				} else {
					returnList.add(evAx);
				}
			}
		}
		
		return returnList;
	}
	
	protected Set<NamedClass> getAllClasses() {
		if(ks.isRemote()){
			return new SPARQLTasks(((SparqlEndpointKS) ks).getEndpoint()).getAllClasses();
		} else {
			Set<NamedClass> classes = new TreeSet<NamedClass>();
			for(OntClass cls : ((LocalModelBasedSparqlEndpointKS)ks).getModel().listClasses().filterDrop(new OWLFilter()).filterDrop(new RDFSFilter()).filterDrop(new RDFFilter()).toList()){
				if(!cls.isAnon()){
					classes.add(new NamedClass(cls.getURI()));
				}
			}
			return classes;
		}
		
	}
	
	protected Model executeConstructQuery(String query) {
		logger.info("Sending query\n{} ...", query);
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			ExtendedQueryEngineHTTP queryExecution = new ExtendedQueryEngineHTTP(endpoint.getURL().toString(),
					query);
			queryExecution.setTimeout(maxExecutionTimeInSeconds * 1000);
			queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
			return queryExecution.execConstruct();
		} else {
			QueryExecution qexec = QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			return qexec.execConstruct();
		}
	}
	
	protected ResultSet executeSelectQuery(String query) {
		logger.info("Sending query\n{} ...", query);
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			ExtendedQueryEngineHTTP queryExecution = new ExtendedQueryEngineHTTP(endpoint.getURL().toString(),
					query);
			queryExecution.setTimeout(maxExecutionTimeInSeconds * 1000);
			queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
			return queryExecution.execSelect();
		} else {
			return executeSelectQuery(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
		}
	}
	
	protected ResultSet executeSelectQuery(String query, Model model) {
		logger.info("Sending query on local model\n{} ...", query);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet rs = qexec.execSelect();;

		return rs;
	}
	
	protected boolean executeAskQuery(String query){
		logger.info("Sending query\n{} ...", query);
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
			return queryExecution.execAsk();
		} else {
			QueryExecution queryExecution = QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			return queryExecution.execAsk();
		}
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
	
	protected boolean terminationCriteriaSatisfied(){
		boolean timeLimitExceeded = maxExecutionTimeInSeconds == 0 ? false : (System.currentTimeMillis() - startTime) >= maxExecutionTimeInSeconds * 1000;
		boolean resultLimitExceeded = maxFetchedRows == 0 ? false : fetchedRows >= maxFetchedRows;
		return  timeLimitExceeded || resultLimitExceeded; 
	}
	
	protected List<Entry<Description, Integer>> sortByValues(Map<Description, Integer> map, final boolean useHierachy){
		List<Entry<Description, Integer>> entries = new ArrayList<Entry<Description, Integer>>(map.entrySet());
		final ClassHierarchy hierarchy = reasoner.getClassHierarchy();
		
        Collections.sort(entries, new Comparator<Entry<Description, Integer>>() {

			@Override
			public int compare(Entry<Description, Integer> o1, Entry<Description, Integer> o2) {
				int ret = o2.getValue().compareTo(o1.getValue());
				//if the score is the same, than we optionally also take into account the subsumption hierarchy
				if(ret == 0 && useHierachy){
					if(hierarchy != null){
						if(hierarchy.contains(o1.getKey()) && hierarchy.contains(o2.getKey())){
							if(hierarchy.isSubclassOf(o1.getKey(), o2.getKey())){
								ret = -1;
							} else if(hierarchy.isSubclassOf(o2.getKey(), o1.getKey())){
								ret = 1;
							} else {
								//we use the depth in the class hierarchy as third ranking property
//								int depth1 = hierarchy.getDepth2Root(o1.getKey());
//								int depth2 = hierarchy.getDepth2Root(o2.getKey());
//								ret = depth1 - depth2;
							}
						}
					}
				}
				
				return ret; 
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
	
	class OWLFilter extends Filter<OntClass>{

		@Override
		public boolean accept(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(OWL.NAMESPACE);
			}
			return false;
		}
		
	}
	
	class RDFSFilter extends Filter<OntClass>{

		@Override
		public boolean accept(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(RDFS.NAMESPACE);
			}
			return false;
		}
		
	}
	
	class RDFFilter extends Filter<OntClass>{

		@Override
		public boolean accept(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(RDF.NAMESPACE);
			}
			return false;
		}
		
	}
	

}
