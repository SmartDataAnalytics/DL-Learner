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

import org.apache.log4j.Logger;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.*;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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

	private Map<Integer, ClientState> clients = new TreeMap<>();
	private Random rand=new Random();
	private static AnnComponentManager cm = AnnComponentManager.getInstance();

	/**
	 * Conversion between different data structures.
	 * 
	 * @author Jens Lehmann
	 * @author Sebastian Hellmann
	 *
	 */
	public static class Datastructures {

		public static boolean strToBool(String str) {
			switch (str) {
				case "true":
					return true;
				case "false":
					return false;
				default:
					throw new Error("Cannot convert to boolean.");
			}
		}

		/**
		 * easy conversion
		 * 
		 * @param s
		 */
		public static String[] setToArray(Set<String> s) {
			if(s==null)return null;
			String[] ret=new String[s.size()];
			int i=0;
			for (String value : s) {
				ret[i] = value;
				i++;

			}
			return ret;

		}

		public static String[] sortedSet2StringListIndividuals(Set<OWLIndividual> individuals){

			String[] ret=new String[individuals.size()];
			Iterator<OWLIndividual> i=individuals.iterator();
			int a=0;
			while (i.hasNext()){
				ret[a++]=i.next().toStringID();
			}
			Arrays.sort(ret);
			return ret;
		}

		public static String[] sortedSet2StringListRoles(Set<OWLObjectProperty> s){

			String[] ret=new String[s.size()];
			Iterator<OWLObjectProperty> i=s.iterator();
			int a=0;
			while (i.hasNext()){
				ret[a++]=i.next().toStringID();
			}
			Arrays.sort(ret);
			return ret;
		}

		public static String[] sortedSet2StringListConcepts(Set<OWLClass> s){

			String[] ret=new String[s.size()];
			Iterator<OWLClass> i=s.iterator();
			int a=0;
			while (i.hasNext()){
				ret[a++]=i.next().toStringID();
			}
			Arrays.sort(ret);
			return ret;
		}

	}
