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
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.gui.Config;
import org.dllearner.gui.ConfigSave;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.owl.ReasoningServiceFactory;
import org.dllearner.utilities.owl.ReasoningServiceFactory.AvailableReasoners;
import org.dllearner.utilities.statistics.SimpleClock;
import org.dllearner.utilities.statistics.Stat;

import com.jamonapi.MonitorFactory;

public class SemanticBibleComparison {

	private static ReasoningService reasoningService;

	private static Logger logger = Logger.getRootLogger();
	public static boolean flawInExperiment = false;

	
	public static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";
	public static String dir = "sembib/";
	//public static String sparqldir = dir+"sparql/";
	public static String exampleDir = dir+"examples/";
	
	public static String tmpFilename = dir + "tmp.conf";
	static File log = new File(dir+"results+prop.txt");
	
	private static Stat accFragment = new Stat();
	private static Stat accOnOnto = new Stat();
	private static Stat accPosExOnOnto = new Stat();
	private static Stat accNegExOnOnto = new Stat();
	private static Stat learningTime = new Stat();
	private static Stat nrOfExtractedTriples = new Stat();
	private static Stat descLength = new Stat();
	private static Stat descDepth = new Stat();
	
	private static Stat timeWhole = new Stat();
	private static Stat accWhole = new Stat();
	private static Stat dLengthWhole = new Stat();
	private static Stat dDepthWhole = new Stat();
	
	
	private static boolean descHasNot = false;
	private static boolean descHasAll = false;
	private static boolean descHasBooleanData = false;
	private static boolean descHasNrRes = false;
	
	private static boolean wholeHasNot = false;
	private static boolean wholeHasAll = false;
	private static boolean wholeHasBooleanData = false;
	private static boolean wholeHasNrRes = false;
	
	
	
	//private static Class usedReasoner = FastInstanceChecker.class;
	
	//private static boolean useSPARQL = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleClock total = new SimpleClock();
		Files.createFile(log, "");
	
		initLogger();
		logger.warn("Start");
		
		
		
		conductExperiment(0);
		
	
		
