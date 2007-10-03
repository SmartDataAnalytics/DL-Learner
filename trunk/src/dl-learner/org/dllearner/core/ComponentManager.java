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

	// these variables are valid for the complete lifetime of DL-Learner
	private static String componentsFile = "lib/components.ini";
	private static ComponentManager cm = new ComponentManager();
	private static Set<Class<? extends Component>> components;
	private static Set<Class<? extends KnowledgeSource>> knowledgeSources;
	private static Set<Class<? extends ReasonerComponent>> reasonerComponents;
	private static Set<Class<? extends LearningProblem>> learningProblems;
	private static Set<Class<? extends LearningAlgorithmNew>> learningAlgorithms;

	// list of all configuration options of all components
	private static Map<Class<? extends Component>, List<ConfigOption<?>>> componentOptions;
	private static Map<Class<? extends Component>, Map<String, ConfigOption<?>>> componentOptionsByName;
	private static Map<Class<? extends LearningAlgorithmNew>, Collection<Class<? extends LearningProblem>>> algorithmProblemsMapping;

	private Comparator<Class<?>> classComparator = new Comparator<Class<?>>() {

		public int compare(Class<?> c1, Class<?> c2) {
			return c1.getName().compareTo(c2.getName());
		}

	};

	@SuppressWarnings( { "unchecked" })
	private ComponentManager() {
		// read in components file
		List<String> componentsString = readComponentsFile();

		// component list
		components = new TreeSet<Class<? extends Component>>(classComparator);
		knowledgeSources = new TreeSet<Class<? extends KnowledgeSource>>(classComparator);
		reasonerComponents = new TreeSet<Class<? extends ReasonerComponent>>(classComparator);
		learningProblems = new TreeSet<Class<? extends LearningProblem>>(classComparator);
		learningAlgorithms = new TreeSet<Class<? extends LearningAlgorithmNew>>(classComparator);
		algorithmProblemsMapping = new TreeMap<Class<? extends LearningAlgorithmNew>, Collection<Class<? extends LearningProblem>>>(
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
				else if (LearningAlgorithmNew.class.isAssignableFrom(component)) {
					Class<? extends LearningAlgorithmNew> learningAlgorithmClass = (Class<? extends LearningAlgorithmNew>) component;
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

		// read in all configuration options
		componentOptions = new HashMap<Class<? extends Component>, List<ConfigOption<?>>>();
		componentOptionsByName = new HashMap<Class<? extends Component>, Map<String, ConfigOption<?>>>();

		for (Class<? extends Component> component : components) {

			List<ConfigOption<?>> options = (List<ConfigOption<?>>) invokeStaticMethod(component,
					"createConfigOptions");

			componentOptions.put(component, options);

			Map<String, ConfigOption<?>> byName = new HashMap<String, ConfigOption<?>>();
			for (ConfigOption<?> option : options)
				byName.put(option.getName(), option);
			componentOptionsByName.put(component, byName);

		}

		// System.out.println(components);
	}

	/**
	 * 
	 * @return The singleton <code>ComponentManager</code> instance.
	 */
	public static ComponentManager getInstance() {
		return cm;
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
				try {
					@SuppressWarnings( { "unchecked" })
					ConfigEntry<T> entry = new ConfigEntry<T>((ConfigOption<T>) option, value);
					component.applyConfigEntry(entry);
				} catch (InvalidConfigOptionValueException e) {
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
			return true;
		} catch (InvalidConfigOptionValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Factory method for creating a knowledge source.
	 * @param source A registered knowledge source component.
	 * @return An instance of the given knowledge source class.
	 */
	public KnowledgeSource knowledgeSource(Class<? extends KnowledgeSource> source) {
		if (!knowledgeSources.contains(source))
			System.err.println("Warning: knowledge source " + source
					+ " is not a registered knowledge source component.");

		return invokeConstructor(source, new Class[] {}, new Object[] {});
	}

	public <T extends ReasonerComponent> ReasoningService reasoningService(Class<T> reasoner,
			KnowledgeSource source) {
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		sources.add(source);
		return reasoningService(reasoner, sources);
	}

	public <T extends ReasonerComponent> ReasoningService reasoningService(Class<T> reasoner,
			Set<KnowledgeSource> sources) {
		if (!reasonerComponents.contains(reasoner))
			System.err.println("Warning: reasoner component " + reasoner
					+ " is not a registered reasoner component.");

		T reasonerInstance = invokeConstructor(reasoner, new Class[] { Set.class },
				new Object[] { sources });
		return new ReasoningService(reasonerInstance);
	}

	public <T extends LearningProblem> T learningProblem(Class<T> lp, ReasoningService reasoner) {
		if (!learningProblems.contains(lp))
			System.err.println("Warning: learning problem " + lp
					+ " is not a registered learning problem component.");

		return invokeConstructor(lp, new Class[] { ReasoningService.class },
				new Object[] { reasoner });
	}

	// automagically calls the right constructor for the given learning problem
	public <T extends LearningAlgorithmNew> T learningAlgorithm(Class<T> la, LearningProblem lp) {
		if (!learningAlgorithms.contains(la))
			System.err.println("Warning: learning algorithm " + la
					+ " is not a registered learning algorithm component.");

		// find the right constructor: use the one that is registered and
		// has the class of the learning problem as a subclass
		Class<? extends LearningProblem> constructorArgument = null;
		for (Class<? extends LearningProblem> problemClass : algorithmProblemsMapping.get(la)) {
			if (problemClass.isAssignableFrom(lp.getClass()))
				constructorArgument = problemClass;
		}

		if (constructorArgument == null) {
			System.err.println("Warning: No suitable constructor registered for algorithm "
					+ la.getName() + " and problem " + lp.getClass().getName()
					+ ". Registered constructors for " + la.getName() + ": "
					+ algorithmProblemsMapping.get(la) + ".");
			return null;
		}

		return invokeConstructor(la, new Class[] { constructorArgument }, new Object[] { lp });
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
	
	public ConfigOption<?> getConfigOption(Class<? extends Component> component, String name) {
		return componentOptionsByName.get(component).get(name);
	}

}
