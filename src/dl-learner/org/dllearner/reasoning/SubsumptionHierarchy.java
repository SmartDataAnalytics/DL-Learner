package org.dllearner.reasoning;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.Bottom;
import org.dllearner.dl.Concept;
import org.dllearner.dl.Top;
import org.dllearner.utilities.ConceptComparator;

/**
 * Repräsentiert eine Subsumptionhierarchie (ohne äquivalente Konzepte).
 * 
 * @author jl
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
	
	// es wird geklont, damit Subsumptionhierarchie nicht von außen verändert 
	// werden kann
	@SuppressWarnings("unchecked")
	public SortedSet<Concept> getMoreGeneralConcepts(Concept concept) {
		return (TreeSet<Concept>) subsumptionHierarchyUp.get(concept).clone();	
		// return subsumptionHierarchyUp.get(concept); // ohne klonen geht es nicht
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<Concept> getMoreSpecialConcepts(Concept concept) {
		return (TreeSet<Concept>) subsumptionHierarchyDown.get(concept).clone();
		// return subsumptionHierarchyDown.get(concept); // ohne klonen geht es nicht
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
