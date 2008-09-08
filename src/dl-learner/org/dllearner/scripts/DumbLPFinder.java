/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.scripts;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Individual;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.learn.ConfWriter;
import org.dllearner.utilities.learn.LearnOWLFile;
import org.dllearner.utilities.learn.LearnOWLFileConfiguration;
import org.dllearner.utilities.learn.LearnSPARQLConfiguration;
import org.dllearner.utilities.learn.LearnSparql;
import org.dllearner.utilities.owl.ReasoningServiceFactory;
import org.dllearner.utilities.owl.ReasoningServiceFactory.AvailableReasoners;

public class DumbLPFinder {

	private static Logger logger = Logger.getRootLogger();

	private static ReasoningService reasoningService;
	
	public static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";
	
	//private static Class usedReasoner = FastInstanceChecker.class;
	private static Class usedReasoner = OWLAPIReasoner.class;
	private static boolean allOrExists = true;
	private static boolean tenORthirty = true;
	
	private static boolean sparql = true;
	
	private static boolean DEBUG = false;
	//private static boolean allOrExists = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initLogger();
		logger.info("started");
		//String fileURL = new File(ontologyFile).toURI().toString();
		
		reasoningService = ReasoningServiceFactory.getReasoningService(
				ontologyPath, AvailableReasoners.OWLAPIREASONERPELLET);
		
		String baseDir = "sembib/";
		Files.mkdir(baseDir);
		String baseDirSparql = baseDir+ "sparql/";
		Files.mkdir(baseDirSparql);
		String baseDirNormal = baseDir+"normal/";
		Files.mkdir(baseDirNormal);
		
		SortedSet<Individual> allIndividuals = new TreeSet<Individual>();
		allIndividuals.addAll( reasoningService.getIndividuals());
		
		reasoningService = null;
		ComponentManager.getInstance().freeAllComponents();
		
