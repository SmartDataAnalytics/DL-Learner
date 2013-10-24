package org.dllearner.algorithms.isle.index;

import org.dllearner.core.owl.Entity;

import java.util.Set;

public interface EntityCandidatesTrie {
	
	/**
	 * Adds an entity to the set of candidates of a string
	 * @param s
	 * @param e
	 */
	public void addEntry(String s, Entity e);
	
	
	/**
	 * Gets set of candidate entities for an exact given String
	 * @param s
	 * @return
	 */
	public Set<Entity> getCandidateEntities(String s);


	/**
	 * Returns the string on which this entry is based on. This is used e.g. for storing the original
     * ontology string when the parameter string has been added to the trie after generation by using
     * WordNet or other additional methods.
     *
	 * @param s the string to search in the trie
	 * @return string generating the path of the longest match in the trie
	 */
	public String getGeneratingStringForLongestMatch(String s);

    /**
     * Gets the longest matching string
     * @param s
     * @return
     */
    public String getLongestMatchingText(String s);
}
