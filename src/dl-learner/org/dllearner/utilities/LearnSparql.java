package org.dllearner.utilities;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.reasoning.FastInstanceChecker;

public class LearnSparql {

	
	
	public void learn(SortedSet<String> posExamples,SortedSet<String> negExamples,
			String uri, SortedSet<String> ignoredConcepts){
		
	
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
		cm.applyConfigEntry(ks, "recursionDepth",1);
		cm.applyConfigEntry(ks, "predefinedFilter",1);
		cm.applyConfigEntry(ks, "predefinedEndpoint",1);
		//cm.applyConfigEntry(ks, "format","KB");
		
		ks.init();
		sources.add(ks);
		//System.out.println(ks.getNTripleURL());
		//
		
		ReasonerComponent r = new FastInstanceChecker(sources);
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
		cm.applyConfigEntry(la,"minExecutionTimeInSeconds",100);
		cm.applyConfigEntry(la,"maxExecutionTimeInSeconds",100);
		cm.applyConfigEntry(la,"guaranteeXgoodDescriptions",1);
		
		//cm.applyConfigEntry(la,"quiet",false);
		if(ignoredConcepts.size()>0)
			cm.applyConfigEntry(la,"ignoredConcepts",ignoredConcepts);
		la.init();	
		
		System.out.println("start learning");
		la.start();
		
		//System.out.println("best"+la(20));
		((ExampleBasedROLComponent)la).printBestSolutions(200);
		
		}catch (Exception e) {e.printStackTrace();}
		//System.out.println( la.getBestSolution());;
	}
	
	
	
}
