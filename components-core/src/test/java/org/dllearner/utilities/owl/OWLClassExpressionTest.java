package org.dllearner.utilities.owl;

import com.clarkparsia.owlapiv3.XSD;
import org.dllearner.core.StringRenderer;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLFacet;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;

import java.util.Collections;

/**
 * Test OWL Class Expresssions
 */
public class OWLClassExpressionTest {

	@Test
	public void OWLClassExpressionLength() {
		StringRenderer.setRenderer(StringRenderer.Rendering.DL_SYNTAX);
		OWLDataFactory df = new OWLDataFactoryImpl();
		OWLLiteral min = df.getOWLLiteral(10);
		OWLLiteral max = df.getOWLLiteral(20);

		OWLDatatypeRestriction restriction1 =
				 df.getOWLDatatypeRestriction(
						 XSD.INTEGER,
				Collections.singleton(df.getOWLFacetRestriction(
						OWLFacet.MIN_INCLUSIVE,
						min)));
		OWLDataProperty dp = new OWLDataPropertyImpl(IRI.create("p1"));
		OWLObjectProperty op = df.getOWLObjectProperty(IRI.create("op1"));

		OWLDatatypeRestriction restriction2 =
				 df.getOWLDatatypeRestriction(
						 XSD.INT,
				Collections.singleton(df.getOWLFacetRestriction(
						OWLFacet.MAX_INCLUSIVE,
						max)));
		OWLClass klass = df.getOWLClass(IRI.create("C"));

		OWLClassExpression[] ce = new OWLClassExpression[]{
				df.getOWLDataSomeValuesFrom(dp, restriction1),
				df.getOWLDataSomeValuesFrom(dp, restriction2),
				df.getOWLDataHasValue(dp, df.getOWLLiteral(true)),
				df.getOWLObjectAllValuesFrom(op, df.getOWLThing()),
				df.getOWLObjectSomeValuesFrom(op.getInverseProperty(), df.getOWLThing()),
				df.getOWLObjectMaxCardinality(3, op, df.getOWLThing()),
				df.getOWLObjectMinCardinality(3, op, df.getOWLThing()),
				df.getOWLObjectAllValuesFrom(op, klass),
				df.getOWLObjectAllValuesFrom(op, klass.getComplementNNF()),
		};

		//OWLClassExpressionLengthMetric metric = OWLClassExpressionLengthMetric.getDefaultMetric();
		OWLClassExpressionLengthMetric metric = OWLClassExpressionLengthMetric.getOCELMetric();
		//metric.objectCardinalityLength = 0;
		metric.objectComplementLength = 0;
		int[] lengths = new int[ce.length];
		for (int i = 0; i < ce.length; ++i) {
			lengths[i] = new OWLClassExpressionLengthCalculator(metric).getLength(ce[i]);
		}

		for (int i = 0; i < ce.length; ++i) {
			System.err.println("length of " + ce[i] + " is " + lengths[i]);
		}
	}
}
