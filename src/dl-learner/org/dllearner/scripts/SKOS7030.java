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
	
	
	//examples
	static int sparqlResultSize=2000;
	static double percentOfSKOSSet=0.2;
	static double negfactor=1.0;
	SortedSet<String> posExamples = new TreeSet<String>();
	SortedSet<String> fullPositiveSet = new TreeSet<String>();
	SortedSet<String> fullPosSetWithoutPosExamples = new TreeSet<String>();
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
			sparqlTasks = new SPARQLTasks(Cache.getPersistentCache(),SparqlEndpoint.EndpointLOCALDBpedia());
		}else{
			url = "http://dbpedia.openlinksw.com:8890/sparql";
			sparqlTasks = new SPARQLTasks(Cache.getPersistentCache(),SparqlEndpoint.EndpointDBpedia());
		}
		
		System.out.println(sparqlTasks.getDomain("http://dbpedia.org/property/predecessor", 1000));
		
		String prim="http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom";
		
		String award=("http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners");
		
		SKOS7030 s= new SKOS7030();
		
		s.makeExamples(prim, percentOfSKOSSet, negfactor, sparqlResultSize);
		
		SortedSet<Description> conceptresults = s.learn();
		logger.debug("found nr of concepts: "+conceptresults.size());
		System.out.println(conceptresults);
		
		int x=0;
	
		SortedSet<ResultMostCoveredInRest> res = new TreeSet<ResultMostCoveredInRest>();
		for (Description concept : conceptresults) {
			if(x++==100)break;
			res.add(s.evaluate(concept, 1000));
			
		}
		
		x=0;
		for (ResultMostCoveredInRest resultMostCoveredInRest : res) {
			if(x++==10)break;
			System.out.println(resultMostCoveredInRest.concept);
			System.out.println(resultMostCoveredInRest.accuracy);
			System.out.println(resultMostCoveredInRest.retrievedInstancesSize);
			
			
		}
		
		s.print(res.first().concept, 1000);
		
		System.out.println("Finished");
		JamonMonitorLogger.printAllSortedByLabel();
		
	}	
	
	 void  print(Description concept, int sparqlResultLimit){
		logger.debug("evaluating concept: "+concept);
//		SortedSet<String> instances = sparqlTasks.retrieveInstancesForConcept(oneConcept.toKBSyntaxString(), sparqlResultLimit);
		SortedSet<String> instances = 
			sparqlTasks.retrieveInstancesForConceptIncludingSubclasses(
					concept.toKBSyntaxString(),sparqlResultLimit);
		
		SortedSet<String> coveredInRest = new TreeSet<String>(fullPosSetWithoutPosExamples);
		coveredInRest.retainAll(instances);
	
		
		SortedSet<String> coveredTotal = new TreeSet<String>(fullPositiveSet);
		coveredTotal.retainAll(instances);
		
		
		SortedSet<String> notCoveredInRest = new TreeSet<String>(fullPosSetWithoutPosExamples);
		notCoveredInRest.retainAll(coveredInRest);
		System.out.println(notCoveredInRest);
		
		SortedSet<String> notCoveredTotal = new TreeSet<String>(fullPositiveSet);
		notCoveredTotal.retainAll(coveredTotal);
		System.out.println(notCoveredTotal);
		
	}
	
	ResultMostCoveredInRest evaluate(Description concept, int sparqlResultLimit){
		logger.debug("evaluating concept: "+concept);
//		SortedSet<String> instances = sparqlTasks.retrieveInstancesForConcept(oneConcept.toKBSyntaxString(), sparqlResultLimit);
		SortedSet<String> instances = 
			sparqlTasks.retrieveInstancesForConceptIncludingSubclasses(
					concept.toKBSyntaxString(),sparqlResultLimit);
		
		SortedSet<String> coveredInRest = new TreeSet<String>(fullPosSetWithoutPosExamples);
		coveredInRest.retainAll(instances);
		
		SortedSet<String> coveredTotal = new TreeSet<String>(fullPositiveSet);
		coveredTotal.retainAll(instances);
		
		
		SortedSet<String> notCoveredInRest = new TreeSet<String>(fullPosSetWithoutPosExamples);
		notCoveredInRest.retainAll(coveredInRest);
		
		SortedSet<String> notCoveredTotal = new TreeSet<String>(fullPositiveSet);
		notCoveredTotal.retainAll(coveredTotal);
		double acc = (double) (coveredInRest.size() / fullPosSetWithoutPosExamples.size());
		System.out.println("Accuracy: "+acc);
		return new ResultMostCoveredInRest(concept,acc,instances.size());
		
		
		
		
		
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
		this.negExamples =  aneg.getNegativeExamples(neglimit);
		
		logger.debug("POSITIVE EXAMPLES");
		for (String pos : posExamples) {
			logger.debug("+"+pos);
		}
	
		logger.debug("NEGATIVE EXAMPLES");
		for (String negs : this.negExamples) {
			logger.debug("-"+negs);
		}
	
		
		
		fullPosSetWithoutPosExamples = fullPositiveSet;
		fullPosSetWithoutPosExamples.removeAll(posExamples);
		
		
		logger.debug(fullPositiveSet);
		logger.debug(fullPosSetWithoutPosExamples);
	}
	
	public SortedSet<Description> learn(){
		
		SortedSet<String> instances = new TreeSet<String>();
		instances.addAll(this.posExamples);
		instances.addAll(this.negExamples);
		
		
		logger.info("Start Learning with");
		logger.info("positive examples: \t"+posExamples.size());
		logger.info("negative examples: \t"+negExamples.size());
		logger.info("instances \t"+instances.size());
		
		
		
		
	
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
		((PosNegLP) lp).setPositiveExamples(SetManipulation.stringToInd(this.posExamples));
		((PosNegLP) lp).setNegativeExamples(SetManipulation.stringToInd(this.negExamples));
		
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
		
		
		return la.getCurrentlyBestDescriptions();
		
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
		
	private class ResultCompare implements Comparable<ResultCompare>{
		Description concept ;
		double accuracy = 0.0;
		int retrievedInstancesSize=0;
		
		public int compareTo(ResultCompare o2) {
			return 0;
		}
		public boolean equals(ResultCompare o2){
			return this.concept.equals(o2.concept);
		}
		
		
		public ResultCompare(Description conceptKBSyntax, double accuracy, int retrievedInstancesSize) {
			super();
			this.concept = conceptKBSyntax;
			this.accuracy = accuracy;
			this.retrievedInstancesSize = retrievedInstancesSize;
		}
		
		
	}
	
	private class ResultMostCoveredInRest extends ResultCompare{
		
		public ResultMostCoveredInRest(Description concept, double accuracy,
				int retrievedInstancesSize) {
			super(concept, accuracy, retrievedInstancesSize);
			
		}

		public int compareTo(ResultMostCoveredInRest o2) {
			if(this.equals(o2))return 0;
			
			if(this.accuracy > o2.accuracy){
				return 1;
			}
			else if(this.accuracy == o2.accuracy) {
				if(this.retrievedInstancesSize < o2.retrievedInstancesSize )
					return 1;
				else if(this.retrievedInstancesSize > o2.retrievedInstancesSize){
					return -1;
				}
				else return this.concept.toKBSyntaxString().compareTo(o2.concept.toKBSyntaxString());
			}else {
				return -1;
			}
			
		}
		
	}
	
	
}
