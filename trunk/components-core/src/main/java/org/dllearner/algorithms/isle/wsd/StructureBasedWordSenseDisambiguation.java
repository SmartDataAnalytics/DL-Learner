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

	/**
	 * @param ontology
	 */
	public StructureBasedWordSenseDisambiguation(OWLOntology ontology) {
		super(ontology);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.wsd.WordSenseDisambiguation#disambiguate(org.dllearner.algorithms.isle.index.Annotation, java.util.Set)
	 */
	@Override
	public SemanticAnnotation disambiguate(Annotation annotation, Set<Entity> candidateEntities) {
		//TODO we should find the sentence in which the annotated token is contained in
		String content = annotation.getReferencedDocument().getContent();
		for (Entity entity : candidateEntities) {
			Set<String> entityContext = StructuralEntityContext.getContextInNaturalLanguage(ontology, entity);
		}
		return null;
	}

}
