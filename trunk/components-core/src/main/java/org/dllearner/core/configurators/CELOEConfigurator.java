/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import java.util.Set;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.configurators.RefinementOperatorConfigurator;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
@SuppressWarnings("all")
public  class CELOEConfigurator  extends RefinementOperatorConfigurator implements Configurator {

private boolean reinitNecessary = false;
private CELOE cELOE;

/**
* @param cELOE see CELOE
**/
public CELOEConfigurator(CELOE cELOE){
this.cELOE = cELOE;
}

/**
* @param reasoningService see reasoningService
* @param learningProblem see learningProblem
* @throws LearningProblemUnsupportedException see 
* @return CELOE
**/
public static CELOE getCELOE(AbstractLearningProblem learningProblem, AbstractReasonerComponent reasoningService) throws LearningProblemUnsupportedException{
CELOE component = ComponentManager.getInstance().learningAlgorithm(CELOE.class, learningProblem, reasoningService);
return component;
}

/**
* useAllConstructor specifies whether the universal concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseAllConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "useAllConstructor") ;
}
/**
* useExistsConstructor specifies whether the existential concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseExistsConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "useExistsConstructor") ;
}
/**
* useHasValueConstructor specifies whether the hasValue constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseHasValueConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "useHasValueConstructor") ;
}
/**
* useDataHasValueConstructor specifies whether the hasValue constructor is used in the learning algorithm in combination with data properties.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseDataHasValueConstructor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "useDataHasValueConstructor") ;
}
/**
* valueFrequencyThreshold specifies how often an object must occur as value in order to be considered for hasValue restrictions.
* mandatory: false| reinit necessary: true
* default value: 3
* @return int 
**/
public int getValueFrequencyThreshold() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "valueFrequencyThreshold") ;
}
/**
* useCardinalityRestrictions specifies whether CardinalityRestrictions is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseCardinalityRestrictions() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "useCardinalityRestrictions") ;
}
/**
* cardinalityLimit Gives the maximum number used in cardinality restrictions..
* mandatory: false| reinit necessary: true
* default value: 5
* @return int 
**/
public int getCardinalityLimit() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "cardinalityLimit") ;
}
/**
* useNegation specifies whether negation is used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseNegation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "useNegation") ;
}
/**
* useBooleanDatatypes specifies whether boolean datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseBooleanDatatypes() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "useBooleanDatatypes") ;
}
/**
* useDoubleDatatypes specifies whether double datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseDoubleDatatypes() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "useDoubleDatatypes") ;
}
/**
* maxExecutionTimeInSeconds algorithm will stop after specified seconds.
* mandatory: false| reinit necessary: true
* default value: 10
* @return int 
**/
public int getMaxExecutionTimeInSeconds() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "maxExecutionTimeInSeconds") ;
}
/**
* noisePercentage the (approximated) percentage of noise within the examples.
* mandatory: false| reinit necessary: true
* default value: 0.0
* @return double 
**/
public double getNoisePercentage() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "noisePercentage") ;
}
/**
* terminateOnNoiseReached specifies whether to terminate when noise criterion is met.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getTerminateOnNoiseReached() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "terminateOnNoiseReached") ;
}
/**
* maxDepth maximum depth of description.
* mandatory: false| reinit necessary: true
* default value: 7
* @return int 
**/
public int getMaxDepth() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "maxDepth") ;
}
/**
* maxNrOfResults Sets the maximum number of results one is interested in. (Setting this to a lower value may increase performance as the learning algorithm has to store/evaluate/beautify less descriptions)..
* mandatory: false| reinit necessary: true
* default value: 10
* @return int 
**/
public int getMaxNrOfResults() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "maxNrOfResults") ;
}
/**
* maxClassDescriptionTests The maximum number of candidate hypothesis the algorithm is allowed to test (0 = no limit). The algorithm will stop afterwards. (The real number of tests can be slightly higher, because this criterion usually won't be checked after each single test.).
* mandatory: false| reinit necessary: true
* default value: 0
* @return int 
**/
public int getMaxClassDescriptionTests() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "maxClassDescriptionTests") ;
}
/**
* singleSuggestionMode Use this if you are interested in only one suggestion and your learning problem has many (more than 1000) examples..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getSingleSuggestionMode() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "singleSuggestionMode") ;
}
/**
* instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getInstanceBasedDisjoints() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "instanceBasedDisjoints") ;
}
/**
* filterDescriptionsFollowingFromKB If true, then the results will not contain suggestions, which already follow logically from the knowledge base. Be careful, since this requires a potentially expensive consistency check for candidate solutions..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getFilterDescriptionsFollowingFromKB() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "filterDescriptionsFollowingFromKB") ;
}
/**
* reuseExistingDescription If true, the algorithm tries to find a good starting point close to an existing definition/super class of the given class in the knowledge base..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getReuseExistingDescription() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "reuseExistingDescription") ;
}
/**
* writeSearchTree specifies whether to write a search tree.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getWriteSearchTree() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "writeSearchTree") ;
}
/**
* searchTreeFile file to use for the search tree.
* mandatory: false| reinit necessary: true
* default value: log/searchTree.txt
* @return String 
**/
public String getSearchTreeFile() {
return (String) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "searchTreeFile") ;
}
/**
* replaceSearchTree specifies whether to replace the search tree in the log file after each run or append the new search tree.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getReplaceSearchTree() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "replaceSearchTree") ;
}
/**
* expansionPenaltyFactor heuristic penalty per syntactic construct used (lower = finds more complex expression, but might miss simple ones).
* mandatory: false| reinit necessary: true
* default value: 0.1
* @return double 
**/
public double getExpansionPenaltyFactor() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "expansionPenaltyFactor") ;
}
/**
* allowedConcepts concepts the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getAllowedConcepts() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "allowedConcepts") ;
}
/**
* ignoredConcepts concepts the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getIgnoredConcepts() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(cELOE,  "ignoredConcepts") ;
}

/**
* @param useAllConstructor specifies whether the universal concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseAllConstructor(boolean useAllConstructor) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "useAllConstructor", useAllConstructor);
reinitNecessary = true;
}
/**
* @param useExistsConstructor specifies whether the existential concept constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseExistsConstructor(boolean useExistsConstructor) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "useExistsConstructor", useExistsConstructor);
reinitNecessary = true;
}
/**
* @param useHasValueConstructor specifies whether the hasValue constructor is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseHasValueConstructor(boolean useHasValueConstructor) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "useHasValueConstructor", useHasValueConstructor);
reinitNecessary = true;
}
/**
* @param useDataHasValueConstructor specifies whether the hasValue constructor is used in the learning algorithm in combination with data properties.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseDataHasValueConstructor(boolean useDataHasValueConstructor) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "useDataHasValueConstructor", useDataHasValueConstructor);
reinitNecessary = true;
}
/**
* @param valueFrequencyThreshold specifies how often an object must occur as value in order to be considered for hasValue restrictions.
* mandatory: false| reinit necessary: true
* default value: 3
**/
public void setValueFrequencyThreshold(int valueFrequencyThreshold) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "valueFrequencyThreshold", valueFrequencyThreshold);
reinitNecessary = true;
}
/**
* @param useCardinalityRestrictions specifies whether CardinalityRestrictions is used in the learning algorithm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseCardinalityRestrictions(boolean useCardinalityRestrictions) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "useCardinalityRestrictions", useCardinalityRestrictions);
reinitNecessary = true;
}
/**
* @param cardinalityLimit Gives the maximum number used in cardinality restrictions..
* mandatory: false| reinit necessary: true
* default value: 5
**/
public void setCardinalityLimit(int cardinalityLimit) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "cardinalityLimit", cardinalityLimit);
reinitNecessary = true;
}
/**
* @param useNegation specifies whether negation is used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseNegation(boolean useNegation) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "useNegation", useNegation);
reinitNecessary = true;
}
/**
* @param useBooleanDatatypes specifies whether boolean datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseBooleanDatatypes(boolean useBooleanDatatypes) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "useBooleanDatatypes", useBooleanDatatypes);
reinitNecessary = true;
}
/**
* @param useDoubleDatatypes specifies whether double datatypes are used in the learning algorothm.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseDoubleDatatypes(boolean useDoubleDatatypes) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "useDoubleDatatypes", useDoubleDatatypes);
reinitNecessary = true;
}
/**
* @param maxExecutionTimeInSeconds algorithm will stop after specified seconds.
* mandatory: false| reinit necessary: true
* default value: 10
**/
public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "maxExecutionTimeInSeconds", maxExecutionTimeInSeconds);
reinitNecessary = true;
}
/**
* @param noisePercentage the (approximated) percentage of noise within the examples.
* mandatory: false| reinit necessary: true
* default value: 0.0
**/
public void setNoisePercentage(double noisePercentage) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "noisePercentage", noisePercentage);
reinitNecessary = true;
}
/**
* @param terminateOnNoiseReached specifies whether to terminate when noise criterion is met.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setTerminateOnNoiseReached(boolean terminateOnNoiseReached) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "terminateOnNoiseReached", terminateOnNoiseReached);
reinitNecessary = true;
}
/**
* @param maxDepth maximum depth of description.
* mandatory: false| reinit necessary: true
* default value: 7
**/
public void setMaxDepth(int maxDepth) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "maxDepth", maxDepth);
reinitNecessary = true;
}
/**
* @param maxNrOfResults Sets the maximum number of results one is interested in. (Setting this to a lower value may increase performance as the learning algorithm has to store/evaluate/beautify less descriptions)..
* mandatory: false| reinit necessary: true
* default value: 10
**/
public void setMaxNrOfResults(int maxNrOfResults) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "maxNrOfResults", maxNrOfResults);
reinitNecessary = true;
}
/**
* @param maxClassDescriptionTests The maximum number of candidate hypothesis the algorithm is allowed to test (0 = no limit). The algorithm will stop afterwards. (The real number of tests can be slightly higher, because this criterion usually won't be checked after each single test.).
* mandatory: false| reinit necessary: true
* default value: 0
**/
public void setMaxClassDescriptionTests(int maxClassDescriptionTests) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "maxClassDescriptionTests", maxClassDescriptionTests);
reinitNecessary = true;
}
/**
* @param singleSuggestionMode Use this if you are interested in only one suggestion and your learning problem has many (more than 1000) examples..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setSingleSuggestionMode(boolean singleSuggestionMode) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "singleSuggestionMode", singleSuggestionMode);
reinitNecessary = true;
}
/**
* @param instanceBasedDisjoints Specifies whether to use real disjointness checks or instance based ones (no common instances) in the refinement operator..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setInstanceBasedDisjoints(boolean instanceBasedDisjoints) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "instanceBasedDisjoints", instanceBasedDisjoints);
reinitNecessary = true;
}
/**
* @param filterDescriptionsFollowingFromKB If true, then the results will not contain suggestions, which already follow logically from the knowledge base. Be careful, since this requires a potentially expensive consistency check for candidate solutions..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setFilterDescriptionsFollowingFromKB(boolean filterDescriptionsFollowingFromKB) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "filterDescriptionsFollowingFromKB", filterDescriptionsFollowingFromKB);
reinitNecessary = true;
}
/**
* @param reuseExistingDescription If true, the algorithm tries to find a good starting point close to an existing definition/super class of the given class in the knowledge base..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setReuseExistingDescription(boolean reuseExistingDescription) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "reuseExistingDescription", reuseExistingDescription);
reinitNecessary = true;
}
/**
* @param writeSearchTree specifies whether to write a search tree.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setWriteSearchTree(boolean writeSearchTree) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "writeSearchTree", writeSearchTree);
reinitNecessary = true;
}
/**
* @param searchTreeFile file to use for the search tree.
* mandatory: false| reinit necessary: true
* default value: log/searchTree.txt
**/
public void setSearchTreeFile(String searchTreeFile) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "searchTreeFile", searchTreeFile);
reinitNecessary = true;
}
/**
* @param replaceSearchTree specifies whether to replace the search tree in the log file after each run or append the new search tree.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setReplaceSearchTree(boolean replaceSearchTree) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "replaceSearchTree", replaceSearchTree);
reinitNecessary = true;
}
/**
* @param expansionPenaltyFactor heuristic penalty per syntactic construct used (lower = finds more complex expression, but might miss simple ones).
* mandatory: false| reinit necessary: true
* default value: 0.1
**/
public void setExpansionPenaltyFactor(double expansionPenaltyFactor) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "expansionPenaltyFactor", expansionPenaltyFactor);
reinitNecessary = true;
}
/**
* @param allowedConcepts concepts the algorithm is allowed to use.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setAllowedConcepts(Set<String> allowedConcepts) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "allowedConcepts", allowedConcepts);
reinitNecessary = true;
}
/**
* @param ignoredConcepts concepts the algorithm must ignore.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setIgnoredConcepts(Set<String> ignoredConcepts) {
ComponentManager.getInstance().applyConfigEntry(cELOE, "ignoredConcepts", ignoredConcepts);
reinitNecessary = true;
}

/**
* true, if this component needs reinitializsation.
* @return boolean
**/
public boolean isReinitNecessary(){
return reinitNecessary;
}


}
