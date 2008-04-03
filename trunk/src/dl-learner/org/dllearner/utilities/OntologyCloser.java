package org.dllearner.utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.ObjectExactCardinalityRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.PropertyAxiom;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.KBFile;
import org.dllearner.parser.KBParser;
import org.dllearner.reasoning.OWLAPIReasoner;

public class OntologyCloser {
	KB kb;
	KBFile kbFile;
	Set<KnowledgeSource> ks;
	ReasoningService rs;

	public OntologyCloser(KB kb) {
		super();
		this.kb = kb;
		this.kbFile = new KBFile(this.kb);
		this.ks = new HashSet<KnowledgeSource>();
		this.ks.add(this.kbFile);
		OWLAPIReasoner owlapi = new OWLAPIReasoner(ks);
		owlapi.init();

		this.rs = new ReasoningService(owlapi);

	}

	/**
	 * counts the number of roles used by each individual and assigns
	 * ExactCardinalityRestriction
	 */
	public void applyNumberRestrictions() {
		Set<ObjectProperty> allRoles = this.rs.getAtomicRoles();
		// Set<Individual> allind = this.rs.getIndividuals();
		Set<PropertyAxiom> ax = kb.getRbox();
		for (PropertyAxiom propertyAxiom : ax) {
			if (propertyAxiom.getClass().getSimpleName().equals(
					"TransitiveObjectPropertyAxiom")) {

				System.out
						.println("WARNING transitive object property can't be used in cardinality restriction\n"
								+ propertyAxiom.toString());
			}
		}

		for (ObjectProperty oneRole : allRoles) {

			// System.out.println(oneRole.getClass());
			Map<Individual, SortedSet<Individual>> allRoleMembers = this.rs
					.getRoleMembers(oneRole);
			for (Individual oneInd : allRoleMembers.keySet()) {
				SortedSet<Individual> fillers = allRoleMembers.get(oneInd);
				if (fillers.size() > 0) {
					ObjectExactCardinalityRestriction oecr = new ObjectExactCardinalityRestriction(
							fillers.size(), oneRole, new Thing());
					kb.addABoxAxiom(new ClassAssertionAxiom(oecr, oneInd));
				}
			}

		}
		// System.out.println("good ontology? " + rs.isSatisfiable());

	}

	public static void closeKB(KB kb) {
		new OntologyCloser(kb).applyNumberRestrictions();
	}

	public void verifyConcept(String conceptStr) {
		Description d;
		StringBuffer sb = new StringBuffer();
		sb.append(conceptStr);
		conceptStr = sb.toString();
		try {
			d = KBParser.parseConcept(conceptStr);
			System.out.println(d.toManchesterSyntaxString("", new HashMap<String,String>()));
			System.out.println(d.toString());
			System.out.println("Starting retrieval");
			SortedSet<Individual> ind = this.rs.retrieval(d);
			System.out.println("retrieved: " + ind.size() + " instances");

			//for (Individual individual : ind) {
				//System.out.println(individual + "");
			//}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
