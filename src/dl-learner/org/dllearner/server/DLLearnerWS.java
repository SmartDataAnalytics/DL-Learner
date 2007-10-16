/**
 * Copyright (C) 2007, Jens Lehmann
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

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.utilities.Datastructures;

/**
 * DL-Learner web service interface.
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 *
 */
@WebService(name = "DLLearnerWebService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DLLearnerWS {

	private Map<Integer, ClientState> clients = new TreeMap<Integer,ClientState>();
	private Random rand=new Random();
	private static ComponentManager cm = ComponentManager.getInstance();
	
	// defines the components, which are accessible for the web service
	private static Map<String,Class<? extends KnowledgeSource>> knowledgeSourceMapping = new TreeMap<String,Class<? extends KnowledgeSource>>();
	private static Map<String,Class<? extends ReasonerComponent>> reasonerMapping = new TreeMap<String,Class<? extends ReasonerComponent>>();
	private static Map<String,Class<? extends LearningProblem>> learningProblemMapping = new TreeMap<String,Class<? extends LearningProblem>>();
	private static Map<String,Class<? extends LearningAlgorithm>> learningAlgorithmMapping = new TreeMap<String,Class<? extends LearningAlgorithm>>();
	
	public DLLearnerWS() {
		knowledgeSourceMapping.put("owlfile", OWLFile.class);
		knowledgeSourceMapping.put("sparql", SparqlEndpoint.class);
		reasonerMapping.put("dig", DIGReasoner.class);
		learningProblemMapping.put("posNegDefinition", PosNegDefinitionLP.class);
		learningProblemMapping.put("posNegInclusion", PosNegInclusionLP.class);
		learningAlgorithmMapping.put("refinement", ROLearner.class);
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
	@SuppressWarnings({"unused"})
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
	
	/**
	 * Adds a knowledge source.
	 * 
	 * @return An identifier for the component.
	 */
	@WebMethod
	public int addKnowledgeSource(int id, String component, String url) throws ClientNotKnownException, UnknownComponentException {
		ClientState state = getState(id);
		Class<? extends KnowledgeSource> ksClass = knowledgeSourceMapping.get(component);
		if(ksClass == null)
			throw new UnknownComponentException(component);
		KnowledgeSource ks = cm.knowledgeSource(ksClass);
		cm.applyConfigEntry(ks, "url", url);
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
	public int setLearningAlgorithm(int id, String component) throws ClientNotKnownException, UnknownComponentException {
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
	 */
	@WebMethod
	public void init(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		for(KnowledgeSource ks : state.getKnowledgeSources())
			ks.init();
		state.getReasonerComponent().init();
		state.getLearningProblem().init();
		state.getLearningAlgorithm().init();
	}
	
	/**
	 * Starts the learning algorithm and returns the best concept found. This
	 * method will block until learning is completed.
	 * 
	 * @param id Session ID.
	 * @return The best solution found.
	 * @throws ClientNotKnownException
	 */
	@WebMethod
	public String learn(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		state.getLearningAlgorithm().start();
		return state.getLearningAlgorithm().getBestSolution().toString();
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
				state.setAlgorithmRunning(true);
				state.getLearningAlgorithm().start();
				state.setAlgorithmRunning(false);
			}
		};
		learningThread.start();
	}
	
	@WebMethod
	public String getCurrentlyBestConcept(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		return state.getLearningAlgorithm().getBestSolution().toString();
	}
	
	@WebMethod
	public boolean isAlgorithmRunning(int id) throws ClientNotKnownException {
		return getState(id).isAlgorithmRunning();
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
		Set<AtomicConcept> atomicConcepts = getState(id).getReasoningService().getAtomicConcepts();
		return Datastructures.sortedSet2StringListConcepts(atomicConcepts);
	}
	
	@WebMethod
	public String getSubsumptionHierarchy(int id) throws ClientNotKnownException {
		return getState(id).getReasoningService().toString();
	}
	
	@WebMethod
	public String[] retrieval(int id, String conceptString) throws ClientNotKnownException {
		ClientState state = getState(id);
		// call parser to parse concept
		Concept concept = null;
		try {
			concept = KBParser.parseConcept(conceptString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<Individual> individuals = state.getReasoningService().retrieval(concept);
		return Datastructures.sortedSet2StringListIndividuals(individuals);
	}
	
	@WebMethod
	public String[] getAtomicRoles(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<AtomicRole> roles = state.getReasoningService().getAtomicRoles();
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
		Map<Individual,SortedSet<Individual>> m = state.getReasoningService().getRoleMembers(new AtomicRole(role));
		Set<Individual> individuals = m.keySet();
		return Datastructures.sortedSet2StringListIndividuals(individuals);
	}
}
