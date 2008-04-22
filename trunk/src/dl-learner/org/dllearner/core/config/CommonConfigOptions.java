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
package org.dllearner.core.config;


/**
 * Contains methods for creating common configuration options, i.e. options
 * which are or may be of use for several components. 
 * 
 * @author Jens Lehmann
 *
 */
public final class CommonConfigOptions {
	
	//public static boolean applyAllFilterDefault = true;
	//public static boolean applyExistsFilterDefault = true;	
	//public static boolean useTooWeakListDefault = true;
	//public static boolean useOverlyGeneralListDefault = true;
	//public static boolean useShortConceptConstructionDefault = true;
	//public static boolean improveSubsumptionHierarchyDefault = true;
	public static boolean useAllConstructorDefault = true;
	public static boolean useExistsConstructorDefault = true;
	public static boolean useCardinalityRestrictionsDefault = true;
	public static boolean useNegationDefault = true;
	public static boolean useBooleanDatatypesDefault = true;
	public static int maxExecutionTimeInSecondsDefault = 0;
	public static int minExecutionTimeInSecondsDefault = 0;
	public static int guaranteeXgoodDescriptionsDefault = 1;
	//public static double noisePercentageDefault = 0.0;
	
	

	public static StringConfigOption getVerbosityOption() {
		StringConfigOption verbosityOption = new StringConfigOption("verbosity", "control verbosity of output for this component", "warning");
		String[] allowedValues = new String[] {"quiet", "error", "warning", "notice", "info", "debug"};
		verbosityOption.setAllowedValues(allowedValues);
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
	
	public static BooleanConfigOption getUNA() {
		return new BooleanConfigOption("una", "unique names assumption", false);
	}
	
	public static BooleanConfigOption getOWA() {
		return new BooleanConfigOption("owa", "open world assumption (if set to false, we try to close the world", true);
	}
	
	public static StringSetConfigOption allowedConcepts() {
		return new StringSetConfigOption("allowedConcepts", "concepts the algorithm is allowed to use");
	}
	
	public static StringSetConfigOption allowedRoles() {
		return new StringSetConfigOption("allowedRoles", "roles the algorithm is allowed to use");
	}
	
	public static StringSetConfigOption ignoredConcepts() {
		return new StringSetConfigOption("ignoredConcepts", "concepts the algorithm must ignore");
	}
	
	public static StringSetConfigOption ignoredRoles() {
		return new StringSetConfigOption("ignoredRoles", "roles the algorithm must ignore");
	}	
	
	public static BooleanConfigOption useAllConstructor() {
		return new BooleanConfigOption("useAllConstructor", "specifies whether the universal concept constructor is used in the learning algorithm",useAllConstructorDefault);
	}
	
	public static BooleanConfigOption useExistsConstructor() {
		return new BooleanConfigOption("useExistsConstructor", "specifies whether the existential concept constructor is used in the learning algorithm",useExistsConstructorDefault);
	}
	
	public static BooleanConfigOption useCardinalityRestrictions() {
		return new BooleanConfigOption("useCardinalityRestrictions", "specifies whether CardinalityRestrictions is used in the learning algorithm",useCardinalityRestrictionsDefault);
	}
	
	public static BooleanConfigOption useNegation() {
		return new BooleanConfigOption("useNegation", "specifies whether negation is used in the learning algorothm",useNegationDefault);
	}
	
	public static BooleanConfigOption useBooleanDatatypes() {
		return new BooleanConfigOption("useBooleanDatatypes", "specifies whether boolean datatypes are used in the learning algorothm",useBooleanDatatypesDefault);
	}	
	
	public static IntegerConfigOption maxExecutionTimeInSeconds() {
		return new IntegerConfigOption("maxExecutionTimeInSeconds", "algorithm will stop after specified seconds",maxExecutionTimeInSecondsDefault);
	}	
	
	public static IntegerConfigOption minExecutionTimeInSeconds() {
		return new IntegerConfigOption("minExecutionTimeInSeconds", "algorithm will run at least specified seconds",minExecutionTimeInSecondsDefault);
	}	
	
	public static IntegerConfigOption guaranteeXgoodDescriptions() {
		return new IntegerConfigOption("guaranteeXgoodDescriptions", "algorithm will run until X good (100%) concept descritpions are found",guaranteeXgoodDescriptionsDefault);
	}	
}
