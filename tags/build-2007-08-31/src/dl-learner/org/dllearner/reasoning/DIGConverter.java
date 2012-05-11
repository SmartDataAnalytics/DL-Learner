package org.dllearner.reasoning;

import java.util.Set;

import org.dllearner.dl.All;
import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.AtomicRole;
import org.dllearner.dl.Axiom;
import org.dllearner.dl.Bottom;
import org.dllearner.dl.Concept;
import org.dllearner.dl.ConceptAssertion;
import org.dllearner.dl.Conjunction;
import org.dllearner.dl.Disjunction;
import org.dllearner.dl.Equality;
import org.dllearner.dl.Exists;
import org.dllearner.dl.FunctionalRoleAxiom;
import org.dllearner.dl.Inclusion;
import org.dllearner.dl.Individual;
import org.dllearner.dl.InverseRole;
import org.dllearner.dl.KB;
import org.dllearner.dl.MultiConjunction;
import org.dllearner.dl.MultiDisjunction;
import org.dllearner.dl.Negation;
import org.dllearner.dl.Quantification;
import org.dllearner.dl.Role;
import org.dllearner.dl.RoleAssertion;
import org.dllearner.dl.SubRoleAxiom;
import org.dllearner.dl.SymmetricRoleAxiom;
import org.dllearner.dl.Top;
import org.dllearner.dl.TransitiveRoleAxiom;

/**
 * Methods for converting internal representation to DIG and
 * vice versa.
 * 
 * @author jl
 *
 */
public class DIGConverter {
	
	public static StringBuilder getDIGString(KB kb) {
		StringBuilder sb = new StringBuilder();
		
		// erstmal alle Konzepte, Rollen und Individuen definieren
		Set<AtomicConcept> atomicConcepts = kb.findAllAtomicConcepts();
		Set<AtomicRole>	atomicRoles = kb.findAllAtomicRoles();
		Set<Individual>	individuals = kb.findAllIndividuals();				
		
		for(AtomicConcept ac : atomicConcepts)
			sb.append("<defconcept name=\""+ac.getName()+"\"/>");
		
		for(AtomicRole ar : atomicRoles)
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
		if(axiom instanceof RoleAssertion)
			sb.append("<related>"+ "<individual name=\"" + ((RoleAssertion)axiom).getIndividual1().getName() + "\"/>"
					+ getDIGString(((RoleAssertion)axiom).getRole()) +					
					"<individual name=\"" + ((RoleAssertion)axiom).getIndividual2().getName() + "\"/></related>\n");
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
	
	public static StringBuilder getDIGString(Role role) {
		if(role instanceof AtomicRole)
			return new StringBuilder("<ratom name=\"" + ((AtomicRole)role).getName() + "\"/>");
		else if(role instanceof InverseRole)
			return new StringBuilder("<inverse><ratom name=\"" + ((InverseRole)role).getName() + "\"/></inverse>");
		
		throw new RuntimeException("Can only create DIG Strings for atomic and inverse roles, not for " + role + ".");
	}

	
}