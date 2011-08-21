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
