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

import org.dllearner.core.owl.Description;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxParser;
import org.junit.Test;

/**
 * 
 * OWL API specific tests.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPITests {

	@Test
	public void testManchesterSyntaxParser() throws ParseException {
//		String s = "BIGPROP SOME smallclass";
//		String s = "<http://test.de/prop> some <http://test.de/Class>";
		String s = "<http://test.de/Class>";
		Description d = ManchesterOWLSyntaxParser.getDescription(s);
		System.out.println(d);
	}
	
}
