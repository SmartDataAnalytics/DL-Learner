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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.Start;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.StringFormatter;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.owl.ReasoningServiceFactory;
import org.dllearner.utilities.owl.ReasoningServiceFactory.AvailableReasoners;
import org.dllearner.utilities.statistics.SimpleClock;

import com.jamonapi.Monitor;

public class SemanticBible2 {

	private static ReasoningService reasoningService;

	private static Logger logger = Logger.getRootLogger();

	
	public static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";
	public static String dir = "sembib/";
	public static String sparqldir = dir+"sparql/";
	public static String normaldir = dir+"normal/";
	
	public static String tmpFilename = dir + "tmp.conf";
	static File log = new File(dir+"results.txt");
	
	
	//private static Class usedReasoner = FastInstanceChecker.class;
	
	private static boolean useSPARQL = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleClock total = new SimpleClock();
		initLogger();
		logger.warn("Start");
		File tmpFile = new File(tmpFilename);
		String del="\t";
		String cr="\n";
		
		List<File> confs = getFilesContaining(useSPARQL,"ten","all", "99+"); 
		//analyzeFiles(confs);
		Files.createFile(log,
				"accOnFragment"+del+
				"accOnOnt"+del+
				"timeFragme"+del+
				"coveredPos"+del+
				"coveredNeg"+del+
				"timeWhole"+cr);
		
		
		reasoningService = ReasoningServiceFactory.getReasoningService(ontologyPath, AvailableReasoners.OWLAPIREASONERPELLET);
		ComponentManager cm =ComponentManager.getInstance();
		try{
		
		for (File f : confs) {
			String fileContent = Files.readFile(f);
			
			
			String logLine ="";
			SortedSet<Individual> posEx = SetManipulation.stringToInd(getIndividuals(fileContent, true));
			SortedSet<Individual> negEx = SetManipulation.stringToInd(getIndividuals(fileContent, false));
			
			
			StringBuffer sbuf = new StringBuffer(fileContent);
			sbuf.insert(0, (useSPARQL)?sparqlOptions():normalOptions());
			Files.createFile(tmpFile, sbuf.toString());
			//System.out.println(tmpFile.getCanonicalPath());
			
			Monitor m = JamonMonitorLogger.getTimeMonitor(SemanticBible2.class, "learn on fragment").start();
			SimpleClock sc = new SimpleClock();
			Start.main(new String[] { tmpFilename });
			long time = sc.getTime();
			m.stop();
			LearningAlgorithm la = getLearningAlgorithm();
			
			EvaluatedDescription onFragment =(la.getCurrentlyBestEvaluatedDescription());
			logLine += StringFormatter.doubleToPercent(onFragment.getAccuracy())+del;
			SortedSet<Individual> retrieved = reasoningService.retrieval(onFragment.getDescription());
			EvaluatedDescription onOnto = reEvaluateDescription(
					onFragment.getDescription(), retrieved, posEx, negEx);
			/*if(onOnto.getAccuracy()!=1.0){
				Files.appendFile(log, onOnto.toString()+"\n");
				System.out.println(onOnto.toString());
				
				System.out.println(onOnto.getCoveredPositives());
				System.out.println(onOnto.getCoveredNegatives().size());
				
				System.out.println(onOnto.getNotCoveredPositives());
				System.out.println(onOnto.getNotCoveredNegatives());
				System.out.println("p then n");
				System.out.println(posEx);
				System.out.println(negEx);
				System.out.println(retrieved);
				System.out.println((5-onOnto.getCoveredNegatives().size())+"");
				double n = (double) (5-onOnto.getCoveredNegatives().size());
				System.out.println();
				System.out.println((n/5.0));
				System.exit(0);
			}*/
			logLine += StringFormatter.doubleToPercent(onOnto.getAccuracy())+del;
			logLine += time+del;
			logLine += StringFormatter.doubleToPercent((double)(onOnto.getCoveredPositives().size()/5))+del;
			double n = (double) (5-onOnto.getCoveredNegatives().size());
			logLine += StringFormatter.doubleToPercent(n/5.0)+del;
			logLine += " size of retrieve: "+retrieved.size()+del;
			logLine += f.toString();
			Files.appendFile(log, logLine+cr);
			Cache.getDefaultCache().clearCache();
			cm.freeAllComponents();
			
			
		}//end for
		}catch (Exception e) {
			e.printStackTrace();
			
		}
		total.printAndSet("Finished");
		//logger.warn("Finished");
	
	}
	
	public static EvaluatedDescription reEvaluateDescription(Description d, SortedSet<Individual> retrieved ,SortedSet<Individual> posEx ,SortedSet<Individual> negEx ){
		SortedSet<Individual> PosAsPos = new TreeSet<Individual>();
		SortedSet<Individual> PosAsNeg = new TreeSet<Individual>();
		SortedSet<Individual> NegAsPos = new TreeSet<Individual>();
		SortedSet<Individual> NegAsNeg = new TreeSet<Individual>();
		
		// PosAsPos
		PosAsPos.addAll(posEx);
		PosAsPos.retainAll(retrieved);

		//System.out.println(PosAsPos);
		
		// PosAsNeg
		PosAsNeg.addAll(posEx);
		PosAsNeg.removeAll(retrieved);
		
		//System.out.println(PosAsNeg);
		
		// NegAsPos
		NegAsPos.addAll(negEx);
		NegAsPos.retainAll(retrieved);
		
		//System.out.println(NegAsPos);

		// PosAsNeg
		NegAsNeg.addAll(negEx);
		NegAsNeg.removeAll(retrieved);
		
		//System.out.println(NegAsNeg);
		
		
		return new EvaluatedDescription(d, PosAsPos, PosAsNeg, NegAsPos,NegAsNeg);
		
	}
	
	public static LearningAlgorithm getLearningAlgorithm(){
		ComponentManager cm =ComponentManager.getInstance();
		
		List<Component> comp = cm.getLiveComponents();
		for (Component component : comp) {
			if(component instanceof LearningAlgorithm){
				return (LearningAlgorithm) component;
			}
			
		}
		return null;
	}
	
	public static List<File>  getFilesContaining(boolean sparql, String numExamples, String allOrEx, String acc) {
		List<File> ret = new ArrayList<File>();
		//SortedSet<File> ret = new TreeSet<File>();
		
			String actualDir = (sparql)?sparqldir:normaldir;
			logger.warn(actualDir);
			File f = new File(actualDir);
		    String[] files = f.list();
		    Arrays.sort(files);
		    int consistent = 0;
		  try{
		    for (int i = 0; i < files.length; i++) {
		    	
				if(		files[i].contains(numExamples) 
						&& files[i].contains(allOrEx)
						&& files[i].contains(acc)
						){
					consistent++;
					ret.add(new File(actualDir+files[i]));
					if(ret.size() != consistent){
						logger.warn("double file: "+files[i]);
					}
				}
					
			}    
		}catch (Exception e) {
			
			e.printStackTrace();
		}
		if(consistent != ret.size()){
			logger.warn("double files"+consistent+"::"+ret.size());
			System.exit(0);
		}else{
			logger.warn("all files different");
		}
	    return ret;
	}
	
	/*public static void analyzeFiles(List<File> l){
		
		SortedSet<String> differentIndividuals = new TreeSet<String>();
		for ( content : l) {
			differentIndividuals.addAll(getIndividuals(content, true));
			differentIndividuals.addAll(getIndividuals(content, false));
			
		}
		System.out.println("found diff inds "+differentIndividuals.size());
		
	}*/
	
	public static SortedSet<String> getIndividuals(String target, boolean posOrNeg){
		if(posOrNeg){
			return getAllStringsBetween(target, "+\"", "\"");
		}else{
			return getAllStringsBetween(target, "-\"", "\"");
		}
		
	}
	
	public static  SortedSet<String> getAllStringsBetween(String target, String start, String end){
		SortedSet<String> ret = new TreeSet<String>();
		StringTokenizer st = new StringTokenizer(target,"\n");
		while(st.hasMoreElements()){
			String line = st.nextToken();
			if(line.contains(start)){
				line = line.substring(line.indexOf(start)+start.length());
				String current = line.substring(0,line.indexOf(end));
				ret.add(current);
			}
		}
		
		return ret;
	}
	
	public static String getCombinedOptions(){
		String s="\n"+
		"algorithm = refexamples;\n"+
		"refexamples.useAllConstructor = true;\n"+
		"refexamples.useNegation = true;\n"+
		"refexamples.useCardinalityRestrictions = true;\n"+
		"refexamples.guaranteeXgoodDescriptions = 1;\n"+
		
		"\n"+
		"reasoner = owlAPI;\n"+
		//"reasoner = fastInstanceChecker;\n"+
		"owlAPIReasoner.reasonerType = pellet;\n\n"+
		"";
	return s;
	}
	
	public static String sparqlOptions (){
		String s="// SPARQL options\n"+
			"sparql.recursionDepth = 3;\n"+
			"sparql.useLits = true;\n"+
			"sparql.predefinedEndpoint = \"LOCALJOSEKIBIBLE\";\n"+
			"import(\"lalala\",\"SPARQL\");\n"+
			getCombinedOptions()+
			"";
		return s;
	}
	
	public static String normalOptions (){
		String s="\n"+
			"import(\"NTNcombined.owl\");\n"+
			"refexamples.maxExecutionTimeInSeconds = 1800;\n"+
			getCombinedOptions()+
			"";
		return s;
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

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.WARN);
		

	}

	
}
