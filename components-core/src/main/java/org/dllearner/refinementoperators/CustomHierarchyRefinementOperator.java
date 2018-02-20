package org.dllearner.refinementoperators;

import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;

/**
 *
 * A refinement operator for which hierarchies other those of the
 * reasoner can be injected. Using those hierarchies means that only classes
 * from the hierarchies should occur in refinements.
 *
 */
public interface CustomHierarchyRefinementOperator extends RefinementOperator {
	interface Builder<T extends CustomHierarchyRefinementOperator>
			extends org.dllearner.core.Builder<T> {

		Builder<T> setClassHierarchy(ClassHierarchy classHierarchy);

		Builder<T> setObjectPropertyHierarchy(ObjectPropertyHierarchy objectPropertyHierarchy);

		Builder<T> setDataPropertyHierarchy(DatatypePropertyHierarchy dataPropertyHierarchy);

	}
}
