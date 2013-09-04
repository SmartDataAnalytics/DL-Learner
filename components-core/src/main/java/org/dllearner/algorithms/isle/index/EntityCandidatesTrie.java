package org.dllearner.algorithms.isle.index;

import java.util.Map.Entry;
import java.util.Set;

import org.dllearner.core.owl.Entity;
import org.dllearner.utilities.datastructures.PrefixTrie;

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
	 * Gets the longest matching string
	 * @param s
	 * @return
	 */
	public String getLongestMatch(String s);
	
	
}
