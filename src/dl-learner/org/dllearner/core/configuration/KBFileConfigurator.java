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
import org.dllearner.kb.KBFile;

/**
* automatically generated, do not edit manually
**/
@SuppressWarnings("unused")
public class KBFileConfigurator extends Configurator {

private boolean reinitNecessary = false;
private KBFile KBFile;
private String filename = null;
private String url = null;

public KBFileConfigurator (KBFile KBFile){
this.KBFile = KBFile;
}

/**
* @param filename pointer to the KB file on local file system
**/
public static KBFile getKBFile (ComponentManager cm, String filename ) {
KBFile component = cm.knowledgeSource(KBFile.class );
cm.applyConfigEntry(component, "filename", filename);
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("filename")){
filename = (String)  entry.getValue();
}else if (optionName.equals("url")){
url = (String)  entry.getValue();
}
}

/**
* option name: filename
* pointer to the KB file on local file system
* default value: null
**/
public String getFilename ( ) {
return this.filename;
}
/**
* option name: url
* URL pointer to the KB file
* default value: null
**/
public String getUrl ( ) {
return this.url;
}

/**
* option name: filename
* pointer to the KB file on local file system
* default value: null
**/
public void setFilename ( ComponentManager cm, String filename) {
cm.applyConfigEntry(KBFile, "filename", filename);
reinitNecessary = true;
}
/**
* option name: url
* URL pointer to the KB file
* default value: null
**/
public void setUrl ( ComponentManager cm, String url) {
cm.applyConfigEntry(KBFile, "url", url);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