//	/**
//	 * Returns the DL-Learner version this web service is based on.
//	 * @return DL-Learner-Build.
//	 */
//	@WebMethod
//	public String getBuild() {
//		return Info.build;
//	}

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
		Set<String> components = cm.getComponentStrings();
		return components.toArray(new String[components.size()]);
	}

	/**
	 * Gets a list of all DL-Learner knowledge source components accessible via this web service.
	 * @return All knowledge source components accessible via this web service.
	 */
	@WebMethod
	public String[] getKnowledgeSources() {
		Set<String> knowledgeSources = cm.getComponentStringsOfType(KnowledgeSource.class);
		return knowledgeSources.toArray(new String[knowledgeSources.size()]);
	}

	/**
	 * Gets a list of all DL-Learner reasoner components accessible via this web service.
	 * @return All reasoner components accessible via this web service.
	 */
	@WebMethod
	public String[] getReasoners() {
		Set<String> reasoners = cm.getComponentStringsOfType(AbstractReasonerComponent.class);
		return reasoners.toArray(new String[reasoners.size()]);
	}

	/**
	 * Gets a list of all DL-Learner learning problem components accessible via this web service.
	 * @return All learning problem components accessible via this web service.
	 */
	@WebMethod
	public String[] getLearningProblems() {
		Set<String> learningProblems = cm.getComponentStringsOfType(AbstractLearningProblem.class);
		return learningProblems.toArray(new String[learningProblems.size()]);
	}

	/**
	 * Gets a list of all DL-Learner learning algorithm components accessible via this web service.
	 * @return All learning algorithm components accessible via this web service.
	 */
	@WebMethod
	public String[] getLearningAlgorithms() {
		Set<String> learningAlgorithms = cm.getComponentStringsOfType(AbstractCELA.class);
		return learningAlgorithms.toArray(new String[learningAlgorithms.size()]);
	}

	/**
	 * Gets the configuration options supported by the component. This allows e.g. to
	 * automatically build user interfaces for configuring components.
	 * @param component Name of the component.
	 * @param allInfo Whether or not complete information is desired (including option description, required, default value, example value).
	 * @return A list of configuration options supported by the component.
	 * @throws UnknownComponentException Thrown if component is not known (see {@link #getComponents()}).
	 */
	@WebMethod
	public String[] getConfigOptions(String component, boolean allInfo) {
		Class<? extends Component> componentClass = cm.getComponentClass(component);
		Set<Field> options = AnnComponentManager.getConfigOptions(componentClass);
		String[] optionsString = new String[options.size()];
		int i = 0;
		for(Field f : options) {
			ConfigOption option = f.getAnnotation(ConfigOption.class);
			optionsString[i] = AnnComponentManager.getName(f);
			if(allInfo) {
				optionsString[i] += "#" + option.description();
				optionsString[i] += "#" + option.required();
				optionsString[i] += "#" + option.defaultValue();
				optionsString[i] += "#" + option.exampleValue();
			}
			i++;
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
		logger.info("Adding knowledge source " + component + " with URL parameter " + url + "...");
		ClientState state = getState(id);
		Class<? extends AbstractKnowledgeSource> ksClass = (Class<? extends AbstractKnowledgeSource>) cm.getComponentClass(component);
		if(ksClass == null)
			throw new UnknownComponentException(component);
		AbstractKnowledgeSource ks = null;
		try {
			ks = ksClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		if(ks instanceof OWLFile) {
			((OWLFile) ks).setUrl(new URL(url));
		}
		logger.info("...done.");
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
		logger.info("Setting reasoner " + component + "...");
		ClientState state = getState(id);
		Class<? extends AbstractReasonerComponent> rcClass = (Class<? extends AbstractReasonerComponent>) cm.getComponentClass(component);
		if(rcClass == null)
			throw new UnknownComponentException(component);

		AbstractReasonerComponent rc = null;
		try {
			rc = rcClass.getConstructor(Set.class).newInstance(state.getKnowledgeSources());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		logger.info("...done.");
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
		logger.info("Setting learning problem " + component + "...");
		ClientState state = getState(id);
		Class<? extends AbstractClassExpressionLearningProblem> lpClass = (Class<? extends AbstractClassExpressionLearningProblem>) cm.getComponentClass(component);
		if(lpClass == null)
			throw new UnknownComponentException(component);

		AbstractClassExpressionLearningProblem lp = null;
		try {
			lp = lpClass.getConstructor(AbstractReasonerComponent.class).newInstance(state.getReasonerComponent());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		logger.info("...done.");
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
	public int setLearningAlgorithm(int id, String component) throws ClientNotKnownException, UnknownComponentException {
		logger.info("Setting learning algorithm " + component + "...");
		ClientState state = getState(id);
		Class<? extends AbstractCELA> laClass = (Class<? extends AbstractCELA>) cm.getComponentClass(component);
		if(laClass == null)
			throw new UnknownComponentException(component);

		AbstractCELA la = null;
		try {
			la = laClass.getConstructor(AbstractClassExpressionLearningProblem.class, AbstractReasonerComponent.class)
					.newInstance(state.getLearningProblem(), state.getReasonerComponent());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		logger.info("...done.");
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
		logger.info("Initializing knowledge sources...");
		for(AbstractKnowledgeSource ks : state.getKnowledgeSources())
			ks.init();
		logger.info("Initializing reasoner...");
		state.getReasonerComponent().init();
		logger.info("Initializing learning problem...");
		state.getLearningProblem().init();
		logger.info("Initializing learning algorithm...");
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
	public void init(int id, int componentID) throws ClientNotKnownException, ComponentInitException {
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
		OWLClassExpression solution = state.getLearningAlgorithm().getCurrentlyBestDescription();
		switch (format) {
			case "manchester":
				return OWLAPIRenderers.toManchesterOWLSyntax(solution);
			case "kb":
				return OWLAPIRenderers.toManchesterOWLSyntax(solution);
			default:
				return solution.toString();
		}
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
		NavigableSet<? extends EvaluatedDescription> descriptions = state.getLearningAlgorithm()
				.getCurrentlyBestEvaluatedDescriptions();
		String json = "{";
		int count = 1;
		for (EvaluatedDescription description : descriptions.descendingSet()) {
			if (count > 1)
				json += ",\"solution" + count + "\" : " + description.asJSON();
			else
				json += "\"solution" + count + "\" : " + description.asJSON();
			count++;
		}
		json += "}";
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
		List<OWLClassExpression> bestConcepts = state.getLearningAlgorithm().getCurrentlyBestDescriptions(nrOfConcepts);
		List<String> conc= new LinkedList<>();
		Iterator<OWLClassExpression> iter=bestConcepts.iterator();
		while (iter.hasNext())
			switch (format) {
				case "manchester":
					conc.add(OWLAPIRenderers.toManchesterOWLSyntax(iter.next()));
					break;
				case "kb":
					conc.add(OWLAPIRenderers.toManchesterOWLSyntax(iter.next()));
					break;
				default:
					conc.add(iter.next().toString());
					break;
			}
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
	private String currentlyBestEvaluatedDescriptions(int id, int nrOfDescriptions, double accuracyThreshold,
			boolean filterNonMinimalDescriptions) throws ClientNotKnownException {
		ClientState state = getState(id);
		List<? extends EvaluatedDescription> descriptions;
		if (accuracyThreshold != -1) {
			descriptions = state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions,
					accuracyThreshold, filterNonMinimalDescriptions);
		} else {
			descriptions = state.getLearningAlgorithm().getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions);
		}
		String json = "{";
		System.out.println(json);
		int count = 1;
		for (EvaluatedDescription description : descriptions) {
			if (count > 1)
				json += ",\"solution" + count + "\" : " + description.asJSON();
			else
				json += "\"solution" + count + "\" : " + description.asJSON();
			count++;
		}
		json += "}";
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
		Set<String> posExamples = new TreeSet<>(Arrays.asList(positiveExamples));
		SortedSet<OWLIndividual> inds = new TreeSet<>();
		for (String ex : posExamples) {
			inds.add(new OWLNamedIndividualImpl(IRI.create(ex)));
		}
		if (state.getLearningProblem() instanceof PosOnlyLP) {
			((PosOnlyLP)state.getLearningProblem()).setPositiveExamples(inds);
		} else {
			((PosNegLP)state.getLearningProblem()).setPositiveExamples(inds);
		}
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
		Set<String> negExamples = new TreeSet<>(Arrays.asList(negativeExamples));
		Set<OWLIndividual> inds = new HashSet<>();
		for (String ex : negExamples) {
			inds.add(new OWLNamedIndividualImpl(IRI.create(ex)));
		}
		((PosNegLP)state.getLearningProblem()).setNegativeExamples(inds);
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
		Set<String> stringSet = new TreeSet<>(Arrays.asList(value));
		applyConfigEntry(sessionID, componentID,optionName,stringSet);
	}

	/**
	 *
	 * @param sessionID The session ID.
	 * @param componentID The componentID.
	 * @param optionName The name of the configuration option.
	 * @param keys
	 * @param values
	 * @throws ClientNotKnownException Thrown if client (session ID) is not known.
	 * @throws UnknownComponentException
	 */
	@WebMethod
	public void applyConfigEntryStringTupleList(int sessionID, int componentID, String optionName, String[] keys, String[] values) throws ClientNotKnownException, UnknownComponentException {
		List<StringTuple> tuples = new LinkedList<>();
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
	private void applyConfigEntry(int sessionID, int componentID, String optionName, Object value) throws ClientNotKnownException {
		ClientState state = getState(sessionID);
		AbstractComponent component = state.getComponent(componentID);
		System.out.println("Config option->" + component + "::" + optionName + "=" + value);
		try {
			Field field = component.getClass().getDeclaredField(optionName);
			field.setAccessible(true);
			if(optionName.equals("classToDescribe")) {
				value = new OWLClassImpl(IRI.create((URL)value));
			} else if(optionName.equals("ignoredConcepts")) {
				Set<OWLClass> ignoredConcepts = new HashSet<>();
				for (String iri : (TreeSet<String>)value) {
					ignoredConcepts.add(new OWLClassImpl(IRI.create(iri)));
				}
				value = ignoredConcepts;
			}
			field.set(component, value);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
//		try {
//			component.getClass().getMethod(optionName, value.getClass()).invoke(component, value);
//		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
//				| SecurityException e) {
//			e.printStackTrace();
//		}
//		cm.applyConfigEntry(component, optionName, value);
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
		Set<OWLClass> atomicConcepts = getState(id).getReasonerComponent().getClasses();
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
		OWLClassExpression concept = KBParser.parseConcept(conceptString);
		Set<OWLIndividual> individuals = state.getReasonerComponent().getIndividuals(concept);
		return Datastructures.sortedSet2StringListIndividuals(individuals);
	}

	@WebMethod
	public int getConceptLength(String conceptString) throws ParseException {
		// call parser to parse concept
		return OWLClassExpressionUtils.getLength(KBParser.parseConcept(conceptString));
	}

	@WebMethod
	public String[] getAtomicRoles(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<OWLObjectProperty> roles = state.getReasonerComponent().getObjectProperties();
		return Datastructures.sortedSet2StringListRoles(roles);
	}

	@WebMethod
	public String[] getInstances(int id) throws ClientNotKnownException {
		ClientState state = getState(id);
		Set<OWLIndividual> individuals = state.getReasonerComponent().getIndividuals();
		return Datastructures.sortedSet2StringListIndividuals(individuals);
	}

	@WebMethod
	public String[] getIndividualsForARole(int id, String role) throws ClientNotKnownException {
		ClientState state = getState(id);
		Map<OWLIndividual,SortedSet<OWLIndividual>> m = state.getReasonerComponent().getPropertyMembers(
				new OWLObjectPropertyImpl(IRI.create(role)));
		Set<OWLIndividual> individuals = m.keySet();
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
	 * @see SparqlEndpoint#getEndpointByName(String)
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
		List<OWLClassExpression> bestConcepts = state.getLearningAlgorithm().getCurrentlyBestDescriptions(nrOfConcepts);
		Iterator<OWLClassExpression> iter = bestConcepts.iterator();
		int[] depth = new int[bestConcepts.size()];
		int i = 0;
		while (iter.hasNext()) {
			depth[i] = OWLClassExpressionUtils.getDepth(iter.next());
			i++;
		}
		return depth;
	}

	@WebMethod
	public int[] getConceptArity(int id, int nrOfConcepts) throws ClientNotKnownException {
		ClientState state = getState(id);
		List<OWLClassExpression> bestConcepts = state.getLearningAlgorithm().getCurrentlyBestDescriptions(nrOfConcepts);
		Iterator<OWLClassExpression> iter = bestConcepts.iterator();
		int[] arity = new int[bestConcepts.size()];
		int i = 0;
		while (iter.hasNext()) {
			arity[i] = OWLClassExpressionUtils.getArity(iter.next());
			i++;
		}
		return arity;
	}

	@WebMethod
	public String SparqlRetrieval(String conceptString,int limit) {
		// call parser to parse concept
//		return SparqlQueryDescriptionConvertVisitor.getSparqlQuery(conceptString,limit, false, false);
		// TODO Refactoring replace
		return null;
	}

	@WebMethod
	public String[] getNegativeExamples(int sessionID, int componentID,String[] positives, int results, String namespace, String[] filterClasses) throws ClientNotKnownException
	{
		int sparqlResultSetLimit = 500;
		SortedSet<String> positiveSet = new TreeSet<>(Arrays.asList(positives));
		SortedSet<String> filterSet = new TreeSet<>(Arrays.asList(filterClasses));
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

	private Object getConfigOptionValue(int sessionID, int componentID, String optionName) throws ClientNotKnownException {
		ClientState state = getState(sessionID);
		AbstractComponent component = state.getComponent(componentID);
		return "";//cm.getConfigOptionValue(component, optionName);
	}

}
