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
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.dllearner.core.config.BooleanEditor;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 */
public abstract class AbstractAxiomLearningAlgorithm<T extends OWLAxiom, S extends OWLObject, E extends OWLEntity> extends AbstractComponent implements AxiomLearningAlgorithm<T>{
	
	protected LearningProblem learningProblem;
	protected final Logger logger;
	
	protected NumberFormat format = DecimalFormat.getPercentInstance();
	
	@ConfigOption(name="maxExecutionTimeInSeconds", defaultValue="10", description="", propertyEditorClass=IntegerEditor.class)
	protected int maxExecutionTimeInSeconds = 10;
	@ConfigOption(name="returnOnlyNewAxioms", defaultValue="false", description="", propertyEditorClass=BooleanEditor.class)
	protected boolean returnOnlyNewAxioms;
	@ConfigOption(name="maxFetchedRows", description="The maximum number of rows fetched from the endpoint to approximate the result.", propertyEditorClass=IntegerEditor.class)
	protected int maxFetchedRows;
	
	
	protected SparqlEndpointKS ks;
	// the instances which are set
	private SPARQLReasoner ksReasoner;
	private QueryExecutionFactory ksQef;
	// the instances on which the algorithms are really applied
	protected SPARQLReasoner reasoner;
	protected QueryExecutionFactory qef;
	
	
	protected SortedSet<EvaluatedAxiom<T>> currentlyBestAxioms;
	protected SortedSet<T> existingAxioms;
	
	protected long startTime;
	
	protected boolean timeout = true;
	
	protected boolean forceSPARQL_1_0_Mode = false;
	
	protected int chunkCount = 0;
	protected int chunkSize = 1000;
	protected int offset = 0;
	protected int lastRowCount = 0;
	
	protected boolean fullDataLoaded = false;
	
	protected List<String> allowedNamespaces = new ArrayList<String>();
	
	protected ParameterizedSparqlString iterativeQueryTemplate;
	
	protected Model sample;
	
	protected ParameterizedSparqlString posExamplesQueryTemplate;
	protected ParameterizedSparqlString negExamplesQueryTemplate;
	protected ParameterizedSparqlString existingAxiomsTemplate;
	
	protected OWLDataFactory df = new OWLDataFactoryImpl();
	
//	protected AxiomLearningProgressMonitor progressMonitor = new SilentAxiomLearningProgressMonitor();
	protected AxiomLearningProgressMonitor progressMonitor = new ConsoleAxiomLearningProgressMonitor();
	
	protected AxiomType<T> axiomType;
	protected E entityToDescribe;
	
	protected boolean useSampling = true;
	
