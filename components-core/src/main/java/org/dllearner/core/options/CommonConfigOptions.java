/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.core.options;

/**
 * Contains methods for creating common configuration options, i.e. options
 * which are or may be of use for several components.
 * 
 * @author Jens Lehmann
 *
 */
public final class CommonConfigOptions {
	
	// some default values

	public static int valueFrequencyThresholdDefault = 3;
	public static int minExecutionTimeInSecondsDefault = 0;
	public static int guaranteeXgoodDescriptionsDefault = 1;
	public static int maxClassDescriptionTestsDefault = 0;
	public static String logLevelDefault = "DEBUG";

}
