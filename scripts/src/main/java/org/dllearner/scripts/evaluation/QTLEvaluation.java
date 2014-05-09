/**
 * 
 */
package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.QTL2Disjunctive;
import org.dllearner.algorithms.qtl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.cli.CLI;
import org.dllearner.cli.CrossValidation;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.scripts.NestedCrossValidation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class QTLEvaluation {
	
	int nrOfFolds = 3;
	private int nrOfPosExamples = 300;
	private int nrOfNegExamples = 300;
	
	CLI cli = new CLI(new File("../test/qtl/carcinogenesis/train.conf"));
	
	private Model model;
	private OWLOntology ontology;
	private QueryTreeFactory<String> queryTreeFactory;
	private PosNegLP lp;

	
	
	public QTLEvaluation() throws ComponentInitException, IOException {
		queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.setMaxDepth(3);
		
		loadDataset();
		
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
		
		cli.init();
		lp = (PosNegLP) cli.getLearningProblem();
		
		// get examples and shuffle them
		List<Individual> posExamples = new LinkedList<Individual>(((PosNegLP)lp).getPositiveExamples());
		Collections.shuffle(posExamples, new Random(1));			
		List<Individual> negExamples = new LinkedList<Individual>(((PosNegLP)lp).getNegativeExamples());
		Collections.shuffle(negExamples, new Random(2));
		posExamples = posExamples.subList(0, Math.min(posExamples.size(), nrOfPosExamples));
		negExamples = negExamples.subList(0, Math.min(negExamples.size(), nrOfNegExamples));
		
		Set<Individual> posSet = new TreeSet<Individual>(
				NestedCrossValidation.getFolds(NestedCrossValidation.getFolds(posExamples, 3).get(0).getTrainList(), 3).get(0).getTrainList());
		Set<Individual> negSet = new TreeSet<Individual>(
				NestedCrossValidation.getFolds(NestedCrossValidation.getFolds(negExamples, 3).get(0).getTrainList(), 3).get(0).getTrainList());
		
		
		
		this.lp = new PosNegLPStandard();
		this.lp.setPositiveExamples(posSet);
		this.lp.setNegativeExamples(negSet);
	}
	
	public void run(boolean multiThreaded) throws ComponentInitException, LearningProblemUnsupportedException{
		long startTime = System.currentTimeMillis();
		FastInstanceChecker reasoner = new FastInstanceChecker(new OWLAPIOntology(ontology));
		reasoner.init();
		lp.setReasoner(reasoner);
		lp.init();
		QTL2Disjunctive la = new QTL2Disjunctive(lp, reasoner);
		la.setBeta(0.5);
		la.init();
		la.start();
		
		CrossValidation.outputFile = new File("log/qtl-cv.log");
		CrossValidation.writeToFile = true;
		CrossValidation.multiThreaded = multiThreaded;
//		CrossValidation cv = new CrossValidation(la, lp, reasoner, nrOfFolds, false);
		long endTime = System.currentTimeMillis();
		System.err.println((endTime - startTime) + "ms");
	}

	
	public static void main(String[] args) throws Exception {
		boolean multiThreaded = Boolean.valueOf(args[0]);
		new QTLEvaluation().run(multiThreaded);
	}

}
