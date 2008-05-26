package org.dllearner.scripts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;

public class SKOS7030 {

	private static SPARQLTasks sparqlTasks;
	
	private static Logger logger = Logger.getRootLogger();
    static boolean local = true;
    static String url = "";
	
	//LEARNING
	static int recursiondepth=1;
	static boolean closeAfterRecursion=true;
	static boolean randomizeCache=false;
	static double noise=15;
	static int maxExecutionTimeInSeconds = 30;
	static int guaranteeXgoodDescriptions = 40;
	
	//static int limit=200;
	
	
	
	
	
	//examples
	static int sparqlResultSize=2000;
	static double percentOfSKOSSet=0.2;
	static double negfactor=1.0;
	SortedSet<String> posExamples = new TreeSet<String>();
	SortedSet<String> fullPositiveSet = new TreeSet<String>();
	SortedSet<String> fullminusposRest = new TreeSet<String>();
	SortedSet<String> negExamples = new TreeSet<String>();
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start");
		
		initLogger();
		//parameters
		
		
		if(local){
			url = "http://139.18.2.37:8890/sparql";
			//RBC
			sparqlTasks = new SPARQLTasks(Cache.getPersistentCache(),SparqlEndpoint.EndpointLOCALDBpedia());
		}else{
			url = "http://dbpedia.openlinksw.com:8890/sparql";
			sparqlTasks = new SPARQLTasks(Cache.getPersistentCache(),SparqlEndpoint.EndpointDBpedia());
		}
		
		String prim="http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom";
		
		String award=("http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners");
		
		SKOS7030 s= new SKOS7030();
		
		s.makeExamples(prim, percentOfSKOSSet, negfactor, sparqlResultSize);
		//QUALITY s.posExamples
		List<Description> conceptresults = s.learn(s.posExamples, s.negExamples);
		logger.debug("found nr of concepts: "+conceptresults.size());
		
