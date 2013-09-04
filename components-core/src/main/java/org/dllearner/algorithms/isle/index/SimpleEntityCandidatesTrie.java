package org.dllearner.algorithms.isle.index;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dllearner.algorithms.isle.textretrieval.AnnotationEntityTextRetriever;
import org.dllearner.algorithms.isle.textretrieval.EntityTextRetriever;
import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.datastructures.PrefixTrie;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class SimpleEntityCandidatesTrie implements EntityCandidatesTrie {

	PrefixTrie<Set<Entity>> trie;
	EntityTextRetriever entityTextRetriever;
	
	public SimpleEntityCandidatesTrie(EntityTextRetriever entityTextRetriever) {
		this.entityTextRetriever = entityTextRetriever;
		this.trie = new PrefixTrie<Set<Entity>>();
	}
	
	public SimpleEntityCandidatesTrie(EntityTextRetriever entityTextRetriever, OWLOntology ontology) {
		this(entityTextRetriever);
		buildTrie(ontology);
	}
	
	public void buildTrie(OWLOntology ontology) {		
		Map<Entity, Set<String>> relevantText = entityTextRetriever.getRelevantText(ontology);
		
		for (Entity entity : relevantText.keySet()) {
			for (String text : relevantText.get(entity)) {
				addEntry(text, entity);
				// Adds also composing words, e.g. for "has child", "has" and "child" are also added
				if (text.contains(" ")) {
					for (String subtext : text.split(" ")) {
						addEntry(subtext, entity);
						//System.out.println("trie.add("+subtext+","++")");
					}
				}
			}
		}
	}
	
	@Override
	public void addEntry(String s, Entity e) {
		Set<Entity> candidates = trie.get(s);
		if (candidates==null)
			candidates = new HashSet<Entity>();
		
		candidates.add(e);
		
		trie.put(s, candidates);
	}

	@Override
	public Set<Entity> getCandidateEntities(String s) {
		return trie.get(s);
	}

	@Override
	public String getLongestMatch(String s) {
		return trie.getLongestMatch(s).toString();
	}

}
