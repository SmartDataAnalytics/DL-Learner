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
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.learn.ConfWriter;
import org.dllearner.utilities.owl.ReasoningServiceFactory;
import org.dllearner.utilities.owl.ReasoningServiceFactory.AvailableReasoners;
import org.dllearner.utilities.statistics.SimpleClock;
import org.dllearner.utilities.statistics.Stat;

import com.jamonapi.MonitorFactory;

public class SemanticBible2 {

	private static ReasoningService reasoningService;

	private static Logger logger = Logger.getRootLogger();

	
	public static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";
	public static String dir = "sembib/";
	public static String sparqldir = dir+"sparql/";
	public static String normaldir = dir+"normal/";
	
	public static String tmpFilename = dir + "tmp.conf";
	static File log = new File(dir+"results+prop.txt");
	
	private static Stat accFragment = new Stat();
	private static Stat accOnOnto = new Stat();
	private static Stat accPosExOnOnto = new Stat();
	private static Stat accNegExOnOnto = new Stat();
	private static Stat timeFragment = new Stat();
	private static Stat nrOfExtractedTriples = new Stat();
	private static Stat dLengthFragment = new Stat();
	private static Stat dDepthFragment = new Stat();
	
	private static Stat timeWhole = new Stat();
	private static Stat accWhole = new Stat();
	private static Stat dLengthWhole = new Stat();
	private static Stat dDepthWhole = new Stat();
	
	private static int normalNoisePercentage = 0;
	private static int normalMaxExecution = 500;
	private static int sparqllMaxExecution = 250;
	
	private static boolean fragHasNot = false;
	private static boolean fragHasAll = false;
	private static boolean fragHasBooleanData = false;
	private static boolean fragHasNrRes = false;
	
	private static boolean wholeHasNot = false;
	private static boolean wholeHasAll = false;
	private static boolean wholeHasBooleanData = false;
	private static boolean wholeHasNrRes = false;
	
	
	
	//private static Class usedReasoner = FastInstanceChecker.class;
	
	private static boolean useSPARQL = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleClock total = new SimpleClock();
		Files.createFile(log, "");
	
		initLogger();
		logger.warn("Start");
		File tmpFile = new File(tmpFilename);
		
		
		List<File> confs = getFilesContaining(useSPARQL,"ten","all", "99+"); 
		//analyzeFiles(confs);
		
		
		
