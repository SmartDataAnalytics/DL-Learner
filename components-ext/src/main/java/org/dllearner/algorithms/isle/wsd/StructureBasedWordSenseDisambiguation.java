/**
 * 
 */
package org.dllearner.algorithms.isle.wsd;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dllearner.algorithms.isle.StructuralEntityContext;
import org.dllearner.algorithms.isle.VSMCosineDocumentSimilarity;
import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.algorithms.isle.index.EntityScorePair;
import org.dllearner.algorithms.isle.index.SemanticAnnotation;
import org.dllearner.algorithms.isle.index.Token;
import org.dllearner.algorithms.isle.textretrieval.AnnotationEntityTextRetriever;
import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class StructureBasedWordSenseDisambiguation extends WordSenseDisambiguation{

	private ContextExtractor contextExtractor;
	private AnnotationEntityTextRetriever textRetriever;

	/**
	 * @param ontology
	 */
	public StructureBasedWordSenseDisambiguation(ContextExtractor contextExtractor, OWLOntology ontology) {
		super(ontology);
		this.contextExtractor = contextExtractor;
		
		textRetriever = new RDFSLabelEntityTextRetriever(ontology);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.wsd.WordSenseDisambiguation#disambiguate(org.dllearner.algorithms.isle.index.Annotation, java.util.Set)
	 */
	@Override
	public SemanticAnnotation disambiguate(Annotation annotation, Set<EntityScorePair> candidateEntities) {
		//filter out candidates for which the head noun does not match with the annotated token
		for (Iterator<EntityScorePair> iterator = candidateEntities.iterator(); iterator.hasNext();) {
			EntityScorePair entityPair = iterator.next();
			OWLEntity entity = entityPair.getEntity();
			
			Map<List<Token>, Double> relevantText = textRetriever.getRelevantText(entity);
			
			boolean matched = false;
			
			for (Entry<List<Token>, Double> entry : relevantText.entrySet()) {
				List<Token> tokens = entry.getKey();
				
				
				for (Token token : tokens) {
					if(token.isHead()){
						for (Token annotatedToken : annotation.getTokens()) {
							if(token.getRawForm().equals(annotatedToken.getRawForm())){
								matched = true;
							}
						}
					}
				}
				
			}
			
			if(!matched){
				iterator.remove();
			}
		}
		
		System.out.println(annotation);
		for (EntityScorePair entityScorePair : candidateEntities) {
			System.out.println(entityScorePair);
		}
		
		if(!candidateEntities.isEmpty()){
			//get the context of the annotated token
			List<String> tokenContext = contextExtractor.extractContext(annotation);
			
			//compare this context with the context of each entity candidate
			double maxScore = Double.NEGATIVE_INFINITY;
			OWLEntity bestEntity = null;
			for (EntityScorePair entityScorePair : candidateEntities) {
				OWLEntity entity = entityScorePair.getEntity();
                //get the context of the entity by analyzing the structure of the ontology
                Set<String> entityContext = StructuralEntityContext.getContextInNaturalLanguage(ontology, entity);
                //compute the VSM Cosine Similarity
                double score = computeScore(tokenContext, entityContext);
                //set best entity
                if (score > maxScore) {
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
		return Sets.intersection(new HashSet<>(words1), new HashSet<>(words2)).size();
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
	
	public static void main(String[] args) throws Exception {
		String s = "OWLEntity";
		System.out.println(s.replace("^(OWL)Entity", "OWLEntity"));
	}
}
