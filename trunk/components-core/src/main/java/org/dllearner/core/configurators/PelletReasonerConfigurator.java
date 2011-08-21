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
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.reasoning.PelletReasoner;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class PelletReasonerConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private PelletReasoner pelletReasoner;

/**
* @param pelletReasoner see PelletReasoner
**/
public PelletReasonerConfigurator(PelletReasoner pelletReasoner){
this.pelletReasoner = pelletReasoner;
}

/**
* @param knowledgeSource see knowledgeSource
* @return PelletReasoner
**/
public static PelletReasoner getPelletReasoner(Set<AbstractKnowledgeSource> knowledgeSource) {
PelletReasoner component = ComponentManager.getInstance().reasoner(PelletReasoner.class, knowledgeSource);
return component;
}

/**
* defaultNegation Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getDefaultNegation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(pelletReasoner,  "defaultNegation") ;
}

/**
* @param defaultNegation Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setDefaultNegation(boolean defaultNegation) {
ComponentManager.getInstance().applyConfigEntry(pelletReasoner, "defaultNegation", defaultNegation);
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
