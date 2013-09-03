/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.util.Set;

import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class EntityCandidateGenerator {
	
	private OWLOntology ontology;

	public EntityCandidateGenerator(OWLOntology ontology) {
		this.ontology = ontology;
	}

	public abstract Set<Entity> getCandidates(Annotation annotation);
}
