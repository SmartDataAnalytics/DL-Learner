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
 * Created by Simon Bin on 16-1-27.
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
		OWLClassExpression ce1 = df.getOWLDataSomeValuesFrom(dp, restriction1);

		OWLDatatypeRestriction restriction2 =
				 df.getOWLDatatypeRestriction(
						 XSD.INT,
				Collections.singleton(df.getOWLFacetRestriction(
						OWLFacet.MAX_INCLUSIVE,
						max)));
		OWLClassExpression ce2 = df.getOWLDataSomeValuesFrom(dp, restriction2);
		OWLClassExpression ce3 = df.getOWLDataHasValue(dp, df.getOWLLiteral(true));
		OWLClassExpression ce4 = df.getOWLObjectAllValuesFrom(op, df.getOWLThing());
		OWLClassExpression ce5 = df.getOWLObjectSomeValuesFrom(op.getInverseProperty(), df.getOWLThing());
		int length1 = new OWLClassExpressionLengthCalculator().getLength(ce1);
		int length2 = new OWLClassExpressionLengthCalculator().getLength(ce2);
		int length3 = new OWLClassExpressionLengthCalculator().getLength(ce3);
		int length4 = new OWLClassExpressionLengthCalculator().getLength(ce4);
		int length5 = new OWLClassExpressionLengthCalculator().getLength(ce5);
		System.err.println("length of "+ce1+" is "+length1);
		System.err.println("length of "+ce2+" is "+length2);
		System.err.println("length of "+ce3+" is "+length3);
		System.err.println("length of "+ce4+" is "+length4);
		System.err.println("length of "+ce5+" is "+length5);
	}
}
