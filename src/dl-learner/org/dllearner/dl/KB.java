package org.dllearner.dl;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class KB implements KBElement {

	private Set<AssertionalAxiom> abox = new HashSet<AssertionalAxiom>();
	private Set<TerminologicalAxiom> tbox = new HashSet<TerminologicalAxiom>();
	private Set<RBoxAxiom> rbox = new HashSet<RBoxAxiom>();
	
	public SortedSet<Individual> findAllIndividuals() {
		SortedSet<Individual> individuals = new TreeSet<Individual>();
		
		for(Axiom axiom : abox) {
			if(axiom instanceof RoleAssertion) {
				individuals.add(((RoleAssertion)axiom).getIndividual1());
				individuals.add(((RoleAssertion)axiom).getIndividual2());
			} else if(axiom instanceof ConceptAssertion) {
				individuals.add(((ConceptAssertion)axiom).getIndividual());
			}	
		}		
		
		return individuals;
	}
	
	public Set<AtomicRole> findAllAtomicRoles() {
		Set<String> roleNames = new HashSet<String>();
		
		for(Axiom axiom : abox) {
			if(axiom instanceof RoleAssertion)
				roleNames.add(((RoleAssertion)axiom).getRole().getName());
		}
		
		for(Axiom axiom : tbox) {
			if(axiom instanceof Inclusion) {
				roleNames.addAll(findAllRoleNames(((Inclusion)axiom).getSubConcept()));
				roleNames.addAll(findAllRoleNames(((Inclusion)axiom).getSuperConcept()));
			} else if(axiom instanceof Equality) {
				roleNames.addAll(findAllRoleNames(((Equality)axiom).getConcept1()));
				roleNames.addAll(findAllRoleNames(((Equality)axiom).getConcept2()));
			}
		}
		
		for(Axiom axiom : rbox) {
			if(axiom instanceof SymmetricRoleAxiom)
				roleNames.add(((SymmetricRoleAxiom)axiom).getRole().getName());
			else if(axiom instanceof TransitiveRoleAxiom)
				roleNames.add(((TransitiveRoleAxiom)axiom).getRole().getName());
			else if(axiom instanceof FunctionalRoleAxiom)
				roleNames.add(((FunctionalRoleAxiom)axiom).getRole().getName());	
			else if(axiom instanceof SubRoleAxiom) {
				roleNames.add(((SubRoleAxiom)axiom).getRole().getName());
				roleNames.add(((SubRoleAxiom)axiom).getSubRole().getName());
			} else if(axiom instanceof InverseRoleAxiom) {
				roleNames.add(((InverseRoleAxiom)axiom).getRole().getName());
				roleNames.add(((InverseRoleAxiom)axiom).getInverseRole().getName());
			}		
		}
		
		Set<AtomicRole> ret = new HashSet<AtomicRole>();
		for(String name : roleNames) {
			ret.add(new AtomicRole(name));
		}
		return ret;		
	}
	
	public Set<String> findAllRoleNames(Concept concept) {
		Set<String> ret = new TreeSet<String>();
		
		if(concept instanceof Quantification)
			ret.add(((Quantification)concept).getRole().getName());
		
		for(Concept child : concept.getChildren())
			ret.addAll(findAllRoleNames(child));		
		
		return ret;
	}
	
	public Set<AtomicConcept> findAllAtomicConcepts() {
		// erstmal eine Menge von Konzeptnamen finden, dadurch ist sichergestellt,
		// dass kein Name zweimal auftaucht (wenn später mal ein Comparator
		// für Konzepte implementiert ist, dann ist dieser Zwischenschritt 
		// nicht mehr notwendig)
		Set<String> conceptNames = new HashSet<String>();
		
		for(Axiom axiom : abox) {
			if(axiom instanceof ConceptAssertion)
				conceptNames.addAll(findAllConceptNames(((ConceptAssertion)axiom).getConcept()));
		}
		
		for(Axiom axiom : tbox) {
			if(axiom instanceof Inclusion) {
				conceptNames.addAll(findAllConceptNames(((Inclusion)axiom).getSubConcept()));
				conceptNames.addAll(findAllConceptNames(((Inclusion)axiom).getSuperConcept()));
			} else if(axiom instanceof Equality) {
				conceptNames.addAll(findAllConceptNames(((Equality)axiom).getConcept1()));
				conceptNames.addAll(findAllConceptNames(((Equality)axiom).getConcept2()));
			}
		}
		
		Set<AtomicConcept> ret = new HashSet<AtomicConcept>();
		for(String name : conceptNames) {
			ret.add(new AtomicConcept(name));
		}
		return ret;
	}
	
	private Set<String> findAllConceptNames(Concept concept) {
		Set<String> ret = new TreeSet<String>();

		if(concept instanceof AtomicConcept) {
			ret.add(((AtomicConcept)concept).getName());
		}
			
		for(Concept child : concept.getChildren())
			ret.addAll(findAllConceptNames(child));

		return ret;
	}
	
	public Set<AssertionalAxiom> getAbox() {
		return abox;
	}

	public Set<RBoxAxiom> getRbox() {
		return rbox;
	}

	public Set<TerminologicalAxiom> getTbox() {
		return tbox;
	}

	public void addABoxAxiom(AssertionalAxiom axiom) {
		abox.add(axiom);
	}
	
	public void addTBoxAxiom(TerminologicalAxiom axiom) {
		tbox.add(axiom);
	}
	
	public void addRBoxAxiom(RBoxAxiom axiom) {
		rbox.add(axiom);
	}

	public int getLength() {
		int length = 0;
		for(Axiom a : abox)
			length += a.getLength();
		for(Axiom a : tbox)
			length += a.getLength();
		for(Axiom a : rbox)
			length += a.getLength();
		return length;
	}
	
	@Override		
	public String toString() {
		String str = "TBox["+tbox.size()+"]:\n";
		for(Axiom a : tbox)
			str += "  " + a.toString()+"\n";
		str += "RBox["+rbox.size()+"]:\n";
		for(Axiom a : rbox)
			str += "  " + a.toString()+"\n";
		str += "ABox["+abox.size()+"]:\n";
		for(Axiom a : abox)
			str += "  " + a.toString()+"\n";
		return str;
	}
	

	public Set<Individual> findRelatedIndividuals(Individual individual) {
		return findRelatedIndividuals(individual, new TreeSet<Individual>());
	}
	
	@SuppressWarnings("unchecked")	
	public Set<Individual> findRelatedIndividuals(Individual individual, TreeSet<Individual> searchedIndividuals) {
		// Individual als durchsucht markieren (wir können nicht davon ausgehen,
		// dass es sich um einen Individuenbaum handelt, also müssen wir dafür
		// sorgen, dass jedes Individual nur einmal durchsucht wird)
		searchedIndividuals.add(individual);
		
		// alle direkt mit diesem Individual verbundenen Individuals finden
		TreeSet<Individual> connectedSet = new TreeSet<Individual>();
		for(AssertionalAxiom axiom : abox) {
			if(axiom instanceof RoleAssertion) {
				RoleAssertion ra = (RoleAssertion)axiom;
				if(ra.getIndividual1().equals(individual))
					connectedSet.add(ra.getIndividual2());
			}
		}
		
		TreeSet<Individual> connectedSetCopy = (TreeSet<Individual>) connectedSet.clone();
		// rekursive Aufrufe
		for(Individual i : connectedSetCopy) {
			if(!searchedIndividuals.contains(i))
				connectedSet.addAll(findRelatedIndividuals(i,searchedIndividuals));
		}
		
		return connectedSet;
	}
	
	public int getNumberOfAxioms() {
		return (abox.size() + tbox.size() + rbox.size());
	}
	
}
