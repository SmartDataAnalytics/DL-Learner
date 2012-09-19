/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.core.owl;

import org.dllearner.algorithms.gp.ADC;

/**
 * Visitor for elements in complex class descriptions (it supports 
 * the currently relevant ones - needs to be extended when further
 * constructs are needed).
 * 
 * @author Jens Lehmann
 *
 */
public interface DescriptionVisitor {
	
	public void visit(NamedClass description);
	
	public void visit(ADC description);
	
	public void visit(Negation description);
	
	public void visit(ObjectAllRestriction description);
	
	public void visit(ObjectSomeRestriction description);
	
	public void visit(DatatypeSomeRestriction description);
	
	public void visit(Nothing description);
	
	public void visit(Thing description);
	
	public void visit(Intersection description);
	
	public void visit(Union description);	
	
	public void visit(ObjectOneOf description);	
	
	public void visit(ObjectMinCardinalityRestriction description);
	
	public void visit(ObjectExactCardinalityRestriction description);
	
	public void visit(ObjectMaxCardinalityRestriction description);
	
	public void visit(ObjectValueRestriction description);
	
	public void visit(DatatypeMinCardinalityRestriction description);
	
	public void visit(DatatypeExactCardinalityRestriction description);
	
	public void visit(DatatypeMaxCardinalityRestriction description);	
	
	public void visit(DatatypeValueRestriction description);
	
}
