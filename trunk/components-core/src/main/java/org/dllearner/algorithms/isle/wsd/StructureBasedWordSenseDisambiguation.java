/**
 * 
 */
package org.dllearner.algorithms.isle.wsd;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithms.isle.StructuralEntityContext;
import org.dllearner.algorithms.isle.VSMCosineDocumentSimilarity;
import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.algorithms.isle.index.SemanticAnnotation;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class StructureBasedWordSenseDisambiguation extends WordSenseDisambiguation{

	private ContextExtractor contextExtractor;

	/**
	 * @param ontology
	 */
	public StructureBasedWordSenseDisambiguation(ContextExtractor contextExtractor, OWLOntology ontology) {
		super(ontology);
		this.contextExtractor = contextExtractor;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.wsd.WordSenseDisambiguation#disambiguate(org.dllearner.algorithms.isle.index.Annotation, java.util.Set)
	 */
	@Override
	public SemanticAnnotation disambiguate(Annotation annotation, Set<Entity> candidateEntities) {
		if(!candidateEntities.isEmpty()){
			//get the context of the annotated token
			List<String> tokenContext = contextExtractor.extractContext(annotation);
			
			//compare this context with the context of each entity candidate
			double maxScore = Double.NEGATIVE_INFINITY;
			Entity bestEntity = null;
			for (Entity entity : candidateEntities) {
				//get the context of the entity by analyzing the structure of the ontology
				Set<String> entityContext = StructuralEntityContext.getContextInNaturalLanguage(ontology, entity);
				//compute the VSM Cosine Similarity
				double score = computeScore(tokenContext, entityContext);
				//set best entity
				if(score > maxScore){
					maxScore = score;
					bestEntity = entity;
				}
			}
			
			return new SemanticAnnotation(annotation, bestEntity);
		}
		return null;
	}
	
	/**
	 * Compute the overlap between 2 set of words
	 * @param words1
	 * @param words2
	 * @return
	 */
	private double computeScoreSimple(Collection<String> words1, Collection<String> words2){
		return Sets.intersection(new HashSet<String>(words1), new HashSet<String>(words2)).size();
	}
	
	/**
	 * Compute the Cosine Similarity using as VSM.
	 * @param words1
	 * @param words2
	 */
	private double computeScore(Collection<String> words1, Collection<String> words2){
		double score = 0d;
		try {
			score = VSMCosineDocumentSimilarity.getCosineSimilarity(Joiner.on(" ").join(words1), Joiner.on(" ").join(words2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return score;
	}
}
