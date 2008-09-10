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
import org.dllearner.learningproblems.PosNegInclusionLP;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class PosNegInclusionLPConfigurator implements Configurator {

private boolean reinitNecessary = false;
@SuppressWarnings("unused")

private PosNegInclusionLP posNegInclusionLP;

/**
* @param posNegInclusionLP see PosNegInclusionLP
**/
public PosNegInclusionLPConfigurator(PosNegInclusionLP posNegInclusionLP){
this.posNegInclusionLP = posNegInclusionLP;
}

/**
* @param reasoningService see reasoningService
* @param positiveExamples positive examples
* @param negativeExamples negative examples
* @return PosNegInclusionLP
**/
public static PosNegInclusionLP getPosNegInclusionLP(ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples) {
PosNegInclusionLP component = ComponentManager.getInstance().learningProblem(PosNegInclusionLP.class, reasoningService);
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
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posNegInclusionLP,  "positiveExamples") ;
}
/**
* negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getNegativeExamples() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(posNegInclusionLP,  "negativeExamples") ;
}
/**
* useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseRetrievalForClassficiation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(posNegInclusionLP,  "useRetrievalForClassficiation") ;
}
/**
* percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
* @return double 
**/
public double getPercentPerLenghtUnit() {
return (Double) ComponentManager.getInstance().getConfigOptionValue(posNegInclusionLP,  "percentPerLenghtUnit") ;
}
/**
* useMultiInstanceChecks See UseMultiInstanceChecks enum..
* mandatory: false| reinit necessary: true
* default value: twoChecks
* @return String 
**/
public String getUseMultiInstanceChecks() {
return (String) ComponentManager.getInstance().getConfigOptionValue(posNegInclusionLP,  "useMultiInstanceChecks") ;
}

/**
* @param positiveExamples positive examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setPositiveExamples(Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegInclusionLP, "positiveExamples", positiveExamples);
}
/**
* @param negativeExamples negative examples.
* mandatory: true| reinit necessary: false
* default value: null
**/
public void setNegativeExamples(Set<String> negativeExamples) {
ComponentManager.getInstance().applyConfigEntry(posNegInclusionLP, "negativeExamples", negativeExamples);
}
/**
* @param useRetrievalForClassficiation Specifies whether to use retrieval or instance checks for testing a concept..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseRetrievalForClassficiation(boolean useRetrievalForClassficiation) {
ComponentManager.getInstance().applyConfigEntry(posNegInclusionLP, "useRetrievalForClassficiation", useRetrievalForClassficiation);
reinitNecessary = true;
}
/**
* @param percentPerLenghtUnit describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one.
* mandatory: false| reinit necessary: true
* default value: 0.05
**/
public void setPercentPerLenghtUnit(double percentPerLenghtUnit) {
ComponentManager.getInstance().applyConfigEntry(posNegInclusionLP, "percentPerLenghtUnit", percentPerLenghtUnit);
reinitNecessary = true;
}
/**
* @param useMultiInstanceChecks See UseMultiInstanceChecks enum..
* mandatory: false| reinit necessary: true
* default value: twoChecks
**/
public void setUseMultiInstanceChecks(String useMultiInstanceChecks) {
ComponentManager.getInstance().applyConfigEntry(posNegInclusionLP, "useMultiInstanceChecks", useMultiInstanceChecks);
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
