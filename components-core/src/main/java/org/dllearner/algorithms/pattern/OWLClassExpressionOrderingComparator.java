package org.dllearner.algorithms.pattern;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassExpressionImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectExactCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasValueImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMaxCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMinCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

public class OWLClassExpressionOrderingComparator implements Comparator<OWLClassExpression> {

	private static final List<Class<? extends OWLClassExpressionImpl>> ordering = Arrays.asList(
			OWLClassImpl.class,
			OWLObjectSomeValuesFromImpl.class, 
			OWLObjectAllValuesFromImpl.class,
			OWLObjectComplementOfImpl.class,
			OWLObjectIntersectionOfImpl.class,
			OWLObjectUnionOfImpl.class,
			OWLObjectHasValueImpl.class,
			OWLObjectMinCardinalityImpl.class,
			OWLObjectMaxCardinalityImpl.class,
			OWLObjectExactCardinalityImpl.class
			);

	@Override
	public int compare(OWLClassExpression o1, OWLClassExpression o2) {
		int diff = ordering.indexOf(o1.getClass()) - ordering.indexOf(o2.getClass());
		if(diff == 0){
			return o1.compareTo(o2);
		} else {
			return diff;
		}
	}

}
