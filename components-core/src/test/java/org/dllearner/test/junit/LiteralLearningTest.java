/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.test.junit;

import com.clarkparsia.owlapiv3.XSD;
import com.google.common.collect.Sets;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplInteger;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;

public final class LiteralLearningTest {
	static final String NUMBERS = "http://dl-learner.org/test/numbers#";
	static final String DOUBLES = "http://dl-learner.org/test/doubles#";
	static final String SHORTS = "http://dl-learner.org/test/shorts#";
	static final String FLOATS = "http://dl-learner.org/test/floats#";
	static final String DATES = "http://dl-learner.org/test/dates#";
	static final String DATETIMES = "http://dl-learner.org/test/datetimes#";
	static final String MONTHS = "http://dl-learner.org/test/months#";
	
	
	static final String NUMBERS_OWL = "../test/literals/numbers.owl";
	static final String DOUBLES_OWL = "../test/literals/doubles.owl";
	static final String SHORTS_OWL = "../test/literals/shorts.owl";
	static final String FLOATS_OWL = "../test/literals/floats.owl";
	static final String DATES_OWL = "../test/literals/dates.owl";
	static final String DATETIMES_OWL = "../test/literals/datetimes.owl";
	static final String MONTHS_OWL = "../test/literals/months-noz.owl";
	
	private class TestRunner {
		public AbstractReasonerComponent[] rcs;
		private String prefix;
		private File file;
		private PrefixManager pm;
		public AbstractKnowledgeSource ks;
		public OWLDataFactory df;
		private OWLClassExpression target;
		private OWLDatatype restrictionType;
		private int maxNrOfSplits;
		TestRunner(String prefix, String owlfile, OWLDatatype restrictionType, int maxNrOfSplits) throws OWLOntologyCreationException, ComponentInitException {
			this.prefix = prefix;
			this.restrictionType = restrictionType;
			this.maxNrOfSplits = maxNrOfSplits;
//			StringRenderer.setRenderer(Rendering.MANCHESTER_SYNTAX);
//			StringRenderer.setRenderer(Rendering.DL_SYNTAX);

			File file = new File(owlfile);
			OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
			df = new OWLDataFactoryImpl();
			pm = new DefaultPrefixManager();
			pm.setDefaultPrefix(prefix);
			ks = new OWLAPIOntology(ontology);
			ks.init();
		}
		TestRunner(String prefix, String owlfile, OWLDatatype restrictionType) throws OWLOntologyCreationException, ComponentInitException {
			this(prefix, owlfile, restrictionType, 12);
		}
		public void run() throws ComponentInitException {
			Level oldLevel = Logger.getLogger("org.dllearner").getLevel();
			//Level oldLevelCELOE = Logger.getLogger(CELOE.class).getLevel();
			try {
				Logger.getLogger("org.dllearner").setLevel(Level.DEBUG);
                //Logger.getLogger(CELOE.class).setLevel(Level.DEBUG);

				Set<OWLIndividual> positiveExamples = new TreeSet<>();
				positiveExamples.add(df.getOWLNamedIndividual("N1", pm));
				positiveExamples.add(df.getOWLNamedIndividual("N2", pm));
				positiveExamples.add(df.getOWLNamedIndividual("N3", pm));

				Set<OWLIndividual> negativeExamples = new TreeSet<>();
				negativeExamples.add(df.getOWLNamedIndividual("N100", pm));
				negativeExamples.add(df.getOWLNamedIndividual("N102", pm));
				negativeExamples.add(df.getOWLNamedIndividual("N104", pm));

				for (AbstractReasonerComponent rc : rcs) {
					PosNegLPStandard lp = new PosNegLPStandard(rc);
					lp.setPositiveExamples(positiveExamples);
					lp.setNegativeExamples(negativeExamples);
					lp.init();

					RhoDRDown op = new RhoDRDown();
					op.setUseTimeDatatypes(true);
					op.setUseNumericDatatypes(true);
					op.setReasoner(rc);
					op.setMaxNrOfSplits(maxNrOfSplits);
					op.init();

					CELOE alg = new CELOE(lp, rc);
					alg.setMaxClassExpressionTests(1000);
					alg.setMaxExecutionTimeInSeconds(0);
					alg.setOperator(op);
					alg.init();

					alg.start();
					OWLClassExpression soln = alg.getCurrentlyBestDescription();

					assertTrue(soln.getNNF().equals(target));

				}
			} finally {
				Logger.getLogger("org.dllearner").setLevel(oldLevel);
				//Logger.getLogger(CELOE.class).setLevel(oldLevelCELOE);
			}
		}
		public void setSingleRestrictionTarget(OWLFacet facetType, String solution) {
			this.target = df.getOWLDataSomeValuesFrom(
					df.getOWLDataProperty(IRI.create(prefix + "value")),
					df.getOWLDatatypeRestriction(
							restrictionType,
							df.getOWLFacetRestriction(
									facetType,
									df.getOWLLiteral(solution, restrictionType))));
		}

