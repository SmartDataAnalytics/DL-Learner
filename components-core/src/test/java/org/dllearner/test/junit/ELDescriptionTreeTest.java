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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TreeSet;

import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.algorithms.el.ELDescriptionTree;
import org.dllearner.algorithms.el.ELDescriptionTreeComparator;
import org.dllearner.algorithms.el.Simulation;
import org.dllearner.algorithms.el.TreeTuple;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 * Tests related to EL description tree including operations on
 * them, simulations, equivalence checks, minimisation etc.
 * 
 * @author Jens Lehmann
 *
 */
public final class ELDescriptionTreeTest {

	@Test
	public void simulationTest() {
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.EMPTY);
		Simulation s = new Simulation();
		ELDescriptionTree tree1 = new ELDescriptionTree(rs);
		ELDescriptionTree tree2 = new ELDescriptionTree(rs);
		ELDescriptionNode t1 = new ELDescriptionNode(tree1);
		ELDescriptionNode t2 = new ELDescriptionNode(tree2);
		TreeTuple tuple1 = new TreeTuple(t1,t2);
		s.addTuple(tuple1);
		assertTrue(s.in(t2).size() == 1);
//		assertTrue(s.out(t2).size() == 0);
		OWLObjectProperty p = new OWLObjectPropertyImpl(IRI.create("p"));
		TreeSet<OWLClass> l3 = new TreeSet<>();
		ELDescriptionNode t3 = new ELDescriptionNode(t1,p,l3);
		assertTrue(t3.getLevel() == 2);
		assertTrue(tree1.getMaxLevel() == 2);
	}
	
	@Test
	public void minimalityTest() throws ParseException, ComponentInitException {
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE);
		// the following should be recognized as non-minimal
		OWLClassExpression d = KBParser.parseConcept("(human AND (EXISTS has.animal AND EXISTS has.TOP))");
		d = ConceptTransformation.cleanConcept(d);
		ELDescriptionTree tree = new ELDescriptionTree(rs, d);
		assertFalse(tree.isMinimal());
	}
	
	@Test
	public void cloneTest() throws ParseException {
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.EMPTY);
		OWLClassExpression d = KBParser.parseConcept("(male AND (human AND EXISTS hasChild.(female AND EXISTS hasChild.male)))");
		d = ConceptTransformation.cleanConcept(d);
		ELDescriptionTree tree = new ELDescriptionTree(rs, d);
		// clone performance (false for simple unit test, true for clone performance test)
		boolean testPerformance = false;
		ELDescriptionTree treeCloned = null;
		if(testPerformance) {
			int runs = 1000000;
			long startTime = System.nanoTime();
			for(int i=0; i<runs; i++) {
				treeCloned = tree.clone();
			}
			long runTime = System.nanoTime() - startTime;
			System.out.println(Helper.prettyPrintNanoSeconds(runTime/runs, true, true) + " per clone operation");
		} else {
			treeCloned = tree.clone();
		}
		
		ELDescriptionTreeComparator comparator = new ELDescriptionTreeComparator();
		assertTrue(comparator.compare(tree, treeCloned) == 0);
	}
	
}