	public AbstractAxiomLearningAlgorithm() {
		existingAxioms = new TreeSet<T>();
		
		logger = LoggerFactory.getLogger(this.getClass());
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
    
    /**
	 * @param entityToDescribe the entityToDescribe to set
	 */
	public void setEntityToDescribe(E entityToDescribe) {
		this.entityToDescribe = entityToDescribe;
	}
	
	/**
	 * @return the entityToDescribe
	 */
	public E getEntityToDescribe() {
		return entityToDescribe;
	}
	
	public void setUseSampling(boolean useSampling) {
		this.useSampling = useSampling;
	}
	
	public boolean isUseSampling() {
		return useSampling;
	}
    
    /**
	 * @return the axiomType
	 */
	public AxiomType<T> getAxiomType() {
		return axiomType;
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
		logger.info("Start learning...");
		startTime = System.currentTimeMillis();
		
		currentlyBestAxioms = new TreeSet<EvaluatedAxiom<T>>();
		
		if(returnOnlyNewAxioms){
			getExistingAxioms();
		}
		
		if(useSampling){
			generateSample();
		} else {
			qef = ksQef;
			reasoner = ksReasoner;
		}
		
		learnAxioms();
		
		logger.info("...finished in {}ms.", (System.currentTimeMillis()-startTime));
		logger.info("Found " + currentlyBestAxioms.size() + " axiom candidates.");
		if(!currentlyBestAxioms.isEmpty()){
			logger.info("Best axiom was " + currentlyBestAxioms.first());
		}
		
	}
	
	private void generateSample(){
		logger.info("Generating sample...");
		sample = ModelFactory.createDefaultModel();
		
		// we have to set up a new query execution factory working on our local model
		qef = new QueryExecutionFactoryModel(sample);
		reasoner = new SPARQLReasoner(qef, false);
		
		// get the page size 
		//TODO put to base class
		long pageSize = 10000;//PaginationUtils.adjustPageSize(globalQef, 10000);
		
		ParameterizedSparqlString sampleQueryTemplate = getSampleQuery();
		sampleQueryTemplate.setIri("p", entityToDescribe.toStringID());
		Query query = sampleQueryTemplate.asQuery();
		query.setLimit(pageSize);
		
		
		boolean isEmpty = false;
		int i = 0;
		while(!isTimeout() && !isEmpty){
			// get next sample
			logger.debug("Extending sample...");
			query.setOffset(i++ * pageSize);
			QueryExecution qe = ksQef.createQueryExecution(query);
			Model tmp = qe.execConstruct();
			sample.add(tmp);
			
			// if last call returned empty model, we can leave loop
			isEmpty = tmp.isEmpty();
		}
		logger.info("...done. Sample size: " + sample.size() + " triples");
	}
	
	/**
	 * @param progressMonitor the progressMonitor to set
	 */
	public void setProgressMonitor(AxiomLearningProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	@Override
	public void init() throws ComponentInitException {
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ks.getEndpoint();
			ksQef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
			if(ks.getCache() != null){
				ksQef = new QueryExecutionFactoryCacheEx(ksQef, ks.getCache());
			}
//			qef = new QueryExecutionFactoryPaginated(qef, 10000);
			
		} else {
			ksQef = new QueryExecutionFactoryModel(((LocalModelBasedSparqlEndpointKS)ks).getModel());
		}
		ks.init();
		if(reasoner == null){
			reasoner = new SPARQLReasoner(ksQef);
		}
		timeout = true;
	}
	
	/**
	 * Get the defined axioms in the knowledge base.
	 * @return
	 */
	protected abstract void getExistingAxioms();
	
	/**
	 * Learn new OWL axioms based on instance data.
	 */
	protected abstract void learnAxioms();
	
	/**
	 * The SPARQL CONSTRUCT query used to generate a sample for the given axiom type  and entity.
	 * @return
	 */
	protected abstract ParameterizedSparqlString getSampleQuery();

	@Override
	public List<T> getCurrentlyBestAxioms() {
		return getCurrentlyBestAxioms(Integer.MAX_VALUE);
	}
	
	public List<T> getCurrentlyBestAxioms(int nrOfAxioms) {
		return getCurrentlyBestAxioms(nrOfAxioms, 0.0);
	}
	
	public boolean wasTimeout() {
		return timeout;
	}
	
	public boolean isTimeout(){
		return maxExecutionTimeInSeconds == 0 ? false : getRemainingRuntimeInMilliSeconds() <= 0;
	}
	
	public List<T> getCurrentlyBestAxioms(int nrOfAxioms,
			double accuracyThreshold) {
		List<T> bestAxioms = new ArrayList<T>();
		for(EvaluatedAxiom<T> evAx : getCurrentlyBestEvaluatedAxioms(nrOfAxioms, accuracyThreshold)){
			bestAxioms.add(evAx.getAxiom());
		}
		return bestAxioms;
	}
	
	public List<T> getCurrentlyBestAxioms(double accuracyThreshold) {
		List<T> bestAxioms = new ArrayList<T>();
		for(EvaluatedAxiom<T> evAx : getCurrentlyBestEvaluatedAxioms(accuracyThreshold)){
			bestAxioms.add(evAx.getAxiom());
		}
		return bestAxioms;
	}
	
	public EvaluatedAxiom<T> getCurrentlyBestEvaluatedAxiom() {
		List<EvaluatedAxiom<T>> currentlyBestEvaluatedAxioms = getCurrentlyBestEvaluatedAxioms(1);
		if(currentlyBestEvaluatedAxioms.isEmpty()){
			return null;
		}
		return currentlyBestEvaluatedAxioms.get(0);
	}
	
	public List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms() {
		return new ArrayList<EvaluatedAxiom<T>>(currentlyBestAxioms);
	}

	public List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		return getCurrentlyBestEvaluatedAxioms(nrOfAxioms, 0.0);
	}
	
