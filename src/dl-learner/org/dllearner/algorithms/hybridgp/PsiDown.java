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

/**
 * Operatoren Psi-Down und Psi-Up müssen noch so umgeschrieben werden, dass sie
 * nur konsistente Konzepte (mit korrekten parent-Links) enthalten. Dazu müssen
 * alle verwendeten atomaren Konzepte geklont werden. 
 * 
 * Außerdem erscheint es ratsam weitere konzeptverkürzende Maßnahmen einzuführen,
 * z.B. EXISTS r.A => BOTTOM für down bzw. TOP für up
 * => Konzepte erreichen etwa eine Länge von 20
 * 
 * @author jl
 *
 */
public class PsiDown implements RefinementOperator {

	ConceptComparator conceptComparator = new ConceptComparator();
	
	DefinitionLP learningProblem;
	ReasoningService reasoningService;
	
	private TreeSet<Concept> topSet;
	
	public PsiDown(DefinitionLP learningProblem) {
		this.learningProblem = learningProblem;
		reasoningService = learningProblem.getReasoningService();
		
		// Top-Menge erstellen
		createTopSet();
	}
	
	private void createTopSet() {
		topSet = new TreeSet<Concept>(conceptComparator);
		
		// TOP OR TOP => Was soll mit Refinements passieren, die immer improper sind?
		MultiDisjunction md = new MultiDisjunction();
		md.addChild(new Top());
		md.addChild(new Top());
		topSet.add(md);
		
		// allgemeinste Konzepte
		topSet.addAll(reasoningService.getMoreSpecialConcepts(new Top()));
		
		// negierte speziellste Konzepte
		Set<Concept> tmp = learningProblem.getReasoningService().getMoreGeneralConcepts(new Bottom());
		for(Concept c : tmp) 
			topSet.add(new Negation(c));
	
		// EXISTS r.TOP und ALL r.TOP für alle r
		for(AtomicRole r : reasoningService.getAtomicRoles()) {
			topSet.add(new All(r, new Top()));
			topSet.add(new Exists(r, new Top()));
		}		
	}
	
	@SuppressWarnings("unchecked")
	public Set<Concept> refine(Concept concept) {
		
		Set<Concept> refinements = new HashSet<Concept>();
		Set<Concept> tmp = new HashSet<Concept>();
		
		if (concept instanceof Top) {
			return (Set<Concept>) topSet.clone();
		} else if (concept instanceof Bottom) {
			// return new TreeSet<Concept>(conceptComparator);
			return new HashSet<Concept>();
		} else if (concept instanceof AtomicConcept) {
			// beachte: die Funktion gibt bereits nur nicht-äquivalente Konzepte zurück
			// beachte weiter: die zurückgegebenen Instanzen dürfen nicht verändert werden,
			// da beim Caching der Subsumptionhierarchie (momentan) keine Kopien gemacht werden
			// Bottom wird hier ggf. automatisch mit zurückgegeben
			refinements.addAll(reasoningService.getMoreSpecialConcepts(concept));
		// negiertes atomares Konzept
		} else if (concept instanceof Negation && concept.getChild(0) instanceof AtomicConcept) {
			tmp.addAll(reasoningService.getMoreGeneralConcepts(concept.getChild(0)));
			
			// Top rausschmeissen
			boolean containsTop = false;
			Iterator<Concept> it = tmp.iterator();
			while(it.hasNext()) {
				Concept c = it.next();
				if(c instanceof Top) {
					it.remove();
					containsTop = true;
				}
			}			
			if(containsTop)
				refinements.add(new Bottom());
			
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
			
			// ein Element der Disjunktion kann weggelassen werden
			for(Concept child : concept.getChildren()) {
				List<Concept> newChildren = new LinkedList<Concept>(concept.getChildren());
				newChildren.remove(child);
				// wenn nur ein Kind da ist, dann wird Disjunktion gleich weggelassen
				if(newChildren.size()==1)
					refinements.add(newChildren.get(0));
				else {
					MultiDisjunction md = new MultiDisjunction(newChildren);
					refinements.add(md);
				}
			}
			
		} else if (concept instanceof Exists) {
			tmp = refine(concept.getChild(0));
			for(Concept c : tmp) {
				refinements.add(new Exists(((Quantification)concept).getRole(),c));
			}		
			
			// falls Kind Bottom ist, dann kann exists weggelassen werden
			if(concept.getChild(0) instanceof Bottom)
				refinements.add(new Bottom());
			
		} else if (concept instanceof All) {
			tmp = refine(concept.getChild(0));
			for(Concept c : tmp) {
				refinements.add(new All(((Quantification)concept).getRole(),c));
			}		
			
			if(concept.getChild(0) instanceof Bottom)
				refinements.add(new Bottom());
			
			// falls es keine spezielleren atomaren Konzepte gibt, dann wird 
			// bottom angehangen => nur wenn es ein atomares Konzept (insbesondere != bottom)
			// ist
			// if(tmp.size()==0) {
			// if(concept.getChild(0) instanceof AtomicConcept && tmp.size()==0) {
			//	refinements.add(new All(((Quantification)concept).getRole(),new Bottom()));
			//}
		} else
			throw new RuntimeException(concept.toString());
		
		// falls Konzept ungleich Bottom oder Top, dann kann ein Refinement von Top
		// angehangen werden
		if(concept instanceof MultiDisjunction || concept instanceof AtomicConcept ||
				concept instanceof Negation || concept instanceof Exists || concept instanceof All) {
			
			// es wird AND TOP angehangen
			MultiConjunction mc = new MultiConjunction();
			mc.addChild(concept);
			mc.addChild(new Top());					
			refinements.add(mc);
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
		
		// Zwischenschritt wird weggelassen - man muss nicht alle Konzepte cleanen,
		// um dann nur eins davon auszuwählen
		
		return refinements;
		
	}

	public Set<Concept> refine(Concept concept, int maxLength,
			List<Concept> knownRefinements) {
		throw new RuntimeException();
	}

}
