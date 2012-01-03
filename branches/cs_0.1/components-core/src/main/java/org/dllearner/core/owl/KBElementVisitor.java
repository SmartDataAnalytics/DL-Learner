/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.core.owl;

/**
 * Visitor for all elements of a knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public interface KBElementVisitor extends AxiomVisitor, DescriptionVisitor, PropertyExpressionVisitor {

	void visit(Datatype datatype);	
	
	void visit(BooleanDataRange booleanDataRange);

	void visit(DoubleMaxValue doubleMaxValue);

	void visit(DoubleMinValue doubleMinValue);

	void visit(Individual individual);

	void visit(KB kb);

	void visit(TypedConstant typedConstant);

	void visit(UntypedConstant untypedConstant);

	void visit(Annotation annotation);

}
