package org.dllearner.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.dllearner.utilities.AutomaticExampleFinderSPARQL;
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
	static boolean useRelated = false;
	static boolean useSuperClasses = true;
	static boolean useParallelClasses = true;
	static int poslimit = 0;
	static int neglimit = 0;
	static boolean randomizeCache = true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		System.out.println("Start");
		//logger.setLevel(Level.TRACE);
		logger.setLevel(Level.WARN);
		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(Level.WARN);
		//System.out.println(Logger.getLogger(SparqlQuery.class).getLevel());
		SimpleClock sc=new SimpleClock();
		LocalDBpediaEvaluation();
	
		sc.printAndSet("Finished");

	}
	
	
	static void LocalDBpediaEvaluation(){
		boolean local=true;
		String  url="";
		if(local){
			se = SparqlEndpoint.EndpointLOCALDBpedia();
			
			 url = "http://139.18.2.37:8890/sparql";
			
		}else{
			se = SparqlEndpoint.EndpointDBpedia();
			 url= "http://dbpedia.openlinksw.com:8890/sparql";
		}
		
		
		SortedSet<String> concepts = new TreeSet<String>();
		SortedSet<String> tmpSet = new TreeSet<String>();
		//selectDBpediaConcepts(number);
		tmpSet=initConcepts();
		int number=tmpSet.size();
		//concepts.add("\"http://dbpedia.org/class/yago/Flamethrower103356559\"");
		for (String string : tmpSet) {
			//System.out.println("\""+string+"\",");
			concepts.add("\""+string+"\"");
		}
		
		
		
		SortedSet<String> posExamples = new TreeSet<String>();
		SortedSet<String> negExamples = new TreeSet<String>();
		
		for (int a = 0; a < 3; a++) {
			
			poslimit+=5;
			neglimit+=5;
		
		for (String oneConcept : concepts) {
			int recursiondepth=0;
			boolean closeAfterRecursion=true;
			
			System.out.println(oneConcept);
			AutomaticExampleFinderSPARQL ae= new AutomaticExampleFinderSPARQL( se);	
			
			ae.initDBpedia(oneConcept, useRelated, useSuperClasses,useParallelClasses, poslimit, neglimit);
			
			posExamples = ae.getPosExamples();
			negExamples = ae.getNegExamples();
		
			for(int i=0;i<3;i++) {
				/*if(i==0){;}
				else if(closeAfterRecursion) {
					closeAfterRecursion=false;
					recursiondepth++;
				}
				else {
					closeAfterRecursion=true;
				}*/
				//Statistics.setCurrentLabel(recursiondepth+""+((closeAfterRecursion)?"+":""));
				
				Statistics.setCurrentLabel(recursiondepth+""+((closeAfterRecursion)?"":""));
				
				Statistics.print(number);
				
				System.out.println("currently at label "+Statistics.getCurrentLabel()+"||i: "+i);
				
				LearnSparql ls = new LearnSparql();
				TreeSet<String> igno = new TreeSet<String>();
				System.out.println(oneConcept);
				
				ls.learnDBpedia(posExamples, negExamples, url,igno,recursiondepth, closeAfterRecursion,randomizeCache);
					
			
			}
		}
		Statistics.print(number);
		String pre="log/gnu_";
		String comment1="# "+poslimit+neglimit+"examples\n";
		String f1=pre+"1avgtrip_"+poslimit+neglimit+"example"+concepts.size()+"classes";
		writeToFile(f1, comment1+Statistics.getAVGTriplesForRecursionDepth(number));
		String comment2="# "+poslimit+neglimit+"examples\n";
		String f2=pre+"2avgTimeExtraction_"+poslimit+neglimit+"example"+concepts.size()+"classes";
		writeToFile(f2, comment2+Statistics.getAVGTimeCollecting(number));
		String comment3="# "+poslimit+neglimit+"examples\n";
		String f3=pre+"2avgTimeLearning_"+poslimit+neglimit+"example"+concepts.size()+"classes";
		writeToFile(f3, comment3+Statistics.getAVGTimeLearning(number));
		
		
		}//outer
	}
	


	public static void init() {
		
		SimpleLayout layout = new SimpleLayout();
		// create logger (a simple logger which outputs
		// its messages to the console)
		FileAppender fileAppender =null; ;
		try{
			fileAppender = new FileAppender(layout,"log/sparqleval.txt",false);
		}catch (Exception e) {e.printStackTrace();}

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		
		c = new Cache("cachetemp");
		

	}
	
	public static SortedSet<String> selectDBpediaConcepts(int number){
		String query = "SELECT DISTINCT ?concept WHERE { \n" + 
		"[] a ?concept .FILTER (regex(str(?concept),'yago'))" +
		" \n} LIMIT "+4000+" \n "; //

		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		return SetManipulation.fuzzyShrink(rsc.getStringListForVariable("concept"),number);
	}
	
	public static  SortedSet<String> initConcepts(){
		SortedSet<String> concepts = new TreeSet<String>();
		concepts.add("http://dbpedia.org/class/yago/AirLane108492546");
		concepts.add("http://dbpedia.org/class/yago/AlphaBlocker102698769");
		concepts.add("http://dbpedia.org/class/yago/Articulation107131854");
		concepts.add("http://dbpedia.org/class/yago/Caliphate108550766");
		concepts.add("http://dbpedia.org/class/yago/Ceremony107450842");
		concepts.add("http://dbpedia.org/class/yago/CookingOil107673145");
		concepts.add("http://dbpedia.org/class/yago/Corticosteroid114751417");
		concepts.add("http://dbpedia.org/class/yago/Curlew102033561");
		concepts.add("http://dbpedia.org/class/yago/DataStructure105728493");
		concepts.add("http://dbpedia.org/class/yago/Disappearance100053609");
		concepts.add("http://dbpedia.org/class/yago/Flintstone114871268");
		concepts.add("http://dbpedia.org/class/yago/Form105930736");
		concepts.add("http://dbpedia.org/class/yago/Hypochondriac110195487");
		concepts.add("http://dbpedia.org/class/yago/Industrialist110204177");
		concepts.add("http://dbpedia.org/class/yago/Lifeboat103662601");
		concepts.add("http://dbpedia.org/class/yago/Particulate114839439");
		concepts.add("http://dbpedia.org/class/yago/Reservation108587174");
		concepts.add("http://dbpedia.org/class/yago/Schoolteacher110560352");
		concepts.add("http://dbpedia.org/class/yago/Singer110599806");
		concepts.add("http://dbpedia.org/class/yago/SupremeCourt108336188");
		concepts.add("http://dbpedia.org/class/yago/AirLane108492546");
		return concepts;
	}
	
	protected static void writeToFile(String filename, String content) {
		// create the file we want to use
		File file = new File( filename);

		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(filename, false);
			// ObjectOutputStream o = new ObjectOutputStream(fos);
			fos.write(content.getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
