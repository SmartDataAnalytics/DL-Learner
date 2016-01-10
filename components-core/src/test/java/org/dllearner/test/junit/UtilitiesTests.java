package org.dllearner.test.junit;

import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.learn.UsedEntitiesDetection;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * Various tests for methods/classes in the utilities package.
 * 
 * @author Jens Lehmann
 *
 */
public class UtilitiesTests {

	@Test
	public void entityDetection() {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.DATA1);
		int maxDepth = 2;
		Set<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
		individuals.add(new OWLNamedIndividualImpl(IRI.create("http://localhost/foo#tim")));
		UsedEntitiesDetection detection = new UsedEntitiesDetection(reasoner, individuals, maxDepth);
		System.out.println(detection);
	}
	
}
