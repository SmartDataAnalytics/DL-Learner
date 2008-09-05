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

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.configuration.Configurator;
import org.dllearner.reasoning.DIGReasoner;

/**
* automatically generated, do not edit manually
**/
@SuppressWarnings("unused")
public class DIGReasonerConfigurator extends Configurator {

private boolean reinitNecessary = false;
private DIGReasoner DIGReasoner;
private String reasonerUrl = null;
private boolean writeDIGProtocol = false;
private String digProtocolFile = "log/digProtocol.txt";

public DIGReasonerConfigurator (DIGReasoner DIGReasoner){
this.DIGReasoner = DIGReasoner;
}

/**
**/
public static DIGReasoner getDIGReasoner (ComponentManager cm, KnowledgeSource knowledgeSource ) {
DIGReasoner component = cm.reasoner(DIGReasoner.class, knowledgeSource );
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("reasonerUrl")){
reasonerUrl = (String)  entry.getValue();
}else if (optionName.equals("writeDIGProtocol")){
writeDIGProtocol = (Boolean)  entry.getValue();
}else if (optionName.equals("digProtocolFile")){
digProtocolFile = (String)  entry.getValue();
}
}

/**
* option name: reasonerUrl
* URL of the DIG reasoner
* default value: null
**/
public String getReasonerUrl ( ) {
return this.reasonerUrl;
}
/**
* option name: writeDIGProtocol
* specifies whether or not to write a protocoll of send and received DIG requests
* default value: false
**/
public boolean getWriteDIGProtocol ( ) {
return this.writeDIGProtocol;
}
/**
* option name: digProtocolFile
* the file to store the DIG protocol
* default value: log/digProtocol.txt
**/
public String getDigProtocolFile ( ) {
return this.digProtocolFile;
}

/**
* option name: reasonerUrl
* URL of the DIG reasoner
* default value: null
**/
public void setReasonerUrl ( ComponentManager cm, String reasonerUrl) {
cm.applyConfigEntry(DIGReasoner, "reasonerUrl", reasonerUrl);
}
/**
* option name: writeDIGProtocol
* specifies whether or not to write a protocoll of send and received DIG requests
* default value: false
**/
public void setWriteDIGProtocol ( ComponentManager cm, boolean writeDIGProtocol) {
cm.applyConfigEntry(DIGReasoner, "writeDIGProtocol", writeDIGProtocol);
}
/**
* option name: digProtocolFile
* the file to store the DIG protocol
* default value: log/digProtocol.txt
**/
public void setDigProtocolFile ( ComponentManager cm, String digProtocolFile) {
cm.applyConfigEntry(DIGReasoner, "digProtocolFile", digProtocolFile);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
