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

package org.dllearner.core.configurators;

import java.util.Set;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.fuzzydll.FuzzyPosNegLPStandard;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class FuzzyPosNegLPStandardConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private FuzzyPosNegLPStandard fuzzyPosNegLPStandard;

/**
* @param fuzzyPosNegLPStandard see FuzzyPosNegLPStandard
**/
public FuzzyPosNegLPStandardConfigurator(FuzzyPosNegLPStandard fuzzyPosNegLPStandard){
this.fuzzyPosNegLPStandard = fuzzyPosNegLPStandard;
}

/**
* @param reasoningService see reasoningService
* @param fuzzyExamples fuzzy examples
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @return FuzzyPosNegLPStandard
**/
public static FuzzyPosNegLPStandard getFuzzyPosNegLPStandard(AbstractReasonerComponent reasoningService, Set<Object> fuzzyExamples, Set<String> positiveExamples, Set<String> negativeExamples) {
FuzzyPosNegLPStandard component = ComponentManager.getInstance().learningProblem(FuzzyPosNegLPStandard.class, reasoningService);
ComponentManager.getInstance().applyConfigEntry(component, "fuzzyExamples", fuzzyExamples);
ComponentManager.getInstance().applyConfigEntry(component, "positiveExamples", positiveExamples);
ComponentManager.getInstance().applyConfigEntry(component, "negativeExamples", negativeExamples);
return component;
}

/**
* fuzzyExamples fuzzy examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(Object) 
**/
public Set<Object> getFuzzyExamples() {
return (Set<Object>) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "fuzzyExamples") ;
}
/**
* positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getPositiveExamples() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "positiveExamples") ;
}
/**
* negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getNegativeExamples() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "negativeExamples") ;
}
/**
* useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept. - NO LONGER FULLY SUPPORTED..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseRetrievalForClassficiation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "useRetrievalForClassficiation") ;
}
/**
* percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
* @return double 
**/
public double getPercentPerLenghtUnit() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "percentPerLenghtUnit") ;
}
/**
* useMultiInstanceChecks See UseMultiInstanceChecks enum. - NO LONGER FULLY SUPPORTED..
* mandatory: false| reinit necessary: true
* default value: twoChecks
* @return String 
**/
public String getUseMultiInstanceChecks() {
return (String) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "useMultiInstanceChecks") ;
}
/**
* useApproximations whether to use stochastic approximations for computing accuracy.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseApproximations() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "useApproximations") ;
}
/**
* approxAccuracy accuracy of the approximation (only for expert use).
* mandatory: false| reinit necessary: true
* default value: 0.05
* @return double 
**/
public double getApproxAccuracy() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "approxAccuracy") ;
}
/**
* accuracyMethod Specifies, which method/function to use for computing accuracy..
* mandatory: false| reinit necessary: true
* default value: predacc
* @return String 
**/
public String getAccuracyMethod() {
return (String) ComponentManager.getInstance().getConfigOptionValue(fuzzyPosNegLPStandard,  "accuracyMethod") ;
}

/**
* @param fuzzyExamples fuzzy examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setFuzzyExamples(Set<Object> fuzzyExamples) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "fuzzyExamples", fuzzyExamples);
}
/**
* @param positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setPositiveExamples(Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "positiveExamples", positiveExamples);
}
/**
* @param negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setNegativeExamples(Set<String> negativeExamples) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "negativeExamples", negativeExamples);
}
/**
* @param useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept. - NO LONGER FULLY SUPPORTED..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseRetrievalForClassficiation(boolean useRetrievalForClassficiation) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "useRetrievalForClassficiation", useRetrievalForClassficiation);
reinitNecessary = true;
}
/**
* @param percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
**/
public void setPercentPerLenghtUnit(double percentPerLenghtUnit) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "percentPerLenghtUnit", percentPerLenghtUnit);
reinitNecessary = true;
}
/**
* @param useMultiInstanceChecks See UseMultiInstanceChecks enum. - NO LONGER FULLY SUPPORTED..
* mandatory: false| reinit necessary: true
* default value: twoChecks
**/
public void setUseMultiInstanceChecks(String useMultiInstanceChecks) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "useMultiInstanceChecks", useMultiInstanceChecks);
reinitNecessary = true;
}
/**
* @param useApproximations whether to use stochastic approximations for computing accuracy.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseApproximations(boolean useApproximations) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "useApproximations", useApproximations);
reinitNecessary = true;
}
/**
* @param approxAccuracy accuracy of the approximation (only for expert use).
* mandatory: false| reinit necessary: true
* default value: 0.05
**/
public void setApproxAccuracy(double approxAccuracy) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "approxAccuracy", approxAccuracy);
reinitNecessary = true;
}
/**
* @param accuracyMethod Specifies, which method/function to use for computing accuracy..
* mandatory: false| reinit necessary: true
* default value: predacc
**/
public void setAccuracyMethod(String accuracyMethod) {
ComponentManager.getInstance().applyConfigEntry(fuzzyPosNegLPStandard, "accuracyMethod", accuracyMethod);
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
