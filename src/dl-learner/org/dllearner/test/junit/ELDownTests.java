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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.OWLFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.ELDown2;
import org.dllearner.refinementoperators.RefinementOperator;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.statistics.Stat;
import org.junit.Test;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Tests related to the EL downward refinement operator.
 * 
 * @author Jens Lehmann
 *
 */
public class ELDownTests {

	private static Logger logger = Logger.getLogger(ELDownTests.class);		
	
	/**
	 * Implementation of test case created by Christoph Haase for 
	 * new operator.
	 * 
	 * @throws ParseException Thrown if concept syntax does not correspond
	 * to current KB syntax.
	 * @throws ComponentInitException 
	 */
	@Test
	public void test1() throws ParseException, ComponentInitException {
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE);
		
		// input description
		Description input = KBParser.parseConcept("(human AND EXISTS has.animal)");
		System.out.println("refining: " + input.toString(KBParser.internalNamespace, null));		
		
		// TODO For this test, we need to turn instance based disjoints
		// off! (We do not have any instances here.)
		RefinementOperator operator = new ELDown2(rs);
		
		// desired refinements as strings
		Set<String> desiredString = new TreeSet<String>();
		desiredString.add("(human AND EXISTS hasPet.animal)");
		desiredString.add("(human AND EXISTS has.bird)");
		desiredString.add("(human AND EXISTS has.cat)");
		desiredString.add("((human AND EXISTS hasPet.TOP) AND EXISTS has.animal)");
		desiredString.add("((human AND EXISTS hasChild.TOP) AND EXISTS has.animal)");
		desiredString.add("((human AND EXISTS hasPet.TOP) AND EXISTS has.animal)");
		desiredString.add("((human AND EXISTS has.human) AND EXISTS has.animal)");
		desiredString.add("((human AND EXISTS has.EXISTS has.TOP) AND EXISTS has.animal)");
		desiredString.add("(human AND EXISTS has.(animal AND EXISTS has.TOP))");
		
		ConceptComparator cc = new ConceptComparator();
		SortedSet<Description> desired = new TreeSet<Description>(cc);
		for(String str : desiredString) {
			Description tmp = KBParser.parseConcept(str);
			// eliminate conjunctions nested in other conjunctions
			ConceptTransformation.cleanConcept(tmp);
			ConceptTransformation.transformToOrderedForm(tmp, cc);
			desired.add(tmp);
			System.out.println("desired: " + tmp.toString(KBParser.internalNamespace, null));
		}
		
		// perform refinement and compare solutions
		long startTime = System.nanoTime();
		Set<Description> refinements = operator.refine(input);
		long runTime = System.nanoTime() - startTime;
		logger.debug("Refinement step took " + Helper.prettyPrintNanoSeconds(runTime, true, true) + ".");
		boolean runStats = false;
		if(runStats) {
			Stat stat = new Stat();
			int runs = 1000;
			for(int run=0; run<runs; run++) {
				Monitor refinementTime = MonitorFactory.start("extraction time");
				startTime = System.nanoTime();
				refinements = operator.refine(input);
				runTime = System.nanoTime() - startTime;
				refinementTime.stop();
				
				stat.addNumber(runTime/1000000);
			}
//			System.out.println("Identical 2nd refinement step took " + Helper.prettyPrintNanoSeconds(runTime, true, true) + ".");
			System.out.println("average over " + runs + " runs:");
			System.out.println(stat.prettyPrint("ms"));
		}
	
		// number of refinements has to be correct and each produced
		// refinement must be in the set of desired refinements
//		assertTrue(refinements.size() == desired.size());
		System.out.println("\nproduced refinements and their unit test status (true = assertion satisfied):");
		for(Description refinement : refinements) {
			boolean ok = desired.contains(refinement);			
			System.out.println(ok + ": " + refinement.toString(KBParser.internalNamespace, null));
//			assertTrue(desired.contains(refinement));
		}
		
		File jamonlog = new File("log/jamontest.html");
		Files.createFile(jamonlog, MonitorFactory.getReport());		
		
		// generated by operator (and currently corresponding to its definition):
		// false (http://localhost/foo#human AND EXISTS http://localhost/foo#has.(http://localhost/foo#animal AND http://localhost/foo#human
		// false (http://localhost/foo#animal AND http://localhost/foo#human AND EXISTS http://localhost/foo#has.http://localhost/foo#animal
		// solution: element of ncc should be tested for disjointness with any other candidate (here: animal and human)
		
		// edge added, but refinement not recognized as being minimal
		// (http://localhost/foo#human AND EXISTS http://localhost/foo#has.http://localhost/foo#animal AND EXISTS http://localhost/foo#has.TOP)
	}
	
	@Test
	public void test2() throws ParseException, IOException {
//		Logger logger = Logger.getRootLogger();
//		logger.setLevel(Level.TRACE);
//		SimpleLayout layout = new SimpleLayout();
//		FileAppender app = new FileAppender(layout, "log/el/log.txt", false);
//		logger.removeAllAppenders();
//		logger.addAppender(app);			
		
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE);
		
		// input description
		Description input = KBParser.parseConcept("(human AND (EXISTS has.bird AND EXISTS has.cat))");
		ConceptTransformation.cleanConcept(input);
		
		RefinementOperator operator = new ELDown2(rs);
		
		operator.refine(input);
		
	}
	
	@Test
	public void test3() throws ComponentInitException, ParseException, IOException {
		
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.TRACE);
		SimpleLayout layout = new SimpleLayout();
		FileAppender app = new FileAppender(layout, "log/el/log.txt", false);
		logger.removeAllAppenders();
		logger.addAppender(app);	
		
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		String ont = "/home/jl/promotion/ontologien/galen2.owl";
		cm.applyConfigEntry(source, "url", new File(ont).toURI().toURL());
		source.init();
		ReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, source);
		reasoner.init();
		System.out.println("Galen loaded.");
		
		Description input = KBParser.parseConcept("(\"http://www.co-ode.org/ontologies/galen#15.0\" AND (\"http://www.co-ode.org/ontologies/galen#30.0\" AND (EXISTS \"http://www.co-ode.org/ontologies/galen#Attribute\".\"http://www.co-ode.org/ontologies/galen#5.0\" AND EXISTS \"http://www.co-ode.org/ontologies/galen#Attribute\".\"http://www.co-ode.org/ontologies/galen#6.0\")))");
		ConceptTransformation.cleanConcept(input);
		
		RefinementOperator operator = new ELDown2(reasoner);
		operator.refine(input);

	}
	
}
