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

package org.dllearner.core.owl;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class KB implements KBElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1114373618440145279L;
	// private Set<Axiom> axioms = new HashSet<Axiom>();
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

	/**
	 * Add another knowledge base to this one.
	 * @param kb The knowledge base to add.
	 */
	public void addKB(KB kb) {
		for(AssertionalAxiom axiom : kb.getAbox())
			abox.add(axiom);
		for(PropertyAxiom axiom : kb.getRbox())
			rbox.add(axiom);
		for(TerminologicalAxiom axiom : kb.getTbox())
			tbox.add(axiom);		
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
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		StringBuffer strbuff = new StringBuffer();
		strbuff.append("// TBox["+tbox.size()+"]:\n");
		for(Axiom a : tbox)
			strbuff.append("  " + a.toKBSyntaxString(baseURI, prefixes)+"\n");
		strbuff.append("\n// RBox["+rbox.size()+"]:\n");
		for(Axiom a : rbox)
			strbuff.append("  " + a.toKBSyntaxString(baseURI, prefixes)+"\n");
		strbuff.append("\n// ABox["+abox.size()+"]:\n");
		for(Axiom a : abox)
			strbuff.append("  " + a.toKBSyntaxString(baseURI, prefixes)+"\n");
		return strbuff.toString();
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

	/**
	 * Returns all axioms in the ontology. Note that calling this
	 * method is not efficient for large knowledge bases, since
	 * internally all axioms are separated into ABox, RBox, and 
	 * TBox, which means that a union of these sets is computed
	 * and returned here.
	 * 
	 * @return All axioms in the ontology.
	 */
	public Set<Axiom> getAxioms() {
		Set<Axiom> axioms = new HashSet<Axiom>();
		axioms.addAll(abox);
		axioms.addAll(rbox);
		axioms.addAll(tbox);
		return axioms;
	}
	
	public void export(File file, org.dllearner.core.OntologyFormat format){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("http://example.com");
        IRI physicalIRI = IRI.create(file.toURI());
        SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, physicalIRI);
        manager.addIRIMapper(mapper);
        OWLOntology ontology;
		try {
			ontology = manager.createOntology(ontologyIRI);
			// OWLAPIReasoner.fillOWLAPIOntology(manager,ontology,kb);
			OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, this);
			manager.saveOntology(ontology);			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (UnknownOWLOntologyException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}
}
