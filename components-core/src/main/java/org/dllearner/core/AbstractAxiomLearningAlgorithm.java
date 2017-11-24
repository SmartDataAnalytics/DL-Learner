/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.annotations.Unused;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//import org.apache.jena.util.iterator.Filter;

/**
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 */
public abstract class AbstractAxiomLearningAlgorithm<T extends OWLAxiom, S extends OWLObject, E extends OWLEntity> extends AbstractComponent implements AxiomLearningAlgorithm<T>{
	
	@Unused
	protected LearningProblem learningProblem;
	protected final Logger logger;
	
	protected NumberFormat format = DecimalFormat.getPercentInstance(Locale.ROOT);
	
	@ConfigOption(defaultValue="10", description="maximum execution of the algorithm in seconds (abstract)")
	protected int maxExecutionTimeInSeconds = 10;
	@ConfigOption(defaultValue="false", description="omit axioms already existing in the knowledge base")
	protected boolean returnOnlyNewAxioms;
	@ConfigOption(description="The maximum number of rows fetched from the endpoint to approximate the result.")
	protected int maxFetchedRows;
	
	@ConfigOption(description = "the sparql endpoint knowledge source")
	protected SparqlEndpointKS ks;
	
	// the instances which are set
	private SPARQLReasoner ksReasoner;
	private QueryExecutionFactory ksQef;
	
	// the instances on which the algorithms are really applied
	@ConfigOption(description = "The sparql reasoner instance to use", defaultValue = "SPARQLReasoner")
	protected SPARQLReasoner reasoner;
	protected QueryExecutionFactory qef;
	
	
	protected SortedSet<EvaluatedAxiom<T>> currentlyBestAxioms;
	protected SortedSet<T> existingAxioms;
	
	protected long startTime;
	
	protected boolean timeout = true;
	
	@Unused
	protected boolean forceSPARQL_1_0_Mode = false;
	
	protected int chunkCount = 0;
	protected int chunkSize = 1000;
	protected int offset = 0;
	protected int lastRowCount = 0;
	
	protected boolean fullDataLoaded = false;
	
	protected List<String> allowedNamespaces = new ArrayList<>();
	
	protected ParameterizedSparqlString iterativeQueryTemplate;
	
	protected Model sample;
	
	protected ParameterizedSparqlString posExamplesQueryTemplate;
	protected ParameterizedSparqlString negExamplesQueryTemplate;
	protected ParameterizedSparqlString existingAxiomsTemplate;
	
	protected OWLDataFactory df = new OWLDataFactoryImpl();

	@NoConfigOption
//	protected AxiomLearningProgressMonitor progressMonitor = new SilentAxiomLearningProgressMonitor();
	protected AxiomLearningProgressMonitor progressMonitor = new ConsoleAxiomLearningProgressMonitor();
	
	protected AxiomType<T> axiomType;
	
	@ConfigOption(description = "the OWL entity to learn about")
	protected E entityToDescribe;
	
	@Unused
	protected boolean useSampling = true;
	protected int popularity;
	
	public AbstractAxiomLearningAlgorithm() {
		existingAxioms = new TreeSet<>();
		
		logger = LoggerFactory.getLogger(this.getClass());
	}

