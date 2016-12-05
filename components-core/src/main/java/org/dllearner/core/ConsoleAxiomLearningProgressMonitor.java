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
package org.dllearner.core;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * A progress monitor for axiom learning algorithms which prints to the console.
 *
 * @see org.dllearner.core.AxiomLearningProgressMonitor
 * @author Lorenz Buehmann
 *
 */
public class ConsoleAxiomLearningProgressMonitor implements AxiomLearningProgressMonitor{
	
	final char[] animationChars = new char[] {'|', '/', '-', '\\'};
	
	private static final NumberFormat format = DecimalFormat.getPercentInstance(Locale.ROOT);
	
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
