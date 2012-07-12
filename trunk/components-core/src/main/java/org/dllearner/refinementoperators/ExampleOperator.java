package org.dllearner.refinementoperators;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.Description;

/**
 * Example refinement operator.
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "example refinement operator", shortName = "exop", version = 0.8)
public class ExampleOperator implements LengthLimitedRefinementOperator {

	@Override
	public void init() throws ComponentInitException {
	}

	@Override
	public Set<Description> refine(Description description) {
		return new TreeSet<Description>();
	}

	@Override
	public Set<Description> refine(Description description, int maxLength) {
		return refine(description);
	}

	@Override
	public Set<Description> refine(Description description, int maxLength,
			List<Description> knownRefinements) {
		return refine(description);		
	}

}
