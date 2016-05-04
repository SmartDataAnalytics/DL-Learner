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
package org.dllearner.algorithms.properties;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.algorithms.properties.AxiomAlgorithms.AxiomTypeCluster;
import org.dllearner.core.*;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This is a wrapper class to handle more than one property axiom type in a more intelligent way,
 * e.g. when sampling is activated it might be better to generate a unique sample and apply the 
 * algorithms on that sample afterwards.
 * Note that this only works for subsets of axiom types that have the same sample structure.
 * </br>
 * Additionally, this class is able to configure and run the algorithms in a parallel way.
 * @author Lorenz Buehmann
 *
 */
public class MultiPropertyAxiomLearner {
	
	private static final Logger logger = LoggerFactory.getLogger(MultiPropertyAxiomLearner.class);
	
	private SparqlEndpointKS ks;
	private SPARQLReasoner reasoner;
	
	private boolean useSampling = false;
	private long pageSize = 10000;

	private boolean multiThreaded = true;
	private int maxNrOfThreads = 1;
	
	private long maxExecutionTimeMilliseconds = -1;

	private QueryExecutionFactory qef;

	private long startTime;

	private AxiomLearningProgressMonitor progressMonitor = new SilentAxiomLearningProgressMonitor();
	
	private Map<AxiomType<? extends OWLAxiom>, List<EvaluatedAxiom<OWLAxiom>>> results;

	private OWLEntity entity;

	private Set<AxiomType<? extends OWLAxiom>> axiomTypes;
	
	private Map<AxiomType<? extends OWLAxiom>, AbstractAxiomLearningAlgorithm> algorithms = new HashMap<>();
	
	public MultiPropertyAxiomLearner(SparqlEndpointKS ks) {
		this(ks.getQueryExecutionFactory());
		this.ks = ks;
	}
	
	public MultiPropertyAxiomLearner(QueryExecutionFactory qef) {
		this.qef = qef;
		this.reasoner = new SPARQLReasoner(qef);
	}
	
	public void setProgressMonitor(AxiomLearningProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}
	
	public void setEntityToDescribe(OWLEntity entity){
		this.entity = entity;
	}
	
	public void setAxiomTypes(Set<AxiomType<? extends OWLAxiom>> axiomTypes){
		this.axiomTypes = axiomTypes;
	}
	
