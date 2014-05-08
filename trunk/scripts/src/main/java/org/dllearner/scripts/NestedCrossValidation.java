/**
 * Copyright (C) 2007-2009, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.scripts;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.CLI;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.TrainTestList;
import org.dllearner.utilities.statistics.Stat;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Performs nested cross validation for the given problem. A k fold outer and l
 * fold inner cross validation is used. Parameters:
 * <ul>
 * <li>The conf file to use.</li>
 * <li>k (number of outer folds)</li>
 * <li>l (number of inner folds)</li>
 * <li>parameter name to vary</li>
 * <li>a set of parameter values to test</li>
 * </ul>
 * 
 * Example arguments: bla.conf 10 5 noise 25-40
 * 
 * Currently, only the optimisation of a single parameter is supported.
 * 
 * Later versions may include support for testing a variety of parameters, e.g.
 * --conf bla.conf --outerfolds 10 --innerfolds 5 --complexparameters=
 * "para1=val1;...paran=valn#...#para1=val1;...paran=valn" This tests all
 * parameter combinations separated by #.
 * 
 * Alternatively: --conf bla.conf --outerfolds 10 --innerfolds 5 --parameters=
 * "para1#para2#para3" --values="25-40#(high,medium,low)#boolean" This tests all
 * combinations of parameters and given values, where the script recognises
 * special patterns, e.g. integer ranges or the keyword boolean for
 * "true/false".
 * 
 * Currently, only learning from positive and negative examples is supported.
 * 
 * Currently, the script can only optimise towards classification accuracy.
 * (Can be extended to handle optimising F measure or other combinations of
 * precision, recall, accuracy.)
 * 
 * @author Jens Lehmann
 * 
 */
public class NestedCrossValidation {
	
	private File outputFile = new File("log/nested-cv.log");
	DecimalFormat df = new DecimalFormat();	
	
	// overall statistics
	Stat globalAcc = new Stat();
	Stat globalF = new Stat();
	Stat globalRecall = new Stat();
	Stat globalPrecision = new Stat();
	
	Map<Double,Stat> globalParaStats = new HashMap<Double,Stat>();

	/**
	 * Entry method, which uses JOptSimple to parse parameters.
	 * 
	 * @param args
	 *            Command line arguments (see class documentation).
	 * @throws IOException
	 * @throws ParseException 
	 * @throws ComponentInitException 
	 * @throws org.dllearner.confparser.ParseException 
	 */
	public static void main(String[] args) throws IOException, ComponentInitException, ParseException, org.dllearner.confparser.ParseException {

		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("h", "?", "help"), "Show help.");
		parser.acceptsAll(asList("c", "conf"), "The comma separated list of conffiles to be used.").withRequiredArg().describedAs("file1, file2, ...");
		parser.acceptsAll(asList( "v", "verbose"), "Be more verbose.");
		parser.acceptsAll(asList( "o", "outerfolds"), "Number of outer folds.").withRequiredArg().ofType(Integer.class).describedAs("#folds");
		parser.acceptsAll(asList( "i", "innerfolds"), "Number of inner folds.").withRequiredArg().ofType(Integer.class).describedAs("#folds");
		parser.acceptsAll(asList( "p", "parameter"), "Parameter to vary.").withRequiredArg();
		parser.acceptsAll(asList( "r", "pvalues", "range"), "Values of parameter. $x-$y can be used for integer ranges.").withRequiredArg();
		parser.acceptsAll(asList( "s", "stepsize", "steps"), "Step size of range.").withOptionalArg().ofType(Double.class).defaultsTo(1d);

		// parse options and display a message for the user in case of problems
		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch(Exception e) {
			System.out.println("Error: " + e.getMessage() + ". Use -? to get help.");
			System.exit(0);
		}
		
		// print help screen
		if (options.has("?")) {
			parser.printHelpOn(System.out);
		// all options present => start nested cross validation
		} else if(options.has("c") && options.has("o") && options.has("i") && options.has("p") && options.has("r")) {
			// read all options in variables and parse option values
			String confFilesString = (String) options.valueOf("c");
			List<File> confFiles = new ArrayList<File>();
			for (String fileString : confFilesString.split(",")) {
				confFiles.add(new File(fileString.trim()));
			}
			
			int outerFolds = (Integer) options.valueOf("o");
			int innerFolds = (Integer) options.valueOf("i");
			String parameter = (String) options.valueOf("p");
			String range = (String) options.valueOf("r");
			String[] rangeSplit = range.split("-");
			double rangeStart = Double.valueOf(rangeSplit[0]);
			double rangeEnd = Double.valueOf(rangeSplit[1]);
			double stepsize = (Double) options.valueOf("s");
			boolean verbose = options.has("v");
			
			// create logger (a simple logger which outputs
			// its messages to the console)
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.setLevel(Level.WARN);
			Logger.getLogger("org.dllearner.algorithms").setLevel(Level.INFO);
//			logger.addAppender(new FileAppender(layout, "nested-cv.log", false));
			// disable OWL API info output
			java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.WARNING);
			
