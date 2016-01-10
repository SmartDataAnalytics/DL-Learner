package org.dllearner.refinementoperators;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;

/**
 * The class uses an existing refinement operator and inverts it, i.e. a 
 * downward refinement operator is turned into an upward refinement operator
 * and vice versa.
 * 
 * @author Jens Lehmann
 *
 */
public class OperatorInverter implements LengthLimitedRefinementOperator {

	private LengthLimitedRefinementOperator operator;
	private boolean useNegationNormalForm = true;
	private boolean guaranteeLength = true;
	
	public OperatorInverter(LengthLimitedRefinementOperator operator) {
		this.operator = operator;
	}
	
	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description) {
		Set<OWLClassExpression> refinements = operator.refine(getNegation(description));
		TreeSet<OWLClassExpression> results = new TreeSet<>();
		for(OWLClassExpression d : refinements) {
			results.add(getNegation(d));
		}
		return results;
	}

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength) {
		OWLClassExpression negatedDescription = getNegation(description);
//		System.out.println("negated description: " + negatedDescription);
		// concept length can change because of the conversion process; as a heuristic
		// we increase maxLength by the length difference of negated and original concept
		int lengthDiff = Math.max(0, OWLClassExpressionUtils.getLength(negatedDescription) - OWLClassExpressionUtils.getLength(description));
		Set<OWLClassExpression> refinements = operator.refine(negatedDescription, maxLength+lengthDiff+1);
//		System.out.println("refinv: " + refinements);
		TreeSet<OWLClassExpression> results = new TreeSet<>();
		for(OWLClassExpression d : refinements) {
			OWLClassExpression dNeg = getNegation(d);
//			System.out.println("dNeg: " + dNeg);
			// to satisfy the guarantee that the method does not return longer
			// concepts, we perform an additional check
			if(!guaranteeLength || OWLClassExpressionUtils.getLength(dNeg) <= maxLength) {
				results.add(dNeg);
			}
		}
		return results;	
	}

	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength,
			List<OWLClassExpression> knownRefinements) {
		throw new Error("Method not implemented.");
	}

	private OWLClassExpression getNegation(OWLClassExpression description) {
		OWLClassExpression negatedDescription = new OWLObjectComplementOfImpl(description);
		if(useNegationNormalForm) {
			negatedDescription = negatedDescription.getNNF();
		}
		return negatedDescription;
	}

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}
	
}
