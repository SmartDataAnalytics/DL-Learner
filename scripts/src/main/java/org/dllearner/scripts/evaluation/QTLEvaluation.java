/**
 * 
 */
package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.algorithms.celoe.CELOE;
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
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann
 *
 */
public class QTLEvaluation {
	
	private static final Logger logger = Logger.getLogger(QTLEvaluation.class.getName());
	
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

	double start = 1.0;
	double end = 2.0;
	double stepsize = 1.0;
	
	ArrayList<HeuristicType> heuristics = Lists.newArrayList(
//			HeuristicType.MATTHEWS_CORRELATION, 
			HeuristicType.PRED_ACC 
//			HeuristicType.FMEASURE
//			HeuristicType.ENTROPY
			);
	
	CLI[] clis = new CLI[]{
//			carcinogenesis, 
//			breasttissue, 
//			heart, 
			parkinsons, 
//			mammographic, 
//			mutagenesis
			};
	private Map<HeuristicType, Map<Double, Stat>> fMeasureStats;
	private Map<HeuristicType, Map<Double, Stat>> accuracyStats;

	private int maxExecutionTimeInSeconds = 60;
	
	
	public QTLEvaluation() throws ComponentInitException, IOException {
		init();
		
//		loadDataset();
		
		loadExamples();
	}
	
	private void init() throws IOException{
		queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.setMaxDepth(3);
		
		fMeasureStats = new HashMap<HeuristicType, Map<Double,Stat>>();
		for (HeuristicType h : heuristics) {
			Map<Double, Stat> beta2Stat = new TreeMap<Double, Stat>();
			fMeasureStats.put(h, beta2Stat);
			for(double beta = start; beta <= end; beta +=stepsize){
				Double val = Math.round(beta * 10d)/10d;
				beta2Stat.put(val, new Stat());
			}
		}
		
		accuracyStats = new HashMap<HeuristicType, Map<Double,Stat>>();
		for (HeuristicType h : heuristics) {
			Map<Double, Stat> beta2Stat = new TreeMap<Double, Stat>();
			accuracyStats.put(h, beta2Stat);
			for(double beta = start; beta <= end; beta +=stepsize){
				Double val = Math.round(beta * 10d)/10d;
				beta2Stat.put(val, new Stat());
			}
		}
		
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
		
//		FastInstanceChecker reasoner = new FastInstanceChecker(new OWLAPIOntology(ontology));
//		reasoner.init();
//		lp.setReasoner(reasoner);
//		lp.init();
		CrossValidation.outputFile = new File("log/qtl-cv.log");
		CrossValidation.writeToFile = true;
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
		long startTime = System.currentTimeMillis();
		CrossValidation.multiThreaded = multiThreaded;
		StringBuilder sb = new StringBuilder();
		
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
					double val = Math.round(beta * 10d)/10d;
					la.setBeta(val);
					la.init();
					CrossValidation cv = new CrossValidation(la, lp, reasoner, nrOfFolds, false);
					sb.append(Math.round(beta * 10d)/10d + "\t\t" + cv.getfMeasure().getMean() + "\t\t" + cv.getAccuracy().getMean()).append("\n");
					fMeasureStats.get(heuristicType).get(val).add(cv.getfMeasure());
					accuracyStats.get(heuristicType).get(val).add(cv.getAccuracy());
				}
				sb.append("******************************\n");
			}
			
			//run CELOE algorithm for comparison
			//TODO: adjust parameters
//			CELOE celoe = new CELOE(lp, reasoner);
//			celoe.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
//			RhoDRDown op = new RhoDRDown();
//			op.setReasoner(reasoner);
//			op.setUseNegation(false);
//			op.init();
//			celoe.init();
//			CrossValidation cv = new CrossValidation(celoe, lp, reasoner, nrOfFolds, false);
//			sb.append("CELOE\n");
//			sb.append("F-measure\t\tAccuracy").append("\n");
//			sb.append(cv.getfMeasure().getMean() + "\t\t" + cv.getAccuracy().getMean()).append("\n");
		}
		
		long endTime = System.currentTimeMillis();
		logger.info((endTime - startTime) + "ms");
		
		//write stats for each heuristic and each dataset
		logger.info(sb.toString());
		
		//write total stats by heuristic
		String s = "";
		for (Entry<HeuristicType, Map<Double, Stat>> entry : accuracyStats.entrySet()) {
			HeuristicType h = entry.getKey();
			s += "Heuristic: " + h.name() + "\n";
			Map<Double, Stat> param2AccuracyStat = entry.getValue();
			s += "Beta\t\tF-measure\t\tAccuracy\n";
			for (Entry<Double, Stat> entry2 : param2AccuracyStat.entrySet()) {
				Double param = entry2.getKey();
				Stat accuracyStat = entry2.getValue();
				Stat fMeasureStat = fMeasureStats.get(h).get(param);
				s += param + "\t\t" + fMeasureStat.getMean() + "\t\t" + accuracyStat.getMean() + "\n";
			}
			
		}
		logger.info("TOTAL\n:" + s);
	}

	
	public static void main(String[] args) throws Exception {
		boolean multiThreaded = Boolean.valueOf(args[0]);
		new QTLEvaluation().run(multiThreaded);
	}

}
