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

import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.parser.ParseException;
import org.junit.Test;

public class SparqlQueryConverter {

	@Test
	public static void test(){
		SortedSet<String> s = new TreeSet<String>();
		s.add("(\"http://dbpedia.org/ontology/Work\" AND (EXISTS \"http://dbpedia.org/property/hasPhotoCollection\".TOP AND (\"http://dbpedia.org/ontology/TelevisionEpisode\" OR EXISTS \"http://xmlns.com/foaf/0.1/depiction\".TOP)))");
		s.add("(\"http://dbpedia.org/ontology/Work\" AND (EXISTS \"http://dbpedia.org/property/hasPhotoCollection\".TOP AND \"http://dbpedia.org/ontology/TelevisionEpisode\"))");
		s.add(" ( EXISTS \"http://dbpedia.org/property/hasPhotoCollection\".TOP AND \"http://dbpedia.org/ontology/TelevisionEpisode\")");
		s.add(" EXISTS \"http://dbpedia.org/property/hasPhotoCollection\".TOP ");
		s.add(" EXISTS \"http://dbpedia.org/property/hasPhotoCollection\".(TOP OR \"http://dbpedia.org/ontology/TelevisionEpisode\") ");
		s.add(" EXISTS \"http://dbpedia.org/property/hasPhotoCollection\".(TOP AND \"http://dbpedia.org/ontology/TelevisionEpisode\") ");
		convert(s);
		if (true) {
			System.exit(0);
		}
	} 

	private static void convert(Set<String> s){
	try{
		HashMap<String, String> result = new HashMap<String, String>();
		String query = "";
		SparqlQueryDescriptionConvertVisitor visit = new SparqlQueryDescriptionConvertVisitor();
		visit.setLabels(false);
		visit.setDistinct(false);
//		visit.setClassToSubclassesVirtuoso(subclassMap);
		
		
		
		for (String kbsyntax : s) {
			query = visit.getSparqlQuery(kbsyntax);
			result.put(kbsyntax, query);
		}
		System.out.println("************************");
		for (String string : result.keySet()) {
			System.out.println("KBSyntayString: " + string);
			System.out.println("Query:\n" + result.get(string));
			System.out.println("************************");
		}
		System.out.println("Finished");
	} catch (ParseException e) {
		e.printStackTrace();
	}
	}
}