		total.printAndSet("Finished");
		if(flawInExperiment){
			logger.error("There were exceptions");
		}
		//logger.warn("Finished");
	
	}
	
	public static void conductExperiment(int experiment){
		
		try{
			//prepare everything
			List<String> confs = getFiles();
			ComponentManager cm =ComponentManager.getInstance();
			
			for (String filename : confs) {
				// read the file and get the examples
				File f = new File(filename);
				Cache.getDefaultCache().clearCache();
				String fileContent = Files.readFile(f);
				SortedSet<Individual> posEx = SetManipulation.stringToInd(getIndividuals(fileContent, true));
				SortedSet<Individual> negEx = SetManipulation.stringToInd(getIndividuals(fileContent, false));
				
				ExampleBasedROLComponent la = experimentalSetup1(posEx,negEx);
				//TODO measure time
				initAllComponents();
				
				SimpleClock learningTimeClock = new SimpleClock();
				la.start();
				learningTime.addNumber((double) learningTimeClock.getTime());
			
				EvaluatedDescription bestDescription =(la.getCurrentlyBestEvaluatedDescription());
				
				accFragment.addNumber(bestDescription.getAccuracy());
				descDepth.addNumber((double)bestDescription.getDescriptionDepth());
				descLength.addNumber((double)bestDescription.getDescriptionLength());
				
				String desc = bestDescription.getDescription().toKBSyntaxString();

				descHasNot = ( descHasNot || desc.contains("NOT"));
				descHasAll = (descHasAll || desc.contains("ALL"));
				descHasBooleanData = (descHasBooleanData || desc.contains("= FALSE")|| desc.contains("= TRUE"));
				descHasNrRes = (descHasNrRes || desc.contains("<")|| desc.contains(">"));
				
				// evaluate Concept versus Ontology
				reasoningService = ReasoningServiceFactory.getReasoningService(ontologyPath, AvailableReasoners.OWLAPIREASONERPELLET);
				SortedSet<Individual> retrieved = reasoningService.retrieval(bestDescription.getDescription());
				EvaluatedDescription onOnto = reEvaluateDescription(
						bestDescription.getDescription(), retrieved, posEx, negEx);
				
				accOnOnto.addNumber(onOnto.getAccuracy());
				
				//int tmp = (int)(Math.floor(onOnto.getAccuracy()*100));
				//normalNoisePercentage = 100-tmp;
				accPosExOnOnto.addNumber((double)(onOnto.getCoveredPositives().size()/5));
				double n = (double) (5-onOnto.getCoveredNegatives().size());
				accNegExOnOnto.addNumber(n/5.0);
				SparqlKnowledgeSource s=null;
				for(KnowledgeSource ks : cm.getLiveKnowledgeSources()){
					if (ks instanceof SparqlKnowledgeSource) {
						s = (SparqlKnowledgeSource) ks;
					}
				}
				if(s!=null){
					double nrtrip = (double)(s.getNrOfExtractedAxioms());
					nrOfExtractedTriples.addNumber(nrtrip);
				}else{
					nrOfExtractedTriples.addNumber(0.0);
				}
				
				cm.freeAllComponents();
				
				
				
			}//end for
			}catch (Exception e) {
				e.printStackTrace();
				flawInExperiment = true;
			}
		
	}
	
	public static ExampleBasedROLComponent experimentalSetup1(SortedSet<Individual> posExamples, SortedSet<Individual> negExamples ){
		ExampleBasedROLComponent la = prepareSparqlExperiment(posExamples, negExamples);
		
		//la.getConfigurator();
		//appendtoFile
		
		return la;
	}
	
	public static ExampleBasedROLComponent prepareSparqlExperiment(SortedSet<Individual> posExamples, SortedSet<Individual> negExamples){
		

		ExampleBasedROLComponent la = null;
		try{
			SortedSet<Individual> instances = new TreeSet<Individual>();
			instances.addAll(posExamples);
			instances.addAll(negExamples);
	
			SparqlKnowledgeSource ks = ComponentFactory
					.getSparqlKnowledgeSource(URI.create(
							"http://localhost:2020/bible").toURL(), SetManipulation
							.indToString(instances));
	
			ks.getConfigurator().setCloseAfterRecursion(true);
			ks.getConfigurator().setRecursionDepth(2);
			ks.getConfigurator().setPredefinedEndpoint("LOCALJOSEKIBIBLE");
			ks.getConfigurator().setUseLits(true);
			ks.getConfigurator().setGetAllSuperClasses(true);
			ks.getConfigurator().setGetPropertyInformation(true);
			
			Set<KnowledgeSource> tmp = new HashSet<KnowledgeSource>();
			tmp.add(ks);
			// reasoner
			OWLAPIReasoner f = ComponentFactory
					.getOWLAPIReasoner(tmp);
			ReasoningService rs = ComponentManager.getInstance()
					.reasoningService(f);
	
			// learning problem
			PosNegDefinitionLP lp = ComponentFactory.getPosNegDefinitionLP(rs,
					SetManipulation.indToString(posExamples), SetManipulation
							.indToString(negExamples));
	
			// learning algorithm
			la = ComponentFactory.getExampleBasedROLComponent(lp, rs);
			la.getConfigurator().setGuaranteeXgoodDescriptions(1);
			Config c = new Config(ComponentManager.getInstance(), ks, f, rs, lp, la);
			new ConfigSave(c).saveFile(new File(tmpFilename));
			
		}catch (Exception e) {
			 e.printStackTrace();
			 flawInExperiment = true;
		}
		return la;
	}
	
	public static void initAllComponents(){
		ComponentManager cm = ComponentManager.getInstance();
		for(Component c : cm.getLiveComponents()){
			try{
			 c.init();
			}catch (Exception e) {
				 e.printStackTrace();
				 flawInExperiment = true;
			}
		}
	}
	
	public static void writeLog(){
		String l = "\n\n\n*********************\n";
		l +="COUNT: "+accFragment.getCount()+"\n";
		l +="FRAGMENT: ALL: "+descHasAll+" BOOL: "+descHasBooleanData+" NOT: "+descHasNot+" <>=: "+descHasNrRes+"\n";
		l +="WHOLE: ALL: "+wholeHasAll+" BOOL: "+wholeHasBooleanData+" NOT: "+wholeHasNot+" <>=: "+wholeHasNrRes+"\n";
		
			
		l+="accFragment\t\t"+accFragment.getMeanAsPercentage()+" +-"+accFragment.getStandardDeviation()+"\n";
		l+="accOnOnto\t\t"+accOnOnto.getMeanAsPercentage()+" +-"+accOnOnto.getStandardDeviation()+"\n";
		l+="accPosExOnOnto\t\t"+accPosExOnOnto.getMeanAsPercentage()+" +-"+accPosExOnOnto.getStandardDeviation()+"\n";
		l+="accNegExOnOnto\t\t"+accNegExOnOnto.getMeanAsPercentage()+" +-"+accNegExOnOnto.getStandardDeviation()+"\n";
		l+="timeFragment\t\t"+learningTime.getMean()+" +-"+learningTime.getStandardDeviation()+"\n";
		l+="nrOfExtractedTriples\t\t"+nrOfExtractedTriples.getMean()+" +-"+nrOfExtractedTriples.getStandardDeviation()+"\n";
		l+="dLengthFragment\t\t"+descLength.getMean()+" +-"+descLength.getStandardDeviation()+"\n";
		l+="dDepthFragment\t\t"+descDepth.getMean()+" +-"+descDepth.getStandardDeviation()+"\n";
		
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
	
	
	public static List<String>  getFiles(){
			String actualDir = exampleDir;
			logger.warn(actualDir);
			File f = new File(actualDir);
		    String[] files = f.list();
		    Arrays.sort(files);
		 return Arrays.asList(files);   
	}
	
	
	
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
	
	/*public static List<File>  getFilesContaining(boolean sparql, String numExamples, String allOrEx, String acc) {
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
}*/

/*public static void analyzeFiles(List<File> l){
	
	SortedSet<String> differentIndividuals = new TreeSet<String>();
	for ( content : l) {
		differentIndividuals.addAll(getIndividuals(content, true));
		differentIndividuals.addAll(getIndividuals(content, false));
		
	}
	System.out.println("found diff inds "+differentIndividuals.size());
	
}*/

	
}
