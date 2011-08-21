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
