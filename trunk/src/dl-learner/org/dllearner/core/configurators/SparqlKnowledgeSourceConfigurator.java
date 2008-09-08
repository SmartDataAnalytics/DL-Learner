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

import java.util.List;
import java.util.Set;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.datastructures.StringTuple;

/**
* automatically generated, do not edit manually
**/
public class SparqlKnowledgeSourceConfigurator  {

private boolean reinitNecessary = false;
private SparqlKnowledgeSource SparqlKnowledgeSource;

public SparqlKnowledgeSourceConfigurator (SparqlKnowledgeSource SparqlKnowledgeSource){
this.SparqlKnowledgeSource = SparqlKnowledgeSource;
}

/**
* @param instances relevant instances e.g. positive and negative examples in a learning problem
**/
public static SparqlKnowledgeSource getSparqlKnowledgeSource (Set<String> instances ) {
SparqlKnowledgeSource component = ComponentManager.getInstance().knowledgeSource(SparqlKnowledgeSource.class );
ComponentManager.getInstance().applyConfigEntry(component, "instances", instances);
return component;
}

/**
* option name: url
* URL of SPARQL Endpoint
* default value: null
**/
public String getUrl ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "url") ;
}
/**
* option name: cacheDir
* dir of cache
* default value: null
**/
public String getCacheDir ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "cacheDir") ;
}
/**
* option name: instances
* relevant instances e.g. positive and negative examples in a learning problem
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getInstances ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "instances") ;
}
/**
* option name: recursionDepth
* recursion depth of KB fragment selection
* default value: 1
**/
public int getRecursionDepth ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "recursionDepth") ;
}
/**
* option name: predefinedFilter
* the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST
* default value: null
**/
public String getPredefinedFilter ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "predefinedFilter") ;
}
/**
* option name: predefinedEndpoint
* the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK
* default value: null
**/
public String getPredefinedEndpoint ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "predefinedEndpoint") ;
}
/**
* option name: predefinedManipulator
* the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR
* default value: null
**/
public String getPredefinedManipulator ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "predefinedManipulator") ;
}
/**
* option name: predList
* list of all ignored roles
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getPredList ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "predList") ;
}
/**
* option name: objList
* list of all ignored objects
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getObjList ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "objList") ;
}
/**
* option name: classList
* list of all ignored classes
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getClassList ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "classList") ;
}
/**
* option name: format
* N-TRIPLES or KB format
* default value: N-TRIPLES
**/
public String getFormat ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "format") ;
}
/**
* option name: dumpToFile
* Specifies whether the extracted ontology is written to a file or not.
* default value: true
**/
public boolean getDumpToFile ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "dumpToFile") ;
}
/**
* option name: convertNT2RDF
* Specifies whether the extracted NTriples are converted to RDF and deleted.
* default value: true
**/
public boolean getConvertNT2RDF ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "convertNT2RDF") ;
}
/**
* option name: useLits
* use Literals in SPARQL query
* default value: true
**/
public boolean getUseLits ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "useLits") ;
}
/**
* option name: getAllSuperClasses
* If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.
* default value: true
**/
public boolean getGetAllSuperClasses ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "getAllSuperClasses") ;
}
/**
* option name: useCache
* If true a Cache is used
* default value: true
**/
public boolean getUseCache ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "useCache") ;
}
/**
* option name: replacePredicate
* rule for replacing predicates
* default value: null
**/
public List<StringTuple> getReplacePredicate ( ) {
return (List<StringTuple>) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "replacePredicate") ;
}
/**
* option name: replaceObject
* rule for replacing predicates
* default value: null
**/
public List<StringTuple> getReplaceObject ( ) {
return (List<StringTuple>) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "replaceObject") ;
}
/**
* option name: breakSuperClassRetrievalAfter
* stops a cyclic hierarchy after specified number of classes
* default value: 1000
**/
public int getBreakSuperClassRetrievalAfter ( ) {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "breakSuperClassRetrievalAfter") ;
}
/**
* option name: closeAfterRecursion
* gets all classes for all instances
* default value: true
**/
public boolean getCloseAfterRecursion ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "closeAfterRecursion") ;
}
/**
* option name: getPropertyInformation
* gets all types for extracted ObjectProperties
* default value: false
**/
public boolean getGetPropertyInformation ( ) {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "getPropertyInformation") ;
}
/**
* option name: verbosity
* control verbosity of output for this component
* default value: warning
**/
public String getVerbosity ( ) {
return (String) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "verbosity") ;
}
/**
* option name: defaultGraphURIs
* a list of all default Graph URIs
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getDefaultGraphURIs ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "defaultGraphURIs") ;
}
/**
* option name: namedGraphURIs
* a list of all named Graph URIs
* default value: null
**/
@SuppressWarnings("unchecked")
public Set<String> getNamedGraphURIs ( ) {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(SparqlKnowledgeSource,  "namedGraphURIs") ;
}

