package org.dllearner.core;

import org.dllearner.core.fuzzydll.FuzzyIndividualReasoner;

/**
 * List of available reasoning/query methods.
 * 
 * @author Jens Lehmann
 *
 */
public interface Reasoner extends BaseReasoner, SchemaReasoner, IndividualReasoner, FuzzyIndividualReasoner {

}
