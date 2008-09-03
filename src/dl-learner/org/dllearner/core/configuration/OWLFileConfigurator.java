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
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.configuration.Configurator;
import org.dllearner.kb.OWLFile;


/**
* automatically generated, do not edit manually
**/
public class OWLFileConfigurator extends Configurator {

OWLFile OWLFile;
private String url = null;


/**
URL pointing to the OWL file
**/
public void setMandatoryOptions (String url ) {
this.url = url;
}
/**
URL pointing to the OWL file
**/
public static OWLFile getOWLFile (ComponentManager cm, String url ) {
OWLFile component = cm.knowledgeSource(OWLFile.class);
cm.applyConfigEntry(component, "url", url);
return component;
}


@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if (optionName.equals(url)){
url = (String)  entry.getValue();
}
}


/**
* URL pointing to the OWL file
**/
public void setUrl (String url) {
this.url = url;
}



/**
* URL pointing to the OWL file
* 
**/
public String getUrl ( ) {
return this.url;
}



}