/**
* option name: url
* URL of SPARQL Endpoint
* default value: null
**/
public void setUrl ( String url) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "url", url);
reinitNecessary = true;
}
/**
* option name: cacheDir
* dir of cache
* default value: null
**/
public void setCacheDir ( String cacheDir) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "cacheDir", cacheDir);
reinitNecessary = true;
}
/**
* option name: instances
* relevant instances e.g. positive and negative examples in a learning problem
* default value: null
**/
public void setInstances ( Set<String> instances) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "instances", instances);
}
/**
* option name: recursionDepth
* recursion depth of KB fragment selection
* default value: 1
**/
public void setRecursionDepth ( int recursionDepth) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "recursionDepth", recursionDepth);
reinitNecessary = true;
}
/**
* option name: predefinedFilter
* the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST
* default value: null
**/
public void setPredefinedFilter ( String predefinedFilter) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "predefinedFilter", predefinedFilter);
reinitNecessary = true;
}
/**
* option name: predefinedEndpoint
* the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK
* default value: null
**/
public void setPredefinedEndpoint ( String predefinedEndpoint) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "predefinedEndpoint", predefinedEndpoint);
reinitNecessary = true;
}
/**
* option name: predefinedManipulator
* the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR
* default value: null
**/
public void setPredefinedManipulator ( String predefinedManipulator) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "predefinedManipulator", predefinedManipulator);
reinitNecessary = true;
}
/**
* option name: predList
* list of all ignored roles
* default value: null
**/
public void setPredList ( Set<String> predList) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "predList", predList);
reinitNecessary = true;
}
/**
* option name: objList
* list of all ignored objects
* default value: null
**/
public void setObjList ( Set<String> objList) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "objList", objList);
reinitNecessary = true;
}
/**
* option name: classList
* list of all ignored classes
* default value: null
**/
public void setClassList ( Set<String> classList) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "classList", classList);
reinitNecessary = true;
}
/**
* option name: format
* N-TRIPLES or KB format
* default value: N-TRIPLES
**/
public void setFormat ( String format) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "format", format);
reinitNecessary = true;
}
/**
* option name: dumpToFile
* Specifies whether the extracted ontology is written to a file or not.
* default value: true
**/
public void setDumpToFile ( boolean dumpToFile) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "dumpToFile", dumpToFile);
reinitNecessary = true;
}
/**
* option name: convertNT2RDF
* Specifies whether the extracted NTriples are converted to RDF and deleted.
* default value: true
**/
public void setConvertNT2RDF ( boolean convertNT2RDF) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "convertNT2RDF", convertNT2RDF);
reinitNecessary = true;
}
/**
* option name: useLits
* use Literals in SPARQL query
* default value: true
**/
public void setUseLits ( boolean useLits) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "useLits", useLits);
reinitNecessary = true;
}
/**
* option name: getAllSuperClasses
* If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.
* default value: true
**/
public void setGetAllSuperClasses ( boolean getAllSuperClasses) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "getAllSuperClasses", getAllSuperClasses);
reinitNecessary = true;
}
/**
* option name: useCache
* If true a Cache is used
* default value: true
**/
public void setUseCache ( boolean useCache) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "useCache", useCache);
reinitNecessary = true;
}
/**
* option name: replacePredicate
* rule for replacing predicates
* default value: null
**/
public void setReplacePredicate ( List<StringTuple> replacePredicate) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "replacePredicate", replacePredicate);
reinitNecessary = true;
}
/**
* option name: replaceObject
* rule for replacing predicates
* default value: null
**/
public void setReplaceObject ( List<StringTuple> replaceObject) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "replaceObject", replaceObject);
reinitNecessary = true;
}
/**
* option name: breakSuperClassRetrievalAfter
* stops a cyclic hierarchy after specified number of classes
* default value: 1000
**/
public void setBreakSuperClassRetrievalAfter ( int breakSuperClassRetrievalAfter) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "breakSuperClassRetrievalAfter", breakSuperClassRetrievalAfter);
reinitNecessary = true;
}
/**
* option name: closeAfterRecursion
* gets all classes for all instances
* default value: true
**/
public void setCloseAfterRecursion ( boolean closeAfterRecursion) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "closeAfterRecursion", closeAfterRecursion);
reinitNecessary = true;
}
/**
* option name: getPropertyInformation
* gets all types for extracted ObjectProperties
* default value: false
**/
public void setGetPropertyInformation ( boolean getPropertyInformation) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "getPropertyInformation", getPropertyInformation);
reinitNecessary = true;
}
/**
* option name: verbosity
* control verbosity of output for this component
* default value: warning
**/
public void setVerbosity ( String verbosity) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "verbosity", verbosity);
reinitNecessary = true;
}
/**
* option name: defaultGraphURIs
* a list of all default Graph URIs
* default value: null
**/
public void setDefaultGraphURIs ( Set<String> defaultGraphURIs) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "defaultGraphURIs", defaultGraphURIs);
reinitNecessary = true;
}
/**
* option name: namedGraphURIs
* a list of all named Graph URIs
* default value: null
**/
public void setNamedGraphURIs ( Set<String> namedGraphURIs) {
ComponentManager.getInstance().applyConfigEntry(SparqlKnowledgeSource, "namedGraphURIs", namedGraphURIs);
reinitNecessary = true;
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
