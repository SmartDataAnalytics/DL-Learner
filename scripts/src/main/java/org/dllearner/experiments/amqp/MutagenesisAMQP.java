package org.dllearner.experiments.amqp;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.distributed.amqp.CELOE;
import org.dllearner.distributed.amqp.OEHeuristicRuntime;
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

public class MutagenesisAMQP {

	public static void main(String[] args) throws OWLOntologyCreationException, ComponentInitException {
		List<String> expectedArgs = Arrays.asList(
				"<worker ID>",
				"<is master (1)/is not master (0)>",
				"<path to amqp config>",
				"<path to ont file>",
				"<max master exec time/worker base time>",
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

		int id = Integer.parseInt(args[0]);
		int isMaster = Integer.parseInt(args[1]);
		String amqpConfFilePath = args[2];
		String ontFilePath = args[3];
		int runtimeInSecsOrWorkerBaseTime = Integer.parseInt(args[4]);
		int noisePercent = Integer.parseInt(args[5]);
		String searchTreeFilePath = args[6];

		Set<OWLIndividual> pos = MutagUtils.buildPos();
		Set<OWLIndividual> neg = MutagUtils.buildNeg();

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
		lp.setPercentPerLengthUnit(0);
		lp.init();

		// ------------------ learning algorithm -----------------------------
		OEHeuristicRuntime h = new OEHeuristicRuntime();
		h.setExpansionPenaltyFactor(0);
		h.setStartNodeBonus(0);
		h.setNodeRefinementPenalty(0);
		h.init();

		CELOE la = new CELOE();
		la.setHeuristic(h);
		la.setLearningProblem(lp);
		if (isMaster == 1)
			la.setMaxExecutionTimeInSeconds(runtimeInSecsOrWorkerBaseTime);
		else
			la.setWorkerRuntimeBaseInSecs(runtimeInSecsOrWorkerBaseTime);
		la.setNoisePercentage(noisePercent / 100.);
		la.setOperator(op);
		la.setReasoner(reasoner);
		la.setSearchTreeFile(searchTreeFilePath);
		la.setWriteSearchTree(true);

		la.setAMQPConfigFilePath(amqpConfFilePath);
		if (isMaster == 1) {
			la.setRunMaster();
		}
		la.init();

		la.start();
		la.stop();
	}
}
