package org.dllearner.core;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * A progress monitor for axiom learning algorithms which prints to the console.
 *
 * @see org.dllearner.core.AxiomLearningProgressMonitor
 * @author Lorenz Buehmann
 *
 */
public class ConsoleAxiomLearningProgressMonitor implements AxiomLearningProgressMonitor{
	
	final char[] animationChars = new char[] {'|', '/', '-', '\\'};
	
	private static final NumberFormat format = DecimalFormat.getPercentInstance();
	
	private int lastPercentage;

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningStarted(java.lang.String)
	 */
	@Override
	public void learningStarted(AxiomType<? extends OWLAxiom> axiomType) {
		System.out.print("Learning");
		if(axiomType != null){
			System.out.print(" of " + axiomType.getName() + " axioms");
		}
		System.out.println(" ...");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningStopped()
	 */
	@Override
	public void learningStopped(AxiomType<? extends OWLAxiom> axiomType) {
		System.out.print(" ... ");
		if(axiomType != null){
			System.out.print(axiomType.getName() + " axioms ");
		}
		System.out.println("finished");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningProgressChanged(int, int)
	 */
	@Override
	public void learningProgressChanged(AxiomType<? extends OWLAxiom> axiomType, int value, int max) {
		if (max > 0) {
            int percent = value * 100 / max;
            if (lastPercentage != percent) {
                System.out.print("    " + percent + "%" + "\r");
//                System.out.println();
                lastPercentage = percent;
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningTaskBusy()
	 */
	@Override
	public void learningTaskBusy(AxiomType<? extends OWLAxiom> axiomType) {
		System.out.println(axiomType.getName() + " ...");
	}
	
	@Override
	public void learningFailed(AxiomType<? extends OWLAxiom> axiomType) {
		System.err.println("Learning ");
		if(axiomType != null){
			System.err.println("of " + axiomType.getName() + " axioms ");
		}
		System.err.println("failed.");
	}

}
