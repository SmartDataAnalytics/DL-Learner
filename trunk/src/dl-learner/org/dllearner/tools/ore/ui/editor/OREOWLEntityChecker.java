package org.dllearner.tools.ore.ui.editor;

import org.semanticweb.owl.expression.OWLEntityChecker;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;

public class OREOWLEntityChecker implements OWLEntityChecker {

	private OWLEntityFinder finder;

	public OREOWLEntityChecker(OWLEntityFinder finder) {
		this.finder = finder;
	}

	public OWLClass getOWLClass(String rendering) {
		return finder.getOWLClass(rendering);
	}

	public OWLObjectProperty getOWLObjectProperty(String rendering) {
		return finder.getOWLObjectProperty(rendering);
	}

	public OWLDataProperty getOWLDataProperty(String rendering) {
		return finder.getOWLDataProperty(rendering);
	}

	public OWLIndividual getOWLIndividual(String rendering) {
		return finder.getOWLIndividual(rendering);
	}

	public OWLDataType getOWLDataType(String rendering) {
		return finder.getOWLDatatype(rendering);
	}

}
