package org.dllearner.core;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * An axiom learning progress monitor that is doing nothing.
 *
 * @author Lorenz Buehmann
 *
 */
public class SilentAxiomLearningProgressMonitor implements AxiomLearningProgressMonitor{

	@Override
	public void learningStarted(AxiomType<? extends OWLAxiom> axiomType) {}

	@Override
	public void learningStopped(AxiomType<? extends OWLAxiom> axiomType) {}

	@Override
	public void learningProgressChanged(AxiomType<? extends OWLAxiom> axiomType, int value, int max) {}

	@Override
	public void learningTaskBusy(AxiomType<? extends OWLAxiom> axiomType) {}

	@Override
	public void learningFailed(AxiomType<? extends OWLAxiom> axiomType) {}
}
