package org.dllearner.algorithms.isle.index;

import java.util.Map.Entry;
import java.util.Set;

import org.dllearner.core.owl.Entity;

public interface EntityCandidatesTrie {

	/**
	 * Adds an entry to the trie. If string already existent, adds to entity to its set of candidates
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
	 * Gets longest matching string and its candidate entities
	 * @param s
	 * @return
	 */
	public Entry<String,Set<Entity>> getLongestMatchWithCandidates(String s);
	
	/**
	 * Gets the longest matching string
	 * @param s
	 * @return
	 */
	public String getLongestMatch(String s);
	
	
}
