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

package org.dllearner.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.algorithms.el.ELDescriptionTree;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Thing;
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

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ELDownTests.class);		
	
	/**
	 * Implementation of test case created by Christoph Haase for 
	 * new operator.
	 * 
	 * @throws ParseException Thrown if concept syntax does not correspond
	 * to current KB syntax.
	 * @throws ComponentInitException 
	 * @throws IOException 
	 */
	@Test
	public void test1() throws ParseException, ComponentInitException, IOException {
		System.out.println("TEST 1");		
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE);
		
//		ELDescriptionTree t = new ELDescriptionTree(rs);
//		ObjectProperty p1 = new ObjectProperty("p1");
//		ObjectProperty p2 = new ObjectProperty("p2");
//		NamedClass a1 = new NamedClass("a1");
//		ELDescriptionNode n1 = new ELDescriptionNode(t, a1);
//		ELDescriptionNode n2 = new ELDescriptionNode(n1, p1, a1);
//		
//		System.out.println(t);
//		System.out.println(t.getSize());
//		System.exit(0);
		
		// input description
		Description input = KBParser.parseConcept("(human AND EXISTS has.animal)");
		System.out.println("refining: " + input.toString(KBParser.internalNamespace, null));		
		
		// For this test, we need to turn instance based disjoints
		// off! (We do not have any instances here.)
		RefinementOperator operator = new ELDown2(rs, false);
		
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
		
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.TRACE);
		SimpleLayout layout = new SimpleLayout();
		FileAppender app = new FileAppender(layout, "log/el/test.txt", false);
		logger.removeAllAppenders();
		logger.addAppender(app);			
		
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
		assertTrue(refinements.size() == desired.size());
		System.out.println("\nproduced refinements and their unit test status (true = assertion satisfied):");
		for(Description refinement : refinements) {
			boolean ok = desired.contains(refinement);			
			System.out.println(ok + ": " + refinement.toString(KBParser.internalNamespace, null));
			assertTrue(desired.contains(refinement));
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
		System.out.println("TEST 2");			
		
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE_NO_DR);
		
		// input description
		Description input = KBParser.parseConcept("(human AND EXISTS hasPet.bird)");
		ConceptTransformation.cleanConcept(input);
		
		Set<String> desiredString = new TreeSet<String>();
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS has.human))");
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS has.cat))");
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS has.EXISTS has.TOP))");
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS has.(cat AND bird)))");
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS hasPet.cat))");
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS hasPet.EXISTS has.TOP))");
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS hasChild.TOP))");
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS hasPet.human))");
		desiredString.add("(human AND (EXISTS hasPet.bird AND EXISTS hasPet.(animal AND EXISTS has.TOP)))"); 
		desiredString.add("(human AND EXISTS hasPet.(bird AND cat))");
		desiredString.add("(human AND (EXISTS has.(animal AND EXISTS has.TOP) AND EXISTS hasPet.bird))");
		desiredString.add("(human AND (EXISTS has.(bird AND EXISTS has.TOP) AND EXISTS hasPet.bird))");
		desiredString.add("(human AND EXISTS hasPet.(bird AND EXISTS has.TOP))"); 
		
		ConceptComparator cc = new ConceptComparator();
		SortedSet<Description> desired = new TreeSet<Description>(cc);
		for(String str : desiredString) {
			Description tmp = KBParser.parseConcept(str);
			ConceptTransformation.cleanConcept(tmp);
			ConceptTransformation.transformToOrderedForm(tmp, cc);
			desired.add(tmp);
			System.out.println("desired: " + tmp.toString(KBParser.internalNamespace, null));
		}		
		
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.TRACE);
		SimpleLayout layout = new SimpleLayout();
		FileAppender app = new FileAppender(layout, "log/el/test_no_dr.txt", false);
		logger.removeAllAppenders();
		logger.addAppender(app);		
		
		RefinementOperator operator = new ELDown2(rs);
		
		Set<Description> refinements = operator.refine(input);
		
//		assertTrue(refinements.size() == desired.size());
		System.out.println("\nproduced refinements and their unit test status (true = assertion satisfied):");
		for(Description refinement : refinements) {
			ConceptTransformation.transformToOrderedForm(refinement, cc);
			boolean ok = desired.contains(refinement);
			System.out.println(ok + ": " + refinement.toString(KBParser.internalNamespace, null));
//			assertTrue(desired.contains(refinement));
		}		
	}
	
	@Test
	public void test3() throws ParseException, IOException {
		System.out.println("TEST 3");
		
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE_NO_DISJOINT);
		
		// input description
		Description input = KBParser.parseConcept("(human AND (EXISTS hasChild.human AND EXISTS has.animal))");
		ConceptTransformation.cleanConcept(input);
		
		Set<String> desiredString = new TreeSet<String>();
		desiredString.add("(human AND (animal AND (EXISTS hasChild.human AND EXISTS has.animal)))");
		desiredString.add("(human AND (EXISTS hasChild.human AND EXISTS has.(animal AND human)))");
		desiredString.add("(human AND (EXISTS hasChild.human AND EXISTS has.bird))");
		desiredString.add("(human AND (EXISTS hasChild.human AND EXISTS has.cat))");
		desiredString.add("(human AND (EXISTS hasChild.human AND EXISTS hasPet.animal))");
		desiredString.add("(human AND (EXISTS hasChild.human AND (EXISTS has.TOP AND EXISTS has.animal)))");
		desiredString.add("(human AND (EXISTS hasChild.human AND EXISTS has.(animal AND EXISTS has.TOP)))");
		desiredString.add("(human AND (EXISTS hasChild.human AND (EXISTS has.animal AND EXISTS has.EXISTS has.TOP)))");
		
		ConceptComparator cc = new ConceptComparator();
		SortedSet<Description> desired = new TreeSet<Description>(cc);
		for(String str : desiredString) {
			Description tmp = KBParser.parseConcept(str);
			ConceptTransformation.cleanConcept(tmp);
			ConceptTransformation.transformToOrderedForm(tmp, cc);
			desired.add(tmp);
			System.out.println("desired: " + tmp.toString(KBParser.internalNamespace, null));
		}		
		
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.TRACE);
		SimpleLayout layout = new SimpleLayout();
		FileAppender app = new FileAppender(layout, "log/el/test_no_disjoint.txt", false);
		logger.removeAllAppenders();
		logger.addAppender(app);		
		
		RefinementOperator operator = new ELDown2(rs);
		
		Set<Description> refinements = operator.refine(input);
		
