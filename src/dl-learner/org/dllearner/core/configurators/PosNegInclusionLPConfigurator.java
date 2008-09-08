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
 **/

package org.dllearner.core.configurators;

import java.util.Set;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasoningService;
import org.dllearner.learningproblems.PosNegInclusionLP;

/**
* automatically generated, do not edit manually
**/
public class PosNegInclusionLPConfigurator  {

private boolean reinitNecessary = false;
private PosNegInclusionLP PosNegInclusionLP;

public PosNegInclusionLPConfigurator (PosNegInclusionLP PosNegInclusionLP){
this.PosNegInclusionLP = PosNegInclusionLP;
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
**/
public static PosNegInclusionLP getPosNegInclusionLP (ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples ) {
PosNegInclusionLP component = ComponentManager.getInstance().learningProblem(PosNegInclusionLP.class, reasoningService );
ComponentManager.getInstance().applyConfigEntry(component, "positiveExamples", positiveExamples);
ComponentManager.getInstance().applyConfigEntry(component, "negativeExamples", negativeExamples);
return component;
}

/**
* option name: positiveExamples
* positive examples
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getPositiveExamples ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(PosNegInclusionLP,  "positiveExamples") ;
}
/**
* option name: negativeExamples
* negative examples
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getNegativeExamples ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(PosNegInclusionLP,  "negativeExamples") ;
}
/**
* option name: useRetrievalForClassficiation
* Specifies whether to use retrieval or instance checks for testing a concept.
* default value: false
**/
public boolean getUseRetrievalForClassficiation ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(PosNegInclusionLP,  "useRetrievalForClassficiation") ;
}
/**
* option name: percentPerLenghtUnit
* describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one
* default value: 0.05
**/
public double getPercentPerLenghtUnit ( ) {
return (Double) ComponentManager.getInstance().getConfigOptionValue(PosNegInclusionLP,  "percentPerLenghtUnit") ;
}
/**
* option name: useMultiInstanceChecks
* See UseMultiInstanceChecks enum.
* default value: twoChecks
**/
public String getUseMultiInstanceChecks ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(PosNegInclusionLP,  "useMultiInstanceChecks") ;
}

/**
* option name: positiveExamples
* positive examples
* default value: null
**/
public void setPositiveExamples ( Set<String> positiveExamples) {
ComponentManager.getInstance().applyConfigEntry(PosNegInclusionLP, "positiveExamples", positiveExamples);
}
/**
* option name: negativeExamples
* negative examples
* default value: null
**/
public void setNegativeExamples ( Set<String> negativeExamples) {
ComponentManager.getInstance().applyConfigEntry(PosNegInclusionLP, "negativeExamples", negativeExamples);
}
/**
* option name: useRetrievalForClassficiation
* Specifies whether to use retrieval or instance checks for testing a concept.
* default value: false
**/
public void setUseRetrievalForClassficiation ( boolean useRetrievalForClassficiation) {
ComponentManager.getInstance().applyConfigEntry(PosNegInclusionLP, "useRetrievalForClassficiation", useRetrievalForClassficiation);
reinitNecessary = true;
}
/**
* option name: percentPerLenghtUnit
* describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one
* default value: 0.05
**/
public void setPercentPerLenghtUnit ( double percentPerLenghtUnit) {
ComponentManager.getInstance().applyConfigEntry(PosNegInclusionLP, "percentPerLenghtUnit", percentPerLenghtUnit);
reinitNecessary = true;
}
/**
* option name: useMultiInstanceChecks
* See UseMultiInstanceChecks enum.
* default value: twoChecks
**/
public void setUseMultiInstanceChecks ( String useMultiInstanceChecks) {
ComponentManager.getInstance().applyConfigEntry(PosNegInclusionLP, "useMultiInstanceChecks", useMultiInstanceChecks);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
