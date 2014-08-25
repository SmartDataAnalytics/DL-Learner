/**
 * 
 */
package org.dllearner.core;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Lorenz Buehmann
 *
 */
public class ConsoleAxiomLearningProgressMonitor implements AxiomLearningProgressMonitor{
	
	private static final NumberFormat format = DecimalFormat.getPercentInstance();
	
	private int lastPercentage;

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningStarted(java.lang.String)
	 */
	@Override
	public void learningStarted(String algorithmName) {
		System.out.print("Learning");
		if(algorithmName != null){
			System.out.print(" of " + algorithmName);
		}
		System.out.println(" ...");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningStopped()
	 */
	@Override
	public void learningStopped() {
		System.out.println("    ... finished");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningProgressChanged(int, int)
	 */
	@Override
	public void learningProgressChanged(int value, int max) {
		if (max > 0) {
            int percent = value * 100 / max;
            if (lastPercentage != percent) {
                System.out.print("    ");
                System.out.println(percent + "%");
                lastPercentage = percent;
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningTaskBusy()
	 */
	@Override
	public void learningTaskBusy() {
		System.out.println(" ...");
	}

}
