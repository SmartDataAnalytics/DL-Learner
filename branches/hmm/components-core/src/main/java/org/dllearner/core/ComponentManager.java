/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.SimpleSubclassLearner;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.DisjointObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.SubDataPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.TransitiveObjectPropertyAxiomLearner;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.utilities.datastructures.Maps;

/**
 * Central manager class for DL-Learner. There are currently four types of
 * components in DL-Learner: knowledge sources, reasoners, learning problems,
 * and learning algorithms. For accessing these components you should create
 * instances and configure them using this class. The component manager is
 * implemented as a Singleton and will read the components file (containing a
 * list of all components) at startup. This allows interfaces (command line,
 * graphical, web service) to easily query the available components, set and get
 * their configuration options, and run the algorithm.
 * 
 * @author Jens Lehmann
 * 
 */
public final class ComponentManager {

	private static Logger logger = Logger
		.getLogger(ComponentManager.class);
	
	private ComponentPool pool = new ComponentPool();
	
	// these variables are valid for the complete lifetime of a DL-Learner session
	private static Collection<Class<? extends AbstractComponent>> components;
	private static Collection<Class<? extends AbstractKnowledgeSource>> knowledgeSources;
	private static Collection<Class<? extends AbstractReasonerComponent>> reasonerComponents;
	private static Collection<Class<? extends AbstractLearningProblem>> learningProblems;
	private static Collection<Class<? extends AbstractCELA>> learningAlgorithms;
	// you can either use the components.ini file or directly specify the classes to use
	@Deprecated
    private static String componentsFile = "org/dllearner/components.ini";
    private static List<String> componentClasses = new ArrayList<String>  ( Arrays.asList(new String[]{
            "org.dllearner.kb.OWLFile",
            "org.dllearner.kb.KBFile",
            "org.dllearner.kb.sparql.SparqlKnowledgeSource",
            "org.dllearner.kb.sparql.simple.SparqlSimpleExtractor",
            "org.dllearner.kb.OWLAPIOntology",
//            "org.dllearner.kb.SparqlEndpointKS",
//reasoners
            "org.dllearner.reasoning.OWLAPIReasoner",
            "org.dllearner.reasoning.fuzzydll.FuzzyOWLAPIReasoner",  // added by Josue
            "org.dllearner.reasoning.DIGReasoner",
            "org.dllearner.reasoning.FastRetrievalReasoner",
            "org.dllearner.reasoning.FastInstanceChecker",
            "org.dllearner.reasoning.ProtegeReasoner",
            "org.dllearner.reasoning.PelletReasoner",
//learning problems
            "org.dllearner.learningproblems.PosNegLPStandard",
            "org.dllearner.learningproblems.FuzzyPosNegLPStandard", // added by Josue
            "org.dllearner.learningproblems.PosNegLPStrict",
            "org.dllearner.learningproblems.PosOnlyLP",
            "org.dllearner.learningproblems.ClassLearningProblem",
//learning algorithms
            "org.dllearner.algorithms.RandomGuesser",
            "org.dllearner.algorithms.BruteForceLearner",
            "org.dllearner.algorithms.refinement.ROLearner",
            "org.dllearner.algorithms.ocel.OCEL",
            "org.dllearner.algorithms.gp.GP",
            "org.dllearner.algorithms.el.ELLearningAlgorithm",
            "org.dllearner.algorithms.el.ELLearningAlgorithmDisjunctive",
            "org.dllearner.algorithms.celoe.CELOE",
            "org.dllearner.algorithms.fuzzydll.FuzzyCELOE", //added by Josue
            "org.dllearner.algorithms.isle.ISLE"
     } ));

	private static ComponentManager cm = null;	

	// list of all configuration options of all components
	private static Map<Class<? extends AbstractComponent>, String> componentNames;
	private static Map<Class<? extends AbstractComponent>, List<ConfigOption<?>>> componentOptions;
	private static Map<Class<? extends AbstractComponent>, Map<String, ConfigOption<?>>> componentOptionsByName;
	private static Map<Class<? extends AbstractCELA>, Collection<Class<? extends AbstractLearningProblem>>> algorithmProblemsMapping;
	private static Map<Class<? extends AbstractLearningProblem>, Collection<Class<? extends AbstractCELA>>> problemAlgorithmsMapping;
	
	// list of default values of config options
//	private static Map<ConfigOption<?>,Object> configOptionDefaults;
	
