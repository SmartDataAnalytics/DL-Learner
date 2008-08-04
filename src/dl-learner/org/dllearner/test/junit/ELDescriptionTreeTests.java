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
package org.dllearner.test.junit;

import static org.junit.Assert.*;

import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.algorithms.el.Simulation;
import org.dllearner.algorithms.el.TreeTuple;
import org.junit.Test;

/**
 * Tests related to EL description tree including operations on
 * them, simulations, equivalence checks, minimisation etc.
 * 
 * @author Jens Lehmann
 *
 */
public final class ELDescriptionTreeTests {

	@Test
	public void simulationTest() {
		Simulation s = new Simulation();
		ELDescriptionNode t1 = new ELDescriptionNode();
		ELDescriptionNode t2 = new ELDescriptionNode();
		TreeTuple tuple1 = new TreeTuple(t1,t2);
		s.addTuple(tuple1);
		assertTrue(s.in(t2).size() == 1);
		assertTrue(s.out(t2).size() == 0);
	}
	
}
