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
package org.dllearner.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.utilities.Files;

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
public class ComponentManager {

	private ComponentPool pool = new ComponentPool();
	
	// these variables are valid for the complete lifetime of a DL-Learner session
	private static Collection<Class<? extends Component>> components;
	private static Collection<Class<? extends KnowledgeSource>> knowledgeSources;
	private static Collection<Class<? extends ReasonerComponent>> reasonerComponents;
	private static Collection<Class<? extends LearningProblem>> learningProblems;
	private static Collection<Class<? extends LearningAlgorithm>> learningAlgorithms;
	// you can either use the components.ini file or directly specify the classes to use
	private static String componentsFile = "lib/components.ini";
	private static String[] componentClasses = new String[]{}; 
	private static ComponentManager cm = new ComponentManager();	

	// list of all configuration options of all components
	private static Map<Class<? extends Component>, String> componentNames;
	private static Map<Class<? extends Component>, List<ConfigOption<?>>> componentOptions;
	private static Map<Class<? extends Component>, Map<String, ConfigOption<?>>> componentOptionsByName;
	private static Map<Class<? extends LearningAlgorithm>, Collection<Class<? extends LearningProblem>>> algorithmProblemsMapping;

	// list of default values of config options
//	private static Map<ConfigOption<?>,Object> configOptionDefaults;
	
	private Comparator<Class<?>> classComparator = new Comparator<Class<?>>() {

		public int compare(Class<?> c1, Class<?> c2) {
			return c1.getName().compareTo(c2.getName());
		}

	};

