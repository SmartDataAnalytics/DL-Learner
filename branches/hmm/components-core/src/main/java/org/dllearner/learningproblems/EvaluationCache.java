/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.learningproblems;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Union;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.owl.ConceptComparator;

/**
 * Caches results of previous concept evaluation to speed up
 * further evaluations. Implements a fast evaluation approach,
 * which tries to infer the covered examples of a given concept
 * from previous results.
 * 
 * TODO Under construction.
 * @author Jens Lehmann
 *
 */
public class EvaluationCache {

	// maps a concept to a list of individuals it covers
	private Map<Description,SortedSet<Individual>> cache;
	private boolean checkForEqualConcepts = false;
	
	// concept comparator for concept indexing 
	// (so only logarithmic time complexity is needed to access a cached object)
	private ConceptComparator conceptComparator;
	
	private SortedSet<Individual> examples;
	
	public EvaluationCache(SortedSet<Individual> examples) {
		this.examples = examples;
		conceptComparator = new ConceptComparator();
		cache = new TreeMap<Description,SortedSet<Individual>>(conceptComparator);
	}
	
	public void put(Description concept, SortedSet<Individual> individuals) {
		cache.put(concept, individuals);
	}

	/**
	 * Determines which examples are instances of a concept.
	 * @param concept
	 * @return A tuple of two sets, where the first element is the
	 * set of individuals belonging to the class and the second element
	 * is the set of individuals not belonging to the class. For all
	 * elements, which are in neither of the sets, the cache cannot
	 * safely determine whether they are concept instances or not.
	 */
	public SortedSetTuple<Individual> infer(Description concept) {
		if(checkForEqualConcepts) {
			SortedSet<Individual> pos = cache.get(concept);
			SortedSet<Individual> neg = Helper.difference(examples, pos);
			return new SortedSetTuple<Individual>(pos,neg);
		} else {
			// for a negation NOT C we can only say which concepts are not in it
			// (those in C), but we cannot say which ones are in NOT C
			
			// for a conjunction we know that the intersection of instances
			// of all children belongs to the concept			
			if(concept instanceof Intersection) {
				handleMultiConjunction((Intersection)concept);
			// disjunctions are similar to conjunctions but we use union here;
			// note that there can be instances which are neither in a concept
			// C nor in a concept D, but in (C OR D)				
			} else if(concept instanceof Union) {
				SortedSet<Individual> ret = cache.get(concept.getChild(0));
				for(int i=1; i<concept.getChildren().size(); i++) {
					ret = Helper.union(ret, cache.get(concept.getChild(i)));
				}
			// in all other cases we cannot infer anything, so we return an
			// empty tuple
			} else {
				return new SortedSetTuple<Individual>();
			}
		}
		
		return null;
	}
	
	private SortedSetTuple<Individual> handleMultiConjunction(Intersection mc) {
		Set<Individual> pos = cache.get(mc.getChild(0));
		for(int i=1; i<mc.getChildren().size(); i++) {
			pos = Helper.intersection(pos, cache.get(mc.getChild(i)));
		}		
		// TODO: handle the case that some children may not be in cache
		return null;
	}
}
