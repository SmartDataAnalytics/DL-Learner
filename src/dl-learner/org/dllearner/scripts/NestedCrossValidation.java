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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.TrainTestList;

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
	 */
	public static void main(String[] args) throws IOException, ComponentInitException, ParseException {

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
			
			new NestedCrossValidation(confFile, outerFolds, innerFolds, parameter, rangeStart, rangeEnd);
			
		// an option is missing => print help screen and message
		} else {
			parser.printHelpOn(System.out);
			System.out.println("\nYou need to specify the options c, i, o, p, r. Please consult the help table above.");
		}

	}

	public NestedCrossValidation(File confFile, int outerFolds, int innerFolds, String parameter, int startValue, int endValue) throws FileNotFoundException, ComponentInitException, ParseException {
		
		DecimalFormat df = new DecimalFormat();	
		ComponentManager cm = ComponentManager.getInstance();
		
		Start start = new Start(confFile);
		LearningProblem lp = start.getLearningProblem();
		
		if(!(lp instanceof PosNegLP)) {
			System.out.println("Positive only learning not supported yet.");
			System.exit(0);
		}
		
		// get examples and shuffle them
		LinkedList<Individual> posExamples = new LinkedList<Individual>(((PosNegLP)lp).getPositiveExamples());
		Collections.shuffle(posExamples, new Random(1));			
		LinkedList<Individual> negExamples = new LinkedList<Individual>(((PosNegLP)lp).getNegativeExamples());
		Collections.shuffle(negExamples, new Random(2));	
		
		List<TrainTestList> posLists = getFolds(posExamples, outerFolds);
		List<TrainTestList> negLists = getFolds(negExamples, outerFolds);
		
		for(int currOuterFold=0; currOuterFold<outerFolds; currOuterFold++) {
			
			System.out.println("Start processing outer fold " + currOuterFold);
			TrainTestList posList = posLists.get(currOuterFold);
			TrainTestList negList = negLists.get(currOuterFold);
			
			for(int currParaValue=startValue; currParaValue<=endValue; currParaValue++) {
				
				System.out.println("  Start Processing parameter value " + currParaValue);
				// split train folds again (computation of inner folds for each parameter
				// value is redundant, but not a big problem)
				List<Individual> trainPosList = posList.getTrainList();
				List<TrainTestList> innerPosLists = getFolds(trainPosList, innerFolds);
				List<Individual> trainNegList = negList.getTrainList();
				List<TrainTestList> innerNegLists = getFolds(trainNegList, innerFolds);
				
				for(int currInnerFold=0; currInnerFold<innerFolds; currInnerFold++) {
					
					System.out.println("    Inner fold " + currInnerFold + " ... ");
					// get positive & negative examples for training run
					List<Individual> posEx = innerPosLists.get(currInnerFold).getTrainList();
					List<Individual> negEx = innerNegLists.get(currInnerFold).getTrainList();
					
					// read conf file and exchange options for pos/neg examples 
					// and parameter to optimise
					start = new Start(confFile);
					lp = start.getLearningProblem();
					cm.applyConfigEntry(lp, "positiveExamples", posEx);
					cm.applyConfigEntry(lp, "negativeExamples", negEx);
					LearningAlgorithm la = start.getLearningAlgorithm();
					cm.applyConfigEntry(la, parameter, (double)currParaValue);
					
					lp.init();
					la.init();
					la.start();
					
					// evaluate learned expression
					Description concept = la.getCurrentlyBestDescription();
					
					TreeSet<Individual> posTest = new TreeSet<Individual>(innerPosLists.get(currInnerFold).getTestList());
					TreeSet<Individual> negTest = new TreeSet<Individual>(innerNegLists.get(currInnerFold).getTestList());
					
					ReasonerComponent rs = start.getReasonerComponent();
					Set<Individual> posCorrect = rs.hasType(concept, posTest);
					Set<Individual> posError = Helper.difference(posTest, posCorrect);
					Set<Individual> negError = rs.hasType(concept, negTest);
					Set<Individual> negCorrect = Helper.difference(negTest, negError);
					
//					System.out.println("test set errors pos: " + tmp2);
//					System.out.println("test set errors neg: " + tmp3);
					
					double accuracy = 100*((double)(posCorrect.size()+negCorrect.size())/(posTest.size()+negTest.size()));
					
					System.out.println("    accuracy: " + df.format(accuracy));
					
					// free memory
					rs.releaseKB();
					cm.freeAllComponents();
				}
				
			}
			
		}
		
		/*
		
		// calculate splits using CV class
		int[] splitsPos = CrossValidation.calculateSplits(posExamples.size(), outerFolds);
		int[] splitsNeg = CrossValidation.calculateSplits(negExamples.size(), outerFolds);
		
		// the training and test sets used later on
//		List<List<Individual>> trainingSetsPos = new LinkedList<List<Individual>>();
//		List<List<Individual>> trainingSetsNeg = new LinkedList<List<Individual>>();
//		List<List<Individual>> testSetsPos = new LinkedList<List<Individual>>();
//		List<List<Individual>> testSetsNeg = new LinkedList<List<Individual>>();		
		
		// calculating training and test sets for outer folds
		for(int i=0; i<outerFolds; i++) {
			
			
			
			// sets for positive examples
			int posFromIndex = (i==0) ? 0 : splitsPos[i-1];
			int posToIndex = splitsPos[i];
			List<Individual> testPos = posExamples.subList(posFromIndex, posToIndex);
			List<Individual> trainPos = new LinkedList<Individual>(posExamples);
			trainPos.removeAll(testPos);
 			
			// sets for negative examples
			int negFromIndex = (i==0) ? 0 : splitsNeg[i-1];
			int negToIndex = splitsNeg[i];
			List<Individual> testNeg = posExamples.subList(negFromIndex, negToIndex);
			List<Individual> trainNeg = new LinkedList<Individual>(negExamples);
			trainNeg.removeAll(testNeg);	
						
			// split train folds
			int[] innerSplitPos = CrossValidation.calculateSplits(trainPos.size(), innerFolds);
			int[] innerSplitNeg = CrossValidation.calculateSplits(trainNeg.size(), innerFolds);
			
			for(int j=0; j<innerFolds; j++) {
				
			}
			
			// add to list of folds
//			trainingSetsPos.add(trainPos);
//			trainingSetsNeg.add(trainNeg);
//			testSetsPos.add(testPos);
//			testSetsNeg.add(testNeg);
		}	
		
		*/
		
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
}
