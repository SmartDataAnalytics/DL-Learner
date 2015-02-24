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

package org.dllearner.learningproblems;

import java.util.Set;

import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Jens Lehmann
 *
 */
public class ScorePosOnly extends Score {

	private static final long serialVersionUID = 2191608162129054464L;
	
	private Set<OWLIndividual> coveredInstances;
	private Set<OWLIndividual> notCoveredPositives;
	private Set<OWLIndividual> additionalInstances;
	
	private double coverage;
	private double addition;
	private double accuracy;	
	
	public ScorePosOnly(Set<OWLIndividual> coveredInstances, Set<OWLIndividual> notCoveredPositives, double coverage, Set<OWLIndividual> additionalInstances, double protusion, double accuracy) {
		this.coveredInstances = coveredInstances;
		this.notCoveredPositives = notCoveredPositives;
		this.additionalInstances = additionalInstances;
		this.coverage = coverage;
		this.addition = protusion;
		this.accuracy = accuracy;		
	}
	
	/**
	 * @return Coverage of the class description.
	 */
	public double getCoverage() {
		return coverage;
	}

	/**
	 * Let C be the considered class OWLClassExpression and A the class to learn. 
	 * The addition number is calculated as the number of instances of C which are also
	 * instances of A divided by the number of instances of C.
	 * @return Additional instances of the class description.
	 */
	public double getAddition() {
		return addition;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Score#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
//		return 0.5 * (coverage + addition);
		return accuracy;
	}

	/**
	 * @return the coveredInstances
	 */
	public Set<OWLIndividual> getCoveredInstances() {
		return coveredInstances;
	}

	/**
	 * @return the coveredInstances
	 */
	public Set<OWLIndividual> getNotCoveredPositives() {
		return notCoveredPositives;
	}
	
	/**
	 * @return the additionalInstances
	 */
	public Set<OWLIndividual> getAdditionalInstances() {
		return additionalInstances;
	}		

}
