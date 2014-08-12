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

package org.dllearner.utilities.datastructures;

import java.util.LinkedList;
import java.util.List;

import org.dllearner.core.owl.Individual;

/**
 * A tuple of training set and test set.
 * 
 * @author Jens Lehmann
 *
 */
public class TrainTestList {

	private List<OWLIndividual> trainList;
	private List<OWLIndividual> testList;
	
	public TrainTestList() {
		trainList = new LinkedList<OWLIndividual>();
		testList = new LinkedList<OWLIndividual>();
	}
	
	public TrainTestList(List<OWLIndividual> trainList, List<OWLIndividual> testList) {
		this.trainList = trainList;
		this.testList = testList;
	}

	public List<OWLIndividual> getTrainList() {
		return trainList;
	}

	public List<OWLIndividual> getTestList() {
		return testList;
	}
	
}
