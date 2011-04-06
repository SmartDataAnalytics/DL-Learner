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
import org.dllearner.learningproblems.PosNegLPStrict;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class PosNegLPStrictConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private PosNegLPStrict posNegLPStrict;

/**
* @param posNegLPStrict see PosNegLPStrict
**/
public PosNegLPStrictConfigurator(PosNegLPStrict posNegLPStrict){
this.posNegLPStrict = posNegLPStrict;
}

/**
* @param reasoningService see reasoningService
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @return PosNegLPStrict
**/
public static PosNegLPStrict getPosNegLPStrict(ReasonerComponent reasoningService, Set<String> positiveExamples, Set<String> negativeExamples) {
PosNegLPStrict component = ComponentManager.getInstance().learningProblem(PosNegLPStrict.class, reasoningService);
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
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posNegLPStrict,  "positiveExamples") ;
}
/**
* negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getNegativeExamples() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posNegLPStrict,  "negativeExamples") ;
}
/**
* useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept. - NO LONGER FULLY SUPPORTED..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseRetrievalForClassficiation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(posNegLPStrict,  "useRetrievalForClassficiation") ;
}
/**
* percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
* @return double 
**/
public double getPercentPerLenghtUnit() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegLPStrict,  "percentPerLenghtUnit") ;
}
/**
* useMultiInstanceChecks See UseMultiInstanceChecks enum. - NO LONGER FULLY SUPPORTED..
* mandatory: false| reinit necessary: true
* default value: twoChecks
* @return String 
**/
public String getUseMultiInstanceChecks() {
return (String) ComponentManager.getInstance().getConfigOptionValue(posNegLPStrict,  "useMultiInstanceChecks") ;
}
/**
* penaliseNeutralExamples if set to true neutral examples are penalised.
* mandatory: false| reinit necessary: true
* default value: null
* @return boolean 
**/
public boolean getPenaliseNeutralExamples() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(posNegLPStrict,  "penaliseNeutralExamples") ;
}
/**
* accuracyPenalty penalty for pos/neg examples which are classified as neutral.
* mandatory: false| reinit necessary: true
* default value: 1.0
* @return double 
**/
public double getAccuracyPenalty() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegLPStrict,  "accuracyPenalty") ;
}
/**
* errorPenalty penalty for pos. examples classified as negative or vice versa.
* mandatory: false| reinit necessary: true
* default value: 3.0
* @return double 
**/
public double getErrorPenalty() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegLPStrict,  "errorPenalty") ;
}

/**
* @param positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setPositiveExamples(Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStrict, "positiveExamples", positiveExamples);
}
/**
* @param negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setNegativeExamples(Set<String> negativeExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStrict, "negativeExamples", negativeExamples);
}
/**
* @param useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept. - NO LONGER FULLY SUPPORTED..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseRetrievalForClassficiation(boolean useRetrievalForClassficiation) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStrict, "useRetrievalForClassficiation", useRetrievalForClassficiation);
reinitNecessary = true;
}
/**
* @param percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
**/
public void setPercentPerLenghtUnit(double percentPerLenghtUnit) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStrict, "percentPerLenghtUnit", percentPerLenghtUnit);
reinitNecessary = true;
}
/**
* @param useMultiInstanceChecks See UseMultiInstanceChecks enum. - NO LONGER FULLY SUPPORTED..
* mandatory: false| reinit necessary: true
* default value: twoChecks
**/
public void setUseMultiInstanceChecks(String useMultiInstanceChecks) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStrict, "useMultiInstanceChecks", useMultiInstanceChecks);
reinitNecessary = true;
}
/**
* @param penaliseNeutralExamples if set to true neutral examples are penalised.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setPenaliseNeutralExamples(boolean penaliseNeutralExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStrict, "penaliseNeutralExamples", penaliseNeutralExamples);
reinitNecessary = true;
}
/**
* @param accuracyPenalty penalty for pos/neg examples which are classified as neutral.
* mandatory: false| reinit necessary: true
* default value: 1.0
**/
public void setAccuracyPenalty(double accuracyPenalty) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStrict, "accuracyPenalty", accuracyPenalty);
reinitNecessary = true;
}
/**
* @param errorPenalty penalty for pos. examples classified as negative or vice versa.
* mandatory: false| reinit necessary: true
* default value: 3.0
**/
public void setErrorPenalty(double errorPenalty) {
ComponentManager.getInstance().applyConfigEntry(posNegLPStrict, "errorPenalty", errorPenalty);
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
