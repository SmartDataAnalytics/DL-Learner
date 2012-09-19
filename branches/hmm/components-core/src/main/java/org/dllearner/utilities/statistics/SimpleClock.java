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

package org.dllearner.utilities.statistics;

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
import org.apache.log4j.Logger;

/**
 * Class for counting time and outputting it.
 * 
 * @author Unknown
 */
public class SimpleClock {

	private static Logger logger = Logger.getLogger(SimpleClock.class);

	private long time;

	public SimpleClock() {
		time = System.currentTimeMillis();
	}

	/**
	 * prints time needed and resets the clock
	 */
	public void printAndSet() {
		logger.info("needed " + getTime() + " ms");
		setTime();
	}

	/**
	 * prints time needed and resets the clock
	 * 
	 * @param s
	 *            String for printing
	 */
	public void printAndSet(String s) {
		logger.info(s + " needed " + getTime() + " ms");
		setTime();
	}

	public String getAndSet(String s) {
		String ret = s + " needed " + getTime() + " ms";
		setTime();
		return ret;
	}

	/**
	 * prints time needed
	 * 
	 * @param s
	 *            String for printing
	 */
	public void print(String s) {
		logger.info(s + " needed " + getTime() + " ms");
	}

	/**
	 * resets the clock
	 */
	public void setTime() {
		time = System.currentTimeMillis();
	}

	/**
	 * resets the clock
	 */
	public void reset() {
		setTime();
	}

	/**
	 * returns the needed time up to now in ms
	 * @return
	 */
	public long getTime() {
		long now = System.currentTimeMillis();
		return now - time;
	}

}
