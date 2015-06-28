package org.dllearner.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.DLSyntaxObjectRenderer;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLFacet;

import com.clarkparsia.owlapiv3.XSD;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplInteger;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxRenderer;

public final class LiteralLearningTest {
	static final String NUMBERS = "http://dl-learner.org/test/numbers#";
	static final String DOUBLES = "http://dl-learner.org/test/doubles#";
	static final String SHORTS = "http://dl-learner.org/test/shorts#";
	static final String FLOATS = "http://dl-learner.org/test/floats#";
	static final String DATE = "http://dl-learner.org/test/dates#";
	static final String DATETIMES = "http://dl-learner.org/test/datetimes#";
	static final String NUMBERS_OWL = "../test/literals/numbers.owl";
	static final String DOUBLES_OWL = "../test/literals/doubles.owl";
	static final String SHORTS_OWL = "../test/literals/shorts.owl";
	static final String FLOATS_OWL = "../test/literals/floats.owl";
	static final String DATES_OWL = "../test/literals/dates.owl";
	static final String DATETIMES_OWL = "../test/literals/datetimes.owl";
	
	private void genericNumericTypeTest (String prefix, String owlfile, OWLDatatype restrictionType, String solution) throws OWLOntologyCreationException, ComponentInitException {
		org.apache.log4j.Logger.getLogger("org.dllearner").setLevel(Level.DEBUG);
		//org.apache.log4j.Logger.getLogger("org.dllearner.algorithms").setLevel(Level.DEBUG);
//		org.apache.log4j.Logger.getLogger(CELOE.class).setLevel(Level.DEBUG);

//		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		File file = new File(owlfile);
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		OWLDataFactory df = new OWLDataFactoryImpl();
		PrefixManager pm = new DefaultPrefixManager(prefix);
		
		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
		cwr.init();
		
		OWLAPIReasoner oar = new OWLAPIReasoner(ks);
//		oar.setReasonerImplementation(ReasonerImplementation.HERMIT);
		oar.init();
		
		AbstractReasonerComponent rcs[] = {
				//cwr, 
				 oar
				};
		
		Set<OWLIndividual> positiveExamples = new TreeSet<OWLIndividual>();
		positiveExamples.add(df.getOWLNamedIndividual("N1", pm));
		positiveExamples.add(df.getOWLNamedIndividual("N2", pm));
		positiveExamples.add(df.getOWLNamedIndividual("N3", pm));
		
		Set<OWLIndividual> negativeExamples = new TreeSet<OWLIndividual>();
		negativeExamples.add(df.getOWLNamedIndividual("N100", pm));
		negativeExamples.add(df.getOWLNamedIndividual("N102", pm));
		negativeExamples.add(df.getOWLNamedIndividual("N104", pm));
		
		OWLClassExpression target = df.getOWLDataSomeValuesFrom(
				df.getOWLDataProperty(IRI.create(prefix + "value")),
				df.getOWLDatatypeRestriction(
						restrictionType,
						df.getOWLFacetRestriction(
								OWLFacet.MAX_INCLUSIVE,
								df.getOWLLiteral(solution, restrictionType))));

		for(AbstractReasonerComponent rc : rcs) {
			PosNegLPStandard lp = new PosNegLPStandard(rc);
			lp.setPositiveExamples(positiveExamples);
			lp.setNegativeExamples(negativeExamples);
			lp.init();
			
			RhoDRDown op = new RhoDRDown();
			op.setUseTimeDatatypes(true);
			op.setUseNumericDatatypes(true);
			op.setReasoner(rc);
			op.init();
			
			CELOE alg = new CELOE(lp, rc);
			alg.setMaxClassDescriptionTests(1000);
			alg.setMaxExecutionTimeInSeconds(0);
			alg.setOperator(op);
			alg.init();
			
			alg.start();
			OWLClassExpression soln = alg.getCurrentlyBestDescription();
			//System.err.println(soln);
			assertTrue(soln.getNNF().equals(target));
			
		}
	}

	@Test
	public void doubleTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		genericNumericTypeTest(DOUBLES, DOUBLES_OWL, (new OWLDataFactoryImpl()).getDoubleOWLDatatype(), "9.5");
	}
	
	@Test
	public void numericTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		genericNumericTypeTest(NUMBERS, NUMBERS_OWL, (new OWLDataFactoryImpl()).getIntegerOWLDatatype(), "9");
	}

	@Test
	public void shortTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		genericNumericTypeTest(SHORTS, SHORTS_OWL, XSD.SHORT, "9");
	}
	
	@Test
	public void floatTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		genericNumericTypeTest(FLOATS, FLOATS_OWL, (new OWLDataFactoryImpl()).getFloatOWLDatatype(), "9.5");
	}
	
	@Test
	public void dateTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		// E+: 1970-10-22, 1970-11-27, 1971-09-24 
		// E-: 1970-01-05, 2002-03-24, 2002-09-27
		// T : 1970-10-22 <= x <= 1971-09-24
		genericNumericTypeTest(DATE, DATES_OWL, XSD.DATE, "1971-09-24");
	}
	
	@Test
	public void datetimeTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		// E+: 1970-10-22, 1970-11-27, 1971-09-24 
		// E-: 1970-01-05, 2002-03-24, 2002-09-27
		// T : 1970-10-22 <= x <= 1971-09-24
		genericNumericTypeTest(DATETIMES, DATETIMES_OWL, XSD.DATE_TIME, "1971-09-24");
	}

	@Test
	public void literalComparisonTest () {
		OWLLiteralImplInteger lit1 = new OWLLiteralImplInteger(50, XSD.INTEGER);
		OWLLiteralImplInteger lit2 = new OWLLiteralImplInteger(100, XSD.INTEGER);
		
		int diffImpl = lit1.compareTo(lit2);
		System.out.println(diffImpl);
		
		int diffValue = Integer.compare(lit1.parseInteger(), lit2.parseInteger());
		System.out.println(diffValue);
		
		System.out.println("Same sorting:" + (Math.signum(diffImpl) == Math.signum(diffValue)));
		
	}
	
	
}
