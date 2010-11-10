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

import java.net.URL;
import java.util.List;
import java.util.Set;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.datastructures.StringTuple;

/**
* automatically generated, do not edit manually.
* run org.dllearner.scripts.ConfigJavaGenerator to update
**/
public  class SparqlKnowledgeSourceConfigurator  implements Configurator {

private boolean reinitNecessary = false;
private SparqlKnowledgeSource sparqlKnowledgeSource;

/**
* @param sparqlKnowledgeSource see SparqlKnowledgeSource
**/
public SparqlKnowledgeSourceConfigurator(SparqlKnowledgeSource sparqlKnowledgeSource){
this.sparqlKnowledgeSource = sparqlKnowledgeSource;
}

/**
* @param url URL of SPARQL Endpoint
* @param instances relevant instances e.g. positive and negative examples in a learning problem
* @return SparqlKnowledgeSource
**/
public static SparqlKnowledgeSource getSparqlKnowledgeSource(URL url, Set<String> instances) {
SparqlKnowledgeSource component = ComponentManager.getInstance().knowledgeSource(SparqlKnowledgeSource.class);
ComponentManager.getInstance().applyConfigEntry(component, "url", url);
ComponentManager.getInstance().applyConfigEntry(component, "instances", instances);
return component;
}

/**
* url URL of SPARQL Endpoint.
* mandatory: true| reinit necessary: true
* default value: null
* @return URL 
**/
public URL getUrl() {
return (URL) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "url") ;
}
/**
* cacheDir dir of cache.
* mandatory: false| reinit necessary: true
* default value: cache
* @return String 
**/
public String getCacheDir() {
return (String) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "cacheDir") ;
}
/**
* useCache If true a Cache is used.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseCache() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "useCache") ;
}
/**
* useCacheDatabase If true, H2 database is used, otherwise one file per query is written..
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseCacheDatabase() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "useCacheDatabase") ;
}
/**
* instances relevant instances e.g. positive and negative examples in a learning problem.
* mandatory: true| reinit necessary: true
* default value: null
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getInstances() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "instances") ;
}
/**
* recursionDepth recursion depth of KB fragment selection.
* mandatory: false| reinit necessary: true
* default value: 1
* @return int 
**/
public int getRecursionDepth() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "recursionDepth") ;
}
/**
* predefinedFilter the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST.
* mandatory: false| reinit necessary: true
* default value: null
* @return String 
**/
public String getPredefinedFilter() {
return (String) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "predefinedFilter") ;
}
/**
* predefinedEndpoint the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK.
* mandatory: false| reinit necessary: true
* default value: null
* @return String 
**/
public String getPredefinedEndpoint() {
return (String) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "predefinedEndpoint") ;
}
/**
* predefinedManipulator the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR.
* mandatory: false| reinit necessary: true
* default value: null
* @return String 
**/
public String getPredefinedManipulator() {
return (String) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "predefinedManipulator") ;
}
/**
* predList list of all ignored roles.
* mandatory: false| reinit necessary: true
* default value: []
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getPredList() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "predList") ;
}
/**
* objList list of all ignored objects.
* mandatory: false| reinit necessary: true
* default value: []
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getObjList() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "objList") ;
}
/**
* saveExtractedFragment Specifies whether the extracted ontology is written to a file or not. The OWL file is written to the cache dir.Some DBpedia URI will make the XML invalid.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getSaveExtractedFragment() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "saveExtractedFragment") ;
}
/**
* replacePredicate rule for replacing predicates.
* mandatory: false| reinit necessary: true
* default value: []
* @return List(StringTuple) 
**/
@SuppressWarnings("unchecked")
public List<StringTuple> getReplacePredicate() {
return (List<StringTuple>) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "replacePredicate") ;
}
/**
* replaceObject rule for replacing predicates.
* mandatory: false| reinit necessary: true
* default value: []
* @return List(StringTuple) 
**/
@SuppressWarnings("unchecked")
public List<StringTuple> getReplaceObject() {
return (List<StringTuple>) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "replaceObject") ;
}
/**
* breakSuperClassRetrievalAfter stops a cyclic hierarchy after specified number of classes.
* mandatory: false| reinit necessary: true
* default value: 1000
* @return int 
**/
public int getBreakSuperClassRetrievalAfter() {
return (Integer) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "breakSuperClassRetrievalAfter") ;
}
/**
* useLits use Literals in SPARQL query.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getUseLits() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "useLits") ;
}
/**
* getAllSuperClasses If true then all superclasses are retrieved until the most general class (owl:Thing) is reached..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getGetAllSuperClasses() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "getAllSuperClasses") ;
}
/**
* closeAfterRecursion gets all classes for all instances.
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getCloseAfterRecursion() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "closeAfterRecursion") ;
}
/**
* getPropertyInformation gets all types for extracted ObjectProperties.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getGetPropertyInformation() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "getPropertyInformation") ;
}
/**
* dissolveBlankNodes determines whether Blanknodes are dissolved. This is a costly function..
* mandatory: false| reinit necessary: true
* default value: true
* @return boolean 
**/
public boolean getDissolveBlankNodes() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "dissolveBlankNodes") ;
}
/**
* useImprovedSparqlTupelAquisitor uses deeply nested SparqlQueries, according to recursion depth, still EXPERIMENTAL.
* mandatory: false| reinit necessary: true
* default value: false
* @return boolean 
**/
public boolean getUseImprovedSparqlTupelAquisitor() {
return (Boolean) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "useImprovedSparqlTupelAquisitor") ;
}
/**
* verbosity control verbosity of output for this component.
* mandatory: false| reinit necessary: true
* default value: warning
* @return String 
**/
public String getVerbosity() {
return (String) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "verbosity") ;
}
/**
* defaultGraphURIs a list of all default Graph URIs.
* mandatory: false| reinit necessary: true
* default value: []
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getDefaultGraphURIs() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "defaultGraphURIs") ;
}
/**
* namedGraphURIs a list of all named Graph URIs.
* mandatory: false| reinit necessary: true
* default value: []
* @return Set(String) 
**/
@SuppressWarnings("unchecked")
public Set<String> getNamedGraphURIs() {
return (Set<String>) ComponentManager.getInstance().getConfigOptionValue(sparqlKnowledgeSource,  "namedGraphURIs") ;
}

