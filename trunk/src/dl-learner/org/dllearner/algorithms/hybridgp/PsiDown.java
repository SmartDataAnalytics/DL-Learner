package org.dllearner.algorithms.hybridgp;

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
	
	PosNegLP learningProblem;
	ReasoningService reasoningService;
	
	private TreeSet<Description> topSet;
	
	public PsiDown(PosNegLP learningProblem) {
		this.learningProblem = learningProblem;
		reasoningService = learningProblem.getReasoningService();
		
		// Top-Menge erstellen
		createTopSet();
	}
	
	private void createTopSet() {
		topSet = new TreeSet<Description>(conceptComparator);
		
		// TOP OR TOP => Was soll mit Refinements passieren, die immer improper sind?
		Union md = new Union();
		md.addChild(new Thing());
		md.addChild(new Thing());
		topSet.add(md);
		
		// allgemeinste Konzepte
		topSet.addAll(reasoningService.getMoreSpecialConcepts(new Thing()));
		
		// negierte speziellste Konzepte
		Set<Description> tmp = learningProblem.getReasoningService().getMoreGeneralConcepts(new Nothing());
		for(Description c : tmp) 
			topSet.add(new Negation(c));
	
		// EXISTS r.TOP und ALL r.TOP für alle r
		for(ObjectProperty r : reasoningService.getAtomicRoles()) {
			topSet.add(new ObjectAllRestriction(r, new Thing()));
			topSet.add(new ObjectSomeRestriction(r, new Thing()));
		}		
	}
	
	@SuppressWarnings("unchecked")
	public Set<Description> refine(Description concept) {
		
		Set<Description> refinements = new HashSet<Description>();
		Set<Description> tmp = new HashSet<Description>();
		
		if (concept instanceof Thing) {
			return (Set<Description>) topSet.clone();
		} else if (concept instanceof Nothing) {
			// return new TreeSet<Concept>(conceptComparator);
			return new HashSet<Description>();
		} else if (concept instanceof NamedClass) {
			// beachte: die Funktion gibt bereits nur nicht-äquivalente Konzepte zurück
			// beachte weiter: die zurückgegebenen Instanzen dürfen nicht verändert werden,
			// da beim Caching der Subsumptionhierarchie (momentan) keine Kopien gemacht werden
			// Bottom wird hier ggf. automatisch mit zurückgegeben
			refinements.addAll(reasoningService.getMoreSpecialConcepts(concept));
		// negiertes atomares Konzept
		} else if (concept instanceof Negation && concept.getChild(0) instanceof NamedClass) {
			tmp.addAll(reasoningService.getMoreGeneralConcepts(concept.getChild(0)));
			
			// Top rausschmeissen
			boolean containsTop = false;
			Iterator<Description> it = tmp.iterator();
			while(it.hasNext()) {
				Description c = it.next();
				if(c instanceof Thing) {
					it.remove();
					containsTop = true;
				}
			}			
			if(containsTop)
				refinements.add(new Nothing());
			
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
			
			// ein Element der Disjunktion kann weggelassen werden
			for(Description child : concept.getChildren()) {
				List<Description> newChildren = new LinkedList<Description>(concept.getChildren());
				newChildren.remove(child);
				// wenn nur ein Kind da ist, dann wird Disjunktion gleich weggelassen
				if(newChildren.size()==1)
					refinements.add(newChildren.get(0));
				else {
					Union md = new Union(newChildren);
					refinements.add(md);
				}
			}
			
		} else if (concept instanceof ObjectSomeRestriction) {
			tmp = refine(concept.getChild(0));
			for(Description c : tmp) {
				refinements.add(new ObjectSomeRestriction(((ObjectQuantorRestriction)concept).getRole(),c));
			}		
			
			// falls Kind Bottom ist, dann kann exists weggelassen werden
			if(concept.getChild(0) instanceof Nothing)
				refinements.add(new Nothing());
			
		} else if (concept instanceof ObjectAllRestriction) {
			tmp = refine(concept.getChild(0));
			for(Description c : tmp) {
				refinements.add(new ObjectAllRestriction(((ObjectQuantorRestriction)concept).getRole(),c));
			}		
			
			if(concept.getChild(0) instanceof Nothing)
				refinements.add(new Nothing());
			
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
		if(concept instanceof Union || concept instanceof NamedClass ||
				concept instanceof Negation || concept instanceof ObjectSomeRestriction || concept instanceof ObjectAllRestriction) {
			
			// es wird AND TOP angehangen
			Intersection mc = new Intersection();
			mc.addChild(concept);
			mc.addChild(new Thing());					
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

	public Set<Description> refine(Description concept, int maxLength,
			List<Description> knownRefinements) {
		throw new RuntimeException();
	}

}
