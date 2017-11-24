package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Wraps an OWL axiom that denotes a pattern and whether it's asserted or a generalized version of an asserted
 * pattern.
 *
 * @author Lorenz Buehmann
 */
public class OWLAxiomPattern {

	private OWLAxiom axiom;
	private boolean asserted = true;

	public OWLAxiomPattern(OWLAxiom axiom, boolean asserted) {
		this.axiom = axiom;
		this.asserted = asserted;
	}

	/**
	 * @return the OWL axiom
	 */
	public OWLAxiom getAxiom() {
		return axiom;
	}

	/**
	 * @return whether it's asserted or a generalized version of an asserted
	 * pattern
	 */
	public boolean isAsserted() {
		return asserted;
	}

	@Override
	public String toString() {
		return "OWLAxiomPattern{" +
				"axiom=" + axiom +
				", asserted=" + asserted +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OWLAxiomPattern)) return false;

		OWLAxiomPattern that = (OWLAxiomPattern) o;

		if (asserted != that.asserted) return false;
		return axiom != null ? axiom.equals(that.axiom) : that.axiom == null;
	}

	@Override
	public int hashCode() {
		int result = axiom != null ? axiom.hashCode() : 0;
		result = 31 * result + (asserted ? 1 : 0);
		return result;
	}
}
