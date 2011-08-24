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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.configurators.SparqlKnowledgeSourceConfigurator;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.gui.Config;
import org.dllearner.gui.ConfigSave;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.StringFormatter;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.ExampleContainer;
import org.dllearner.utilities.statistics.SimpleClock;
import org.dllearner.utilities.statistics.Stat;
import org.dllearner.utilities.statistics.Table;
import org.dllearner.utilities.statistics.TableColumn;

import com.jamonapi.MonitorFactory;

public class SemanticBibleComparison {

	
	private static int nrOfFilesInExperiment = 200;
	
	
	private static AbstractReasonerComponent reasoningService;

	private static Logger logger = Logger.getRootLogger();
	private static boolean flawInExperiment = false;

	
	private static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";
	private static String dir = "sembib100/";
	//public static String sparqldir = dir+"sparql/";
	private static String exampleDir = dir+"examples/";
	private static String tableDir = dir+"table/";
	
	private static String tmpFilename = dir + "tmp.conf";
	//static File log = new File(dir+"results+prop.txt");
	//private static String tableFile = tableDir+"sembib.table";
	//private static String tableLatex = tableDir+"sembibLatex.table";
	
	private static Stat accFragment = new Stat();
	private static Stat accOnOnto = new Stat();
	private static Stat accPosExOnOnto = new Stat();
	private static Stat accNegExOnOnto = new Stat();
	private static Stat learningTime = new Stat();
	//private static Stat initializationTime = new Stat();
	private static Stat reasonerInitializationTime = new Stat();
	private static Stat ksinitializationTime = new Stat();
	private static Stat nrOfExtractedAxioms = new Stat();
	private static Stat descLength = new Stat();
	private static Stat descDepth = new Stat();
	
	private static boolean descHasNot = false;
	private static boolean descHasAll = false;
	private static boolean descHasBooleanData = false;
	private static boolean descHasNrRes = false;
	
	private static Table t;
	//private static boolean wholeHasNot = false;
	//private static boolean wholeHasAll = false;
	//private static boolean wholeHasBooleanData = false;
	//private static boolean wholeHasNrRes = false;
	
	//10s means fixed time 10s
	private static enum Experiments  {
		SPARQL_10s,
		NORMAL_10s, 
		SPARQL_100s,
		NORMAL_100s,
		SPARQL_1000_CTESTS,
		NORMAL_1000_CTESTS,
		SPARQL_10000_CTESTS,
		NORMAL_10000_CTESTS,
		
		NORMAL_10000_CTESTS_FASTINST,
		
		SPARQL_10000_CTESTS_SPECIAL_REC2_NOPROP,
		SPARQL_10000_CTESTS_SPECIAL_REC2_NOCLOSE_NOPROP,
		SPARQL_10000_CTESTS_SPECIAL_REC1,
		SPARQL_10000_CTESTS_SPECIAL_REC3,
		
		SPARQL_10000_CTESTS_SPECIAL_REC2_NOPROP_HASVALUE,
		SPARQL_10000_CTESTS_SPECIAL_REC2_NOCLOSE_NOPROP_HASVALUE,
		SPARQL_10000_CTESTS_SPECIAL_REC1_HASVALUE,
		SPARQL_10000_CTESTS_SPECIAL_REC3_HASVALUE,
		SPARQL_10000_CTESTS_HASVALUE
		
		};
	
	
	//private static Class usedReasoner = FastInstanceChecker.class;
	
	//private static boolean useSPARQL = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleClock total = new SimpleClock();
		
	
		initLogger();
		logger.warn("Start");
		prepareTable();
		
		List<String> l = getFiles();
		analyzeFiles(l);
		
	
		
			/*finished experiments:
			
			 * NORMAL_1000_CONCEPT_TESTS
			 * SPARQL_10000_CONCEPT_TESTS
			 * NORMAL_10000_CONCEPT_TESTS
			 * to be repeated:
			 * NORMAL_10s
			 * SPARQL_100s
			 * NORMAL_100s
			 * SPARQL_10s
			 * SPARQL_1000_CONCEPT_TESTS
			 * SPARQL_10000_CONCEPT_TESTS
			 * NORMAL_100s
			 * 
			 * */
		
