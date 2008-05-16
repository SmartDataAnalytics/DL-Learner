package org.dllearner.scripts;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.utilities.AutomaticExampleFinderSKOSSPARQL;
import org.dllearner.utilities.JenaResultSetConvenience;
import org.dllearner.utilities.LearnSparql;
import org.dllearner.utilities.SetManipulation;
import org.dllearner.utilities.SimpleClock;

import com.hp.hpl.jena.query.ResultSet;

public class SKOS7030 {

	static Cache c;
	static SparqlEndpoint se;
	private static Logger logger = Logger.getRootLogger();
	
	static String standardSettings="";
	static String algorithm="refexamples";
	static String standardSettingsRefexamples = 
		"refexamples.minExecutionTimeInSeconds = 30;\n" +
		"refexamples.maxExecutionTimeInSeconds = 30;\n" +
		"//refexamples.guaranteeXgoodDescriptions = 10;\n" +
		"refexamples.logLevel=\"TRACE\";\n" +
		"refexamples.noisePercentage = 0.10;\n" +
		"refexamples.writeSearchTree = false;\n" +
		"refexamples.searchTreeFile = \"searchTree.txt\";\n" +
		"refexamples.replaceSearchTree = true;\n\n" ;
	
	static String standardSettingsRefinement = 
		"refinement.minExecutionTimeInSeconds = 30;\n" +
		"refinement.maxExecutionTimeInSeconds = 30;\n" +
		"//refinement.guaranteeXgoodDescriptions = 10;\n" +
		"refinement.logLevel=\"TRACE\";\n" +
		"refinement.writeSearchTree = false;\n" +
		"refinement.searchTreeFile = \"searchTree.txt\";\n" +
		"refinement.replaceSearchTree = true;\n\n" ;
	
	
	
	static String standardDBpedia="" +
			"sparql.recursionDepth = 1;\n" +
			"sparql.predefinedFilter = \"YAGO\";\n" + 
			"sparql.predefinedEndpoint = \"DBPEDIA\";\n";
			//"sparql.logLevel = \"INFO\";\n";
	
	
	//vars
	static boolean useRelated = false;
	static boolean useSuperClasses = false;
	static boolean useParallelClasses = true;
	static int poslimit = 10;
	static int neglimit = 20;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		//logger.setLevel(Level.TRACE);
		Logger.getLogger(KnowledgeSource.class).setLevel(Level.INFO);
		//System.out.println(Logger.getLogger(SparqlQuery.class).getLevel());
		SimpleClock sc=new SimpleClock();
		
		standardSettings=standardSettingsRefexamples+standardDBpedia;
		//standardSettings=standardSettingsRefinement+standardDBpedia;
		
		DBpediaSKOS();
		//algorithm="refinement";
		//roles();
		
		/*System.out.println(Level.DEBUG.getClass());
			System.out.println(Level.toLevel("INFO"));
			System.out.println(Level.INFO);*/
			//System.exit(0);
			
	
		
			sc.printAndSet("Finished");

	}
	
	

	static void DBpediaSKOS(){
		se = SparqlEndpoint.EndpointLOCALDBpedia();
		se = SparqlEndpoint.EndpointDBpedia();
		String url = "http://dbpedia.openlinksw.com:8890/sparql";
		//url = "http://139.18.2.37:8890/sparql";
		
		SortedSet<String> concepts = new TreeSet<String>();
		
		concepts.add("http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom");
		//concepts.add("http://dbpedia.org/resource/Category:Grammy_Award_winners");
		//concepts.add("EXISTS \"http://dbpedia.org/property/grammyawards\".TOP");
		
		SortedSet<String> posExamples = new TreeSet<String>();
		SortedSet<String> negExamples = new TreeSet<String>();
		
		//HashMap<String, ResultSet> result = new HashMap<String, ResultSet>();
		//HashMap<String, String> result2 = new HashMap<String, String>();
		//System.out.println(concepts.first());
		//logger.setLevel(Level.TRACE);
		String concept=concepts.first();
		
			AutomaticExampleFinderSKOSSPARQL ae= new AutomaticExampleFinderSKOSSPARQL( se);	
			useRelated = false;
			useParallelClasses = true;
			int recursiondepth=1;
			boolean closeAfterRecursion=true;
			boolean randomizeCache=false;
			ae.initDBpediaSKOS(concept, 0.1, useRelated, useParallelClasses);
			posExamples = ae.getPosExamples();
			negExamples = ae.getNegExamples();
			
			for (String string2 : negExamples) {
				logger.debug("-"+string2);
			}
			
			for (String string2 : posExamples) {
				logger.debug("+"+string2);
			}
			SortedSet<String> totalSKOSset= ae.totalSKOSset;
			SortedSet<String> rest= ae.rest;
		
			
			
			LearnSparql ls = new LearnSparql();
	
			//igno.add(oneConcept.replaceAll("\"", ""));
			
			SortedSet<String> conceptresults= ls.learnDBpediaSKOS(posExamples, negExamples, url,new TreeSet<String>(),recursiondepth, closeAfterRecursion,randomizeCache);
			System.out.println(conceptresults);
			for (String string : conceptresults) {
				SortedSet<String> instances = ae.queryConceptAsStringSet(string, 0);
				if(instances.size()>0)System.out.println("size "+instances.size());
				if(instances.size()<=15 && instances.size()>0) System.out.println(instances);
			}
			
			
			
			//System.out.println("AAAAAAAA");
			//System.exit(0);
			//"relearned concept: ";
			//cf.writeSPARQL(confname, posExamples, negExamples, url, new TreeSet<String>(),standardSettings,algorithm);
			//
	
		
		//Statistics.print();
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
		
		SimpleLayout layout = new SimpleLayout();
		// create logger (a simple logger which outputs
		// its messages to the console)
		FileAppender fileAppender =null; ;
		try{
			fileAppender = new FileAppender(layout,"the_log.txt",false);
		}catch (Exception e) {e.printStackTrace();}

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);
		c = new Cache("cachetemp");
		

	}
	
	public static SortedSet<String> selectDBpediaConcepts(int number){
		String query = "SELECT DISTINCT ?concept WHERE { \n" + 
		"[] a ?concept .FILTER (regex(str(?concept),'yago'))" +
		" \n}  \n"; //LIMIT "+number+"
		

		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		return SetManipulation.fuzzyShrink(rsc.getStringListForVariable("concept"),number);
	}
	
	

}
