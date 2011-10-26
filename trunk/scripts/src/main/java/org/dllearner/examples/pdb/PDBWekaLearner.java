package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class PDBWekaLearner {
	
	private static Logger logger = Logger.getRootLogger();
	
	public PDBWekaLearner (File arffFile) throws IOException{
		
		// create logger (configure this to your needs)
		SimpleLayout layout = new SimpleLayout();
		FileAppender fileAppender = new FileAppender(layout, "log/sample_log.txt", false);

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);
				
		
		try{
			 DataSource source = new DataSource(arffFile.getPath());
			 Instances data = source.getDataSet();
			 // setting class attribute if the data format does not provide this information
			 // E.g., the XRFF format saves the class attribute information as well
			 if (data.classIndex() == -1)
			   data.setClassIndex(data.numAttributes() - 1);
			 
			 String[] options = new String[1];
			 options[0] = "-U";            // unpruned tree
			 J48 tree = new J48();         // new instance of tree
			 tree.setOptions(options);     // set the options
			 //tree.buildClassifier(data);   // build classifier
			 Evaluation eval = new Evaluation(data);
			 eval.crossValidateModel(tree, data, 10, new Random(1));
			 
			 // gather the results of the evaluation process
	        String resultsFileName = arffFile.getPath().replace(".arff", ".weka.res");
			logger.debug(resultsFileName);
			FileWriter resultsFile = new FileWriter(new File(resultsFileName));
			String results = new String("Results for " + arffFile.getName() + "\n"
					+ "\t# of correctly classified instances: " + eval.correct() + "\n"
					+ "\t% of correctly classified instances: " + eval.pctCorrect() + "\n"
					+ "\tKappa statistics: " + eval.kappa() + "\n"
					+ "\tCorrelation coefficient: " + eval.correlationCoefficient() + "\n"
					+ "\tMean absolute error: " + eval.meanAbsoluteError() + "\n"
					+ "\tRoot mean squared error: " + eval.rootMeanSquaredError() + "\n"
					+ "\t# of unclassified instances: " + eval.unclassified() + "\n"
					+ "\t% of unclassified instances: " + eval.pctUnclassified() + "\n");
			System.out.println(results);
			resultsFile.write(results);
			resultsFile.close();
			 
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
