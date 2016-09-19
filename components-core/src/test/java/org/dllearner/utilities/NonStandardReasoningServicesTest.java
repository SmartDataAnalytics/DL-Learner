package org.dllearner.utilities;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Lorenz Buehmann
 */
public class NonStandardReasoningServicesTest {

	AbstractReasonerComponent reasoner;
	OWLDataFactory df;
	PrefixManager pm;

	@Before
	public void setUp() throws Exception {
		reasoner = new OWLAPIReasoner();
		reasoner.setSources(new OWLAPIOntology(OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
				new File("src/test/resources/org/dllearner/utilities/property_hierarchy.ttl"))));
		reasoner.init();

		df = new OWLDataFactoryImpl();

		pm = new DefaultPrefixManager();
		pm.setDefaultPrefix("http://www.dl-learner.org/test/hierarchy/properties#");
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void getLeastCommonSubsumer() throws Exception {
		OWLProperty lcs = NonStandardReasoningServices.getLeastCommonSubsumer(
				reasoner,
				df.getOWLObjectProperty("s3", pm),
				df.getOWLObjectProperty("s5", pm));

		assertTrue(lcs.equals(df.getOWLObjectProperty("p1", pm)));

		lcs = NonStandardReasoningServices.getLeastCommonSubsumer(
				reasoner,
				df.getOWLObjectProperty("s3", pm),
				df.getOWLObjectProperty("s4", pm));

		assertTrue(lcs.equals(df.getOWLObjectProperty("s3", pm)));

		lcs = NonStandardReasoningServices.getLeastCommonSubsumer(
				reasoner,
				df.getOWLObjectProperty("s1", pm),
				df.getOWLObjectProperty("s2", pm));

		assertTrue(lcs.equals(df.getOWLObjectProperty("p1", pm)));

		lcs = NonStandardReasoningServices.getLeastCommonSubsumer(
				reasoner,
				df.getOWLObjectProperty("s3", pm),
				df.getOWLObjectProperty("p2", pm));

		assertTrue(lcs == null);

	}

}