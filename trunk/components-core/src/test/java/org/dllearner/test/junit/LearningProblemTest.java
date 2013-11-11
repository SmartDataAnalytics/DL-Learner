/**
 * 
 */
package org.dllearner.test.junit;

import static org.junit.Assert.*;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.KBFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class LearningProblemTest {
	
	@Test
	public void posOnlyLPLearningTests() throws ComponentInitException {
		// create artificial ontology
		KB kb = new KB();
		String ns = "http://dl-learner.org/junit/";
		NamedClass[] nc = new NamedClass[5];
		for(int i=0; i<5; i++) {
			nc[i] = new NamedClass(ns + "A" + i);
		}
		Individual[] ind = new Individual[100];
		for(int i=0; i<100; i++) {
			ind[i] = new Individual(ns + "i" + i);
		}
		
		// assert individuals to owl:Thing (such that they exist in the knowledge base)
		for(int i=0; i<100; i++) {
			kb.addAxiom(new ClassAssertionAxiom(Thing.instance,ind[i]));
		}
		
		// A0
		kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[0]));
		kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[1]));
		kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[5]));
		
		// A1
		kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[0]));
		kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[1]));
		kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[2]));
		kb.addAxiom(new ClassAssertionAxiom(nc[1],ind[5]));
		
		AbstractKnowledgeSource ks = new KBFile(kb);
		
		AbstractReasonerComponent reasoner = new FastInstanceChecker(ks);
		reasoner.init();		
		
		SortedSet<Individual> positiveExamples = new TreeSet<Individual>(Sets.newHashSet(ind[0], ind[1], ind[2], ind[3], ind[4]));
		PosOnlyLP lp = new PosOnlyLP(reasoner);
		lp.setPositiveExamples(positiveExamples);
		
		assertEquals(lp.getAccuracyOrTooWeak(nc[0], 1.0), 2/3d, 0.000000001d); // P=2/3, R=2/5
		assertEquals(lp.getAccuracyOrTooWeak(nc[1], 1.0), 3/4d, 0.000000001d); // P=3/4, R=3/5
		assertEquals(lp.getAccuracyOrTooWeak(nc[2], 1.0), 0d, 0.000000001d); // P=0, R=0 
	}

}
