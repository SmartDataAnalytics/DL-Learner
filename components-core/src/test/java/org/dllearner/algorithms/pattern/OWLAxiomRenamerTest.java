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
package org.dllearner.algorithms.pattern;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import static org.junit.Assert.assertEquals;

public class OWLAxiomRenamerTest {
	private OWLDataFactory df;
	
	private OWLClass clsA;
	private OWLClass clsB;
	private OWLClass clsC;
	private OWLObjectProperty propR;
	private OWLObjectProperty propS;
	private OWLIndividual indA;
	private OWLIndividual indB;
	
	private OWLAxiomRenamer renamer;

	private Rendering rendering = Rendering.MANCHESTER_SYNTAX;

	@Before
	public void setUp() throws Exception {
		df = new OWLDataFactoryImpl();
		PrefixManager pm = new DefaultPrefixManager();
		pm.setDefaultPrefix("http://examples.org/ontology#");
		
		clsA = df.getOWLClass("A", pm);
		clsB = df.getOWLClass("B", pm);
		clsC = df.getOWLClass("C", pm);
		
		propR = df.getOWLObjectProperty("r", pm);
		propS = df.getOWLObjectProperty("s", pm);
		
		indA = df.getOWLNamedIndividual("a", pm);
		indB = df.getOWLNamedIndividual("b", pm);
		
		renamer = new OWLAxiomRenamer(df);
	}

	@Test
	public void testRename() {
		StringRenderer.setRenderer(rendering);
		OWLAxiom ax1 = df.getOWLSubClassOfAxiom(clsA, clsB);
		OWLAxiom ax2 = df.getOWLSubClassOfAxiom(clsB, clsC);
		
		System.out.print(ax1 + "->");ax1 = renamer.rename(ax1);System.out.println(ax1);
		System.out.print(ax2 + "->");ax2 = renamer.rename(ax2);System.out.println(ax1);
		assertEquals(ax1, ax2);
		
		ax1 = df.getOWLSubClassOfAxiom(
				clsA, 
				df.getOWLObjectIntersectionOf(
						clsB, 
						df.getOWLObjectSomeValuesFrom(propR, clsC)));
		ax2 = df.getOWLSubClassOfAxiom(
				clsA, 
				df.getOWLObjectIntersectionOf(
						df.getOWLObjectSomeValuesFrom(propS, clsB), 
						clsC));
		System.out.print(ax1 + "->");ax1 = renamer.rename(ax1);System.out.println(ax1);
		System.out.print(ax2 + "->");ax2 = renamer.rename(ax2);System.out.println(ax2);
		assertEquals(ax1, ax2);
		
		ax1 = df.getOWLSubClassOfAxiom(
				clsA, 
				df.getOWLObjectIntersectionOf(
						clsB, 
						df.getOWLObjectSomeValuesFrom(
								propR, 
								df.getOWLObjectUnionOf(clsB, clsA))));
		ax2 = df.getOWLSubClassOfAxiom(
				clsA, 
				df.getOWLObjectIntersectionOf(
						df.getOWLObjectSomeValuesFrom(
								propS, 
								df.getOWLObjectUnionOf(clsB, clsA)), 
						clsB));
		System.out.print(ax1 + "->");ax1 = renamer.rename(ax1);System.out.println(ax1);
		System.out.print(ax2 + "->");ax2 = renamer.rename(ax2);System.out.println(ax2);
		assertEquals(ax1, ax2);

		ax1 = df.getOWLDisjointClassesAxiom(clsA, clsB, clsC);
		ax1 = renamer.rename(ax1);System.out.println(ax1);
		
	}
	
	public void testRenameOntology() throws OWLOntologyCreationException{
		String ontologyURL = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = man.getOWLDataFactory();
		OWLOntology ontology = man.loadOntology(IRI.create(ontologyURL));
		
		OWLAxiomRenamer renamer = new OWLAxiomRenamer(dataFactory);
		Multiset<OWLAxiom> multiset = HashMultiset.create();
		for (OWLAxiom axiom : ontology.getLogicalAxioms()) {
			OWLAxiom renamedAxiom = renamer.rename(axiom);
			multiset.add(renamedAxiom);
//			System.out.println(axiom + "-->" + renamedAxiom);
		}
		for (OWLAxiom owlAxiom : multiset.elementSet()) {
			System.out.println(owlAxiom + ": " + multiset.count(owlAxiom));
		}
	}

}
