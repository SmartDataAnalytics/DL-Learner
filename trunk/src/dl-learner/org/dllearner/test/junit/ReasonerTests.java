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
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.junit.Test;

/**
 * A suite of JUnit tests related to the DL-Learner reasoning.
 * 
 * @author Jens Lehmann
 * 
 */
public class ReasonerTests {

	private static Logger logger = Logger.getLogger(ReasonerTests.class);
	
	private String baseURI;

	public KB getSimpleKnowledgeBase() {
		String kb = "person SUB TOP.";
		kb += "man SUB person.";
		kb += "man SUB male.";
		kb += "woman SUB person.";
		kb += "woman SUB female.";
		kb += "(male AND female) = BOTTOM.";
		kb += "man(stephen).";
		kb += "woman(maria).";
		kb += "hasChild(stephen,maria).";
		KB kbObject = null;
		try {
			kbObject = KBParser.parseKBFile(kb);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return kbObject;
	}

	/**
	 * Performs an instance checks on all reasoner components to verify that
	 * they all return the correct result.
	 */
	@Test
	public void instanceCheckTest() {
		
		// DIG can be excluded from test since it requires a separate DIG reasoner and is no
		// longer the default reasoning mechanism
		boolean excludeDIG = true;
		
		try {
			ComponentManager cm = ComponentManager.getInstance();
			KB kb = getSimpleKnowledgeBase();
			KnowledgeSource ks = new KBFile(kb);
			ks.init();
			Description d;
			// d = KBParser.parseConcept("man");
			d = KBParser.parseConcept("(person AND EXISTS hasChild.female)");
			Individual i = new Individual(KBParser.getInternalURI("stephen"));
			List<Class<? extends ReasonerComponent>> reasonerClasses = cm.getReasonerComponents();
			for (Class<? extends ReasonerComponent> reasonerClass : reasonerClasses) {
				if(excludeDIG && reasonerClass.equals(DIGReasoner.class)) {
					continue;
				}
				ReasonerComponent reasoner = cm.reasoner(reasonerClass, ks);
				reasoner.init();
//				long startTime = System.nanoTime();
				boolean result = false;
//				for(int n=0; n<10000; n++) {
					result = reasoner.hasType(d, i);
//				}
//				long time = System.nanoTime() - startTime;
				logger.debug("instance check: " + reasoner + " " + d + " " + i + " " + result);
				assertTrue(result);
			}
		} catch (ParseException e) {
			e.printStackTrace();
//		} catch (ReasoningMethodUnsupportedException e) {
//			e.printStackTrace(); 
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test of fast instance check algorithm on carcinogenesis data set.
	 * @throws ComponentInitException 
	 * @throws ParseException 
	 */
	@Test
	public void fastInstanceCheckTest() throws ComponentInitException, ParseException {
		String file = "examples/carcinogenesis/carcinogenesis.owl";
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource ks = cm.knowledgeSource(OWLFile.class);
		try {
			cm.applyConfigEntry(ks, "url", new File(file).toURI().toURL());
		} catch (MalformedURLException e) {
			// should never happen
			e.printStackTrace();
		}
		ks.init();
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, ks);
		reasoner.init();
		baseURI = reasoner.getBaseURI();
		
		List<Description> testDescriptions = new LinkedList<Description>();
		List<List<Individual>> posIndividuals = new LinkedList<List<Individual>>();
		List<List<Individual>> negIndividuals = new LinkedList<List<Individual>>();
		
		// TODO manually verify that the results are indeed correct 
		testDescriptions.add(KBParser.parseConcept("(\"http://dl-learner.org/carcinogenesis#Compound\" AND ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS TRUE) OR >= 2 \"http://dl-learner.org/carcinogenesis#hasStructure\".\"http://dl-learner.org/carcinogenesis#Ar_halide\"))"));
		posIndividuals.add(getIndSet("d113","d133","d171","d262","d265","d294","d68","d77","d79"));
		negIndividuals.add(getIndSet("d139","d199","d202","d203","d283","d42"));

		// TODO add more descriptions and instances
		
		// make the specified assertions
		for(int i=0; i<testDescriptions.size(); i++) {
			Description description = testDescriptions.get(i);
			List<Individual> pos = posIndividuals.get(i);
			List<Individual> neg = negIndividuals.get(i);
			
			for(Individual ind : pos) {
				System.out.println("description: " + description.toString(baseURI, null) + " individual: " + ind.toString(baseURI, null));
				assertTrue(reasoner.hasType(description, ind));
			}
			
			for(Individual ind : neg) {
				System.out.println("description: " + description.toString(baseURI, null) + " individual: " + ind.toString(baseURI, null));
				assertTrue(!reasoner.hasType(description, ind));
			}			
		}
	}

	@Test
	public void fastInstanceCheck2() throws ComponentInitException, ParseException {
		String file = "examples/epc/sap_epc.owl";
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource ks = cm.knowledgeSource(OWLFile.class);
		try {
			cm.applyConfigEntry(ks, "url", new File(file).toURI().toURL());
		} catch (MalformedURLException e) {
			// should never happen
			e.printStackTrace();
		}
		ks.init();
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, ks);
		reasoner.init();
		baseURI = reasoner.getBaseURI();
		
		Description description = KBParser.parseConcept("(\"http://localhost/aris/sap_model.owl#EPC\" AND EXISTS \"http://localhost/aris/sap_model.owl#hasModelElements\".(\"http://localhost/aris/sap_model.owl#Event\" AND >= 2 \"http://localhost/aris/sap_model.owl#previousObjects\".TOP))");
		Individual ind = new Individual("http://localhost/aris/sap_model.owl#e4j0__6_____u__");
		boolean result = reasoner.hasType(description, ind);
		System.out.println(result);
	}
	
	// simple unit test for new retrieval algorithm
	@Test
	public void fastInstanceCheck3() throws MalformedURLException, ComponentInitException, ParseException {
		String file = "examples/family/father_oe.owl";
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource ks = cm.knowledgeSource(OWLFile.class);
		cm.applyConfigEntry(ks, "url", new File(file).toURI().toURL());
		ks.init();
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, ks);
		reasoner.init();
		baseURI = reasoner.getBaseURI();
		Description description = KBParser.parseConcept("(\"http://example.com/father#male\" AND EXISTS \"http://example.com/father#hasChild\".TOP)");
//		Description description = KBParser.parseConcept("EXISTS \"http://example.com/father#hasChild\".TOP");
		SortedSet<Individual> result = reasoner.getIndividuals(description);
		assertTrue(result.size()==3);
		assertTrue(result.contains(new Individual("http://example.com/father#markus")));
		assertTrue(result.contains(new Individual("http://example.com/father#martin")));
		assertTrue(result.contains(new Individual("http://example.com/father#stefan")));
//		System.out.println(result);	
		
		Description description2 = KBParser.parseConcept("(\"http://example.com/father#male\" AND ALL \"http://example.com/father#hasChild\".\"http://example.com/father#father\")");
		SortedSet<Individual> result2 = reasoner.getIndividuals(description2);
		assertTrue(result2.size()==2);
		assertTrue(result2.contains(new Individual("http://example.com/father#heinz")));
		assertTrue(result2.contains(new Individual("http://example.com/father#stefan")));
	}
	
	private List<Individual> getIndSet(String... inds) {
		List<Individual> individuals = new LinkedList<Individual>();
		for(String ind : inds) {
			individuals.add(new Individual(uri(ind)));
		}
		return individuals;
	}
	
	private String uri(String name) {
//		return "\""+baseURI+name+"\"";
		return baseURI+name;
	}	
	
}
