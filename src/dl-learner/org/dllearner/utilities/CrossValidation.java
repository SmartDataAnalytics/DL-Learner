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
package org.dllearner.utilities;

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.Start;
import org.dllearner.core.LearningAlgorithm;

/**
 * Performs cross validation for the given problem.
 * 
 * @author Jens Lehmann
 *
 */
public class CrossValidation {

	private static Logger logger = Logger.getRootLogger();	
	
	public static void main(String[] args) {
		File file = new File(args[0]);
		
		int folds = 10;
		if(args.length > 1)
			folds = Integer.parseInt(args[1]);
		
		// create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.INFO);		
		
		new CrossValidation(file, folds);
		
	}
	
	public CrossValidation(File file, int folds) {
		Start start = new Start(file);
		LearningAlgorithm la = start.getLearningAlgorithm();
		
		for(int currFold=0; currFold<folds; currFold++) {
			la.start();
		}		
	}
	
}