	@SuppressWarnings( { "unchecked" })
	private ComponentManager() {
		// read in components file
		List<String> componentsString;
		if(componentClasses.length > 0)
			componentsString = Arrays.asList(componentClasses);
		else
			componentsString = readComponentsFile();

		// component list
		components = new TreeSet<Class<? extends Component>>(classComparator);
		knowledgeSources = new TreeSet<Class<? extends KnowledgeSource>>(classComparator);
		reasonerComponents = new TreeSet<Class<? extends ReasonerComponent>>(classComparator);
		learningProblems = new TreeSet<Class<? extends LearningProblem>>(classComparator);
		learningAlgorithms = new TreeSet<Class<? extends LearningAlgorithm>>(classComparator);
		algorithmProblemsMapping = new TreeMap<Class<? extends LearningAlgorithm>, Collection<Class<? extends LearningProblem>>>(
				classComparator);

		// create classes from strings
		for (String componentString : componentsString) {
			try {
				Class<? extends Component> component = Class.forName(componentString).asSubclass(
						Component.class);
				components.add(component);

				if (KnowledgeSource.class.isAssignableFrom(component))
					knowledgeSources.add((Class<? extends KnowledgeSource>) component);
				else if (ReasonerComponent.class.isAssignableFrom(component))
					reasonerComponents.add((Class<? extends ReasonerComponent>) component);
				else if (LearningProblem.class.isAssignableFrom(component))
					learningProblems.add((Class<? extends LearningProblem>) component);
				else if (LearningAlgorithm.class.isAssignableFrom(component)) {
					Class<? extends LearningAlgorithm> learningAlgorithmClass = (Class<? extends LearningAlgorithm>) component;
					learningAlgorithms.add(learningAlgorithmClass);
					Collection<Class<? extends LearningProblem>> problems = (Collection<Class<? extends LearningProblem>>) invokeStaticMethod(
							learningAlgorithmClass, "supportedLearningProblems");
					algorithmProblemsMapping.put(learningAlgorithmClass, problems);
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		componentNames = new HashMap<Class<? extends Component>, String>();
		// read in all configuration options
		componentOptions = new HashMap<Class<? extends Component>, List<ConfigOption<?>>>();
		componentOptionsByName = new HashMap<Class<? extends Component>, Map<String, ConfigOption<?>>>();
//		configOptionDefaults = new HashMap<ConfigOption<?>,Object>();
		
		for (Class<? extends Component> component : components) {

			String name = (String) invokeStaticMethod(component, "getName");
			componentNames.put(component, name);
			
			// assign options to components
			List<ConfigOption<?>> options = (List<ConfigOption<?>>) invokeStaticMethod(component,
					"createConfigOptions");
			componentOptions.put(component, options);

			// make config options accessible by name
			Map<String, ConfigOption<?>> byName = new HashMap<String, ConfigOption<?>>();
			for (ConfigOption<?> option : options)
				byName.put(option.getName(), option);
			componentOptionsByName.put(component, byName);
			
		}

		// System.out.println(components);
		// System.out.println(learningProblems);
	}

	/**
	 * 
	 * @return The singleton <code>ComponentManager</code> instance.
	 */
	public static ComponentManager getInstance() {
		if(cm == null)
			cm = new ComponentManager();
		return cm;
	}

	public static void setComponentClasses(String[] componentClasses) {
		ComponentManager.componentClasses = componentClasses;
	}
	
	private static List<String> readComponentsFile() {
		List<String> componentStrings = new LinkedList<String>();

		try {
			FileInputStream fstream = new FileInputStream(componentsFile);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = br.readLine()) != null) {
				if (!(line.startsWith("#") || line.startsWith("//") || line.startsWith("%") || line
						.length() <= 1))
					componentStrings.add(line);
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
	 * @param component
	 * @param optionName
	 * @param value
	 */
	@SuppressWarnings( { "unchecked" })
	public <T> void applyConfigEntry(Component component, String optionName, T value) {
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
			} else
				System.out.println("Warning: undefined option " + optionName + " in component "
						+ component);
		} else
			System.out.println("Warning: unregistered component " + component);
	}

	/**
	 * Applies a config entry to a component. If the entry is not valid, the method
	 * prints an exception and returns false.
	 * @param <T> Type of the config option.
	 * @param component A component object.
	 * @param entry The configuration entry to set.
	 * @return True of the config entry could be applied succesfully, otherwise false.
	 */
	public <T> boolean applyConfigEntry(Component component, ConfigEntry<T> entry) {
		try {
			component.applyConfigEntry(entry);
			pool.addConfigEntry(component,entry,true);
			return true;
		} catch (InvalidConfigOptionValueException e) {
			pool.addConfigEntry(component,entry,false);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Factory method for creating a knowledge source.
	 * @param source A registered knowledge source component.
	 * @return An instance of the given knowledge source class.
	 */
	public <T extends KnowledgeSource> T knowledgeSource(Class<T> source) {
		if (!knowledgeSources.contains(source))
			System.err.println("Warning: knowledge source " + source
					+ " is not a registered knowledge source component.");

		T ks = invokeConstructor(source, new Class[] {}, new Object[] {});
		pool.registerComponent(ks);
		return ks;
	}

	public <T extends ReasonerComponent> T reasoner(Class<T> reasoner,
			KnowledgeSource source) {
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		sources.add(source);
		return reasoner(reasoner, sources);
	}

	public <T extends ReasonerComponent> T reasoner(Class<T> reasoner,
			Set<KnowledgeSource> sources) {
		if (!reasonerComponents.contains(reasoner))
			System.err.println("Warning: reasoner component " + reasoner
					+ " is not a registered reasoner component.");

		T rc = invokeConstructor(reasoner, new Class[] { Set.class },
				new Object[] { sources });
		pool.registerComponent(rc);
		return rc;
	}

	/**
	 * This method returns an instance of <code>ReasoningService</code>. The
	 * difference between <code>ReasoningService</code> and <code>ReasonerComponent</code>
	 * is that the former delegates all calls to the latter and collects statistics
	 * while doing this. This means that the reasoning service enables the
	 * collection of query information, while the <code>ReasonerComponent</code>
	 * implements the actual reasoning methods defined by the <code>Reasoner</code>
	 * interface.
	 * 
	 * @param reasoner A reasoner component.
	 * @return The reasoning service encapsulating the reasoner.
	 */
	public ReasoningService reasoningService(ReasonerComponent reasoner) {
		return new ReasoningService(reasoner);
	}
	
	public <T extends LearningProblem> T learningProblem(Class<T> lpClass, ReasoningService reasoner) {
		if (!learningProblems.contains(lpClass))
			System.err.println("Warning: learning problem " + lpClass
					+ " is not a registered learning problem component.");

		T lp = invokeConstructor(lpClass, new Class[] { ReasoningService.class },
				new Object[] { reasoner });
		pool.registerComponent(lp);
		return lp;
	}

	// automagically calls the right constructor for the given learning problem
	public <T extends LearningAlgorithm> T learningAlgorithm(Class<T> laClass, LearningProblem lp, ReasoningService rs) throws LearningProblemUnsupportedException {
		if (!learningAlgorithms.contains(laClass))
			System.err.println("Warning: learning algorithm " + laClass
					+ " is not a registered learning algorithm component.");

		// find the right constructor: use the one that is registered and
		// has the class of the learning problem as a subclass
		Class<? extends LearningProblem> constructorArgument = null;
		for (Class<? extends LearningProblem> problemClass : algorithmProblemsMapping.get(laClass)) {
			if (problemClass.isAssignableFrom(lp.getClass()))
				constructorArgument = problemClass;
		}

		if (constructorArgument == null) {
			throw new LearningProblemUnsupportedException(lp.getClass(), laClass, algorithmProblemsMapping.get(laClass));
//			System.err.println("Warning: No suitable constructor registered for algorithm "
//					+ laClass.getName() + " and problem " + lp.getClass().getName()
//					+ ". Registered constructors for " + laClass.getName() + ": "
//					+ algorithmProblemsMapping.get(laClass) + ".");
//			return null;
		}

		T la = invokeConstructor(laClass, new Class[] { constructorArgument, ReasoningService.class }, new Object[] { lp, rs });
		pool.registerComponent(la);
		return la;
	}

	/**
	 * The <code>ComponentManager</code> factory methods produce component
	 * instances, which can be freed using this method. Calling the factory
	 * methods without freeing components when they are not used anymore 
	 * can (in theory) cause memory problems.
	 * 
	 * @param component The component to free. 
	 */
	public void freeComponent(Component component) {
		pool.unregisterComponent(component);
	}
	
	public void freeAllComponents() {
		pool.clearComponents();
	}
	
	public <T> T getConfigOptionValue(Component component, ConfigOption<T> option) {
		T object = pool.getLastValidConfigValue(component, option);
		if(object==null)
			return option.getDefaultValue();
		else
			return object;
	}
	
	public Object getConfigOptionValue(Component component, String optionName) {
		ConfigOption<?> option = (ConfigOption<?>) componentOptionsByName.get(
				component.getClass()).get(optionName);
		return getConfigOptionValue(component, option);
	}
	
	public void writeConfigDocumentation(File file) {
		String doc = "";
		doc += "This file contains an automatically generated files of all components and their config options.\n\n";
		
		// go through all types of components and write down their documentation
		doc += "*********************\n";
		doc += "* Knowledge Sources *\n";
		doc += "*********************\n\n";
		for(Class<? extends Component> component : knowledgeSources)
			doc += getComponentConfigString(component);
		
		doc += "*************\n";
		doc += "* Reasoners *\n";
		doc += "*************\n\n";
		for(Class<? extends Component> component : reasonerComponents)
			doc += getComponentConfigString(component);
		
		doc += "*********************\n";
		doc += "* Learning Problems *\n";
		doc += "*********************\n\n";
		for(Class<? extends Component> component : learningProblems)
			doc += getComponentConfigString(component);
		
		doc += "***********************\n";
		doc += "* Learning Algorithms *\n";
		doc += "***********************\n\n";
		for(Class<? extends Component> component : learningAlgorithms)
			doc += getComponentConfigString(component);
		
		Files.createFile(file, doc);
	}
	
	private String getComponentConfigString(Class<? extends Component> component) {
		String componentDescription =  "component: " + invokeStaticMethod(component,"getName") + " (" + component.getName() + ")";
		String str = componentDescription + "\n";
		for(int i=0; i<componentDescription.length(); i++)
			str += "=";
		str += "\n\n";
		
		for(ConfigOption<?> option : componentOptions.get(component)) {
			str += option.toString() + "\n";
		}		
		return str;
	}
	
	private Object invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
		// unfortunately Java does not seem to offer a way to call
		// a static method given a class object directly, so we have
		// to use reflection
		try {
			Method method = clazz.getMethod(methodName);
			return method.invoke(null, args);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private <T> T invokeConstructor(Class<T> clazz, Class<?>[] argumentClasses,
			Object[] argumentObjects) {
		try {
			Constructor<T> constructor = clazz.getConstructor(argumentClasses);
			return constructor.newInstance(argumentObjects);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	public static List<ConfigOption<?>> getConfigOptions(Class<? extends Component> componentClass) {
		if (!components.contains(componentClass))
			System.err.println("Warning: component " + componentClass
					+ " is not a registered component. [ComponentManager.getConfigOptions]");
		return componentOptions.get(componentClass);
	}
	
	public ConfigOption<?> getConfigOption(Class<? extends Component> component, String name) {
		return componentOptionsByName.get(component).get(name);
	}
	
	public String getComponentName(Class<? extends Component> component) {
		return componentNames.get(component);
	}

	/**
	 * @return the components
	 */
	public List<Class<? extends Component>> getComponents() {
		return new LinkedList<Class<? extends Component>>(components);
	}

	/**
	 * @return the knowledgeSources
	 */
	public List<Class<? extends KnowledgeSource>> getKnowledgeSources() {
		return new LinkedList<Class<? extends KnowledgeSource>>(knowledgeSources);
	}

	/**
	 * @return the reasonerComponents
	 */
	public List<Class<? extends ReasonerComponent>> getReasonerComponents() {
		return new LinkedList<Class<? extends ReasonerComponent>>(reasonerComponents);
	}

	/**
	 * @return the learningProblems
	 */
	public List<Class<? extends LearningProblem>> getLearningProblems() {
		return new LinkedList<Class<? extends LearningProblem>>(learningProblems);
	}

	/**
	 * @return the learningAlgorithms
	 */
	public List<Class<? extends LearningAlgorithm>> getLearningAlgorithms() {
		return new LinkedList<Class<? extends LearningAlgorithm>>(learningAlgorithms);
	}

}
