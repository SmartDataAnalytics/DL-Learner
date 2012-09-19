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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.test.junit;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.runner.JUnitCore;

/**
 * Class designed to run all DL-Learner component tests. Note,
 * that in Eclipse (and similar in other IDEs) you can run 
 * JUnit tests by clicking on a file containing methods annotated
 * with @Test and "Run As JUnit Test".
 * 
 * @author Jens Lehmann
 * 
 */
public class AllTestsRunner {

	/**
	 * use the following arguments (or similar): -Djava.library.path=lib/fact/32bit/ -Xmx2000m
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		// create logger
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.DEBUG);
		
		// runs everything except example test
		JUnitCore.main("org.dllearner.test.junit.ClassExpressionTests",
				"org.dllearner.test.junit.ComponentTests",
				"org.dllearner.test.junit.ELDescriptionTreeTests",
				"org.dllearner.test.junit.ELDownTests",
				"org.dllearner.test.junit.HeuristicTests",
//				"org.dllearner.test.junit.OWLAPITests",
				"org.dllearner.test.junit.ParserTests",
				"org.dllearner.test.junit.ReasonerTests",
				"org.dllearner.test.junit.RefinementOperatorTests",
				"org.dllearner.test.junit.SimulationTests",
				"org.dllearner.test.junit.UtilitiesTests");
	}

}
