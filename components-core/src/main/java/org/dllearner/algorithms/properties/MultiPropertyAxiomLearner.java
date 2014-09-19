/**
 * 
 */
package org.dllearner.algorithms.properties;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.dllearner.algorithms.properties.AxiomAlgorithms.AxiomTypeCluster;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.hp.hpl.jena.query.ParameterizedSparqlString;

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

	private boolean multiThreaded = false;
	private int maxNrOfThreads = 2;
	
	private long maxExecutionTimeMilliseconds = -1;
	
	
	public MultiPropertyAxiomLearner(SparqlEndpointKS ks) {
		this.ks = ks;
	}
	
	public void generateAxioms(OWLEntity entity, Set<AxiomType<? extends OWLAxiom>> axiomTypes){
		
		EntityType<?> entityType = entity.getEntityType();
		
		// check for axiom types that are not for the given entity
		Set<AxiomType<? extends OWLAxiom>> possibleAxiomTypes = AxiomAlgorithms.getAxiomTypes(entityType);
		SetView<AxiomType<? extends OWLAxiom>> notAllowed = Sets.difference(axiomTypes, possibleAxiomTypes);
		if(!notAllowed.isEmpty()){
			logger.warn("Not supported axiom types for entity " + entity + " :" + notAllowed);
		}
		
		Set<AxiomType<? extends OWLAxiom>> todo = Sets.intersection(axiomTypes,  possibleAxiomTypes);
		
		// compute samples for axiom types
		if(useSampling){
			Set<AxiomTypeCluster> sampleClusters = AxiomAlgorithms.getSameSampleClusters(entityType);
			
			for (AxiomTypeCluster cluster : sampleClusters) {
				SetView<AxiomType<? extends OWLAxiom>> sampleAxiomTypes = Sets.intersection(cluster.getAxiomTypes(), todo);
				
				ParameterizedSparqlString sampleQuery = cluster.getSampleQuery();
				
				
			}
		}
		
		for (AxiomType<? extends OWLAxiom> axiomType : todo) {
			Class<? extends AbstractAxiomLearningAlgorithm<? extends OWLAxiom, ? extends OWLObject, ? extends OWLEntity>> algorithmClass = AxiomAlgorithms.getAlgorithmClass(axiomType);
			
			AbstractAxiomLearningAlgorithm learner = null;
			try {
				learner = (AbstractAxiomLearningAlgorithm)algorithmClass.getConstructor(
						SparqlEndpointKS.class).newInstance(ks);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			learner.setEntityToDescribe(entity);
			
		}
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
	

}
