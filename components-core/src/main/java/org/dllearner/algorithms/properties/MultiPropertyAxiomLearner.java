/**
 * 
 */
package org.dllearner.algorithms.properties;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.algorithms.properties.AxiomAlgorithms.AxiomTypeCluster;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.AxiomLearningProgressMonitor;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
	
	private boolean useSampling = true;
	private long pageSize = 10000;

	private boolean multiThreaded = true;
	private int maxNrOfThreads = 4;
	
	private long maxExecutionTimeMilliseconds = -1;

	private QueryExecutionFactory qef;

	private long startTime;

	private AxiomLearningProgressMonitor monitor;
	
	public MultiPropertyAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;
		
		qef = new QueryExecutionFactoryHttp(ks.getEndpoint().getURL().toString(), ks.getEndpoint().getDefaultGraphURIs());
	}
	
	public MultiPropertyAxiomLearner(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	public void setProgressMonitor(AxiomLearningProgressMonitor monitor){
		this.monitor = monitor;
	}
	
	public Map<AxiomType<? extends OWLAxiom>, List<EvaluatedAxiom<OWLAxiom>>> generateAxioms(final OWLEntity entity, Set<AxiomType<? extends OWLAxiom>> axiomTypes){
		startTime = System.currentTimeMillis();
		
		final Map<AxiomType<? extends OWLAxiom>, List<EvaluatedAxiom<OWLAxiom>>> results = Maps.newConcurrentMap();
		
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
						if(useSampling){
							Model sample = generateSample(entity, cluster);
							ks = new LocalModelBasedSparqlEndpointKS(sample);
						}
						
						for (AxiomType<? extends OWLAxiom> axiomType : sampleAxiomTypes) {
							try {
								List<EvaluatedAxiom<OWLAxiom>> result = applyAlgorithm(entity, axiomType, ks);
								results.put(axiomType, result);
							} catch (Exception e) {
								logger.error("Error occurred while generating " + axiomType.getName() + " for entity " + entity, e);
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
		
		return results;
	}
	
	private List<EvaluatedAxiom<OWLAxiom>> applyAlgorithm(OWLEntity entity, AxiomType<? extends OWLAxiom> axiomType, SparqlEndpointKS ks) throws ComponentInitException{
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
		learner.setProgressMonitor(monitor);
		learner.init();
		learner.start();
		
		return learner.getCurrentlyBestEvaluatedAxioms();
	}
	
	private Model generateSample(OWLEntity entity, AxiomTypeCluster cluster){
		logger.info("Generating sample...");
		Model sample = ModelFactory.createDefaultModel();
		
		ParameterizedSparqlString sampleQueryTemplate = cluster.getSampleQuery();
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
		logger.info("...done. Sample size:" + sample.size() + " triples");
		return sample;
	}
	
	private boolean isTimeout(){
		return maxExecutionTimeMilliseconds <= 0 ? false : getRemainingRuntimeMilliSeconds() <= 0;
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
	 * @param maxNrOfThreads the maxNrOfThreads to set
	 */
	public void setMaxNrOfThreads(int maxNrOfThreads) {
		this.maxNrOfThreads = maxNrOfThreads;
	}
	
	/**
	 * @param maxExecutionTimeMilliseconds the maxExecutionTimeMilliseconds to set
	 */
	public void setMaxExecutionTime(long executionTimeDuration, TimeUnit executionTimeUnit) {
		this.maxExecutionTimeMilliseconds = executionTimeUnit.toMillis(executionTimeDuration);
	}
	
	public static void main(String[] args) throws Exception{
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://linkedspending.aksw.org/sparql"), "http://dbpedia.org");
//		endpoint = SparqlEndpoint.getEndpointDBpedia();
		MultiPropertyAxiomLearner la = new MultiPropertyAxiomLearner(new SparqlEndpointKS(endpoint));
		OWLEntity entity = new OWLObjectPropertyImpl(IRI.create("http://dbpedia.org/ontology/routeEnd"));
		la.generateAxioms(
				entity, 
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(AxiomType.OBJECT_PROPERTY_DOMAIN, 
						AxiomType.OBJECT_PROPERTY_RANGE, 
						AxiomType.FUNCTIONAL_OBJECT_PROPERTY, AxiomType.ASYMMETRIC_OBJECT_PROPERTY, AxiomType.IRREFLEXIVE_OBJECT_PROPERTY,
						AxiomType.INVERSE_OBJECT_PROPERTIES));
	}

}
