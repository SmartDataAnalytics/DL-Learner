package org.dllearner.scripts;

import java.net.URLEncoder;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
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
		boolean useRelated = false;
		boolean useSuperClasses = false;
		boolean useParallelClasses = true;
		int poslimit = 10;
		int neglimit = 20;
		
		
		
		try {
			//logger.setLevel(Level.TRACE);
			Logger.getLogger(SparqlKnowledgeSource.class).setLevel(Level.INFO);
			//System.out.println(Logger.getLogger(SparqlQuery.class).getLevel());
		
			/*System.out.println(Level.DEBUG.getClass());
			System.out.println(Level.toLevel("INFO"));
			System.out.println(Level.INFO);*/
			//System.exit(0);
			SimpleClock sc=new SimpleClock();
			//concepts.add("(EXISTS \"monarch\".TOP AND EXISTS \"predecessor\".(\"Knight\" OR \"Secretary\"))");
					
			SortedSet<String> concepts = new TreeSet<String>();
			concepts.add("(\"http://dbpedia.org/class/yago/HeadOfState110164747\" AND (\"http://dbpedia.org/class/yago/Negotiator110351874\" AND \"http://dbpedia.org/class/yago/Representative110522035\"))");
			//concepts.add("\"http://dbpedia.org/class/yago/Person100007846\"");
			//concepts.add("\"http://dbpedia.org/class/yago/FieldMarshal110086821\"");
			//concepts.add("http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom");
			//concepts.add("http://dbpedia.org/resource/Category:Grammy_Award_winners");
			//concepts.add("EXISTS \"http://dbpedia.org/property/grammyawards\".TOP");
			
			SortedSet<String> posExamples = new TreeSet<String>();
			SortedSet<String> negExamples = new TreeSet<String>();
			String url = "http://dbpedia.openlinksw.com:8890/sparql";
			//HashMap<String, ResultSet> result = new HashMap<String, ResultSet>();
			//HashMap<String, String> result2 = new HashMap<String, String>();
			//System.out.println(concepts.first());
			//logger.setLevel(Level.TRACE);
			AutomaticExampleFinderSPARQL ae= new AutomaticExampleFinderSPARQL( se);
			//System.out.println(new JenaResultSetConvenience(ae.queryConcept(concepts.first(), 0)).getStringListForVariable("?subject")   );;
			//System.out.println(new JenaResultSetConvenience(ae.queryConcept(concepts.first(), 0)).getStringListForVariable("?subject").size()   );;
			//ae.getSubClasses(concepts.first());
			//System.exit(0);
			
			ae.init(concepts.first(), useRelated, useSuperClasses,useParallelClasses, poslimit, neglimit);
		
			posExamples = ae.getPosExamples();
			negExamples = ae.getNegExamples();
			
			System.out.println(posExamples);
			System.out.println(negExamples);
			//System.exit(0);
			String tmp = concepts.first().replace("http://dbpedia.org/resource/Category:", "").replace("\"","");
			tmp = tmp.replace("http://dbpedia.org/class/yago/", "");
			tmp = tmp.replace("http://dbpedia.org/property/", "");
			String confname = URLEncoder.encode(tmp, "UTF-8")+".conf";
			//
			ConfWriter cf=new ConfWriter();
			cf.addToStats("relearned concept: "+concepts.first());
			
			//System.exit(0);
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
