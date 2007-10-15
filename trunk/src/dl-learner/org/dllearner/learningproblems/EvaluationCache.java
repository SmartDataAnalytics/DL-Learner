/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.learningproblems;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.MultiConjunction;
import org.dllearner.core.dl.MultiDisjunction;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.Helper;

/**
 * Caches results of previous concept evaluation to speed up
 * further evaluations. Implements a fast evaluation approach,
 * which tries to infer the covered examples of a given concept
 * from previous results.
 * 
 * @todo Under construction.
 * @author Jens Lehmann
 *
 */
public class EvaluationCache {

	// maps a concept to a list of individuals it covers
	private Map<Concept,SortedSet<Individual>> cache;
	
	// concept comparator for concept indexing 
	// (so only logarithmic time complexity is needed to access a cached object)
	private ConceptComparator conceptComparator;
	
	private SortedSet<Individual> examples;
	
	public EvaluationCache(SortedSet<Individual> examples) {
		this.examples = examples;
		conceptComparator = new ConceptComparator();
		cache = new TreeMap<Concept,SortedSet<Individual>>(conceptComparator);
	}
	
	public void put(Concept concept, SortedSet<Individual> individuals) {
		cache.put(concept, individuals);
	}

	/**
	 * Determines which examples are instances of a concept.
	 * @param concept
	 * @return Set of inferred individuals or null if none were infered.
	 */
	public SortedSet<Individual> inferMember(Concept concept) {
		// return examples for atomic concepts if it is in the cache
		if(concept instanceof AtomicConcept)
			return cache.get(concept);
		// for a negation NOT C we can only say which concepts are not in it
		// (those in C), but we cannot say which ones are in NOT C
//		else if(concept instanceof Negation)
//			return Helper.difference(examples, inferPos(concept.getChild(0)));
		// for a conjunction we know that the intersection of instances
		// of all children belongs to the concept
		else if(concept instanceof MultiConjunction) {
			SortedSet<Individual> ret = inferMember(concept.getChild(0));
			for(int i=1; i<concept.getChildren().size(); i++) {
				ret = Helper.intersection(ret, inferMember(concept.getChild(i)));
			}
			return ret;
		// disjunctions are similar to conjunctions but we use union here;
		// note that there can be instances which are neither in a concept
		// C nor in a concept D, but in (C OR D)
		} else if(concept instanceof MultiDisjunction) {
			SortedSet<Individual> ret = inferMember(concept.getChild(0));
			for(int i=1; i<concept.getChildren().size(); i++) {
				ret = Helper.union(ret, inferMember(concept.getChild(i)));
			}
			return ret;
		}
		
		return new TreeSet<Individual>();
	}
	
	/**
	 * Determines which examples are not instance of a concept.
	 * @param concept
	 * @return
	 */
	public SortedSet<Individual> inferNonMember(Concept concept) {
		return null;
	}
}
