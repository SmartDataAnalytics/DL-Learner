package org.dllearner.utilities.examples;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.scripts.tiger.ExperimentConfig;
import org.dllearner.utilities.Files;

public class ExperimentCollector {

	public static String dir = "results/";
	public String details ;
	public String totalGNU ;
	public String totalLatex ;
	public String timeGNU ;
	public String timeLatex ;

	public static DecimalFormat df = new DecimalFormat(".####");
	public static DecimalFormat dfhuman = new DecimalFormat("##.##%");
	public static DecimalFormat dfRuntime = new DecimalFormat("####.");
	List<ExperimentConfig> experimentConfigs = new ArrayList<ExperimentConfig>();

	public ExperimentCollector(String filePrefix) {
		details = dir + filePrefix + "_" + "details";
		totalGNU = dir + filePrefix + "_" + "totalGNU";
		totalLatex = dir + filePrefix + "_" + "totalLatex";
		timeGNU = dir + filePrefix + "_" + "timeGNU";
		timeLatex = dir + filePrefix + "_" + "timeLatex";
	}

	public void addExperimentConfig(ExperimentConfig experimentConfig) {
		experimentConfigs.add(experimentConfig);
	}

	public void write(int iterations) {
		Files.appendFile(new File(details), "");
		Files.appendFile(new File(timeGNU), "\n***********\n\n");
		Files.appendFile(new File(timeLatex), "\n***********\n\n");
		Files.appendFile(new File(totalLatex), "\n***********\n\n");
		Files.appendFile(new File(totalGNU), "\n***********\n\n");
		String headerGNU = "\t";
		String headerLatex = "\t&\t";
		for (ExperimentConfig ec : experimentConfigs) {
			 headerGNU += ec.label + "\t";
 			 Files.appendFile(new File(details), ec.toString());
		}
		for (int i = 0; i < iterations; i++) {
			headerLatex += (i+1) + "\t&\t";
		}
		
		Files.appendFile(new File(totalGNU), headerGNU + "\n");
		Files.appendFile(new File(totalLatex), headerLatex + "\n");

		for (int i = 0; i < iterations; i++) {
			String fmeasureGNU = i + "\t";
			String learningTimeGNU = i + "\t";
			String totalTimeGNU  = i + "\t";
			for (ExperimentConfig ec : experimentConfigs) {
				fmeasureGNU += df.format(ec.iterationFmeasure[i].getAvg()) + "\t";
				learningTimeGNU+= df.format(ec.iterationLearningTime[i].getAvg())+"\t";
				totalTimeGNU+= df.format(ec.iterationTotalTime[i].getAvg())+"\t";
			}
			Files.appendFile(new File(totalGNU), fmeasureGNU + "\n");
			Files.appendFile(new File(timeGNU), learningTimeGNU + "\n");
			Files.appendFile(new File(timeGNU), totalTimeGNU + "\n");
		}

		for (ExperimentConfig ec : experimentConfigs) {
			String label = ec.label + "\t&\t";
			String learningTimeLatex = label+"learn";
			String totalTimeLatex = label+"total";
			String fmeasureLatex = label;
			for (int i = 0; i < iterations; i++) {
				learningTimeLatex += dfRuntime.format(ec.iterationLearningTime[i].getAvg()) + "\t&\t";
				totalTimeLatex += dfRuntime.format(ec.iterationTotalTime[i].getAvg()) + "\t&\t";
				fmeasureLatex += dfhuman.format(ec.iterationFmeasure[i].getAvg()) + "\t&\t";
			}
			Files.appendFile(new File(timeLatex), learningTimeLatex + "\n");
			Files.appendFile(new File(timeLatex), totalTimeLatex + "\n");
			Files.appendFile(new File(timeLatex), "\n\n\n");
			Files.appendFile(new File(totalLatex), fmeasureLatex + "\n");
			
		}
		for (ExperimentConfig ec : experimentConfigs) {
			String label = ec.label + "\t&\t";
			String learningTimeHuman = label+"learn";
			String totalTimeHuman = label+"total";
			String fmeasureHuman = label;
			for (int i = 0; i < iterations; i++) {
				learningTimeHuman += dfRuntime.format(ec.iterationLearningTime[i].getAvg()) +" ("+dfRuntime.format(ec.iterationLearningTime[i].getStdDev()) + ")\t&\t";
				totalTimeHuman += dfRuntime.format(ec.iterationTotalTime[i].getAvg()) +" ("+ dfRuntime.format(ec.iterationTotalTime[i].getStdDev())+ ")\t&\t";
				fmeasureHuman += dfhuman.format(ec.iterationFmeasure[i].getAvg()) +" ("+ dfhuman.format(ec.iterationFmeasure[i].getStdDev())+ ")\t&\t";
			}
			Files.appendFile(new File(timeLatex), learningTimeHuman + "\n");
			Files.appendFile(new File(timeLatex), totalTimeHuman + "\n");
			Files.appendFile(new File(totalLatex), fmeasureHuman + "\n");
		}

	}

}
