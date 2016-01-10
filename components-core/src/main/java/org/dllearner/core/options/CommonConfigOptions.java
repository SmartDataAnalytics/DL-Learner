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
