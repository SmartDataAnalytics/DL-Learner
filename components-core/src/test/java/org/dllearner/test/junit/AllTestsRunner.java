/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Class designed to run all DL-Learner component tests. Note,
 * that in Eclipse (and similar in other IDEs) you can run 
 * JUnit tests by clicking on a file containing methods annotated
 * with @Test and "Run As JUnit Test".
 * 
 * @author Jens Lehmann
 * 
 */
public class AllTestsRunner{
	

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
		Result result = JUnitCore.runClasses(
				ClassExpressionTest.class,
				ComponentTest.class,
				ELDescriptionTreeTest.class,
				ELDownTest.class,
				HeuristicTest.class,
				ParserTest.class,
				RefinementOperatorTest.class,
				SimulationTest.class,
				UtilitiesTest.class);

		if(result.wasSuccessful()) {
			System.out.println("All tests succeeded!");
		} else {
			System.err.println("Some tests failed:");

			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
		}
	}

}
