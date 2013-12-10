/**
 * 
 */
package org.dllearner.algorithms.isle;

import org.dllearner.algorithms.isle.index.Annotation;
import org.dllearner.algorithms.isle.index.EntityScorePair;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashMap;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class EntityCandidateGenerator {
	
	private OWLOntology ontology;

	public EntityCandidateGenerator(OWLOntology ontology) {
		this.ontology = ontology;
	}

	public abstract Set<EntityScorePair> getCandidates(Annotation annotation);
	

	public abstract HashMap<Annotation,Set<EntityScorePair>> getCandidatesMap(Set<Annotation> annotations);
}
