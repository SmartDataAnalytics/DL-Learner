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

import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Tests for various parsers in DL-Learner.
 * 
 * 
 * @author Jens Lehmann.
 *
 */
public class ParserTest {

	@Test
	public void KBParserTest() throws ParseException {
//		String test = "(\"Sentence\" AND (EXISTS \"syntaxTreeHasPart\".\"VVPP\" AND EXISTS \"syntaxTreeHasPart\".(\"stts:AuxilliaryVerb\" AND (\"hasLemma\" STRINGVALUE \"werden\"))))";
		String test = "(someproperty HASVALUE someindividual)";
		OWLClassExpression d = KBParser.parseConcept(test);
		System.out.println(d);
	}
	
}
