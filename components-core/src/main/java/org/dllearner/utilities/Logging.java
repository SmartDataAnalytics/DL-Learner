package org.dllearner.utilities;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Logging centered utility class.
 * 
 * @author Jens Lehmann
 *
 */
public class Logging {

	/**
	 * Prints the currently available log4j loggers to system out.
	 */
	@SuppressWarnings({"unchecked"})
	public static void printCurrentLoggers() {
		LoggerRepository rep = LogManager.getLoggerRepository();
		Enumeration<Logger> e = rep.getCurrentLoggers();		
		while(e.hasMoreElements()) {
			Logger l = e.nextElement();
			String name = l.getName();
			Level level = l.getLevel();
			Enumeration<Appender> appenders = l.getAllAppenders();
			
			if(appenders.hasMoreElements())
				System.out.println("APPENDER: " + appenders.nextElement());
			
			System.out.println("name : " + name);
			System.out.println("level: " + level);
			System.out.println("appenders: " + appenders);
			System.out.println();
		}
	}
	
}
