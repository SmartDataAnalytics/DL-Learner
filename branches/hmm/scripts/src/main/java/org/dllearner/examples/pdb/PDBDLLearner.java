/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.**/

package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;

import com.jamonapi.MonitorFactory;

/**
 * Sample script showing how to use DL-Learner. Provides an entry point for tool
 * developers.
 * 
 * @author Sebastian Hellmann
 * @author Jens Lehmann
 * 
 */

public class PDBDLLearner {

	private static Logger logger = Logger.getRootLogger();
	private static DecimalFormat df = new DecimalFormat();

	private File _confFile = null;
	private ArrayList<OWLFile> _ksFiles = new ArrayList<OWLFile>();
	private SortedSet<Individual> _posExamples = new TreeSet<Individual>();
	private SortedSet<Individual> _negExamples = new TreeSet<Individual>();
	
	
	public PDBDLLearner (File confFile) throws IOException, ComponentInitException,
	LearningProblemUnsupportedException {

		// create logger (configure this to your needs)
		SimpleLayout layout = new SimpleLayout();
		FileAppender fileAppender = new FileAppender(layout, "log/sample_log.txt", false);

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);
		
		_confFile = confFile;
		try {
			LineNumberReader confReader = new LineNumberReader(new FileReader(_confFile));
			String line;
			while ((line = confReader.readLine()) != null){
				String parameter = "";
				if (line.contains("\"")){
					parameter = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
				} 
				logger.debug("Line: " + line + "=> Parameter: " + parameter);
				if (line.startsWith("import") && !(line.isEmpty()) ){
					logger.debug(_confFile.getParent() + File.separator + parameter);
					OWLFile ks = new OWLFile(_confFile.getParent() + File.separator + parameter);
					logger.debug("ks file name: " + ks.getFileName());
					_ksFiles.add(ks);
				} else if (line.startsWith("+") && !(line.isEmpty()) ){
					_posExamples.add(new Individual(parameter));
				} else if (line.startsWith("-") && !(line.isEmpty()) ){
					_negExamples.add(new Individual(parameter));
				}
			}
			
		} catch (FileNotFoundException e) {
			logger.error("File " + _confFile.getPath() +  " not found.");
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Something went wrong while reading " + _confFile.getPath() + "." );
		}
	
		String resultsFileName = _confFile.getPath().replace(".conf", ".dll.sol");
		logger.debug(resultsFileName);
		FileWriter resultsFile = new FileWriter(new File(resultsFileName));
		List<? extends EvaluatedDescription> results = learn(_ksFiles, _posExamples, _negExamples, 5);
		int x = 0;
		for (EvaluatedDescription ed : results) {
			String solution = new String("solution: " + x + "\n"
					+ "  description: \t" + ed.getDescription().toManchesterSyntaxString(null, null) + "\n"
					+ "  accuracy: \t" + df.format(((EvaluatedDescriptionPosNeg)ed).getAccuracy() * 100) + "%\n\n");
			System.out.println(solution);
			resultsFile.write(solution);
			x++;
		}
		resultsFile.close();
	
		Files.createFile(new File( _confFile.getPath() + ".jamon.log.html"), MonitorFactory.getReport());

	}

	public static List<? extends EvaluatedDescription> learn(List<OWLFile> ksFiles, SortedSet<Individual> posExamples,
			SortedSet<Individual> negExamples, int maxNrOfResults) throws ComponentInitException,
			LearningProblemUnsupportedException {

		logger.info("Start Learning with");
		logger.info("positive examples: \t" + posExamples.size());
		logger.info("negative examples: \t" + negExamples.size());

		// use the specified OWL files
		// and load them into the reasoner
		FastInstanceChecker r = new FastInstanceChecker();
		
		for (int i = 0; i < ksFiles.size(); i++){
			OWLFile ks = ksFiles.get(i);
			ks.init();
			r.setSources(ks);
		}	
		r.init();
		
		// configure learning problem
		PosNegLPStandard lp = new PosNegLPStandard();
		lp.setReasoner(r);
		lp.setPositiveExamples(posExamples);
		lp.setNegativeExamples(negExamples);
		lp.init();
		
		// configure target language
		RhoDRDown rho = new RhoDRDown();
		rho.setReasoner(r);
		rho.setUseAllConstructor(true);
		rho.setUseExistsConstructor(true);
		rho.setUseNegation(true);
		rho.setUseCardinalityRestrictions(true);
		rho.init();
		
		// configure learning algorithm
		CELOE la = new CELOE();
		la.setReasoner(r);
		la.setLearningProblem(lp);
		la.setWriteSearchTree(false);
		la.setSearchTreeFile("log/searchTree.txt");
		la.setReplaceSearchTree(true);
		la.setNoisePercentage(20.0);
		la.setMaxExecutionTimeInSeconds(2);
		la.init();
		
		// start learning algorithm
		logger.debug("start learning");
		la.start();
		
		return la.getCurrentlyBestEvaluatedDescriptions(maxNrOfResults);
	}
}