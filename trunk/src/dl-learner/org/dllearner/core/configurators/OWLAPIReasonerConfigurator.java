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
import org.dllearner.core.KnowledgeSource;
import org.dllearner.reasoning.OWLAPIReasoner;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class OWLAPIReasonerConfigurator  implements Configurator {

private boolean reinitNecessary = false;
@SuppressWarnings("unused")

private OWLAPIReasoner oWLAPIReasoner;

/**
* @param oWLAPIReasoner see OWLAPIReasoner
**/
public OWLAPIReasonerConfigurator(OWLAPIReasoner oWLAPIReasoner){
this.oWLAPIReasoner = oWLAPIReasoner;
}

/**
* @param knowledgeSource see knowledgeSource
* @return OWLAPIReasoner
**/
public static OWLAPIReasoner getOWLAPIReasoner(Set<KnowledgeSource> knowledgeSource) {
OWLAPIReasoner component = ComponentManager.getInstance().reasoner(OWLAPIReasoner.class, knowledgeSource);
return component;
}

/**
* reasonerType FaCT++,, HermiT or Pellet, which means "pellet" or "fact".
* mandatory: false| reinit necessary: true
* default value: pellet
* @return String 
**/
public String getReasonerType() {
return (String) ComponentManager.getInstance().getConfigOptionValue(oWLAPIReasoner,  "reasonerType") ;
}

/**
* @param reasonerType FaCT++,, HermiT or Pellet, which means "pellet" or "fact".
* mandatory: false| reinit necessary: true
* default value: pellet
**/
public void setReasonerType(String reasonerType) {
ComponentManager.getInstance().applyConfigEntry(oWLAPIReasoner, "reasonerType", reasonerType);
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
