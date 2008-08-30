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
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.cli.Start;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderOWL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.dllearner.utilities.owl.ReasoningServiceFactory;
import org.dllearner.utilities.owl.ReasoningServiceFactory.AvailableReasoners;
import org.dllearner.utilities.statistics.SimpleClock;

public class SemanticBible2 {

	private static ReasoningService reasoningService;

	private static Logger logger = Logger.getRootLogger();

	
	public static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";
	public static String dir = "sembib/";
	public static String sparqldir = dir+"sparql/";
	public static String normaldir = dir+"normal/";
	
	public static String tmpFilename = dir + "tmp.conf";
	
	//private static Class usedReasoner = FastInstanceChecker.class;
	
	private static boolean useSPARQL = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleClock sc = new SimpleClock();
		initLogger();
		logger.info("Start");
		File tmpFile = new File(tmpFilename);
		
		List<File> confs = getFilesContaining(useSPARQL,"ten","all", "99+"); 
		System.out.println(confs);
		//reasoningService = ReasoningServiceFactory.getReasoningService(ontologyPath, AvailableReasoners.OWLAPIREASONERPELLET);
		
		try{
		for (File file : confs) {
			System.out.println(file.getAbsolutePath());
			StringBuffer sbuf = new StringBuffer(Files.readFile( file));
			sbuf.insert(0, (useSPARQL)?sparqlOptions():normalOptions());
			Files.createFile(tmpFile, sbuf.toString());
			//System.out.println(tmpFile.getCanonicalPath());
			
			Start.main(new String[] { tmpFilename });
			ComponentManager cm =ComponentManager.getInstance();
			List<Component> comp = cm.getLiveComponents();
			for (Component component : comp) {
				System.out.println(component.getClass().getCanonicalName());
				if(component instanceof LearningAlgorithm){
					System.out.println("yyyy");
					System.exit(0);
				}
				
			}
			
			
			Cache.getDefaultCache().clearCache();
			cm.freeAllComponents();
			System.exit(0);
		}//end for
		}catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("finished");
	}
	
	public static List<File> getFilesContaining(boolean sparql, String numExamples, String allOrEx, String acc) {
		List<File> ret = new ArrayList<File>();
		try{
			String actualDir = (sparql)?sparqldir:normaldir;
			System.out.println(actualDir);
			File f = new File(actualDir);
		    String[] files = f.list();
		    
		    for (int i = 0; i < files.length; i++) {
		    	System.out.println(files[i]);
				if(
						files[i].contains(numExamples) 
						&& files[i].contains(allOrEx)
						&& files[i].contains(acc)
						){
					ret.add(new File(actualDir+files[i]));
				}
					
			}    
		}catch (Exception e) {
			logger.warn("deleting cache failed");
			e.printStackTrace();
		}
	    return ret;
	}
	
	
	public static String sparqlOptions (){
		String s="// SPARQL options\n"+
			"sparql.recursionDepth = 3;\n"+
			"sparql.useLits = true;\n"+
			"sparql.predefinedEndpoint = \"LOCALJOSEKIBIBLE\";\n"+
			"import(\"lalala\",\"SPARQL\");\n"+

			"algorithm = refexamples;\n"+
			"refexamples.useAllConstructor = true;\n"+
			"refexamples.useNegation = true;\n"+
			"refexamples.useCardinalityRestrictions = true;\n"+
			"refexamples.guaranteeXgoodDescriptions = 1;\n"+
			"refexamples.maxExecutionTimeInSeconds = 1;\n"+
			"\n"+
			"reasoner = owlAPI;\n"+
			//"reasoner = fastInstanceChecker;\n"+
			//"owlAPIReasoner.reasonerType = pellet;\n\n";
			"";
		return s;
	}
	
	public static String normalOptions (){
		String s="\n"+
			"import(\"NTNcombined.owl\");\n"+
			"algorithm = refexamples;\n"+
			"refexamples.useAllConstructor = true;\n"+
			"refexamples.useNegation = true;\n"+
			"refexamples.useCardinalityRestrictions = true;\n"+
			"refexamples.guaranteeXgoodDescriptions = 1;\n"+
			"\n"+
			"reasoner = owlAPI;\n"+
			//"reasoner = fastInstanceChecker;\n"+
			//"owlAPIReasoner.reasonerType = pellet;\n\n";
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
		logger.setLevel(Level.DEBUG);
		

	}

	
}
