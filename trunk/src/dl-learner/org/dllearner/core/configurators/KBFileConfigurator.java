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
import org.dllearner.kb.KBFile;

/**
* automatically generated, do not edit manually
**/
public class KBFileConfigurator  {

private boolean reinitNecessary = false;
private KBFile KBFile;

public KBFileConfigurator (KBFile KBFile){
this.KBFile = KBFile;
}

/**
* @param filename pointer to the KB file on local file system
**/
public static KBFile getKBFile (String filename ) {
KBFile component = ComponentManager.getInstance().knowledgeSource(KBFile.class );
ComponentManager.getInstance().applyConfigEntry(component, "filename", filename);
return component;
}

/**
* option name: filename
* pointer to the KB file on local file system
* default value: null
**/
public String getFilename ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(KBFile,  "filename") ;
}
/**
* option name: url
* URL pointer to the KB file
* default value: null
**/
public String getUrl ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(KBFile,  "url") ;
}

/**
* option name: filename
* pointer to the KB file on local file system
* default value: null
**/
public void setFilename ( String filename) {
ComponentManager.getInstance().applyConfigEntry(KBFile, "filename", filename);
reinitNecessary = true;
}
/**
* option name: url
* URL pointer to the KB file
* default value: null
**/
public void setUrl ( String url) {
ComponentManager.getInstance().applyConfigEntry(KBFile, "url", url);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
