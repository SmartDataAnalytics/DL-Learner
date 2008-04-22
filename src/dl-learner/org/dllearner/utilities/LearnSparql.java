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
import org.dllearner.core.owl.Individual;
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
		((PosNegLP) lp).setPositiveExamples(toInd(posExamples));
		((PosNegLP) lp).setNegativeExamples(toInd(negExamples));
		//cm.applyConfigEntry(lp, "negativeExamples",toInd(negExamples));
		lp.init();
		
		la = cm.learningAlgorithm(ExampleBasedROLComponent.class, lp, rs);

		cm.applyConfigEntry(la,"useAllConstructor",false);
		cm.applyConfigEntry(la,"useExistsConstructor",true);
		cm.applyConfigEntry(la,"useCardinalityRestrictions",false);
		cm.applyConfigEntry(la,"useNegation",false);
		cm.applyConfigEntry(la,"minExecutionTimeInSeconds",0);
		cm.applyConfigEntry(la,"guaranteeXgoodDescriptions",20);
		
		//cm.applyConfigEntry(la,"quiet",false);
		if(ignoredConcepts.size()>0)
			cm.applyConfigEntry(la,"ignoredConcepts",ignoredConcepts);
		la.init();	
		
		System.out.println("start learning");
		la.start();
		
		//System.out.println("best"+la(20));
		((ExampleBasedROLComponent)la).printBestSolutions(20);
		
		}catch (Exception e) {e.printStackTrace();}
		//System.out.println( la.getBestSolution());;
	}
	
	protected  SortedSet<Individual> toInd(SortedSet<String> set ){
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for (String ind : set) {
			ret.add(new Individual(ind));
		}
		return ret;
	}
	
}
