package org.dllearner.test;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Test for learning on SWORE ontology.
 * 
 * @author Jens Lehmann
 * 
 */
public class SworeTest {

	/**
	 * @param args
	 * @throws ComponentInitException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ComponentInitException, MalformedURLException {
		
		// create knowledge source
		String example = "examples/swore/swore.rdf";
		AbstractKnowledgeSource source = new OWLFile(example);
		
		// create OWL API reasoning service with standard settings
//		AbstractReasonerComponent reasoner = cm.reasoner(OWLAPIReasoner.class, source);
//		reasoner.init();
		
		// set up a closed-world reasoner
		AbstractReasonerComponent reasoner = new ClosedWorldReasoner(source);
		reasoner.init();
		
		// create a learning problem and set positive and negative examples
		PosNegLPStandard lp = new PosNegLPStandard(reasoner);
		Set<OWLIndividual> positiveExamples = new TreeSet<>();
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		positiveExamples.add(df.getOWLNamedIndividual(IRI.create("http://ns.softwiki.de/req/important")));
		positiveExamples.add(df.getOWLNamedIndividual(IRI.create("http://ns.softwiki.de/req/very_important")));
		Set<OWLIndividual> negativeExamples = new TreeSet<>();
		negativeExamples.add(df.getOWLNamedIndividual(IRI.create("http://ns.softwiki.de/req/Topic")));
		lp.setPositiveExamples(positiveExamples);
		lp.setNegativeExamples(negativeExamples);
		lp.init();
		
		// create the learning algorithm
		AbstractCELA la = null;
		la = new OCEL(lp, reasoner);
		la.init();
	
		// start the algorithm and print the best concept found
		la.start();
		System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
	}

}
