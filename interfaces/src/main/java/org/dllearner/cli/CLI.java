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

import org.apache.log4j.Level;
import org.dllearner.algorithms.decisiontrees.dsttdt.DSTTDTClassifier;
import org.dllearner.algorithms.decisiontrees.refinementoperators.DLTreesRefinementOperator;
import org.dllearner.algorithms.decisiontrees.tdt.TDTClassifier;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser.ConfParserConfiguration;
import org.dllearner.confparser.ParseException;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.refinementoperators.RefinementOperator;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//import org.dllearner.algorithms.qtl.QTL2;

/**
 * 
 * New commandline interface.
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "Command Line Interface", version = 0, shortName = "")
public class CLI extends CLIBase2 {

	private static Logger logger = LoggerFactory.getLogger(CLI.class);

	private LearningAlgorithm algorithm;
	private KnowledgeSource knowledgeSource;
	
	// some CLI options
	@ConfigOption(defaultValue = "false", description = "Run in Cross-Validation mode")
	private boolean performCrossValidation = false;
	@ConfigOption(defaultValue = "10", description = "Number of folds in Cross-Validation mode")
	private int nrOfFolds = 10;

	private AbstractClassExpressionLearningProblem lp;

	private AbstractReasonerComponent rs;

	private AbstractCELA la;
	

	public CLI() {
		
	}
	
	public CLI(File confFile) {
		this();
		this.confFile = confFile;
	}
	
	// separate init methods, because some scripts may want to just get the application
	// context from a conf file without actually running it
	@Override
	public void init() throws IOException {
    	if(context == null) {
		    super.init();
            
            knowledgeSource = context.getBean(KnowledgeSource.class);
            rs = getMainReasonerComponent();
    		la = context.getBean(AbstractCELA.class);
    		lp = context.getBean(AbstractClassExpressionLearningProblem.class);
    	}
	}
	
    @Override
    public void run() {
    	try {
			org.apache.log4j.Logger.getLogger("org.dllearner").setLevel(Level.toLevel(logLevel.toUpperCase()));
		} catch (Exception e) {
			logger.warn("Error setting log level to " + logLevel);
		}

		rs = getMainReasonerComponent();
		
		
			if (performCrossValidation) {
				la = context.getBeansOfType(AbstractCELA.class).entrySet().iterator().next().getValue();
				
				PosNegLP lp = context.getBean(PosNegLP.class);
//				if(la instanceof QTL2){
//					//new SPARQLCrossValidation((QTL2Disjunctive) la,lp,rs,nrOfFolds,false);
//				}
				if((la instanceof TDTClassifier)||(la instanceof DSTTDTClassifier) ){
					
					//TODO:  verify if the quality of the code can be improved
					RefinementOperator op = context.getBeansOfType(DLTreesRefinementOperator.class).entrySet().iterator().next().getValue();
					ArrayList<OWLClass> concepts = new ArrayList<>(rs.getClasses());
					((DLTreesRefinementOperator) op).setAllConcepts(concepts);
					
					ArrayList<OWLObjectProperty> roles = new ArrayList<>(rs.getAtomicRolesList());
					((DLTreesRefinementOperator) op).setAllConcepts(concepts);
					((DLTreesRefinementOperator) op).setAllRoles(roles);
					((DLTreesRefinementOperator) op).setReasoner(getMainReasonerComponent());
					
					if (la instanceof TDTClassifier)
					    ((TDTClassifier)la).setOperator(op);
					else
						((DSTTDTClassifier)la).setOperator(op);
					new CrossValidation2(la,lp,rs,nrOfFolds,false);
				}else {
					new CrossValidation2(la,lp,rs,nrOfFolds,false);
				}
			} else {
				if(context.getBean(AbstractLearningProblem.class) instanceof AbstractClassExpressionLearningProblem) {
					lp = context.getBean(AbstractClassExpressionLearningProblem.class);
				} else {
					
				}
				
				Map<String, LearningAlgorithm> learningAlgs = context.getBeansOfType(LearningAlgorithm.class);
//				knowledgeSource = context.getBeansOfType(Knowledge1Source.class).entrySet().iterator().next().getValue();
				for(Entry<String, LearningAlgorithm> entry : learningAlgs.entrySet()){
					algorithm = entry.getValue();
					logger.info("Running algorithm instance \"" + entry.getKey() + "\" (" + algorithm.getClass().getSimpleName() + ")");
					algorithm.start();
				}
			}
    }

	/**
	 * @return the lp
	 */
	public AbstractClassExpressionLearningProblem getLearningProblem() {
		return lp;
	}
	
	/**
	 * @return the rs
	 */
	public AbstractReasonerComponent getReasonerComponent() {
		return rs;
	}
	
	/**
	 * @return the la
	 */
	public AbstractCELA getLearningAlgorithm() {
		return la;
	}
    
	/**
	 * @param args
	 * @throws ParseException
	 * @throws IOException
	 * @throws ReasoningMethodUnsupportedException
	 */
	public static void main(String[] args) throws ParseException, IOException, ReasoningMethodUnsupportedException {
		
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
		
		Resource confFile = new FileSystemResource(file);
		
		List<Resource> springConfigResources = new ArrayList<>();

        try {
            //DL-Learner Configuration Object
            IConfiguration configuration = new ConfParserConfiguration(confFile);

            ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
            ApplicationContext context =  builder.buildApplicationContext(configuration,springConfigResources);

            // TODO: later we could check which command line interface is specified in the conf file
            // for now we just use the default one

            CLIBase2 cli;
            if(context.containsBean("cli")) {
                cli = (CLIBase2) context.getBean("cli");
            } else {
                cli = new CLI();
            }
            cli.setContext(context);
            cli.setConfFile(file);
            cli.run();
        } catch (Exception e) {
            String stacktraceFileName = "log/error.log";
            
            //Find the primary cause of the exception.
            Throwable primaryCause = findPrimaryCause(e);

            // Get the Root Error Message
            logger.error("An Error Has Occurred During Processing.");
            if (primaryCause != null) {
            	logger.error(primaryCause.getMessage());
            }
            logger.debug("Stack Trace: ", e);
            logger.error("Terminating DL-Learner...and writing stacktrace to: " + stacktraceFileName);
            createIfNotExists(new File(stacktraceFileName));

            FileOutputStream fos = new FileOutputStream(stacktraceFileName);
            PrintStream ps = new PrintStream(fos);
            e.printStackTrace(ps);
        }
    }

	public boolean isPerformCrossValidation() {
		return performCrossValidation;
	}

	public void setPerformCrossValidation(boolean performCrossValiation) {
		this.performCrossValidation = performCrossValiation;
	}

	public int getNrOfFolds() {
		return nrOfFolds;
	}

	public void setNrOfFolds(int nrOfFolds) {
		this.nrOfFolds = nrOfFolds;
	}

	//	public LearningAlgorithm getLearningAlgorithm() {
//		return algorithm;
//	}
	
	public KnowledgeSource getKnowledgeSource() {
		return knowledgeSource;
	}
	
}
