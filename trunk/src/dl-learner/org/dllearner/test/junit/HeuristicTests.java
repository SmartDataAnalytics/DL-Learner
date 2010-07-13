/**
 * Copyright (C) 2007-2010, Jens Lehmann
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

import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.junit.Test;

/**
 * Tests for various heuristics employed in learning problems.
 * 
 * @author Jens Lehmann
 * 
 */
public class HeuristicTests {

	@Test
	public void classLearningTests() {
		// create artificial ontology
		KB kb = new KB();
		NamedClass[] nc = new NamedClass[5];
		for(int i=0; i<5; i++) {
			nc[i] = new NamedClass("A" + i);
		}
		Individual[] ind = new Individual[5];
		for(int i=0; i<100; i++) {
			ind[i] = new Individual("a" + i);
		}
		
		for(int i=0; i<20; i++) {
			kb.addAxiom(new ClassAssertionAxiom(nc[0],ind[i]));
		}
	}
	
}
