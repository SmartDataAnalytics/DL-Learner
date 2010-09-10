package org.dllearner.tools.ore.ui.editor;

import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

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

	public OWLNamedIndividual getOWLIndividual(String rendering) {
		return finder.getOWLIndividual(rendering);
	}

	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String rendering) {
		return finder.getOWLAnnotationProperty(rendering);
	}

	@Override
	public OWLDatatype getOWLDatatype(String rendering) {
		return finder.getOWLDatatype(rendering);
	}

}
