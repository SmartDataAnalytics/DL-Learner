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

import java.util.List;
import java.util.Set;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.configuration.Configurator;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.datastructures.StringTuple;

/**
* automatically generated, do not edit manually
**/
@SuppressWarnings("unused")
public class SparqlKnowledgeSourceConfigurator extends Configurator {

private boolean reinitNecessary = false;
private SparqlKnowledgeSource SparqlKnowledgeSource;
private String url = null;
private String cacheDir = null;
private Set<String> instances = null;
private int recursionDepth = 1;
private String predefinedFilter = null;
private String predefinedEndpoint = null;
private String predefinedManipulator = null;
private Set<String> predList = null;
private Set<String> objList = null;
private Set<String> classList = null;
private String format = "N-TRIPLES";
private boolean dumpToFile = true;
private boolean convertNT2RDF = true;
private boolean useLits = true;
private boolean getAllSuperClasses = true;
private boolean useCache = true;
private List<StringTuple> replacePredicate = null;
private List<StringTuple> replaceObject = null;
private int breakSuperClassRetrievalAfter = 1000;
private boolean closeAfterRecursion = true;
private boolean getPropertyInformation = false;
private String verbosity = "warning";
private Set<String> defaultGraphURIs = null;
private Set<String> namedGraphURIs = null;

public SparqlKnowledgeSourceConfigurator (SparqlKnowledgeSource SparqlKnowledgeSource){
this.SparqlKnowledgeSource = SparqlKnowledgeSource;
}

/**
* @param instances relevant instances e.g. positive and negative examples in a learning problem
**/
public static SparqlKnowledgeSource getSparqlKnowledgeSource (ComponentManager cm, Set<String> instances ) {
SparqlKnowledgeSource component = cm.knowledgeSource(SparqlKnowledgeSource.class );
cm.applyConfigEntry(component, "instances", instances);
return component;
}

@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if(false){//empty block 
}else if (optionName.equals("url")){
url = (String)  entry.getValue();
}else if (optionName.equals("cacheDir")){
cacheDir = (String)  entry.getValue();
}else if (optionName.equals("instances")){
instances = (Set<String>)  entry.getValue();
}else if (optionName.equals("recursionDepth")){
recursionDepth = (Integer)  entry.getValue();
}else if (optionName.equals("predefinedFilter")){
predefinedFilter = (String)  entry.getValue();
}else if (optionName.equals("predefinedEndpoint")){
predefinedEndpoint = (String)  entry.getValue();
}else if (optionName.equals("predefinedManipulator")){
predefinedManipulator = (String)  entry.getValue();
}else if (optionName.equals("predList")){
predList = (Set<String>)  entry.getValue();
}else if (optionName.equals("objList")){
objList = (Set<String>)  entry.getValue();
}else if (optionName.equals("classList")){
classList = (Set<String>)  entry.getValue();
}else if (optionName.equals("format")){
format = (String)  entry.getValue();
}else if (optionName.equals("dumpToFile")){
dumpToFile = (Boolean)  entry.getValue();
}else if (optionName.equals("convertNT2RDF")){
convertNT2RDF = (Boolean)  entry.getValue();
}else if (optionName.equals("useLits")){
useLits = (Boolean)  entry.getValue();
}else if (optionName.equals("getAllSuperClasses")){
getAllSuperClasses = (Boolean)  entry.getValue();
}else if (optionName.equals("useCache")){
useCache = (Boolean)  entry.getValue();
}else if (optionName.equals("replacePredicate")){
replacePredicate = (List<StringTuple>)  entry.getValue();
}else if (optionName.equals("replaceObject")){
replaceObject = (List<StringTuple>)  entry.getValue();
}else if (optionName.equals("breakSuperClassRetrievalAfter")){
breakSuperClassRetrievalAfter = (Integer)  entry.getValue();
}else if (optionName.equals("closeAfterRecursion")){
closeAfterRecursion = (Boolean)  entry.getValue();
}else if (optionName.equals("getPropertyInformation")){
getPropertyInformation = (Boolean)  entry.getValue();
}else if (optionName.equals("verbosity")){
verbosity = (String)  entry.getValue();
}else if (optionName.equals("defaultGraphURIs")){
defaultGraphURIs = (Set<String>)  entry.getValue();
}else if (optionName.equals("namedGraphURIs")){
namedGraphURIs = (Set<String>)  entry.getValue();
}
}

