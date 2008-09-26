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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.QuickStart;
import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.utilities.Helper;
import org.junit.Test;

/**
 * Tests related to learning problems in the examples directory.
 * 
 * @author Jens Lehmann
 * 
 */
public class ExampleTests {

	/**
	 * This test runs all conf files in the examples directory. Each conf file
	 * corresponds to one unit test, which is succesful if a concept was
	 * learned. This unit test takes several hours.
	 * 
	 * @throws ComponentInitException
	 *             If any component initialisation exception occurs in the
	 *             process.
	 */
	@Test
	public void testAllConfFiles() throws ComponentInitException {
		// we use a logger, which outputs few messages (warnings, errors)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.WARN);

		// map containing a list of conf files for each path
		HashMap<String, ArrayList<String>> confFiles = new HashMap<String, ArrayList<String>>();
		String exampleDir = "." + File.separator + "examples";
		File f = new File(exampleDir);
		QuickStart.getAllConfs(f, exampleDir, confFiles);

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		// ignore list (examples which are temporarily not working due
		// to server downtime, lack of features etc., but should still
		// remain in the example directory
		Set<String> ignore = new TreeSet<String>();
		ignore.add("./examples/sparql/govtrack.conf"); // HTTP 500 Server error
		ignore.add("./examples/sparql/musicbrainz.conf"); // HTTP 502 error - NullPointer in extraction
		ignore.add("./examples/sparql/SKOSTEST_local.conf"); // Out of Memory Error
		ignore.add("./examples/sparql/scrobble.conf"); // HTTP 502 Proxy Error
		ignore.add("./examples/family-benchmark/Cousin.conf"); // Out of Memory Error
		ignore.add("./examples/sparql/SilentBobWorking2.conf"); // Out of Memory Error
		ignore.add("./examples/family/father_posonly.conf"); // ArrayOutOfBoundsException in Pellet - main problem: pos only not working
		ignore.add("./examples/sparql/difference/DBPediaSKOS_kohl_vs_angela.conf"); // Pellet: literal cannot be cast to individual
		ignore.add("./examples/family-benchmark/Aunt.conf"); // did not terminate so far (waited 45 minutes)
		
		for (String path : confFiles.keySet()) {
			for (String file : confFiles.get(path)) {
				String conf = path + file + ".conf";
				if(ignore.contains(conf)) {
					System.out.println("Skipping " + conf + " (is on ignore list).");
				} else {
					System.out.println("Testing " + conf + " (time: " + sdf.format(new Date()) + ").");
					long startTime = System.nanoTime();
					// start example
					Start start = new Start(new File(conf));
					start.start(false);
					// test is successful if a concept was learned
					assert (start.getLearningAlgorithm().getCurrentlyBestDescription() != null);
					long timeNeeded = System.nanoTime() - startTime;
					start.getReasoningService().releaseKB();
					ComponentManager.getInstance().freeAllComponents();
					System.out.println("Test of " + conf + " completed in " + Helper.prettyPrintNanoSeconds(timeNeeded) + ".");
				}
			}
		}

	}

}
