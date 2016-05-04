package org.dllearner.algorithms.isle.index;

import org.dllearner.algorithms.isle.EntityCandidateGenerator;
import org.dllearner.algorithms.isle.wsd.WordSenseDisambiguation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides methods to annotate documents.
 *
 * @author Daniel Fleischhacker
 */
public class SemanticAnnotator {
	
	private WordSenseDisambiguation wordSenseDisambiguation;
	private EntityCandidateGenerator entityCandidateGenerator;
	private LinguisticAnnotator linguisticAnnotator;
	

    /**
     * Initialize this semantic annotator to use the entities from the provided ontology.
     *
     */
    public SemanticAnnotator(WordSenseDisambiguation wordSenseDisambiguation, 
    		EntityCandidateGenerator entityCandidateGenerator, LinguisticAnnotator linguisticAnnotator) {
		this.wordSenseDisambiguation = wordSenseDisambiguation;
		this.entityCandidateGenerator = entityCandidateGenerator;
		this.linguisticAnnotator = linguisticAnnotator;
    }

    /**
     * Processes the given document and returns the annotated version of this document.
     *
     * @param document the document to annotate
     * @return the given document extended with annotations
     */
    public AnnotatedDocument processDocument(TextDocument document){
    	Set<Annotation> annotations = linguisticAnnotator.annotate(document);
    	Set<SemanticAnnotation> semanticAnnotations = new HashSet<>();
    	HashMap<Annotation, Set<EntityScorePair>> candidatesMap = entityCandidateGenerator.getCandidatesMap(annotations);
    	for (Annotation annotation : candidatesMap.keySet()) {
    		Set<EntityScorePair> candidateEntities = candidatesMap.get(annotation);
            if (candidateEntities == null || candidateEntities.size() == 0) {
                continue;
            }
            SemanticAnnotation semanticAnnotation = wordSenseDisambiguation.disambiguate(annotation, candidateEntities);
    		if(semanticAnnotation != null){
    			semanticAnnotations.add(semanticAnnotation);
    		}
		}
    	AnnotatedDocument annotatedDocument = new AnnotatedTextDocument(document, semanticAnnotations);
    	return annotatedDocument;
    }
}
