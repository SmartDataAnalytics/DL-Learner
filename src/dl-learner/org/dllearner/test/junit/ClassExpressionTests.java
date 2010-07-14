/**
 * Copyright (C) 2007-2010, Jens Lehmann
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

import static org.junit.Assert.*;

import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
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
		ReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.FATHER_OE);
		DescriptionMinimizer minimizer = new DescriptionMinimizer(reasoner);
		Description d = KBParser.parseConcept("(\"http://example.com/father#male\" AND (\"http://example.com/father#male\" OR EXISTS \"http://example.com/father#hasChild\".TOP))");		
		Description minD = minimizer.minimize(d);
		assertTrue(minD.toString().equals("http://example.com/father#male"));
	}
	
	@Test
	public void minimizeTest2() throws ParseException {
		// this tests for a bug, when in A AND A AND SOMETHING, both A were removed because they subsume 
		// each other, while in fact only one A should be removed
		ReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.MDM);
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
}
