package org.dllearner.scripts;

import java.net.URLEncoder;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.utilities.AutomaticExampleFinderRolesSPARQL;
import org.dllearner.utilities.AutomaticExampleFinderSPARQL;
import org.dllearner.utilities.ConfWriter;
import org.dllearner.utilities.JenaResultSetConvenience;
import org.dllearner.utilities.LearnSparql;
import org.dllearner.utilities.SetManipulation;
import org.dllearner.utilities.SimpleClock;
import org.dllearner.utilities.Statistics;

import com.hp.hpl.jena.query.ResultSet;

public class SPARQLExtractionEvaluation {

	static Cache c;
	static SparqlEndpoint se;
	private static Logger logger = Logger.getRootLogger();
	
	//static String standardSettings="";
	//static String algorithm="refexamples";
	
	//vars
	static boolean useRelated = true;
	static boolean useSuperClasses = true;
	static boolean useParallelClasses = true;
	static int poslimit = 10;
	static int neglimit = 10;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		//logger.setLevel(Level.TRACE);
		logger.setLevel(Level.WARN);
		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(Level.WARN);
		//System.out.println(Logger.getLogger(SparqlQuery.class).getLevel());
		SimpleClock sc=new SimpleClock();
		LocalDBpediaEvaluation();
	
		sc.printAndSet("Finished");

	}
	
	
	static void LocalDBpediaEvaluation(){
		se = SparqlEndpoint.EndpointLOCALDBpedia();
		int number=1;
		SortedSet<String> concepts = new TreeSet<String>();
		SortedSet<String> tmpSet=selectDBpediaConcepts(number);
		
		for (String string : tmpSet) {
			concepts.add("\""+string+"\"");
		}
		
		SortedSet<String> posExamples = new TreeSet<String>();
		SortedSet<String> negExamples = new TreeSet<String>();
		
		String  url = "http://139.18.2.37:8890/sparql";
		int recursiondepth=0;
		boolean closeAfterRecursion=false;
		
		for(int i=0;i<8;i++) {
			if(i==0){;}
			else if(closeAfterRecursion) {
				closeAfterRecursion=false;
				recursiondepth++;
			}
			else {
				closeAfterRecursion=true;
			}
			Statistics.setCurrentLabel(recursiondepth+""+((closeAfterRecursion)?"+":""));
			
			
			for (String oneConcept : concepts) {
				AutomaticExampleFinderSPARQL ae= new AutomaticExampleFinderSPARQL( se);	
				
				ae.initDBpedia(oneConcept, useRelated, useSuperClasses,useParallelClasses, poslimit, neglimit);
				posExamples = ae.getPosExamples();
				negExamples = ae.getNegExamples();
			
				
				LearnSparql ls = new LearnSparql();
				TreeSet<String> igno = new TreeSet<String>();
				System.out.println(oneConcept);
			
				ls.learnDBpedia(posExamples, negExamples, url,igno,recursiondepth, closeAfterRecursion);
				
		
			}
		}
		Statistics.print(number);
	}
	


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
		
		c = new Cache();
		

	}
	
	public static SortedSet<String> selectDBpediaConcepts(int number){
		String query = "SELECT DISTINCT ?concept WHERE { \n" + 
		"[] a ?concept .FILTER (regex(str(?concept),'yago'))" +
		" \n} LIMIT "+1000+" \n"; //

		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		return SetManipulation.fuzzyShrink(rsc.getStringListForVariable("concept"),number);
	}
	
	

}
