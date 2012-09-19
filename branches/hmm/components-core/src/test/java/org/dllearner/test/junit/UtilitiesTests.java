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

package org.dllearner.test.junit;

import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Individual;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.dllearner.utilities.learn.UsedEntitiesDetection;
import org.junit.Test;

/**
 * Various tests for methods/classes in the utilities package.
 * 
 * @author Jens Lehmann
 *
 */
public class UtilitiesTests {

	@Test
	public void entityDetection() {
		AbstractReasonerComponent reasoner = TestOntologies.getTestOntology(TestOntology.DATA1);
		int maxDepth = 2;
		Set<Individual> individuals = new TreeSet<Individual>();
		individuals.add(new Individual("http://localhost/foo#tim"));
		UsedEntitiesDetection detection = new UsedEntitiesDetection(reasoner, individuals, maxDepth);
		System.out.println(detection);
	}
	
}