		reasoningService = ReasoningServiceFactory.getReasoningService(ontologyPath, AvailableReasoners.OWLAPIREASONERPELLET);
		ComponentManager cm =ComponentManager.getInstance();
		try{
		
		
		for (File f : confs) {
			Cache.getDefaultCache().clearCache();
			String fileContent = Files.readFile(f);
			
			SortedSet<Individual> posEx = SetManipulation.stringToInd(getIndividuals(fileContent, true));
			SortedSet<Individual> negEx = SetManipulation.stringToInd(getIndividuals(fileContent, false));
			
			
			StringBuffer sbuf = new StringBuffer(fileContent);
			sbuf.insert(0, sparqlOptions());
			Files.createFile(tmpFile, sbuf.toString());
			
			SimpleClock sc = new SimpleClock();
			Start.main(new String[] { tmpFilename });
			timeFragment.addNumber((double) sc.getTime());
		
			LearningAlgorithm la = cm.getLiveLearningAlgorithms().remove(0);
			
			EvaluatedDescription onFragment =(la.getCurrentlyBestEvaluatedDescription());
			
			accFragment.addNumber(onFragment.getAccuracy());
			dDepthFragment.addNumber((double)onFragment.getDescriptionDepth());
			dLengthFragment.addNumber((double)onFragment.getDescriptionLength());
			
			String desc = onFragment.getDescription().toKBSyntaxString();

			fragHasNot = ( fragHasNot || desc.contains("NOT"));
			fragHasAll = (fragHasAll || desc.contains("ALL"));
			fragHasBooleanData = (fragHasBooleanData || desc.contains("= FALSE")|| desc.contains("= TRUE"));
			fragHasNrRes = (fragHasNrRes || desc.contains("<")|| desc.contains(">"));
			
			SortedSet<Individual> retrieved = reasoningService.retrieval(onFragment.getDescription());
			EvaluatedDescription onOnto = reEvaluateDescription(
					onFragment.getDescription(), retrieved, posEx, negEx);
			
			accOnOnto.addNumber(onOnto.getAccuracy());
			
			int tmp = (int)(Math.floor(onOnto.getAccuracy()*100));
			normalNoisePercentage = 100-tmp;
			accPosExOnOnto.addNumber((double)(onOnto.getCoveredPositives().size()/5));
			double n = (double) (5-onOnto.getCoveredNegatives().size());
			accNegExOnOnto.addNumber(n/5.0);
			SparqlKnowledgeSource s=null;
			for(KnowledgeSource ks : cm.getLiveKnowledgeSources()){
				if (ks instanceof SparqlKnowledgeSource) {
					s = (SparqlKnowledgeSource) ks;
				}
			}
			
			double nrtrip = (double)(s.getNrOfExtractedTriples());
			nrOfExtractedTriples.addNumber(nrtrip);
			
			
			
			cm.freeAllComponents();
			/*************comp**/
			logger.warn("learning normal");

			StringBuffer sbufNormal = new StringBuffer();
			sbufNormal.append(normalOptions());
			sbufNormal.append(ConfWriter.listExamples(true, posEx));
			sbufNormal.append(ConfWriter.listExamples(false, negEx));
			//sbufNormal.append(ConfWriter.)
			Files.createFile(tmpFile, sbufNormal.toString());
			
			
			SimpleClock scNormal = new SimpleClock();
			Start.main(new String[] { tmpFilename });
			timeWhole.addNumber((double) scNormal.getTime());
			
			la = cm.getLiveLearningAlgorithms().remove(0);
			
			EvaluatedDescription normalOnOnto =(la.getCurrentlyBestEvaluatedDescription());
			accWhole.addNumber(normalOnOnto.getAccuracy());
			dDepthWhole.addNumber((double)normalOnOnto.getDescriptionDepth());
			dLengthWhole.addNumber((double)normalOnOnto.getDescriptionLength());
			
			fragHasNot = ( fragHasNot || desc.contains("NOT"));
			fragHasAll = (fragHasAll || desc.contains("ALL"));
			fragHasBooleanData = (fragHasBooleanData || desc.contains("= FALSE")|| desc.contains("= TRUE"));
			fragHasNrRes = (fragHasNrRes || desc.contains("<")|| desc.contains(">"));
			
			cm.freeAllComponents();
			writeLog();
			
			
		}//end for
		}catch (Exception e) {
			e.printStackTrace();
			
		}
		writeLog();
		total.printAndSet("Finished");
		//logger.warn("Finished");
	
	}
	
	public static void writeLog(){
		String l = "\n\n\n*********************\n";
		l +="COUNT: "+accFragment.getCount()+"\n";
		l +="FRAGMENT: ALL: "+fragHasAll+" BOOL: "+fragHasBooleanData+" NOT: "+fragHasNot+" <>=: "+fragHasNrRes+"\n";
		l +="WHOLE: ALL: "+wholeHasAll+" BOOL: "+wholeHasBooleanData+" NOT: "+wholeHasNot+" <>=: "+wholeHasNrRes+"\n";
		
			
		l+="accFragment\t\t"+accFragment.getMeanAsPercentage()+" +-"+accFragment.getStandardDeviation()+"\n";
		l+="accOnOnto\t\t"+accOnOnto.getMeanAsPercentage()+" +-"+accOnOnto.getStandardDeviation()+"\n";
		l+="accPosExOnOnto\t\t"+accPosExOnOnto.getMeanAsPercentage()+" +-"+accPosExOnOnto.getStandardDeviation()+"\n";
		l+="accNegExOnOnto\t\t"+accNegExOnOnto.getMeanAsPercentage()+" +-"+accNegExOnOnto.getStandardDeviation()+"\n";
		l+="timeFragment\t\t"+timeFragment.getMean()+" +-"+timeFragment.getStandardDeviation()+"\n";
		l+="nrOfExtractedTriples\t\t"+nrOfExtractedTriples.getMean()+" +-"+nrOfExtractedTriples.getStandardDeviation()+"\n";
		l+="dLengthFragment\t\t"+dLengthFragment.getMean()+" +-"+dLengthFragment.getStandardDeviation()+"\n";
		l+="dDepthFragment\t\t"+dDepthFragment.getMean()+" +-"+dDepthFragment.getStandardDeviation()+"\n";
		
		l+="timeWhole\t\t"+timeWhole.getMean()+" +-"+timeWhole.getStandardDeviation()+"\n";
		l+="accWhole\t\t"+accWhole.getMeanAsPercentage()+" +-"+accWhole.getStandardDeviation()+"\n";
		l+="dLengthWhole\t\t"+dLengthWhole.getMean()+" +-"+dLengthWhole.getStandardDeviation()+"\n";
		l+="dDepthWhole\t\t"+dDepthWhole.getMean()+" +-"+dDepthWhole.getStandardDeviation()+"\n";
		
		Files.appendFile(log, l);
		
//		 write JaMON report in HTML file
		File jamonlog = new File("sembib/jamon.html");
		Files.createFile(jamonlog, MonitorFactory.getReport());
		Files.appendFile(jamonlog, "<xmp>\n"+JamonMonitorLogger.getStringForAllSortedByLabel());
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
			"sparql.recursionDepth = 2;\n"+
			"sparql.useLits = true;\n"+
			"sparql.predefinedEndpoint = \"LOCALJOSEKIBIBLE\";\n"+
			"sparql.getPropertyInformation = true;\n"+
			"refexamples.maxExecutionTimeInSeconds = "+sparqllMaxExecution+";\n"+
			"import(\"lalala\",\"SPARQL\");\n"+
			getCombinedOptions()+
			"";
		return s;
	}
	
	public static String normalOptions (){
		String s="\n"+
			"import(\"NTNcombined.owl\");\n"+
			"refexamples.maxExecutionTimeInSeconds = "+normalMaxExecution+";\n";
			
			s+="refexamples.noisePercentage = "+normalNoisePercentage+";\n"+
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
