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

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.reasoning.DIGReasoner;

/**
* automatically generated, do not edit manually
**/
public class DIGReasonerConfigurator  {

private boolean reinitNecessary = false;
private DIGReasoner DIGReasoner;

public DIGReasonerConfigurator (DIGReasoner DIGReasoner){
this.DIGReasoner = DIGReasoner;
}

/**
**/
public static DIGReasoner getDIGReasoner (KnowledgeSource knowledgeSource ) {
DIGReasoner component = ComponentManager.getInstance().reasoner(DIGReasoner.class, knowledgeSource );
return component;
}

/**
* option name: reasonerUrl
* URL of the DIG reasoner
* default value: null
**/
public String getReasonerUrl ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(DIGReasoner,  "reasonerUrl") ;
}
/**
* option name: writeDIGProtocol
* specifies whether or not to write a protocoll of send and received DIG requests
* default value: false
**/
public boolean getWriteDIGProtocol ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(DIGReasoner,  "writeDIGProtocol") ;
}
/**
* option name: digProtocolFile
* the file to store the DIG protocol
* default value: log/digProtocol.txt
**/
public String getDigProtocolFile ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(DIGReasoner,  "digProtocolFile") ;
}

/**
* option name: reasonerUrl
* URL of the DIG reasoner
* default value: null
**/
public void setReasonerUrl ( String reasonerUrl) {
ComponentManager.getInstance().applyConfigEntry(DIGReasoner, "reasonerUrl", reasonerUrl);
reinitNecessary = true;
}
/**
* option name: writeDIGProtocol
* specifies whether or not to write a protocoll of send and received DIG requests
* default value: false
**/
public void setWriteDIGProtocol ( boolean writeDIGProtocol) {
ComponentManager.getInstance().applyConfigEntry(DIGReasoner, "writeDIGProtocol", writeDIGProtocol);
reinitNecessary = true;
}
/**
* option name: digProtocolFile
* the file to store the DIG protocol
* default value: log/digProtocol.txt
**/
public void setDigProtocolFile ( String digProtocolFile) {
ComponentManager.getInstance().applyConfigEntry(DIGReasoner, "digProtocolFile", digProtocolFile);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
