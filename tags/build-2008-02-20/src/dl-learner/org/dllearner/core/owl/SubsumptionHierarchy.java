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
package org.dllearner.core.owl;

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
	TreeMap<Description,TreeSet<Description>> subsumptionHierarchyUp; // = new TreeMap<Concept,TreeSet<Concept>>(conceptComparator);
	TreeMap<Description,TreeSet<Description>> subsumptionHierarchyDown; // = new TreeMap<Concept,TreeSet<Concept>>(conceptComparator);
	Set<Description> allowedConceptsInSubsumptionHierarchy;
	
	public SubsumptionHierarchy(Set<NamedClass> atomicConcepts, TreeMap<Description,TreeSet<Description>> subsumptionHierarchyUp , TreeMap<Description,TreeSet<Description>> subsumptionHierarchyDown) {
		this.subsumptionHierarchyUp = subsumptionHierarchyUp;
		this.subsumptionHierarchyDown = subsumptionHierarchyDown;
		allowedConceptsInSubsumptionHierarchy = new TreeSet<Description>(conceptComparator);
		allowedConceptsInSubsumptionHierarchy.addAll(atomicConcepts);
		allowedConceptsInSubsumptionHierarchy.add(new Thing());
		allowedConceptsInSubsumptionHierarchy.add(new Nothing());
	}
		
	@SuppressWarnings("unchecked")	
	public SortedSet<Description> getMoreGeneralConcepts(Description concept) {
		// we clone all concepts before returning them such that they cannot be
		// modified externally
		return (TreeSet<Description>) subsumptionHierarchyUp.get(concept).clone();	
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<Description> getMoreSpecialConcepts(Description concept) {
		return (TreeSet<Description>) subsumptionHierarchyDown.get(concept).clone();
	}	
	
	public void improveSubsumptionHierarchy() {
		TreeMap<Description,TreeSet<Description>> hierarchyDownNew = new TreeMap<Description,TreeSet<Description>>(conceptComparator);
		// hierarchyDownNew.put(new Top(), new TreeSet<Concept>(conceptComparator));
		TreeMap<Description,TreeSet<Description>> hierarchyUpNew = new TreeMap<Description,TreeSet<Description>>(conceptComparator);
		
		// Einträge für alle Konzepte machen (init)
		for(Description c : allowedConceptsInSubsumptionHierarchy) {
			hierarchyDownNew.put(c,	new TreeSet<Description>(conceptComparator));
			hierarchyUpNew.put(c,	new TreeSet<Description>(conceptComparator));
		}
		
		for(Description c : allowedConceptsInSubsumptionHierarchy) {
			// schauen, ob es mehrere allgemeinere Nachbarn gibt
			SortedSet<Description> moreGeneral = subsumptionHierarchyUp.get(c);
			if(moreGeneral != null) {
				Description chosenParent = moreGeneral.first();
				hierarchyDownNew.get(chosenParent).add(c);
			}
		}	
		
		// for(Concept c : allowedConceptsInSubsumptionHierarchy) {
		for(Description c : allowedConceptsInSubsumptionHierarchy) {
			SortedSet<Description> moreSpecial = subsumptionHierarchyDown.get(c);
			if(moreSpecial != null) {
				Description chosenParent = moreSpecial.first();
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
			str += toString(subsumptionHierarchyDown, new Thing(), 0);
			str += "upward subsumption:\n";
			str += toString(subsumptionHierarchyUp, new Nothing(), 0);
			return str;
		} else {
			return toString(subsumptionHierarchyDown, new Thing(), 0);
		}
	}
	
	private String toString(TreeMap<Description,TreeSet<Description>> hierarchy, Description concept, int depth) {
		String str = "";
		for(int i=0; i<depth; i++)
			str += "  ";
		str += concept.toString() + "\n";
		Set<Description> tmp = hierarchy.get(concept);
		if(tmp!=null) {
			for(Description c : tmp)
				str += toString(hierarchy, c, depth+1);
		}
		return str;
	}	
}
