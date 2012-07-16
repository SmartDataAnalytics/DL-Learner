package org.dllearner.algorithm.tbsl.util;

import java.util.HashMap;
import java.util.Map;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Property;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class PMI {
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache;
	
	public PMI(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	public double getDirectedPMI(ObjectProperty prop, NamedClass cls){
		System.out.println(String.format("Computing PMI(%s, %s)", prop, cls));
		String query  = String.format("SELECT (COUNT(?x) AS ?cnt) WHERE {?x a <%s>}", cls.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		double classOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		System.out.println("Class occurence: " + classOccurenceCnt);
		
		query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}", prop.getName());
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		double propertyOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		System.out.println("Property occurence: " + propertyOccurenceCnt);
		
		query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o. ?o a <%s>}", prop.getName(), cls.getName());
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		double coOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		System.out.println("Co-occurence: " + coOccurenceCnt);
		
		query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o}");
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		double total = rs.next().getLiteral("cnt").getInt();
		System.out.println("Total: " + total);
		
		if(classOccurenceCnt == 0 || propertyOccurenceCnt == 0 || coOccurenceCnt == 0){
			return 0;
		}
		
		double pmi = Math.log( (coOccurenceCnt * total) / (classOccurenceCnt * propertyOccurenceCnt) );
		
		return pmi;
	}
	
	public double getDirectedPMI(NamedClass cls, Property prop){
		System.out.println(String.format("Computing PMI(%s, %s)", cls, prop));
		String query  = String.format("SELECT (COUNT(?x) AS ?cnt) WHERE {?x a <%s>}", cls.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		double classOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		System.out.println("Class occurence: " + classOccurenceCnt);
		
		query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}", prop.getName());
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		double propertyOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		System.out.println("Property occurence: " + propertyOccurenceCnt);
		
		query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s a <%s>. ?s <%s> ?o}", cls.getName(), prop.getName());
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		double coOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		System.out.println("Co-occurence: " + coOccurenceCnt);
		
		query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o}");
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		double total = rs.next().getLiteral("cnt").getInt();
		System.out.println("Total: " + total);
		
		if(classOccurenceCnt == 0 || propertyOccurenceCnt == 0 || coOccurenceCnt == 0){
			return 0;
		}
		
		double pmi = Math.log( (coOccurenceCnt * total) / (classOccurenceCnt * propertyOccurenceCnt) );
		
		return pmi;
	}
	
	/**
	 * Returns the direction of the given triple, computed by calculated the PMI values of each combination.
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return -1 if the given triple should by reversed, else 1.
	 */
	public int getDirection(NamedClass subject, ObjectProperty predicate, NamedClass object){
		System.out.println(String.format("Computing direction between [%s, %s, %s]", subject, predicate, object));
		double pmi_obj_pred = getDirectedPMI(object, predicate);System.out.println("PMI(OBJECT, PREDICATE): " + pmi_obj_pred);
		double pmi_pred_subj = getDirectedPMI(predicate, subject);System.out.println("PMI(PREDICATE, SUBJECT): " + pmi_pred_subj);
		double pmi_subj_pred = getDirectedPMI(subject, predicate);System.out.println("PMI(SUBJECT, PREDICATE): " + pmi_subj_pred);
		double pmi_pred_obj = getDirectedPMI(predicate, object);System.out.println("PMI(PREDICATE, OBJECT): " + pmi_pred_obj);
		
		double threshold = 2.0;
		
		double value = ((pmi_obj_pred + pmi_pred_subj) - (pmi_subj_pred + pmi_pred_obj));
		System.out.println("(PMI(OBJECT, PREDICATE) + PMI(PREDICATE, SUBJECT)) - (PMI(SUBJECT, PREDICATE) + PMI(PREDICATE, OBJECT)) = " + value);
		
		if( value > threshold){
			System.out.println(object + "---" + predicate + "--->" + subject);
			return -1;
		} else {
			System.out.println(subject + "---" + predicate + "--->" + object);
			return 1;
		}
	}
	
	public Map<ObjectProperty, Integer> getMostFrequentProperties(NamedClass cls1, NamedClass cls2){
		Map<ObjectProperty, Integer> prop2Cnt = new HashMap<ObjectProperty, Integer>();
		String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?x1 a <%s>. ?x2 a <%s>. ?x1 ?p ?x2} GROUP BY ?p", cls1, cls2);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			ObjectProperty p = new ObjectProperty(qs.getResource("p").getURI());
			int cnt = qs.getLiteral("cnt").getInt();
			prop2Cnt.put(p, cnt);
		}
		return prop2Cnt;
	}
	
	public static void main(String[] args) {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		ExtractionDBCache cache = new ExtractionDBCache("cache");
		String NS = "http://dbpedia.org/ontology/";
		
		PMI pmiGen = new PMI(endpoint, cache);
		System.out.println(pmiGen.getDirectedPMI(
				new ObjectProperty(NS + "author"), 
				new NamedClass(NS+ "Person")));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirectedPMI(
				new ObjectProperty(NS + "author"), 
				new NamedClass(NS+ "Writer")));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirectedPMI(
				new NamedClass(NS+ "Book"),
				new ObjectProperty(NS + "author")) 
				);
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirection(
				new NamedClass(NS+ "Writer"), 
				new ObjectProperty(NS + "author"), 
				new NamedClass(NS+ "Book")));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirection(
				new NamedClass(NS+ "Person"), 
				new ObjectProperty(NS + "starring"), 
				new NamedClass(NS+ "Film")));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(
				new NamedClass(NS+ "Person"), 
				new NamedClass(NS+ "Film")));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(
				new NamedClass(NS+ "Film"), 
				new NamedClass(NS+ "Actor")));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(
				new NamedClass(NS+ "Film"), 
				new NamedClass(NS+ "Person")));
		
	}

}
