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
public class SparqlKnowledgeSourceConfigurator extends Configurator {

SparqlKnowledgeSource SparqlKnowledgeSource;
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


/**
relevant instances e.g. positive and negative examples in a learning problem
**/
public void setMandatoryOptions (Set<String> instances ) {
this.instances = instances;
}
/**
relevant instances e.g. positive and negative examples in a learning problem
**/
public static SparqlKnowledgeSource getSparqlKnowledgeSource (ComponentManager cm, Set<String> instances ) {
SparqlKnowledgeSource component = cm.knowledgeSource(SparqlKnowledgeSource.class);
cm.applyConfigEntry(component, "instances", instances);
return component;
}


@SuppressWarnings({ "unchecked" })
public <T> void applyConfigEntry(ConfigEntry<T> entry){
String optionName = entry.getOptionName();
if (optionName.equals(url)){
url = (String)  entry.getValue();
}else if (optionName.equals(cacheDir)){
cacheDir = (String)  entry.getValue();
}else if (optionName.equals(instances)){
instances = (Set<String>)  entry.getValue();
}else if (optionName.equals(recursionDepth)){
recursionDepth = (Integer)  entry.getValue();
}else if (optionName.equals(predefinedFilter)){
predefinedFilter = (String)  entry.getValue();
}else if (optionName.equals(predefinedEndpoint)){
predefinedEndpoint = (String)  entry.getValue();
}else if (optionName.equals(predefinedManipulator)){
predefinedManipulator = (String)  entry.getValue();
}else if (optionName.equals(predList)){
predList = (Set<String>)  entry.getValue();
}else if (optionName.equals(objList)){
objList = (Set<String>)  entry.getValue();
}else if (optionName.equals(classList)){
classList = (Set<String>)  entry.getValue();
}else if (optionName.equals(format)){
format = (String)  entry.getValue();
}else if (optionName.equals(dumpToFile)){
dumpToFile = (Boolean)  entry.getValue();
}else if (optionName.equals(convertNT2RDF)){
convertNT2RDF = (Boolean)  entry.getValue();
}else if (optionName.equals(useLits)){
useLits = (Boolean)  entry.getValue();
}else if (optionName.equals(getAllSuperClasses)){
getAllSuperClasses = (Boolean)  entry.getValue();
}else if (optionName.equals(useCache)){
useCache = (Boolean)  entry.getValue();
}else if (optionName.equals(replacePredicate)){
replacePredicate = (List<StringTuple>)  entry.getValue();
}else if (optionName.equals(replaceObject)){
replaceObject = (List<StringTuple>)  entry.getValue();
}else if (optionName.equals(breakSuperClassRetrievalAfter)){
breakSuperClassRetrievalAfter = (Integer)  entry.getValue();
}else if (optionName.equals(closeAfterRecursion)){
closeAfterRecursion = (Boolean)  entry.getValue();
}else if (optionName.equals(getPropertyInformation)){
getPropertyInformation = (Boolean)  entry.getValue();
}else if (optionName.equals(verbosity)){
verbosity = (String)  entry.getValue();
}else if (optionName.equals(defaultGraphURIs)){
defaultGraphURIs = (Set<String>)  entry.getValue();
}else if (optionName.equals(namedGraphURIs)){
namedGraphURIs = (Set<String>)  entry.getValue();
}
}


/**
* URL of SPARQL Endpoint
**/
public void setUrl (String url) {
this.url = url;
}

/**
* dir of cache
**/
public void setCacheDir (String cacheDir) {
this.cacheDir = cacheDir;
}

/**
* relevant instances e.g. positive and negative examples in a learning problem
**/
public void setInstances (Set<String> instances) {
this.instances = instances;
}

/**
* recursion depth of KB fragment selection
**/
public void setRecursionDepth (int recursionDepth) {
this.recursionDepth = recursionDepth;
}

/**
* the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST
**/
public void setPredefinedFilter (String predefinedFilter) {
this.predefinedFilter = predefinedFilter;
}

/**
* the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK
**/
public void setPredefinedEndpoint (String predefinedEndpoint) {
this.predefinedEndpoint = predefinedEndpoint;
}

/**
* the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR
**/
public void setPredefinedManipulator (String predefinedManipulator) {
this.predefinedManipulator = predefinedManipulator;
}

/**
* list of all ignored roles
**/
public void setPredList (Set<String> predList) {
this.predList = predList;
}

/**
* list of all ignored objects
**/
public void setObjList (Set<String> objList) {
this.objList = objList;
}

/**
* list of all ignored classes
**/
public void setClassList (Set<String> classList) {
this.classList = classList;
}

/**
* N-TRIPLES or KB format
**/
public void setFormat (String format) {
this.format = format;
}

/**
* Specifies whether the extracted ontology is written to a file or not.
**/
public void setDumpToFile (boolean dumpToFile) {
this.dumpToFile = dumpToFile;
}

/**
* Specifies whether the extracted NTriples are converted to RDF and deleted.
**/
public void setConvertNT2RDF (boolean convertNT2RDF) {
this.convertNT2RDF = convertNT2RDF;
}

/**
* use Literals in SPARQL query
**/
public void setUseLits (boolean useLits) {
this.useLits = useLits;
}

/**
* If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.
**/
public void setGetAllSuperClasses (boolean getAllSuperClasses) {
this.getAllSuperClasses = getAllSuperClasses;
}

/**
* If true a Cache is used
**/
public void setUseCache (boolean useCache) {
this.useCache = useCache;
}

/**
* rule for replacing predicates
**/
public void setReplacePredicate (List<StringTuple> replacePredicate) {
this.replacePredicate = replacePredicate;
}

/**
* rule for replacing predicates
**/
public void setReplaceObject (List<StringTuple> replaceObject) {
this.replaceObject = replaceObject;
}

/**
* stops a cyclic hierarchy after specified number of classes
**/
public void setBreakSuperClassRetrievalAfter (int breakSuperClassRetrievalAfter) {
this.breakSuperClassRetrievalAfter = breakSuperClassRetrievalAfter;
}

/**
* gets all classes for all instances
**/
public void setCloseAfterRecursion (boolean closeAfterRecursion) {
this.closeAfterRecursion = closeAfterRecursion;
}

/**
* gets all types for extracted ObjectProperties
**/
public void setGetPropertyInformation (boolean getPropertyInformation) {
this.getPropertyInformation = getPropertyInformation;
}

/**
* control verbosity of output for this component
**/
public void setVerbosity (String verbosity) {
this.verbosity = verbosity;
}

/**
* a list of all default Graph URIs
**/
public void setDefaultGraphURIs (Set<String> defaultGraphURIs) {
this.defaultGraphURIs = defaultGraphURIs;
}

/**
* a list of all named Graph URIs
**/
public void setNamedGraphURIs (Set<String> namedGraphURIs) {
this.namedGraphURIs = namedGraphURIs;
}



/**
* URL of SPARQL Endpoint
* 
**/
public String getUrl ( ) {
return this.url;
}

/**
* dir of cache
* 
**/
public String getCacheDir ( ) {
return this.cacheDir;
}

/**
* relevant instances e.g. positive and negative examples in a learning problem
* 
**/
public Set<String> getInstances ( ) {
return this.instances;
}

/**
* recursion depth of KB fragment selection
* 
**/
public int getRecursionDepth ( ) {
return this.recursionDepth;
}

/**
* the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST
* 
**/
public String getPredefinedFilter ( ) {
return this.predefinedFilter;
}

/**
* the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK
* 
**/
public String getPredefinedEndpoint ( ) {
return this.predefinedEndpoint;
}

/**
* the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR
* 
**/
public String getPredefinedManipulator ( ) {
return this.predefinedManipulator;
}

/**
* list of all ignored roles
* 
**/
public Set<String> getPredList ( ) {
return this.predList;
}

/**
* list of all ignored objects
* 
**/
public Set<String> getObjList ( ) {
return this.objList;
}

/**
* list of all ignored classes
* 
**/
public Set<String> getClassList ( ) {
return this.classList;
}

/**
* N-TRIPLES or KB format
* 
**/
public String getFormat ( ) {
return this.format;
}

/**
* Specifies whether the extracted ontology is written to a file or not.
* 
**/
public boolean getDumpToFile ( ) {
return this.dumpToFile;
}

/**
* Specifies whether the extracted NTriples are converted to RDF and deleted.
* 
**/
public boolean getConvertNT2RDF ( ) {
return this.convertNT2RDF;
}

/**
* use Literals in SPARQL query
* 
**/
public boolean getUseLits ( ) {
return this.useLits;
}

/**
* If true then all superclasses are retrieved until the most general class (owl:Thing) is reached.
* 
**/
public boolean getGetAllSuperClasses ( ) {
return this.getAllSuperClasses;
}

/**
* If true a Cache is used
* 
**/
public boolean getUseCache ( ) {
return this.useCache;
}

/**
* rule for replacing predicates
* 
**/
public List<StringTuple> getReplacePredicate ( ) {
return this.replacePredicate;
}

/**
* rule for replacing predicates
* 
**/
public List<StringTuple> getReplaceObject ( ) {
return this.replaceObject;
}

/**
* stops a cyclic hierarchy after specified number of classes
* 
**/
public int getBreakSuperClassRetrievalAfter ( ) {
return this.breakSuperClassRetrievalAfter;
}

/**
* gets all classes for all instances
* 
**/
public boolean getCloseAfterRecursion ( ) {
return this.closeAfterRecursion;
}

/**
* gets all types for extracted ObjectProperties
* 
**/
public boolean getGetPropertyInformation ( ) {
return this.getPropertyInformation;
}

/**
* control verbosity of output for this component
* 
**/
public String getVerbosity ( ) {
return this.verbosity;
}

/**
* a list of all default Graph URIs
* 
**/
public Set<String> getDefaultGraphURIs ( ) {
return this.defaultGraphURIs;
}

/**
* a list of all named Graph URIs
* 
**/
public Set<String> getNamedGraphURIs ( ) {
return this.namedGraphURIs;
}



}
