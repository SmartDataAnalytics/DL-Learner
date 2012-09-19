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

package org.dllearner.utilities.learn;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;

/**
 * This class takes a reasoner and individuals as input and detects
 * the relevant (wrt. a learning process) classes and properties
 * at a certain distance of the examples.
 * 
 * @author Jens Lehmann
 *
 */
public class UsedEntitiesDetection {

	Comparator<Set<ObjectProperty>> keyComp = new Comparator<Set<ObjectProperty>>() {

		@Override
		public int compare(Set<ObjectProperty> key1, Set<ObjectProperty> key2) {
			// first criterion: size of key
			int sizeDiff = key1.size() - key2.size();
			if(sizeDiff == 0) {
				Iterator<ObjectProperty> it1 = key1.iterator();
				Iterator<ObjectProperty> it2 = key2.iterator();
				// compare elements one by one (assumes that both use the same
				// ordering, which is the case)
				while(it1.hasNext()) {
					ObjectProperty prop1 = it1.next();
					ObjectProperty prop2 = it2.next();
					int comp = prop1.compareTo(prop2);
					if(comp != 0) {
						return comp;
					}
				}
				// all elements of the set are equal
				return 0;
			} else {
				return sizeDiff;
			}
		}
		
	};
	
	private Map<Set<ObjectProperty>,Set<NamedClass>> usedClasses;
	
	private Map<Set<ObjectProperty>,Set<ObjectProperty>> usedObjectProperties;
	
	private AbstractReasonerComponent reasoner;
	private int maxDepth;
	
	/**
	 * Computes used properties in classes. 
	 * TODO more explanation
	 * 
	 * @param reasoner A reasoner.
	 * @param individuals A set of individuals to start from.
	 * @param depth The maximum depth for the search.
	 */
	public UsedEntitiesDetection(AbstractReasonerComponent reasoner, Set<Individual> individuals, int maxDepth) {
		this.reasoner = reasoner;
		this.maxDepth = maxDepth;
		usedClasses = new TreeMap<Set<ObjectProperty>,Set<NamedClass>>(keyComp);
		usedObjectProperties = new TreeMap<Set<ObjectProperty>,Set<ObjectProperty>>(keyComp);
		
		Set<ObjectProperty> startKey = new TreeSet<ObjectProperty>();
		computeUsedEntitiesRec(startKey, individuals);
		
	}

	private void computeUsedEntitiesRec(Set<ObjectProperty> key, Set<Individual> individuals) {
		Set<NamedClass> types = new TreeSet<NamedClass>();
//		Set<ObjectProperty> properties = new TreeSet<ObjectProperty>();
		// we must use the object property comparator to avoid double occurences of properties
		Map<ObjectProperty,Set<Individual>> relations = new TreeMap<ObjectProperty,Set<Individual>>();
		
		for(Individual individual : individuals) {
			// add all types
			types.addAll(reasoner.getTypes(individual));
			
			// compute outgoing properties
			Map<ObjectProperty,Set<Individual>> map = reasoner.getObjectPropertyRelationships(individual);
			for(Entry<ObjectProperty,Set<Individual>> entry : map.entrySet()) {
				ObjectProperty prop = entry.getKey();
				// we must use the individual comparator to avoid 
				// multiple occurrences of the same individual
				Set<Individual> inds = new TreeSet<Individual>(entry.getValue());
								
				// if property exists, add the found individuals 
				if(relations.containsKey(prop)) {
					relations.get(prop).addAll(inds);
				// if property not encountered before, add it
				} else {
					relations.put(prop, inds);
				}
			}
		}
		
		// store all found relations
		usedClasses.put(key, types);
		usedObjectProperties.put(key, relations.keySet());
		
		// recurse if limit not reached yet
		if(key.size() < maxDepth) {
			for(Entry<ObjectProperty,Set<Individual>> entry : relations.entrySet()) {
				// construct new key (copy and add)
				Set<ObjectProperty> newKey = new TreeSet<ObjectProperty>(key);
				newKey.add(entry.getKey());
				
				// recursion
				computeUsedEntitiesRec(newKey, entry.getValue());
			}
		}

	}
	
	public Set<Set<ObjectProperty>> getKeys() {
		return usedClasses.keySet();
	}
	
	/**
	 * @return the usedClasses
	 */
	public Map<Set<ObjectProperty>, Set<NamedClass>> getUsedClasses() {
		return usedClasses;
	}

	/**
	 * @return the usedObjectProperties
	 */
	public Map<Set<ObjectProperty>, Set<ObjectProperty>> getUsedObjectProperties() {
		return usedObjectProperties;
	}
	
	@Override
	public String toString() {
		String str = "";
		Set<Set<ObjectProperty>> keys = getKeys();
		for(Set<ObjectProperty> key : keys) {
			str += key.toString() + ": \n";
			str += "  classes: " + usedClasses.get(key) + "\n";
			str += "  object properties: " + usedObjectProperties.get(key) + "\n";
		}
		return str;
	}
	
}