		public void setDualRestrictionTarget(String minSolution, String maxSolution) {
			this.target = df.getOWLDataSomeValuesFrom(
					df.getOWLDataProperty(IRI.create(prefix + "value")),
					df.getOWLDatatypeRestriction(
							restrictionType,
							Sets.newHashSet(
									df.getOWLFacetRestriction(
											OWLFacet.MAX_INCLUSIVE,
											df.getOWLLiteral(maxSolution, restrictionType)
											),
											df.getOWLFacetRestriction(
													OWLFacet.MIN_INCLUSIVE,
													df.getOWLLiteral(minSolution, restrictionType))
									)));
		}
		public void setReasoners(AbstractReasonerComponent... rcs) throws ComponentInitException {
			this.rcs = rcs;
			for(AbstractReasonerComponent rc : this.rcs) {
				rc.init();
			}
		}
	}
	
	private void genericNumericTypeTest (String prefix, String owlfile, OWLDatatype restrictionType, String solution) throws OWLOntologyCreationException, ComponentInitException {
		TestRunner runner = new TestRunner(prefix, owlfile, restrictionType);
		
		runner.setSingleRestrictionTarget(OWLFacet.MAX_INCLUSIVE, solution);
		
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(runner.ks);
		OWLAPIReasoner oar = new OWLAPIReasoner(runner.ks);
		runner.setReasoners(cwr, oar);
		
		runner.run();

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
		TestRunner runner = new TestRunner(DATES, DATES_OWL, XSD.DATE);
		
		runner.setDualRestrictionTarget("1970-01-22", "1971-09-24");
		
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(runner.ks);
		OWLAPIReasoner oar = new OWLAPIReasoner(runner.ks); // upload fixed version of Pellet and confirm that it works
		runner.setReasoners(cwr , oar);
		
		runner.run();
	}
	
	@Test
	public void datetimeTypeTest () throws ComponentInitException, OWLOntologyCreationException {
		// E+: 1970-10-22, 1970-11-27, 1971-09-24
		// E-: 1970-01-05, 2002-03-24, 2002-09-27
		// T : 1970-10-22 <= x <= 1971-09-24
		TestRunner runner = new TestRunner(DATETIMES, DATETIMES_OWL, XSD.DATE_TIME);
		
//		runner.setDualRestrictionTarget("1970-01-22T08:10:10", "1971-09-24T02:22:22");

		runner.setDualRestrictionTarget("1970-01-22T08:10:10", "1972-01-22T11:11:11");

		ClosedWorldReasoner cwr = new ClosedWorldReasoner(runner.ks);
//		OWLAPIReasoner oar = new OWLAPIReasoner(runner.ks);
//		oar.setReasonerImplementation(ReasonerImplementation.HERMIT);
		runner.setReasoners(cwr /*, oar */); // TODO: figure out why this crashes on ci and @Patrick
		
		runner.run();
	}

	@Test
	public void gMonthTypeTest () throws OWLOntologyCreationException, ComponentInitException {
		// TODO Pellet does not support any time zone
		TestRunner runner = new TestRunner(MONTHS, MONTHS_OWL, XSD.G_MONTH, 12);
		
		runner.setDualRestrictionTarget("--03", "--05");
		
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(runner.ks);
		OWLAPIReasoner oar = new OWLAPIReasoner(runner.ks); // upload fixed version of Pellet and confirm that it works
		runner.setReasoners(cwr , oar);
		
		runner.run();
	}

	@Test
	public void stringTypeTest () throws OWLOntologyCreationException, ComponentInitException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		OWLOntology ontology = man.createOntology();
		PrefixManager pm = new DefaultPrefixManager();
		pm.setDefaultPrefix("http://dl-learner.org/test/strings/");
		OWLDataProperty dp = df.getOWLDataProperty("stringValue", pm);
		OWLClass cls = df.getOWLClass("A", pm);
		man.addAxiom(ontology, df.getOWLDataPropertyDomainAxiom(dp, cls));
		man.addAxiom(ontology, df.getOWLDataPropertyRangeAxiom(dp, OWL2Datatype.XSD_STRING.getDatatype(df)));

		Set<OWLIndividual> positiveExamples = new TreeSet<>();
		for (int i = 0; i < 10; i++) {
			OWLNamedIndividual ind = df.getOWLNamedIndividual("p" + i, pm);
			positiveExamples.add(ind);
			man.addAxiom(ontology, df.getOWLDataPropertyAssertionAxiom(dp, ind, "X"));
			man.addAxiom(ontology, df.getOWLClassAssertionAxiom(cls, ind));
		}

		Set<OWLIndividual> negativeExamples = new TreeSet<>();
		for (int i = 0; i < 10; i++) {
			OWLNamedIndividual ind = df.getOWLNamedIndividual("n" + i, pm);
			negativeExamples.add(ind);
			man.addAxiom(ontology, df.getOWLDataPropertyAssertionAxiom(dp, ind, "Y"));
			man.addAxiom(ontology, df.getOWLClassAssertionAxiom(cls, ind));
		}

		OWLAPIOntology ks = new OWLAPIOntology(ontology);
		ks.init();

		ClosedWorldReasoner reasoner = new ClosedWorldReasoner(ks);
		reasoner.init();

		PosNegLPStandard lp = new PosNegLPStandard(reasoner);
		lp.setPositiveExamples(positiveExamples);
		lp.setNegativeExamples(negativeExamples);
		lp.init();

		RhoDRDown op = new RhoDRDown();
		op.setUseDataHasValueConstructor(true);
		op.setReasoner(reasoner);
		op.init();

		CELOE alg = new CELOE(lp, reasoner);
		alg.setMaxClassExpressionTests(1000);
		alg.setMaxExecutionTimeInSeconds(0);
		alg.setOperator(op);
		alg.init();

		alg.start();
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
