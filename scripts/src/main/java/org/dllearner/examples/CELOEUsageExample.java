package org.dllearner.examples;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

import com.google.common.collect.Sets;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * This class should exemplify how to run CELOE programmatically, i.e. by
 * creating all components explicitly in Java. This example runs the same
 * experiment as the examples/father.conf does.
 */
public class CELOEUsageExample {
	static File familyExamplesDir = new File("../examples");
	static String uriPrefix = "http://example.com/father#";

	public static void main(String[] args) throws ComponentInitException {
		/* Define the knowledge source
		 * > ks.type = "OWL File"
		 * > ks.fileName = "father.owl"
		 */
		OWLFile ks = new OWLFile();
		ks.setFileName(familyExamplesDir.getAbsolutePath() + "/father.owl");
		ks.init();

		/* Set up the reasoner
		 * > reasoner.type = "closed world reasoner"
		 * > reasoner.sources = { ks }
		 */
		ClosedWorldReasoner reasoner = new ClosedWorldReasoner();

		// create { ks }, i.e. a set containing ks
		Set<KnowledgeSource> sources = new HashSet<>();
		sources.add(ks);

		reasoner.setSources(sources);
		reasoner.init();

		/* Set up the learning problem
		 * > lp.type = "posNegStandard"
		 * > lp.positiveExamples = { "ex:stefan", "ex:markus", "ex:martin" }
		 * > lp.negativeExamples = { "ex:heinz", "ex:anna", "ex:michelle" }
		 */
		PosNegLPStandard lp = new PosNegLPStandard(reasoner);

		HashSet<OWLIndividual> posExamples = Sets.newHashSet(
				new OWLNamedIndividualImpl(IRI.create(uriPrefix + "stefan")),
				new OWLNamedIndividualImpl(IRI.create(uriPrefix + "markus")),
				new OWLNamedIndividualImpl(IRI.create(uriPrefix + "martin"))
		);
		lp.setPositiveExamples(posExamples);

		HashSet<OWLIndividual> negExamples = Sets.newHashSet(
				new OWLNamedIndividualImpl(IRI.create(uriPrefix + "heinz")),
				new OWLNamedIndividualImpl(IRI.create(uriPrefix + "anna")),
				new OWLNamedIndividualImpl(IRI.create(uriPrefix + "michelle"))
		);
		lp.setNegativeExamples(negExamples);

		lp.init();

		/* Set up the learning algorithm
		 * > alg.type = "celoe"
		 * > alg.maxExecutionTimeInSeconds = 1
		 */
		CELOE alg = new CELOE();
		alg.setMaxExecutionTimeInSeconds(1);

		// This 'wiring' is not part of the configuration file since it is
		// done automatically when using bin/cli. However it has to be done explicitly,
		// here.
		alg.setLearningProblem(lp);
		alg.setReasoner(reasoner);

		alg.init();

		alg.start();
	}
}
