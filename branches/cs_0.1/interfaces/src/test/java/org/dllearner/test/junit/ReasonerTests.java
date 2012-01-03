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
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.reasoning.ProtegeReasoner;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.owl.ConceptComparator;
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
	
	private ConceptComparator comparator = new ConceptComparator();

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
				//we skip the ProtegeReasoner, because the underlying OWLReasoner is not available in this test
				if(reasonerClass.equals(ProtegeReasoner.class)){
					continue;
				}
				if(excludeDIG && reasonerClass.equals(DIGReasoner.class)) {
					continue;
				}
				ReasonerComponent reasoner = cm.reasoner(reasonerClass, ks);
				reasoner.init();
				//if it is the PelletReasoner we have to call a separate method to make the CWA
				if(reasonerClass.equals(PelletReasoner.class)){
					((PelletReasoner)reasoner).dematerialise();
				}
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
	
	@Test
	public void domainTest() throws MalformedURLException, ComponentInitException, ParseException {
		
		ComponentManager cm = ComponentManager.getInstance();
		
		String kb = "person SUB animal.";
		kb += "man SUB (person AND male).";
		kb += "woman SUB (person AND female).";
		kb += "OPDOMAIN(hasChild) = (man AND woman).";
		kb += "male = male2.";
		
		
		KB kbf  = KBParser.parseKBFile(kb);
		KnowledgeSource ks = new KBFile(kbf);
		ks.init();
		ReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		reasoner.init();
		ObjectProperty property = new ObjectProperty(KBParser.getInternalURI("hasChild"));
		Description description = KBParser.parseConcept("man");
		assertTrue(reasoner.getDomain(property).equals(description));
		
//		reasoner.releaseKB();
//		ks = new KBFile(getSimpleKnowledgeBase());
//		ks.init();
//		reasoner = cm.reasoner(OWLAPIReasoner.class,ks);
//		reasoner.init();
//		property = new ObjectProperty(KBParser.getInternalURI("hasChild"));
//		description = KBParser.parseConcept("TOP");
//		assertTrue(reasoner.getDomain(property).equals(description));
		
		String file = "examples/family/father_oe.owl";
		ks = cm.knowledgeSource(OWLFile.class);
		cm.applyConfigEntry(ks, "url", new File(file).toURI().toURL());
		ks.init();
		reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		reasoner.init();
		property = new ObjectProperty(reasoner.getBaseURI() + "hasChild");
		description = new NamedClass(reasoner.getBaseURI() + "person");
		assertTrue(reasoner.getDomain(property).equals(description));
		
		file = "examples/arch/arch.owl";
		ks = cm.knowledgeSource(OWLFile.class);
		cm.applyConfigEntry(ks, "url", new File(file).toURI().toURL());
		ks.init();
		reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		reasoner.init();
		property = new ObjectProperty(reasoner.getBaseURI() + "hasParallelpipe");
		description = new NamedClass(reasoner.getBaseURI() + "construction");
		assertTrue(reasoner.getDomain(property).equals(description));
		
				
		file = "test/ore/koala.owl";
		ks = cm.knowledgeSource(OWLFile.class);
		cm.applyConfigEntry(ks, "url", new File(file).toURI().toURL());
		ks.init();
		reasoner = cm.reasoner(OWLAPIReasoner.class, ks);
		reasoner.init();
		DatatypeProperty dProperty = new DatatypeProperty(reasoner.getBaseURI() + "isHardWorking");
		description = new NamedClass(reasoner.getBaseURI() + "Person");
		assertTrue(reasoner.getDomain(dProperty).equals(description));
		
	}
	
	@Test
	public void pelletSlowConsistencyCheck() throws ParseException {
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SWORE);
		Description d = KBParser.parseConcept("<= 1 \"http://ns.softwiki.de/req/defines\".\"http://ns.softwiki.de/req/AbstractRequirement\"");
		NamedClass nc = new NamedClass("http://ns.softwiki.de/req/AbstractComment");
		Axiom axiom = new EquivalentClassesAxiom(nc, d);
		boolean res = rs.remainsSatisfiable(axiom);
		System.out.println(res);
	}
	
	@Test
	public void multipleKnowledgeSourcesTest() throws ComponentInitException {
		String file1 = "examples/father.owl";
		String file2 = "examples/lymphography/lymphography.owl";
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource ks1 = cm.knowledgeSource(OWLFile.class);
		KnowledgeSource ks2 = cm.knowledgeSource(OWLFile.class);
		try {
			cm.applyConfigEntry(ks1, "url", new File(file1).toURI().toURL());
			cm.applyConfigEntry(ks2, "url", new File(file2).toURI().toURL());
		} catch (MalformedURLException e) {
			// should never happen
			e.printStackTrace();
		}
		ks1.init();
		ks2.init();
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, ks1, ks2);
		reasoner.init();
		baseURI = reasoner.getBaseURI();
		System.out.println(reasoner.getSubClasses(Thing.instance));
		assertTrue(reasoner.getSubClasses(Thing.instance).size()==55);
	}
	
	@Test
	public void compareReasoners() throws FileNotFoundException, ComponentInitException, ParseException, org.dllearner.confparser.ParseException{
		
		ComponentManager cm = ComponentManager.getInstance();
		Start start;
		FastInstanceChecker reasoner;
		LearningProblem lp;
		LearningAlgorithm la;
		KnowledgeSource ks;
		
		for(File conf : getTestConfigFiles()){
			System.out.println("Test file: " + conf.getName());
			start = new Start(conf);
			lp = start.getLearningProblem();
			la = start.getLearningAlgorithm();
			ks = start.getSources().iterator().next();
			
			TreeSet<? extends EvaluatedDescription> result = new TreeSet<EvaluatedDescription>();
			
			for(String type : getReasonerTypes()){
				System.out.println("Using " + type + " reasoner...");
				try {
					reasoner = cm.reasoner(FastInstanceChecker.class, ks);
					reasoner.getConfigurator().setReasonerType(type);
					reasoner.init();
					
					lp.changeReasonerComponent(reasoner);
					lp.init();
					
					la.init();
					la.start();
					if(!result.isEmpty()){
						assertTrue(compareTreeSets(la.getCurrentlyBestEvaluatedDescriptions(), result));
					}
					
					result = la.getCurrentlyBestEvaluatedDescriptions();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	public boolean compareTreeSets(TreeSet<? extends EvaluatedDescription> tree1, TreeSet<? extends EvaluatedDescription> tree2){
		boolean equal = true;
		
		List<? extends EvaluatedDescription> list1 = new ArrayList<EvaluatedDescription>(tree1);
		List<? extends EvaluatedDescription> list2 = new ArrayList<EvaluatedDescription>(tree2);
		
		EvaluatedDescription d1;
		EvaluatedDescription d2;
		for(int i = 0; i < list1.size(); i++){
			d1 = list1.get(i);
			d2 = list2.get(i);
			if(!(comparator.compare(d1.getDescription(), d2.getDescription()) == 0) && 
					d1.getAccuracy() == d2.getAccuracy()){
				equal = false;
				break;
			}
		}
		
		return equal;
	}
	
	private Set<File> getTestConfigFiles(){
		Set<File> files = new HashSet<File>();
		File directory = new File("test" + File.separator + "testReasoners");
		for(File file : directory.listFiles()){
			if(file.toString().endsWith(".conf")){
				files.add(file);
			}
		}
		return files;
	}
	
	private List<String> getReasonerTypes(){
		List<String> reasonerTypes = new LinkedList<String>();
		reasonerTypes.add("pellet");
//		reasonerTypes.add("hermit"); too slow at the moment
		reasonerTypes.add("fact");
		reasonerTypes.add("owllink");
		
		return reasonerTypes;
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
