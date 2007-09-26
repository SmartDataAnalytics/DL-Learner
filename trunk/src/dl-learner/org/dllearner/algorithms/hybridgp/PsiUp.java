package org.dllearner.algorithms.hybridgp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.LearningProblem;
import org.dllearner.algorithms.refinement.RefinementOperator;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.dl.All;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Bottom;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Exists;
import org.dllearner.core.dl.MultiConjunction;
import org.dllearner.core.dl.MultiDisjunction;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.Quantification;
import org.dllearner.core.dl.Top;
import org.dllearner.learningproblems.DefinitionLP;
import org.dllearner.utilities.ConceptComparator;

public class PsiUp implements RefinementOperator {

	ConceptComparator conceptComparator = new ConceptComparator();
	
	DefinitionLP learningProblem;
	ReasoningService reasoningService;
	
	private TreeSet<Concept> bottomSet;
	
	public PsiUp(DefinitionLP learningProblem) {
		this.learningProblem = learningProblem;
		reasoningService = learningProblem.getReasoningService();
		
		// Top-Menge erstellen
		createBottomSet();
	}
	
	private void createBottomSet() {
		bottomSet = new TreeSet<Concept>(conceptComparator);
		
		// BOTTOM AND BOTTOM
		MultiConjunction mc = new MultiConjunction();
		mc.addChild(new Bottom());
		mc.addChild(new Bottom());
		bottomSet.add(mc);
		
		// speziellste Konzepte
		bottomSet.addAll(reasoningService.getMoreGeneralConcepts(new Bottom()));
		
		// negierte allgemeinste Konzepte
		Set<Concept> tmp = reasoningService.getMoreSpecialConcepts(new Top());
		for(Concept c : tmp) 
			bottomSet.add(new Negation(c));
	
		// EXISTS r.BOTTOM und ALL r.BOTTOM für alle r
		for(AtomicRole r : reasoningService.getAtomicRoles()) {
			bottomSet.add(new All(r, new Bottom()));
			bottomSet.add(new Exists(r, new Bottom()));
		}
	}
	
