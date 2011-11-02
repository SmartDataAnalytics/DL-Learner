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

import org.dllearner.core.owl.Description;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ManchesterSyntaxParser;
import org.dllearner.parser.ParseException;
import org.junit.Test;

/**
 * Tests for various parsers in DL-Learner.
 * 
 * 
 * @author Jens Lehmann.
 *
 */
public class ParserTests {

	@Test
	public void KBParserTest() throws ParseException {
//		String test = "(\"Sentence\" AND (EXISTS \"syntaxTreeHasPart\".\"VVPP\" AND EXISTS \"syntaxTreeHasPart\".(\"stts:AuxilliaryVerb\" AND (\"hasLemma\" STRINGVALUE \"werden\"))))";
		String test = "(someproperty HASVALUE someindividual)";
		Description d = KBParser.parseConcept(test);
		System.out.println(d.toKBSyntaxString("http://localhost/foo#", null));
		Description d2 = KBParser.parseConcept(d.toKBSyntaxString());
		System.out.println(d2.toKBSyntaxString("http://localhost/foo#", null));	
	}
	
	@Test
	public void ManchesterParserTest() throws ParseException {
		String[] tests = new String[] { 
		// simple URI
		"<http://example.com/foo>",
		// existential restriction
		"(<http://example.com/prop> some <http://example.com/class>)",
		// universal restriction
		"(<http://example.com/prop> only <http://example.com/class>)",
		// intersection
		"(<http://example.com/class1> and <http://example.com/class2>)",
		// disjunction
		"(<http://example.com/class1> or <http://example.com/class2>)",
		// has value
		"(<http://example.com/prop> value <http://example.com/ind>)",
		// has value with string
		"(<http://example.com/prop> value \"string\")",
		// nested expression
		"(<http://example.com/prop> some (<http://example.com/class1> and <http://example.com/class2>))",
		// another nested expression
		"(<http://nlp2rdf.lod2.eu/schema/string/Document> and (<http://nlp2rdf.lod2.eu/schema/string/subStringTrans> some <http://www.w3.org/2002/07/owl#Thing>))",
		// a test with a single quoted string
		"(<http://nlp2rdf.lod2.eu/schema/string/Document> and (<http://nlp2rdf.lod2.eu/schema/string/subStringTrans> some ( <http://nlp2rdf.lod2.eu/schema/sso/lemma> value 'copper')))"		
		};
		
		// loop through all test cases
		for(String test : tests) {
			System.out.print(test + " --> ");
			Description d = ManchesterSyntaxParser.parseClassExpression(test);
			System.out.println(d.toManchesterSyntaxString(null, null));
		}
		
	}
	
	@Test
	public void ParseAndSPARQLConvertTest() throws ParseException {
		// add your test strings here (do not use prefixes)
		String[] kbArray = new String[] { 
				"(a AND b)", 
				"(someproperty HASVALUE someindividual)" 
		};
		
		for(String kbString : kbArray) {
			// convert to description and back
			Description description = KBParser.parseConcept(kbString);
			String tmp = description.toKBSyntaxString(KBParser.internalNamespace, null);
			String kbString2 = tmp.replace("\"", "");
//			System.out.println(kbString);
//			System.out.println(kbString2);
			
			// convert to SPARQL
			String query1 = SparqlQueryDescriptionConvertVisitor.getSparqlQuery(description, 10, true, true);
			String query2 = SparqlQueryDescriptionConvertVisitor.getSparqlQuery(kbString, 10, true, true);
//			System.out.println(query1);
//			System.out.println(query2);
			
			assertTrue(kbString.equals(kbString2));
			assertTrue(query1.equals(query2));
		}
	}
	
}
