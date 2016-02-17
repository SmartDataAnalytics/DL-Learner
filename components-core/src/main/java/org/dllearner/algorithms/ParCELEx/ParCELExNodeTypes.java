package org.dllearner.algorithms.ParCELEx;


/**
 * This class declares constants representing types of definitions and how they were created
 * <ol>
 * 	<li>types: partial / counter partial definition</li>
 * 	<li>created by: directly from the refinement, combined with cpdef (for pdef), online / lazy, etc.</li>
 * </ol>
 * 
 * This is used in ParCELEx to keep the type of partial definition to use in other tasks such as 
 * 	reduction, statistic, etc.
 * 
 * @author An C. Tran
 *
 */

public class ParCELExNodeTypes {

	/**
	 * The new created and unused counter partial definition
	 */
	public static final int COUNTER_PARTIAL_DEFINITION_UNUSED = 0;
	
	
	/**
	 * Partial definition which is created directly from the refinement 
	 */
	public static final int PARTIAL_DEFINITION_DIRECT_REFINED = 1;
	
	
	/**
	 * Counter partial definition which had been combined with descriptions to 
	 * constitute the partial definitions   
	 */
	public static final int COUNTER_PARTIAL_DEFINITION_USED = 1;
	
	
	/**
	 * Partial definition which is created by the combination of a description in refinements and counter partial definition(s)
	 * on the fly (that means it is created in the worker, after the refinement)
	 */
	public static final int PARTIAL_DEFINITION_ONLINE_COMBINATION = 3;
	
	
	/**
	 * Partial definition which is created by the combination of a description and counter partial definition(s)
	 * after the learning finishes (Lazy combination)
	 */
	public static final int PARTIAL_DEFINITION_LAZY_COMBINATION = 2;
	
	
	/**
	 * Partial definition which is created by the combination of the refined node and the new counter partial 
	 * definition 
	 */
	public static final int PARTIAL_DEFINITION_REFINED_NODE = 4;
	
	
	/**
	 * Partial definition which is created by the combination of the refined node and the new counter partial 
	 * definition 
	 */
	public static final int POTENTIAL_PARTIAL_DEFINITION = 5;

	
}
