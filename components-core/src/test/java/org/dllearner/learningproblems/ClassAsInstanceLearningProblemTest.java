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
package org.dllearner.learningproblems;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Lorenz Buehmann
 *
 */
public class ClassAsInstanceLearningProblemTest {

	/**
	 * Test method for {@link org.dllearner.learningproblems.ClassAsInstanceLearningProblem#getAccuracyOrTooWeak(org.semanticweb.owlapi.model.OWLClassExpression, double)}.
	 */
	@Test
	public void testGetAccuracyOrTooWeak() throws Exception {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		OWLOntology ontology = man.createOntology();
		
		// p1 subPropertyOf p2
		man.addAxiom(ontology, df.getOWLSubObjectPropertyOfAxiom(
				df.getOWLObjectProperty(IRI.create("p1")), 
				df.getOWLObjectProperty(IRI.create("p2"))));
		
		// n classes total
		int n = 10;
		for(int i = 0; i < n; i++) {
			man.addAxiom(ontology, df.getOWLDeclarationAxiom(df.getOWLClass(IRI.create("A" + i))));
		}
		
		// n_p positive examples
		int n_p = 5;
		Set<OWLClass> posExamples = new HashSet<>();
		for(int i = 0; i < n_p; i++) {
			posExamples.add(df.getOWLClass(IRI.create("A" + i)));
		}
		
		// n_t examples fulfill target concept requirements
		int n_t = (int) (0.5 * n_p);
		OWLObjectProperty p1 = df.getOWLObjectProperty(IRI.create("p1"));
		OWLObjectProperty p2 = df.getOWLObjectProperty(IRI.create("p2"));
		for(int i = 0; i < n_t; i++) {
			man.addAxiom(ontology, df.getOWLSubClassOfAxiom(
					df.getOWLClass(IRI.create("A" + i)), 
					df.getOWLObjectSomeValuesFrom(Math.random() < 0.5 ? p1 : p2, df.getOWLThing())));
		}
		
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		AbstractReasonerComponent reasoner = new OWLAPIReasoner(ks);
		reasoner.init();
		
		ClassAsInstanceLearningProblem lp = new ClassAsInstanceLearningProblem();
		lp.setReasoner(reasoner);
		lp.setPositiveExamples(posExamples);
		lp.init();
		
		OWLObjectSomeValuesFrom target = df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(IRI.create("p2")), df.getOWLThing());
		double accuracy = lp.getAccuracyOrTooWeak(target, 1.0);
		
		assertTrue(accuracy == 0.4);
	}

}