/**
* option name: url
* URL of SPARQL Endpoint
* default value: null
**/
public String getUrl ( ) {
return this.url;
}
/**
* option name: cacheDir
* dir of cache
* default value: null
**/
public String getCacheDir ( ) {
return this.cacheDir;
}
/**
* option name: instances
* relevant instances e.g. positive and negative examples in a learning problem
* default value: null
**/
public Set<String> getInstances ( ) {
return this.instances;
}
/**
* option name: recursionDepth
* recursion depth of KB fragment selection
* default value: 1
**/
public int getRecursionDepth ( ) {
return this.recursionDepth;
}
/**
* option name: predefinedFilter
* the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST
* default value: null
**/
public String getPredefinedFilter ( ) {
return this.predefinedFilter;
}
/**
* option name: predefinedEndpoint
* the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK
* default value: null
**/
public String getPredefinedEndpoint ( ) {
return this.predefinedEndpoint;
}
/**
* option name: predefinedManipulator
* the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR
* default value: null
**/
public String getPredefinedManipulator ( ) {
return this.predefinedManipulator;
}
/**
* option name: predList
* list of all ignored roles
* default value: null
**/
public Set<String> getPredList ( ) {
return this.predList;
}
/**
* option name: objList
* list of all ignored objects
* default value: null
**/
public Set<String> getObjList ( ) {
return this.objList;
}
/**
* option name: classList
* list of all ignored classes
* default value: null
**/
public Set<String> getClassList ( ) {
return this.classList;
}
/**
* option name: format
* N-TRIPLES or KB format
* default value: N-TRIPLES
**/
public String getFormat ( ) {
return this.format;
}
/**
* option name: dumpToFile
* Specifies whether the extracted ontology is written to a file or not.
* default value: true
**/
public boolean getDumpToFile ( ) {
return this.dumpToFile;
}
/**
* option name: convertNT2RDF
* Specifies whether the extracted NTriples are converted to RDF and deleted.
* default value: true
**/
public boolean getConvertNT2RDF ( ) {
return this.convertNT2RDF;
}
/**
* option name: useLits
* use Literals in SPARQL query
* default value: true
**/
public boolean getUseLits ( ) {
return this.useLits;
}
/**
* option name: getAllSuperClasses
* If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.
* default value: true
**/
public boolean getGetAllSuperClasses ( ) {
return this.getAllSuperClasses;
}
/**
* option name: useCache
* If true a Cache is used
* default value: true
**/
public boolean getUseCache ( ) {
return this.useCache;
}
/**
* option name: replacePredicate
* rule for replacing predicates
* default value: null
**/
public List<StringTuple> getReplacePredicate ( ) {
return this.replacePredicate;
}
/**
* option name: replaceObject
* rule for replacing predicates
* default value: null
**/
public List<StringTuple> getReplaceObject ( ) {
return this.replaceObject;
}
/**
* option name: breakSuperClassRetrievalAfter
* stops a cyclic hierarchy after specified number of classes
* default value: 1000
**/
public int getBreakSuperClassRetrievalAfter ( ) {
return this.breakSuperClassRetrievalAfter;
}
/**
* option name: closeAfterRecursion
* gets all classes for all instances
* default value: true
**/
public boolean getCloseAfterRecursion ( ) {
return this.closeAfterRecursion;
}
/**
* option name: getPropertyInformation
* gets all types for extracted ObjectProperties
* default value: false
**/
public boolean getGetPropertyInformation ( ) {
return this.getPropertyInformation;
}
/**
* option name: verbosity
* control verbosity of output for this component
* default value: warning
**/
public String getVerbosity ( ) {
return this.verbosity;
}
/**
* option name: defaultGraphURIs
* a list of all default Graph URIs
* default value: null
**/
public Set<String> getDefaultGraphURIs ( ) {
return this.defaultGraphURIs;
}
/**
* option name: namedGraphURIs
* a list of all named Graph URIs
* default value: null
**/
public Set<String> getNamedGraphURIs ( ) {
return this.namedGraphURIs;
}

