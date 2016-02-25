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

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.StringRenderer;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.junit.Test;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests on class expressins, e.g. transformations or tests on them.
 * 
 * @author Jens Lehmann
 *
 */
public class ClassExpressionTest {
	
	OWLDataFactory df = new OWLDataFactoryImpl();
	PrefixManager pm = new DefaultPrefixManager();

	public ClassExpressionTest() {
		pm.setDefaultPrefix("");
	}
	

	/**
	 * We have 
	 * male SubClassOf person
	 * Domain(hasChild, person)
	 * @throws ParseException
	 */
	@Test
	public void minimizeTest1() throws ParseException {
		OWLClassExpression ce = KBParser.parseConcept("(\"http://example.com/father#male\" AND (\"http://example.com/father#male\" OR EXISTS \"http://example.com/father#hasChild\".TOP))");		
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.FATHER_OE);
		OWLClassExpressionMinimizer minimizer = new OWLClassExpressionMinimizer(df, reasoner);
		OWLClassExpression minD = minimizer.minimize(ce);
		assertTrue(minD.equals(df.getOWLClass(IRI.create("http://example.com/father#male"))));
	}
	
	@Test
	public void minimizeTest2() throws ParseException {
		// this tests for a bug, when in A AND A AND SOMETHING, both A were removed because they subsume 
		// each other, while in fact only one A should be removed
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.MDM);
		OWLClassExpressionMinimizer minimizer = new OWLClassExpressionMinimizer(df, reasoner);
		OWLClass nc = df.getOWLClass(IRI.create("http://acl/BMV#MedicalThings"));
		OWLObjectProperty op = df.getOWLObjectProperty(IRI.create("http://acl/BMV#refersSubstance"));
		OWLClassExpression tmp1 = df.getOWLObjectAllValuesFrom(op,nc);
		OWLClassExpression d = df.getOWLObjectIntersectionOf(nc,tmp1);
		OWLClassExpression d2 = df.getOWLObjectIntersectionOf(nc,nc,tmp1);
		OWLClassExpression minD = minimizer.minimizeClone(d);
		OWLClassExpression minD2 = minimizer.minimizeClone(d2);

		assertEquals(minD.toString(),minD2.toString());
	}	
	
	@Test
	public void subExpressionTest1() throws ParseException {
		OWLClassExpression d = KBParser.parseConcept("(\"http://example.com/father#male\" AND (\"http://example.com/father#male\" OR EXISTS \"http://example.com/father#hasChild\".TOP))");		
		OWLClassExpression d2 = KBParser.parseConcept("EXISTS \"http://example.com/father#hasChild\".TOP");
		assertTrue(ConceptTransformation.isSubdescription(d, d2));
		
		OWLClassExpression d3 = KBParser.parseConcept("(\"http://example.com/test#A\" AND (\"http://example.com/father#A\" AND EXISTS \"http://example.com/father#hasChild\".TOP))");		
		OWLClassExpression d4 = KBParser.parseConcept("EXISTS \"http://example.com/father#hasChild\".TOP");
		assertTrue(ConceptTransformation.isSubdescription(d3, d4));	
		
		// related to http://sourceforge.net/tracker/?func=detail&atid=986319&aid=3029181&group_id=203619
		OWLClassExpression d5 = KBParser.parseConcept("(\"http://acl/BMV#MedicalThings\" AND (\"http://acl/BMV#MedicalThings\" AND ALL \"http://acl/BMV#refersSubstance\".\"http://acl/BMV#MedicalThings\"))");
		OWLClassExpression d6 = KBParser.parseConcept("ALL \"http://acl/BMV#refersSubstance\".\"http://acl/BMV#MedicalThings\"");
		assertTrue(ConceptTransformation.isSubdescription(d5, d6));	
		
	}
	
	/**
	 * We test a method, which delivers in which context all quantifiers occur
	 * (i.e. how they are nested in a class expression).
	 */
	@Test
	public void forAllContextTest() {
		// if we rely on toString comparison below we better be a bit more explicit...
		OWLObjectRenderer lastRenderer = StringRenderer.getRenderer();
		try {
			StringRenderer.setRenderer(StringRenderer.Rendering.OWLAPI_SYNTAX);
			// create some basic ontology elements
			OWLClass a1 = df.getOWLClass("a1", pm);
			OWLClass a2 = df.getOWLClass("a2", pm);
			OWLObjectProperty p1 = df.getOWLObjectProperty("p1", pm);
			OWLObjectProperty p2 = df.getOWLObjectProperty("p2", pm);
			OWLObjectProperty p3 = df.getOWLObjectProperty("p3", pm);
			OWLIndividual i1 = df.getOWLNamedIndividual("i1", pm);

			// create some class expressions
			OWLClassExpression d1 = df.getOWLObjectIntersectionOf(a1, a2);
			OWLClassExpression d2 = df.getOWLObjectAllValuesFrom(p1, a1);
			OWLClassExpression d3 = df.getOWLObjectUnionOf(d1, d2);
			OWLClassExpression d4 = df.getOWLObjectHasValue(p2, i1);
			OWLClassExpression d5 = df.getOWLObjectAllValuesFrom(p2, d2);
			OWLClassExpression d6 = df.getOWLObjectAllValuesFrom(p1, d4);
			OWLClassExpression d7 = df.getOWLObjectSomeValuesFrom(p3, d5);
			OWLClassExpression d8 = df.getOWLObjectUnionOf(d2, d5);
			OWLClassExpression d9 = df.getOWLObjectAllValuesFrom(p1, d8);

			// a1 AND a2 => should be empty result
			assertTrue(ConceptTransformation.getForallContexts(d1).isEmpty());
			// note: the assertions below use toString() which should usually be avoided, but since
			// the toString() method of SortedSet is unlikely to change we use it here for convenience
			// ALL p1.a1 => context: [[p1]]
			System.out.println("[[<p1>]] ? " + ConceptTransformation.getForallContexts(d2).toString());
			assertTrue(ConceptTransformation.getForallContexts(d2).toString().equals("[[<p1>]]"));
			// (a1 AND a2) OR ALL p1.a1 => [[p1]]
			System.out.println("[[<p1>]] ? " + ConceptTransformation.getForallContexts(d3).toString());
			assertTrue(ConceptTransformation.getForallContexts(d3).toString().equals("[[<p1>]]"));
			// p2 hasValue i1 => []
			assertTrue(ConceptTransformation.getForallContexts(d4).isEmpty());
			// ALL p2.ALL p1.a1 => [[p2],[p1,p2]]
			System.out.println("[[<p2>, <p1>], [<p2>]] ? " + ConceptTransformation.getForallContexts(d5).toString());
			assertTrue(ConceptTransformation.getForallContexts(d5).toString().equals("[[<p2>, <p1>], [<p2>]]"));
			// ALL p1.p2 hasValue i1 => [[p1]]
			System.out.println("[[<p1>]] ? " + ConceptTransformation.getForallContexts(d6).toString());
			assertTrue(ConceptTransformation.getForallContexts(d6).toString().equals("[[<p1>]]"));
			// EXISTS p3.ALL p2.ALL p1.a1 => [[p3,p2],[p3,p2,p1]]
			System.out.println("[[<p3>, <p2>, <p1>], [<p3>, <p2>]] ? " + ConceptTransformation.getForallContexts(d7).toString());
			assertTrue(ConceptTransformation.getForallContexts(d7).toString().equals("[[<p3>, <p2>, <p1>], [<p3>, <p2>]]"));
			// (ALL p1.a1 OR ALL p2.ALL p1.a1)
			assertTrue(ConceptTransformation.getForallContexts(d8).toString().equals("[[<p2>, <p1>], [<p1>], [<p2>]]"));
			// ALL p1.(ALL p1.a1 OR ((a1 AND a2) OR ALL p1.a1)) => [[p1],[p1,p1],[p1,p2],[p1,p2,p1]]
			assertTrue(ConceptTransformation.getForallContexts(d9).toString().equals("[[<p1>, <p2>, <p1>], [<p1>, <p1>], [<p1>, <p2>], [<p1>]]"));
		} finally {
			StringRenderer.setRenderer(lastRenderer);
		}
	}
}
