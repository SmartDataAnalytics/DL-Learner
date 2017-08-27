package org.dllearner.temporal.examples;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.OWLTimePosOnlyLP;
import org.dllearner.reasoning.InfluxDBOWLTimeReasoner;
import org.dllearner.sampling.temporal.RandomSamplingStrategy;
import org.dllearner.sampling.temporal.TemporalSamplingStrategy;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

public class InfluxDBOWLTimeExample {
	private static String kbFilePath = "src/test/resources/workstation_crashes.owl";
	private static String prefix = "http://dl-learner.org/ontologies/workstationcrashes#";
	private static OWLDataFactory df = OWLManager.getOWLDataFactory();

	public static void main(String[] args) throws ComponentInitException {
		OWLFile ks = new OWLFile(kbFilePath);
		ks.init();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();

		Set<KnowledgeSource> sources = new HashSet<>();
		sources.add(ks);

		reasoner.setSources(sources);

		OWLDataProperty occcurredAt = df.getOWLDataProperty(IRI.create(prefix + "occurredAt"));
		OWLClass instantClass = df.getOWLClass(IRI.create(prefix + "Event"));

		reasoner.setDateTimePropertyPath(occcurredAt);
		reasoner.setTimeInstantClassExpression(instantClass);

		reasoner.init();

		TemporalSamplingStrategy strategy = new RandomSamplingStrategy();
		OWLTimePosOnlyLP lp = new OWLTimePosOnlyLP();
		SortedSet<OWLIndividual> posExamples = new TreeSet<>();
		posExamples.add(new OWLNamedIndividualImpl(IRI.create(prefix + "outage001")));
		lp.setPositiveExamples(posExamples);
		lp.setReasoner(reasoner);
		lp.setSamplingStrategy(strategy);
		lp.init();

		CELOE la = new CELOE();
		la.setMaxExecutionTimeInSeconds(60);
		la.setLearningProblem(lp);
		la.setReasoner(reasoner);

		la.init();
		la.start();
	}
}
