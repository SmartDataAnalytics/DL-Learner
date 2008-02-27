package org.dllearner.refinementoperators;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.refinement.RefinementOperator;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.ConceptComparator;

public class PsiUp implements RefinementOperator {

	ConceptComparator conceptComparator = new ConceptComparator();
	
	PosNegLP learningProblem;
	ReasoningService reasoningService;
	
	private TreeSet<Description> bottomSet;
	
	public PsiUp(PosNegLP learningProblem) {
		this.learningProblem = learningProblem;
		reasoningService = learningProblem.getReasoningService();
		
		// Top-Menge erstellen
		createBottomSet();
	}
	
	private void createBottomSet() {
		bottomSet = new TreeSet<Description>(conceptComparator);
		
		// BOTTOM AND BOTTOM
		Intersection mc = new Intersection();
		mc.addChild(new Nothing());
		mc.addChild(new Nothing());
		bottomSet.add(mc);
		
		// speziellste Konzepte
		bottomSet.addAll(reasoningService.getMoreGeneralConcepts(new Nothing()));
		
		// negierte allgemeinste Konzepte
		Set<Description> tmp = reasoningService.getMoreSpecialConcepts(new Thing());
		for(Description c : tmp) 
			bottomSet.add(new Negation(c));
	
		// EXISTS r.BOTTOM und ALL r.BOTTOM für alle r
		for(ObjectProperty r : reasoningService.getAtomicRoles()) {
			bottomSet.add(new ObjectAllRestriction(r, new Nothing()));
			bottomSet.add(new ObjectSomeRestriction(r, new Nothing()));
		}
	}
	
	@SuppressWarnings("unchecked")
	public Set<Description> refine(Description concept) {
		
		Set<Description> refinements = new HashSet<Description>();
		Set<Description> tmp = new HashSet<Description>();
		
		if (concept instanceof Thing) {
			return new TreeSet<Description>(conceptComparator);
		} else if (concept instanceof Nothing) {
			return (Set<Description>) bottomSet.clone();			
		} else if (concept instanceof NamedClass) {
			// Top darf hier mit dabei sein
			refinements.addAll(reasoningService.getMoreGeneralConcepts(concept));
			
		// negiertes atomares Konzept
		} else if (concept instanceof Negation && concept.getChild(0) instanceof NamedClass) {
			tmp.addAll(reasoningService.getMoreSpecialConcepts(concept.getChild(0)));
			
			// Bottom rausschmeissen
			boolean containsBottom = false;
			Iterator<Description> it = tmp.iterator();
			while(it.hasNext()) {
				Description c = it.next();
				if(c instanceof Nothing) {
					it.remove();
					containsBottom = true;
				}
			}
			// es soll z.B. NOT male auch zu NOT BOTTOM d.h. zu TOP verfeinert
			// werden können
			if(containsBottom)
				refinements.add(new Thing());
			
			for(Description c : tmp) {
				refinements.add(new Negation(c));
			}
		} else if (concept instanceof Intersection) {
			// eines der Elemente kann verfeinert werden
			for(Description child : concept.getChildren()) {
				
				// Refinement für das Kind ausführen
				tmp = refine(child);
				
				// neue MultiConjunction konstruieren
				for(Description c : tmp) {
					// TODO: müssen auch alle Konzepte geklont werden??
					// hier wird nur eine neue Liste erstellt
					// => eigentlich muss nicht geklont werden (d.h. deep copy) da
					// die Konzepte nicht verändert werden während des Algorithmus
					List<Description> newChildren = new LinkedList<Description>(concept.getChildren());
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					int index = newChildren.indexOf(child);
					newChildren.add(index, c);					
					newChildren.remove(child);
					Intersection mc = new Intersection(newChildren);
					refinements.add(mc);	
				}
			}
			
			// ein Element der Konjunktion kann weggelassen werden
			for(Description child : concept.getChildren()) {
				List<Description> newChildren = new LinkedList<Description>(concept.getChildren());
				newChildren.remove(child);
				if(newChildren.size()==1)
					refinements.add(newChildren.get(0));
				else {
					Intersection md = new Intersection(newChildren);
					refinements.add(md);
				}
			}			
		} else if (concept instanceof Union) {
			// eines der Elemente kann verfeinert werden
			for(Description child : concept.getChildren()) {
				
				// Refinement für das Kind ausführen
				// tmp = refine(child);
				tmp = refine(child);
				// neue MultiConjunction konstruieren
				for(Description c : tmp) {
					List<Description> newChildren = new LinkedList<Description>(concept.getChildren());
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					int index = newChildren.indexOf(child);
					newChildren.add(index, c);					
					newChildren.remove(child);					
					Union md = new Union(newChildren);
					refinements.add(md);	
				}
			}
		} else if (concept instanceof ObjectSomeRestriction) {
			tmp = refine(concept.getChild(0));
			for(Description c : tmp) {
				refinements.add(new ObjectSomeRestriction(((ObjectQuantorRestriction)concept).getRole(),c));
			}		
			
			if(concept.getChild(0) instanceof Thing)
				refinements.add(new Thing());
			
		} else if (concept instanceof ObjectAllRestriction) {
			tmp = refine(concept.getChild(0));
			for(Description c : tmp) {
				refinements.add(new ObjectAllRestriction(((ObjectQuantorRestriction)concept).getRole(),c));
			}		
			
			if(concept.getChild(0) instanceof Thing)
				refinements.add(new Thing());			
			
			// falls es keine spezielleren atomaren Konzepte gibt, dann wird 
			// bottom angehangen => nur wenn es ein atomares Konzept (insbesondere != bottom)
			// ist
			// if(tmp.size()==0) {
			// if(concept.getChild(0) instanceof AtomicConcept && tmp.size()==0) {
			//	refinements.add(new All(((Quantification)concept).getRole(),new Bottom()));
			//}
		} else
			throw new RuntimeException(concept.toString());
		
		if(concept instanceof Union || concept instanceof NamedClass ||
				concept instanceof Negation || concept instanceof ObjectSomeRestriction || concept instanceof ObjectAllRestriction) {
			
			// es wird OR BOTTOM angehangen
			Union md = new Union();
			md.addChild(concept);
			md.addChild(new Nothing());					
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

	public Set<Description> refine(Description concept, int maxLength,
			List<Description> knownRefinements) {
		throw new RuntimeException();
	}

}
