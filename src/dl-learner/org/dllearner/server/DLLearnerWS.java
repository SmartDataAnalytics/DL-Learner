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
package org.dllearner.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.dllearner.Info;
import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.DBpediaNavigationSuggestor;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.NaturalLanguageDescriptionConvertVisitor;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.kb.sparql.SparqlQueryException;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;

/**
 * DL-Learner web service interface.
 * 
 * @author Jens Lehmann
 *
 */
@WebService(name = "DLLearnerWebService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DLLearnerWS {

	@SuppressWarnings("unused")
	private static Logger logger = Logger
		.getLogger(DLLearnerWS.class);
	
	private Map<Integer, ClientState> clients = new TreeMap<Integer,ClientState>();
	private Random rand=new Random();
	private static ComponentManager cm = ComponentManager.getInstance();
	
	// defines the components, which are accessible for the web service
	private static Map<String,Class<? extends KnowledgeSource>> knowledgeSourceMapping = new TreeMap<String,Class<? extends KnowledgeSource>>();
	private static Map<String,Class<? extends ReasonerComponent>> reasonerMapping = new TreeMap<String,Class<? extends ReasonerComponent>>();
	private static Map<String,Class<? extends LearningProblem>> learningProblemMapping = new TreeMap<String,Class<? extends LearningProblem>>();
	private static Map<String,Class<? extends LearningAlgorithm>> learningAlgorithmMapping = new TreeMap<String,Class<? extends LearningAlgorithm>>();
	private static Set<String> components;
	
	public DLLearnerWS() {
		// TODO maybe use ConfMapper (one could add methods there for removing
		// certain components, which should not be accessible by the web service)
		knowledgeSourceMapping.put("owlfile", OWLFile.class);
		knowledgeSourceMapping.put("sparql", SparqlKnowledgeSource.class);
		reasonerMapping.put("dig", DIGReasoner.class);
		reasonerMapping.put("owlapi", OWLAPIReasoner.class);
		reasonerMapping.put("fastInstanceChecker", FastInstanceChecker.class);
		learningProblemMapping.put("posNegDefinition", PosNegDefinitionLP.class);
		learningProblemMapping.put("posNegInclusion", PosNegInclusionLP.class);
		learningProblemMapping.put("posOnlyDefinition", PosOnlyDefinitionLP.class);
		learningAlgorithmMapping.put("random", RandomGuesser.class);
		learningAlgorithmMapping.put("bruteForce", BruteForceLearner.class);		
		learningAlgorithmMapping.put("gp", GP.class);
		learningAlgorithmMapping.put("refinement", ROLearner.class);
		learningAlgorithmMapping.put("refexamples", ExampleBasedROLComponent.class);
		learningAlgorithmMapping.put("dbpediaNavigationSuggestor", DBpediaNavigationSuggestor.class);
		components = Helper.union(knowledgeSourceMapping.keySet(),reasonerMapping.keySet());
		components = Helper.union(components, learningProblemMapping.keySet());
		components = Helper.union(components, learningAlgorithmMapping.keySet());
	}
	
	/**
	 * Returns the DL-Learner version this web service is based on.
	 * @return DL-Learner-Build.
	 */
	@WebMethod
	public String getBuild() {
		return Info.build;
	}
	
	/**
	 * Generates a unique ID for the client and initialises a session. 
	 * Using the ID the client can call the other web service methods. 
	 * Two calls to this method are guaranteed to return different results. 
	 * 
	 * @return A session ID.
	 */
	@WebMethod
	public int generateID() {
		int id;
		do {
			id = rand.nextInt();
		} while(clients.containsKey(id));
		clients.put(id, new ClientState());
		return id;
	}
	
	// returns session state or throws client not known exception
	private ClientState getState(int id) throws ClientNotKnownException {
		ClientState state = clients.get(id);
		if(state==null)
			throw new ClientNotKnownException(id);
		return state;
	}
	
	// returns the class which is referred to by the string
	private Class<? extends Component> getComponent(String component) throws UnknownComponentException {
		if(knowledgeSourceMapping.containsKey(component))
			return knowledgeSourceMapping.get(component);
		else if(reasonerMapping.containsKey(component))
			return reasonerMapping.get(component);
		else if(learningProblemMapping.containsKey(component))
			return learningProblemMapping.get(component);
		else if(learningAlgorithmMapping.containsKey(component))
			return learningAlgorithmMapping.get(component);
		else
			throw new UnknownComponentException(component);
	}
	
	///////////////////////////////////////
	// methods for basic component setup //
	///////////////////////////////////////
	
	@WebMethod
	public String[] getComponents() {
		return components.toArray(new String[components.size()]);
	}
	
	@WebMethod
	public String[] getKnowledgeSources() {
		Set<String> knowledgeSources = knowledgeSourceMapping.keySet();
		return knowledgeSources.toArray(new String[knowledgeSources.size()]);
	}
	
	@WebMethod
	public String[] getReasoners() {
		Set<String> reasoners = reasonerMapping.keySet();
		return reasoners.toArray(new String[reasoners.size()]);		
	}
	
	@WebMethod
	public String[] getLearningProblems() {
		Set<String> learningProblems = learningProblemMapping.keySet();
		return learningProblems.toArray(new String[learningProblems.size()]);		
	}
	
	@WebMethod
	public String[] getLearningAlgorithms() {
		Set<String> learningAlgorithms = learningAlgorithmMapping.keySet();
		return learningAlgorithms.toArray(new String[learningAlgorithms.size()]);		
	}	
	
	@WebMethod
	public String[] getConfigOptions(String component, boolean allInfo) throws UnknownComponentException {
		Class<? extends Component> componentClass = getComponent(component);
		List<ConfigOption<?>> options = ComponentManager.getConfigOptions(componentClass);
		String[] optionsString = new String[options.size()];
		for(int i=0; i<options.size(); i++) {
			ConfigOption<?> option = options.get(i);
			optionsString[i] = option.getName();
			if(allInfo) {
				optionsString[i] += "#" + option.getDescription();
				optionsString[i] += "#" + option.getAllowedValuesDescription();
				optionsString[i] += "#" + option.getDefaultValue();				
			}	
		}
		return optionsString;
	}
	
	/**
	 * Adds a knowledge source.
	 * 
	 * @return An identifier for the component.
	 * @throws MalformedURLException 
	 */
	@WebMethod
	public int addKnowledgeSource(int id, String component, String url) throws ClientNotKnownException, UnknownComponentException, MalformedURLException {
		ClientState state = getState(id);
		Class<? extends KnowledgeSource> ksClass = knowledgeSourceMapping.get(component);
		if(ksClass == null)
			throw new UnknownComponentException(component);
		KnowledgeSource ks = cm.knowledgeSource(ksClass);
		cm.applyConfigEntry(ks, "url", new URL(url));
		return state.addKnowledgeSource(ks);
	}
	
	@WebMethod
	public void removeKnowledgeSource(int id, int componentID) throws ClientNotKnownException {
		getState(id).removeKnowledgeSource(componentID);
	}
	
	@WebMethod
	public int setReasoner(int id, String component) throws ClientNotKnownException, UnknownComponentException {
		ClientState state = getState(id);
		Class<? extends ReasonerComponent> rcClass = reasonerMapping.get(component);
		if(rcClass == null)
			throw new UnknownComponentException(component);
		
		ReasonerComponent rc = cm.reasoner(rcClass, state.getKnowledgeSources());
		return state.setReasonerComponent(rc);
	}
	
	@WebMethod
	public int setLearningProblem(int id, String component) throws ClientNotKnownException, UnknownComponentException {
		ClientState state = getState(id);
		Class<? extends LearningProblem> lpClass = learningProblemMapping.get(component);
		if(lpClass == null)
			throw new UnknownComponentException(component);
		
		LearningProblem lp = cm.learningProblem(lpClass, state.getReasoningService());
		return state.setLearningProblem(lp);
	}
	
	@WebMethod
	public int setLearningAlgorithm(int id, String component) throws ClientNotKnownException, UnknownComponentException, LearningProblemUnsupportedException {
		ClientState state = getState(id);
		Class<? extends LearningAlgorithm> laClass = learningAlgorithmMapping.get(component);
		if(laClass == null)
			throw new UnknownComponentException(component);
		
		LearningAlgorithm la = cm.learningAlgorithm(laClass, state.getLearningProblem(), state.getReasoningService());
		return state.setLearningAlgorithm(la);
	}
	
	/**
	 * Initialise all components.
	 * @param id Session ID.
	 * @throws ComponentInitException 
	 */
	@WebMethod
	public void initAll(int id) throws ClientNotKnownException, ComponentInitException {
		ClientState state = getState(id);
		for(KnowledgeSource ks : state.getKnowledgeSources())
			ks.init();
		state.getReasonerComponent().init();
		state.getLearningProblem().init();
		state.getLearningAlgorithm().init();
	}
	
	/**
	 * Initialise the specified component.
	 * @param id Session-ID.
	 * @param componentID Component-ID.
	 * @throws ClientNotKnownException Thrown if the client ID is nor registered.
	 * @throws UnknownComponentException Thrown if the component is unknown.
	 * @throws ComponentInitException 
	 */
	@WebMethod
	public void init(int id, int componentID) throws ClientNotKnownException, UnknownComponentException, ComponentInitException {
		ClientState state = getState(id);
		Component component = state.getComponent(componentID);
		component.init();
	}
	
	/**
	 * Starts the learning algorithm and returns the best concept found. This
	 * method will block until learning is completed.
	 * 
	 * @param id Session ID.
	 * @param format The format of the result string: "manchester", "kb", "dl".
	 * @return The best solution found.
	 * @throws ClientNotKnownException
	 */	
	@WebMethod
	public String learn(int id, String format) throws ClientNotKnownException {
		ClientState state = getState(id);
		state.getLearningAlgorithm().start();
		Description solution = state.getLearningAlgorithm().getCurrentlyBestDescription();
		if(format.equals("manchester"))
			return solution.toManchesterSyntaxString(state.getReasoningService().getBaseURI(), new HashMap<String,String>());
		else if(format.equals("kb"))
			return solution.toKBSyntaxString();
		else
			return solution.toString();
	}
	
	@WebMethod
	public String learnDescriptionsEvaluated(int id, int limit) throws ClientNotKnownException {
		ClientState state = getState(id);
		state.getLearningAlgorithm().start();
		List<EvaluatedDescription> descriptions = state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescriptions(limit);
		String json = "{";
		int count = 1;
		for(EvaluatedDescription description : descriptions) {
			//FIXME this is not a general method, but specific to dbpedia
			// the mehtod name schould mention, that the return String is in JSON
			// also it throws errors
			json += "\"solution" + count + "\" : " + description.asJSON();
			
			count++;
		}
		return json;
	}
	
	/**
	 * Starts the learning algorithm and returns immediately. The learning
	 * algorithm is executed in its own thread and can be queried and 
	 * controlled using other Web Service methods.
	 * 
	 * @param id Session ID.
	 * @throws ClientNotKnownException
	 */
	@WebMethod 
	public void learnThreaded(int id) throws ClientNotKnownException {
		final ClientState state = getState(id);
		Thread learningThread = new Thread() {
			@Override
			public void run() {
//				state.setAlgorithmRunning(true);
				state.getLearningAlgorithm().start();
//				state.setAlgorithmRunning(false);
			}
		};
		learningThread.start();
	}
	
	@WebMethod
	public String getCurrentlyBestConcept(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		return state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescription().toString();
	}
	
	@WebMethod
	public String[] getCurrentlyBestConcepts(int id, int nrOfConcepts, String format) throws ClientNotKnownException {
		ClientState state = getState(id);
		List<Description> bestConcepts = state.getLearningAlgorithm().getCurrentlyBestDescriptions(nrOfConcepts);
		List<String> conc=new LinkedList<String>();
		Iterator<Description> iter=bestConcepts.iterator();
		while (iter.hasNext())
			if (format.equals("manchester"))
				conc.add(iter.next().toManchesterSyntaxString(state.getReasoningService().getBaseURI(), new HashMap<String,String>()));
			else if(format.equals("kb"))
				conc.add(iter.next().toKBSyntaxString());
			else
			    conc.add(iter.next().toString());
		return conc.toArray(new String[conc.size()]);
	}
	
	@WebMethod
	public String getCurrentlyBestEvaluatedDescriptions(int id, int limit) throws ClientNotKnownException{
		return currentlyBestEvaluatedDescriptions(id,limit,-1,false);
	}
	
	@WebMethod
	public String getCurrentlyBestEvaluatedDescriptionsFiltered(int id,int nrOfDescriptions, double accuracyThreshold, boolean filterNonMinimalDescriptions) throws ClientNotKnownException
	{
		return currentlyBestEvaluatedDescriptions(id,nrOfDescriptions,accuracyThreshold,filterNonMinimalDescriptions);
	}
	
	private String currentlyBestEvaluatedDescriptions(int id,int nrOfDescriptions, double accuracyThreshold, boolean filterNonMinimalDescriptions) throws ClientNotKnownException
	{
		ClientState state = getState(id);
		List<EvaluatedDescription> descriptions;
		if (accuracyThreshold!=-1) descriptions = state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions, accuracyThreshold, filterNonMinimalDescriptions);
		else descriptions = state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions);
		String json = "{";
		int count = 1;
		for(EvaluatedDescription description : descriptions) {
			if (count>1) json += ",\"solution" + count + "\" : " + description.asJSON();
			else json += "\"solution" + count + "\" : " + description.asJSON();
			count++;
		}
		json+="}";
		return json;
	}
	
	@WebMethod
	public boolean isAlgorithmRunning(int id) throws ClientNotKnownException {
		return getState(id).getLearningAlgorithm().isRunning();
	}
	
	/**
	 * Stops the learning algorithm smoothly.
	 * @param id
	 * @throws ClientNotKnownException
	 */
	@WebMethod
	public void stop(int id) throws ClientNotKnownException {
		getState(id).getLearningAlgorithm().stop();
	}
	
	/////////////////////////////////////////
	// methods for component configuration //
	/////////////////////////////////////////
	
	@WebMethod
	public void setPositiveExamples(int id, String[] positiveExamples) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<String> posExamples = new TreeSet<String>(Arrays.asList(positiveExamples));
		cm.applyConfigEntry(state.getLearningProblem(), "positiveExamples", posExamples);
	}
	
	@WebMethod
	public void setNegativeExamples(int id, String[] negativeExamples) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<String> negExamples = new TreeSet<String>(Arrays.asList(negativeExamples));
		cm.applyConfigEntry(state.getLearningProblem(), "negativeExamples", negExamples);
	}
	
	@WebMethod
	public void applyConfigEntryInt(int sessionID, int componentID, String optionName, Integer value) throws ClientNotKnownException, UnknownComponentException	{
		applyConfigEntry(sessionID, componentID,optionName,value);
	}
	
	@WebMethod
	public void applyConfigEntryString(int sessionID, int componentID, String optionName, String value) throws ClientNotKnownException, UnknownComponentException {
		applyConfigEntry(sessionID, componentID,optionName,value);
	}
	
	@WebMethod
	public void applyConfigEntryURL(int sessionID, int componentID, String optionName, String value) throws ClientNotKnownException, UnknownComponentException, MalformedURLException {
		// URLs are passed as String and then converted
		URL url = new URL(value);
		applyConfigEntry(sessionID, componentID,optionName,url);
	}	
	
	@WebMethod
	public void applyConfigEntryStringArray(int sessionID, int componentID, String optionName, String[] value) throws ClientNotKnownException, UnknownComponentException {
		Set<String> stringSet = new TreeSet<String>(Arrays.asList(value));
		applyConfigEntry(sessionID, componentID,optionName,stringSet);
	}
	
	@WebMethod
	public void applyConfigEntryBoolean(int sessionID, int componentID, String optionName, Boolean value) throws ClientNotKnownException, UnknownComponentException	{
		applyConfigEntry(sessionID, componentID,optionName,value);
	}
	
	private void applyConfigEntry(int sessionID, int componentID, String optionName, Object value) throws ClientNotKnownException, UnknownComponentException {
		ClientState state = getState(sessionID);
		Component component = state.getComponent(componentID);
		cm.applyConfigEntry(component, optionName, value);
	}
	
	@WebMethod
	public String[] getConfigOptionValueStringArray(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, String[].class);
	}
	
	@WebMethod
	public String getConfigOptionValueString(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, String.class);
	}
	
	@WebMethod
	public String getConfigOptionValueURL(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		URL url = getConfigOptionValue(sessionID, componentID, optionName, URL.class);
		return url.toString();
	}	
	
	@WebMethod
	public Double getConfigOptionValueDouble(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, Double.class);
	}
	
	@WebMethod
	public Boolean getConfigOptionValueBoolean(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, Boolean.class);
	}
	
	@WebMethod
	public Integer getConfigOptionValueInt(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, Integer.class);
	}
	
	@SuppressWarnings({"unchecked"})
	private <T> T getConfigOptionValue(int sessionID, int componentID, String optionName, Class<T> clazz) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		Object value = getConfigOptionValue(sessionID, componentID, optionName);
		if(clazz.isInstance(value))
			return (T) value;
		else
			throw new ConfigOptionTypeException(optionName, clazz, value.getClass());
	}
	
	private Object getConfigOptionValue(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException {
		ClientState state = getState(sessionID);
		Component component = state.getComponent(componentID);
		return cm.getConfigOptionValue(component, optionName);
	}
	
	////////////////////////////////////
	// reasoning and querying methods //
	////////////////////////////////////
	
	@WebMethod
	public String[] getAtomicConcepts(int id) throws ClientNotKnownException {
		Set<NamedClass> atomicConcepts = getState(id).getReasoningService().getNamedClasses();
		return Datastructures.sortedSet2StringListConcepts(atomicConcepts);
	}
	
	@WebMethod
	public String getSubsumptionHierarchy(int id) throws ClientNotKnownException {
		return getState(id).getReasoningService().toString();
	}
	
	@WebMethod
	public String[] retrieval(int id, String conceptString) throws ClientNotKnownException, ParseException {
		ClientState state = getState(id);
		// call parser to parse concept
		Description concept = null;
		concept = KBParser.parseConcept(conceptString);
		Set<Individual> individuals = state.getReasoningService().retrieval(concept);
		return Datastructures.sortedSet2StringListIndividuals(individuals);
	}
	
	@WebMethod
	public int getConceptLength(String conceptString) throws ParseException {
		// call parser to parse concept
		return KBParser.parseConcept(conceptString).getLength();
	}
	
	@WebMethod
	public String[] getAtomicRoles(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<ObjectProperty> roles = state.getReasoningService().getObjectProperties();
		return Datastructures.sortedSet2StringListRoles(roles);
	}
	
	@WebMethod
	public String[] getInstances(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<Individual> individuals = state.getReasoningService().getIndividuals();
		return Datastructures.sortedSet2StringListIndividuals(individuals);
	}
	
	@WebMethod
	public String[] getIndividualsForARole(int id, String role) throws ClientNotKnownException {
		ClientState state = getState(id);
		Map<Individual,SortedSet<Individual>> m = state.getReasoningService().getRoleMembers(new ObjectProperty(role));
		Set<Individual> individuals = m.keySet();
		return Datastructures.sortedSet2StringListIndividuals(individuals);
	}
	
	////////////////////////////////////////
	//     SPARQL component methods       //
	////////////////////////////////////////
	
		
	@WebMethod
	public String getAsJSON(int sessionID, int queryID) throws ClientNotKnownException, SparqlQueryException
	{
		ClientState state = getState(sessionID);
		//ResultSet resultSet=null;
		String json = null;
		try {
		    json = state.getQuery(queryID).getJson();
		}catch (Exception e) {
		    e.printStackTrace();
		    throw new SparqlQueryException("SparqlQuery failed"+e.toString());
		}
		
		if(json == null) { throw new SparqlQueryException("Sparql Query failed. Please try again later.");}
		return json;
		//if ((json=state.getQuery(queryID).getJson())!=null) return json;
		//else if ((resultSet=state.getQuery(queryID).getResultSet())!=null) return SparqlQuery.getAsJSON(resultSet); 
		//else return SparqlQuery.getAsJSON(state.getQuery(queryID).send());
	}
	
	@WebMethod
	public String getAsXMLString(int sessionID, int queryID) throws ClientNotKnownException, SparqlQueryException
	{
		ClientState state = getState(sessionID);
		
		String xml = null;
		try{	
		    xml = state.getQuery(queryID).getXMLString();
        	}catch (Exception e) {
        	    e.printStackTrace();
        	    throw new SparqlQueryException("SparqlQuery failed"+e.toString());
        	}
		
		if(xml == null) throw new SparqlQueryException("SparqlQuery failed xml was null");
		return xml;
		//if ((resultSet=state.getQuery(queryID).getResultSet())!=null) return SparqlQuery.getAsXMLString(resultSet);
		//if ((json=state.getQuery(queryID).getJson())!=null) return SparqlQuery.getAsXMLString(SparqlQuery.JSONtoResultSet(json));
		//else return SparqlQuery.getAsXMLString(state.getQuery(queryID).send());
	}
	
	@WebMethod
	public int sparqlQueryThreaded(int sessionID, int componentID, String query) throws ClientNotKnownException
	{
		final ClientState state = getState(sessionID);
		Component component = state.getComponent(componentID);
		final SparqlKnowledgeSource ks=(SparqlKnowledgeSource)component;
		final int id=state.addQuery(ks.sparqlQuery(query));
		Thread sparqlThread = new Thread() {
			@Override
			public void run() {
				if (ks.isUseCache()){
					state.getQuery(id).setRunning(true);
					Cache cache=new Cache(ks.getCacheDir());
					cache.executeSparqlQuery(state.getQuery(id));
					state.getQuery(id).setRunning(false);
				}
				else{
					state.getQuery(id).setRunning(true);
					state.getQuery(id).send();
					state.getQuery(id).setRunning(false);
				}
			}
		};
		sparqlThread.start();
		return id;
	}
	
	@WebMethod
	public String sparqlQuery(int sessionID, int componentID, String query) throws ClientNotKnownException
	{
		ClientState state = getState(sessionID);
		Component component = state.getComponent(componentID);
		SparqlKnowledgeSource ks=(SparqlKnowledgeSource)component;
		return ks.getSPARQLTasks().query(query);
		/*SparqlQuery sparql=ks.sparqlQuery(query);
		if (ks.isUseCache()){
			Cache cache=new Cache(ks.getCacheDir());
			return cache.executeSparqlQuery(sparql);
		}
		else return sparql.getJson();*/
	}
	
	@WebMethod
	public boolean isSparqlQueryRunning(int sessionID, int queryID) throws ClientNotKnownException
	{
		ClientState state = getState(sessionID);
		return state.getQuery(queryID).isRunning();
	}
	
	@WebMethod
	public void stopSparqlThread(int sessionID, int queryID) throws ClientNotKnownException
	{
		ClientState state = getState(sessionID);
		state.getQuery(queryID).stop();
	}
	
	@WebMethod
	public int[] getConceptDepth(int id, int nrOfConcepts) throws ClientNotKnownException {
		ClientState state = getState(id);
		List<Description> bestConcepts = state.getLearningAlgorithm().getCurrentlyBestDescriptions(nrOfConcepts);
		Iterator<Description> iter=bestConcepts.iterator();
		int[] length=new int[bestConcepts.size()];
		int i=0;
		while (iter.hasNext()){
			length[i]=iter.next().getDepth();
			i++;
		}
		return length;
	}
	
	@WebMethod
	public int[] getConceptArity(int id, int nrOfConcepts) throws ClientNotKnownException {
		ClientState state = getState(id);
		List<Description> bestConcepts = state.getLearningAlgorithm().getCurrentlyBestDescriptions(nrOfConcepts);
		Iterator<Description> iter=bestConcepts.iterator();
		int[] arity=new int[bestConcepts.size()];
		int i=0;
		while (iter.hasNext()){
			arity[i]=iter.next().getArity();
			i++;
		}
		return arity;
	}
	
	@WebMethod
	public String SparqlRetrieval(String conceptString) throws ParseException {
		// call parser to parse concept
		return SparqlQueryDescriptionConvertVisitor.getSparqlQuery(conceptString);
	}
	
	@WebMethod
	public String getNaturalDescription(String conceptString) throws ParseException {
		// call parser to parse concept
		return NaturalLanguageDescriptionConvertVisitor.getNaturalLanguageDescription(conceptString);
	}
	
	@WebMethod
	public String[] getNegativeExamples(int sessionID, int componentID,String[] positives, int results, String namespace) throws ClientNotKnownException
	{
		int sparqlResultSetLimit = 500;
		SortedSet<String> positiveSet = new TreeSet<String>(Arrays.asList(positives));
		ClientState state = getState(sessionID);
		Component component = state.getComponent(componentID);
		SparqlKnowledgeSource ks=(SparqlKnowledgeSource)component;
		SPARQLTasks task=ks.getSPARQLTasks();
		AutomaticNegativeExampleFinderSPARQL finder=new AutomaticNegativeExampleFinderSPARQL(positiveSet,task);
		
		finder.makeNegativeExamplesFromParallelClasses(positiveSet, sparqlResultSetLimit);
		SortedSet<String> negExamples=finder.getNegativeExamples(results);
		if(negExamples.isEmpty()){
			 finder.makeNegativeExamplesFromRelatedInstances(positiveSet, namespace);
			 negExamples = finder.getNegativeExamples(results);
			 if(negExamples.isEmpty()){
				 finder.makeNegativeExamplesFromSuperClassesOfInstances(positiveSet, sparqlResultSetLimit);
				 negExamples = finder.getNegativeExamples(results);
				 if(negExamples.isEmpty()) {
					 finder.makeNegativeExamplesFromRandomInstances();
					 negExamples = finder.getNegativeExamples(results);
				 }
			 }
		}
		
		return negExamples.toArray(new String[negExamples.size()]);
	}
	
	@WebMethod
	public void debug(String deb)
	{
		System.out.println(deb);
	}
}
