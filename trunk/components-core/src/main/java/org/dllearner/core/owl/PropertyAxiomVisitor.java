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
 * Visitor for property (RBox) axioms.
 * 
 * @author Jens Lehmann
 *
 */
public interface PropertyAxiomVisitor {

	public void visit(FunctionalObjectPropertyAxiom axiom);
	
	public void visit(InverseObjectPropertyAxiom axiom);
	
	public void visit(SymmetricObjectPropertyAxiom axiom);
	
	public void visit(TransitiveObjectPropertyAxiom axiom);
	
	public void visit(SubObjectPropertyAxiom axiom);
	
	void visit(DatatypePropertyDomainAxiom axiom);

	void visit(ObjectPropertyDomainAxiom axiom);
	
	void visit(DatatypePropertyRangeAxiom axiom);

	void visit(ObjectPropertyRangeAxiom axiom);	
}
