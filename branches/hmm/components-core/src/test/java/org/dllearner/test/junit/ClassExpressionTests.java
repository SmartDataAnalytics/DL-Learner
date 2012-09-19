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

import static org.junit.Assert.*;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.Union;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.DescriptionMinimizer;
import org.junit.Test;

/**
 * Tests on class expressins, e.g. transformations or tests on them.
 * 
 * @author Jens Lehmann
 *
 */
public class ClassExpressionTests {

	@Test
	public void minimizeTest1() throws ParseException {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.FATHER_OE);
		DescriptionMinimizer minimizer = new DescriptionMinimizer(reasoner);
		Description d = KBParser.parseConcept("(\"http://example.com/father#male\" AND (\"http://example.com/father#male\" OR EXISTS \"http://example.com/father#hasChild\".TOP))");		
		Description minD = minimizer.minimize(d);
		assertTrue(minD.toString().equals("http://example.com/father#male"));
	}
	
	@Test
	public void minimizeTest2() throws ParseException {
		// this tests for a bug, when in A AND A AND SOMETHING, both A were removed because they subsume 
		// each other, while in fact only one A should be removed
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.MDM);
		DescriptionMinimizer minimizer = new DescriptionMinimizer(reasoner);
		NamedClass nc = new NamedClass("http://acl/BMV#MedicalThings");
		ObjectProperty op = new ObjectProperty("http://acl/BMV#refersSubstance");
		Description tmp1 = new ObjectAllRestriction(op,nc);
		Description d = new Intersection(nc,tmp1);
		Description d2 = new Intersection(nc,nc,tmp1);
		Description minD = minimizer.minimizeClone(d);
		Description minD2 = minimizer.minimizeClone(d2);

		assertEquals(minD.toString(),minD2.toString());
	}	
	
	@Test
	public void subExpressionTest1() throws ParseException {
		Description d = KBParser.parseConcept("(\"http://example.com/father#male\" AND (\"http://example.com/father#male\" OR EXISTS \"http://example.com/father#hasChild\".TOP))");		
		Description d2 = KBParser.parseConcept("EXISTS \"http://example.com/father#hasChild\".TOP");
		assertTrue(ConceptTransformation.isSubdescription(d, d2));
		
		Description d3 = KBParser.parseConcept("(\"http://example.com/test#A\" AND (\"http://example.com/father#A\" AND EXISTS \"http://example.com/father#hasChild\".TOP))");		
		Description d4 = KBParser.parseConcept("EXISTS \"http://example.com/father#hasChild\".TOP");
		assertTrue(ConceptTransformation.isSubdescription(d3, d4));	
		
		// related to http://sourceforge.net/tracker/?func=detail&atid=986319&aid=3029181&group_id=203619
		Description d5 = KBParser.parseConcept("(\"http://acl/BMV#MedicalThings\" AND (\"http://acl/BMV#MedicalThings\" AND ALL \"http://acl/BMV#refersSubstance\".\"http://acl/BMV#MedicalThings\"))");
		Description d6 = KBParser.parseConcept("ALL \"http://acl/BMV#refersSubstance\".\"http://acl/BMV#MedicalThings\"");
		assertTrue(ConceptTransformation.isSubdescription(d5, d6));	
		
	}
	
	/**
	 * We test a method, which delivers in which context all quantifiers occur
	 * (i.e. how they are nested in a class expression).
	 */
	@Test
	public void forAllContextTest() {
		// create some basic ontology elements
		NamedClass a1 = new NamedClass("a1");
		NamedClass a2 = new NamedClass("a2");
		ObjectProperty p1 = new ObjectProperty("p1");
		ObjectProperty p2 = new ObjectProperty("p2");
		ObjectProperty p3 = new ObjectProperty("p3");
		Individual i1 = new Individual("i1");
		
		// create some class expressions
		Description d1 = new Intersection(a1,a2);
		Description d2 = new ObjectAllRestriction(p1,a1);
		Description d3 = new Union(d1,d2);
		Description d4 = new ObjectValueRestriction(p2,i1);
		Description d5 = new ObjectAllRestriction(p2,d2);
		Description d6 = new ObjectAllRestriction(p1,d4);
		Description d7 = new ObjectSomeRestriction(p3,d5);
		Description d8 = new Union(d2,d5);
		Description d9 = new ObjectAllRestriction(p1,d8);
		
		// a1 AND a2 => should be empty result
		assertTrue(ConceptTransformation.getForallContexts(d1).isEmpty());
		// note: the assertions below use toString() which should usually be avoided, but since
		// the toString() method of SortedSet is unlikely to change we use it here for convenience
		// ALL p1.a1 => context: [[p1]]		
		assertTrue(ConceptTransformation.getForallContexts(d2).toString().equals("[[p1]]"));
		// (a1 AND a2) OR ALL p1.a1 => [[p1]]
		assertTrue(ConceptTransformation.getForallContexts(d3).toString().equals("[[p1]]"));
		// p2 hasValue i1 => []
		assertTrue(ConceptTransformation.getForallContexts(d4).isEmpty());
		// ALL p2.ALL p1.a1 => [[p2],[p1,p2]]
		assertTrue(ConceptTransformation.getForallContexts(d5).toString().equals("[[p2, p1], [p2]]"));
		// ALL p1.p2 hasValue i1 => [[p1]]
		assertTrue(ConceptTransformation.getForallContexts(d6).toString().equals("[[p1]]"));
		// EXISTS p3.ALL p2.ALL p1.a1 => [[p3,p2],[p3,p2,p1]]
		assertTrue(ConceptTransformation.getForallContexts(d7).toString().equals("[[p3, p2, p1], [p3, p2]]"));
		// (ALL p1.a1 OR ALL p2.ALL p1.a1)
		assertTrue(ConceptTransformation.getForallContexts(d8).toString().equals("[[p2, p1], [p1], [p2]]"));
		// ALL p1.(ALL p1.a1 OR ((a1 AND a2) OR ALL p1.a1)) => [[p1],[p1,p1],[p1,p2],[p1,p2,p1]]
		assertTrue(ConceptTransformation.getForallContexts(d9).toString().equals("[[p1, p2, p1], [p1, p1], [p1, p2], [p1]]"));
		
	}
}
