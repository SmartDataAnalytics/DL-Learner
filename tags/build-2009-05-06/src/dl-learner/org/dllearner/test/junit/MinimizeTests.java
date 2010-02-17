/**
 * Copyright (C) 2007-2009, Jens Lehmann
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

import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.owl.DescriptionMinimizer;
import org.junit.Test;

/**
 * Tests for minimizing class descriptions.
 * 
 * @author Jens Lehmann
 *
 */
public class MinimizeTests {

	@Test
	public void minimizeTest1() throws ParseException {
		ReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.FATHER_OE);
		DescriptionMinimizer minimizer = new DescriptionMinimizer(reasoner);
		Description d = KBParser.parseConcept("(\"http://example.com/father#male\" AND (\"http://example.com/father#male\" OR EXISTS \"http://example.com/father#hasChild\".TOP))");		
		Description minD = minimizer.minimize(d);
		assert(minD.toString().equals("http://example.com/father#male"));
	}
}
