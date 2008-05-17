package org.dllearner.scripts;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.utilities.AutomaticExampleFinderSKOSSPARQL;
import org.dllearner.utilities.JenaResultSetConvenience;
import org.dllearner.utilities.LearnSparql;
import org.dllearner.utilities.SetManipulation;
import org.dllearner.utilities.SimpleClock;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;

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
	
	static int recursiondepth=1;
	static boolean closeAfterRecursion=true;
	static boolean randomizeCache=false;
	
	static int resultsize=50;
	static double noise=15;
	static int limit=200;
	static double percentage=0.7;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		//logger.setLevel(Level.TRACE);
		Logger.getLogger(KnowledgeSource.class).setLevel(Level.WARN);
		//System.out.println(Logger.getLogger(SparqlQuery.class).getLevel());
		SimpleClock sc=new SimpleClock();
		
		
		se = SparqlEndpoint.EndpointLOCALDBpedia();
//		String t="\"http://dbpedia.org/class/yago/Fiction106367107\"";
//		t="(\"http://dbpedia.org/class/yago/HeadOfState110164747\" AND (\"http://dbpedia.org/class/yago/Negotiator110351874\" AND \"http://dbpedia.org/class/yago/Representative110522035\"))";
//		//System.out.println(t);
//		//t="\"http://www.w3.org/2004/02/skos/core#subject\"";
//		//conceptRewrite(t);
//		//getSubClasses(t);
//		
//		AutomaticExampleFinderSKOSSPARQL ae= new AutomaticExampleFinderSKOSSPARQL( se);	
//			try{
//			System.out.println("oneconcept: "+t);
//			SortedSet<String> instances = ae.queryConceptAsStringSet(conceptRewrite(t), 200);
//			if(instances.size()>=0)System.out.println("size of instances "+instances.size());
//			if(instances.size()>=0 && instances.size()<100) System.out.println("instances"+instances);
//			}catch (Exception e) {
//				e.printStackTrace();
//			}
		SortedSet<String> concepts = new TreeSet<String>();
		
		String prim="http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom";
		
		String award=("http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners");
		
		double acc1=0.0;
		for (int i = 0; i < 5; i++) {
			acc1+=DBpediaSKOS(prim);
		}
		System.out.println("accprim"+(acc1/5));
		
		double acc2=0.0;
		for (int i = 0; i < 5; i++) {
			acc2+=DBpediaSKOS(award);
		}
		System.out.println("accprim"+(acc2/5));
		
