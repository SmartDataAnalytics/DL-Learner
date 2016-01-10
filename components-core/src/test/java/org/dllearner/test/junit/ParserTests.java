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
public class ParserTests {

	@Test
	public void KBParserTest() throws ParseException {
//		String test = "(\"Sentence\" AND (EXISTS \"syntaxTreeHasPart\".\"VVPP\" AND EXISTS \"syntaxTreeHasPart\".(\"stts:AuxilliaryVerb\" AND (\"hasLemma\" STRINGVALUE \"werden\"))))";
		String test = "(someproperty HASVALUE someindividual)";
		OWLClassExpression d = KBParser.parseConcept(test);
		System.out.println(d);
	}
	
}
