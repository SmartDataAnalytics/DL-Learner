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
package org.dllearner.test.junit;

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

	public static void main(String[] args) {
		JUnitCore.main("org.dllearner.test.ComponentTests");
	}

}
