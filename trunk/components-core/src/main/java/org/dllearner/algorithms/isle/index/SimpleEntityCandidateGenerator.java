/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.isle.EntityCandidateGenerator;
import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public class SimpleEntityCandidateGenerator extends EntityCandidateGenerator{
	
	private Set<Entity> allEntities = new HashSet<Entity>();
	
	public SimpleEntityCandidateGenerator(OWLOntology ontology) {
		super(ontology);
		
		Set<OWLEntity> owlEntities = new HashSet<OWLEntity>();
		owlEntities.addAll(ontology.getClassesInSignature());
		owlEntities.addAll(ontology.getObjectPropertiesInSignature());
		owlEntities.addAll(ontology.getDataPropertiesInSignature());
		
		allEntities.addAll(OWLAPIConverter.getEntities(owlEntities));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.EntityCandidateGenerator#getCandidates(org.dllearner.algorithms.isle.index.Annotation)
	 */
	@Override
	public Set<Entity> getCandidates(Annotation annotation) {
		return allEntities;
	}

	@Override
	public HashMap<Annotation, Set<Entity>> getCandidatesMap(Set<Annotation> annotations) {
		HashMap<Annotation, Set<Entity>> result = new HashMap<Annotation, Set<Entity>>();
		for (Annotation annotation: annotations) 
			result.put(annotation, getCandidates(annotation));
		
		return result;
	}

}