	@NoConfigOption
	public void setQueryExecutionFactory(QueryExecutionFactory qef) {
		this.ksQef = qef;
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
	 * @param entityToDescribe the entity for which axioms will be computed
	 */
	public void setEntityToDescribe(E entityToDescribe) {
		this.entityToDescribe = entityToDescribe;
	}
	
	/**
	 * @return the entity for which axioms will be computed
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
		this.ksReasoner = reasoner;
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
		logger.info("Started learning of " + axiomType.getName() + " axioms for " +
				OWLAPIUtils.getPrintName(entityToDescribe.getEntityType()) + " " + entityToDescribe.toStringID() + "...");
		startTime = System.currentTimeMillis();
		
		currentlyBestAxioms = new TreeSet<>();

		// check whether the knowledge base contains information about the entity, if not terminate
		popularity = reasoner.getPopularity(entityToDescribe);
		if(popularity == 0){
			logger.warn("Cannot make " + axiomType.getName() + " axiom suggestions for empty " + OWLAPIUtils.getPrintName(entityToDescribe.getEntityType()) + " " + entityToDescribe.toStringID());
			return;
		}

		// if enabled, we check for existing axioms that will filtered out in the final result
		if(returnOnlyNewAxioms){
			getExistingAxioms();
		}

		// if enabled, we generated a sample of the knowledge base and do the rest of the compuatation locally
		if(useSampling){
			generateSample();
		} else {
			qef = ksQef;
			reasoner = ksReasoner;
		}

		// compute the axiom type specific popularity of the entity to describe
		popularity = getPopularity();

		// start the real learning algorithm
		progressMonitor.learningStarted(axiomType);
		try {
			learnAxioms();
		} catch (Exception e) {
			progressMonitor.learningFailed(axiomType);
			throw e;
		} finally {
			progressMonitor.learningStopped(axiomType);
		}
		
		logger.info("...finished learning of " + axiomType.getName()
				+ " axioms for " + OWLAPIUtils.getPrintName(entityToDescribe.getEntityType())
				+ " " + entityToDescribe.toStringID() + " in {}ms.", (System.currentTimeMillis() - startTime));

		showResult();

	}

	protected void showResult() {
		DecimalFormat format = new DecimalFormat("0.0000");
		format.setRoundingMode(RoundingMode.DOWN);
		if(this instanceof ObjectPropertyCharacteristicsAxiomLearner){
			logger.info("Suggested axiom: " + currentlyBestAxioms.first());
		} else {
			logger.info("Found " + currentlyBestAxioms.size() + " axiom candidates. " +
								(returnOnlyNewAxioms
										? currentlyBestAxioms.size() - existingAxioms.size() +
										" out of them do not already exists in the knowledge base."
										: ""));
			if(!currentlyBestAxioms.isEmpty()){
				EvaluatedAxiom<T> bestAxiom = currentlyBestAxioms.last();
				String s = "Best axiom candidate is " + bestAxiom.hypothesis + " with an accuracy of " +
						format.format(bestAxiom.getAccuracy());
				if(returnOnlyNewAxioms) {
					if(existingAxioms.contains(bestAxiom.hypothesis)) {
						s += ", but it's already contained in the knowledge base.";
						if(existingAxioms.size() != currentlyBestAxioms.size()) {
							Optional<EvaluatedAxiom<T>> bestNewAxiom = currentlyBestAxioms.stream().filter(
									ax -> !existingAxioms.contains(ax.hypothesis)).findFirst();
							if(bestNewAxiom.isPresent()) {
								s += " The best new one is " + bestNewAxiom.get().hypothesis + " with an accuracy of " +
										format.format(bestNewAxiom.get().getAccuracy());
							}
						}
					}
				}
				logger.info(s);
			}
		}
	}

	/**
	 * Compute the popularity of the entity to describe. This depends on the axiom type, thus, the mehtod might
	 * be overwritten in the corresponding algorithm classes.
	 * @return the populairty of the entity to describe
	 */
	protected int getPopularity() {
		return reasoner.getPopularity(entityToDescribe);
	}
	
	private void generateSample(){
		logger.info("Generating sample...");
		sample = ModelFactory.createDefaultModel();
		
		// we have to set up a new query execution factory working on our local model
		qef = new QueryExecutionFactoryModel(sample);
		reasoner = new SPARQLReasoner(qef);
		
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
			ksQef = ks.getQueryExecutionFactory();
		} else {
			ksQef = new QueryExecutionFactoryModel(((LocalModelBasedSparqlEndpointKS)ks).getModel());
		}
		if(ksReasoner == null){
			ksReasoner = new SPARQLReasoner(ksQef);
		}
//		ksReasoner.supportsSPARQL1_1();
		reasoner = ksReasoner;
		
		initialized = true;
	}
	
	/**
	 * Compute the defined axioms in the knowledge base.
	 */
	protected abstract void getExistingAxioms();
	
	/**
	 * Learn new OWL axioms based on instance data.
	 */
	protected abstract void learnAxioms();
	
	/**
	 * The SPARQL CONSTRUCT query used to generate a sample for the given axiom type  and entity.
	 * @return the SPARQL query
	 */
	protected abstract ParameterizedSparqlString getSampleQuery();

	@Override
	public List<T> getCurrentlyBestAxioms() {
		return getCurrentlyBestAxioms(Integer.MAX_VALUE);
	}
	
	@Override
	public List<T> getCurrentlyBestAxioms(int nrOfAxioms) {
		return getCurrentlyBestAxioms(nrOfAxioms, 0.0);
	}
	
	public boolean wasTimeout() {
		return timeout;
	}
	
	public boolean isTimeout(){
		return maxExecutionTimeInSeconds != 0 && getRemainingRuntimeInMilliSeconds() <= 0;
	}
	
	public List<T> getCurrentlyBestAxioms(int nrOfAxioms,
			double accuracyThreshold) {
		return getCurrentlyBestEvaluatedAxioms(nrOfAxioms, accuracyThreshold)
				.stream()
				.map(EvaluatedAxiom<T>::getAxiom)
				.collect(Collectors.toList());
	}
	
	public List<T> getCurrentlyBestAxioms(double accuracyThreshold) {
		return getCurrentlyBestEvaluatedAxioms(accuracyThreshold)
				.stream()
				.map(EvaluatedAxiom<T>::getAxiom)
				.collect(Collectors.toList());
	}
	
	public EvaluatedAxiom<T> getCurrentlyBestEvaluatedAxiom() {
		if(currentlyBestAxioms.isEmpty()) {
			return null;
		}
		return currentlyBestAxioms.last();
	}
	
