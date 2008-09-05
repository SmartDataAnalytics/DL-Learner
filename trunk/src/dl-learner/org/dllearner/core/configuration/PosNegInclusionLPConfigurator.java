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

package org.dllearner.core.configuration;

import java.util.Set;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.configuration.Configurator;
import org.dllearner.learningproblems.PosNegInclusionLP;

/**
* automatically generated, do not edit manually
**/
@SuppressWarnings("unused")
public class PosNegInclusionLPConfigurator extends Configurator {

private boolean reinitNecessary = false;
private PosNegInclusionLP PosNegInclusionLP;
private Set<String> positiveExamples = null;
private Set<String> negativeExamples = null;
private boolean useRetrievalForClassficiation = false;
private double percentPerLenghtUnit = 0.05;
private String useMultiInstanceChecks = "twoChecks";

public PosNegInclusionLPConfigurator (PosNegInclusionLP PosNegInclusionLP){
this.PosNegInclusionLP = PosNegInclusionLP;
}

/**
* @param positiveExamples positive examples
* @param negativeExamples negative examples
**/
public static PosNegInclusionLP getPosNegInclusionLP (ComponentManager cm, ReasoningService reasoningService, Set<String> positiveExamples, Set<String> negativeExamples ) {
PosNegInclusionLP component = cm.learningProblem(PosNegInclusionLP.class, reasoningService );
cm.applyConfigEntry(component, "positiveExamples", positiveExamples);
cm.applyConfigEntry(component, "negativeExamples", negativeExamples);
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("positiveExamples")){
positiveExamples = (Set<String>)  entry.getValue();
}else if (optionName.equals("negativeExamples")){
negativeExamples = (Set<String>)  entry.getValue();
}else if (optionName.equals("useRetrievalForClassficiation")){
useRetrievalForClassficiation = (Boolean)  entry.getValue();
}else if (optionName.equals("percentPerLenghtUnit")){
percentPerLenghtUnit = (Double)  entry.getValue();
}else if (optionName.equals("useMultiInstanceChecks")){
useMultiInstanceChecks = (String)  entry.getValue();
}
}

/**
* option name: positiveExamples
* positive examples
* default value: null
**/
public Set<String> getPositiveExamples ( ) {
return this.positiveExamples;
}
/**
* option name: negativeExamples
* negative examples
* default value: null
**/
public Set<String> getNegativeExamples ( ) {
return this.negativeExamples;
}
/**
* option name: useRetrievalForClassficiation
* Specifies whether to use retrieval or instance checks for testing a concept.
* default value: false
**/
public boolean getUseRetrievalForClassficiation ( ) {
return this.useRetrievalForClassficiation;
}
/**
* option name: percentPerLenghtUnit
* describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one
* default value: 0.05
**/
public double getPercentPerLenghtUnit ( ) {
return this.percentPerLenghtUnit;
}
/**
* option name: useMultiInstanceChecks
* See UseMultiInstanceChecks enum.
* default value: twoChecks
**/
public String getUseMultiInstanceChecks ( ) {
return this.useMultiInstanceChecks;
}

/**
* option name: positiveExamples
* positive examples
* default value: null
**/
public void setPositiveExamples ( ComponentManager cm, Set<String> positiveExamples) {
cm.applyConfigEntry(PosNegInclusionLP, "positiveExamples", positiveExamples);
}
/**
* option name: negativeExamples
* negative examples
* default value: null
**/
public void setNegativeExamples ( ComponentManager cm, Set<String> negativeExamples) {
cm.applyConfigEntry(PosNegInclusionLP, "negativeExamples", negativeExamples);
}
/**
* option name: useRetrievalForClassficiation
* Specifies whether to use retrieval or instance checks for testing a concept.
* default value: false
**/
public void setUseRetrievalForClassficiation ( ComponentManager cm, boolean useRetrievalForClassficiation) {
cm.applyConfigEntry(PosNegInclusionLP, "useRetrievalForClassficiation", useRetrievalForClassficiation);
}
/**
* option name: percentPerLenghtUnit
* describes the reduction in classification accuracy in percent one is willing to accept for reducing the length of the concept by one
* default value: 0.05
**/
public void setPercentPerLenghtUnit ( ComponentManager cm, double percentPerLenghtUnit) {
cm.applyConfigEntry(PosNegInclusionLP, "percentPerLenghtUnit", percentPerLenghtUnit);
}
/**
* option name: useMultiInstanceChecks
* See UseMultiInstanceChecks enum.
* default value: twoChecks
**/
public void setUseMultiInstanceChecks ( ComponentManager cm, String useMultiInstanceChecks) {
cm.applyConfigEntry(PosNegInclusionLP, "useMultiInstanceChecks", useMultiInstanceChecks);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
