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
import org.dllearner.reasoning.DIGReasoner;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class DIGReasonerConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private DIGReasoner dIGReasoner;

/**
* @param dIGReasoner see DIGReasoner
**/
public DIGReasonerConfigurator(DIGReasoner dIGReasoner){
this.dIGReasoner = dIGReasoner;
}

/**
* @param knowledgeSource see knowledgeSource
* @return DIGReasoner
**/
public static DIGReasoner getDIGReasoner(Set<AbstractKnowledgeSource> knowledgeSource) {
DIGReasoner component = ComponentManager.getInstance().reasoner(DIGReasoner.class, knowledgeSource);
return component;
}

/**
* reasonerUrl URL of the DIG reasoner.
* mandatory: false| reinit necessary: true
* default value: null
* @return String 
**/
public String getReasonerUrl() {
return (String) ComponentManager.getInstance().getConfigOptionValue(dIGReasoner,  "reasonerUrl") ;
}
/**
* writeDIGProtocol specifies whether or not to write a protocoll of send and received DIG requests.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getWriteDIGProtocol() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(dIGReasoner,  "writeDIGProtocol") ;
}
/**
* digProtocolFile the file to store the DIG protocol.
* mandatory: false| reinit necessary: true
* default value: log/digProtocol.txt
* @return String 
**/
public String getDigProtocolFile() {
return (String) ComponentManager.getInstance().getConfigOptionValue(dIGReasoner,  "digProtocolFile") ;
}

/**
* @param reasonerUrl URL of the DIG reasoner.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setReasonerUrl(String reasonerUrl) {
ComponentManager.getInstance().applyConfigEntry(dIGReasoner, "reasonerUrl", reasonerUrl);
reinitNecessary = true;
}
/**
* @param writeDIGProtocol specifies whether or not to write a protocoll of send and received DIG requests.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setWriteDIGProtocol(boolean writeDIGProtocol) {
ComponentManager.getInstance().applyConfigEntry(dIGReasoner, "writeDIGProtocol", writeDIGProtocol);
reinitNecessary = true;
}
/**
* @param digProtocolFile the file to store the DIG protocol.
* mandatory: false| reinit necessary: true
* default value: log/digProtocol.txt
**/
public void setDigProtocolFile(String digProtocolFile) {
ComponentManager.getInstance().applyConfigEntry(dIGReasoner, "digProtocolFile", digProtocolFile);
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