	@Override
	public List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms() {
		ArrayList<EvaluatedAxiom<T>> axioms = new ArrayList<>(currentlyBestAxioms);

		// revert because highest element in tree set is last
		Collections.reverse(axioms);

		return axioms;
	}

	@Override
	public List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		return getCurrentlyBestEvaluatedAxioms(nrOfAxioms, 0.0);
	}
	
	public List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(double accuracyThreshold) {
		return getCurrentlyBestEvaluatedAxioms(Integer.MAX_VALUE, accuracyThreshold);
	}

	@Override
	public List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms,
			double accuracyThreshold) {
		List<EvaluatedAxiom<T>> returnList = new ArrayList<>();
		
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
			return new TreeSet<>(currentlyBestAxioms).last();
		}
		return null;
	}
	
	protected Set<OWLClass> getAllClasses() {
		if(ks.isRemote()){
			return new SPARQLTasks(ks.getEndpoint()).getAllClasses();
		} else {
			return ((LocalModelBasedSparqlEndpointKS) ks).getModel().listClasses()
					.filterDrop(new OWLFilter())
					.filterDrop(new RDFSFilter())
					.filterDrop(new RDFFilter())
					.toList().stream()
					.filter(cls -> !cls.isAnon())
					.map(cls -> df.getOWLClass(IRI.create(cls.getURI())))
					.collect(Collectors.toCollection(TreeSet::new));
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
		return qexec.execSelect();
	}
	
	protected boolean executeAskQuery(String query){
		logger.trace("Sending query\n{} ...", query);
		return qef.createQueryExecution(query).execAsk();
	}
	
	protected <K, V extends Comparable<V>> List<Entry<K, V>> sortByValues(Map<K, V> map){
		List<Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return entries;
	}
	
	protected long getRemainingRuntimeInMilliSeconds(){
		return Math.max(0, (maxExecutionTimeInSeconds * 1000) - (System.currentTimeMillis() - startTime));
	}
	
	protected boolean terminationCriteriaSatisfied(){
		return maxExecutionTimeInSeconds != 0 && getRemainingRuntimeInMilliSeconds() <= 0;
	}
	
	protected List<Entry<OWLClassExpression, Integer>> sortByValues(Map<OWLClassExpression, Integer> map, final boolean useHierachy){
		List<Entry<OWLClassExpression, Integer>> entries = new ArrayList<>(map.entrySet());
		final ClassHierarchy hierarchy = reasoner.getClassHierarchy();
		
        Collections.sort(entries, (o1, o2) -> {
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
	public Set<S> getPositiveExamples(EvaluatedAxiom<T> evAxiom) {
		return getExamples(posExamplesQueryTemplate, evAxiom);
	}

	@SuppressWarnings("unchecked")
	public Set<S> getNegativeExamples(EvaluatedAxiom<T> evAxiom) {
		return getExamples(negExamplesQueryTemplate, evAxiom);
	}

	@SuppressWarnings("unchecked")
	protected Set<S> getExamples(ParameterizedSparqlString queryTemplate, EvaluatedAxiom<T> evAxiom) {
		ResultSet rs = executeSelectQuery(queryTemplate.toString());

		Set<OWLObject> negExamples = new TreeSet<>();

		while(rs.hasNext()){
			RDFNode node = rs.next().get("s");
			if(node.isResource()){
				negExamples.add(df.getOWLNamedIndividual(IRI.create(node.asResource().getURI())));
			} else if(node.isLiteral()){
				negExamples.add(convertLiteral(node.asLiteral()));
			}
		}
		return (Set<S>) negExamples;
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
	 * @param lit the JENA API literal
	 * @return the OWL API literal
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
		StringBuilder sb = new StringBuilder();
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
	
	protected <K,J extends Set<V>, V> void addToMap(Map<K, J> map, K key, V value ){
		J values = map.get(key);
		if(values == null){
			try {
				values = (J) value.getClass().newInstance();
				values.add(value);
			}
			catch (InstantiationException | IllegalAccessException e) {e.printStackTrace();return;}
		}
		values.add(value);
	}
	
	protected <K,J extends Set<V>, V> void addToMap(Map<K, J> map, K key, Collection<V> newValues ){
		J values = map.get(key);
		if(values == null){
			try {
				values = (J) newValues.getClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			map.put(key, values);
		}
		values.addAll(newValues);
	}
	
	@Autowired
	public void setKs(SparqlEndpointKS ks) {
		this.ks = ks;
	}
	
	class OWLFilter implements Predicate<OntClass> {

		@Override
		public boolean test(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(OWL2.getURI());
			}
			return false;
		}
	}
	
	class RDFSFilter implements Predicate<OntClass>{

		@Override
		public boolean test(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(RDFS.getURI());
			}
			return false;
		}
		
	}
	
	class RDFFilter implements Predicate<OntClass>{

		@Override
		public boolean test(OntClass cls) {
			if(!cls.isAnon()){
				return cls.getURI().startsWith(RDF.getURI());
			}
			return false;
		}
		
	}
	

}
