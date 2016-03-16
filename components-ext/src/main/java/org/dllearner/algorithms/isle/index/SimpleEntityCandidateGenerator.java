/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.isle.EntityCandidateGenerator;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public class SimpleEntityCandidateGenerator extends EntityCandidateGenerator{
	
	private Set<OWLEntity> allEntities = new HashSet<>();
	
	public SimpleEntityCandidateGenerator(OWLOntology ontology) {
		super(ontology);
		
		allEntities.addAll(ontology.getClassesInSignature());
		allEntities.addAll(ontology.getObjectPropertiesInSignature());
		allEntities.addAll(ontology.getDataPropertiesInSignature());
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.EntityCandidateGenerator#getCandidates(org.dllearner.algorithms.isle.index.Annotation)
	 */
	@Override
	public Set<EntityScorePair> getCandidates(Annotation annotation) {
        HashSet<EntityScorePair> result = new HashSet<>();
        for (OWLEntity e : allEntities) {
            result.add(new EntityScorePair(e, 1.0));
        }
        return result;
    }

	@Override
	public HashMap<Annotation, Set<EntityScorePair>> getCandidatesMap(Set<Annotation> annotations) {
		HashMap<Annotation, Set<EntityScorePair>> result = new HashMap<>();
		for (Annotation annotation: annotations) 
			result.put(annotation, getCandidates(annotation));
		
		return result;
	}

}