		for (Description oneConcept : conceptresults) {
			
			//s.evaluate(oneConcept, 1000);
			
		}
		
		
		System.out.println("Finished");
		JamonMonitorLogger.printAllSortedByLabel();
		
	}	
	
	void evaluate(Description oneConcept, int sparqlResultLimit){
		logger.debug("oneconcept: "+oneConcept);
		SortedSet<String> instances = sparqlTasks.retrieveInstancesForConcept(oneConcept.toKBSyntaxString(), sparqlResultLimit);
		
		System.out.println(fullminusposRest.size());
		System.out.println(instances.size());
		
		SortedSet<String> coveredInRest = new TreeSet<String>(fullminusposRest);
		coveredInRest.retainAll(instances);
		
		System.out.println(fullminusposRest.size());
		System.out.println(instances.size());
		System.out.println(coveredInRest.size());
		
		
		
		//SortedSet<String> possibleNewCandidates = new TreeSet<String>();
		//SortedSet<String> notCoveredInTotal = new TreeSet<String>();
		
		
	}
	
	
	static void DBpediaSKOS(String SKOSConcept){
		
		
		//concepts.add("http://dbpedia.org/resource/Category:Grammy_Award_winners");
		//concepts.add("EXISTS \"http://dbpedia.org/property/grammyawards\".TOP");
		
		
		
		//HashMap<String, ResultSet> result = new HashMap<String, ResultSet>();
		//HashMap<String, String> result2 = new HashMap<String, String>();
		//System.out.println(concepts.first());
		//logger.setLevel(Level.TRACE);
		
		
		
			
			
//			LearnSparql ls = new LearnSparql();
//	
//			//igno.add(oneConcept.replaceAll("\"", ""));
//			
//			List<Description> conceptresults= ls.learnDBpediaSKOS(posExamples, negExamples, url,new TreeSet<String>(),recursiondepth, closeAfterRecursion,randomizeCache,resultsize,noise);
//			
//			System.out.println("concepts"+conceptresults);
//			//System.exit(0);
//			
//			SortedSet<ResultCompare> res=new TreeSet<ResultCompare>();
//			for (Description oneConcept : conceptresults) {
//				try{
//				
//				
//				int i=0;
//				int a=0;
//				for (String oneinst : instances) {
//					boolean inRest=false;
//					boolean inTotal=false;
//					for (String onerest : rest) {
//						if(onerest.equalsIgnoreCase(oneinst))
//							{ i++; inRest=true; break;}
//						
//					}
//					if (inRest){coveredInRest.add(oneinst);};
//					
//					for (String onetotal : totalSKOSset) {
//						if(onetotal.equalsIgnoreCase(oneinst))
//						{ a++; inTotal=true; break;}
//					}
//					if(!inRest && !inTotal){
//						possibleNewCandidates.add(oneinst);
//					}
//				}
//				
//				for (String onetotal : totalSKOSset) {
//					boolean mm=false;
//					for (String oneinst : instances) {
//						if(onetotal.equalsIgnoreCase(oneinst)){
//							mm=true;break;
//						}
//							
//					}
//					if(!mm)notCoveredInTotal.add(onetotal);
//					
//				}
//				
//				
//				
//				double accuracy= (double)i/rest.size();
//				double accuracy2= (double)a/totalSKOSset.size();
//				
//				logger.debug((new ResultCompare(oneConcept.toKBSyntaxString(),instances,accuracy,accuracy2,instances.size(),
//						coveredInRest,possibleNewCandidates,notCoveredInTotal)).toStringFull());
//				
//				//if(instances.size()>=0)System.out.println("size of instances "+instances.size());
//				//if(instances.size()>=0 && instances.size()<100) System.out.println("instances"+instances);
//				}catch (Exception e) {e.printStackTrace();}
//			}
			
//			System.out.println(res.last());
//			res.remove(res.last());
//			System.out.println(res.last());
//			res.remove(res.last());
//			System.out.println(res.last());
//			res.remove(res.last());
//			
			
			//double percent=0.80*(double)res.size();;
//			double acc=res.first().accuracy;
//			logger.debug(res.first().toStringFull());
//			res.remove(res.first());
//			logger.debug(res.first().toStringFull());
//			res.remove(res.first());
//			int i=0;
//			while (res.size()>0){
//				logger.debug(res.first());
//				res.remove(res.first());
//				//if(res.size()<=percent)break;
//				if(i>50)break;
//				i++;
//				
//			}
//			
//			return 0.0;
			
			
			//System.out.println("AAAAAAAA");
			//System.exit(0);
			//"relearned concept: ";
			//cf.writeSPARQL(confname, posExamples, negExamples, url, new TreeSet<String>(),standardSettings,algorithm);
			//
	
		
		//Statistics.print();
	}
	

	

	

	public static void initLogger() {
		
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
		Logger.getLogger(KnowledgeSource.class).setLevel(Level.WARN);
	
	}
	
	/*public static SortedSet<String> selectDBpediaConcepts(int number){
		String query = "SELECT DISTINCT ?concept WHERE { \n" + 
		"[] a ?concept .FILTER (regex(str(?concept),'yago'))" +
		" \n}  \n"; //LIMIT "+number+"
		

		String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
		ResultSet rs =SparqlQuery.JSONtoResultSet(JSON);
		JenaResultSetConvenience rsc = new JenaResultSetConvenience(rs);
		return SetManipulation.fuzzyShrink(rsc.getStringListForVariable("concept"),number);
	}*/
	
	public void makeExamples(String SKOSConcept, double percentOfSKOSSet , double negfactor, int sparqlResultSize){
		
		//POSITIVES
		AutomaticPositiveExampleFinderSPARQL apos = new AutomaticPositiveExampleFinderSPARQL(sparqlTasks);
		apos.makePositiveExamplesFromSKOSConcept(SKOSConcept);
		fullPositiveSet =  apos.getPosExamples();
		
		//System.exit(0);
		
		int poslimit=(int)Math.round(percentOfSKOSSet*fullPositiveSet.size());
		int neglimit=(int)Math.round(poslimit*negfactor);
		
		this.posExamples =  SetManipulation.fuzzyShrink(fullPositiveSet,poslimit);
		
		
		
		//NEGATIVES
		
		AutomaticNegativeExampleFinderSPARQL aneg = new AutomaticNegativeExampleFinderSPARQL(fullPositiveSet,sparqlTasks);
		
		aneg.makeNegativeExamplesFromParallelClasses(posExamples, sparqlResultSize);
		SortedSet<String> negativeSet =  aneg.getNegativeExamples(neglimit);
		
		logger.debug("POSITIVE EXAMPLES");
		for (String pos : posExamples) {
			logger.debug("+"+pos);
		}
	
		logger.debug("NEGATIVE EXAMPLES");
		for (String negs : negativeSet) {
			logger.debug("-"+negs);
		}
	
		
		
		fullminusposRest = fullPositiveSet;
		fullminusposRest.removeAll(posExamples);
		
		
		logger.debug(fullPositiveSet);
		logger.debug(fullminusposRest);
	}
	
	public List<Description> learn(SortedSet<String> posExamples, SortedSet<String> negExamples){
		
		SortedSet<String> instances = new TreeSet<String>();
		instances.addAll(posExamples);
		instances.addAll(negExamples);
		
		
	
		ComponentManager cm = ComponentManager.getInstance();
		LearningAlgorithm la = null;
		ReasoningService rs = null;
		LearningProblem lp = null; 
		SparqlKnowledgeSource ks =null;
		try {
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
		ReasonerComponent r = new FastInstanceChecker(sources);
		rs = new ReasoningService(r); 
		//System.out.println("satisfy: "+rs.isSatisfiable());
		lp = new PosNegDefinitionLP(rs);	
		((PosNegLP) lp).setPositiveExamples(SetManipulation.stringToInd(posExamples));
		((PosNegLP) lp).setNegativeExamples(SetManipulation.stringToInd(negExamples));
		
		la = cm.learningAlgorithm(ExampleBasedROLComponent.class, lp, rs);

		logger.debug("start learning");
		
		
		//KNOWLEDGESOURCE
		cm.applyConfigEntry(ks, "instances",instances);
		cm.applyConfigEntry(ks, "url",url);
		cm.applyConfigEntry(ks, "recursionDepth",recursiondepth);
		cm.applyConfigEntry(ks, "closeAfterRecursion",closeAfterRecursion);
		cm.applyConfigEntry(ks, "predefinedFilter","YAGO");
		if(local)
			cm.applyConfigEntry(ks, "predefinedEndpoint","LOCALDBPEDIA");
		else {
			cm.applyConfigEntry(ks, "predefinedEndpoint","DBPEDIA");
		}
		if(randomizeCache)
			cm.applyConfigEntry(ks, "cacheDir","cache/"+System.currentTimeMillis()+"");
		else {cm.applyConfigEntry(ks, "cacheDir",Cache.getDefaultCacheDir());}
		
		//LEARNINGALGORITHM
		cm.applyConfigEntry(la,"useAllConstructor",false);
		cm.applyConfigEntry(la,"useExistsConstructor",true);
		cm.applyConfigEntry(la,"useCardinalityRestrictions",false);
		cm.applyConfigEntry(la,"useNegation",false);
		cm.applyConfigEntry(la,"minExecutionTimeInSeconds",0);
		cm.applyConfigEntry(la,"maxExecutionTimeInSeconds",maxExecutionTimeInSeconds);
		cm.applyConfigEntry(la,"guaranteeXgoodDescriptions",guaranteeXgoodDescriptions);
		cm.applyConfigEntry(la,"writeSearchTree",false);
		cm.applyConfigEntry(la,"searchTreeFile","log/SKOS.txt");
		cm.applyConfigEntry(la,"replaceSearchTree",true);
		cm.applyConfigEntry(la,"noisePercentage",noise);
		//cm.applyConfigEntry(la,"guaranteeXgoodDescriptions",999999);
		cm.applyConfigEntry(la,"logLevel","TRACE");
		/*if(ignoredConcepts.size()>0)
			cm.applyConfigEntry(la,"ignoredConcepts",ignoredConcepts);
		*/
		
		ks.init();
		sources.add(ks);
		r.init();
		lp.init();
		la.init();	
		
		
		la.start();
		//Statistics.addTimeCollecting(sc.getTime());
		//Statistics.addTimeLearning(sc.getTime());
		
		
		return la.getGoodSolutions();
		
		}catch (Exception e) {e.printStackTrace();}
		return null;
		
	}

	
