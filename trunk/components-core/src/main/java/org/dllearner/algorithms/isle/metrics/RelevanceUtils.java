/**
 * 
 */
package org.dllearner.algorithms.isle.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public class RelevanceUtils {
	
	
	private static final Logger logger = Logger.getLogger(RelevanceUtils.class.getName());
	
	public static Map<Entity, Double> getRelevantEntities(Entity entity, Set<Entity> otherEntities, RelevanceMetric metric){
		Map<Entity, Double> relevantEntities = new HashMap<Entity, Double>();
		
		for (Entity otherEntity : otherEntities) {
			double relevance = metric.getRelevance(entity, otherEntity);
			relevantEntities.put(otherEntity, relevance);
		}
		
		return relevantEntities;
	}
	
	public static Map<Entity, Double> getRelevantEntities(Entity entity, OWLOntology ontology, RelevanceMetric metric){
		logger.info("Get relevant entities for " + entity);
		Map<Entity, Double> relevantEntities = new HashMap<Entity, Double>();
		
		Set<OWLEntity> owlEntities = new HashSet<OWLEntity>();
		owlEntities.addAll(ontology.getClassesInSignature());
		owlEntities.addAll(ontology.getDataPropertiesInSignature());
		owlEntities.addAll(ontology.getObjectPropertiesInSignature());
		Set<Entity> otherEntities = OWLAPIConverter.getEntities(owlEntities);
		
		otherEntities.remove(entity);
		for (Entity otherEntity : otherEntities) {
			double relevance = metric.getNormalizedRelevance(entity, otherEntity);
			logger.info(otherEntity + ":" + relevance);
			relevantEntities.put(otherEntity, relevance);
		}
		
		return relevantEntities;
	}

}
