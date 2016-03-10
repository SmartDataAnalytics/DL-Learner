package org.dllearner.learningproblems.sampling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.learningproblems.sampling.CELOEPlusSampling;
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

	final static Logger logger = Logger.getLogger(CELOEPlusTest.class);

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
	 * Namespace of the individuals to be considered, discarding sameAs
	 * instances.
	 */
	private static final String NAMESPACE = "http://dbpedia.org/resource/";

	/**
	 * Class whose expression is to be learned.
	 */
	private static final String TARGET_CLASS = "http://dbpedia.org/class/yago/InfectiousAgent109312843";

	@Test
	public void test() {

		CELOEPlusSampling cps = CELOEPlusSampling.getInstance();
		assertNotNull(cps);

		Collection<OWLIndividual> pos = getIndividuals(SUPERCLASS_URI,
				NAMESPACE, PATH_PREFIX + "dbo-fungus-pos.nt");
		assertEquals(pos.size(), 3);
		Collection<OWLIndividual> neg = getIndividuals(SUPERCLASS_URI,
				NAMESPACE, PATH_PREFIX + "dbo-fungus-neg.nt");
		assertEquals(neg.size(), 3);

		cps.sample(TARGET_CLASS, pos, neg);

	}

	/**
	 * @param superclass
	 * @param namespace
	 * @param filename
	 * @return
	 */
	private Set<OWLIndividual> getIndividuals(String superclass,
			String namespace, String filename) {

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

		OWLReasoner reasoner = PelletReasonerFactory.getInstance()
				.createReasoner(o);
		Set<OWLNamedIndividual> instances = reasoner.getInstances(
				OWL.Class(IRI.create(superclass)), false).getFlattened();

		// filter out all owl:sameAs instances...
		Set<OWLIndividual> ind = new TreeSet<>();
		for (OWLNamedIndividual i : instances) {
			// TODO if not done earlier, this should be moved inside
			// CELOEPlusSampling.sample()
			if (i.getIRI().toString().startsWith(namespace))
				ind.add(i);
		}
		logger.info("|I| = " + ind.size() + "\t\tI = " + ind);

		return ind;

	}

}
