package org.dllearner.learningproblems.sampling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
public class CELOEPlusTest {

	final static Logger logger = Logger.getRootLogger();

	private static OWLOntologyManager m;

	/**
	 * Path to the test files.
	 */
	private static final String PATH_PREFIX = "src/test/resources/org/dllearner/kb/";

	/**
	 * URI of the superclass which the individuals belongs to.
	 */
	private static final String SUPERCLASS_URI = "http://dbpedia.org/ontology/Fungus";

	/**
	 * Class whose expression is to be learned.
	 */
	private static final String TARGET_CLASS = "http://dbpedia.org/class/yago/InfectiousAgent109312843";

	@Test
	public void test() {
		
		CELOEPlusSampling cps = CELOEPlusSampling.getInstance();
		assertNotNull(cps);

		OWLOntology ontPos = getOntology(PATH_PREFIX + "dbo-fungus-pos.nt");
		Collection<OWLIndividual> pos = getIndividuals(SUPERCLASS_URI,
				ontPos);
		assertEquals(pos.size(), 19);
		OWLOntology ontNeg = getOntology(PATH_PREFIX + "dbo-fungus-neg.nt");
		Collection<OWLIndividual> neg = getIndividuals(SUPERCLASS_URI,
				ontNeg);
		assertEquals(neg.size(), 15);
		
		KnowledgeSource ksPos = new OWLAPIOntology(ontPos);
		KnowledgeSource ksNeg = new OWLAPIOntology(ontNeg);
		try {
			ksPos.init();
			ksNeg.init();
		} catch (NullPointerException | ComponentInitException e) {
			fail("Could not initialize ontology.");
			return;
		}
		OWLAPIReasoner reasoner = new OWLAPIReasoner(ksPos, ksNeg);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			fail("Could not initialize reasoner.");
			return;
		}
		
		cps.sample(reasoner, TARGET_CLASS, pos, neg);

	}
	
	/**
	 * @param filename
	 * @return
	 */
	private OWLOntology getOntology(String filename) {
		File file = new File(filename);

		if (m == null)
			m = OWLManager.createOWLOntologyManager();

		OWLOntology o;
		try {
			o = m.loadOntologyFromOntologyDocument(IRI.create(file.toURI()));
		} catch (OWLOntologyCreationException e) {
			fail("Cannot load ontology.");
			return null;
		}
		assertNotNull(o);

		return o;
	}

	/**
	 * @param superclass
	 * @param o
	 * @return
	 */
	private Set<OWLIndividual> getIndividuals(String superclass,
			OWLOntology o) {


		OWLReasoner reasoner = PelletReasonerFactory.getInstance()
				.createReasoner(o);
		Set<OWLNamedIndividual> instances = reasoner.getInstances(
				OWL.Class(IRI.create(superclass)), false).getFlattened();

		// filter out all owl:sameAs instances...
		Set<OWLIndividual> ind = new TreeSet<>();
		for (OWLNamedIndividual i : instances) {
			ind.add(i);
		}
		logger.info("|I| = " + ind.size() + "\t\tI = " + ind);

		return ind;

	}

}
