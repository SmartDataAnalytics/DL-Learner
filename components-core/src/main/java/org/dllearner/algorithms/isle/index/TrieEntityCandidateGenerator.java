package org.dllearner.algorithms.isle.index;

import java.util.Set;

import org.dllearner.algorithms.isle.EntityCandidateGenerator;
import org.dllearner.core.owl.Entity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Generates candidates using a entity candidates prefix trie
 * @author Andre Melo
 *
 */
public class TrieEntityCandidateGenerator extends EntityCandidateGenerator{

	EntityCandidatesTrie candidatesTrie;
	
	public TrieEntityCandidateGenerator(OWLOntology ontology, EntityCandidatesTrie candidatesTrie) {
		super(ontology);
		this.candidatesTrie = candidatesTrie;
	}
	
	public Set<Entity> getCandidates(Annotation annotation) {
		return candidatesTrie.getCandidateEntities(annotation.getToken());
	}

}
