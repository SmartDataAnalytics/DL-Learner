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
	
	//public static boolean applyAllFilterDefault = true;
	//public static boolean applyExistsFilterDefault = true;
	//public static boolean useTooWeakListDefault = true;
	//public static boolean useOverlyGeneralListDefault = true;
	//public static boolean useShortConceptConstructionDefault = true;
	//public static boolean improveSubsumptionHierarchyDefault = true;
	public static boolean useAllConstructorDefault = true;
	public static boolean useExistsConstructorDefault = true;
	public static boolean useHasValueConstructorDefault = false;
	public static boolean useDataHasValueConstructorDefault = false;
	public static int valueFrequencyThresholdDefault = 3;
	public static boolean useCardinalityRestrictionsDefault = true;
	public static int cardinalityLimitDefault = 5;
	public static boolean useNegationDefault = true;
	public static boolean useBooleanDatatypesDefault = true;
	public static boolean useDoubleDatatypesDefault = true;
	public static boolean useStringDatatypesDefault = false;
	public static int maxExecutionTimeInSecondsDefault = 10;
	public static int minExecutionTimeInSecondsDefault = 0;
	public static int guaranteeXgoodDescriptionsDefault = 1;
	public static int maxClassDescriptionTestsDefault = 0;
	public static String logLevelDefault = "DEBUG";
	public static double noisePercentageDefault = 0.0;
	public static boolean terminateOnNoiseReachedDefault = true;
	public static boolean instanceBasedDisjointsDefault = true;
	
}