//	String t="\"http://dbpedia.org/class/yago/Fiction106367107\"";
//	t="(\"http://dbpedia.org/class/yago/HeadOfState110164747\" AND (\"http://dbpedia.org/class/yago/Negotiator110351874\" AND \"http://dbpedia.org/class/yago/Representative110522035\"))";
//	//System.out.println(t);
//	//t="\"http://www.w3.org/2004/02/skos/core#subject\"";
//	//conceptRewrite(t);
//	//getSubClasses(t);
//	
//	AutomaticExampleFinderSKOSSPARQL ae= new AutomaticExampleFinderSKOSSPARQL( se);	
//		try{
//		System.out.println("oneconcept: "+t);
//		SortedSet<String> instances = ae.queryConceptAsStringSet(conceptRewrite(t), 200);
//		if(instances.size()>=0)System.out.println("size of instances "+instances.size());
//		if(instances.size()>=0 && instances.size()<100) System.out.println("instances"+instances);
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
	//SortedSet<String> concepts = new TreeSet<String>();
	
	
	
	//System.out.println(DBpediaSKOS(prim));
//	double acc1=0.0;
//	for (int i = 0; i < 5; i++) {
//		acc1+=DBpediaSKOS(prim);
//	}
//	System.out.println("accprim"+(acc1/5));
//	
//	double acc2=0.0;
//	for (int i = 0; i < 5; i++) {
//		acc2+=DBpediaSKOS(award);
//	}
//	System.out.println("accprim"+(acc2/5));
	
//	DBpediaSKOS(concepts.first());
//	DBpediaSKOS(concepts.first());
//	concepts.remove(concepts.first());
//	DBpediaSKOS(concepts.first());
//	DBpediaSKOS(concepts.first());
//	concepts.remove(concepts.first());
//	DBpediaSKOS(concepts.first());
//	DBpediaSKOS(concepts.first());
	//algorithm="refinement";
	//roles();
	
	/*System.out.println(Level.DEBUG.getClass());
		System.out.println(Level.toLevel("INFO"));
		System.out.println(Level.INFO);*/
		//System.exit(0);
		

	
	
	
}
