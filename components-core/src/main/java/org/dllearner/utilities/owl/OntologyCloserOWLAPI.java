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
package org.dllearner.utilities.owl;

import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;

import java.net.URI;
import java.util.*;

public class OntologyCloserOWLAPI {

	OWLOntology onto;
	OWLAPIReasoner rs;
//	ReasonerComponent rs;
	HashMap<OWLIndividual, Set<OWLObjectExactCardinality>> indToRestr;
	OWLDataFactory factory;
	OWLOntologyManager manager;
	public int numberOfStatementsChanged = 0;

	public OntologyCloserOWLAPI(OWLAPIReasoner reasoner) {
		this.rs = reasoner;
		this.indToRestr = new HashMap<>();
//		this.rs = new ReasonerComponent(reasoner);
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.onto = reasoner.getOntology();
	}

	/**
	 * counts the number of roles used by each individual and assigns
	 * ExactCardinalityRestriction to Individuals
	 */
	public void applyNumberRestrictions() {
		System.out.println("apply ExactCardinalityRestriction to Individuals");
		Set<OWLObjectProperty> allRoles = this.rs.getObjectProperties();
		System.out.println("found: " + allRoles.size() + " roles");
		testForTransitiveProperties(true);

		for (OWLObjectProperty oneRole : allRoles) {

			Map<OWLIndividual, SortedSet<OWLIndividual>> allRoleMembers = this.rs
					.getPropertyMembers(oneRole);
			for (OWLIndividual oneInd : allRoleMembers.keySet()) {
				SortedSet<OWLIndividual> fillers = allRoleMembers.get(oneInd);
				// only where roles exist
				if (fillers.size() > 0) {
					OWLObjectExactCardinality oecr = factory
							.getOWLObjectExactCardinality(fillers.size(), factory.getOWLObjectProperty(IRI
									.create(oneRole.toStringID())), factory.getOWLThing());

					OWLAxiom axiom = factory.getOWLClassAssertionAxiom(oecr, factory
							.getOWLNamedIndividual(IRI.create(oneInd.toStringID())));
					AddAxiom addAxiom = new AddAxiom(this.onto, axiom);
					try {
						// add change
						manager.applyChange(addAxiom);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}

	}

	/**
	 * counts the number of roles used by each individual and assigns
	 * ExactCardinalityRestriction, but uses only one Intersection: role1
	 * exactly 1 and role2 exactly 2 and ....
	 * 
	 */
	public void applyNumberRestrictionsConcise() {

		Set<OWLObjectProperty> allRoles = this.rs.getObjectProperties();
		testForTransitiveProperties(true);

		// collect info for roles and individuals
		for (OWLObjectProperty oneRole : allRoles) {
			Map<OWLIndividual, SortedSet<OWLIndividual>> allRoleMembers = this.rs
					.getPropertyMembers(oneRole);
			for (OWLIndividual oneInd : allRoleMembers.keySet()) {
				SortedSet<OWLIndividual> fillers = allRoleMembers.get(oneInd);
				if (fillers.size() > 0) {
					OWLObjectExactCardinality oecr = factory.getOWLObjectExactCardinality(fillers.size(), factory.getOWLObjectProperty(IRI
									.create(oneRole.toStringID())),
							factory.getOWLThing());
					collectExObjRestrForInd(oneInd, oecr);
				}
			}
		}// end for

		Set<OWLClassExpression> target = new HashSet<>();
		Set<OWLObjectExactCardinality> s = null;

		for (OWLIndividual oneInd : indToRestr.keySet()) {
			s = indToRestr.get(oneInd);
			for (OWLObjectExactCardinality oecr : s) {
				target.add(oecr);
			}
			// collect everything in an intersection
			OWLObjectIntersectionOf intersection = factory.getOWLObjectIntersectionOf(target);
			s = null;
			target = new HashSet<>();

			OWLAxiom axiom = factory.getOWLClassAssertionAxiom(intersection, factory
					.getOWLNamedIndividual(IRI.create(oneInd.toStringID())));
			AddAxiom addAxiom = new AddAxiom(this.onto, axiom);
			try {
				manager.applyChange(addAxiom);
				numberOfStatementsChanged++;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}// end for
	}

	/**
	 * @param printflag
	 *            boolean for some output
	 * @return true if some roles are transitive
	 */
	public boolean testForTransitiveProperties(boolean printflag) {

		Set<OWLTransitiveObjectPropertyAxiom> ax = onto.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY);
		boolean retval = !ax.isEmpty();
		for (OWLPropertyAxiom propertyAxiom : ax) {
			if (printflag) {
				System.out.println("WARNING transitive object property can't be used in cardinality restriction\n"
						+ propertyAxiom.toString() + "but I'm ignoring it");
			}
		}
		if (printflag) {
			System.out.println("No transitive Properties found");
		}
		return retval;
	}

	public void writeOWLFile(URI filename) {
		try {
			manager.saveOntology(this.onto, new RDFXMLDocumentFormat(),
					IRI.create(filename));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean collectExObjRestrForInd(OWLIndividual ind,
			OWLObjectExactCardinality oecr) {
		Set<OWLObjectExactCardinality> s = indToRestr.get(ind);

		if (s == null) {

			indToRestr.put(ind,
					new HashSet<>());
			s = indToRestr.get(ind);
		}
		return s.add(oecr);
	}

}
