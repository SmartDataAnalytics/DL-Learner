/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
package org.dllearner.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.xmlbeans.XmlObject;
import org.dllearner.Info;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.configuration.util.SpringConfigurationXMLBeanConverter;
import org.dllearner.confparser3.ConfParserConfiguration;
import org.dllearner.confparser3.ParseException;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.utilities.Files;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * 
 * New commandline interface.
 * 
 * @author Jens Lehmann
 *
 */
public class CLI {

	private static Logger logger = Logger.getLogger(CLI.class);
	private static Logger rootLogger = Logger.getRootLogger();
	
	private boolean writeSpringConfiguration = false;
	private ApplicationContext context;
	
	public CLI(File file) throws IOException{
		Resource confFile = new FileSystemResource(file);
		
		List<Resource> springConfigResources = new ArrayList<Resource>();

        //DL-Learner Configuration Object
        IConfiguration configuration = new ConfParserConfiguration(confFile);

        ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
        context =  builder.buildApplicationContext(configuration,springConfigResources);
        
        // a lot of debugging stuff
//        FastInstanceChecker fi = context.getBean("reasoner", FastInstanceChecker.class);
//        System.out.println(fi.getClassHierarchy());
//        NamedClass male = new NamedClass("http://localhost/foo#male");
//        System.out.println(fi.getIndividuals(new NamedClass("http://localhost/foo#male")));
//        System.out.println(fi.getIndividuals().size());
//        System.out.println("has type: " + fi.hasTypeImpl(male, new Individual("http://localhost/foo#bernd")));
//        
//        PosNegLPStandard lp = context.getBean("lp", PosNegLPStandard.class);
//        System.out.println(lp.getPositiveExamples());
//        System.out.println(lp.getNegativeExamples());
//        System.out.println(lp.getAccuracy(new NamedClass("http://localhost/foo#male")));
    
        // get a CLI bean if it exists
        CLI cli = null;
        if(context.getBeansOfType(CLI.class).size()>0) {
        	System.out.println();
        	cli = context.getBean(CLI.class);
        	SpringConfigurationXMLBeanConverter converter = new SpringConfigurationXMLBeanConverter();
        	XmlObject xml = converter.convert(configuration);
        	String springFilename = file.getCanonicalPath().replace(".conf", ".xml");
        	File springFile = new File(springFilename);
        	if(springFile.exists()) {
        		logger.warn("Cannot write Spring configuration, because " + springFilename + " already exists.");
        	} else {
        		Files.createFile(springFile, xml.toString());
        	}
//        	SpringConfigurationXMLBeanConverter converter;
        }
        
        // start algorithm in conf file
//        LearningAlgorithm algorithm = context.getBean("alg",LearningAlgorithm.class);
//        algorithm.start();
    }

    public void run() { // ApplicationContext context, String algorithmBeanName){
    	LearningAlgorithm algorithm = context.getBean(LearningAlgorithm.class);
//        LearningAlgorithm algorithm = context.getBean(algorithmBeanName, LearningAlgorithm.class);
        algorithm.start();
    }

    public boolean isWriteSpringConfiguration() {
		return writeSpringConfiguration;
	}

	public void setWriteSpringConfiguration(boolean writeSpringConfiguration) {
		this.writeSpringConfiguration = writeSpringConfiguration;
	}    
    
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ReasoningMethodUnsupportedException 
	 */
	public static void main(String[] args) throws ParseException, IOException, ReasoningMethodUnsupportedException {
		
		Layout layout = new PatternLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		rootLogger.removeAllAppenders();
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.INFO);
		
//		System.out.println("DL-Learner " + Info.build + " [TODO: read pom.version and put it here (make sure that the code for getting the version also works in the release build!)] command line interface");
		System.out.println("DL-Learner command line interface");
		
		// currently, CLI has exactly one parameter - the conf file
		if(args.length == 0) {
			System.out.println("You need to give a conf file as argument.");
			System.exit(0);
		}
		
		// read file and print and print a message if it does not exist
		File file = new File(args[args.length - 1]);
		if(!file.exists()) {
			System.out.println("File \"" + file + "\" does not exist.");
			System.exit(0);			
		}
		
        CLI cli = new CLI(file);
        cli.run();
	}

	public ApplicationContext getContext() {
		return context;
	}

}
