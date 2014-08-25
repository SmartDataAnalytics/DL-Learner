/**
 * 
 */
package org.dllearner.core;

/**
 * An axiom learning progress monitor that is doing nothing.
 * @author Lorenz Buehmann
 *
 */
public class SilentAxiomLearningProgressMonitor implements AxiomLearningProgressMonitor{

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningStarted(java.lang.String)
	 */
	@Override
	public void learningStarted(String algorithmName) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningStopped()
	 */
	@Override
	public void learningStopped() {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningProgressChanged(int, int)
	 */
	@Override
	public void learningProgressChanged(int value, int max) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AxiomLearningProgressMonitor#learningTaskBusy()
	 */
	@Override
	public void learningTaskBusy() {
	}

}
