/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
import org.dllearner.cli.ConfMapper;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.NaturalLanguageDescriptionConvertVisitor;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.kb.sparql.SparqlQueryException;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;

/**
 * DL-Learner web service interface. The web service makes use of the component
 * architecture of DL-Learner (see 
 * <a href="http://dl-learner.org/wiki/Architecture">architecture wiki page</a>),
 * i.e. it allows to create, configure and run components. In addition, it provides 
 * access to some reasoning and querying methods.
 * 
 * @author Jens Lehmann
 *
 */
@WebService(name = "DLLearnerWebService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DLLearnerWS {

	private static Logger logger = Logger.getLogger(DLLearnerWS.class);
	
	private Map<Integer, ClientState> clients = new TreeMap<Integer,ClientState>();
	private Random rand=new Random();
	private static ComponentManager cm = ComponentManager.getInstance();
	private static ConfMapper confMapper = new ConfMapper();
	
	/**
	 * Returns the DL-Learner version this web service is based on.
	 * @return DL-Learner-Build.
	 */
	@WebMethod
	public String getBuild() {
		return Info.build;
	}
	
	/**
	 * Method to check whether web service is online and how fast it responses.
	 * This method simply returns true.
	 * @return Always returns true.
	 */
	@WebMethod
	public boolean ping() {
		return true;
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
			id = Math.abs(rand.nextInt());
		} while(clients.containsKey(id));
		clients.put(id, new ClientState());
		logger.info("New client " + id + " at DL-Learner web service.");
		return id;
	}
	
	///////////////////////////////////////
	// methods for basic component setup //
	///////////////////////////////////////
	
	/**
	 * Gets a list of all DL-Learner components accessible via this web service. 
	 * @return All components accessible via this web service.
	 */
	@WebMethod
	public String[] getComponents() {
		Set<String> components = confMapper.getComponents();
		return components.toArray(new String[components.size()]);
	}
	
	/**
	 * Gets a list of all DL-Learner knowledge source components accessible via this web service. 
	 * @return All knowledge source components accessible via this web service.
	 */
	@WebMethod
	public String[] getKnowledgeSources() {
		Set<String> knowledgeSources = confMapper.getKnowledgeSources();
		return knowledgeSources.toArray(new String[knowledgeSources.size()]);
	}
	
	/**
	 * Gets a list of all DL-Learner reasoner components accessible via this web service. 
	 * @return All reasoner components accessible via this web service.
	 */	
	@WebMethod
	public String[] getReasoners() {
		Set<String> reasoners = confMapper.getReasoners();
		return reasoners.toArray(new String[reasoners.size()]);		
	}
	
	/**
	 * Gets a list of all DL-Learner learning problem components accessible via this web service. 
	 * @return All learning problem components accessible via this web service.
	 */	
	@WebMethod
	public String[] getLearningProblems() {
		Set<String> learningProblems = confMapper.getLearningProblems();
		return learningProblems.toArray(new String[learningProblems.size()]);		
	}
	
	/**
	 * Gets a list of all DL-Learner learning algorithm components accessible via this web service. 
	 * @return All learning algorithm components accessible via this web service.
	 */	
	@WebMethod
	public String[] getLearningAlgorithms() {
		Set<String> learningAlgorithms = confMapper.getLearningAlgorithms();
		return learningAlgorithms.toArray(new String[learningAlgorithms.size()]);		
	}	
	
	/**
	 * Gets the configuration options supported by the component. This allows e.g. to
	 * automatically build user interfaces for configuring components.
	 * @param component Name of the component.
	 * @param allInfo Whether or not complete information is desired (including option description, allowed values, default value).
	 * @return A list of configuration options supported by the component.
	 * @throws UnknownComponentException Thrown if component is not known (see {@link #getComponents()}).
	 */
	@WebMethod
	public String[] getConfigOptions(String component, boolean allInfo) throws UnknownComponentException {
		Class<? extends AbstractComponent> componentClass = confMapper.getComponentClass(component);
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
	 * @param id The session ID.
	 * @param component The name of the component.
	 * @param url The URL of the knowledge source.
	 * @return An identifier for the component.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException Thrown if component is not known (see {@link #getComponents()}).
	 * @throws MalformedURLException Thrown if passed URL is malformed.
	 */
	@WebMethod
	public int addKnowledgeSource(int id, String component, String url) throws ClientNotKnownException, UnknownComponentException, MalformedURLException {
		ClientState state = getState(id);
		Class<? extends AbstractKnowledgeSource> ksClass = confMapper.getKnowledgeSourceClass(component);
		if(ksClass == null)
			throw new UnknownComponentException(component);
		AbstractKnowledgeSource ks = cm.knowledgeSource(ksClass);
		cm.applyConfigEntry(ks, "url", new URL(url));
		return state.addKnowledgeSource(ks);
	}
	
	/**
	 * Removes a knowledge source.
	 * 
	 * @param id The session ID.
	 * @param componentID ID of knowledge source to remove.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public void removeKnowledgeSource(int id, int componentID) throws ClientNotKnownException {
		getState(id).removeKnowledgeSource(componentID);
	}
	
	/**
	 * Sets the reasoner to use.
	 * 
	 * @param id The session ID.
	 * @param component The name of the component.
	 * @return An identifier for the component.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException  Thrown if component is not known (see {@link #getComponents()}).
	 */
	@WebMethod
	public int setReasoner(int id, String component) throws ClientNotKnownException, UnknownComponentException {
		ClientState state = getState(id);
		Class<? extends AbstractReasonerComponent> rcClass = confMapper.getReasonerComponentClass(component);
		if(rcClass == null)
			throw new UnknownComponentException(component);
		
		AbstractReasonerComponent rc = cm.reasoner(rcClass, state.getKnowledgeSources());
		return state.setReasonerComponent(rc);
	}
	
	/**
	 * Sets the learning problem to use.
	 * 
	 * @param id The session ID.
	 * @param component The name of the component.
	 * @return An identifier for the component.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException  Thrown if component is not known (see {@link #getComponents()}).
	 */
	@WebMethod
	public int setLearningProblem(int id, String component) throws ClientNotKnownException, UnknownComponentException {
		ClientState state = getState(id);
		Class<? extends AbstractLearningProblem> lpClass = confMapper.getLearningProblemClass(component);
		if(lpClass == null)
			throw new UnknownComponentException(component);
		
		AbstractLearningProblem lp = cm.learningProblem(lpClass, state.getReasonerComponent());
		return state.setLearningProblem(lp);
	}
	
	/**
	 * Sets the learning algorithm to use.
	 * 
	 * @param id The session ID.
	 * @param component The name of the component.
	 * @return An identifier for the component.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException  Thrown if component is not known (see {@link #getComponents()}).
	 * @throws LearningProblemUnsupportedException Thrown if the learning problem is not supported by the specified learning algorithm.
	 */
	@WebMethod
	public int setLearningAlgorithm(int id, String component) throws ClientNotKnownException, UnknownComponentException, LearningProblemUnsupportedException {
		ClientState state = getState(id);
		Class<? extends AbstractCELA> laClass = confMapper.getLearningAlgorithmClass(component);
		if(laClass == null)
			throw new UnknownComponentException(component);
		
		AbstractCELA la = cm.learningAlgorithm(laClass, state.getLearningProblem(), state.getReasonerComponent());
		return state.setLearningAlgorithm(la);
	}
	
	/**
	 * Initialise all components.
	 * @param id Session ID.
	 * @throws ComponentInitException Thrown if an error occurs during component initialisation.
	 */
	@WebMethod
	public void initAll(int id) throws ClientNotKnownException, ComponentInitException {
		ClientState state = getState(id);
		for(AbstractKnowledgeSource ks : state.getKnowledgeSources())
			ks.init();
		state.getReasonerComponent().init();
		state.getLearningProblem().init();
		state.getLearningAlgorithm().init();
	}
	
	/**
	 * Initialise the specified component.
	 * @param id Session-ID.
	 * @param componentID Component-ID.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException Thrown if the component is unknown.
	 * @throws ComponentInitException 
	 */
	@WebMethod
	public void init(int id, int componentID) throws ClientNotKnownException, UnknownComponentException, ComponentInitException {
		ClientState state = getState(id);
		AbstractComponent component = state.getComponent(componentID);
		component.init();
	}
	
	/**
	 * Starts the learning algorithm and returns the best concept found. This
	 * method will block until learning is completed.
	 * 
	 * @param id Session ID.
	 * @param format The format of the result string: "manchester", "kb", "dl".
	 * @return The best solution found.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */	
	@WebMethod
	public String learn(int id, String format) throws ClientNotKnownException {
		ClientState state = getState(id);
		state.getLearningAlgorithm().start();
		Description solution = state.getLearningAlgorithm().getCurrentlyBestDescription();
		if(format.equals("manchester"))
			return solution.toManchesterSyntaxString(state.getReasonerComponent().getBaseURI(), new HashMap<String,String>());
		else if(format.equals("kb"))
			return solution.toKBSyntaxString();
		else
			return solution.toString();
	}
	
	/**
	 * Returns a list of JSON encoded description including extra information
	 * (which partially depends on the learning problem) such as the accuracy
	 * of the learned description.
	 * 
	 * @param id The session ID.
	 * @return A JSON string encoding learned descriptions.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public String learnDescriptionsEvaluated(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		state.getLearningAlgorithm().start();
		TreeSet<? extends EvaluatedDescription> descriptions = state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescriptions();
		String json = "{";
		int count = 1;
		for(EvaluatedDescription description : descriptions.descendingSet()) {
			if (count>1) json += ",\"solution" + count + "\" : " + description.asJSON();
			else json += "\"solution" + count + "\" : " + description.asJSON();
			count++;
		}
		json+="}";
		return json;
	}	
	
	/**
	 * Returns a list of JSON encoded description including extra information
	 * (which partially depends on the learning problem) such as the accuracy
	 * of the learned description.
	 * 
	 * @param id The session ID.
	 * @param limit Maximum number of results desired.
	 * @return A JSON string encoding learned descriptions.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public String learnDescriptionsEvaluatedLimit(int id, int limit) throws ClientNotKnownException {
		ClientState state = getState(id);
		state.getLearningAlgorithm().start();
		List<? extends EvaluatedDescription> descriptions = state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescriptions(limit);
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
	
	/**
	 * Starts the learning algorithm and returns immediately. The learning
	 * algorithm is executed in its own thread and can be queried and 
	 * controlled using other Web Service methods.
	 * 
	 * @param id Session ID.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
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
	
	/**
	 * 
	 * @param id The session ID.
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public String getCurrentlyBestConcept(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		return state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescription().toString();
	}
	
	/**
	 * 
	 * @param id The session ID.
	 * @param nrOfConcepts
	 * @param format
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public String[] getCurrentlyBestConcepts(int id, int nrOfConcepts, String format) throws ClientNotKnownException {
		ClientState state = getState(id);
		List<Description> bestConcepts = state.getLearningAlgorithm().getCurrentlyBestDescriptions(nrOfConcepts);
		List<String> conc=new LinkedList<String>();
		Iterator<Description> iter=bestConcepts.iterator();
		while (iter.hasNext())
			if (format.equals("manchester"))
				conc.add(iter.next().toManchesterSyntaxString(state.getReasonerComponent().getBaseURI(), new HashMap<String,String>()));
			else if(format.equals("kb"))
				conc.add(iter.next().toKBSyntaxString());
			else
			    conc.add(iter.next().toString());
		return conc.toArray(new String[conc.size()]);
	}
	
	/**
	 * 
	 * @param id The session ID.
	 * @param limit
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public String getCurrentlyBestEvaluatedDescriptions(int id, int limit) throws ClientNotKnownException{
		return currentlyBestEvaluatedDescriptions(id,limit,-1,false);
	}
	
	/**
	 * 
	 * @param id The session ID.
	 * @param nrOfDescriptions
	 * @param accuracyThreshold
	 * @param filterNonMinimalDescriptions
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public String getCurrentlyBestEvaluatedDescriptionsFiltered(int id,int nrOfDescriptions, double accuracyThreshold, boolean filterNonMinimalDescriptions) throws ClientNotKnownException
	{
		return currentlyBestEvaluatedDescriptions(id,nrOfDescriptions,accuracyThreshold,filterNonMinimalDescriptions);
	}
	
	/**
	 * 
	 * @param id The session ID.
	 * @param nrOfDescriptions
	 * @param accuracyThreshold
	 * @param filterNonMinimalDescriptions
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	private String currentlyBestEvaluatedDescriptions(int id,int nrOfDescriptions, double accuracyThreshold, boolean filterNonMinimalDescriptions) throws ClientNotKnownException
	{
		ClientState state = getState(id);
		List<? extends EvaluatedDescription> descriptions;
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
	
	/**
	 * 
	 * @param id The session ID.
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public boolean isAlgorithmRunning(int id) throws ClientNotKnownException {
		return getState(id).getLearningAlgorithm().isRunning();
	}
	
	/**
	 * Stops the learning algorithm smoothly.
	 * @param id The session ID.
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public void stop(int id) throws ClientNotKnownException {
		getState(id).getLearningAlgorithm().stop();
	}
	
	/////////////////////////////////////////
	// methods for component configuration //
	/////////////////////////////////////////
	
	/**
	 * 
	 * @param id The session ID.
	 * @param positiveExamples
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */ 
	@WebMethod
	public void setPositiveExamples(int id, String[] positiveExamples) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<String> posExamples = new TreeSet<String>(Arrays.asList(positiveExamples));
		cm.applyConfigEntry(state.getLearningProblem(), "positiveExamples", posExamples);
	}
	

	/**
	 * 
	 * @param id The session ID.
	 * @param negativeExamples
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 */
	@WebMethod
	public void setNegativeExamples(int id, String[] negativeExamples) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<String> negExamples = new TreeSet<String>(Arrays.asList(negativeExamples));
		cm.applyConfigEntry(state.getLearningProblem(), "negativeExamples", negExamples);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @param value
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 */
	@WebMethod
	public void applyConfigEntryInt(int sessionID, int componentID, String optionName, Integer value) throws ClientNotKnownException, UnknownComponentException	{
		applyConfigEntry(sessionID, componentID,optionName,value);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @param value
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 */
	@WebMethod
	public void applyConfigEntryString(int sessionID, int componentID, String optionName, String value) throws ClientNotKnownException, UnknownComponentException {
		applyConfigEntry(sessionID, componentID,optionName,value);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @param value
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 * @throws MalformedURLException
	 */
	@WebMethod
	public void applyConfigEntryURL(int sessionID, int componentID, String optionName, String value) throws ClientNotKnownException, UnknownComponentException, MalformedURLException {
		// URLs are passed as String and then converted
		URL url = new URL(value);
		applyConfigEntry(sessionID, componentID,optionName,url);
	}	
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @param value
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 */
	@WebMethod
	public void applyConfigEntryStringArray(int sessionID, int componentID, String optionName, String[] value) throws ClientNotKnownException, UnknownComponentException {
		Set<String> stringSet = new TreeSet<String>(Arrays.asList(value));
		applyConfigEntry(sessionID, componentID,optionName,stringSet);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @param value
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 */
	@WebMethod
	public void applyConfigEntryStringTupleList(int sessionID, int componentID, String optionName, String[] keys, String[] values) throws ClientNotKnownException, UnknownComponentException {
		List<StringTuple> tuples = new LinkedList<StringTuple>();
		for(int i=0; i<keys.length; i++) {
			StringTuple st = new StringTuple(keys[i],values[i]);
			tuples.add(st);
		}
//		Set<String> stringSet = new TreeSet<String>(Arrays.asList(value));
		applyConfigEntry(sessionID, componentID, optionName, tuples);
	}	
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @param value
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 */
	@WebMethod
	public void applyConfigEntryBoolean(int sessionID, int componentID, String optionName, Boolean value) throws ClientNotKnownException, UnknownComponentException	{
		applyConfigEntry(sessionID, componentID,optionName,value);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @param value
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 */
	private void applyConfigEntry(int sessionID, int componentID, String optionName, Object value) throws ClientNotKnownException, UnknownComponentException {
		ClientState state = getState(sessionID);
		AbstractComponent component = state.getComponent(componentID);
		cm.applyConfigEntry(component, optionName, value);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 * @throws ConfigOptionTypeException
	 */
	@WebMethod
	public String[] getConfigOptionValueStringArray(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, String[].class);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 * @throws ConfigOptionTypeException
	 */
	@WebMethod
	public String getConfigOptionValueString(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, String.class);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 * @throws ConfigOptionTypeException
	 */
	@WebMethod
	public String getConfigOptionValueURL(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		URL url = getConfigOptionValue(sessionID, componentID, optionName, URL.class);
		return url.toString();
	}	
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 * @throws ConfigOptionTypeException
	 */
	@WebMethod
	public Double getConfigOptionValueDouble(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, Double.class);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 * @throws ConfigOptionTypeException
	 */
	@WebMethod
	public Boolean getConfigOptionValueBoolean(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, Boolean.class);
	}
	
	/**
	 * 
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @return
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 * @throws ConfigOptionTypeException
	 */
	@WebMethod
	public Integer getConfigOptionValueInt(int sessionID, int componentID, String optionName) throws ClientNotKnownException, UnknownComponentException, ConfigOptionTypeException {
		return getConfigOptionValue(sessionID, componentID, optionName, Integer.class);
	}
	
	////////////////////////////////////
	// reasoning and querying methods //
	////////////////////////////////////
	
	@WebMethod
	public String[] getAtomicConcepts(int id) throws ClientNotKnownException {
		Set<NamedClass> atomicConcepts = getState(id).getReasonerComponent().getNamedClasses();
		return Datastructures.sortedSet2StringListConcepts(atomicConcepts);
	}
	
	@WebMethod
	public String getSubsumptionHierarchy(int id) throws ClientNotKnownException {
		return getState(id).getReasonerComponent().toString();
	}
	
	@WebMethod
	public String[] retrieval(int id, String conceptString) throws ClientNotKnownException, ParseException {
		ClientState state = getState(id);
		// call parser to parse concept
		Description concept = null;
		concept = KBParser.parseConcept(conceptString);
		Set<Individual> individuals = state.getReasonerComponent().getIndividuals(concept);
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
		Set<ObjectProperty> roles = state.getReasonerComponent().getObjectProperties();
		return Datastructures.sortedSet2StringListRoles(roles);
	}
	
	@WebMethod
	public String[] getInstances(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<Individual> individuals = state.getReasonerComponent().getIndividuals();
		return Datastructures.sortedSet2StringListIndividuals(individuals);
	}
	
	@WebMethod
	public String[] getIndividualsForARole(int id, String role) throws ClientNotKnownException {
		ClientState state = getState(id);
		Map<Individual,SortedSet<Individual>> m = state.getReasonerComponent().getPropertyMembers(new ObjectProperty(role));
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
		AbstractComponent component = state.getComponent(componentID);
		final SparqlKnowledgeSource ks=(SparqlKnowledgeSource)component;
		final int id=state.addQuery(ks.sparqlQuery(query));
		Thread sparqlThread = new Thread() {
			@Override
			public void run() {
				if (ks.isUseCache()){
					Cache cache=new Cache(ks.getCacheDir());
					cache.executeSparqlQuery(state.getQuery(id));
				}
				else{
					state.getQuery(id).send();
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
		AbstractComponent component = state.getComponent(componentID);
		SparqlKnowledgeSource ks=(SparqlKnowledgeSource)component;
		return ks.getSPARQLTasks().query(query);
		/*SparqlQuery sparql=ks.sparqlQuery(query);
		if (ks.isUseCache()){
			Cache cache=new Cache(ks.getCacheDir());
			return cache.executeSparqlQuery(sparql);
		}
		else return sparql.getJson();*/
	}
	
	/**
	 * Queries one of the standard endpoints defined in DL-Learner.
	 * @param predefinedEndpoint A string describing the endpoint e.g. DBpedia.
	 * @param query The SPARQL query.
	 * @param useCache Specify whether to use a cache for queries.
	 * @return The result of the SPARQL query in JSON format or null if the endpoint does not exist.
	 * @see SPARQLEndpoint#getEndpointByName;
	 */
	@WebMethod
	public String sparqlQueryPredefinedEndpoint(String predefinedEndpoint, String query, boolean useCache) {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointByName(predefinedEndpoint);
		SPARQLTasks st;
		if(useCache) {
			st = new SPARQLTasks(endpoint);
		} else {
			st = new SPARQLTasks(Cache.getDefaultCache(), endpoint);
		}
		return st.query(query);
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
	public String SparqlRetrieval(String conceptString,int limit) throws ParseException {
		// call parser to parse concept
		return SparqlQueryDescriptionConvertVisitor.getSparqlQuery(conceptString,limit, false, false);
	}
	
	@WebMethod
	public String getNaturalDescription(int id, String conceptString, String endpoint) throws ParseException, ClientNotKnownException {
		// call parser to parse concept
		ClientState state = getState(id);
		AbstractReasonerComponent service = state.getReasonerComponent();
		return NaturalLanguageDescriptionConvertVisitor.getNaturalLanguageDescription(conceptString, service);
	}
	
	@WebMethod
	public String[] getNegativeExamples(int sessionID, int componentID,String[] positives, int results, String namespace, String[] filterClasses) throws ClientNotKnownException
	{
		int sparqlResultSetLimit = 500;
		SortedSet<String> positiveSet = new TreeSet<String>(Arrays.asList(positives));
		SortedSet<String> filterSet = new TreeSet<String>(Arrays.asList(filterClasses));
		ClientState state = getState(sessionID);
		AbstractComponent component = state.getComponent(componentID);
		SparqlKnowledgeSource ks=(SparqlKnowledgeSource)component;
		SPARQLTasks task=ks.getSPARQLTasks();
		AutomaticNegativeExampleFinderSPARQL finder=new AutomaticNegativeExampleFinderSPARQL(positiveSet,task,filterSet);
		
		/*finder.makeNegativeExamplesFromNearbyClasses(positiveSet, sparqlResultSetLimit);
		SortedSet<String> negExamples=finder.getNegativeExamples(results);
		if (negExamples.isEmpty()){*/
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
		//}
		
		return negExamples.toArray(new String[negExamples.size()]);
	}
	
	/////////////////////////////
	// private utility methods //
	/////////////////////////////
	
	// returns session state or throws client not known exception
	private ClientState getState(int id) throws ClientNotKnownException {
		ClientState state = clients.get(id);
		if(state==null)
			throw new ClientNotKnownException(id);
		return state;
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
		AbstractComponent component = state.getComponent(componentID);
		return cm.getConfigOptionValue(component, optionName);
	}	
	
}
