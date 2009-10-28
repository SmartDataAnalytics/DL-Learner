/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.tools.protege;


/**
 * This Class handles the manchester String, the normal string and if the Individual is a positive Individual. 
 * @author Christian Koetteritzsch
 *
 */
public class IndividualObject {

	private final String normalIndividual;
	private boolean isPos;
	private String additionalInformation;
	
	/**
	 * Constructor for the IndividualObject.
	 * @param normal String
	 * @param pos boolean
	 */
	public IndividualObject(String normal, boolean pos) {
		normalIndividual = normal;
		isPos = pos;
	}
	
	/**
	 * This method returns the String of the Individual.
	 * @return String normalIndividual
	 */
	public String getIndividualString() {
		return normalIndividual;
	}
	
	/**
	 * This method returns if the Example is a positive Example.
	 * @return boolean isPos
	 */
	public boolean isPositiveExample() {
		return isPos;
	}
	
	/**
	 * This method sets the example positive or negative if changed to the other list.
	 * @param pos boolean
	 */
	public void setExamplePositive(boolean pos) {
		isPos = pos;
	}

	public void setAdditionalInformation(String additional) {
		this.additionalInformation = additional;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}
	
}
