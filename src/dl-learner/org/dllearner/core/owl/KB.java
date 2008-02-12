package org.dllearner.core.owl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class KB implements KBElement {

	private Set<AssertionalAxiom> abox = new HashSet<AssertionalAxiom>();
	private Set<TerminologicalAxiom> tbox = new HashSet<TerminologicalAxiom>();
	private Set<PropertyAxiom> rbox = new HashSet<PropertyAxiom>();
	
	public SortedSet<Individual> findAllIndividuals() {
		SortedSet<Individual> individuals = new TreeSet<Individual>();
		
		for(Axiom axiom : abox) {
			if(axiom instanceof ObjectPropertyAssertion) {
				individuals.add(((ObjectPropertyAssertion)axiom).getIndividual1());
				individuals.add(((ObjectPropertyAssertion)axiom).getIndividual2());
			} else if(axiom instanceof ClassAssertionAxiom) {
				individuals.add(((ClassAssertionAxiom)axiom).getIndividual());
			}	
		}		
		
		return individuals;
	}
	
	public Set<ObjectProperty> findAllAtomicRoles() {
		Set<String> roleNames = new HashSet<String>();
		
		for(Axiom axiom : abox) {
			if(axiom instanceof ObjectPropertyAssertion)
				roleNames.add(((ObjectPropertyAssertion)axiom).getRole().getName());
		}
		
		for(Axiom axiom : tbox) {
			if(axiom instanceof SubClassAxiom) {
				roleNames.addAll(findAllRoleNames(((SubClassAxiom)axiom).getSubConcept()));
				roleNames.addAll(findAllRoleNames(((SubClassAxiom)axiom).getSuperConcept()));
			} else if(axiom instanceof EquivalentClassesAxiom) {
				roleNames.addAll(findAllRoleNames(((EquivalentClassesAxiom)axiom).getConcept1()));
				roleNames.addAll(findAllRoleNames(((EquivalentClassesAxiom)axiom).getConcept2()));
			}
		}
		
		for(Axiom axiom : rbox) {
			if(axiom instanceof SymmetricObjectPropertyAxiom)
				roleNames.add(((SymmetricObjectPropertyAxiom)axiom).getRole().getName());
			else if(axiom instanceof TransitiveObjectPropertyAxiom)
				roleNames.add(((TransitiveObjectPropertyAxiom)axiom).getRole().getName());
			else if(axiom instanceof FunctionalObjectPropertyAxiom)
				roleNames.add(((FunctionalObjectPropertyAxiom)axiom).getRole().getName());	
			else if(axiom instanceof SubObjectPropertyAxiom) {
				roleNames.add(((SubObjectPropertyAxiom)axiom).getRole().getName());
				roleNames.add(((SubObjectPropertyAxiom)axiom).getSubRole().getName());
			} else if(axiom instanceof InverseObjectPropertyAxiom) {
				roleNames.add(((InverseObjectPropertyAxiom)axiom).getRole().getName());
				roleNames.add(((InverseObjectPropertyAxiom)axiom).getInverseRole().getName());
			}		
		}
		
		Set<ObjectProperty> ret = new HashSet<ObjectProperty>();
		for(String name : roleNames) {
			ret.add(new ObjectProperty(name));
		}
		return ret;		
	}
	
	public Set<String> findAllRoleNames(Description concept) {
		Set<String> ret = new TreeSet<String>();
		
		if(concept instanceof ObjectQuantorRestriction)
			ret.add(((ObjectQuantorRestriction)concept).getRole().getName());
		
		for(Description child : concept.getChildren())
			ret.addAll(findAllRoleNames(child));		
		
		return ret;
	}
	
	public Set<NamedClass> findAllAtomicConcepts() {
		// erstmal eine Menge von Konzeptnamen finden, dadurch ist sichergestellt,
		// dass kein Name zweimal auftaucht (wenn später mal ein Comparator
		// für Konzepte implementiert ist, dann ist dieser Zwischenschritt 
		// nicht mehr notwendig)
		Set<String> conceptNames = new HashSet<String>();
		
		for(Axiom axiom : abox) {
			if(axiom instanceof ClassAssertionAxiom)
				conceptNames.addAll(findAllConceptNames(((ClassAssertionAxiom)axiom).getConcept()));
		}
		
		for(Axiom axiom : tbox) {
			if(axiom instanceof SubClassAxiom) {
				conceptNames.addAll(findAllConceptNames(((SubClassAxiom)axiom).getSubConcept()));
				conceptNames.addAll(findAllConceptNames(((SubClassAxiom)axiom).getSuperConcept()));
			} else if(axiom instanceof EquivalentClassesAxiom) {
				conceptNames.addAll(findAllConceptNames(((EquivalentClassesAxiom)axiom).getConcept1()));
				conceptNames.addAll(findAllConceptNames(((EquivalentClassesAxiom)axiom).getConcept2()));
			}
		}
		
		Set<NamedClass> ret = new HashSet<NamedClass>();
		for(String name : conceptNames) {
			ret.add(new NamedClass(name));
		}
		return ret;
	}
	
	private Set<String> findAllConceptNames(Description concept) {
		Set<String> ret = new TreeSet<String>();

		if(concept instanceof NamedClass) {
			ret.add(((NamedClass)concept).getName());
		}
			
		for(Description child : concept.getChildren())
			ret.addAll(findAllConceptNames(child));

		return ret;
	}
	
	public Set<AssertionalAxiom> getAbox() {
		return abox;
	}

	public Set<PropertyAxiom> getRbox() {
		return rbox;
	}

	public Set<TerminologicalAxiom> getTbox() {
		return tbox;
	}

	/**
	 * Convenience method, which adds an axiom to ABox, RBox, or
	 * TBox depending on whether it is an assertional, role, or
	 * terminological axiom.
	 * 
	 * @param axiom Axiom to add.
	 */
	public void addAxiom(Axiom axiom) {
		if(axiom instanceof AssertionalAxiom)
			addABoxAxiom((AssertionalAxiom) axiom);
		else if(axiom instanceof PropertyAxiom)
			addRBoxAxiom((PropertyAxiom) axiom);
		else if(axiom instanceof TerminologicalAxiom)
			addTBoxAxiom((TerminologicalAxiom) axiom);
		else
			throw new Error(axiom + " has unsupported axiom type.");
	}
	
	public void addABoxAxiom(AssertionalAxiom axiom) {
		abox.add(axiom);
	}
	
	public void addTBoxAxiom(TerminologicalAxiom axiom) {
		tbox.add(axiom);
	}
	
	public void addRBoxAxiom(PropertyAxiom axiom) {
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
		
	public String toString(String baseURI, Map<String,String> prefixes) {
		String str = "TBox["+tbox.size()+"]:\n";
		for(Axiom a : tbox)
			str += "  " + a.toString(baseURI, prefixes)+"\n";
		str += "RBox["+rbox.size()+"]:\n";
		for(Axiom a : rbox)
			str += "  " + a.toString(baseURI, prefixes)+"\n";
		str += "ABox["+abox.size()+"]:\n";
		for(Axiom a : abox)
			str += "  " + a.toString(baseURI, prefixes)+"\n";
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
			if(axiom instanceof ObjectPropertyAssertion) {
				ObjectPropertyAssertion ra = (ObjectPropertyAssertion)axiom;
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
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
	
}
