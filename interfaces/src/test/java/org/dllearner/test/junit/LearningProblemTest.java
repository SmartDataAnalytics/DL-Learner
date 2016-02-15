package org.dllearner.test.junit;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.cli.CLI;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.StringRenderer;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by ailin on 16-2-5.
 */
public class LearningProblemTest {
	@Test
	public void problemQualityTest() {
		try {
			StringRenderer.setRenderer(StringRenderer.Rendering.MANCHESTER_SYNTAX);
			CLI cli = new CLI(new File("../examples/carcinogenesis/validate.conf"));
			cli.init();
			AbstractReasonerComponent reasoner = cli.getReasonerComponent();
			OWLDataFactory df = new OWLDataFactoryImpl();
			AbstractCELA la = cli.getLearningAlgorithm();
			AbstractClassExpressionLearningProblem lp = cli.getLearningProblem();
			OWLClassExpressionLengthMetric metric;
			if (la instanceof OCEL) {
				metric = ((OCEL)la).getLengthMetric();
			} else {
				metric = OWLClassExpressionLengthMetric.getDefaultMetric();
			}

			OWLClassExpression concept = OWLAPIUtils.fromManchester("Compound and hasAtom only (not Nitrogen-35) and ((amesTestPositive some {true}) or hasStructure some Ar_halide)", reasoner, df, true);

			System.out.println("concept: " + concept + " {" + OWLClassExpressionUtils.getLength(concept, metric) + "}");

			double acc = lp.getAccuracyOrTooWeak(concept);

			assertTrue("accuracy was only " + acc, acc > 0.69);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
