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
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.learningproblems.PosNegLPStandard;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class PosNegLPStandardConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private PosNegLPStandard posNegLPStandard;

/**
* @param posNegLPStandard see PosNegLPStandard
**/
public PosNegLPStandardConfigurator(PosNegLPStandard posNegLPStandard){
this.posNegLPStandard = posNegLPStandard;
}

/**
* @param reasoningService see reasoningService
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @return PosNegLPStandard
**/
public static PosNegLPStandard getPosNegLPStandard(ReasonerComponent reasoningService, Set<String> positiveExamples, Set<String> negativeExamples) {
PosNegLPStandard component = ComponentManager.getInstance().learningProblem(PosNegLPStandard.class, reasoningService);
ComponentManager.getInstance().applyConfigEntry(component, "positiveExamples", positiveExamples);
ComponentManager.getInstance().applyConfigEntry(component, "negativeExamples", negativeExamples);
return component;
}

/**
* positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getPositiveExamples() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posNegLPStandard,  "positiveExamples") ;
}
/**
* negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getNegativeExamples() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posNegLPStandard,  "negativeExamples") ;
}
/**
* useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseRetrievalForClassficiation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(posNegLPStandard,  "useRetrievalForClassficiation") ;
}
/**
* percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
* @return double 
**/
public double getPercentPerLenghtUnit() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegLPStandard,  "percentPerLenghtUnit") ;
}
/**
* useMultiInstanceChecks See UseMultiInstanceChecks enum..
* mandatory: false| reinit necessary: true
* default value: twoChecks
* @return String 
**/
public String getUseMultiInstanceChecks() {
return (String) ComponentManager.getInstance().getConfigOptionValue(posNegLPStandard,  "useMultiInstanceChecks") ;
}
/**
* useApproximations whether to use stochastic approximations for computing accuracy.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseApproximations() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(posNegLPStandard,  "useApproximations") ;
}
/**
* approxAccuracy accuracy of the approximation (only for expert use).
* mandatory: false| reinit necessary: true
* default value: 0.05
* @return double 
**/
public double getApproxAccuracy() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegLPStandard,  "approxAccuracy") ;
}
/**
* accuracyMethod Specifies, which method/function to use for computing accuracy..
* mandatory: false| reinit necessary: true
* default value: predacc
* @return String 
**/
public String getAccuracyMethod() {
return (String) ComponentManager.getInstance().getConfigOptionValue(posNegLPStandard,  "accuracyMethod") ;
}

/**
* @param positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setPositiveExamples(Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStandard, "positiveExamples", positiveExamples);
}
/**
* @param negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setNegativeExamples(Set<String> negativeExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStandard, "negativeExamples", negativeExamples);
}
/**
* @param useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseRetrievalForClassficiation(boolean useRetrievalForClassficiation) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStandard, "useRetrievalForClassficiation", useRetrievalForClassficiation);
reinitNecessary = true;
}
/**
* @param percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
**/
public void setPercentPerLenghtUnit(double percentPerLenghtUnit) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStandard, "percentPerLenghtUnit", percentPerLenghtUnit);
reinitNecessary = true;
}
/**
* @param useMultiInstanceChecks See UseMultiInstanceChecks enum..
* mandatory: false| reinit necessary: true
* default value: twoChecks
**/
public void setUseMultiInstanceChecks(String useMultiInstanceChecks) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStandard, "useMultiInstanceChecks", useMultiInstanceChecks);
reinitNecessary = true;
}
/**
* @param useApproximations whether to use stochastic approximations for computing accuracy.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseApproximations(boolean useApproximations) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStandard, "useApproximations", useApproximations);
reinitNecessary = true;
}
/**
* @param approxAccuracy accuracy of the approximation (only for expert use).
* mandatory: false| reinit necessary: true
* default value: 0.05
**/
public void setApproxAccuracy(double approxAccuracy) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStandard, "approxAccuracy", approxAccuracy);
reinitNecessary = true;
}
/**
* @param accuracyMethod Specifies, which method/function to use for computing accuracy..
* mandatory: false| reinit necessary: true
* default value: predacc
**/
public void setAccuracyMethod(String accuracyMethod) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStandard, "accuracyMethod", accuracyMethod);
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
