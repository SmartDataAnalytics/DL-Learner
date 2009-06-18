package org.dllearner.tools.ore.explanation;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;

public class AxiomSelector {

	public static Set<OWLAxiom> getSyntacticRelevantAxioms(
			OWLOntology ontology, OWLClass cl) {

		Set<OWLAxiom> relevantAxioms = new HashSet<OWLAxiom>();

		for (OWLAxiom ax : ontology.getLogicalAxioms()) {
			if (isSyntacticRelevant(ax,cl)) {
				relevantAxioms.add(ax);
			}
		}

		return relevantAxioms;
	}
	
	public static Set<OWLAxiom> getSyntacticRelevantAxioms(
			OWLOntology ontology, Set<OWLAxiom> axioms) {

		Set<OWLAxiom> relevantAxioms = new HashSet<OWLAxiom>();

		for (OWLAxiom ax : ontology.getLogicalAxioms()) {
			for (OWLAxiom ax2 : axioms) {
				if (isSyntacticRelevant(ax, ax2)) {
					relevantAxioms.add(ax);
				}
			}

		}

		return relevantAxioms;
	}

	private static boolean isSyntacticRelevant(OWLAxiom ax1, OWLAxiom ax2) {
		return org.mindswap.pellet.utils.SetUtils.intersects(
				ax1.getSignature(), ax2.getSignature());
	}

	private static boolean isSyntacticRelevant(OWLAxiom ax, OWLClass cl) {
		return org.mindswap.pellet.utils.SetUtils.intersects(ax.getSignature(),
				cl.getSignature());
	}
}
