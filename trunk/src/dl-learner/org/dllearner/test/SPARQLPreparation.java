package org.dllearner.test;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.parser.ParseException;

import com.hp.hpl.jena.query.ResultSet;

public class SPARQLPreparation {

	static Cache c;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SortedSet<String> concepts = new TreeSet<String>();
			HashMap<String,ResultSet> result = new HashMap<String,ResultSet>();
			HashMap<String,String> result2 = new HashMap<String,String>();
			
			SparqlQueryDescriptionConvertVisitor.debug_flag=false;
			c=new Cache("cache");
			String conj="(\"http://dbpedia.org/class/yago/Person100007846\" AND \"http://dbpedia.org/class/yago/Head110162991\")";
			
			
			concepts.add("EXISTS \"http://dbpedia.org/property/disambiguates\".TOP");
			concepts.add("EXISTS \"http://dbpedia.org/property/successor\".\"http://dbpedia.org/class/yago/Person100007846\"");
			concepts.add("EXISTS \"http://dbpedia.org/property/successor\"."+conj);
			//concepts.add("ALL \"http://dbpedia.org/property/disambiguates\".TOP");
			//concepts.add("ALL \"http://dbpedia.org/property/successor\".\"http://dbpedia.org/class/yago/Person100007846\"");
			concepts.add("\"http://dbpedia.org/class/yago/Person100007846\"");
			concepts.add(conj);
			concepts.add("(\"http://dbpedia.org/class/yago/Person100007846\" OR \"http://dbpedia.org/class/yago/Head110162991\")");
			
			//concepts.add("NOT \"http://dbpedia.org/class/yago/Person100007846\"");
			
			for (String kbsyntax : concepts) {
				result.put(kbsyntax,queryConcept(kbsyntax));
			}
			System.out.println("************************");
			for (String string : result.keySet()) {
				System.out.println("KBSyntayString: "+string);
				System.out.println("Query:\n"+result.get(string).hasNext());
				System.out.println("************************");
			}
			System.out.println("Finished");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

	}

	}
	
	public static ResultSet queryConcept(String concept){
		ResultSet rs =null;
		try{
		String query = SparqlQueryDescriptionConvertVisitor.getSparqlQuery(concept);
		SparqlQuery sq= new SparqlQuery(query,SparqlEndpoint.dbpediaEndpoint());
		String JSON = c.executeSparqlQuery(sq);
		
		rs= SparqlQuery.JSONtoResultSet(JSON);
		 
		}catch (Exception e) {e.printStackTrace();}
		
		return rs;
	}
	
}
