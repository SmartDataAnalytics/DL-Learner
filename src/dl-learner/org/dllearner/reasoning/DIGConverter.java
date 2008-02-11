package org.dllearner.reasoning;

import java.net.URI;
import java.util.Set;

import org.dllearner.core.owl.All;
import org.dllearner.core.owl.AtomicConcept;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Bottom;
import org.dllearner.core.owl.Concept;
import org.dllearner.core.owl.ConceptAssertion;
import org.dllearner.core.owl.Conjunction;
import org.dllearner.core.owl.Disjunction;
import org.dllearner.core.owl.Equality;
import org.dllearner.core.owl.Exists;
import org.dllearner.core.owl.FunctionalRoleAxiom;
import org.dllearner.core.owl.Inclusion;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.MultiConjunction;
import org.dllearner.core.owl.MultiDisjunction;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectPropertyInverse;
import org.dllearner.core.owl.Quantification;
import org.dllearner.core.owl.SubRoleAxiom;
import org.dllearner.core.owl.SymmetricRoleAxiom;
import org.dllearner.core.owl.Top;
import org.dllearner.core.owl.TransitiveRoleAxiom;

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
		Set<AtomicConcept> atomicConcepts = kb.findAllAtomicConcepts();
		Set<ObjectProperty>	atomicRoles = kb.findAllAtomicRoles();
		Set<Individual>	individuals = kb.findAllIndividuals();				
		
		for(AtomicConcept ac : atomicConcepts)
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
		else if(axiom instanceof ConceptAssertion)
			sb.append("<instanceof>"+ "<individual name=\"" + ((ConceptAssertion)axiom).getIndividual().getName() + "\"/>" +
					getDIGString(((ConceptAssertion)axiom).getConcept()) + "</instanceof>\n");
		// RBox
		else if(axiom instanceof SymmetricRoleAxiom) {
			// wird nicht direkt in DIG 1.1 unterstützt
			// ich modelliere es hier, indem ich sage, dass eine Rolle gleich ihrem
			// Inversen ist
			sb.append("<equalr>");
			sb.append("<ratom name=\""+((SymmetricRoleAxiom)axiom).getRole().getName()+"\" />\n");
			sb.append("<inverse><ratom name=\""+((SymmetricRoleAxiom)axiom).getRole().getName()+"\" /></inverse>\n");
			sb.append("</equalr>");
		} else if(axiom instanceof TransitiveRoleAxiom)
			sb.append("<transitive>"+ getDIGString(((TransitiveRoleAxiom)axiom).getRole()) + "</transitive>\n");			
		else if(axiom instanceof FunctionalRoleAxiom)
			sb.append("<functional>"+ getDIGString(((FunctionalRoleAxiom)axiom).getRole()) + "</functional>\n");			
		else if(axiom instanceof SubRoleAxiom) 
			sb.append("<impliesr>"+ getDIGString(((SubRoleAxiom)axiom).getSubRole()) + 
					getDIGString(((SubRoleAxiom)axiom).getRole()) + "</impliesr>\n");
		// TBox
		else if(axiom instanceof Equality)
			sb.append("<equalc>"+ getDIGString(((Equality)axiom).getConcept1()) + 
					getDIGString(((Equality)axiom).getConcept2()) + "</equalc>\n");			
		else if(axiom instanceof Inclusion)
			sb.append("<impliesc>"+ getDIGString(((Inclusion)axiom).getSubConcept()) + 
					getDIGString(((Inclusion)axiom).getSuperConcept()) + "</impliesc>\n");		
		else
			throw new RuntimeException();
			
		return sb;
	}
	
	public static StringBuilder getDIGString(Concept concept) {
		StringBuilder sb = new StringBuilder();
		
		if(concept instanceof Top)
			sb.append("<top/>");
		else if(concept instanceof Bottom)
			sb.append("<bottom/>");
		else if(concept instanceof AtomicConcept)
			sb.append("<catom name=\""+((AtomicConcept)concept).getName()+"\"/>");
		else if(concept instanceof Negation)
			sb.append("<not>");
		else if(concept instanceof Conjunction || concept instanceof MultiConjunction)
			sb.append("<and>");
		else if(concept instanceof Disjunction || concept instanceof MultiDisjunction)
			sb.append("<or>");
		else if(concept instanceof Exists)
			sb.append("<some>"+getDIGString(((Quantification)concept).getRole()));
		else if(concept instanceof All)
			sb.append("<all>"+getDIGString(((Quantification)concept).getRole()));
		else
			throw new RuntimeException();
		
		for(Concept child : concept.getChildren()) {
			sb.append(getDIGString(child));
		}
		
		if(concept instanceof Negation)
			sb.append("</not>");
		else if(concept instanceof Conjunction || concept instanceof MultiConjunction)
			sb.append("</and>");
		else if(concept instanceof Disjunction || concept instanceof MultiDisjunction)
			sb.append("</or>");
		else if(concept instanceof Exists)
			sb.append("</some>");
		else if(concept instanceof All)
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
