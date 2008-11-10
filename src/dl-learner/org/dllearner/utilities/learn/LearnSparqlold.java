package org.dllearner.utilities.learn;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponentOld;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.statistics.SimpleClock;
import org.dllearner.utilities.statistics.Statistics;

public class LearnSparqlold {
	
	
	public SimpleClock sc= new SimpleClock();
	
	
	public void learnDBpedia(SortedSet<String> posExamples,SortedSet<String> negExamples,
			String uri, SortedSet<String> ignoredConcepts, int recursiondepth, 
			boolean closeAfterRecursion, boolean randomizeCache){
		
	
		ComponentManager cm = ComponentManager.getInstance();
		LearningAlgorithm la = null;
		ReasoningService rs = null;
		LearningProblem lp = null; 
		SparqlKnowledgeSource ks =null;
		try {
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		
		ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
		
		SortedSet<String> instances = new TreeSet<String>();
		instances.addAll(posExamples);
		instances.addAll(negExamples);
		cm.applyConfigEntry(ks, "instances",instances);
		cm.applyConfigEntry(ks, "url",uri);
		cm.applyConfigEntry(ks, "recursionDepth",recursiondepth);
		cm.applyConfigEntry(ks, "closeAfterRecursion",closeAfterRecursion);
		cm.applyConfigEntry(ks, "predefinedFilter","YAGO");
		cm.applyConfigEntry(ks, "predefinedEndpoint","LOCALDBPEDIA");
		if(randomizeCache)
			cm.applyConfigEntry(ks, "cacheDir","cache/"+System.currentTimeMillis()+"");
		else {}
		//cm.applyConfigEntry(ks, "format","KB");
		
		sc.setTime();
		ks.init();
		Statistics.addTimeCollecting(sc.getTime());
		sources.add(ks);
		//if (true)return;
		//System.out.println(ks.getNTripleURL());
		//
		
		ReasonerComponentOld r = new FastInstanceChecker(sources);
		//cm.applyConfigEntry(r,"useAllConstructor",false);
		//cm.applyConfigEntry(r,"useExistsConstructor",true);
		r.init();
		rs = new ReasoningService(r); 
		
		lp = new PosNegDefinitionLP(rs);
		//cm.applyConfigEntry(lp, "positiveExamples",toInd(posExamples));
		((PosNegLP) lp).setPositiveExamples(SetManipulation.stringToInd(posExamples));
		((PosNegLP) lp).setNegativeExamples(SetManipulation.stringToInd(negExamples));
		//cm.applyConfigEntry(lp, "negativeExamples",toInd(negExamples));
		lp.init();
		
		la = cm.learningAlgorithm(ExampleBasedROLComponent.class, lp, rs);

		cm.applyConfigEntry(la,"useAllConstructor",false);
		cm.applyConfigEntry(la,"useExistsConstructor",true);
		cm.applyConfigEntry(la,"useCardinalityRestrictions",false);
		cm.applyConfigEntry(la,"useNegation",false);
		cm.applyConfigEntry(la,"minExecutionTimeInSeconds",0);
		cm.applyConfigEntry(la,"maxExecutionTimeInSeconds",30);
		cm.applyConfigEntry(la,"writeSearchTree",false);
		cm.applyConfigEntry(la,"searchTreeFile","log/search.txt");
		cm.applyConfigEntry(la,"replaceSearchTree",true);
		//cm.applyConfigEntry(la,"noisePercentage",0.5);
		
		
		//cm.applyConfigEntry(la,"guaranteeXgoodDescriptions",999999);
		cm.applyConfigEntry(la,"logLevel","DEBUG");
		
		//cm.applyConfigEntry(la,"quiet",false);
		//System.out.println(ignoredConcepts.first());;
		if(ignoredConcepts.size()>0)
			cm.applyConfigEntry(la,"ignoredConcepts",ignoredConcepts);
		la.init();	
		
		System.out.println("start learning");
		sc.setTime();
		la.start();
		Statistics.addTimeLearning(sc.getTime());
		
		//if(sc.getTime()/1000 >= 20)System.out.println("XXXMAX time reached");
		
		//System.out.println("best"+la(20));
		//((ExampleBasedROLComponent)la).printBestSolutions(10000);
		
		}catch (Exception e) {e.printStackTrace();}
		//System.out.println( la.getBestSolution());;
	}
	
	
	
	
	
}
