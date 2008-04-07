package org.dllearner.utilities;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.parser.KBParser;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLXMLOntologyFormat;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLPropertyAxiom;

import uk.ac.manchester.cs.owl.OWLObjectExactCardinalityRestrictionImpl;
import uk.ac.manchester.cs.owl.OWLObjectIntersectionOfImpl;

public class OntologyCloserOWLAPI {

	OWLOntology onto;
	OWLAPIReasoner reasoner;
	ReasoningService rs;
	HashMap<Individual, Set<OWLObjectExactCardinalityRestrictionImpl>> indToRestr;
	OWLDataFactory factory;
	OWLOntologyManager manager;
	public int numberOfStatementsChanged = 0;

	public OntologyCloserOWLAPI(OWLAPIReasoner reasoner) {
		this.reasoner = reasoner;
		this.indToRestr = new HashMap<Individual, Set<OWLObjectExactCardinalityRestrictionImpl>>();
		this.rs = new ReasoningService(reasoner);
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.onto = reasoner.getOWLAPIOntologies().get(0);
	}

	/**
	 * counts the number of roles used by each individual and assigns
	 * ExactCardinalityRestriction to Individuals
	 */
	public void applyNumberRestrictions() {
		System.out.println("apply ExactCardinalityRestriction to Individuals");
		Set<ObjectProperty> allRoles = this.rs.getAtomicRoles();
		System.out.println("found: " + allRoles.size() + " roles");
		testForTransitiveProperties(true);

		for (ObjectProperty oneRole : allRoles) {

			Map<Individual, SortedSet<Individual>> allRoleMembers = this.rs
					.getRoleMembers(oneRole);
			for (Individual oneInd : allRoleMembers.keySet()) {
				SortedSet<Individual> fillers = allRoleMembers.get(oneInd);
				// only where roles exist
				if (fillers.size() > 0) {
					OWLObjectExactCardinalityRestrictionImpl oecr = new OWLObjectExactCardinalityRestrictionImpl(
							factory, factory.getOWLObjectProperty(URI
									.create(oneRole.getName())),
							fillers.size(), factory.getOWLThing());

					OWLAxiom axiom = factory.getOWLClassAssertionAxiom(factory
							.getOWLIndividual(URI.create(oneInd.getName())),
							oecr);
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

		Set<ObjectProperty> allRoles = this.rs.getAtomicRoles();
		testForTransitiveProperties(true);

		// collect info for roles and individuals
		for (ObjectProperty oneRole : allRoles) {
			Map<Individual, SortedSet<Individual>> allRoleMembers = this.rs
					.getRoleMembers(oneRole);
			for (Individual oneInd : allRoleMembers.keySet()) {
				SortedSet<Individual> fillers = allRoleMembers.get(oneInd);
				if (fillers.size() > 0) {
					OWLObjectExactCardinalityRestrictionImpl oecr = new OWLObjectExactCardinalityRestrictionImpl(
							factory, factory.getOWLObjectProperty(URI
									.create(oneRole.getName())),
							fillers.size(), factory.getOWLThing());
					collectExObjRestrForInd(oneInd, oecr);
				}
			}
		}// end for

		Set<OWLDescription> target = new HashSet<OWLDescription>();
		Set<OWLObjectExactCardinalityRestrictionImpl> s = null;

		for (Individual oneInd : indToRestr.keySet()) {
			s = indToRestr.get(oneInd);
			for (OWLObjectExactCardinalityRestrictionImpl oecr : s) {
				target.add(oecr);
			}
			// collect everything in an intersection
			OWLObjectIntersectionOfImpl intersection = new OWLObjectIntersectionOfImpl(
					this.factory, target);
			s = null;
			target = new HashSet<OWLDescription>();

			OWLAxiom axiom = factory.getOWLClassAssertionAxiom(factory
					.getOWLIndividual(URI.create(oneInd.getName())),
					intersection);
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

		boolean retval = false;

		Set<OWLPropertyAxiom> ax = onto.getObjectPropertyAxioms();
		for (OWLPropertyAxiom propertyAxiom : ax) {
			if (propertyAxiom.getClass().getSimpleName().equals(
					"OWLTransitiveObjectPropertyAxiomImpl")) {
				retval = true;
				if (printflag) {
					System.out
							.println("WARNING transitive object property can't be used in cardinality restriction\n"
									+ propertyAxiom.toString()
									+ "but I'm ignoring it");
				}
			}
		}
		if (printflag) {
			System.out.println("No transitive Properties found");
		}
		return retval;
	}

	/*
	 * public static void closeKB(KB kb) { new
	 * OntologyCloserOWLAPI(kb).applyNumberRestrictions(); }
	 */

	/**
	 * makes some retrieval queries
	 * @param conceptStr
	 * @return
	 */
	public SortedSet<Individual> verifyConcept(String conceptStr) {

		Description d;
		SimpleClock sc = new SimpleClock();
		StringBuffer sb = new StringBuffer();
		sb.append(conceptStr);
		conceptStr = sb.toString();
		SortedSet<Individual> ind = new TreeSet<Individual>();
		try {
			d = KBParser.parseConcept(conceptStr);
			System.out.println("\n*******************\nStarting retrieval");
			System.out.println(d.toManchesterSyntaxString("",
					new HashMap<String, String>()));
			// System.out.println(d.toString());
			sc.setTime();
			this.rs.retrieval(d);

			System.out.println("retrieved: " + ind.size() + " instances");
			sc.printAndSet();
			for (Individual individual : ind) {
				System.out.print(individual + "|");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ind;
	}

	public void writeOWLFile(URI filename) {
		try {
			manager.saveOntology(this.onto, new OWLXMLOntologyFormat(),
					filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean collectExObjRestrForInd(Individual ind,
			OWLObjectExactCardinalityRestrictionImpl oecr) {
		Set<OWLObjectExactCardinalityRestrictionImpl> s = indToRestr.get(ind);

		if (s == null) {

			indToRestr.put(ind,
					new HashSet<OWLObjectExactCardinalityRestrictionImpl>());
			s = indToRestr.get(ind);
		}
		return s.add(oecr);
	}

}
