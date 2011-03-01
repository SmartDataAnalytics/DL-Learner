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
package org.dllearner.core.configurators;

/**
 * Common options of refinement operators (manually created interface).
 * 
 * @author Jens Lehmann
 * 
 */
public abstract class RefinementOperatorConfigurator {

	public abstract boolean getUseCardinalityRestrictions();

	public abstract boolean getUseNegation();

	public abstract boolean getUseAllConstructor();

	public abstract boolean getUseExistsConstructor();

	public abstract boolean getUseBooleanDatatypes();	
	
	// below are optional parameters (neutral return values choosen)
	
	public abstract boolean getInstanceBasedDisjoints();
	
	public boolean getUseHasValueConstructor() {
		return false;
	}

	public boolean getUseDataHasValueConstructor() {
		return false;
	}
	
	public int getValueFrequencyThreshold() {
		return 3;
	}

	public int getCardinalityLimit() {
		return 5;
	}

	public boolean getUseDoubleDatatypes() {
		return false;
	}
	
	public boolean getUseStringDatatypes() {
		return false;
	}	
}