/**
* @param url URL of SPARQL Endpoint.
* mandatory: true| reinit necessary: true
* default value: null
**/
public void setUrl(URL url) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "url", url);
reinitNecessary = true;
}
/**
* @param cacheDir dir of cache.
* mandatory: false| reinit necessary: true
* default value: cache
**/
public void setCacheDir(String cacheDir) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "cacheDir", cacheDir);
reinitNecessary = true;
}
/**
* @param useCache If true a Cache is used.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseCache(boolean useCache) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "useCache", useCache);
reinitNecessary = true;
}
/**
* @param useCacheDatabase If true, H2 database is used, otherwise one file per query is written..
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseCacheDatabase(boolean useCacheDatabase) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "useCacheDatabase", useCacheDatabase);
reinitNecessary = true;
}
/**
* @param instances relevant instances e.g. positive and negative examples in a learning problem.
* mandatory: true| reinit necessary: true
* default value: null
**/
public void setInstances(Set<String> instances) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "instances", instances);
reinitNecessary = true;
}
/**
* @param recursionDepth recursion depth of KB fragment selection.
* mandatory: false| reinit necessary: true
* default value: 1
**/
public void setRecursionDepth(int recursionDepth) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "recursionDepth", recursionDepth);
reinitNecessary = true;
}
/**
* @param predefinedFilter the mode of the SPARQL Filter, use one of YAGO,SKOS,YAGOSKOS , YAGOSPECIALHIERARCHY, TEST.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setPredefinedFilter(String predefinedFilter) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "predefinedFilter", predefinedFilter);
reinitNecessary = true;
}
/**
* @param predefinedEndpoint the mode of the SPARQL Filter, use one of DBPEDIA, LOCAL, GOVTRACK, REVYU, MYOPENLINK, FACTBOOK.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setPredefinedEndpoint(String predefinedEndpoint) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "predefinedEndpoint", predefinedEndpoint);
reinitNecessary = true;
}
/**
* @param predefinedManipulator the mode of the Manipulator, use one of STANDARD, DBPEDIA-NAVIGATOR.
* mandatory: false| reinit necessary: true
* default value: null
**/
public void setPredefinedManipulator(String predefinedManipulator) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "predefinedManipulator", predefinedManipulator);
reinitNecessary = true;
}
/**
* @param predList list of all ignored roles.
* mandatory: false| reinit necessary: true
* default value: []
**/
public void setPredList(Set<String> predList) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "predList", predList);
reinitNecessary = true;
}
/**
* @param objList list of all ignored objects.
* mandatory: false| reinit necessary: true
* default value: []
**/
public void setObjList(Set<String> objList) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "objList", objList);
reinitNecessary = true;
}
/**
* @param saveExtractedFragment Specifies whether the extracted ontology is written to a file or not. The OWL file is written to the cache dir.Some DBpedia URI will make the XML invalid.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setSaveExtractedFragment(boolean saveExtractedFragment) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "saveExtractedFragment", saveExtractedFragment);
reinitNecessary = true;
}
/**
* @param replacePredicate rule for replacing predicates.
* mandatory: false| reinit necessary: true
* default value: []
**/
public void setReplacePredicate(List<StringTuple> replacePredicate) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "replacePredicate", replacePredicate);
reinitNecessary = true;
}
/**
* @param replaceObject rule for replacing predicates.
* mandatory: false| reinit necessary: true
* default value: []
**/
public void setReplaceObject(List<StringTuple> replaceObject) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "replaceObject", replaceObject);
reinitNecessary = true;
}
/**
* @param breakSuperClassRetrievalAfter stops a cyclic hierarchy after specified number of classes.
* mandatory: false| reinit necessary: true
* default value: 1000
**/
public void setBreakSuperClassRetrievalAfter(int breakSuperClassRetrievalAfter) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "breakSuperClassRetrievalAfter", breakSuperClassRetrievalAfter);
reinitNecessary = true;
}
/**
* @param useLits use Literals in SPARQL query.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setUseLits(boolean useLits) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "useLits", useLits);
reinitNecessary = true;
}
/**
* @param getAllSuperClasses If true then all superclasses are retrieved until the most general class (owl:Thing) is reached..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setGetAllSuperClasses(boolean getAllSuperClasses) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "getAllSuperClasses", getAllSuperClasses);
reinitNecessary = true;
}
/**
* @param closeAfterRecursion gets all classes for all instances.
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setCloseAfterRecursion(boolean closeAfterRecursion) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "closeAfterRecursion", closeAfterRecursion);
reinitNecessary = true;
}
/**
* @param getPropertyInformation gets all types for extracted ObjectProperties.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setGetPropertyInformation(boolean getPropertyInformation) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "getPropertyInformation", getPropertyInformation);
reinitNecessary = true;
}
/**
* @param dissolveBlankNodes determines whether Blanknodes are dissolved. This is a costly function..
* mandatory: false| reinit necessary: true
* default value: true
**/
public void setDissolveBlankNodes(boolean dissolveBlankNodes) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "dissolveBlankNodes", dissolveBlankNodes);
reinitNecessary = true;
}
/**
* @param useImprovedSparqlTupelAquisitor uses deeply nested SparqlQueries, according to recursion depth, still EXPERIMENTAL.
* mandatory: false| reinit necessary: true
* default value: false
**/
public void setUseImprovedSparqlTupelAquisitor(boolean useImprovedSparqlTupelAquisitor) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "useImprovedSparqlTupelAquisitor", useImprovedSparqlTupelAquisitor);
reinitNecessary = true;
}
/**
* @param verbosity control verbosity of output for this component.
* mandatory: false| reinit necessary: true
* default value: warning
**/
public void setVerbosity(String verbosity) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "verbosity", verbosity);
reinitNecessary = true;
}
/**
* @param defaultGraphURIs a list of all default Graph URIs.
* mandatory: false| reinit necessary: true
* default value: []
**/
public void setDefaultGraphURIs(Set<String> defaultGraphURIs) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "defaultGraphURIs", defaultGraphURIs);
reinitNecessary = true;
}
/**
* @param namedGraphURIs a list of all named Graph URIs.
* mandatory: false| reinit necessary: true
* default value: []
**/
public void setNamedGraphURIs(Set<String> namedGraphURIs) {
ComponentManager.getInstance().applyConfigEntry(sparqlKnowledgeSource, "namedGraphURIs", namedGraphURIs);
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
