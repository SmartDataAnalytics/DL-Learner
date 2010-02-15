package org.dllearner.utilities.experiments;

import org.apache.log4j.Logger;

public class IteratedConfig extends ExperimentConfig{
	

	private static final Logger logger = Logger.getLogger(IteratedConfig.class);

	public int resultLimit = -1;
	
	public int splits = 5;
	public int initialsplits = 5;
	
	public boolean useStartClass = true; 
	public boolean searchTree = false; 
	public int noise = 5;
	//sets ValueFrequency treshold and maxExecution time
	public boolean adaptMaxRuntime = true;
		public int maxExecutionTime = 20;
		public double factor = 2.0d ;//1.5d;
	public boolean useDataHasValue = true;
	 
//	private String highestPrecision = "";
//	private String highestRecall = "";
//	private String highestFMeasure = "";
	
	public IteratedConfig(String label, int iterations) {
		super(label, iterations);
	}
	
	//reached iterations
	//reached 100% 
	
	public boolean stopCondition(int iteration, double precision, double recall, double fmeasure, String concept){
		if(iteration == 0){
			//skip first;
			return true;
		}
		logger.info("Testing stop condition (iter: "+iteration+" ) " );
		
//		if(higher(iterationPrecision, precision)){highestPrecision=concept;}
//		if(higher(iterationRecall, recall)){highestRecall=concept;}
//		if(higher(iterationFmeasure, fmeasure)){highestFMeasure=concept;}
		
		boolean condIter = (iteration<this.iterations);
		boolean condPrec = fmeasure <=1.0d;
		if(!condIter){
			logger.info("iterations reached, stopping");
			return false;
		}else if(!condPrec){
			logger.info("fmeasure reached, stopping");
			return false;
		}else{
			return true;
		}
	}
	
	
	
	
		
//		String pre = "\n*********\n"+label+"\n";
//		pre +="highestPrecision: "+highestPrecision+"\n";
//		pre +="highestRecall: "+highestRecall+"\n";
//		pre +="highestFMeasure: "+highestFMeasure+"\n";
//		
//		
//		
//		String precision = "Precision:\n";
//		String hits = "hits:\n";
//		String recall = "Recall:\n";
//		String fmeasure = "F-Measure:\n";
//		String learningtime = "learningtime:\n";
//		String totaltime = "Totaltime:\n";
		
//		for (int i = 0; i < iterationPrecision.length; i++) {
//			precision+=iterationPrecision[i].getAvg()+"\n";
//			hits+=iterationPrecision[i].getHits()+" | ";
//			recall+=iterationRecall[i].getAvg()+"\n";
//			fmeasure+=iterationFmeasure[i].getAvg()+"\n";
//			learningtime+=iterationLearningTime[i].getAvg()+"\n";
//			totaltime+=iterationTotalTime[i].getAvg()+"\n";
//		}	                   

//		return pre+precision+recall+fmeasure+hits+"\n"+learningtime+totaltime;
//	}
//	public static double precision( int posAsPos, int retrieved){
//		double precision = ((double)posAsPos)/((double)retrieved);
//		logger.info("Precision: "+df.format(precision));
//		return precision;
//	}
//	public static double recall( int posAsPos, int allPositives){
//		double recall = ((double)posAsPos)/((double)allPositives);
//		
//		logger.info("Recall: "+df.format(recall));
//		return recall;
//		
//	}
	
	
	
	
}
