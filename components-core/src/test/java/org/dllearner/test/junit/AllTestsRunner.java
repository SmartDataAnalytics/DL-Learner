package org.dllearner.test.junit;

import junit.framework.TestSuite;

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
				ClassExpressionTests.class,
				ComponentTests.class,
				ELDescriptionTreeTests.class,
				ELDownTests.class,
				HeuristicTests.class,
				ParserTests.class,
				RefinementOperatorTests.class,
				SimulationTests.class,
				UtilitiesTests.class);

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
