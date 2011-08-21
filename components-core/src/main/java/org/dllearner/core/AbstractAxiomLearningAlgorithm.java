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

package org.dllearner.core;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.Axiom;

/**
 * @author Lorenz Bühmann
 * @author Jens Lehmann
 */
public class AbstractAxiomLearningAlgorithm extends AbstractComponent implements AxiomLearningAlgorithm{

	@Override
	public void start() {
	}

	@Override
	public void init() throws ComponentInitException {
	}

	@Override
	public List<Axiom> getCurrentlyBestAxioms() {
		return null;
	}
	
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		return getCurrentlyBestAxioms(nrOfAxioms, 0.0);
	}
	
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms,
			double accuracyThreshold) {
		List<Axiom> bestAxioms = new ArrayList<Axiom>();
		for(EvaluatedAxiom evAx : getCurrentlyBestEvaluatedAxioms(nrOfAxioms, accuracyThreshold)){
			bestAxioms.add(evAx.getAxiom());
		}
		return bestAxioms;
	}

	@Override
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms() {
		return null;
	}

	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms) {
		return getCurrentlyBestEvaluatedAxioms(nrOfAxioms, 0.0);
	}

	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms,
			double accuracyThreshold) {
		List<EvaluatedAxiom> returnList = new ArrayList<EvaluatedAxiom>();
		
		//get the currently best evaluated axioms
		List<EvaluatedAxiom> currentlyBestEvAxioms = getCurrentlyBestEvaluatedAxioms();
		
		for(EvaluatedAxiom evAx : currentlyBestEvAxioms){
			if(evAx.getScore().getAccuracy() >= accuracyThreshold && returnList.size() < nrOfAxioms){
				returnList.add(evAx);
			}
		}
		
		return returnList;
	}

	@Override
	public Configurator getConfigurator() {
		// TODO Auto-generated method stub
		return null;
	}

}
