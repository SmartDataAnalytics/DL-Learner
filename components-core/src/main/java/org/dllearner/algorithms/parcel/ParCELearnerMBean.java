package org.dllearner.algorithms.parcel;


/**
 * Interface for a ParCELearner Bean 
 * 
 * @author An C. Tran
 *
 */
public interface ParCELearnerMBean {

	long getTotalDescriptions();
	int getCurrentlyBestDescriptionLength();
	double getCurrentlyBestAccuracy();
	int getWorkerPoolSize();
	int getSearchTreeSize();
	int getCurrentlyMaxExpansion();
}
