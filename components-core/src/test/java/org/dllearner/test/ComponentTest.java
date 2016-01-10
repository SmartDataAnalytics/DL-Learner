package org.dllearner.test;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Test for component based design.
 * 
 * @author Jens Lehmann
 * 
 */
public class ComponentTest {

	/**
	 * @param args
	 * @throws ComponentInitException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ComponentInitException, MalformedURLException {
		
		// create knowledge source
		String example = "../examples/family/uncle.owl";
		KnowledgeSource source = new OWLFile(example);
		
		// create OWL API reasoning service with standard settings
		AbstractReasonerComponent reasoner = new OWLAPIReasoner(Collections.singleton(source));
		reasoner.init();
		
		OWLDataFactory df = new OWLDataFactoryImpl();
		PrefixManager pm = new DefaultPrefixManager("http://localhost/foo#");
		
		// create a learning problem and set positive and negative examples
		PosNegLPStandard lp = new PosNegLPStandard(reasoner);
		Set<OWLIndividual> positiveExamples = new TreeSet<OWLIndividual>();
		positiveExamples.add(df.getOWLNamedIndividual("heinz", pm));
		positiveExamples.add(df.getOWLNamedIndividual("alex", pm));
		Set<OWLIndividual> negativeExamples = new TreeSet<OWLIndividual>();
		negativeExamples.add(df.getOWLNamedIndividual("jan", pm));
		negativeExamples.add(df.getOWLNamedIndividual("anna", pm));
		negativeExamples.add(df.getOWLNamedIndividual("hanna", pm));
		lp.setPositiveExamples(positiveExamples);
		lp.setNegativeExamples(negativeExamples);
		lp.init();
		
		// create the learning algorithm
		OCEL la = new OCEL(lp, reasoner);
		la.setMaxExecutionTimeInSeconds(60);
		la.init();
	
		// start the algorithm and print the best concept found
		la.start();
		System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
		/**
		 * possible solution: (male AND (EXISTS hasSibling.EXISTS hasChild.TOP 
		 *                    OR EXISTS married.EXISTS hasSibling.EXISTS hasChild.TOP))
		 */
	}

}