	private Comparator<Class<?>> classComparator = new Comparator<Class<?>>() {

		public int compare(Class<?> c1, Class<?> c2) {
			return c1.getName().compareTo(c2.getName());
		}

	};

	@SuppressWarnings("unchecked")
	private ComponentManager() {
        
		// read in components file
        /*REMOVED THE BLOCK*/
		/*List<String> componentsString;
		if(componentClasses.length > 0) {
			componentsString = Arrays.asList(componentClasses);
		} else {
			componentsString = readComponentsFile();
		}*/
       //List<String> componentsString2 = Arrays.asList(componentClasses);

		// component list
		components = new TreeSet<Class<? extends AbstractComponent>>(classComparator);
		knowledgeSources = new TreeSet<Class<? extends AbstractKnowledgeSource>>(classComparator);
		reasonerComponents = new TreeSet<Class<? extends AbstractReasonerComponent>>(classComparator);
		learningProblems = new TreeSet<Class<? extends AbstractLearningProblem>>(classComparator);
		learningAlgorithms = new TreeSet<Class<? extends AbstractCELA>>(classComparator);
		algorithmProblemsMapping = new TreeMap<Class<? extends AbstractCELA>, Collection<Class<? extends AbstractLearningProblem>>>(
				classComparator);		

		// create classes from strings
		for (String componentString : componentClasses) {
			try {
				Class<? extends AbstractComponent> component = Class.forName(componentString).asSubclass(
						AbstractComponent.class);
				components.add(component);

				if (AbstractKnowledgeSource.class.isAssignableFrom(component)) {
					knowledgeSources.add((Class<? extends AbstractKnowledgeSource>) component);
				} else if (AbstractReasonerComponent.class.isAssignableFrom(component)) {
					reasonerComponents.add((Class<? extends AbstractReasonerComponent>) component);
				} else if (AbstractLearningProblem.class.isAssignableFrom(component)) {
					learningProblems.add((Class<? extends AbstractLearningProblem>) component);
				} else if (AbstractCELA.class.isAssignableFrom(component)) {
					Class<? extends AbstractCELA> learningAlgorithmClass = (Class<? extends AbstractCELA>) component;
					learningAlgorithms.add(learningAlgorithmClass);
					Collection<Class<? extends AbstractLearningProblem>> problems = (Collection<Class<? extends AbstractLearningProblem>>) invokeStaticMethod(
							learningAlgorithmClass, "supportedLearningProblems");
					algorithmProblemsMapping.put(learningAlgorithmClass, problems);
				}

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		problemAlgorithmsMapping = Maps.revertCollectionMap(algorithmProblemsMapping);

		componentNames = new HashMap<Class<? extends AbstractComponent>, String>();
		// read in all configuration options
		componentOptions = new HashMap<Class<? extends AbstractComponent>, List<ConfigOption<?>>>();
		componentOptionsByName = new HashMap<Class<? extends AbstractComponent>, Map<String, ConfigOption<?>>>();
//		configOptionDefaults = new HashMap<ConfigOption<?>,Object>();
				
		for (Class<? extends AbstractComponent> component : components) {

			String name = (String) invokeStaticMethod(component, "getName");
			componentNames.put(component, name);
			
			// assign options to components
			List<ConfigOption<?>> options = (List<ConfigOption<?>>) invokeStaticMethod(component,
					"createConfigOptions");
			componentOptions.put(component, options);

			// make config options accessible by name
			Map<String, ConfigOption<?>> byName = new HashMap<String, ConfigOption<?>>();
			for (ConfigOption<?> option : options) {
				byName.put(option.getName(), option);
			}
			componentOptionsByName.put(component, byName);
			
		}

	}

	/**
	 * Gets the singleton instance of <code>ComponentManager</code>.
	 * @return The singleton <code>ComponentManager</code> instance.
	 */
	public static ComponentManager getInstance() {
		if(cm == null) {
			cm = new ComponentManager();
		}
		return cm;
	}

	/**
	 * Set the classes, which can be used as components. By default,
	 * this is read from components.ini, but this method can be used
	 * to set the components programmatically. This method must be
	 * used before the first call to {@link #getInstance()}, otherwise
	 * it has no effect.
	 * 
	 * @param componentClasses A list of class names, e.g. 
	 * org.dllearner.refinement.ROLearner.
	 */
	public static void setComponentClasses(String[] componentClasses) {
		ComponentManager.componentClasses = new ArrayList<String> (Arrays.asList(componentClasses));
	}

    @Deprecated
	private static List<String> readComponentsFile() {
		List<String> componentStrings = new LinkedList<String>();

		try {
			 
			InputStream is = ComponentManager.class.getClassLoader().getResourceAsStream(componentsFile);
			DataInputStream in = new DataInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = br.readLine()) != null) {
				if (!(line.startsWith("#") || line.startsWith("//") || line.startsWith("%") || line
						.length() <= 1)) {
					componentStrings.add(line);
				}
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return componentStrings;
	}

	/**
	 * Convenience method for testing purposes. If you know that the type of the
	 * value is correct, it is preferable to create a ConfigEntry object and
	 * apply it to the component (no type checking necessary).
	 * 
	 * @param <T> Type of the config option (Integer, String etc.).
	 * @param component A component.
	 * @param optionName The name of the config option.
	 * @param value The value of the config option.
	 */
	@SuppressWarnings("unchecked")
	public <T> void applyConfigEntry(AbstractComponent component, String optionName, T value) {
		logger.trace(component);
		logger.trace(optionName);
		logger.trace(value);
		logger.trace(value.getClass());
		// first we look whether the component is registered
		if (components.contains(component.getClass())) {

			// look for a config option with the specified name
			ConfigOption<?> option = (ConfigOption<?>) componentOptionsByName.get(
					component.getClass()).get(optionName);
			if (option != null) {
				// check whether the given object has the correct type
				if (!option.checkType(value)) {
					System.out.println("Warning: value " + value + " is not valid for option "
							+ optionName + " in component " + component
							+ ". It does not have the correct type.");
					return;
				}

				// we have checked the type, hence it should now be safe to
				// typecast and
				// create a ConfigEntry object
				ConfigEntry<T> entry = null;
				try {	
					entry = new ConfigEntry<T>((ConfigOption<T>) option, value);
					component.applyConfigEntry(entry);
					pool.addConfigEntry(component, entry, true);
				} catch (InvalidConfigOptionValueException e) {
					pool.addConfigEntry(component, entry, false);
					System.out.println("Warning: value " + value + " is not valid for option "
							+ optionName + " in component " + component);
				}
			} else {
				logger.warn("Warning: undefined option " + optionName + " in component "
						+ component);
			}
		} else {
			logger.warn("Warning: unregistered component " + component);
		}
	}
	
	public ComponentPool getPool() {
		return pool;
	}

	/**
	 * Applies a config entry to a component. If the entry is not valid, the method
	 * prints an exception and returns false.
	 * @param <T> Type of the config option.
	 * @param component A component object.
	 * @param entry The configuration entry to set.
	 * @return True if the config entry could be applied succesfully, otherwise false.
	 */
	public <T> boolean applyConfigEntry(AbstractComponent component, ConfigEntry<T> entry) {
		try {
			component.applyConfigEntry(entry);
			pool.addConfigEntry(component, entry, true);
			return true;
		} catch (InvalidConfigOptionValueException e) {
			pool.addConfigEntry(component, entry, false);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Factory method for creating a knowledge source.
	 * 
	 * @param <T> The type of this method is a subclass of knowledge source.
	 * @param source A registered knowledge source component.
	 * @return An instance of the given knowledge source class.
	 */
	public <T extends AbstractKnowledgeSource> T knowledgeSource(Class<T> source) {
		if (!knowledgeSources.contains(source)) {
			logger.warn("Warning: knowledge source " + source
					+ " is not a registered knowledge source component.");
		}

		T ks = invokeConstructor(source, new Class[] {}, new Object[] {});
		pool.registerComponent(ks);
		return ks;
	}

	/**
	 * Factory method for creating a reasoner component from a single
	 * knowledge source. Example
	 * call: reasoner(OWLAPIReasoner.class, ks) where ks is a 
	 * knowledge source object.
	 * @see #reasoner(Class, Set)
	 * @param <T> The type of this method is a subclass of reasoner component.
	 * @param reasoner A class object, where the class is subclass of ReasonerComponent.
	 * @param source A knowledge source.
	 * @return A reasoner component.
	 */
	public <T extends AbstractReasonerComponent> T reasoner(Class<T> reasoner,
			AbstractKnowledgeSource source) {
		Set<AbstractKnowledgeSource> sources = new HashSet<AbstractKnowledgeSource>();
		sources.add(source);
		return reasoner(reasoner, sources);
	}

	/**
	 * Factory method for creating a reasoner component from a set of
	 * knowledge sources.
	 * @see #reasoner(Class, AbstractKnowledgeSource)
	 * @param <T> The type of this method is a subclass of reasoner component.
	 * @param reasoner A class object, where the class is subclass of ReasonerComponent.
	 * @param sources A set of knowledge sources.
	 * @return A reasoner component.
	 */
	public <T extends AbstractReasonerComponent> T reasoner(Class<T> reasoner,
			Set<AbstractKnowledgeSource> sources) {
		if (!reasonerComponents.contains(reasoner)) {
			System.err.println("Warning: reasoner component " + reasoner
					+ " is not a registered reasoner component.");
		}

		T rc = invokeConstructor(reasoner, new Class[] { Set.class },
				new Object[] { sources });
		pool.registerComponent(rc);
		return rc;
	}

	/**
	 * Factory method for creating a reasoner component from a set of
	 * knowledge sources.
	 * @see #reasoner(Class, AbstractKnowledgeSource)
	 * @param <T> The type of this method is a subclass of reasoner component.
	 * @param reasoner A class object, where the class is subclass of ReasonerComponent.
	 * @param sources A set of knowledge sources.
	 * @return A reasoner component.
	 */
	public <T extends AbstractReasonerComponent> T reasoner(Class<T> reasoner,
			AbstractKnowledgeSource ... sources) {
		Set<AbstractKnowledgeSource> s = new HashSet<AbstractKnowledgeSource>();
		for(AbstractKnowledgeSource source : sources) {
			s.add(source);
		}
		return reasoner(reasoner, s);
	}	
	
	/**
	 * This method returns an instance of <code>ReasonerComponent</code>. The
	 * difference between <code>ReasonerComponent</code> and <code>ReasonerComponent</code>
	 * is that the former delegates all calls to the latter and collects statistics
	 * while doing this. This means that the reasoning service enables the
	 * collection of query information, while the <code>ReasonerComponent</code>
	 * implements the actual reasoning methods defined by the <code>Reasoner</code>
	 * interface.
	 * 
	 * @param reasoner A reasoner component.
	 * @return The reasoning service encapsulating the reasoner.
	 */
//	public ReasonerComponent reasoningService(ReasonerComponent reasoner) {
//		return new ReasonerComponent(reasoner);
//	}
	
	/**
	 * Factory method for creating a learning problem component.
	 * @param <T> The type of this method is a subclass of learning problem.
	 * @param lpClass A class object, where the class is a subclass of learning problem.
	 * @param reasoner A reasoning service object.
	 * @return A learning problem component.
	 */
	public <T extends AbstractLearningProblem> T learningProblem(Class<T> lpClass, AbstractReasonerComponent reasoner) {
		if (!learningProblems.contains(lpClass)) {
			System.err.println("Warning: learning problem " + lpClass
					+ " is not a registered learning problem component.");
		}

		T lp = invokeConstructor(lpClass, new Class[] { AbstractReasonerComponent.class },
				new Object[] { reasoner });
		pool.registerComponent(lp);
		return lp;
	}

	/**
	 * Factory method for creating a learning algorithm, which 
	 * automagically calls the right constructor for the given problem.
	 * @param <T> The type of this method is a subclass of learning algorithm.
	 * @param laClass A class object, where the class is subclass of learning algorithm.
	 * @param lp A learning problem, which the algorithm should try to solve.
	 * @param rs A reasoning service for querying the background knowledge of this learning problem.
	 * @return A learning algorithm component.
	 * @throws LearningProblemUnsupportedException Thrown when the learning problem and
	 * the learning algorithm are not compatible.
	 */
	public <T extends AbstractCELA> T learningAlgorithm(Class<T> laClass, AbstractLearningProblem lp, AbstractReasonerComponent rs) throws LearningProblemUnsupportedException {
		if (!learningAlgorithms.contains(laClass)) {
			System.err.println("Warning: learning algorithm " + laClass
					+ " is not a registered learning algorithm component.");
		}

		// find the right constructor: use the one that is registered and
		// has the class of the learning problem as a subclass
		Class<? extends AbstractLearningProblem> constructorArgument = null;
		for (Class<? extends AbstractLearningProblem> problemClass : algorithmProblemsMapping.get(laClass)) {
			if (problemClass.isAssignableFrom(lp.getClass())) {
				constructorArgument = problemClass;
			}
		}
		
		if (constructorArgument == null) {
			throw new LearningProblemUnsupportedException(lp.getClass(), laClass, algorithmProblemsMapping.get(laClass));
//			System.err.println("Warning: No suitable constructor registered for algorithm "
//					+ laClass.getName() + " and problem " + lp.getClass().getName()
//					+ ". Registered constructors for " + laClass.getName() + ": "
//					+ algorithmProblemsMapping.get(laClass) + ".");
//			return null;
		}

		T la = invokeConstructor(laClass, new Class[] { constructorArgument, AbstractReasonerComponent.class }, new Object[] { lp, rs });
		pool.registerComponent(la);
		return la;
	}
//
//	public <T extends LearningAlgorithm> T learningAlgorithm(Class<T> laClass, KnowledgeSource ks) {
//		T la = invokeConstructor(laClass, new Class[] { KnowledgeSource.class }, new Object[] { ks });
//		return la;
//	}	
	
	/**
	 * The <code>ComponentManager</code> factory methods produce component
	 * instances, which can be freed using this method. Calling the factory
	 * methods without freeing components when they are not used anymore 
	 * can (in theory) cause memory problems.
	 * 
	 * @param component The component to free. 
	 */
	public void freeComponent(AbstractComponent component) {
		pool.unregisterComponent(component);
	}
	
	/**
	 * Frees all references to components created by <code>ComponentManager</code>.
	 * @see #freeComponent(AbstractComponent)
	 */
	public synchronized void  freeAllComponents() {
		pool.clearComponents();
	}
	
	/**
	 * Gets the value of a config option of the specified component.
	 * This is done by first checking, which value the given option
	 * was set to using {@link #applyConfigEntry(AbstractComponent, ConfigEntry)}.
	 * If the value has not been changed, the default value for this
	 * option is returned. Note, that this method will not work properly
	 * if the component options are changed internally surpassing the
	 * component manager (which is discouraged). 
	 * 
	 * @param <T> The type of the config option, e.g. String, boolean, integer.
	 * @param component The component, which has the specified option.
	 * @param option The option for which we want to know its value.
	 * @return The value of the specified option in the specified component.
	 */
	public <T> T getConfigOptionValue(AbstractComponent component, ConfigOption<T> option) {
		T object = pool.getLastValidConfigValue(component, option);
		if(object==null) {
			return option.getDefaultValue();
		} else {
			return object;
		}
	}
	
	/**
	 * Works as {@link #getConfigOptionValue(AbstractComponent, ConfigOption)},
	 * but using the name of the option instead of a <code>ConfigOption</code>
	 * object.
	 * @see #getConfigOptionValue(AbstractComponent, ConfigOption)
	 * @param component A component.
	 * @param optionName A valid option name for this component.
	 * @return The value of the specified option in the specified component.
	 */
	public Object getConfigOptionValue(AbstractComponent component, String optionName) {
		ConfigOption<?> option = (ConfigOption<?>) componentOptionsByName.get(
				component.getClass()).get(optionName);
		return getConfigOptionValue(component, option);
	}
	
	// convenience method for invoking a static method;
	// used as a central point for exception handling for Java reflection
	// static method calls	
	private static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
		// unfortunately Java does not seem to offer a way to call
		// a static method given a class object directly, so we have
		// to use reflection
		try {
			Method method = clazz.getMethod(methodName);
			return method.invoke(null, args);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	// convenience method for invoking a constructor;
	// used as a central point for exception handling for Java reflection
	// constructor calls
	private <T> T invokeConstructor(Class<T> clazz, Class<?>[] argumentClasses,
			Object[] argumentObjects) {
		try {
			Constructor<T> constructor = clazz.getConstructor(argumentClasses);
			return constructor.newInstance(argumentObjects);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns the available options of the specified component.
	 * @param componentClass The class object of a component.
	 * @return A list of available configuration options of the specified component.
	 */
	public static List<ConfigOption<?>> getConfigOptions(Class<? extends AbstractComponent> componentClass) {
		if (!components.contains(componentClass)) {
			System.err.println("Warning: component " + componentClass
					+ " is not a registered component. [ComponentManager.getConfigOptions]");
		}
		return componentOptions.get(componentClass);
	}
	
	/**
	 * Returns a <code>ConfigOption</code> object given a component and
	 * the option name.
	 * @param component A component class object.
	 * @param name A valid configuration option name for the component.
	 * @return A <code>ConfigOption</code> object for the specified component class and option name.
	 */
	public ConfigOption<?> getConfigOption(Class<? extends AbstractComponent> component, String name) {
		return componentOptionsByName.get(component).get(name);
	}
	
	/**
	 * Returns the name of a component.
	 * @param component A component class object.
	 * @return The name of the component.
	 */
	public String getComponentName(Class<? extends AbstractComponent> component) {
		return componentNames.get(component);
	}

	/**
	 * Returns a list of all available components in this instance
	 * of <code>ComponentManager</code>.
	 * @return the components A list of component classes available in this
	 * instance of <code>ComponentManager</code>.
	 */
	public List<Class<? extends AbstractComponent>> getComponents() {
		return new LinkedList<Class<? extends AbstractComponent>>(components);
	}

	/**
	 * Returns a list of all available knowledge sources in this instance
	 * of <code>ComponentManager</code>.
	 * @return the components A list of knowledge source component classes available in this
	 * instance of <code>ComponentManager</code>.
	 */
	public List<Class<? extends AbstractKnowledgeSource>> getKnowledgeSources() {
		return new LinkedList<Class<? extends AbstractKnowledgeSource>>(knowledgeSources);
	}

	/**
	 * Returns a list of all available reasoners in this instance
	 * of <code>ComponentManager</code>.
	 * @return the components A list of reasoner component classes available in this
	 * instance of <code>ComponentManager</code>.
	 */	
	public List<Class<? extends AbstractReasonerComponent>> getReasonerComponents() {
		return new LinkedList<Class<? extends AbstractReasonerComponent>>(reasonerComponents);
	}

	/**
	 * Returns a list of all available learning problems in this instance
	 * of <code>ComponentManager</code>.
	 * @return the components A list of learning problem classes available in this
	 * instance of <code>ComponentManager</code>.
	 */		
	public List<Class<? extends AbstractLearningProblem>> getLearningProblems() {
		return new LinkedList<Class<? extends AbstractLearningProblem>>(learningProblems);
	}

	/**
	 * Returns the set of learning algorithms, which support the given learning problem type.
	 * @param learningProblem A learning problem type.
	 * @return The set of learning algorithms applicable for this learning problem.
	 */
	public List<Class<? extends AbstractCELA>> getApplicableLearningAlgorithms(Class<? extends AbstractLearningProblem> learningProblem) {
		List<Class<? extends AbstractCELA>> algorithms = new LinkedList<Class<? extends AbstractCELA>>();
		for(Entry<Class<? extends AbstractLearningProblem>,Collection<Class<? extends AbstractCELA>>> entry : problemAlgorithmsMapping.entrySet()) {
			Class<? extends AbstractLearningProblem> prob = entry.getKey();
			if(prob.isAssignableFrom(learningProblem)) {
				algorithms.addAll(entry.getValue());
			}
		}
//		System.out.println(learningProblem + ": " + algorithms);
		return algorithms;
	}
	
	/**
	 * Returns a list of all available learning algorithms in this instance
	 * of <code>ComponentManager</code>.
	 * @return the components A list of learning algorithm classes available in this
	 * instance of <code>ComponentManager</code>.
	 */
	public List<Class<? extends AbstractCELA>> getLearningAlgorithms() {
		return new LinkedList<Class<? extends AbstractCELA>>(learningAlgorithms);
	}
	
	
	/**
	 * Retuns a list of all instanciated and registered Components 
	 * @return Currently active components.
	 */
	public List<AbstractComponent> getLiveComponents(){
		return pool.getComponents();
	}
	
	/**
	 *  Retuns a list of all instanciated and registered LearningAlgorithm 
	 * @return Currently active learning algorithms.
	 */
	public List<AbstractCELA> getLiveLearningAlgorithms(){
		List<AbstractCELA> list = new ArrayList<AbstractCELA>();
		for (AbstractComponent component : cm.getLiveComponents()) {
			if(component instanceof AbstractCELA){
				list.add((AbstractCELA) component);
			}
			
		}
		return list;
	}
	
	/**
	 *  Retuns a list of all instanciated and registered KnowledgeSource 
	 * @return Currently active knowledge sources.
	 */
	public List<AbstractKnowledgeSource> getLiveKnowledgeSources(){
		List<AbstractKnowledgeSource> list = new ArrayList<AbstractKnowledgeSource>();
		for (AbstractComponent component : cm.getLiveComponents()) {
			if(component instanceof AbstractKnowledgeSource){
				list.add((AbstractKnowledgeSource) component);
			}
			
		}
		return list;
	}

}
