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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.TrainTestList;
import org.dllearner.utilities.statistics.Stat;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import static java.util.Arrays.*;

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
		parser.acceptsAll(asList("c", "conf"), "Conf file to use.").withRequiredArg().ofType(
				File.class);
		parser.acceptsAll(asList( "v", "verbose"), "Be more verbose.");
		parser.acceptsAll(asList( "o", "outerfolds"), "Number of outer folds.").withRequiredArg().ofType(Integer.class).describedAs("#folds");
		parser.acceptsAll(asList( "i", "innerfolds"), "Number of inner folds.").withRequiredArg().ofType(Integer.class).describedAs("#folds");
		parser.acceptsAll(asList( "p", "parameter"), "Parameter to vary.").withRequiredArg();
		parser.acceptsAll(asList( "r", "pvalues", "range"), "Values of parameter. $x-$y can be used for integer ranges.").withRequiredArg();

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
			File confFile = (File) options.valueOf("c");
			int outerFolds = (Integer) options.valueOf("o");
			int innerFolds = (Integer) options.valueOf("i");
			String parameter = (String) options.valueOf("p");
			String range = (String) options.valueOf("r");
			String[] rangeSplit = range.split("-");
			int rangeStart = new Integer(rangeSplit[0]);
			int rangeEnd = new Integer(rangeSplit[1]);
			boolean verbose = options.has("v");
			
			// create logger (a simple logger which outputs
			// its messages to the console)
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			Logger logger = Logger.getRootLogger();
			logger.removeAllAppenders();
			logger.addAppender(consoleAppender);
			logger.setLevel(Level.WARN);
			// disable OWL API info output
			java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.WARNING);
			
			System.out.println("Warning: The script is not well tested yet. (No known bugs, but needs more testing.)");
			new NestedCrossValidation(confFile, outerFolds, innerFolds, parameter, rangeStart, rangeEnd, verbose);
			
		// an option is missing => print help screen and message
		} else {
			parser.printHelpOn(System.out);
			System.out.println("\nYou need to specify the options c, i, o, p, r. Please consult the help table above.");
		}

	}

	public NestedCrossValidation(File confFile, int outerFolds, int innerFolds, String parameter, int startValue, int endValue, boolean verbose) throws FileNotFoundException, ComponentInitException, ParseException, org.dllearner.confparser.ParseException {
		
		DecimalFormat df = new DecimalFormat();	
		ComponentManager cm = ComponentManager.getInstance();
		
		Start start = new Start(confFile);
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
			
			System.out.println("Outer fold " + currOuterFold);
			TrainTestList posList = posLists.get(currOuterFold);
			TrainTestList negList = negLists.get(currOuterFold);
			
			// measure relevant criterion (accuracy, F-measure) over different parameter values
			Map<Integer,Stat> paraStats = new HashMap<Integer,Stat>();
			
			for(int currParaValue=startValue; currParaValue<=endValue; currParaValue++) {
				
				System.out.println("  Parameter value " + currParaValue + ":");
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
					
					System.out.println("    Inner fold " + currInnerFold + ":");
					// get positive & negative examples for training run
					Set<Individual> posEx = new TreeSet<Individual>(innerPosLists.get(currInnerFold).getTrainList());
					Set<Individual> negEx = new TreeSet<Individual>(innerNegLists.get(currInnerFold).getTrainList());
					
					// read conf file and exchange options for pos/neg examples 
					// and parameter to optimise
					start = new Start(confFile);
					AbstractLearningProblem lpIn = start.getLearningProblem();
					cm.applyConfigEntry(lpIn, "positiveExamples", Datastructures.individualSetToStringSet(posEx));
					cm.applyConfigEntry(lpIn, "negativeExamples", Datastructures.individualSetToStringSet(negEx));
					AbstractCELA laIn = start.getLearningAlgorithm();
					cm.applyConfigEntry(laIn, parameter, (double)currParaValue);
					
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
					
					System.out.println("      hypothesis: " + concept.toManchesterSyntaxString(baseURI, null));
					System.out.println("      accuracy: " + df.format(accuracy) + "%");
					System.out.println("      precision: " + df.format(precision) + "%");
					System.out.println("      recall: " + df.format(recall) + "%");
					System.out.println("      F measure: " + df.format(fmeasure) + "%");
					
					if(verbose) {
						System.out.println("      false positives (neg. examples classified as pos.): " + formatIndividualSet(posError, baseURI));
						System.out.println("      false negatives (pos. examples classified as neg.): " + formatIndividualSet(negError, baseURI));
					}
					
					// free memory
					rs.releaseKB();
					cm.freeAllComponents();
				}
				
				paraStats.put(currParaValue, paraCriterionStat);
				
			}
			
			// decide for the best parameter
			System.out.println("    Summary over parameter values:");
			int bestPara = startValue;
			double bestValue = Double.NEGATIVE_INFINITY;
			for(Entry<Integer,Stat> entry : paraStats.entrySet()) {
				int para = entry.getKey();
				Stat stat = entry.getValue();
				System.out.println("      value " + para + ": " + stat.prettyPrint("%"));
				if(stat.getMean() > bestValue) {
					bestPara = para;
					bestValue = stat.getMean();
				}
			}
			System.out.println("      selected " + bestPara + " as best parameter value (criterion value " + df.format(bestValue) + "%)");
			System.out.println("    Learn on Outer fold:");
			
			// start a learning process with this parameter and evaluate it on the outer fold
			start = new Start(confFile);
			AbstractLearningProblem lpOut = start.getLearningProblem();
			cm.applyConfigEntry(lpOut, "positiveExamples", Datastructures.individualListToStringSet(posLists.get(currOuterFold).getTrainList()));
			cm.applyConfigEntry(lpOut, "negativeExamples", Datastructures.individualListToStringSet(negLists.get(currOuterFold).getTrainList()));
			AbstractCELA laOut = start.getLearningAlgorithm();
			cm.applyConfigEntry(laOut, parameter, (double)bestPara);
			
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
	
			System.out.println("      hypothesis: " + concept.toManchesterSyntaxString(baseURI, null));
			System.out.println("      accuracy: " + df.format(accuracy) + "%");
			System.out.println("      precision: " + df.format(precision) + "%");
			System.out.println("      recall: " + df.format(recall) + "%");
			System.out.println("      F measure: " + df.format(fmeasure) + "%");
			
			if(verbose) {
				System.out.println("      false positives (neg. examples classified as pos.): " + formatIndividualSet(posError, baseURI));
				System.out.println("      false negatives (pos. examples classified as neg.): " + formatIndividualSet(negError, baseURI));
			}			
			
			// update overall statistics
			accOverall.addNumber(accuracy);
			fOverall.addNumber(fmeasure);
			recallOverall.addNumber(recall);
			precisionOverall.addNumber(precision);
			
			// free memory
			rs.releaseKB();
			cm.freeAllComponents();			
		}
		
		// overall statistics
		System.out.println();
		System.out.println("*******************");
		System.out.println("* Overall Results *");
		System.out.println("*******************");
		System.out.println("accuracy: " + accOverall.prettyPrint("%"));
		System.out.println("F measure: " + fOverall.prettyPrint("%"));
		System.out.println("precision: " + precisionOverall.prettyPrint("%"));
		System.out.println("recall: " + recallOverall.prettyPrint("%"));
		
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
