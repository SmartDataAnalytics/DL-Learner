package org.dllearner.scripts.tiger;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.examples.Examples;

import com.jamonapi.Monitor;

public class ExperimentConfig {
	private static final Logger logger = Logger.getLogger(ExperimentConfig.class);

	public String label = "unset";
	
	public int resultLimit = -1;
	
	public int splits = 5;
	public int initialsplits = 5;
	private int iteration;
	
	public boolean useStartClass = true; 
	public boolean searchTree = false; 
	public int noise = 10;
	//sets ValueFrequency treshold and maxExecution time
	public boolean adaptMaxRuntime = true;
		public int maxExecutionTime = 20;
		public int factor = 2;
	public boolean useDataHasValue = true;
//		public int valueFrequencyThreshold = 3;
	 
	public final Monitor[] iterationPrecision;
	public final Monitor[] iterationRecall;
	public final Monitor[] iterationFmeasure;
	public final Monitor[] iterationLearningTime;
	public final Monitor[] iterationTotalTime;
	private String highestPrecision = "";
	private String highestRecall = "";
	private String highestFMeasure = "";
	
	public ExperimentConfig(int iteration, String label){
		this.iteration = iteration;
		this.label = label;
		
		iterationPrecision = new Monitor[this.iteration];
		iterationRecall = new Monitor[this.iteration];
		iterationFmeasure = new Monitor[this.iteration];
		iterationLearningTime = new Monitor[this.iteration];
		iterationTotalTime = new Monitor[this.iteration];
		for (int i = 0; i < iterationPrecision.length; i++) {
			iterationPrecision[i] = JamonMonitorLogger.getStatisticMonitor(this.getClass(), label+"_prec_i"+i);
			iterationRecall[i] = JamonMonitorLogger.getStatisticMonitor(this.getClass(), label+"_rec_i"+i);
			iterationFmeasure[i] = JamonMonitorLogger.getStatisticMonitor(this.getClass(), label+"_fme_i"+i);
			iterationLearningTime[i] = JamonMonitorLogger.getStatisticMonitor(this.getClass(), label+"_learning_i"+i);
			iterationTotalTime[i] = JamonMonitorLogger.getStatisticMonitor(this.getClass(), label+"_total_i"+i);
		}
	}
	
	//reached iterations
	//reached 100% 
	
	public boolean stopCondition(int iteration, Examples learn, SortedSet<String> posAsPos, SortedSet<String> retrieved, Examples allExamples, String concept){
		if(iteration == 0){
			//skip first;
			return true;
		}
		Monitor iterationTime = JamonMonitorLogger.getTimeMonitor(TestIterativeLearning.class, "iterationTime");
		iterationTotalTime[iteration-1].add(iterationTime.getLastValue());
		Monitor learningTime = JamonMonitorLogger.getTimeMonitor(TestIterativeLearning.class, "learningTime");
		iterationLearningTime[iteration-1].add(learningTime.getLastValue());
		logger.info("Testing stop condition (iter: "+iteration+" ) " );
		
		double precision = TestIterativeLearning.precision(posAsPos.size(), retrieved.size());
		double recall = TestIterativeLearning.recall(posAsPos.size(),allExamples.getPosTest().size());
		double fmeasure = fmeasure( precision,  recall);
		iterationPrecision[iteration-1].add(precision);
		iterationRecall[iteration-1].add(recall);
		iterationFmeasure[iteration-1].add(fmeasure);
		
		if(higher(iterationPrecision, precision)){highestPrecision=concept;}
		if(higher(iterationRecall, recall)){highestRecall=concept;}
		if(higher(iterationFmeasure, fmeasure)){highestFMeasure=concept;}
		
		logger.info("F-Measure: "+TestIterativeLearning.df.format(   fmeasure  ));
		
		boolean condIter = (iteration<this.iteration);
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
	
	public static double fmeasure(double precision, double recall){
		return (precision+recall == 0)?0.0d: (2*precision*recall)/(precision+recall);
	}
	
	public boolean higher(Monitor[] a, double current){
		for (int i = 0; i < a.length; i++) {
			if(current>a[i].getMax()){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		
		String pre = "\n*********\n"+label+"\n";
		pre +="highestPrecision: "+highestPrecision+"\n";
		pre +="highestRecall: "+highestRecall+"\n";
		pre +="highestFMeasure: "+highestFMeasure+"\n";
		
		String precision = "Precision:\n";
		String hits = "hits:\n";
		String recall = "Recall:\n";
		String fmeasure = "F-Measure:\n";
		String learningtime = "learningtime:\n";
		String totaltime = "Totaltime:\n";
		
		for (int i = 0; i < iterationPrecision.length; i++) {
			precision+=iterationPrecision[i].getAvg()+"\n";
			hits+=iterationPrecision[i].getHits()+" | ";
			recall+=iterationRecall[i].getAvg()+"\n";
			fmeasure+=iterationFmeasure[i].getAvg()+"\n";
			learningtime+=iterationLearningTime[i].getAvg()+"\n";
			totaltime+=iterationTotalTime[i].getAvg()+"\n";
		}	                   

		return pre+precision+recall+fmeasure+hits+"\n"+learningtime+totaltime;
	}
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
