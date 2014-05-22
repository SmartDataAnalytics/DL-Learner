/**
 * 
 */
package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.algorithms.qtl.QTL2Disjunctive;
import org.dllearner.algorithms.qtl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.QueryTreeHeuristic;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.cli.CLI;
import org.dllearner.cli.CrossValidation;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.learningproblems.PosNegLP;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class QTLEvaluation {
	
	int nrOfFolds = 10;
	private int nrOfPosExamples = 300;
	private int nrOfNegExamples = 300;
	
	CLI carcinogenesis = new CLI(new File("../test/qtl/carcinogenesis/train.conf"));
	CLI mammographic = new CLI(new File("../test/qtl/mammographic/train.conf"));
	CLI suramin = new CLI(new File("../test/qtl/suramin/train.conf"));
	CLI heart = new CLI(new File("../test/qtl/heart/train.conf"));
	CLI breasttissue = new CLI(new File("../test/qtl/breasttissue/train1.conf"));
	CLI parkinsons = new CLI(new File("../test/qtl/parkinsons/train.conf"));
	CLI mutagenesis = new CLI(new File("../test/qtl/mutagenesis/train1.conf"));
	
	private Model model;
	private OWLOntology ontology;
	private QueryTreeFactory<String> queryTreeFactory;
	private PosNegLP lp;

	
	
	public QTLEvaluation() throws ComponentInitException, IOException {
		queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.setMaxDepth(3);
		
//		loadDataset();
		
		loadExamples();
	}
	
	private void loadDataset(){
		File file = new File("../examples/carcinogenesis/carcinogenesis.owl");
		model = ModelFactory.createDefaultModel();
		try {
			model.read(new FileInputStream(file), null, "RDF/XML");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		try {
			ontology = man.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	private void loadExamples() throws ComponentInitException, IOException{
		
//		cli.init();
//		lp = (PosNegLP) cli.getLearningProblem();
		
		// get examples and shuffle them
//		List<Individual> posExamples = new LinkedList<Individual>(((PosNegLP)lp).getPositiveExamples());
//		Collections.shuffle(posExamples, new Random(1));			
//		List<Individual> negExamples = new LinkedList<Individual>(((PosNegLP)lp).getNegativeExamples());
//		Collections.shuffle(negExamples, new Random(2));
//		posExamples = posExamples.subList(0, Math.min(posExamples.size(), nrOfPosExamples));
//		negExamples = negExamples.subList(0, Math.min(negExamples.size(), nrOfNegExamples));
//		
//		Set<Individual> posSet = new TreeSet<Individual>(
//				NestedCrossValidation.getFolds(NestedCrossValidation.getFolds(posExamples, 3).get(0).getTrainList(), 3).get(0).getTrainList());
//		Set<Individual> negSet = new TreeSet<Individual>(
//				NestedCrossValidation.getFolds(NestedCrossValidation.getFolds(negExamples, 3).get(0).getTrainList(), 3).get(0).getTrainList());
//		
//		
//		
//		this.lp = new PosNegLPStandard();
//		this.lp.setPositiveExamples(posSet);
//		this.lp.setNegativeExamples(negSet);
	}
	
	public void run(boolean multiThreaded) throws ComponentInitException, LearningProblemUnsupportedException, IOException{
		Layout layout = new PatternLayout("%m%n");
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.ERROR);
		Logger.getLogger("org.dllearner.algorithms").setLevel(Level.INFO);
		Logger.getLogger("org.dllearner.scripts").setLevel(Level.INFO);
		
		FileAppender fileAppender = new FileAppender(layout, "log/qtl-eval.log", false);
		logger.addAppender(fileAppender);
		fileAppender.setThreshold(Level.INFO);
		// disable OWL API info output
		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.WARNING);
		long startTime = System.currentTimeMillis();
//		FastInstanceChecker reasoner = new FastInstanceChecker(new OWLAPIOntology(ontology));
//		reasoner.init();
//		lp.setReasoner(reasoner);
//		lp.init();
		CrossValidation.outputFile = new File("log/qtl-cv.log");
		CrossValidation.writeToFile = true;
//		CrossValidation.multiThreaded = multiThreaded;
		CrossValidation.multiThreaded = true;
		
		StringBuilder sb = new StringBuilder();
		CLI[] clis = new CLI[]{
				carcinogenesis, 
				breasttissue, 
				heart, 
				parkinsons, 
				mammographic, 
				mutagenesis
				};
//		clis = new CLI[]{parkinsons};
		ArrayList<HeuristicType> heuristics = Lists.newArrayList(
				HeuristicType.MATTHEWS_CORRELATION, 
				HeuristicType.PRED_ACC, 
				HeuristicType.FMEASURE
//				HeuristicType.ENTROPY
				);
		double start = 0.1;
		double end = 1.0;
		double stepsize = 0.1;
		for (CLI cli : clis) {
			sb.append("############################################\n");
			sb.append(cli.getConfFile().getPath()).append("\n");
			cli.init();
			lp = (PosNegLP) cli.getLearningProblem();
			AbstractReasonerComponent reasoner = cli.getReasonerComponent();
			QTL2Disjunctive la = new QTL2Disjunctive(lp, reasoner);
			QueryTreeHeuristic heuristic = new QueryTreeHeuristic();
			la.setHeuristic(heuristic);
			
			for(HeuristicType heuristicType : heuristics){
				sb.append("Heuristic: " + heuristicType.name()).append("\n");
				sb.append("Beta\t\tF-measure\t\tAccuracy").append("\n");
				heuristic.setHeuristicType(heuristicType);
				for(double beta = start; beta <= end; beta +=stepsize){
					la.setBeta(Math.round(beta * 10d)/10d);
					la.init();
					CrossValidation cv = new CrossValidation(la, lp, reasoner, nrOfFolds, false);
					sb.append(Math.round(beta * 10d)/10d + "\t\t" + cv.getfMeasure().getMean() + "\t\t" + cv.getAccuracy().getMean()).append("\n");
				}
				sb.append("******************************\n");
			}
		}
		System.out.println(sb.toString());
		
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) + "ms");
	}

	
	public static void main(String[] args) throws Exception {
		boolean multiThreaded = Boolean.valueOf(args[0]);
		new QTLEvaluation().run(multiThreaded);
	}

}
