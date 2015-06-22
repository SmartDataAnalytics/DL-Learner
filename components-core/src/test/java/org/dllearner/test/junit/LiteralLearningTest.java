package org.dllearner.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.vocab.OWLFacet;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

public final class LiteralLearningTest {
	static final String NUMBERS = "http://dl-learner.org/test/numbers#";
	static final String DOUBLES = "http://dl-learner.org/test/doubles#";
	static final String SHORTS = "http://dl-learner.org/test/shorts#";
	static final String FLOATS = "http://dl-learner.org/test/floats#";
	static final String NUMBERS_OWL = "../test/literals/numbers.owl";
	static final String DOUBLES_OWL = "../test/literals/doubles.owl";
	static final String SHORTS_OWL = "../test/literals/shorts.owl";
	static final String FLOATS_OWL = "../test/literals/floats.owl";
	
	private void genericNumericTypeTest (String prefix, String owlfile, OWLDatatype restrictionType, String solution) throws OWLOntologyCreationException, ComponentInitException {
		File file = new File(owlfile);
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		
		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		AbstractReasonerComponent rc = new ClosedWorldReasoner(ks);
		rc.init();
		
		PosNegLPStandard lp = new PosNegLPStandard();
		Set<OWLIndividual> positiveExamples = new TreeSet<OWLIndividual>();
		positiveExamples.add(new OWLNamedIndividualImpl(IRI.create(prefix + "N1")));
		positiveExamples.add(new OWLNamedIndividualImpl(IRI.create(prefix + "N2")));
		positiveExamples.add(new OWLNamedIndividualImpl(IRI.create(prefix + "N3")));
		Set<OWLIndividual> negativeExamples = new TreeSet<OWLIndividual>();
		negativeExamples.add(new OWLNamedIndividualImpl(IRI.create(prefix + "N100")));
		negativeExamples.add(new OWLNamedIndividualImpl(IRI.create(prefix + "N102")));
		negativeExamples.add(new OWLNamedIndividualImpl(IRI.create(prefix + "N104")));
		lp.setPositiveExamples(positiveExamples);
		lp.setNegativeExamples(negativeExamples);
		lp.setReasoner(rc);
		lp.init();
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(rc);
		op.init();
		
		CELOE alg = new CELOE(lp, rc);
		alg.setMaxClassDescriptionTests(9);
		alg.setOperator(op);
		alg.init();
		
		alg.start();
		OWLClassExpression soln = alg.getCurrentlyBestDescription();
		//System.err.println(soln);
		OWLDataFactory df = new OWLDataFactoryImpl();
		assertTrue(soln.getNNF().equals(
				df.getOWLDataSomeValuesFrom(
						df.getOWLDataProperty(IRI.create(prefix + "value")),
						df.getOWLDatatypeRestriction(
								restrictionType,
								df.getOWLFacetRestriction(
										OWLFacet.MAX_INCLUSIVE,
										df.getOWLLiteral(solution, restrictionType))))));
		
		
	}

	@Test
	public void doubleTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		genericNumericTypeTest(DOUBLES, DOUBLES_OWL, (new OWLDataFactoryImpl()).getDoubleOWLDatatype(), "9.5");
	}
	
	@Test
	public void numericTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		genericNumericTypeTest(NUMBERS, NUMBERS_OWL, (new OWLDataFactoryImpl()).getIntegerOWLDatatype(), "9");
	}

//	@Test
//	public void shortTypeTest () throws ComponentInitException, OWLOntologyCreationException {
//		genericNumericTypeTest(SHORTS, SHORTS_OWL, OWL2Datatype.XSD_SHORT.getDatatype(new OWLDataFactoryImpl()), "9");
//	}
//	
//	@Test
//	public void floatTypeTest () throws ComponentInitException, OWLOntologyCreationException {
//		genericNumericTypeTest(FLOATS, FLOATS_OWL, (new OWLDataFactoryImpl()).getFloatOWLDatatype(), "9.5");
//	}
	
}