//		assertTrue(refinements.size() == desired.size());
		System.out.println("\nproduced refinements and their unit test status (true = assertion satisfied):");
		for(Description refinement : refinements) {
			ConceptTransformation.transformToOrderedForm(refinement, cc);
			boolean ok = desired.contains(refinement);
			System.out.println(ok + ": " + refinement.toString(KBParser.internalNamespace, null));
//			assertTrue(desired.contains(refinement));
		}		
	}	
	
	// not part of the regular test suite, since Galen 2 is required
	@Test
	public void test4() throws ComponentInitException, ParseException, IOException {
		
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.TRACE);
		SimpleLayout layout = new SimpleLayout();
		FileAppender app = new FileAppender(layout, "log/el/log.txt", false);
		logger.removeAllAppenders();
		logger.addAppender(app);	
		
		String ont = "../test/galen2.owl";
		KnowledgeSource source = new OWLFile(ont);
		source.init();
		AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(source));
		reasoner.init();
		System.out.println("Galen loaded.");
		
//		Description input = KBParser.parseConcept("(\"http://www.co-ode.org/ontologies/galen#15.0\" AND (\"http://www.co-ode.org/ontologies/galen#30.0\" AND (EXISTS \"http://www.co-ode.org/ontologies/galen#Attribute\".\"http://www.co-ode.org/ontologies/galen#5.0\" AND EXISTS \"http://www.co-ode.org/ontologies/galen#Attribute\".\"http://www.co-ode.org/ontologies/galen#6.0\")))");
		Description input = KBParser.parseConcept("(\"http://www.co-ode.org/ontologies/galen#1.0\" AND (\"http://www.co-ode.org/ontologies/galen#10.0\" AND (EXISTS \"http://www.co-ode.org/ontologies/galen#DomainAttribute\".(\"http://www.co-ode.org/ontologies/galen#1.0\" AND (\"http://www.co-ode.org/ontologies/galen#6.0\" AND \"http://www.co-ode.org/ontologies/galen#TopCategory\")) AND EXISTS \"http://www.co-ode.org/ontologies/galen#Attribute\".(\"http://www.co-ode.org/ontologies/galen#1.0\" AND (\"http://www.co-ode.org/ontologies/galen#TopCategory\" AND EXISTS \"http://www.co-ode.org/ontologies/galen#Attribute\".TOP)))))");
		ConceptTransformation.cleanConcept(input);
		
		ELDown2 operator = new ELDown2(reasoner);
		operator.refine(input);
		
	}

	@Test
	public void test5() {
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.TRAINS_OWL);
		RefinementOperator operator = new ELDown2(rs);
		Set<Description> refinements = operator.refine(Thing.instance);
		for(Description refinement : refinements) {
			System.out.println(refinement);
		}
		
//		Set<Description> subClasses = rs.getSubClasses(Thing.instance);
//		for(Description cl : subClasses) {
//			System.out.println(cl);
//		}
	}
	
	//	 not part of the regular test suite, since Galen 2 is required
	@Test
	public void asTest() throws ComponentInitException, MalformedURLException {
		
		String ont = "../test/galen2.owl";
		KnowledgeSource source = new OWLFile(ont);
		source.init();
		AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(source));
		reasoner.init();
		System.out.println("Galen loaded.");
		
		ELDescriptionTree tree = new ELDescriptionTree(reasoner);
		NamedClass a1 = new NamedClass("http://www.co-ode.org/ontologies/galen#1.0");
		NamedClass a2 = new NamedClass("http://www.co-ode.org/ontologies/galen#10.0");
		NamedClass a3 = new NamedClass("http://www.co-ode.org/ontologies/galen#6.0");
		NamedClass a4 = new NamedClass("http://www.co-ode.org/ontologies/galen#TopCategory");
		ObjectProperty r1 = new ObjectProperty("http://www.co-ode.org/ontologies/galen#Attribute");
		ObjectProperty r2 = new ObjectProperty("http://www.co-ode.org/ontologies/galen#DomainAttribute");
		ELDescriptionNode v1 = new ELDescriptionNode(tree, a1, a2);
		ELDescriptionNode v2 = new ELDescriptionNode(v1, r2, a1, a3, a4);
		ELDescriptionNode v3 = new ELDescriptionNode(v1, r1, a1, a4);
		new ELDescriptionNode(v3, r1);
		
		ELDescriptionNode w = new ELDescriptionNode(v2, r1);

		ELDown2 operator = new ELDown2(reasoner);
		System.out.println(operator.asCheck(w));		
		
	}
	
}