/**
* option name: url
* URL of SPARQL Endpoint
* default value: null
**/
public void setUrl ( ComponentManager cm, String url) {
cm.applyConfigEntry(SparqlKnowledgeSource, "url", url);
}
/**
* option name: cacheDir
* dir of cache
* default value: null
**/
public void setCacheDir ( ComponentManager cm, String cacheDir) {
cm.applyConfigEntry(SparqlKnowledgeSource, "cacheDir", cacheDir);
}
/**
* option name: instances
* relevant instances e.g. positive and negative examples in a learning problem
* default value: null
**/
public void setInstances ( ComponentManager cm, Set<String> instances) {
cm.applyConfigEntry(SparqlKnowledgeSource, "instances", instances);
}
/**
* option name: recursionDepth
* recursion depth of KB fragment selection
* default value: 1
**/
public void setRecursionDepth ( ComponentManager cm, int recursionDepth) {
cm.applyConfigEntry(SparqlKnowledgeSource, "recursionDepth", recursionDepth);
}
/**
* option name: predefinedFilter
* the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST
* default value: null
**/
public void setPredefinedFilter ( ComponentManager cm, String predefinedFilter) {
cm.applyConfigEntry(SparqlKnowledgeSource, "predefinedFilter", predefinedFilter);
}
/**
* option name: predefinedEndpoint
* the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK
* default value: null
**/
public void setPredefinedEndpoint ( ComponentManager cm, String predefinedEndpoint) {
cm.applyConfigEntry(SparqlKnowledgeSource, "predefinedEndpoint", predefinedEndpoint);
}
/**
* option name: predefinedManipulator
* the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR
* default value: null
**/
public void setPredefinedManipulator ( ComponentManager cm, String predefinedManipulator) {
cm.applyConfigEntry(SparqlKnowledgeSource, "predefinedManipulator", predefinedManipulator);
}
/**
* option name: predList
* list of all ignored roles
* default value: null
**/
public void setPredList ( ComponentManager cm, Set<String> predList) {
cm.applyConfigEntry(SparqlKnowledgeSource, "predList", predList);
}
/**
* option name: objList
* list of all ignored objects
* default value: null
**/
public void setObjList ( ComponentManager cm, Set<String> objList) {
cm.applyConfigEntry(SparqlKnowledgeSource, "objList", objList);
}
/**
* option name: classList
* list of all ignored classes
* default value: null
**/
public void setClassList ( ComponentManager cm, Set<String> classList) {
cm.applyConfigEntry(SparqlKnowledgeSource, "classList", classList);
}
/**
* option name: format
* N-TRIPLES or KB format
* default value: N-TRIPLES
**/
public void setFormat ( ComponentManager cm, String format) {
cm.applyConfigEntry(SparqlKnowledgeSource, "format", format);
}
/**
* option name: dumpToFile
* Specifies whether the extracted ontology is written to a file or not.
* default value: true
**/
public void setDumpToFile ( ComponentManager cm, boolean dumpToFile) {
cm.applyConfigEntry(SparqlKnowledgeSource, "dumpToFile", dumpToFile);
}
/**
* option name: convertNT2RDF
* Specifies whether the extracted NTriples are converted to RDF and deleted.
* default value: true
**/
public void setConvertNT2RDF ( ComponentManager cm, boolean convertNT2RDF) {
cm.applyConfigEntry(SparqlKnowledgeSource, "convertNT2RDF", convertNT2RDF);
}
/**
* option name: useLits
* use Literals in SPARQL query
* default value: true
**/
public void setUseLits ( ComponentManager cm, boolean useLits) {
cm.applyConfigEntry(SparqlKnowledgeSource, "useLits", useLits);
}
/**
* option name: getAllSuperClasses
* If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.
* default value: true
**/
public void setGetAllSuperClasses ( ComponentManager cm, boolean getAllSuperClasses) {
cm.applyConfigEntry(SparqlKnowledgeSource, "getAllSuperClasses", getAllSuperClasses);
}
/**
* option name: useCache
* If true a Cache is used
* default value: true
**/
public void setUseCache ( ComponentManager cm, boolean useCache) {
cm.applyConfigEntry(SparqlKnowledgeSource, "useCache", useCache);
}
/**
* option name: replacePredicate
* rule for replacing predicates
* default value: null
**/
public void setReplacePredicate ( ComponentManager cm, List<StringTuple> replacePredicate) {
cm.applyConfigEntry(SparqlKnowledgeSource, "replacePredicate", replacePredicate);
}
/**
* option name: replaceObject
* rule for replacing predicates
* default value: null
**/
public void setReplaceObject ( ComponentManager cm, List<StringTuple> replaceObject) {
cm.applyConfigEntry(SparqlKnowledgeSource, "replaceObject", replaceObject);
}
/**
* option name: breakSuperClassRetrievalAfter
* stops a cyclic hierarchy after specified number of classes
* default value: 1000
**/
public void setBreakSuperClassRetrievalAfter ( ComponentManager cm, int breakSuperClassRetrievalAfter) {
cm.applyConfigEntry(SparqlKnowledgeSource, "breakSuperClassRetrievalAfter", breakSuperClassRetrievalAfter);
}
/**
* option name: closeAfterRecursion
* gets all classes for all instances
* default value: true
**/
public void setCloseAfterRecursion ( ComponentManager cm, boolean closeAfterRecursion) {
cm.applyConfigEntry(SparqlKnowledgeSource, "closeAfterRecursion", closeAfterRecursion);
}
/**
* option name: getPropertyInformation
* gets all types for extracted ObjectProperties
* default value: false
**/
public void setGetPropertyInformation ( ComponentManager cm, boolean getPropertyInformation) {
cm.applyConfigEntry(SparqlKnowledgeSource, "getPropertyInformation", getPropertyInformation);
}
/**
* option name: verbosity
* control verbosity of output for this component
* default value: warning
**/
public void setVerbosity ( ComponentManager cm, String verbosity) {
cm.applyConfigEntry(SparqlKnowledgeSource, "verbosity", verbosity);
}
/**
* option name: defaultGraphURIs
* a list of all default Graph URIs
* default value: null
**/
public void setDefaultGraphURIs ( ComponentManager cm, Set<String> defaultGraphURIs) {
cm.applyConfigEntry(SparqlKnowledgeSource, "defaultGraphURIs", defaultGraphURIs);
}
/**
* option name: namedGraphURIs
* a list of all named Graph URIs
* default value: null
**/
public void setNamedGraphURIs ( ComponentManager cm, Set<String> namedGraphURIs) {
cm.applyConfigEntry(SparqlKnowledgeSource, "namedGraphURIs", namedGraphURIs);
}

public boolean isReinitNecessary(){
return reinitNecessary;
}


}
