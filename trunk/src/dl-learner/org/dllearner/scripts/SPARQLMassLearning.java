package org.dllearner.scripts;

import java.net.URLEncoder;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.utilities.AutomaticExampleFinderSPARQL;
import org.dllearner.utilities.ConfWriter;
import org.dllearner.utilities.SimpleClock;

public class SPARQLMassLearning {

	static Cache c;
	static SparqlEndpoint se;
	private static Logger logger = Logger.getRootLogger();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		
		//vars
		boolean useRelated = true;
		boolean useSuperClasses = true;
		int poslimit = 10;
		int neglimit = 10;
		
		
		try {
			
			//System.out.println(logger.setLevel(TRACE));
			/*System.out.println(Level.DEBUG.getClass());
			System.out.println(Level.toLevel("INFO"));
			System.out.println(Level.INFO);
			System.exit(0);*/
			SimpleClock sc=new SimpleClock();
			SortedSet<String> concepts = new TreeSet<String>();
			//concepts.add("\"http://dbpedia.org/class/yago/Person100007846\"");
			//concepts.add("\"http://dbpedia.org/class/yago/FieldMarshal110086821\"");
			concepts.add("http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom");
			SortedSet<String> posExamples = new TreeSet<String>();
			SortedSet<String> negExamples = new TreeSet<String>();
			String url = "http://dbpedia.openlinksw.com:8890/sparql";
			//HashMap<String, ResultSet> result = new HashMap<String, ResultSet>();
			//HashMap<String, String> result2 = new HashMap<String, String>();
			
			AutomaticExampleFinderSPARQL ae= new AutomaticExampleFinderSPARQL( se);
			ae.init(concepts.first(), useRelated, useSuperClasses, poslimit, neglimit);
		
			posExamples = ae.getPosExamples();
			negExamples = ae.getNegExamples();
			
			System.out.println(posExamples);
			System.out.println(negExamples);
			//System.exit(0);
			String concept = concepts.first().replace("http://dbpedia.org/resource/Category:", "");
			concept = concept.replace("http://dbpedia.org/class/yago/", "");
			String confname = URLEncoder.encode(concept, "UTF-8")+".conf";
			//
			ConfWriter cf=new ConfWriter();
			cf.addToStats("relearned concept: "+concepts.first());
			
			//"relearned concept: ";
			cf.writeSPARQL(confname, posExamples, negExamples, url, new TreeSet<String>());
			//new LearnSparql().learn(posExamples, negExamples, "http://dbpedia.openlinksw.com:8890/sparql", new TreeSet<String>());
			
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

	

	

	public static void init() {
		
		se = SparqlEndpoint.dbpediaEndpoint();
		// create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.DEBUG);
		

	}
	
	

}