			System.out.println("Warning: The script is not well tested yet. (No known bugs, but needs more testing.)");
			new NestedCrossValidation(confFiles, outerFolds, innerFolds, parameter, rangeStart, rangeEnd, stepsize, verbose);
			
		// an option is missing => print help screen and message
		} else {
			parser.printHelpOn(System.out);
			System.out.println("\nYou need to specify the options c, i, o, p, r. Please consult the help table above.");
		}

	}

	private void print(String s){
		try {
			Files.append(s + "\n", outputFile , Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(s);
	}
	
	public NestedCrossValidation(File confFile, int outerFolds, int innerFolds, String parameter, double startValue, double endValue, double stepsize, boolean verbose) throws ComponentInitException, ParseException, org.dllearner.confparser.ParseException, IOException {
		this(Lists.newArrayList(confFile), outerFolds, innerFolds, parameter, startValue, endValue, stepsize, verbose);
	}

	public NestedCrossValidation(List<File> confFiles, int outerFolds, int innerFolds, String parameter, double startValue, double endValue, double stepsize, boolean verbose) throws ComponentInitException, ParseException, org.dllearner.confparser.ParseException, IOException {
		
		for (File confFile : confFiles) {
			print(confFile.getPath());
			validate(confFile, outerFolds, innerFolds, parameter, startValue, endValue, stepsize, verbose);		
		}
		
		print("********************************************");
		print("********************************************");
		print("********************************************");
		
		// decide for the best parameter
		print("    Summary over parameter values:");
		double bestPara = startValue;
		double bestValue = Double.NEGATIVE_INFINITY;
		for (Entry<Double, Stat> entry : globalParaStats.entrySet()) {
			double para = entry.getKey();
			Stat stat = entry.getValue();
			print("      value " + para + ": " + stat.prettyPrint("%"));
			if (stat.getMean() > bestValue) {
				bestPara = para;
				bestValue = stat.getMean();
			}
		}
		print("      selected " + bestPara + " as best parameter value (criterion value " + df.format(bestValue) + "%)");
		
		// overall statistics
		print("*******************");
		print("* Overall Results *");
		print("*******************");
		print("accuracy: " + globalAcc.prettyPrint("%"));
		print("F measure: " + globalF.prettyPrint("%"));
		print("precision: " + globalPrecision.prettyPrint("%"));
		print("recall: " + globalRecall.prettyPrint("%"));
		
	}
	
	private void validate(File confFile, int outerFolds, int innerFolds, String parameter, double startValue, double endValue, double stepsize, boolean verbose) throws IOException, ComponentInitException{
		CLI start = new CLI(confFile);
		start.init();
		AbstractLearningProblem lp = start.getLearningProblem();
		if(!(lp instanceof PosNegLP)) {
			System.out.println("Positive only learning not supported yet.");
			System.exit(0);
		}
		
		// get examples and shuffle them
		LinkedList<Individual> posExamples = new LinkedList<Individual>(((PosNegLP)lp).getPositiveExamples());
		Collections.shuffle(posExamples, new Random(1));			
		LinkedList<Individual> negExamples = new LinkedList<Individual>(((PosNegLP)lp).getNegativeExamples());
		Collections.shuffle(negExamples, new Random(2));	
		
		AbstractReasonerComponent rc = start.getReasonerComponent();
		String baseURI = rc.getBaseURI();
		
		List<TrainTestList> posLists = getFolds(posExamples, outerFolds);
		List<TrainTestList> negLists = getFolds(negExamples, outerFolds);
		
		// overall statistics
		Stat accOverall = new Stat();
		Stat fOverall = new Stat();
		Stat recallOverall = new Stat();
		Stat precisionOverall = new Stat();
		
		for(int currOuterFold=0; currOuterFold<outerFolds; currOuterFold++) {
			
			print("Outer fold " + currOuterFold);
			TrainTestList posList = posLists.get(currOuterFold);
			TrainTestList negList = negLists.get(currOuterFold);
			
			// measure relevant criterion (accuracy, F-measure) over different parameter values
			Map<Double,Stat> paraStats = new HashMap<Double,Stat>();
			
			for(double currParaValue=startValue; currParaValue<=endValue; currParaValue+=stepsize) {
				
				print("  Parameter value " + currParaValue + ":");
				// split train folds again (computation of inner folds for each parameter
				// value is redundant, but not a big problem)
				List<Individual> trainPosList = posList.getTrainList();
				List<TrainTestList> innerPosLists = getFolds(trainPosList, innerFolds);
				List<Individual> trainNegList = negList.getTrainList();
				List<TrainTestList> innerNegLists = getFolds(trainNegList, innerFolds);
				
				// measure relevant criterion for parameter (by default accuracy,
				// can also be F measure)
				Stat paraCriterionStat = new Stat();
				
				for(int currInnerFold=0; currInnerFold<innerFolds; currInnerFold++) {
					
					print("    Inner fold " + currInnerFold + ":");
					// get positive & negative examples for training run
					Set<Individual> posEx = new TreeSet<Individual>(innerPosLists.get(currInnerFold).getTrainList());
					Set<Individual> negEx = new TreeSet<Individual>(innerNegLists.get(currInnerFold).getTrainList());
					
					// read conf file and exchange options for pos/neg examples 
					// and parameter to optimise
					start = new CLI(confFile);
					start.init();
					AbstractLearningProblem lpIn = start.getLearningProblem();
					((PosNegLP)lpIn).setPositiveExamples(posEx);
					((PosNegLP)lpIn).setNegativeExamples(negEx);
					AbstractCELA laIn = start.getLearningAlgorithm();
					try {
						PropertyUtils.setSimpleProperty(laIn, parameter, currParaValue);
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						e.printStackTrace();
					}
					
					lpIn.init();
					laIn.init();
					laIn.start();
					
					// evaluate learned expression
					Description concept = laIn.getCurrentlyBestDescription();
					
					TreeSet<Individual> posTest = new TreeSet<Individual>(innerPosLists.get(currInnerFold).getTestList());
					TreeSet<Individual> negTest = new TreeSet<Individual>(innerNegLists.get(currInnerFold).getTestList());
					
					AbstractReasonerComponent rs = start.getReasonerComponent();
					// true positive
					Set<Individual> posCorrect = rs.hasType(concept, posTest);
					// false negative
					Set<Individual> posError = Helper.difference(posTest, posCorrect);
					// false positive
					Set<Individual> negError = rs.hasType(concept, negTest);
					// true negative
					Set<Individual> negCorrect = Helper.difference(negTest, negError);
	
//					double posErrorRate = 100*(posError.size()/posTest.size());
//					double negErrorRate = 100*(negError.size()/posTest.size());
					
					double accuracy = 100*((double)(posCorrect.size()+negCorrect.size())/(posTest.size()+negTest.size()));
					double precision = 100 * (double) posCorrect.size() / (posCorrect.size() + negError.size());
					double recall = 100 * (double) posCorrect.size() / (posCorrect.size() + posError.size());
					double fmeasure = 2 * (precision * recall) / (precision + recall);
					
					paraCriterionStat.addNumber(accuracy);
					
					print("      hypothesis: " + concept.toManchesterSyntaxString(baseURI, null));
					print("      accuracy: " + df.format(accuracy) + "%");
					print("      precision: " + df.format(precision) + "%");
					print("      recall: " + df.format(recall) + "%");
					print("      F measure: " + df.format(fmeasure) + "%");
					
					if(verbose) {
						print("      false positives (neg. examples classified as pos.): " + formatIndividualSet(posError, baseURI));
						print("      false negatives (pos. examples classified as neg.): " + formatIndividualSet(negError, baseURI));
					}
					
					// free memory
					rs.releaseKB();
				}
				
				paraStats.put(currParaValue, paraCriterionStat);
				Stat globalParaStat = globalParaStats.get(currParaValue);
				if(globalParaStat == null){
					globalParaStat = new Stat();
					globalParaStats.put(currParaValue, globalParaStat);
				}
				globalParaStat.add(paraCriterionStat);
			}
			
			// decide for the best parameter
			print("    Summary over parameter values:");
			double bestPara = startValue;
			double bestValue = Double.NEGATIVE_INFINITY;
			for(Entry<Double,Stat> entry : paraStats.entrySet()) {
				double para = entry.getKey();
				Stat stat = entry.getValue();
				print("      value " + para + ": " + stat.prettyPrint("%"));
				if(stat.getMean() > bestValue) {
					bestPara = para;
					bestValue = stat.getMean();
				}
			}
			print("      selected " + bestPara + " as best parameter value (criterion value " + df.format(bestValue) + "%)");
			print("    Learn on Outer fold:");
			
			// start a learning process with this parameter and evaluate it on the outer fold
			start = new CLI(confFile);
			start.init();
			AbstractLearningProblem lpOut = start.getLearningProblem();
			((PosNegLP)lpOut).setPositiveExamples(new TreeSet<Individual>(posLists.get(currOuterFold).getTrainList()));
			((PosNegLP)lpOut).setNegativeExamples(new TreeSet<Individual>(negLists.get(currOuterFold).getTrainList()));
			AbstractCELA laOut = start.getLearningAlgorithm();
			try {
				PropertyUtils.setSimpleProperty(laOut, parameter, bestPara);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
			
			lpOut.init();
			laOut.init();
			laOut.start();			
			
			// evaluate learned expression
			Description concept = laOut.getCurrentlyBestDescription();
			
			TreeSet<Individual> posTest = new TreeSet<Individual>(posLists.get(currOuterFold).getTestList());
			TreeSet<Individual> negTest = new TreeSet<Individual>(negLists.get(currOuterFold).getTestList());
			
			AbstractReasonerComponent rs = start.getReasonerComponent();
			// true positive
			Set<Individual> posCorrect = rs.hasType(concept, posTest);
			// false negative
			Set<Individual> posError = Helper.difference(posTest, posCorrect);
			// false positive
			Set<Individual> negError = rs.hasType(concept, negTest);
			// true negative
			Set<Individual> negCorrect = Helper.difference(negTest, negError);

			double accuracy = 100*((double)(posCorrect.size()+negCorrect.size())/(posTest.size()+negTest.size()));
			double precision = 100 * (double) posCorrect.size() / (posCorrect.size() + negError.size());
			double recall = 100 * (double) posCorrect.size() / (posCorrect.size() + posError.size());
			double fmeasure = 2 * (precision * recall) / (precision + recall);
	
			print("      hypothesis: " + concept.toManchesterSyntaxString(baseURI, null));
			print("      accuracy: " + df.format(accuracy) + "%");
			print("      precision: " + df.format(precision) + "%");
			print("      recall: " + df.format(recall) + "%");
			print("      F measure: " + df.format(fmeasure) + "%");
			
			if(verbose) {
				print("      false positives (neg. examples classified as pos.): " + formatIndividualSet(posError, baseURI));
				print("      false negatives (pos. examples classified as neg.): " + formatIndividualSet(negError, baseURI));
			}			
			
			// update overall statistics
			accOverall.addNumber(accuracy);
			fOverall.addNumber(fmeasure);
			recallOverall.addNumber(recall);
			precisionOverall.addNumber(precision);
			
			// free memory
			rs.releaseKB();
		}
		
		globalAcc.add(accOverall);
		globalF.add(fOverall);
		globalPrecision.add(precisionOverall);
		globalRecall.add(recallOverall);
		
		// overall statistics
		print("*******************");
		print("* Overall Results *");
		print("*******************");
		print("accuracy: " + accOverall.prettyPrint("%"));
		print("F measure: " + fOverall.prettyPrint("%"));
		print("precision: " + precisionOverall.prettyPrint("%"));
		print("recall: " + recallOverall.prettyPrint("%"));
	}
	
	// convenience methods, which takes a list of examples and divides them in
	// train-test-lists according to the number of folds specified
	public static List<TrainTestList> getFolds(List<Individual> list, int folds) {
		List<TrainTestList> ret = new LinkedList<TrainTestList>();
		int[] splits = CrossValidation.calculateSplits(list.size(), folds);
		for(int i=0; i<folds; i++) {
			int fromIndex = (i==0) ? 0 : splits[i-1];
			int toIndex = splits[i];
			List<Individual> test = list.subList(fromIndex, toIndex);
			List<Individual> train = new LinkedList<Individual>(list);
			train.removeAll(test);
			ret.add(new TrainTestList(train, test));
		}
		return ret;
	}
	
	private static String formatIndividualSet(Set<Individual> inds, String baseURI) {
		String ret = "";
		int i=0;
		for(Individual ind : inds) {
			ret += ind.toManchesterSyntaxString(baseURI, null) + " ";
			i++;
			if(i==20) {
				break;
			}
		}
		return ret;
	}
}
