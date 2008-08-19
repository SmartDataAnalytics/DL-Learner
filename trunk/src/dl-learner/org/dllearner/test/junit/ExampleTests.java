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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.dllearner.cli.QuickStart;
import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.junit.Test;

/**
 * Tests related to learning problems in the examples directory.
 * 
 * @author Jens Lehmann
 *
 */
public class ExampleTests {

	/**
	 * This test runs all conf files in the examples directory. Each conf file corresponds to one
	 * unit test, which is succesful if a concept was learned.
	 * @throws ComponentInitException If any component initialisation exception occurs in the process.
	 */
	@Test
	public void testAllConfFiles() throws ComponentInitException {
		// map containing a list of conf files for each path
		HashMap<String, ArrayList<String>> confFiles = new HashMap<String, ArrayList<String>>();
		String exampleDir = "." + File.separator + "examples";
		File f = new File(exampleDir);
		QuickStart.getAllConfs(f, exampleDir, confFiles);
		
		for(String path : confFiles.keySet()) {
			for(String file : confFiles.get(path)) {
				String conf = path + file + ".conf";
				// start example
				Start start = new Start(new File(conf));
				start.start(false);
				// test is successful if a concept was learned
				assert(start.getLearningAlgorithm().getCurrentlyBestDescription() != null);
			}
		}
		
		
	}
	
}