			boolean jens=true;
			if(jens){
				
				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC2_NOPROP_HASVALUE);
				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC2_NOPROP);

				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC2_NOCLOSE_NOPROP_HASVALUE);
				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC2_NOCLOSE_NOPROP);
				
				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC1_HASVALUE);
				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC1);
				
				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC3_HASVALUE);
				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC3);

				conductExperiment(Experiments.SPARQL_10000_CTESTS_HASVALUE);
				conductExperiment(Experiments.SPARQL_10000_CTESTS);
				
			}else{
			
				//conductExperiment(Experiments.SPARQL_10s);
				//conductExperiment(Experiments.SPARQL_1000_CTESTS);
				//conductExperiment(Experiments.NORMAL_10s);
				
				//conductExperiment(Experiments.SPARQL_100s);
				
				//conductExperiment(Experiments.NORMAL_100s);
				//conductExperiment(Experiments.SPARQL_10000_CTESTS);
				
				
				//EXTRA
				//conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC2_NOPROP);
				//conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC2_NOCLOSE_NOPROP);
				//conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC1);
				conductExperiment(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC3);
				conductExperiment(Experiments.NORMAL_10000_CTESTS_FASTINST);
				//conductExperiment(Experiments.SPARQL_10000_CTESTS);
			
			}
		
	
		//		 write JaMON report in HTML file
		
		//total.printAndSet("Finished");
		logger.warn(total.getAndSet("Finished"));
		if(flawInExperiment){
			logger.error("There were exceptions, see log");
		}else{
			logger.warn("The experiment finished without any flaws");
		}
		//logger.warn("Finished");
	
	}
	
	public static void writeJamonLog(String filename){
		File jamonlog = new File(filename);
		Files.createFile(jamonlog, MonitorFactory.getReport());
		Files.appendFile(jamonlog, "<xmp>\n"+JamonMonitorLogger.getStringForAllSortedByLabel());
	}
	
	
	
	
	public static void conductExperiment(Experiments exp){
		
		
			//prepare everything
			List<String> confs = getFiles();
			ComponentManager cm =ComponentManager.getInstance();
			
			int count = 0;
			for (String filename : confs) {
				SimpleClock oneExperiment = new SimpleClock();
				try{
				
					
				if (count == nrOfFilesInExperiment){break;}
				
				logger.warn("****"+exp+" "+(count+1) +" from file "+filename);
				
				// read the file and get the examples
				File f = new File(exampleDir+filename);
				Cache.getDefaultCache().clearCache();
				String fileContent = Files.readFile(f);
				SortedSet<Individual> posEx = SetManipulation.stringToInd(getIndividuals(fileContent, true));
				SortedSet<Individual> negEx = SetManipulation.stringToInd(getIndividuals(fileContent, false));
				
				OCEL la = experimentalSetup(exp,posEx,negEx);
				
				//SimpleClock init = new SimpleClock();
				initAllComponents();
				//initializationTime.addNumber((double) init.getTime()/1000);
				
				
				SimpleClock learningTimeClock = new SimpleClock();
				la.start();
				learningTime.addNumber((double) learningTimeClock.getTime()/1000);
				logger.warn(learningTimeClock.getAndSet("learning time")+" in stat: "+learningTime.getMean());
				
				
				EvaluatedDescriptionPosNeg bestDescription =(la.getCurrentlyBestEvaluatedDescription());
				
				accFragment.addNumber(bestDescription.getAccuracy());
				descDepth.addNumber((double)bestDescription.getDescriptionDepth());
				descLength.addNumber((double)bestDescription.getDescriptionLength());
				
				String desc = bestDescription.getDescription().toKBSyntaxString();

				descHasNot = ( descHasNot || desc.contains("NOT"));
				descHasAll = (descHasAll || desc.contains("ALL"));
				descHasBooleanData = (descHasBooleanData || desc.contains("= FALSE")|| desc.contains("= TRUE"));
				descHasNrRes = (descHasNrRes || desc.contains("<")|| desc.contains(">"));
				
				// evaluate Concept versus Ontology
				reasoningService = org.dllearner.utilities.components.ReasonerComponentFactory.getReasonerComponent(ontologyPath, ReasonerType.OWLAPI_PELLET);
				SortedSet<Individual> retrieved = reasoningService.getIndividuals(bestDescription.getDescription());
				EvaluatedDescriptionPosNeg onOnto = reEvaluateDescription(
						bestDescription.getDescription(), retrieved, posEx, negEx);
				
				logger.warn(bestDescription.getAccuracy());
				logger.warn(onOnto.getDescription().toKBSyntaxString());
				logger.warn(onOnto.getAccuracy());
				
				accOnOnto.addNumber(onOnto.getAccuracy());
				
				//int tmp = (int)(Math.floor(onOnto.getAccuracy()*100));
				//normalNoisePercentage = 100-tmp;
				accPosExOnOnto.addNumber((double)(onOnto.getCoveredPositives().size()/5));
				double n = (double) (5-onOnto.getCoveredNegatives().size());
				accNegExOnOnto.addNumber(n/5.0);
				SparqlKnowledgeSource s=null;
				for(AbstractKnowledgeSource ks : cm.getLiveKnowledgeSources()){
					if (ks instanceof SparqlKnowledgeSource) {
						s = (SparqlKnowledgeSource) ks;
					}
				}
				if(s!=null){
					double nrtrip = (double)(s.getNrOfExtractedAxioms());
					nrOfExtractedAxioms.addNumber(nrtrip);
				}else{
					nrOfExtractedAxioms.addNumber(0.0);
				}
				
				
				}catch (Exception e) {
					e.printStackTrace();
					flawInExperiment = true;
					logger.warn(t.getLatexString());
					logger.warn(e);
					
				}finally{
					cm.freeAllComponents();
					
					fillTable(exp, (count+1));
					
					logger.warn(exp+" "+(count+1)+ " " +oneExperiment.getAndSet("")+"****" );
					count++;
				}
				
			}//end for
			String tmp="";
			tmp+="NOT: "+descHasNot+"|";
			tmp+="ALL: "+descHasAll+"|"; 
			tmp+="Bool: "+descHasBooleanData+"|"; 
			tmp+="NRRes: "+descHasNrRes+"";
			logger.warn(tmp);
			
			writeJamonLog(dir+"jamon"+exp+".html");
			reinitStat();
	}//endconduct
	
	public static void analyzeFiles(List<String> l){
		int countDoublettes = 0;
		SortedSet<String> differentIndividuals = new TreeSet<String>();
		for ( String file : l) {
			String fileContent = "";
			try{fileContent = Files.readFile(new File(exampleDir+file));
			}catch (Exception e) {
				 e.printStackTrace();
				 logger.warn(e);
			}
			ExampleContainer ec = new ExampleContainer(
					SetManipulation.stringToInd(getIndividuals(fileContent, true)),
					SetManipulation.stringToInd(getIndividuals(fileContent, false)));
			
			differentIndividuals.addAll(getIndividuals(fileContent, true));
			differentIndividuals.addAll(getIndividuals(fileContent, false));
			
			if(!ExampleContainer.add(ec)){
				countDoublettes++;
			}
		}
		logger.warn("found diff inds "+differentIndividuals.size());
		logger.warn("found doublettes " + countDoublettes);
		
	}
	
	public static OCEL experimentalSetup(Experiments exp,SortedSet<Individual> posExamples, SortedSet<Individual> negExamples ){
		OCEL la = null;
		if(exp.toString().contains("SPARQL"))
			la = prepareSparqlExperiment(exp, posExamples, negExamples);
		else if(exp.toString().contains("NORMAL")){
			if(exp.equals(Experiments.NORMAL_10000_CTESTS_FASTINST)){
				la = prepareNormalExperiment(true, posExamples, negExamples);
				la.setUseAllConstructor(false);
				la.setUseNegation(false);
				la.setUseCardinalityRestrictions(false);
			}else{
				la = prepareNormalExperiment(false, posExamples, negExamples);
			}
		}else {
			logger.error("undefined EXPERIMENT" + exp);
			System.exit(0);
			}
		
//		OCELConfigurator c = la.getConfigurator();
		
		//defaultSettings:
		la.setUseHasValueConstructor(false);
		la.setUseBooleanDatatypes(false);
		la.setUseDoubleDatatypes(false);
		

		if(exp.toString().contains("HASVALUE")){
			la.setUseHasValueConstructor(true);
		}
		
		
		if(exp.toString().contains("10s")){
			la.setMaxExecutionTimeInSeconds(10);
			la.setMinExecutionTimeInSeconds(10);
			
		}else if(exp.toString().contains("100s")){
			la.setMaxExecutionTimeInSeconds(100);
			la.setMinExecutionTimeInSeconds(100);
			
		}else if(exp.toString().contains("1000_CTESTS")){
			la.setMaxClassDescriptionTests(1000);
		}else if(exp.toString().contains("10000_CTESTS")){
			la.setMaxClassDescriptionTests(10000);
			
		}
		//la.getConfigurator();
		//appendtoFile
		
		return la;
	}
	
	
	public static OCEL prepareSparqlExperiment(Experiments exp, SortedSet<Individual> posExamples, SortedSet<Individual> negExamples){
		

		OCEL la = null;
		try{
			SortedSet<Individual> instances = new TreeSet<Individual>();
			instances.addAll(posExamples);
			instances.addAll(negExamples);
	
			SparqlKnowledgeSource ks = ComponentFactory
					.getSparqlKnowledgeSource(URI.create(
							"http://localhost:2020/bible").toURL(), SetManipulation
							.indToString(instances));
	
			SparqlKnowledgeSourceConfigurator c = ks.getConfigurator();
			
			c.setCloseAfterRecursion(true);
			c.setRecursionDepth(2);
			c.setPredefinedEndpoint("LOCALJOSEKIBIBLE");
			c.setUseLits(true);
			c.setGetAllSuperClasses(true);
			c.setGetPropertyInformation(true);
			c.setVerbosity("warning");
			
			if(exp.equals(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC2_NOPROP)){
				c.setGetPropertyInformation(false);
			}else if(exp.equals(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC2_NOCLOSE_NOPROP)){
				c.setCloseAfterRecursion(false);
				c.setGetPropertyInformation(false);
			}else if(exp.equals(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC1)){
				c.setRecursionDepth(1);
			}else if(exp.equals(Experiments.SPARQL_10000_CTESTS_SPECIAL_REC3)){
				c.setRecursionDepth(3);
			}
			
			Set<AbstractKnowledgeSource> tmp = new HashSet<AbstractKnowledgeSource>();
			tmp.add(ks);
			// reasoner
			OWLAPIReasoner f = ComponentFactory
					.getOWLAPIReasoner(tmp);
	
			// learning problem
			PosNegLPStandard lp = ComponentFactory.getPosNegLPStandard(f,
					SetManipulation.indToString(posExamples), SetManipulation
							.indToString(negExamples));
	
			// learning algorithm
			la = ComponentManager.getInstance().learningAlgorithm(OCEL.class, lp, f);
			la.setGuaranteeXgoodDescriptions(1);
			Config conf = new Config(ComponentManager.getInstance(), ks, f, lp, la);
			new ConfigSave(conf).saveFile(new File(tmpFilename));
			
		}catch (Exception e) {
			 e.printStackTrace();
			 logger.warn(e);
			 logger.warn("error in sparqlprepare");
			 flawInExperiment = true;
		}
		return la;
	}
	
	public static OCEL prepareNormalExperiment(boolean fic, SortedSet<Individual> posExamples, SortedSet<Individual> negExamples){
		OCEL la = null;
		try{
			SortedSet<Individual> instances = new TreeSet<Individual>();
			instances.addAll(posExamples);
			instances.addAll(negExamples);	
			
			URL fileURL = null;
			try {
				fileURL = new File(ontologyPath).toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				flawInExperiment = true;
			}
			OWLFile ks = ComponentFactory.getOWLFile( fileURL);
					
			Set<AbstractKnowledgeSource> tmp = new HashSet<AbstractKnowledgeSource>();
			tmp.add(ks);
			
			AbstractReasonerComponent f = null;
			
			// reasoner
			if(fic){
				f = ComponentFactory.getFastInstanceChecker(tmp);
				((FastInstanceChecker)f).setDefaultNegation(true);
				
			}else{
				f = ComponentFactory.getOWLAPIReasoner(tmp);
			}
//			ReasonerComponent rs = ComponentManager.getInstance().reasoningService(f);
	
//			 learning problem
			PosNegLPStandard lp = ComponentFactory.getPosNegLPStandard(f,
					SetManipulation.indToString(posExamples), SetManipulation
							.indToString(negExamples));
	
			// learning algorithm
			la = ComponentManager.getInstance().learningAlgorithm(OCEL.class, lp, f);
			la.setGuaranteeXgoodDescriptions(1);
			Config c = new Config(ComponentManager.getInstance(), ks, f, lp, la);
			new ConfigSave(c).saveFile(new File(tmpFilename));
			
		}catch (Exception e) {
			 e.printStackTrace();
			 logger.warn(e);
			 logger.warn("error in normalprepare");
			 flawInExperiment = true;
		}
		return la;
	}
	
	public static void initAllComponents(){
		ComponentManager cm = ComponentManager.getInstance();
		
		List<AbstractComponent> l = new ArrayList<AbstractComponent>();
		l.addAll(cm.getLiveComponents());
		
		for(AbstractComponent c : l){
			
			try{
			SimpleClock time = new SimpleClock();
			c.init();
			if (c instanceof SparqlKnowledgeSource) {
				ksinitializationTime.addNumber((double) time.getTime()/1000);
			}else if (c instanceof OWLFile) {
				ksinitializationTime.addNumber((double) time.getTime()/1000);
			}else if (c instanceof FastInstanceChecker) {
				reasonerInitializationTime.addNumber((double) time.getTime()/1000);
			}else if (c instanceof OWLAPIReasoner) {
				reasonerInitializationTime.addNumber((double) time.getTime()/1000);
			}
			
			}catch (Exception e) {
				 e.printStackTrace();
				 logger.warn(e);
				 logger.warn("error in initAllComponents");
				 flawInExperiment = true;
			}
		}// end for
		
		
	}
	
	public static void prepareTable(){
		t = new Table("sembib");
		String[] labString = new String[]{
				"count",
				"accuracy on fragment(%)",
				"accuracy on whole (%)",
				"accuracy pos examples on whole (%)",
				"accuracy neg examples on whole (%)",
				"extraction/parsing time",
				"reasoner initialization time",
				"learning time",
				"number of axioms",
				"description length",
				"description depth"
		}; //9
		TableColumn labels = new TableColumn("Semantic Bible",labString);
		t.addColumn(labels);
		Files.mkdir(tableDir);
		//Table.serializeColumns(t, tableDir, tableFile);
		//Files.createFile(new File(tableLatex), t.getLatexString());
		
	}
	
	public static void fillTable(Experiments exp, int count){
		String[] columnString = new String[]{
				count+"",
				StringFormatter.convertStatPercentageToLatex(accFragment, 1, false, true),
				StringFormatter.convertStatPercentageToLatex(accOnOnto, 1, false, true),
				StringFormatter.convertStatPercentageToLatex(accPosExOnOnto, 1, false, true),
				StringFormatter.convertStatPercentageToLatex(accNegExOnOnto, 1, false, true),
				StringFormatter.convertStatDoubleToLatex(ksinitializationTime, 1, "", "s", true),
				StringFormatter.convertStatDoubleToLatex(reasonerInitializationTime, 1, "", "s", true),
				StringFormatter.convertStatDoubleToLatex(learningTime, 1, "", "s", true),
				StringFormatter.convertStatDoubleToLatex(nrOfExtractedAxioms, 0, "", "", true),
				StringFormatter.convertStatDoubleToLatex(descLength, 1, "", "", true),
				StringFormatter.convertStatDoubleToLatex(descDepth, 1, "", "", true),
		}; //9
		t.removeColumn(exp.toString());
		t.addColumn(new TableColumn(exp.toString(),columnString));
		String expId = exp.toString()+count;
		Table.serializeColumns(t, tableDir+expId,  tableDir+expId+File.separator+"table");
		Files.createFile(new File(tableDir+expId+File.separator+"table.tex"), t.getLatexString());
		
		
	}
	
	
	
	private static void reinitStat(){
		accFragment = new Stat();
		accOnOnto = new Stat();
		accPosExOnOnto = new Stat();
		accNegExOnOnto = new Stat();
		ksinitializationTime = new Stat();
		reasonerInitializationTime = new Stat();
		learningTime = new Stat();
		nrOfExtractedAxioms = new Stat();
		descLength = new Stat();
		descDepth = new Stat();
	}
	
	

	
	public static EvaluatedDescriptionPosNeg reEvaluateDescription(Description d, SortedSet<Individual> retrieved ,SortedSet<Individual> posEx ,SortedSet<Individual> negEx ){
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
		
		
		return new EvaluatedDescriptionPosNeg(d, PosAsPos, PosAsNeg, NegAsPos,NegAsNeg);
		
	}
	
	public static AbstractCELA getLearningAlgorithm(){
		ComponentManager cm =ComponentManager.getInstance();
		
		List<AbstractComponent> comp = cm.getLiveComponents();
		for (AbstractComponent component : comp) {
			if(component instanceof AbstractCELA){
				return (AbstractCELA) component;
			}
			
		}
		return null;
	}
	
	
	public static List<String>  getFiles(){
			String actualDir = exampleDir;
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

		// logger 1 is the console, where we print only info messages;
		// the logger is plain, i.e. does not output log level etc.
		Layout layout = new PatternLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		consoleAppender.setThreshold(Level.WARN);
		
		// logger 2 is writes to a file; it records all debug messages
		// and includes the log level
		Layout layout2 = new SimpleLayout();
		FileAppender fileAppenderNormal = null;
		File f = new File("log/sparql.txt");
		try {
		    	fileAppenderNormal = new FileAppender(layout2, "log/semBibleLog.txt", false);
		    	f.delete();
		    	f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppenderNormal);
		logger.setLevel(Level.DEBUG);

		

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



	
}
