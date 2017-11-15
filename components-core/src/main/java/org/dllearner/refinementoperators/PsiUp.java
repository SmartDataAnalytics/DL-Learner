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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.OWLObjectIntersectionOfImplExt;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.learningproblems.PosNegLP;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

import com.google.common.collect.Lists;

public class PsiUp extends RefinementOperatorAdapter {

	PosNegLP learningProblem;
	AbstractReasonerComponent reasoningService;
	
	private TreeSet<OWLClassExpression> bottomSet;
	
	public PsiUp(PosNegLP learningProblem, AbstractReasonerComponent reasoningService) {
		this.learningProblem = learningProblem;
		this.reasoningService = reasoningService;
		
		// Top-Menge erstellen
		createBottomSet();
	}
	
	private void createBottomSet() {
		bottomSet = new TreeSet<>();
		
		// BOTTOM AND BOTTOM
		List<OWLClassExpression> operands = Lists.<OWLClassExpression>newArrayList(df.getOWLNothing(), df.getOWLNothing());
		OWLObjectIntersectionOf mc = new OWLObjectIntersectionOfImplExt(operands);
		bottomSet.add(mc);
		
		// speziellste Konzepte
		bottomSet.addAll(reasoningService.getSuperClasses(df.getOWLNothing()));
		
		// negierte allgemeinste Konzepte
		Set<OWLClassExpression> tmp = reasoningService.getSubClasses(df.getOWLThing());
		for(OWLClassExpression c : tmp) 
			bottomSet.add(df.getOWLObjectComplementOf(c));
	
		// EXISTS r.BOTTOM und ALL r.BOTTOM für alle r
		for(OWLObjectProperty r : reasoningService.getObjectProperties()) {
			bottomSet.add(df.getOWLObjectAllValuesFrom(r, df.getOWLNothing()));
			bottomSet.add(df.getOWLObjectSomeValuesFrom(r, df.getOWLNothing()));
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Set<OWLClassExpression> refine(OWLClassExpression concept) {
		
		Set<OWLClassExpression> refinements = new HashSet<>();
		Set<OWLClassExpression> tmp = new HashSet<>();
		
		if (concept.isOWLThing()) {
			return new TreeSet<>();
		} else if (concept.isOWLNothing()) {
			return (Set<OWLClassExpression>) bottomSet.clone();			
		} else if (!concept.isAnonymous()) {
			// Top darf hier mit dabei sein
			refinements.addAll(reasoningService.getSuperClasses(concept));
			
		// negiertes atomares Konzept
		} else if (concept instanceof OWLObjectComplementOf) {
			OWLClassExpression operand = ((OWLObjectComplementOf) concept).getOperand();
			if(!operand.isAnonymous()){
				tmp.addAll(reasoningService.getSubClasses(operand));
				
				// Bottom rausschmeissen
				boolean containsBottom = false;
				Iterator<OWLClassExpression> it = tmp.iterator();
				while(it.hasNext()) {
					OWLClassExpression c = it.next();
					if(c instanceof OWLObjectComplementOf) {
						it.remove();
						containsBottom = true;
					}
				}
				// es soll z.B. NOT male auch zu NOT BOTTOM d.h. zu TOP verfeinert
				// werden können
				if(containsBottom)
					refinements.add(df.getOWLThing());
				
				for(OWLClassExpression c : tmp) {
					refinements.add(df.getOWLObjectComplementOf(c));
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
			
			// ein Element der Konjunktion kann weggelassen werden
			for(OWLClassExpression child : operands) {
				List<OWLClassExpression> newChildren = new LinkedList<>(operands);
				newChildren.remove(child);
				if(newChildren.size()==1)
					refinements.add(newChildren.get(0));
				else {
					OWLClassExpression md = new OWLObjectIntersectionOfImplExt(newChildren);
					refinements.add(md);
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
		} else if (concept instanceof OWLObjectSomeValuesFrom) {
			OWLObjectPropertyExpression role = ((OWLObjectSomeValuesFrom) concept).getProperty();
			OWLClassExpression filler = ((OWLObjectSomeValuesFrom) concept).getFiller();
			
			tmp = refine(filler);
			for(OWLClassExpression c : tmp) {
				refinements.add(df.getOWLObjectSomeValuesFrom(role, c));
			}		
			
			if(filler.isOWLThing())
				refinements.add(df.getOWLThing());
		} else if (concept instanceof OWLObjectAllValuesFrom) {
			OWLObjectPropertyExpression role = ((OWLObjectAllValuesFrom) concept).getProperty();
			OWLClassExpression filler = ((OWLObjectAllValuesFrom) concept).getFiller();

			tmp = refine(filler);
			for(OWLClassExpression c : tmp) {
				refinements.add(df.getOWLObjectAllValuesFrom(role, c));
			}		
			
			if(concept.isOWLThing())
				refinements.add(df.getOWLThing());			
			
			// falls es keine spezielleren atomaren Konzepte gibt, dann wird 
			// bottom angehangen => nur wenn es ein atomares Konzept (insbesondere != bottom)
			// ist
			// if(tmp.size()==0) {
			// if(concept.getChild(0) instanceof AtomicConcept && tmp.size()==0) {
			//	refinements.add(new All(((Quantification)concept).getRole(),new Bottom()));
			//}
		} else
			throw new RuntimeException(concept.toString());
		
		if(concept instanceof OWLObjectUnionOf || 
				!concept.isAnonymous() ||
				concept instanceof OWLObjectComplementOf || 
				concept instanceof OWLObjectSomeValuesFrom || 
				concept instanceof OWLObjectAllValuesFrom) {
			
			// es wird OR BOTTOM angehangen
			List<OWLClassExpression> operands = Lists.newArrayList(concept, df.getOWLThing());
			OWLClassExpression md = new OWLObjectUnionOfImplExt(operands);
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
