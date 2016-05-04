/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import static org.junit.Assert.assertEquals;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class LearningProblemTest {
	
	@Test
	public void posOnlyLPLearningTests() throws ComponentInitException, OWLOntologyCreationException {
		// create artificial ontology
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		OWLOntology kb = man.createOntology();
		String ns = "http://dl-learner.org/junit/";
		PrefixManager pm = new DefaultPrefixManager();
		pm.setDefaultPrefix(ns);
		OWLClass[] nc = new OWLClass[5];
		for(int i=0; i<5; i++) {
			nc[i] = df.getOWLClass("A" + i, pm);
		}
		OWLIndividual[] ind = new OWLIndividual[100];
		for(int i=0; i<100; i++) {
			ind[i] = df.getOWLNamedIndividual("i" + i, pm);
		}
		
		// assert individuals to owl:Thing (such that they exist in the knowledge base)
		for(int i=0; i<100; i++) {
			man.addAxiom(kb, df.getOWLClassAssertionAxiom(df.getOWLThing(), ind[i]));
		}
		
		// A0
		man.addAxiom(kb, df.getOWLClassAssertionAxiom(nc[0],ind[0]));
		man.addAxiom(kb, df.getOWLClassAssertionAxiom(nc[0],ind[1]));
		man.addAxiom(kb, df.getOWLClassAssertionAxiom(nc[0],ind[5]));
		
		// A1
		man.addAxiom(kb, df.getOWLClassAssertionAxiom(nc[1],ind[0]));
		man.addAxiom(kb, df.getOWLClassAssertionAxiom(nc[1],ind[1]));
		man.addAxiom(kb, df.getOWLClassAssertionAxiom(nc[1],ind[2]));
		man.addAxiom(kb, df.getOWLClassAssertionAxiom(nc[1],ind[5]));
		
		AbstractKnowledgeSource ks = new OWLAPIOntology(kb);
		
		AbstractReasonerComponent reasoner = new ClosedWorldReasoner(ks);
		reasoner.init();		
		
		SortedSet<OWLIndividual> positiveExamples = new TreeSet<>(Sets.newHashSet(ind[0], ind[1], ind[2], ind[3], ind[4]));
		PosOnlyLP lp = new PosOnlyLP(reasoner);
		lp.setPositiveExamples(positiveExamples);
		
		assertEquals(lp.getAccuracyOrTooWeak(nc[0], 1.0), 2/3d, 0.000000001d); // P=2/3, R=2/5
		assertEquals(lp.getAccuracyOrTooWeak(nc[1], 1.0), 3/4d, 0.000000001d); // P=3/4, R=3/5
		assertEquals(lp.getAccuracyOrTooWeak(nc[2], 1.0), 0d, 0.000000001d); // P=0, R=0 
	}

}
