package org.dllearner.algorithms.parcel;


/**
 * Interface for a ParCELearner Bean 
 * 
 * @author An C. Tran
 *
 */
public interface ParCELearnerMBean {

	public int getActiveCount();
	public long getCompleteTaskCount();
	public long getTaskCount();
	public int getUncoveredPositiveExamples();
	
	public boolean isTerminiated();
	public boolean isShutdown();	
}
