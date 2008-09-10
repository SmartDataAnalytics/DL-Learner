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
import org.dllearner.core.ReasoningService;
import org.dllearner.learningproblems.PosNegDefinitionLPStrict;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class PosNegDefinitionLPStrictConfigurator implements Configurator {

private boolean reinitNecessary = false;
@SuppressWarnings("unused")

private PosNegDefinitionLPStrict posNegDefinitionLPStrict;

/**
* @param posNegDefinitionLPStrict see PosNegDefinitionLPStrict
**/
public PosNegDefinitionLPStrictConfigurator(PosNegDefinitionLPStrict posNegDefinitionLPStrict){
this.posNegDefinitionLPStrict = posNegDefinitionLPStrict;
}

/**
* @param reasoningService see reasoningService
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @return PosNegDefinitionLPStrict
**/
public static PosNegDefinitionLPStrict getPosNegDefinitionLPStrict(ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples) {
PosNegDefinitionLPStrict component = ComponentManager.getInstance().learningProblem(PosNegDefinitionLPStrict.class, reasoningService);
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
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posNegDefinitionLPStrict,  "positiveExamples") ;
}
/**
* negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getNegativeExamples() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posNegDefinitionLPStrict,  "negativeExamples") ;
}
/**
* useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseRetrievalForClassficiation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(posNegDefinitionLPStrict,  "useRetrievalForClassficiation") ;
}
/**
* percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
* @return double 
**/
public double getPercentPerLenghtUnit() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegDefinitionLPStrict,  "percentPerLenghtUnit") ;
}
/**
* useMultiInstanceChecks See UseMultiInstanceChecks enum..
* mandatory: false| reinit necessary: true
* default value: twoChecks
* @return String 
**/
public String getUseMultiInstanceChecks() {
return (String) ComponentManager.getInstance().getConfigOptionValue(posNegDefinitionLPStrict,  "useMultiInstanceChecks") ;
}
/**
* penaliseNeutralExamples if set to true neutral examples are penalised.
* mandatory: false| reinit necessary: true
* default value: null
* @return boolean 
**/
public boolean getPenaliseNeutralExamples() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(posNegDefinitionLPStrict,  "penaliseNeutralExamples") ;
}
/**
* accuracyPenalty penalty for pos/neg examples which are classified as neutral.
* mandatory: false| reinit necessary: true
* default value: 1.0
* @return double 
**/
public double getAccuracyPenalty() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegDefinitionLPStrict,  "accuracyPenalty") ;
}
/**
* errorPenalty penalty for pos. examples classified as negative or vice versa.
* mandatory: false| reinit necessary: true
* default value: 3.0
* @return double 
**/
public double getErrorPenalty() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegDefinitionLPStrict,  "errorPenalty") ;
}

/**
* @param positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setPositiveExamples(Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegDefinitionLPStrict, "positiveExamples", positiveExamples);
}
/**
* @param negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setNegativeExamples(Set<String> negativeExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegDefinitionLPStrict, "negativeExamples", negativeExamples);
}
/**
* @param useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseRetrievalForClassficiation(boolean useRetrievalForClassficiation) {
ComponentManager.getInstance().applyConfigEntry(posNegDefinitionLPStrict, "useRetrievalForClassficiation", useRetrievalForClassficiation);
reinitNecessary = true;
}
/**
* @param percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
**/
public void setPercentPerLenghtUnit(double percentPerLenghtUnit) {
ComponentManager.getInstance().applyConfigEntry(posNegDefinitionLPStrict, "percentPerLenghtUnit", percentPerLenghtUnit);
reinitNecessary = true;
}
/**
* @param useMultiInstanceChecks See UseMultiInstanceChecks enum..
* mandatory: false| reinit necessary: true
* default value: twoChecks
**/
public void setUseMultiInstanceChecks(String useMultiInstanceChecks) {
ComponentManager.getInstance().applyConfigEntry(posNegDefinitionLPStrict, "useMultiInstanceChecks", useMultiInstanceChecks);
reinitNecessary = true;
}
/**
* @param penaliseNeutralExamples if set to true neutral examples are penalised.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setPenaliseNeutralExamples(boolean penaliseNeutralExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegDefinitionLPStrict, "penaliseNeutralExamples", penaliseNeutralExamples);
reinitNecessary = true;
}
/**
* @param accuracyPenalty penalty for pos/neg examples which are classified as neutral.
* mandatory: false| reinit necessary: true
* default value: 1.0
**/
public void setAccuracyPenalty(double accuracyPenalty) {
ComponentManager.getInstance().applyConfigEntry(posNegDefinitionLPStrict, "accuracyPenalty", accuracyPenalty);
reinitNecessary = true;
}
/**
* @param errorPenalty penalty for pos. examples classified as negative or vice versa.
* mandatory: false| reinit necessary: true
* default value: 3.0
**/
public void setErrorPenalty(double errorPenalty) {
ComponentManager.getInstance().applyConfigEntry(posNegDefinitionLPStrict, "errorPenalty", errorPenalty);
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
