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
