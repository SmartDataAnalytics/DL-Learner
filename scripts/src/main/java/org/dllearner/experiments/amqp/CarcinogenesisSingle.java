package org.dllearner.experiments.amqp;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.AccMethodPredAcc;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class CarcinogenesisSingle {

	public static void main(String[] args) throws OWLOntologyCreationException, ComponentInitException {
		List<String> expectedArgs = Arrays.asList(
				"<path to ont file>",
				"<max exec time>",
				"<noise percent>",
				"<search tree file>"
		);
		if (args.length < expectedArgs.size()) {
			StringBuilder err = new StringBuilder();
			err.append("usage: ");

			for (String arg : expectedArgs) {
				err.append(arg);
				err.append(" ");
			}

			System.err.println(err);
			System.exit(1);
		}

		String ontFilePath = args[0];
		int runtimeInSecs = Integer.parseInt(args[1]);
		int noisePercent = Integer.parseInt(args[2]);
		String searchTreeFilePath = args[3];

		Set<OWLIndividual> pos = CarcUtils.buildPos();
		Set<OWLIndividual> neg = CarcUtils.buildNeg();

		// -------------- knowledge source + reasoner ------------------------

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = man.loadOntologyFromOntologyDocument(new File(ontFilePath));

		KnowledgeSource ks = new OWLAPIOntology(ont);
		ks.init();

		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
		baseReasoner.init();

		ClosedWorldReasoner reasoner = new ClosedWorldReasoner(baseReasoner);
		reasoner.init();

		// --------------------operator ---------------------------------------
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.init();

		// ------------------- learning problem -------------------------------

		PosNegLPStandard lp = new PosNegLPStandard(reasoner);
		lp.setPositiveExamples(pos);
		lp.setNegativeExamples(neg);
		lp.setAccuracyMethod(new AccMethodPredAcc());
		lp.init();

		CELOE la = new CELOE();
		la.setLearningProblem(lp);
		la.setMaxExecutionTimeInSeconds(runtimeInSecs);
		la.setNoisePercentage(noisePercent / 100.);
		la.setOperator(op);
		la.setReasoner(reasoner);
		la.setSearchTreeFile(searchTreeFilePath);
		la.setWriteSearchTree(true);
		la.init();

		la.start();
		la.stop();
	}



}
