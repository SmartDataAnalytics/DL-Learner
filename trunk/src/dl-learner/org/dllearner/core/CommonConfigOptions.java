/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.core;

/**
 * @author Jens Lehmann
 *
 */
public final class CommonConfigOptions {

	public static final IntegerConfigOption getVerbosityOption() {
		// TODO: temporary code
		IntegerConfigOption verbosityOption = new IntegerConfigOption("verbosity", "control verbosity of output");
		return verbosityOption;
	}
	
	public static DoubleConfigOption getPercentPerLenghtUnitOption(double defaultValue) {
		DoubleConfigOption option = new DoubleConfigOption("percentPerLenghtUnit", "describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one", defaultValue);
		option.setLowerLimit(0.0);
		option.setUpperLimit(1.0);
		return option;
	}
	
	public static StringConfigOption getReturnType() {
		return new StringConfigOption("returnType", "Specifies the type which the solution has to belong to (if already) known. This means we inform the learning algorithm that the solution is a subclass of this type.");
	}
}
