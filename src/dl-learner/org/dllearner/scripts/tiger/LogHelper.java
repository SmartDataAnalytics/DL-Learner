package org.dllearner.scripts.tiger;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

public class LogHelper {

	public static String log4jConfigFile = "log4j.properties";
	private static Logger rootLogger = Logger.getRootLogger();

	public static Logger initLoggers() {
		initHere();
		return Logger.getRootLogger();
		// initFile(log4jConfigFile);
	}
	
	private static void initHere() {
		Layout layout = new PatternLayout();
		layout = new PatternLayout("%-5p [%C{1}]: %m%n");
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		consoleAppender.setThreshold(Level.DEBUG);

		Layout layout2 = null;
		FileAppender fileAppenderNormal = null;
		String fileName;
		layout2 = new PatternLayout("%-5p [%C{1}]: %m%n");
		fileName = "log/log.txt";
		try {
			fileAppenderNormal = new FileAppender(layout2, fileName, false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// add both loggers
		rootLogger.removeAllAppenders();
		rootLogger.addAppender(consoleAppender);
		rootLogger.addAppender(fileAppenderNormal);
		rootLogger.setLevel(Level.DEBUG);
	}

	@SuppressWarnings("unused")
	private static void initFile(String log4jConfigFile) {

		System.out.println("Loading log config from file: '" + log4jConfigFile + "'");
		PropertyConfigurator.configure(log4jConfigFile);

	}
}
