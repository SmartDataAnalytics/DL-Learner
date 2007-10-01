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
package org.dllearner.core.dl;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.utilities.ConceptComparator;

/**
 * Represents a subsumption hierarchy (ignoring equivalent concepts).
 *  
 * @author Jens Lehmann
 *
 */
public class SubsumptionHierarchy {

	ConceptComparator conceptComparator = new ConceptComparator();
	TreeMap<Concept,TreeSet<Concept>> subsumptionHierarchyUp; // = new TreeMap<Concept,TreeSet<Concept>>(conceptComparator);
	TreeMap<Concept,TreeSet<Concept>> subsumptionHierarchyDown; // = new TreeMap<Concept,TreeSet<Concept>>(conceptComparator);
	Set<Concept> allowedConceptsInSubsumptionHierarchy;
	
	public SubsumptionHierarchy(Set<AtomicConcept> atomicConcepts, TreeMap<Concept,TreeSet<Concept>> subsumptionHierarchyUp , TreeMap<Concept,TreeSet<Concept>> subsumptionHierarchyDown) {
		this.subsumptionHierarchyUp = subsumptionHierarchyUp;
		this.subsumptionHierarchyDown = subsumptionHierarchyDown;
		allowedConceptsInSubsumptionHierarchy = new TreeSet<Concept>(conceptComparator);
		allowedConceptsInSubsumptionHierarchy.addAll(atomicConcepts);
		allowedConceptsInSubsumptionHierarchy.add(new Top());
		allowedConceptsInSubsumptionHierarchy.add(new Bottom());
	}
		
	@SuppressWarnings("unchecked")	
	public SortedSet<Concept> getMoreGeneralConcepts(Concept concept) {
		// we clone all concepts before returning them such that they cannot be
		// modified externally
		return (TreeSet<Concept>) subsumptionHierarchyUp.get(concept).clone();	
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<Concept> getMoreSpecialConcepts(Concept concept) {
		return (TreeSet<Concept>) subsumptionHierarchyDown.get(concept).clone();
	}	
	
	public void improveSubsumptionHierarchy() {
		TreeMap<Concept,TreeSet<Concept>> hierarchyDownNew = new TreeMap<Concept,TreeSet<Concept>>(conceptComparator);
		// hierarchyDownNew.put(new Top(), new TreeSet<Concept>(conceptComparator));
		TreeMap<Concept,TreeSet<Concept>> hierarchyUpNew = new TreeMap<Concept,TreeSet<Concept>>(conceptComparator);
		
		// Einträge für alle Konzepte machen (init)
		for(Concept c : allowedConceptsInSubsumptionHierarchy) {
			hierarchyDownNew.put(c,	new TreeSet<Concept>(conceptComparator));
			hierarchyUpNew.put(c,	new TreeSet<Concept>(conceptComparator));
		}
		
		for(Concept c : allowedConceptsInSubsumptionHierarchy) {
			// schauen, ob es mehrere allgemeinere Nachbarn gibt
			SortedSet<Concept> moreGeneral = subsumptionHierarchyUp.get(c);
			if(moreGeneral != null) {
				Concept chosenParent = moreGeneral.first();
				hierarchyDownNew.get(chosenParent).add(c);
			}
		}	
		
		// for(Concept c : allowedConceptsInSubsumptionHierarchy) {
		for(Concept c : allowedConceptsInSubsumptionHierarchy) {
			SortedSet<Concept> moreSpecial = subsumptionHierarchyDown.get(c);
			if(moreSpecial != null) {
				Concept chosenParent = moreSpecial.first();
				hierarchyUpNew.get(chosenParent).add(c);
			}
		}		

		subsumptionHierarchyDown = hierarchyDownNew;
		subsumptionHierarchyUp = hierarchyUpNew;
	}
	
	@Override	
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean showUpwardHierarchy) {
		if(showUpwardHierarchy) {
			String str = "downward subsumption:\n";
			str += toString(subsumptionHierarchyDown, new Top(), 0);
			str += "upward subsumption:\n";
			str += toString(subsumptionHierarchyUp, new Bottom(), 0);
			return str;
		} else {
			return toString(subsumptionHierarchyDown, new Top(), 0);
		}
	}
	
	private String toString(TreeMap<Concept,TreeSet<Concept>> hierarchy, Concept concept, int depth) {
		String str = "";
		for(int i=0; i<depth; i++)
			str += "  ";
		str += concept.toString() + "\n";
		Set<Concept> tmp = hierarchy.get(concept);
		if(tmp!=null) {
			for(Concept c : tmp)
				str += toString(hierarchy, c, depth+1);
		}
		return str;
	}	
}
