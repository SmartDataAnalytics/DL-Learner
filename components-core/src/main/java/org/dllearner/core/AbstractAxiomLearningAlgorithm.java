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

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KBElement;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.TypedConstant;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.owl.AxiomComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

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
	protected int limit = 1000;
	
	protected boolean timeout = true;
	
	protected boolean forceSPARQL_1_0_Mode = false;
	
	protected int chunkCount = 0;
	protected int chunkSize = 1000;
	protected int offset = 0;
	protected int lastRowCount = 0;
	
	protected boolean fullDataLoaded = false;
	
	private List<String> filterNamespaces = new ArrayList<String>();
	
	protected ParameterizedSparqlString iterativeQueryTemplate;
	
	protected Model workingModel;
	protected ParameterizedSparqlString posExamplesQueryTemplate;
	protected ParameterizedSparqlString negExamplesQueryTemplate;
	
	
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
	
	public void setForceSPARQL_1_0_Mode(boolean forceSPARQL_1_0_Mode) {
		this.forceSPARQL_1_0_Mode = forceSPARQL_1_0_Mode;
	}

	@Override
	public void start() {
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public void init() throws ComponentInitException {
		ks.init();
		if(reasoner == null){
			reasoner = new SPARQLReasoner((SparqlEndpointKS) ks);
		}
		timeout = true;
	}

	@Override
	public List<Axiom> getCurrentlyBestAxioms() {
		return null;
	}
	
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		return getCurrentlyBestAxioms(nrOfAxioms, 0.0);
	}
	
	public boolean isTimeout() {
		return timeout;
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
	
	protected Model executeConstructQuery(String query) {System.out.println(query);
		logger.debug("Sending query\n{} ...", query);
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(),
					query);
			queryExecution.setTimeout(getRemainingRuntimeInMilliSeconds());
			queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
			try {
				Model model = queryExecution.execConstruct();
				fetchedRows += model.size();
				timeout = false;
				if(model.size() == 0){
					fullDataLoaded = true;
				}
				logger.info("Got " + model.size() + " triples.");
				return model;
			} catch (QueryExceptionHTTP e) {
				if(e.getCause() instanceof SocketTimeoutException){
					logger.warn("Got timeout");
				} else {
					logger.error("Exception executing query", e);
				}
				return ModelFactory.createDefaultModel();
			}
		} else {
			QueryExecution queryExecution = QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			Model model = queryExecution.execConstruct();
			fetchedRows += model.size();
			if(model.size() == 0){
				fullDataLoaded = true;
			}
			return model;
		}
	}
	
	protected ResultSet executeSelectQuery(String query) {System.out.println(query);
		logger.debug("Sending query\n{} ...", query);
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(),
					query);
			queryExecution.setTimeout(getRemainingRuntimeInMilliSeconds());
			queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
			try {
				ResultSet rs = queryExecution.execSelect();
				timeout = false;
				return rs;
			} catch (QueryExceptionHTTP e) {
				if(e.getCause() instanceof SocketTimeoutException){
					if(timeout){
						logger.warn("Got timeout");
					} else {
						logger.trace("Got local timeout");
					}
					
				} else {
					logger.error("Exception executing query", e);
				}
				return new ResultSetMem();
			}
		} else {
			return executeSelectQuery(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
		}
	}
	
	protected ResultSet executeSelectQuery(String query, Model model) {
		logger.debug("Sending query on local model\n{} ...", query);
		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model);
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
	
	protected long getRemainingRuntimeInMilliSeconds(){
		return Math.max(0, (maxExecutionTimeInSeconds * 1000) - (System.currentTimeMillis() - startTime));
	}
	
	protected boolean terminationCriteriaSatisfied(){
		boolean timeLimitExceeded = maxExecutionTimeInSeconds == 0 ? false : getRemainingRuntimeInMilliSeconds() <= 0;
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
		
		return new AxiomScore(accuracy, confidence, total, success, total-success);
	}
	
	protected double accuracy(int total, int success){
		double[] confidenceInterval = Heuristics.getConfidenceInterval95Wald(total, success);
		return (confidenceInterval[0] + confidenceInterval[1]) / 2;
	}
	
	protected double fMEasure(double precision, double recall){
		return 2 * precision * recall / (precision + recall);
	}
	
	protected ResultSet fetchData(){
		setChunkConditions();
		if(!fullDataLoaded){
			Query query = buildQuery();
			offset += chunkSize;
			ResultSet rs = executeSelectQuery(query.toString());
			chunkCount++;
			return rs;
		}
		return new ResultSetMem();
	}
	
	private void setChunkConditions() {
		// adapt chunk size if needed
		if (chunkCount == 1 && lastRowCount < chunkSize) {
			logger.info("Adapting chunk size from " + chunkSize + " to " + lastRowCount);
			chunkSize = lastRowCount;
			offset = lastRowCount;
		}

		// check if full data was loaded
		if(chunkCount != 0){
			fullDataLoaded = (lastRowCount == 0) || (lastRowCount < chunkSize);
			if (fullDataLoaded) {
				logger.info("Loaded whole data. Early termination.");
			}
		}
	}
	
	private Query buildQuery(){
		Query query = iterativeQueryTemplate.asQuery();
		for(String ns : filterNamespaces){
			((ElementGroup)query.getQueryPattern()).addElementFilter(
					new ElementFilter(
							new E_Regex(
									new E_Str(new ExprVar(Node.createVariable("type"))),
									ns, "")));
		}
		query.setLimit(chunkSize);
		query.setOffset(offset);
		return query;
	}
	
	public void addFilterNamespace(String namespace){
		filterNamespaces.add(namespace);
	}
	
	public Set<KBElement> getPositiveExamples(EvaluatedAxiom axiom){
		if(workingModel != null){
			SortedSet<KBElement> posExamples = new TreeSet<KBElement>();
			
			ResultSet rs = executeSelectQuery(posExamplesQueryTemplate.toString(), workingModel);
			RDFNode node;
			while(rs.hasNext()){
				node = rs.next().get("s");
				if(node.isResource()){
					posExamples.add(new Individual(node.asResource().getURI()));
				} else if(node.isLiteral()){
					posExamples.add(new TypedConstant(node.asLiteral().getLexicalForm(), new Datatype(node.asLiteral().getDatatypeURI())));
				}
			}
			
			return posExamples;
		} else {
			throw new UnsupportedOperationException("Getting positive examples is not possible.");
		}
	}
	
	public Set<KBElement> getNegativeExamples(EvaluatedAxiom axiom){
		if(workingModel != null){
			SortedSet<KBElement> negExamples = new TreeSet<KBElement>();
			
			ResultSet rs = executeSelectQuery(negExamplesQueryTemplate.toString(), workingModel);
			RDFNode node;
			while(rs.hasNext()){
				node = rs.next().get("s");
				if(node.isResource()){
					negExamples.add(new Individual(node.asResource().getURI()));
				} else if(node.isLiteral()){
					negExamples.add(new TypedConstant(node.asLiteral().getLexicalForm(), new Datatype(node.asLiteral().getDatatypeURI())));
				}
			}
			
			return negExamples;
		} else {
			throw new UnsupportedOperationException("Getting negative examples is not possible.");
		}
	}
	
	public void explainScore(EvaluatedAxiom evAxiom){
		int posExampleCnt = getPositiveExamples(evAxiom).size();
		int negExampleCnt = getNegativeExamples(evAxiom).size();
		int total = posExampleCnt + negExampleCnt;
		StringBuilder sb = new StringBuilder();
		String lb = "\n";
		sb.append("######################################").append(lb);
		sb.append("Explanation:").append(lb);
		sb.append("Score(").append(evAxiom.getAxiom()).append(") = ").append(evAxiom.getScore().getAccuracy()).append(lb);
		sb.append("Fragment size:\t").append(workingModel.size()).append(" triples").append(lb);
		sb.append("Total number of resources:\t").append(total).append(lb);
		sb.append("Number of positive examples:\t").append(posExampleCnt).append(lb);
		sb.append("Number of negative examples:\t").append(negExampleCnt).append(lb);
		sb.append("Complete data processed:\t").append(fullDataLoaded).append(lb);
		sb.append("######################################");
		System.out.println(sb.toString());
	}
	
	public long getEvaluatedFramentSize(){
		return workingModel.size();
	}
	
	public static <E> void printSubset(Collection<E> collection, int maxSize){
		StringBuffer sb = new StringBuffer();
		int i = 0;
		Iterator<E> iter = collection.iterator();
		while(iter.hasNext() && i < maxSize){
			sb.append(iter.next().toString()).append(", ");
			i++;
		}
		if(iter.hasNext()){
			sb.append("...(").append(collection.size()-i).append(" more)");
		}
		System.out.println(sb.toString());
	}
	
	protected <K,T extends Set<V>, V> void addToMap(Map<K, T> map, K key, V value ){
		T values = map.get(key);
		if(values == null){
			try {
				values = (T) value.getClass().newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			values.add(value);
		}
		values.add(value);
	}
	
	protected <K,T extends Set<V>, V> void addToMap(Map<K, T> map, K key, Collection<V> newValues ){
		T values = map.get(key);
		if(values == null){
			try {
				values = (T) newValues.getClass().newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			map.put(key, values);
		}
		values.addAll(newValues);
	}
	
	private void adaptChunkCount(){
		
	}
	
	class OWLFilter extends Filter<OntClass>{

		@Override
		public boolean accept(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(OWL2.getURI());
			}
			return false;
		}
		
	}
	
	class RDFSFilter extends Filter<OntClass>{

		@Override
		public boolean accept(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(RDFS.getURI());
			}
			return false;
		}
		
	}
	
	class RDFFilter extends Filter<OntClass>{

		@Override
		public boolean accept(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(RDF.getURI());
			}
			return false;
		}
		
	}
	

}
