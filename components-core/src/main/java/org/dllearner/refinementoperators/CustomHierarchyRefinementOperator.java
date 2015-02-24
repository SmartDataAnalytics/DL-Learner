/**
 * Copyright (C) 2007-2012, Jens Lehmann
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

	public void setClassHierarchy(ClassHierarchy classHierarchy);
	
	public void setObjectPropertyHierarchy(ObjectPropertyHierarchy objectPropertyHierarchy);
	
	public void setDataPropertyHierarchy(DatatypePropertyHierarchy dataPropertyHierarchy);
		
}