	public void start(){
		startTime = System.currentTimeMillis();
		
		//check if entity is empty
		int popularity = reasoner.getPopularity(entity);
		if(popularity == 0){
			logger.warn("Cannot make axiom suggestions for empty " + entity.getEntityType().getName() + " " + entity.toStringID());
			return;
		}
		
		results = Maps.newConcurrentMap();
		
		EntityType<?> entityType = entity.getEntityType();
		
		// check for axiom types that are not for the given entity
		Set<AxiomType<? extends OWLAxiom>> possibleAxiomTypes = AxiomAlgorithms.getAxiomTypes(entityType);
		SetView<AxiomType<? extends OWLAxiom>> notAllowed = Sets.difference(axiomTypes, possibleAxiomTypes);
		if(!notAllowed.isEmpty()){
			logger.warn("Not supported axiom types for entity " + entity + " :" + notAllowed);
		}
		
		Set<AxiomType<? extends OWLAxiom>> todo = Sets.intersection(axiomTypes,  possibleAxiomTypes);
		
		// compute samples for axiom types
		Set<AxiomTypeCluster> sampleClusters = AxiomAlgorithms.getSameSampleClusters(entityType);
		
		ExecutorService tp = Executors.newFixedThreadPool(maxNrOfThreads);
		
		for (final AxiomTypeCluster cluster : sampleClusters) {
			final SetView<AxiomType<? extends OWLAxiom>> sampleAxiomTypes = Sets.intersection(cluster.getAxiomTypes(), todo);
			
			if(!sampleAxiomTypes.isEmpty()){
				tp.submit(new Runnable() {
					
					@Override
					public void run() {
						SparqlEndpointKS ks = MultiPropertyAxiomLearner.this.ks;
						
						// get sample if enabled
						if(useSampling){
							Model sample = generateSample(entity, cluster);
							ks = new LocalModelBasedSparqlEndpointKS(sample);
						}
						
						// process each axiom type
						for (AxiomType<? extends OWLAxiom> axiomType : sampleAxiomTypes) {
							try {
								List<EvaluatedAxiom<OWLAxiom>> result = applyAlgorithm(axiomType, ks);
								results.put(axiomType, result);
							} catch (Exception e) {
								logger.error("An error occurred while generating " + axiomType.getName() + 
										" axioms for " + OWLAPIUtils.getPrintName(entity.getEntityType()) + " " + entity.toStringID(), e);
							}
						}
					}
				});
				
			}
		}
		
		try {
			tp.shutdown();
			tp.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		
//		for (AxiomType<? extends OWLAxiom> axiomType : todo) {
//			try {
//				applyAlgorithm(entity, axiomType, useSampling ? axiomType2Ks.get(axiomType) : ks);
//			} catch (Exception e) {
//				logger.error("Error occurred while generating " + axiomType.getName() + " for entity " + entity, e);
//			}
//		}
	}
	
	public Map<AxiomType<? extends OWLAxiom>, List<EvaluatedAxiom<OWLAxiom>>> getCurrentlyBestEvaluatedAxioms() {
		return results;
	}
	
	public List<EvaluatedAxiom<OWLAxiom>> getCurrentlyBestEvaluatedAxioms(AxiomType<? extends OWLAxiom> axiomType) {
		return new ArrayList<>(results.get(axiomType));
	}

	public List<EvaluatedAxiom<OWLAxiom>> getCurrentlyBestEvaluatedAxioms(AxiomType<? extends OWLAxiom> axiomType, double accuracyThreshold) {
		List<EvaluatedAxiom<OWLAxiom>> result = results.get(axiomType);
		
		// throw exception if computation failed
		if(result == null) {
			return Collections.emptyList();
//			throw new NoResult
		}
		
		// get all axioms above threshold
		List<EvaluatedAxiom<OWLAxiom>> bestAxioms = new ArrayList<>();
		for (EvaluatedAxiom<OWLAxiom> axiom : result) {
			if(axiom.getScore().getAccuracy() >= accuracyThreshold){
				bestAxioms.add(axiom);
			}
		}
		
		return bestAxioms;
	}
	
	public AbstractAxiomLearningAlgorithm getAlgorithm(AxiomType<? extends OWLAxiom> axiomType) {
		return algorithms.get(axiomType);
	}
	
	private List<EvaluatedAxiom<OWLAxiom>> applyAlgorithm(AxiomType<? extends OWLAxiom> axiomType, SparqlEndpointKS ks) throws ComponentInitException{
		Class<? extends AbstractAxiomLearningAlgorithm<? extends OWLAxiom, ? extends OWLObject, ? extends OWLEntity>> algorithmClass = AxiomAlgorithms.getAlgorithmClass(axiomType);
		AbstractAxiomLearningAlgorithm learner = null;
		try {
			learner = (AbstractAxiomLearningAlgorithm)algorithmClass.getConstructor(
					SparqlEndpointKS.class).newInstance(ks);
		} catch (Exception e) {
			e.printStackTrace();
		}
		learner.setEntityToDescribe(entity);
		learner.setUseSampling(false);
		learner.setProgressMonitor(progressMonitor);
		learner.init();
		learner.start();
		
		algorithms.put(axiomType, learner);
		
		return learner.getCurrentlyBestEvaluatedAxioms();
	}
	
	public Set<OWLObject> getPositives(AxiomType<? extends OWLAxiom> axiomType, EvaluatedAxiom<OWLAxiom> axiom){
		AbstractAxiomLearningAlgorithm la = algorithms.get(axiomType);
		return la.getPositiveExamples(axiom);
	}

	public Set<OWLObject> getNegatives(AxiomType<? extends OWLAxiom> axiomType, EvaluatedAxiom<OWLAxiom> axiom){
		AbstractAxiomLearningAlgorithm la = algorithms.get(axiomType);
		return la.getNegativeExamples(axiom);
	}
	
	private Model generateSample(OWLEntity entity, AxiomTypeCluster cluster){
		logger.info("Generating sample for " + OWLAPIUtils.getPrintName(entity.getEntityType()) + " " + entity.toStringID() + "...");
		Model sample = ModelFactory.createDefaultModel();
		
		ParameterizedSparqlString sampleQueryTemplate = cluster.getSampleQuery();
		sampleQueryTemplate.clearParam("entity");
		sampleQueryTemplate.setIri("entity", entity.toStringID());
		
		Query query = sampleQueryTemplate.asQuery();
		query.setLimit(pageSize);
		
		boolean isEmpty = false;
		int i = 0;
		while(!isTimeout() && !isEmpty){
			// get next sample
			logger.debug("Extending sample...");
			query.setOffset(i++ * pageSize);
			QueryExecution qe = qef.createQueryExecution(query);
			Model tmp = qe.execConstruct();
			sample.add(tmp);
			
			// if last call returned empty model, we can leave loop
			isEmpty = tmp.isEmpty();
		}
		logger.info("Finished generating sample. Sample size: " + sample.size() + " triples");
		return sample;
	}
	
	private boolean isTimeout(){
		return maxExecutionTimeMilliseconds > 0 && getRemainingRuntimeMilliSeconds() <= 0;
	}
	
	private long getRemainingRuntimeMilliSeconds(){
		long duration = System.currentTimeMillis() - startTime;
		return Math.max(0, (maxExecutionTimeMilliseconds - duration));
	}
	
	/**
	 * @param useSampling the useSampling to set
	 */
	public void setUseSampling(boolean useSampling) {
		this.useSampling = useSampling;
	}
	
	/**
	 * @param multiThreaded the multiThreaded to set
	 */
	public void setMultiThreaded(boolean multiThreaded) {
		this.multiThreaded = multiThreaded;
	}
	
	/**
	 * @param maxNrOfThreads the max. nr of threads
	 */
	public void setMaxNrOfThreads(int maxNrOfThreads) {
		this.maxNrOfThreads = maxNrOfThreads;
	}
	
	/**
	 * Set the maximum execution time.
	 * @param executionTimeDuration the execution time
	 * @param executionTimeUnit the time unit
	 */
	public void setMaxExecutionTime(long executionTimeDuration, TimeUnit executionTimeUnit) {
		this.maxExecutionTimeMilliseconds = executionTimeUnit.toMillis(executionTimeDuration);
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://dbpedia.org/sparql"), "http://dbpedia.org");
//		endpoint = SparqlEndpoint.getEndpointDBpedia();
		MultiPropertyAxiomLearner la = new MultiPropertyAxiomLearner(new SparqlEndpointKS(endpoint));
		OWLEntity entity = new OWLObjectPropertyImpl(IRI.create("http://dbpedia.org/ontology/league"));
		la.setEntityToDescribe(entity);
		la.setAxiomTypes(Sets.<AxiomType<? extends OWLAxiom>>newHashSet(AxiomType.OBJECT_PROPERTY_DOMAIN, 
				AxiomType.OBJECT_PROPERTY_RANGE, 
				AxiomType.FUNCTIONAL_OBJECT_PROPERTY, AxiomType.ASYMMETRIC_OBJECT_PROPERTY, AxiomType.IRREFLEXIVE_OBJECT_PROPERTY,
				AxiomType.INVERSE_OBJECT_PROPERTIES));
		la.setMaxExecutionTime(1, TimeUnit.MINUTES);
		la.start();
		
	}

}
