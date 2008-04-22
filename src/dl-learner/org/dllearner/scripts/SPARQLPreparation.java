package org.dllearner.scripts;

import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.midi.SysexMessage;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.utilities.ConfWriter;
import org.dllearner.utilities.JenaResultSetConvenience;
import org.dllearner.utilities.LearnSparql;
import org.dllearner.utilities.SimpleClock;

import com.hp.hpl.jena.query.ResultSet;

public class SPARQLPreparation {

	static Cache c;
	static SparqlEndpoint se;
	private static Logger logger = Logger.getRootLogger();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		try {
			
			
			
			SimpleClock sc=new SimpleClock();
			SortedSet<String> concepts = new TreeSet<String>();
			//concepts.add("\"http://dbpedia.org/class/yago/Person100007846\"");
			concepts.add("\"http://dbpedia.org/class/yago/FieldMarshal110086821\"");
			SortedSet<String> posExamples = new TreeSet<String>();
			SortedSet<String> negExamples = new TreeSet<String>();
			String url = "http://dbpedia.openlinksw.com:8890/sparql";
			//HashMap<String, ResultSet> result = new HashMap<String, ResultSet>();
			//HashMap<String, String> result2 = new HashMap<String, String>();
			
			//System.out.println(concepts.first());
			posExamples = new JenaResultSetConvenience(queryConcept(concepts.first(),0))
						.getStringListForVariable("subject");

			for (String string : posExamples) {
				negExamples.addAll( getObjects(string));
				//if(neg.size()>=1)System.out.println(neg);
			}
			
			/*for (String string2 : negExamples) {
				if(posExamples.contains(string2)){
					System.out.println(string2);
					negExamples.remove(string2);
				};
			}*/
			//System.out.println(negExamples.size());
			negExamples.removeAll(posExamples);
			posExamples=shrink(posExamples,5);
			negExamples=shrink(negExamples,posExamples.size());
			//System.out.println(posExamples.first()));
			//System.out.println(posExamples.size());
			//System.out.println(negExamples.size());
			
			//
			new ConfWriter().writeSPARQL("aaa.conf", posExamples, negExamples, url, new TreeSet<String>());
			new LearnSparql().learn(posExamples, negExamples, "http://dbpedia.openlinksw.com:8890/sparql", new TreeSet<String>());
			
			sc.printAndSet("Finished");
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	/***************************************************************************
	 * *********************OLDCODE String
	 * conj="(\"http://dbpedia.org/class/yago/Person100007846\" AND
	 * \"http://dbpedia.org/class/yago/Head110162991\")";
	 * 
	 * 
	 * concepts.add("EXISTS \"http://dbpedia.org/property/disambiguates\".TOP");
	 * concepts.add("EXISTS
	 * \"http://dbpedia.org/property/successor\".\"http://dbpedia.org/class/yago/Person100007846\"");
	 * concepts.add("EXISTS \"http://dbpedia.org/property/successor\"."+conj);
	 * //concepts.add("ALL \"http://dbpedia.org/property/disambiguates\".TOP");
	 * //concepts.add("ALL
	 * \"http://dbpedia.org/property/successor\".\"http://dbpedia.org/class/yago/Person100007846\"");
	 * concepts.add("\"http://dbpedia.org/class/yago/Person100007846\"");
	 * concepts.add(conj);
	 * concepts.add("(\"http://dbpedia.org/class/yago/Person100007846\" OR
	 * \"http://dbpedia.org/class/yago/Head110162991\")");
	 * 
	 * //concepts.add("NOT \"http://dbpedia.org/class/yago/Person100007846\"");
	 * 
	 * for (String kbsyntax : concepts) {
	 * result.put(kbsyntax,queryConcept(kbsyntax)); }
	 * System.out.println("************************"); for (String string :
	 * result.keySet()) { System.out.println("KBSyntayString: "+string);
	 * System.out.println("Query:\n"+result.get(string).hasNext());
	 * System.out.println("************************"); }
	 **************************************************************************/

	static SortedSet<String> getObjects(String subject) {
		// SortedSet<String> result = new TreeSet<String>();

		String query = "SELECT * WHERE { \n" + "<" + subject + "> " + "?p ?o. \n"
				+ "FILTER (REGEX(str(?o), 'http://dbpedia.org/resource/')).\n"
				+ "FILTER (!REGEX(str(?p), 'http://www.w3.org/2004/02/skos'))\n"
				+ "}";
		//System.out.println(query);
		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		//System.out.println(JSON);
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		return rsc.getStringListForVariable("o");
	}

	public static ResultSet queryConcept(String concept,int limit) {
		ResultSet rs = null;
		try {
			String query = SparqlQueryDescriptionConvertVisitor
					.getSparqlQuery(concept,limit);
			
			SparqlQuery sq = new SparqlQuery(query, se);
			String JSON = c.executeSparqlQuery(sq);
			//System.out.println(JSON);
			rs = SparqlQuery.JSONtoResultSet(JSON);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}

	public static void init() {
		SparqlQueryDescriptionConvertVisitor.debug_flag = false;
		c = new Cache("cache");
		se = SparqlEndpoint.dbpediaEndpoint();
		// create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.DEBUG);
		

	}
	
	public static SortedSet<String> shrink(SortedSet<String> s, int limit) {
		SortedSet<String> ret = new TreeSet<String>();
		Random r = new Random();
		double treshold = ((double)limit)/s.size();
		//System.out.println("treshold"+howmany);
		//System.out.println("treshold"+allRetrieved.size());
		System.out.println("treshold"+treshold);
		
		for (String oneInd : s) {
			if(r.nextDouble()<treshold) {
				ret.add(oneInd);
				
			}
		}
		return ret;
	}

}