	@SuppressWarnings("unchecked")
	public Set<Concept> refine(Concept concept) {
		
		Set<Concept> refinements = new HashSet<Concept>();
		Set<Concept> tmp = new HashSet<Concept>();
		
		if (concept instanceof Top) {
			return new TreeSet<Concept>(conceptComparator);
		} else if (concept instanceof Bottom) {
			return (Set<Concept>) bottomSet.clone();			
		} else if (concept instanceof AtomicConcept) {
			// Top darf hier mit dabei sein
			refinements.addAll(reasoningService.getMoreGeneralConcepts(concept));
			
		// negiertes atomares Konzept
		} else if (concept instanceof Negation && concept.getChild(0) instanceof AtomicConcept) {
			tmp.addAll(reasoningService.getMoreSpecialConcepts(concept.getChild(0)));
			
			// Bottom rausschmeissen
			boolean containsBottom = false;
			Iterator<Concept> it = tmp.iterator();
			while(it.hasNext()) {
				Concept c = it.next();
				if(c instanceof Bottom) {
					it.remove();
					containsBottom = true;
				}
			}
			// es soll z.B. NOT male auch zu NOT BOTTOM d.h. zu TOP verfeinert
			// werden können
			if(containsBottom)
				refinements.add(new Top());
			
			for(Concept c : tmp) {
				refinements.add(new Negation(c));
			}
		} else if (concept instanceof MultiConjunction) {
			// eines der Elemente kann verfeinert werden
			for(Concept child : concept.getChildren()) {
				
				// Refinement für das Kind ausführen
				tmp = refine(child);
				
				// neue MultiConjunction konstruieren
				for(Concept c : tmp) {
					// TODO: müssen auch alle Konzepte geklont werden??
					// hier wird nur eine neue Liste erstellt
					// => eigentlich muss nicht geklont werden (d.h. deep copy) da
					// die Konzepte nicht verändert werden während des Algorithmus
					List<Concept> newChildren = new LinkedList<Concept>(concept.getChildren());
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					int index = newChildren.indexOf(child);
					newChildren.add(index, c);					
					newChildren.remove(child);
					MultiConjunction mc = new MultiConjunction(newChildren);
					refinements.add(mc);	
				}
			}
			
			// ein Element der Konjunktion kann weggelassen werden
			for(Concept child : concept.getChildren()) {
				List<Concept> newChildren = new LinkedList<Concept>(concept.getChildren());
				newChildren.remove(child);
				if(newChildren.size()==1)
					refinements.add(newChildren.get(0));
				else {
					MultiConjunction md = new MultiConjunction(newChildren);
					refinements.add(md);
				}
			}			
		} else if (concept instanceof MultiDisjunction) {
			// eines der Elemente kann verfeinert werden
			for(Concept child : concept.getChildren()) {
				
				// Refinement für das Kind ausführen
				// tmp = refine(child);
				tmp = refine(child);
				// neue MultiConjunction konstruieren
				for(Concept c : tmp) {
					List<Concept> newChildren = new LinkedList<Concept>(concept.getChildren());
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					int index = newChildren.indexOf(child);
					newChildren.add(index, c);					
					newChildren.remove(child);					
					MultiDisjunction md = new MultiDisjunction(newChildren);
					refinements.add(md);	
				}
			}
		} else if (concept instanceof Exists) {
			tmp = refine(concept.getChild(0));
			for(Concept c : tmp) {
				refinements.add(new Exists(((Quantification)concept).getRole(),c));
			}		
			
			if(concept.getChild(0) instanceof Top)
				refinements.add(new Top());
			
		} else if (concept instanceof All) {
			tmp = refine(concept.getChild(0));
			for(Concept c : tmp) {
				refinements.add(new All(((Quantification)concept).getRole(),c));
			}		
			
			if(concept.getChild(0) instanceof Top)
				refinements.add(new Top());			
			
			// falls es keine spezielleren atomaren Konzepte gibt, dann wird 
			// bottom angehangen => nur wenn es ein atomares Konzept (insbesondere != bottom)
			// ist
			// if(tmp.size()==0) {
			// if(concept.getChild(0) instanceof AtomicConcept && tmp.size()==0) {
			//	refinements.add(new All(((Quantification)concept).getRole(),new Bottom()));
			//}
		} else
			throw new RuntimeException(concept.toString());
		
		if(concept instanceof MultiDisjunction || concept instanceof AtomicConcept ||
				concept instanceof Negation || concept instanceof Exists || concept instanceof All) {
			
			// es wird OR BOTTOM angehangen
			MultiDisjunction md = new MultiDisjunction();
			md.addChild(concept);
			md.addChild(new Bottom());					
			refinements.add(md);
		}

		// Refinements werden jetzt noch bereinigt, d.h. Verschachtelungen von Konjunktionen
		// werden entfernt; es wird eine neue Menge erzeugt, da die Transformationen die
		// Ordnung des Konzepts ändern könnten
		// TODO: eventuell geht das noch effizienter, da die meisten Refinement-Regeln Refinements
		// von Child-Konzepten sind, die bereits geordnet sind, d.h. man könnte dort eventuell
		// gleich absichern, dass alle neu hinzugefügten Refinements in geordneter Negationsnormalform
		// sind
		// SortedSet<Concept> returnSet = new TreeSet<Concept>(conceptComparator);
		/*
		Set<Concept> returnSet = new HashSet<Concept>();
		for(Concept c : refinements) {
			ConceptTransformation.cleanConcept(c);
			// ConceptTransformation.transformToOrderedNegationNormalForm(c, conceptComparator);
			returnSet.add(c);
		}
		
		return returnSet;
		*/
		return refinements;
	}

	public Set<Concept> refine(Concept concept, int maxLength,
			List<Concept> knownRefinements) {
		throw new RuntimeException();
	}

}