	public List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(double accuracyThreshold) {
		return getCurrentlyBestEvaluatedAxioms(Integer.MAX_VALUE, accuracyThreshold);
	}

	public List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms,
			double accuracyThreshold) {
		List<EvaluatedAxiom<T>> returnList = new ArrayList<EvaluatedAxiom<T>>();
		
		//get the currently best evaluated axioms
		List<EvaluatedAxiom<T>> currentlyBestEvAxioms = getCurrentlyBestEvaluatedAxioms();
		
		for(EvaluatedAxiom<T> evAx : currentlyBestEvAxioms){
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
	
	public EvaluatedAxiom<T> getBestEvaluatedAxiom(){
		if(!currentlyBestAxioms.isEmpty()){
			return new TreeSet<EvaluatedAxiom<T>>(currentlyBestAxioms).last();
		}
		return null;
	}
	
	protected Set<OWLClass> getAllClasses() {
		if(ks.isRemote()){
			return new SPARQLTasks(((SparqlEndpointKS) ks).getEndpoint()).getAllClasses();
		} else {
			Set<OWLClass> classes = new TreeSet<OWLClass>();
			for(OntClass cls : ((LocalModelBasedSparqlEndpointKS)ks).getModel().listClasses().filterDrop(new OWLFilter()).filterDrop(new RDFSFilter()).filterDrop(new RDFFilter()).toList()){
				if(!cls.isAnon()){
					classes.add(df.getOWLClass(IRI.create(cls.getURI())));
				}
			}
			return classes;
		}
		
	}
	
	protected Model executeConstructQuery(String query) {
		logger.trace("Sending query\n{} ...", query);
		QueryExecution qe = qef.createQueryExecution(query);
		try {
			Model model = qe.execConstruct();
			timeout = false;
			if(model.size() == 0){
				fullDataLoaded = true;
			}
			logger.debug("Got " + model.size() + " triples.");
			return model;
		} catch (QueryExceptionHTTP e) {
			if(e.getCause() instanceof SocketTimeoutException){
				logger.warn("Got timeout");
			} else {
				logger.error("Exception executing query", e);
			}
			return ModelFactory.createDefaultModel();
		}
	}
	
	protected ResultSet executeSelectQuery(String query) {
		logger.trace("Sending query\n{} ...", query);
		
		QueryExecution qe = qef.createQueryExecution(query);
		try {
			ResultSet rs = qe.execSelect();
			timeout = false;
			return rs;
		} catch (QueryExceptionHTTP e) {
			if(e.getCause() instanceof SocketTimeoutException){
				if(timeout){
					logger.warn("Got timeout");
					throw e;
				} else {
					logger.trace("Got local timeout");
				}
				
			} else {
				logger.error("Exception executing query", e);
			}
			return new ResultSetMem();
		}
	}
	
	protected ResultSet executeSelectQuery(String query, Model model) {
		logger.trace("Sending query on local model\n{} ...", query);
		QueryExecutionFactory qef = new QueryExecutionFactoryModel(model);
		QueryExecution qexec = qef.createQueryExecution(query);
		ResultSet rs = qexec.execSelect();
		return rs;
	}
	
	protected boolean executeAskQuery(String query){
		logger.trace("Sending query\n{} ...", query);
		return qef.createQueryExecution(query).execAsk();
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
		return  timeLimitExceeded ; 
	}
	
	protected List<Entry<OWLClassExpression, Integer>> sortByValues(Map<OWLClassExpression, Integer> map, final boolean useHierachy){
		List<Entry<OWLClassExpression, Integer>> entries = new ArrayList<Entry<OWLClassExpression, Integer>>(map.entrySet());
		final ClassHierarchy hierarchy = reasoner.getClassHierarchy();
		
        Collections.sort(entries, new Comparator<Entry<OWLClassExpression, Integer>>() {

			@Override
			public int compare(Entry<OWLClassExpression, Integer> o1, Entry<OWLClassExpression, Integer> o2) {
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
	
	protected AxiomScore computeScore(int total, int success){
		return computeScore(total, success, false);
	}
	
	protected AxiomScore computeScore(int total, int success, boolean sample){
		if(success > total){
			logger.warn("success value > total value");
		}
		double[] confidenceInterval = Heuristics.getConfidenceInterval95Wald(total, success);
		
		double accuracy = Heuristics.getConfidenceInterval95WaldAverage(total, success);
	
		double confidence = confidenceInterval[1] - confidenceInterval[0];
		
		return new AxiomScore(accuracy, confidence, success, total-success, sample);
	}
//	
//	protected double accuracy(int total, int success){
//		double[] confidenceInterval = Heuristics.getConfidenceInterval95Wald(total, success);
//		return (confidenceInterval[0] + confidenceInterval[1]) / 2;
//	}
//	
//	protected double fMEasure(double precision, double recall){
//		return 2 * precision * recall / (precision + recall);
//	}
	
	
	public void addFilterNamespace(String namespace){
		allowedNamespaces.add(namespace);
	}
	
	@SuppressWarnings("unchecked")
	public Set<S> getPositiveExamples(EvaluatedAxiom<T> axiom){
		ResultSet rs = executeSelectQuery(posExamplesQueryTemplate.toString());
		Set<OWLObject> posExamples = new TreeSet<OWLObject>();
		
		RDFNode node;
		while(rs.hasNext()){
			node = rs.next().get("s");
			if(node.isResource()){
				posExamples.add(df.getOWLNamedIndividual(IRI.create(node.asResource().getURI())));
			} else if(node.isLiteral()){
				posExamples.add(convertLiteral(node.asLiteral()));
			}
		}
		
		return (Set<S>) posExamples;
//		throw new UnsupportedOperationException("Getting positive examples is not possible.");
		
	}
	
	@SuppressWarnings("unchecked")
	public Set<S> getNegativeExamples(EvaluatedAxiom<T> axiom){
		
		ResultSet rs = executeSelectQuery(negExamplesQueryTemplate.toString());
		
		Set<OWLObject> negExamples = new TreeSet<OWLObject>();
		
		while(rs.hasNext()){
			RDFNode node = rs.next().get("s");
			if(node.isResource()){
				negExamples.add(df.getOWLNamedIndividual(IRI.create(node.asResource().getURI())));
			} else if(node.isLiteral()){
				negExamples.add(convertLiteral(node.asLiteral()));
			}
		}
		return (Set<S>) negExamples;
//		throw new UnsupportedOperationException("Getting negative examples is not possible.");
	}
	
	public void explainScore(EvaluatedAxiom<T> evAxiom){
		AxiomScore score = evAxiom.getScore();
		int posExampleCnt = score.getNrOfPositiveExamples();
		int negExampleCnt = score.getNrOfNegativeExamples();
		int total = posExampleCnt + negExampleCnt;
		StringBuilder sb = new StringBuilder();
		String lb = "\n";
		sb.append("######################################").append(lb);
		sb.append("Explanation:").append(lb);
		sb.append("Score(").append(evAxiom.getAxiom()).append(") = ").append(evAxiom.getScore().getAccuracy()).append(lb);
		sb.append("Total number of resources:\t").append(total).append(lb);
		sb.append("Number of positive examples:\t").append(posExampleCnt).append(lb);
		sb.append("Number of negative examples:\t").append(negExampleCnt).append(lb);
		sb.append("Based on sample:            \t").append(score.isSampleBased()).append(lb);
		if(sample != null){
			sb.append("Sample size(#triples):      \t").append(sample.size()).append(lb);
		}
		sb.append("######################################");
		System.out.println(sb.toString());
	}
	
	public long getEvaluatedFramentSize(){
		return sample.size();
	}
	
	/**
	 * Converts a JENA API Literal object into an OWL API OWLLiteral object.
	 * 
	 * @param lit
	 * @return
	 */
	protected OWLLiteral convertLiteral(Literal lit) {
		String datatypeURI = lit.getDatatypeURI();
		OWLLiteral owlLiteral;
		if (datatypeURI == null) {// rdf:PlainLiteral
			owlLiteral = df.getOWLLiteral(lit.getLexicalForm(), lit.getLanguage());
		} else {
			owlLiteral = df.getOWLLiteral(lit.getLexicalForm(), df.getOWLDatatype(IRI.create(datatypeURI)));
		}
		return owlLiteral;
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
				values.add(value);
			}
			catch (InstantiationException e) {e.printStackTrace();return;}
			catch (IllegalAccessException e) {e.printStackTrace();return;}
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
