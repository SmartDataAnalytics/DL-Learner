/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.parser.ParseException;

/**
 * @author Jens Lehmann
 *
 */
public class TestValidation {
 
	private static Logger logger = Logger.getRootLogger();
	
	public static void main(String args[]) throws ComponentInitException, FileNotFoundException, ParseException, org.dllearner.confparser.ParseException {
		
		// create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.DEBUG);		
		
		String filenameTrain = args[0];
		String filenameTest = args[1];
		
		Start start = new Start(new File(filenameTrain));
		start.start(false);
		Description solution = start.getLearningAlgorithm().getCurrentlyBestDescription();
		
		logger.setLevel(Level.WARN);
		
		Start startTest = new Start(new File(filenameTest));
		ReasonerComponent rs = startTest.getReasonerComponent();
		LearningProblem lp = startTest.getLearningProblem();
		
		Set<Individual> result = rs.getIndividuals(solution);
		System.out.println("retrieval result: " + result);

		ScorePosNeg score = (ScorePosNeg) lp.computeScore(solution);
		System.out.println(score);
	}
	
}
