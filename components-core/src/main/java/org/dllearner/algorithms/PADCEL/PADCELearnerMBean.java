package org.dllearner.algorithms.PADCEL;


/**
 * Interface for a PaDCELearner Bean 
 * 
 * @author An C. Tran
 *
 */
public interface PADCELearnerMBean {

	public int getActiveCount();
	public long getCompleteTaskCount();
	public long getTaskCount();
	public int getUncoveredPositiveExamples();
	
	public boolean isTerminiated();
	public boolean isShutdown();	
}
