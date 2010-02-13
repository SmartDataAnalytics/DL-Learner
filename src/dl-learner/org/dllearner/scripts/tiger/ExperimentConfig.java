package org.dllearner.scripts.tiger;

import org.dllearner.utilities.examples.Examples;

public class ExperimentConfig {

	
	public int resultLimit = -1;
	public int splits = 5;
	public int initialsplits = 30;
	
	public int iteration = 1;
	public int maxExecutionTime = 3;
	
	public boolean stopCondition(int iteration, Examples learn){
		return (iteration<this.iteration);
	}
	
}
