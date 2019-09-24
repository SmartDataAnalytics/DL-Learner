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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.CLI;
import org.dllearner.cli.QuickStart;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.Helper;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests related to learning problems in the examples directory.
 * 
 * @author Jens Lehmann
 * 
 */
public class ExampleTests {

	private int nThreads = 1;//Runtime.getRuntime().availableProcessors() - 1;

	/**
	 * This test runs all conf files in the examples directory. Each conf file
	 * corresponds to one unit test, which is successful if a concept was
	 * learned. This unit test takes several hours.
	 * 
	 * @throws ComponentInitException
	 *             If any component initialisation exception occurs in the
	 *             process.
	 */
	@Test
	public void testAllConfFiles() throws ComponentInitException {
		
		// if true, then examples are executed in random order (avoids the problem
		// that the same examples are tested first on several runs); otherwise
		// it runs the examples in alphabetical order
		boolean randomize = false;
		
		// GPs can be excluded temporarily (because those tests are very time-consuming)
		boolean testGP = false;
		
		// setting for SPARQL based tests (0 = no special treatment, 1 = test only SPARQL
		// examples, 2 = skip SPARQL tests)
		final int sparql = 2;
		
		// we use a logger, which outputs few messages (warnings, errors)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.WARN);

		// map containing a list of conf files for each path
		Map<String, List<String>> confFiles = new TreeMap<>();
		String exampleDir = ".." + File.separator + "examples";
		File f = new File(exampleDir);
		QuickStart.getAllConfs(f, exampleDir, confFiles);

		// put all examples in a flat list
		List<String> examples = new LinkedList<>();
		for(Map.Entry<String, List<String>> entry : confFiles.entrySet()) {
			for(String file : entry.getValue()) {
				examples.add(entry.getKey() + file + ".conf");
			}
		}
		
		if(randomize) {
			Collections.shuffle(examples, new Random());
		} else {
			Collections.sort(examples);
		}
		
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		// ignore list (examples which are temporarily not working due
		// to server downtime, lack of features etc., but should still
		// remain in the example directory
		Set<String> ignore = new HashSet<>();
		
		// "standard" ignores (no problem to keep those)
		ignore.add("./examples/krk/complete_no_draw.conf"); // refers to an OWL file, which has to be auto-generated
		ignore.add("./examples/krk/test_ZERO_against_1to16.conf"); // see above
		ignore.add("./examples/semantic_bible/sparqlbible.conf"); // requires local Joseki
		
		// temporarily not working (have a look at those before next release) 
		// ignore.add("./examples/family/father_posonly.conf"); // ArrayOutOfBoundsException in Pellet - main problem: pos only not working/supported
		
		// ignored due to errors (should be fixed; in case of long running problems or
		// our of memory, it is better to increase the noise parameter and add comments
		// in the conf file about "optimal" parameters)
		 ignore.add("./examples/sparql/govtrack.conf"); // blank node handling error
		 ignore.add("./examples/sparql/difference/DBPediaSKOS_kohl_vs_angela.conf"); // XML parse error (works sometimes)
		 
		//working fine here ignore.add("./examples/sparql/SKOSTEST_local.conf"); // Out of Memory Error
		// ignore.add("./examples/sparql/scrobble.conf"); // HTTP 502 Proxy Error
		// ignore.add("./examples/family-benchmark/Cousin.conf"); // Out of Memory Error => disallowing ALL helps (TODO find out details) 
		//also working fine ignore.add("./examples/sparql/SilentBobWorking2.conf"); // Out of Memory Error
		// ignore.add("./examples/sparql/difference/DBPediaSKOS_kohl_vs_angela.conf"); // Pellet: literal cannot be cast to individual
		// ignore.add("./examples/family-benchmark/Aunt.conf"); // did not terminate so far (waited 45 minutes)  => disallowing ALL helps (TODO find out details)
//		ignore.add("examples/krk/KRK_ZERO_against_1to5_fastInstance.conf"); // stack overflow
//		ignore.add("examples/krk/KRK_ONE_ZERO_fastInstance.conf"); // stack overflow
//		ignore.add("examples/krk/"); // too many stack overflows

		final AtomicInteger failedCounter = new AtomicInteger(0);
		final AtomicInteger counter = new AtomicInteger(1);
		final int total = examples.size();
		
		ExecutorService tp = Executors.newFixedThreadPool(nThreads);
		
		final StringBuffer failed = new StringBuffer();
		final StringBuffer successful = new StringBuffer();

		for (final String conf : examples) {
			boolean ignored = ignore.stream().anyMatch(conf::contains) || (sparql == 2 && conf.contains("sparql"));

			if (ignored) {
				System.out.println("Skipping " + conf + " (is on ignore list).");
			} else {
				
				tp.submit(() -> {
					System.out.println("Testing " + conf + " (example " + counter + " of " + total + ", time: " + sdf.format(new Date()) + ").");
					long startTime = System.nanoTime();
					boolean success = false, started = false;
					try {
						// start example
						CLI start = new CLI(new File(conf));
						start.init();

//							System.out.println("algorithm: " + start.getLearningAlgorithm());
						boolean isSparql = start.getKnowledgeSource() instanceof SparqlKnowledgeSource;
//							boolean isSparql = false;
						AbstractCELA algorithm = start.getLearningAlgorithm();
						if(
//									(testGP || !(algorithm instanceof GP)) &&
								(sparql == 0 || (sparql == 1 &&  isSparql) || (sparql == 2 && !isSparql) ) ) {
							start.run();
							started = true;
//								start.start(false);
							// test is successful if a concept was learned
							assert algorithm.getCurrentlyBestDescription() != null;
//								start.getReasonerComponent().releaseKB();
							success = true;
							successful.append(conf).append("\n");
						} else {
							System.out.println("Test skipped, because of GP or SPARQL settings.");
						}
					} catch (Exception e) {
						e.printStackTrace();
						// unit test not succesful (exceptions are caught explicitly to find
						assert ( false );
						failedCounter.incrementAndGet();
						failed.append("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
						failed.append(conf).append("\n");
						StringWriter writer = new StringWriter();
						PrintWriter printWriter = new PrintWriter( writer );
						e.printStackTrace(printWriter);
						printWriter.flush();
						failed.append(writer.toString());
					}
					long timeNeeded = System.nanoTime() - startTime;
					if(!success && started) {
						System.out.println("TEST FAILED.");
					}
					if(started) {
						System.out.println("Test of " + conf + " completed in " + Helper.prettyPrintNanoSeconds(timeNeeded) + ".");
					}
				});
				
			}	
			counter.incrementAndGet();
		}
		
		tp.shutdown();
		try {
			tp.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.err.println("Finished. " + failedCounter + " tests failed:");
		System.err.println(failed);

	}

}
