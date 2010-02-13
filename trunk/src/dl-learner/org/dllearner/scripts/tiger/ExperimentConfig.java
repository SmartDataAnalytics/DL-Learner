package org.dllearner.scripts.tiger;

import org.dllearner.utilities.examples.Examples;

public class ExperimentConfig {

	
	public int resultLimit = -1;
	
	public int splits = 5;
	public int initialsplits = 10;
	public int iteration = 5;
	
	public boolean useStartClass = true; 
	public int noise = 5;
	//sets ValueFrequency treshold and maxExecution time
	public boolean adaptive = true;
	public int maxExecutionTime = 40;
	public int valueFrequencyThreshold = 3;
	 
	
	public boolean stopCondition(int iteration, Examples learn){
		return (iteration<this.iteration);
	}
	
}
