/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * This class takes a reasoner and individuals as input and detects
 * the relevant (wrt. a learning process) classes and properties
 * at a certain distance of the examples.
 * 
 * @author Jens Lehmann
 *
 */
public class UsedEntitiesDetection {

	Comparator<Set<OWLObjectProperty>> keyComp = new Comparator<Set<OWLObjectProperty>>() {

		@Override
		public int compare(Set<OWLObjectProperty> key1, Set<OWLObjectProperty> key2) {
			// first criterion: size of key
			int sizeDiff = key1.size() - key2.size();
			if(sizeDiff == 0) {
				Iterator<OWLObjectProperty> it1 = key1.iterator();
				Iterator<OWLObjectProperty> it2 = key2.iterator();
				// compare elements one by one (assumes that both use the same
				// ordering, which is the case)
				while(it1.hasNext()) {
					OWLObjectProperty prop1 = it1.next();
					OWLObjectProperty prop2 = it2.next();
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
	
	private Map<Set<OWLObjectProperty>,Set<OWLClass>> usedClasses;
	
	private Map<Set<OWLObjectProperty>,Set<OWLObjectProperty>> usedObjectProperties;
	
	private AbstractReasonerComponent reasoner;
	private int maxDepth;
	
	/**
	 * Computes used properties in classes. 
	 * TODO more explanation
	 * 
	 * @param reasoner A reasoner.
	 * @param individuals A set of individuals to start from.
	 * @param maxDepth The maximum depth for the search.
	 */
	public UsedEntitiesDetection(AbstractReasonerComponent reasoner, Set<OWLIndividual> individuals, int maxDepth) {
		this.reasoner = reasoner;
		this.maxDepth = maxDepth;
		usedClasses = new TreeMap<>(keyComp);
		usedObjectProperties = new TreeMap<>(keyComp);
		
		Set<OWLObjectProperty> startKey = new TreeSet<>();
		computeUsedEntitiesRec(startKey, individuals);
		
	}

	private void computeUsedEntitiesRec(Set<OWLObjectProperty> key, Set<OWLIndividual> individuals) {
		Set<OWLClass> types = new TreeSet<>();
//		Set<ObjectProperty> properties = new TreeSet<ObjectProperty>();
		// we must use the object property comparator to avoid double occurences of properties
		Map<OWLObjectProperty,Set<OWLIndividual>> relations = new TreeMap<>();
		
		for(OWLIndividual individual : individuals) {
			// add all types
			types.addAll(reasoner.getTypes(individual));
			
			// compute outgoing properties
			Map<OWLObjectProperty,Set<OWLIndividual>> map = reasoner.getObjectPropertyRelationships(individual);
			for(Entry<OWLObjectProperty,Set<OWLIndividual>> entry : map.entrySet()) {
				OWLObjectProperty prop = entry.getKey();
				// we must use the individual comparator to avoid 
				// multiple occurrences of the same individual
				Set<OWLIndividual> inds = new TreeSet<>(entry.getValue());
								
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
			for(Entry<OWLObjectProperty,Set<OWLIndividual>> entry : relations.entrySet()) {
				// construct new key (copy and add)
				Set<OWLObjectProperty> newKey = new TreeSet<>(key);
				newKey.add(entry.getKey());
				
				// recursion
				computeUsedEntitiesRec(newKey, entry.getValue());
			}
		}

	}
	
	public Set<Set<OWLObjectProperty>> getKeys() {
		return usedClasses.keySet();
	}
	
	/**
	 * @return the usedClasses
	 */
	public Map<Set<OWLObjectProperty>, Set<OWLClass>> getUsedClasses() {
		return usedClasses;
	}

	/**
	 * @return the usedObjectProperties
	 */
	public Map<Set<OWLObjectProperty>, Set<OWLObjectProperty>> getUsedObjectProperties() {
		return usedObjectProperties;
	}
	
	@Override
	public String toString() {
		String str = "";
		Set<Set<OWLObjectProperty>> keys = getKeys();
		for(Set<OWLObjectProperty> key : keys) {
			str += key.toString() + ": \n";
			str += "  classes: " + usedClasses.get(key) + "\n";
			str += "  object properties: " + usedObjectProperties.get(key) + "\n";
		}
		return str;
	}
	
}
