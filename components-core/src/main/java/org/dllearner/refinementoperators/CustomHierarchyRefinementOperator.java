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
 * @author Jens Lehmann
 *
 */
public interface CustomHierarchyRefinementOperator extends RefinementOperator {

	void setClassHierarchy(ClassHierarchy classHierarchy);
	
	void setObjectPropertyHierarchy(ObjectPropertyHierarchy objectPropertyHierarchy);
	
	void setDataPropertyHierarchy(DatatypePropertyHierarchy dataPropertyHierarchy);
		
}