//		DBpediaSKOS(concepts.first());
//		DBpediaSKOS(concepts.first());
//		concepts.remove(concepts.first());
//		DBpediaSKOS(concepts.first());
//		DBpediaSKOS(concepts.first());
//		concepts.remove(concepts.first());
//		DBpediaSKOS(concepts.first());
//		DBpediaSKOS(concepts.first());
		//algorithm="refinement";
		//roles();
		
		/*System.out.println(Level.DEBUG.getClass());
			System.out.println(Level.toLevel("INFO"));
			System.out.println(Level.INFO);*/
			//System.exit(0);
			
	
		
			sc.printAndSet("Finished");

	}
	
	

	static double DBpediaSKOS(String concept){
		se = SparqlEndpoint.EndpointLOCALDBpedia();
		//se = SparqlEndpoint.EndpointDBpedia();
		String url = "http://dbpedia.openlinksw.com:8890/sparql";
		url = "http://139.18.2.37:8890/sparql";
		
		//concepts.add("http://dbpedia.org/resource/Category:Grammy_Award_winners");
		//concepts.add("EXISTS \"http://dbpedia.org/property/grammyawards\".TOP");
		
		SortedSet<String> posExamples = new TreeSet<String>();
		SortedSet<String> negExamples = new TreeSet<String>();
		
		//HashMap<String, ResultSet> result = new HashMap<String, ResultSet>();
		//HashMap<String, String> result2 = new HashMap<String, String>();
		//System.out.println(concepts.first());
		//logger.setLevel(Level.TRACE);
		
		
			AutomaticExampleFinderSKOSSPARQL ae= new AutomaticExampleFinderSKOSSPARQL( se);	
			
			ae.initDBpediaSKOS(concept,percentage , useRelated, useParallelClasses);
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
			logger.debug(totalSKOSset);
			logger.debug(rest);
			
			
			LearnSparql ls = new LearnSparql();
	
			//igno.add(oneConcept.replaceAll("\"", ""));
			
			SortedSet<String> conceptresults= ls.learnDBpediaSKOS(posExamples, negExamples, url,new TreeSet<String>(),recursiondepth, closeAfterRecursion,randomizeCache,resultsize,noise);
			
			System.out.println("concepts"+conceptresults);
			//System.exit(0);
			logger.debug("found nr of concepts:"+conceptresults.size());
			SortedSet<ResultCompare> res=new TreeSet<ResultCompare>();
			for (String oneConcept : conceptresults) {
				try{
				System.out.println("oneconcept: "+oneConcept);
				SortedSet<String> instances = ae.queryConceptAsStringSet(conceptRewrite(oneConcept), 200);
				SortedSet<String> coveredInRest = new TreeSet<String>();
				SortedSet<String> possibleNewCandidates = new TreeSet<String>();
				SortedSet<String> notCoveredInTotal = new TreeSet<String>();
				
				int i=0;
				int a=0;
				for (String oneinst : instances) {
					boolean inRest=false;
					boolean inTotal=false;
					for (String onerest : rest) {
						if(onerest.equalsIgnoreCase(oneinst))
							{ i++; inRest=true; break;}
						
					}
					if (inRest){coveredInRest.add(oneinst);};
					
					for (String onetotal : totalSKOSset) {
						if(onetotal.equalsIgnoreCase(oneinst))
						{ a++; inTotal=true; break;}
					}
					if(!inRest && !inTotal){
						possibleNewCandidates.add(oneinst);
					}
				}
				
				for (String onetotal : totalSKOSset) {
					boolean mm=false;
					for (String oneinst : instances) {
						if(onetotal.equalsIgnoreCase(oneinst)){
							mm=true;break;
						}
							
					}
					if(!mm)notCoveredInTotal.add(onetotal);
					
				}
				
				
				
				double accuracy= (double)i/rest.size();
				double accuracy2= (double)a/totalSKOSset.size();
				
				res.add(new ResultCompare(oneConcept,instances,accuracy,accuracy2,instances.size(),
						coveredInRest,possibleNewCandidates,notCoveredInTotal));
				
				//if(instances.size()>=0)System.out.println("size of instances "+instances.size());
				//if(instances.size()>=0 && instances.size()<100) System.out.println("instances"+instances);
				}catch (Exception e) {}
			}
			
//			System.out.println(res.last());
//			res.remove(res.last());
//			System.out.println(res.last());
//			res.remove(res.last());
//			System.out.println(res.last());
//			res.remove(res.last());
//			
			
			//double percent=0.80*(double)res.size();;
			double acc=res.first().accuracy;
			logger.debug(res.first().toStringFull());
			res.remove(res.first());
			logger.debug(res.first().toStringFull());
			res.remove(res.first());
			int i=0;
			while (res.size()>0){
				logger.debug(res.first());
				res.remove(res.first());
				//if(res.size()<=percent)break;
				if(i>50)break;
				i++;
				
			}
			
			return acc;
			
			
			//System.out.println("AAAAAAAA");
			//System.exit(0);
			//"relearned concept: ";
			//cf.writeSPARQL(confname, posExamples, negExamples, url, new TreeSet<String>(),standardSettings,algorithm);
			//
	
		
		//Statistics.print();
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
	
	
	/**
	 * 
	 * @param description
	 */
	public static SortedSet<String> getSubClasses(String description, int limit) {
		if(limit==0)limit=10;
		ResultSet rs = null;
		//System.out.println(description);
		SortedSet<String> alreadyQueried = new TreeSet<String>();
		try {
			String query = getSparqlSubclassQuery(description.replaceAll("\"", ""));
			String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
			rs =SparqlQuery.JSONtoResultSet(JSON);
			LinkedList<String> remainingClasses = new LinkedList<String>();
			
			//make back 
			//remainingClasses.addAll(getSubclassesFromResultSet(rs));
			
			alreadyQueried = new TreeSet<String>();
			alreadyQueried.add(description.replaceAll("\"", ""));
			alreadyQueried.addAll(getSubclassesFromResultSet(rs));
		    //remainingClasses.addAll(alreadyQueried);
			return alreadyQueried;
			
			//SortedSet<String> remainingClasses = new JenaResultSetConvenience(rs).getStringListForVariable("subject");
			
//			while (remainingClasses.size()!=0){
//				SortedSet<String> tmpSet = new TreeSet<String>();
//				String tmp = remainingClasses.removeFirst();
//				//remainingClasses.remove(tmp);
//				query = SparqlQueryDescriptionConvertVisitor
//					.getSparqlSubclassQuery(tmp);
//				alreadyQueried.add(tmp);
//				if(alreadyQueried.size()==limit)break;
//				JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
//				rs =SparqlQuery.JSONtoResultSet(JSON);
//				tmpSet=getSubclassesFromResultSet(rs);
//				for (String string : tmpSet) {
//					if(!alreadyQueried.contains(string))
//						remainingClasses.add(string);
//				}
//			}
			//System.out.println(JSON);
			

		} catch (Exception e) {
			
		}
		System.out.println("subclasses "+alreadyQueried);
		System.out.println("nr of subclasses :"+alreadyQueried.size());
		return alreadyQueried;
	}
	
	public static SortedSet<String> getSubclassesFromResultSet(ResultSet rs)
	{
		SortedSet<String> result = new TreeSet<String>();
		List<ResultBinding> l =  ResultSetFormatter.toList(rs);
		String p="",s="";
		for (ResultBinding resultBinding : l) {
				
			s=((resultBinding.get("subject").toString()));
			p=((resultBinding.get("predicate").toString()));
			if(p.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#subClassOf")){
				result.add(s);
			}
		}
		return result;
	}
	
	public static String getSparqlSubclassQuery(String description)
	{	String ret = "SELECT * \n";
		ret+= "WHERE {\n";
		ret+=" ?subject ?predicate  <"+description+"> \n";
		ret+="}\n";
		
		return ret;
	}
	
	public static String conceptRewrite(String description)
	{	String quote = "\"";
		String ret="";
		String currentconcept="";
		int lastPos=0;
		SortedSet<String> subclasses=new TreeSet<String>();
		
		while ((lastPos=description.lastIndexOf(quote))!=-1){
			ret=description.substring(lastPos+1,description.length())+ret;
			description=description.substring(0,lastPos);
			//System.out.println(description);
			lastPos=description.lastIndexOf(quote);
			currentconcept=description.substring(lastPos+1,description.length());
			description=description.substring(0,lastPos);
			//replace
			//currentconcept="\"blabla\"";
			//System.out.println(currentconcept);
			
			
				subclasses = getSubClasses( currentconcept, 0);
			
			if (subclasses.size()==1)currentconcept="\""+currentconcept+"\"";
			else {
				LinkedList<Description> nc = new LinkedList<Description>();
				for (String one : subclasses) {
					nc.add(new NamedClass(one));
				}
				currentconcept=new Union(nc).toKBSyntaxString();
			}
			
			ret=currentconcept+ret;
			//ret+=description;
		}
		ret=description+ret;
			//System.out.println(ret);
		return ret;
	}
	
	

}
