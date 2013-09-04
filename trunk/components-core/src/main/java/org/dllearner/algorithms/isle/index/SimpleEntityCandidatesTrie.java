package org.dllearner.algorithms.isle.index;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.datastructures.PrefixTrie;
import org.semanticweb.owlapi.model.OWLOntology;

public class SimpleEntityCandidatesTrie implements EntityCandidatesTrie {

	PrefixTrie<Set<Entity>> trie;
	OWLOntology ontology;
	
	public SimpleEntityCandidatesTrie(OWLOntology ontology) {
		this.ontology = ontology;
		this.trie = new PrefixTrie<Set<Entity>>();
	}
	
	@Override
	public void addEntry(String s, Entity e) {
		Set<Entity> candidates = trie.get(s);
		if (candidates==null)
			candidates = new HashSet<Entity>();
		
		candidates.add(e);
	}

	@Override
	public Set<Entity> getCandidateEntities(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLongestMatch(String s) {
		return trie.getLongestMatch(s).toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
