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
package org.dllearner.refinementoperators;

import com.google.common.collect.Lists;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.OWLObjectIntersectionOfImplExt;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.learningproblems.PosNegLP;
import org.semanticweb.owlapi.model.*;

import java.util.*;

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
public class PsiDown extends RefinementOperatorAdapter {

	PosNegLP learningProblem;
	AbstractReasonerComponent reasoningService;
	
	private TreeSet<OWLClassExpression> topSet;
	
	public PsiDown(PosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		this.learningProblem = learningProblem;
		this.reasoningService = reasoningService;
		
		// Top-Menge erstellen
		createTopSet();
	}
	
	private void createTopSet() {
		topSet = new TreeSet<>();
		
		// TOP OR TOP => Was soll mit Refinements passieren, die immer improper sind?
		List<OWLClassExpression> operands = Lists.<OWLClassExpression>newArrayList(df.getOWLThing(), df.getOWLThing());
		OWLObjectUnionOf md = new OWLObjectUnionOfImplExt(operands);
		topSet.add(md);
		
		// allgemeinste Konzepte
		topSet.addAll(reasoningService.getSubClasses(df.getOWLThing()));
		
		// negierte speziellste Konzepte
		Set<OWLClassExpression> tmp = reasoningService.getSuperClasses(df.getOWLNothing());
		for(OWLClassExpression c : tmp) 
			topSet.add(df.getOWLObjectComplementOf(c));
	
		// EXISTS r.TOP und ALL r.TOP für alle r
		for(OWLObjectProperty r : reasoningService.getObjectProperties()) {
			topSet.add(df.getOWLObjectAllValuesFrom(r, df.getOWLThing()));
			topSet.add(df.getOWLObjectSomeValuesFrom(r, df.getOWLThing()));
		}		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Set<OWLClassExpression> refine(OWLClassExpression concept) {
		
		Set<OWLClassExpression> refinements = new HashSet<>();
		Set<OWLClassExpression> tmp;
		
		if (concept.isOWLThing()) {
			return (Set<OWLClassExpression>) topSet.clone();
		} else if (concept.isOWLNothing()) {
			return new HashSet<>();
		} else if (!concept.isAnonymous()) {
			// beachte: die Funktion gibt bereits nur nicht-äquivalente Konzepte zurück
			// beachte weiter: die zurückgegebenen Instanzen dürfen nicht verändert werden,
			// da beim Caching der Subsumptionhierarchie (momentan) keine Kopien gemacht werden
			// Bottom wird hier ggf. automatisch mit zurückgegeben
			refinements.addAll(reasoningService.getSubClasses(concept));
		// negiertes atomares Konzept
		} else if (concept instanceof OWLObjectComplementOf) {
			OWLClassExpression operand = ((OWLObjectComplementOf) concept).getOperand();
			if(!operand.isAnonymous()){
				tmp = reasoningService.getSuperClasses(operand);
				
				for(OWLClassExpression c : tmp) {
					if(!c.isOWLThing()){
						refinements.add(df.getOWLObjectComplementOf(c));
					}
				}
			}
		} else if (concept instanceof OWLObjectIntersectionOf) {
			List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) concept).getOperandsAsList();
			// refine one of the elements
			for(OWLClassExpression child : operands) {
				
				// Refinement für das Kind ausführen
				tmp = refine(child);
				
				// neue MultiConjunction konstruieren
				for(OWLClassExpression c : tmp) {
					// TODO: müssen auch alle Konzepte geklont werden??
					// hier wird nur eine neue Liste erstellt
					// => eigentlich muss nicht geklont werden (d.h. deep copy) da
					// die Konzepte nicht verändert werden während des Algorithmus
					List<OWLClassExpression> newChildren = new LinkedList<>(operands);
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					int index = newChildren.indexOf(child);
					newChildren.add(index, c);					
					newChildren.remove(child);
					OWLClassExpression mc = new OWLObjectIntersectionOfImplExt(newChildren);
					refinements.add(mc);	
				}
			}
		} else if (concept instanceof OWLObjectUnionOf) {
			// refine one of the elements
			List<OWLClassExpression> operands = ((OWLObjectUnionOf) concept).getOperandsAsList();
			for(OWLClassExpression child : operands) {
				
				// Refinement für das Kind ausführen
				// tmp = refine(child);
				tmp = refine(child);
				// neue MultiConjunction konstruieren
				for(OWLClassExpression c : tmp) {
					List<OWLClassExpression> newChildren = new LinkedList<>(operands);
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					int index = newChildren.indexOf(child);
					newChildren.add(index, c);					
					newChildren.remove(child);					
					OWLObjectUnionOf md = new OWLObjectUnionOfImplExt(newChildren);
					refinements.add(md);	
				}
			}
			
			// ein Element der Disjunktion kann weggelassen werden
			for(OWLClassExpression child : ((OWLObjectUnionOf) concept).getOperandsAsList()) {
				List<OWLClassExpression> newChildren = new LinkedList<>(operands);
				newChildren.remove(child);
				// wenn nur ein Kind da ist, dann wird Disjunktion gleich weggelassen
				if(newChildren.size()==1)
					refinements.add(newChildren.get(0));
				else {
					OWLObjectUnionOf md = new OWLObjectUnionOfImplExt(newChildren);
					refinements.add(md);
				}
			}
			
		} else if (concept instanceof OWLObjectSomeValuesFrom) {
			OWLObjectPropertyExpression role = ((OWLObjectSomeValuesFrom) concept).getProperty();
			OWLClassExpression filler = ((OWLObjectSomeValuesFrom) concept).getFiller();
			
			tmp = refine(filler);
			for(OWLClassExpression c : tmp) {
				refinements.add(df.getOWLObjectSomeValuesFrom(role, c));
			}		
			
			// falls Kind Bottom ist, dann kann exists weggelassen werden
			if(filler.isOWLNothing())
				refinements.add(df.getOWLNothing());
			
		} else if (concept instanceof OWLObjectAllValuesFrom) {
			OWLObjectPropertyExpression role = ((OWLObjectAllValuesFrom) concept).getProperty();
			OWLClassExpression filler = ((OWLObjectAllValuesFrom) concept).getFiller();

			tmp = refine(filler);
			for(OWLClassExpression c : tmp) {
				refinements.add(df.getOWLObjectAllValuesFrom(role, c));
			}		
			
			if(filler.isOWLNothing())
				refinements.add(df.getOWLNothing());
			
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
		if(concept instanceof OWLObjectUnionOf || 
				!concept.isAnonymous() ||
				concept instanceof OWLObjectComplementOf || 
				concept instanceof OWLObjectSomeValuesFrom || 
				concept instanceof OWLObjectAllValuesFrom) {
			
			// es wird AND TOP angehangen
			List<OWLClassExpression> operands = Lists.newArrayList(concept, df.getOWLThing());
			OWLClassExpression mc = new OWLObjectIntersectionOfImplExt(operands);
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

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression concept, int maxLength,
			List<OWLClassExpression> knownRefinements) {
		throw new RuntimeException();
	}

	@Override
	public void init() throws ComponentInitException {
		initialized = true;
	}

}
