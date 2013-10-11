/**
 * 
 */
package org.dllearner.algorithms.isle.wsd;

import java.util.Set;

import org.dllearner.algorithms.isle.StructuralEntityContext;
import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.algorithms.isle.index.SemanticAnnotation;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

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
		//get the context of the annotated token
		Set<String> tokenContext = contextExtractor.extractContext(annotation.getToken(), annotation.getReferencedDocument().getContent());
		//compare this context with the context of each entity candidate
		for (Entity entity : candidateEntities) {
			Set<String> entityContext = StructuralEntityContext.getContextInNaturalLanguage(ontology, entity);
			
		}
		return null;
	}
}
