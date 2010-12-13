package org.dllearner.autosparql.server.evaluation;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

public class QuerySyntaxFilterScript {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String queryString = "SELECT ?var0 WHERE {<s> <p> ?var0;<p1> <o1>.}";
//		String queryString = "SELECT DISTINCT ?var0, ?var1 WHERE { <http://dbpedia.org/resource/Taylor_Swift> skos:subject ?var0. " +
//				"?var0 rdfs:label ?var1 FILTER langMatches( lang(?var1), \"en\" ) }";
		String queryString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX dbpedia: <http://dbpedia.org/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
				"SELECT ?var0 ?var1 WHERE { { ?var1 ?var0 :Bohol_Sea .} " +
				"FILTER ( ( isURI(?var1) && ( ! regex(?var0, \"^http://dbpedia.org/property/redirect\") ) ) " +
				"&& ( ! regex(?var0, \"^http://dbpedia.org/property/disambiguates\") ) ) }";
		Query query = QueryFactory.create(queryString);
		Element queryPattern = query.getQueryPattern();
//		System.out.println(queryPattern);
		if(queryPattern instanceof ElementGroup){
			for(Element element : ((ElementGroup) queryPattern).getElements()){
				if(element instanceof ElementTriplesBlock){
					BasicPattern triples = ((ElementTriplesBlock) element).getPattern();
					for(Triple triple : triples){
						System.out.println(triple);
						if(triple.getObject().isVariable()){
							System.out.println(triple.getObject().getName());
							System.out.println("Has to be filtered.");
						} else {
							
							System.out.println(triple.getObject().isVariable());
						}
						
					}
				}
			}
		}

	}

}
