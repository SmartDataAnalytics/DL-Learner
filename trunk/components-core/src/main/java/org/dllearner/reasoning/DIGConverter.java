/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.reasoning;

import java.net.URI;
import java.util.Set;

import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectPropertyInverse;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.SubObjectPropertyAxiom;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;

/**
 * Methods for converting internal representation to DIG and
 * vice versa.
 * 
 * @author jl
 *
 */
public class DIGConverter {
	
	public static StringBuilder getDIGString(KB kb, URI kbURI) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		sb.append("<tells xmlns=\"http://dl.kr.org/dig/2003/02/lang\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xsi:schemaLocation=\"http://dl.kr.org/dig/2003/02/lang\n" +
				"http://dl-web.man.ac.uk/dig/2003/02/dig.xsd\" uri=\""+kbURI+"\">");
		sb.append(getDIGString(kb));
		sb.append("</tells>");
		return sb;
	}
	
	public static StringBuilder getDIGString(KB kb) {
		StringBuilder sb = new StringBuilder();
		
		// erstmal alle Konzepte, Rollen und Individuen definieren
		Set<NamedClass> atomicConcepts = kb.findAllAtomicConcepts();
		Set<ObjectProperty>	atomicRoles = kb.findAllAtomicRoles();
		Set<Individual>	individuals = kb.findAllIndividuals();				
		
		for(NamedClass ac : atomicConcepts)
			sb.append("<defconcept name=\""+ac.getName()+"\"/>");
		
		for(ObjectProperty ar : atomicRoles)
			sb.append("<defrole name=\""+ar.getName()+"\"/>");
		
		for(Individual individual : individuals)
			sb.append("<defindividual name=\""+individual.getName()+"\"/>");
		
		for(Axiom axiom : kb.getAbox()) {
			sb.append(getDIGString(axiom));
		}
		for(Axiom axiom : kb.getRbox()) {
			sb.append(getDIGString(axiom));
		}
		for(Axiom axiom : kb.getTbox()) {
			sb.append(getDIGString(axiom));
		}		
		return sb;
	}
	
	public static StringBuilder getDIGString(Axiom axiom) {
		StringBuilder sb = new StringBuilder();
		
		// ABox
		if(axiom instanceof ObjectPropertyAssertion)
			sb.append("<related>"+ "<individual name=\"" + ((ObjectPropertyAssertion)axiom).getIndividual1().getName() + "\"/>"
					+ getDIGString(((ObjectPropertyAssertion)axiom).getRole()) +					
					"<individual name=\"" + ((ObjectPropertyAssertion)axiom).getIndividual2().getName() + "\"/></related>\n");
		else if(axiom instanceof ClassAssertionAxiom)
			sb.append("<instanceof>"+ "<individual name=\"" + ((ClassAssertionAxiom)axiom).getIndividual().getName() + "\"/>" +
					getDIGString(((ClassAssertionAxiom)axiom).getConcept()) + "</instanceof>\n");
		// RBox
		else if(axiom instanceof SymmetricObjectPropertyAxiom) {
			// wird nicht direkt in DIG 1.1 unterstützt
			// ich modelliere es hier, indem ich sage, dass eine Rolle gleich ihrem
			// Inversen ist
			sb.append("<equalr>");
			sb.append("<ratom name=\""+((SymmetricObjectPropertyAxiom)axiom).getRole().getName()+"\" />\n");
			sb.append("<inverse><ratom name=\""+((SymmetricObjectPropertyAxiom)axiom).getRole().getName()+"\" /></inverse>\n");
			sb.append("</equalr>");
		} else if(axiom instanceof TransitiveObjectPropertyAxiom)
			sb.append("<transitive>"+ getDIGString(((TransitiveObjectPropertyAxiom)axiom).getRole()) + "</transitive>\n");			
		else if(axiom instanceof FunctionalObjectPropertyAxiom)
			sb.append("<functional>"+ getDIGString(((FunctionalObjectPropertyAxiom)axiom).getRole()) + "</functional>\n");			
		else if(axiom instanceof SubObjectPropertyAxiom) 
			sb.append("<impliesr>"+ getDIGString(((SubObjectPropertyAxiom)axiom).getSubRole()) + 
					getDIGString(((SubObjectPropertyAxiom)axiom).getRole()) + "</impliesr>\n");
		// TBox
		else if(axiom instanceof EquivalentClassesAxiom)
			sb.append("<equalc>"+ getDIGString(((EquivalentClassesAxiom)axiom).getConcept1()) + 
					getDIGString(((EquivalentClassesAxiom)axiom).getConcept2()) + "</equalc>\n");			
		else if(axiom instanceof SubClassAxiom)
			sb.append("<impliesc>"+ getDIGString(((SubClassAxiom)axiom).getSubConcept()) + 
					getDIGString(((SubClassAxiom)axiom).getSuperConcept()) + "</impliesc>\n");		
		else
			throw new RuntimeException();
			
		return sb;
	}
	
	public static StringBuilder getDIGString(Description concept) {
		StringBuilder sb = new StringBuilder();
		
		if(concept instanceof Thing)
			sb.append("<top/>");
		else if(concept instanceof Nothing)
			sb.append("<bottom/>");
		else if(concept instanceof NamedClass)
			sb.append("<catom name=\""+((NamedClass)concept).getName()+"\"/>");
		else if(concept instanceof Negation)
			sb.append("<not>");
		else if(concept instanceof Intersection)
			sb.append("<and>");
		else if(concept instanceof Union)
			sb.append("<or>");
		else if(concept instanceof ObjectSomeRestriction)
			sb.append("<some>"+getDIGString(((ObjectQuantorRestriction)concept).getRole()));
		else if(concept instanceof ObjectAllRestriction)
			sb.append("<all>"+getDIGString(((ObjectQuantorRestriction)concept).getRole()));
		else
			throw new RuntimeException();
		
		for(Description child : concept.getChildren()) {
			sb.append(getDIGString(child));
		}
		
		if(concept instanceof Negation)
			sb.append("</not>");
		else if(concept instanceof Intersection)
			sb.append("</and>");
		else if(concept instanceof Union)
			sb.append("</or>");
		else if(concept instanceof ObjectSomeRestriction)
			sb.append("</some>");
		else if(concept instanceof ObjectAllRestriction)
			sb.append("</all>");		
		
		return sb;
	}
	
	public static StringBuilder getDIGString(ObjectPropertyExpression role) {
		if(role instanceof ObjectProperty)
			return new StringBuilder("<ratom name=\"" + ((ObjectProperty)role).getName() + "\"/>");
		else if(role instanceof ObjectPropertyInverse)
			return new StringBuilder("<inverse><ratom name=\"" + ((ObjectPropertyInverse)role).getName() + "\"/></inverse>");
		
		throw new RuntimeException("Can only create DIG Strings for atomic and inverse roles, not for " + role + ".");
	}

	
}