		int count = 1;
		while(count<10000){
			
			/*if((count%5)==0){
				//System.out.println(count+" "+allOrExists+"::"+tenORthirty);
			
				if(allOrExists && !tenORthirty){
					allOrExists = true;
					tenORthirty = true;
				}else if(!allOrExists && tenORthirty){
					allOrExists = true;
					tenORthirty = false;
				}else if(!allOrExists && !tenORthirty){
					allOrExists = false;
					tenORthirty = true;
				}else if(allOrExists && tenORthirty){
					allOrExists = false;
					tenORthirty = false;
				}
				//System.out.println(count+" "+allOrExists+"::"+tenORthirty);	
			
			}*/
			
			int exampleSize = (tenORthirty)?10:30;
			int half = (tenORthirty)?5:15;
			
			String filename ="";
			filename += (allOrExists)?"all_":"exists_";
			filename += (tenORthirty)?"ten_":"thirty_";
			
			
			try{
			
			SortedSet<Individual> tmp = SetManipulation.fuzzyShrinkInd(allIndividuals, exampleSize); 
			
			SortedSet<Individual> positiveEx = new TreeSet<Individual>();
			SortedSet<Individual> negativeEx = new TreeSet<Individual>();
			
			for (Individual individual : tmp) {
				if(positiveEx.size()< half){
					positiveEx.add(individual);
				}else{
					negativeEx.add(individual);
				}
				
			}
			
			EvaluatedDescription d;
			if(sparql){
				d = learnSPARQL( positiveEx, negativeEx);
			}else {
				d = learnOriginal( positiveEx, negativeEx);
			}
			 
			
			
				String div = (System.currentTimeMillis()%10000)+"";
				if(d.getAccuracy()>=0.99){
					filename +="99+";
				}else if(d.getAccuracy()>=0.90){
					filename +="90+";
				}else if(d.getAccuracy()>=0.80){
					filename +="80+";
				}else if(d.getAccuracy()>=0.70){
					filename +="70+";
				}else if(d.getAccuracy()>0.50){
					filename +="50+";
				}else {
					filename +="50-";
				}
				filename+="_";
				filename+=(d.getDescriptionLength()<10)?"0"+d.getDescriptionLength():d.getDescriptionLength()+"";
				filename += "_"+div+".conf";
				
				String content = fileString(true, d, positiveEx, negativeEx);
				Files.createFile(new File(baseDirSparql+filename), content);
				content = fileString(false, d, positiveEx, negativeEx);
				Files.createFile(new File(baseDirNormal+filename), content);
				
			
			
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				ComponentManager.getInstance().freeAllComponents();
			}
			//System.out.println(count);
			count++;
		}


	}

	public static String accString(EvaluatedDescription d){
		
		String acc = (d.getAccuracy())+"";
		try { acc = acc.substring(2,6);
		acc= acc.substring(0,2)+"."+acc.substring(3)+"%";}catch (Exception e) {	}
		
		return acc;
	}
	
	public static String fileString(boolean sparql, EvaluatedDescription d, SortedSet<Individual> p, SortedSet<Individual> n){
		
		String str = "/**\n" +
		d.getDescription().toKBSyntaxString() + "\n"+
		d + "\n"+
		"\n"+
		"**/\n"+
		"\n\n";
		if(sparql){
			str+="sparql.instances = {\n";
			for (Individual individual : p) {
				str+="\""+individual+"\",\n";
			}
			for (Individual individual : n) {
				str+="\""+individual+"\",\n";
			}
			str = str.substring(0, str.length()-2);
			str+="};\n";
			
		}
		
		str+="\n"+
		"/**EXAMPLES**/\n"+
		ConfWriter.listExamples(true, p)+"\n"+
		ConfWriter.listExamples(false, n)+"\n";
		
		return str;
	}
	
	private static EvaluatedDescription learnOriginal( SortedSet<Individual> posExamples, SortedSet<Individual> negExamples) {
		
		LearnOWLFile learner = new LearnOWLFile(getConfForOriginal());
		LearningAlgorithm la = null;
		try{
		la = learner.learn(
				SetManipulation.indToString(posExamples), 
				SetManipulation.indToString(negExamples), 
				usedReasoner);
		la.start();
		}catch (Exception e) {
			System.out.println("ignoring the error "+e.toString());
			
		}
		
		EvaluatedDescription d = la.getCurrentlyBestEvaluatedDescription();
		
		return d;
			
		
	}
	
	private static EvaluatedDescription learnSPARQL( SortedSet<Individual> posExamples, SortedSet<Individual> negExamples) {
		
		LearnSparql learner = new LearnSparql(getConfForSparql());
		LearningAlgorithm la = null;
		try{
		la = learner.learn(
				SetManipulation.indToString(posExamples), 
				SetManipulation.indToString(negExamples), 
				usedReasoner);
		la.start();
		}catch (Exception e) {
			System.out.println("ignoring the error "+e.toString());
			
		}
		
		EvaluatedDescription d = la.getCurrentlyBestEvaluatedDescription();
		
		return d;
			
		
	}

	

	private static LearnOWLFileConfiguration getConfForOriginal() {
		LearnOWLFileConfiguration lc = new LearnOWLFileConfiguration();
		
		
		lc.setOWLFileURL(ontologyPath);
		
		lc.noisePercentage = 0;
		lc.guaranteeXgoodDescriptions = 1;
		if(allOrExists){
			lc.useAllConstructor = true;
			lc.useCardinalityRestrictions = true;
			lc.useExistsConstructor =true;
			lc.useNegation = true;
		}else {
			lc.useAllConstructor = false;
			lc.useCardinalityRestrictions = false;
			lc.useExistsConstructor =true;
			lc.useNegation = false;
		}
		
		
		Class tmp = FastInstanceChecker.class;
		if(usedReasoner.equals(tmp)){
			lc.maxExecutionTimeInSeconds = 30;
		}else{
			lc.maxExecutionTimeInSeconds = 200;
		}
		return lc;

	}
	
	private static LearnSPARQLConfiguration getConfForSparql() {
		LearnSPARQLConfiguration lc = new LearnSPARQLConfiguration();
		// lsc.sparqlEndpoint = sparqlTasks.getSparqlEndpoint();

		
		lc.recursiondepth = 2;
		lc.closeAfterRecursion = true;
		lc.useLits = true;
		lc.predefinedEndpoint = "LOCALJOSEKIBIBLE";
		
		lc.noisePercentage = 0;
		lc.guaranteeXgoodDescriptions = 1;
		lc.maxExecutionTimeInSeconds = 30;
		
		
		
		if(allOrExists){
			lc.useAllConstructor = true;
			lc.useCardinalityRestrictions = true;
			lc.useExistsConstructor =true;
			lc.useNegation = true;
		}else {
			lc.useAllConstructor = false;
			lc.useCardinalityRestrictions = false;
			lc.useExistsConstructor =true;
			lc.useNegation = false;
		}
		
		// lsc.searchTreeFile = "log/WikipediaCleaner.txt";

		return lc;

	}
	
	
	private static void initLogger() {

		SimpleLayout layout = new SimpleLayout();
		// create logger (a simple logger which outputs
		// its messages to the console)
		FileAppender fileAppender = null;
		try {
			fileAppender = new FileAppender(layout, "log/semBibleLog.txt",
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		logger.removeAllAppenders();
		if(DEBUG){
			logger.setLevel(Level.DEBUG);
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			logger.addAppender(consoleAppender);
		}else{
			logger.setLevel(Level.INFO);
		}
		logger.addAppender(fileAppender);
		
		

	}

}
