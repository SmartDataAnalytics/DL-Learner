package org.dllearner.algorithms.isle.index;

import org.dllearner.core.owl.Entity;

import java.util.List;
import java.util.Set;

public interface EntityCandidatesTrie {
	
	/**
	 * Adds an entity to the set of candidates of a string
	 * @param s
	 * @param e
	 */
	public void addEntry(List<Token> s, Entity e);
	
	
	/**
	 * Gets set of candidate entities for a list of tokens
	 * @return
	 */
	public Set<EntityScorePair> getCandidateEntities(List<Token> tokens);


	/**
	 * Returns the string on which this entry is based on. This is used e.g. for storing the original
     * ontology string when the parameter string has been added to the trie after generation by using
     * WordNet or other additional methods.
     *
	 * @return string generating the path of the longest match in the trie
	 */
	public List<Token> getGeneratingStringForLongestMatch(List<Token> tokens);

    /**
     * Gets the longest matching string
     * @return
     */
    public List<Token> getLongestMatchingText(List<Token> tokens);
}
