package org.dllearner.test;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;

public class ValueKBParserTest {
	public static void main(String[] args) {
		SortedSet<String> s = new TreeSet<String>();
		s.add("(\"http://nlp2rdf.org/ontology/Sentence\" AND (\"http://nlp2rdf.org/ontology/hasLemma\" value test )");
		
		for (String kbsyntax : s) {
			try {
				KBParser.parseConcept(kbsyntax);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
