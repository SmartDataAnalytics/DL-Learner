/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import com.google.common.collect.Sets;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.log4j.Level;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.accuracymethods.AccMethod;
import org.dllearner.refinementoperators.RefinementOperator;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Component manager for the new (as of 2011) annotation based configuration
 * system.
 *
 * In the future, this may replace the previous implementation of component
 * manager.
 *
 * @author Jens Lehmann
 *
 */
public class AnnComponentManager {
	private static Logger logger = LoggerFactory.getLogger(AnnComponentManager.class);

    // the list of annotation based components (note that we save them as string here
    // instead of class objects in order not to have dependencies on the implementation
    // of components, which are not required to be in the core module - the class
    // objects are only created on invocation of the component manager);
    // components must be listed here if they should be supported in interfaces
    // (CLI, GUI, Web Service) and scripts (HTML documentation generator)
    private static List<String> componentClassNames;
//    ;= new ArrayList<String>  ( Arrays.asList(new String[]{
//            "org.dllearner.algorithms.NaiveALLearner",
//            "org.dllearner.algorithms.celoe.CELOE",
////            "org.dllearner.algorithms.celoe.PCELOE",
//            "org.dllearner.algorithms.el.ELLearningAlgorithm",
//            "org.dllearner.algorithms.el.ELLearningAlgorithmDisjunctive",
////            "org.dllearner.algorithms.fuzzydll.FuzzyCELOE",
////            "org.dllearner.algorithms.BruteForceLearner",
////            "org.dllearner.algorithms.RandomGuesser",
//            "org.dllearner.algorithms.properties.DisjointObjectPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner",
//            "org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner",
//            "org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner",
//            "org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.TransitiveObjectPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.FunctionalDataPropertyAxiomLearner",
//            "org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner",
//            "org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner",
//            "org.dllearner.algorithms.properties.SubDataPropertyOfAxiomLearner",
//            "org.dllearner.algorithms.DisjointClassesLearner",
//            "org.dllearner.algorithms.SimpleSubclassLearner",
//            "org.dllearner.algorithms.qtl.QTL2Disjunctive",
//            "org.dllearner.kb.KBFile",
//            "org.dllearner.kb.OWLFile",
//            "org.dllearner.kb.SparqlEndpointKS",
//            "org.dllearner.kb.LocalModelBasedSparqlEndpointKS",
//            "org.dllearner.kb.sparql.SparqlKnowledgeSource",
//            "org.dllearner.kb.sparql.simple.SparqlSimpleExtractor",
//            "org.dllearner.learningproblems.PosNegLPStandard",
////            "org.dllearner.learningproblems.PosNegLPStrict",
////            "org.dllearner.learningproblems.FuzzyPosNegLPStandard",
//            "org.dllearner.learningproblems.PosOnlyLP",
//            "org.dllearner.learningproblems.ClassLearningProblem",
//            "org.dllearner.learningproblems.PropertyAxiomLearningProblem",
//            "org.dllearner.reasoning.ClosedWorldReasoner",
//            "org.dllearner.reasoning.OWLAPIReasoner",
//            "org.dllearner.reasoning.SPARQLReasoner",
////            "org.dllearner.reasoning.fuzzydll.FuzzyOWLAPIReasoner",
//            "org.dllearner.algorithms.ocel.OCEL",
//            "org.dllearner.algorithms.ocel.MultiHeuristic",
//            "org.dllearner.algorithms.celoe.OEHeuristicRuntime",
//            "org.dllearner.algorithms.isle.NLPHeuristic",
//            "org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristicComplex",
//            "org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristicSimple",
//            "org.dllearner.algorithms.el.DisjunctiveHeuristic",
//            "org.dllearner.algorithms.isle.metrics.RelevanceWeightedStableHeuristic",
//            "org.dllearner.algorithms.el.StableHeuristic",
//            "org.dllearner.refinementoperators.RhoDRDown",
////            "org.dllearner.refinementoperators.SynchronizedRhoDRDown",
//            // just for testing
//            // "org.dllearner.refinementoperators.ExampleOperator",
//            "org.dllearner.utilities.semkernel.SemKernelWorkflow",
//            "org.dllearner.utilities.semkernel.MPSemKernelWorkflow",
//    } ));
    private static Collection<Class<? extends Component>> components;
    private static BidiMap<Class<? extends Component>, String> componentNames;
    private static BidiMap<Class<? extends Component>, String> componentNamesShort;

	private static AnnComponentManager cm = null;
	private static Reflections reflectionScanner = null;

	private AnnComponentManager() {
		if (componentClassNames == null) {
			componentClassNames = new ArrayList<>();
			if (reflectionScanner == null) {
				org.apache.log4j.Logger.getLogger(Reflections.class).setLevel(Level.OFF);
				reflectionScanner = new Reflections("org.dllearner");
			}
			Set<Class<? extends Component>> componentClasses = reflectionScanner.getSubTypesOf(Component.class);
			Set<Class<?>> componentAnnClasses = reflectionScanner.getTypesAnnotatedWith(ComponentAnn.class, true);
			for (Class<?> clazz
					: Sets.intersection(
							componentClasses,
							componentAnnClasses
					)
				) {
				if (!Modifier.isAbstract( clazz.getModifiers() ))
					componentClassNames.add(clazz.getCanonicalName());
			}
			for (Class<?> clazz
					: Sets.difference(componentClasses, componentAnnClasses)
					) {
				if (!Modifier.isAbstract( clazz.getModifiers() ))
					logger.debug("Warning: " + clazz.getCanonicalName() + " implements Component but is not annotated, ignored");
			}
		}
		// conversion of class strings to objects
		components = new TreeSet<>((Comparator<Class<? extends Component>>) (o1, o2) -> {
			return o1.getName().compareTo(o2.getName());
		});
		componentNames = new DualHashBidiMap<>();
		componentNamesShort = new DualHashBidiMap<>();
		for (String componentClassName : componentClassNames) {
			try {
				Class<? extends Component> component = Class.forName(componentClassName).asSubclass(Component.class);
				components.add(component);
				componentNames.put(component, getName(component));
				componentNamesShort.put(component, getShortName(component));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Explicitly sets the list of components to use. This will (re-)initialise the
	 * component manager the next time the singleton instance is retrieved.
	 */
	public static void setComponentClassNames(List<String> componentClassNames) {
		AnnComponentManager.componentClassNames = componentClassNames;
		cm = null;
	}
	
	public static void setReflectionScanner(Reflections ref) {
		AnnComponentManager.reflectionScanner = ref;
		setComponentClassNames(null);
	}

	/**
	 * Gets the singleton instance of <code>ComponentManager</code>.
	 * @return The singleton <code>ComponentManager</code> instance.
	 */
	public static AnnComponentManager getInstance() {
		if(cm == null) {
			cm = new AnnComponentManager();
		}
		return cm;
	}

	/**
	 * Returns a list of all available components in this instance
	 * of <code>ComponentManager</code>.
	 * @return the components A list of component classes available in this
	 * instance of <code>ComponentManager</code>.
	 */
	public Collection<Class<? extends Component>> getComponents() {
		return components;
	}
	
	/**
	 * Returns a list of all available components in this instance
	 * of <code>ComponentManager</code>.
	 * @return the components A list of component classes available in this
	 * instance of <code>ComponentManager</code>.
	 */
	public SortedSet<String> getComponentStrings() {
		SortedSet<String> result = getComponents().stream()
				.map(AnnComponentManager::getShortName)
				.collect(Collectors.toCollection(TreeSet::new));
		return result;
	}

    /**
     * Get registered components which are of the specified type.
     *
     * @param type The super type.
     * @return All sub classes of type.
     */
    public SortedSet<String> getComponentStringsOfType(Class type) {

    	SortedSet<String> result = getComponentsOfType(type).stream()
			    .map(AnnComponentManager::getShortName)
			    .collect(Collectors.toCollection(TreeSet::new));

	    return result;
    }
    
    /**
     * Get the corresponding component class given the long or short name.
     *
     * @param componentName The long or short name of the component.
     * @return The class of the component.
     */
    public Class<? extends Component> getComponentClass(String componentName) {
    	// lookup by long name
    	Class<? extends Component> componentClass = componentNames.getKey(componentName);
    	
    	// lookup by short name
    	if(componentClass == null) {
    		componentClass = componentNamesShort.getKey(componentName);
    	}

        return componentClass;
    }
    
    /**
     * Get registered components which are of the specified type.
     *
     * @param type The super type.
     * @return All sub classes of type.
     */
    public Collection<Class<? extends Component>> getComponentsOfType(Class type) {

        Collection<Class<? extends Component>> result = components.stream()
		        .filter(component -> type.isAssignableFrom(component))
		        .collect(Collectors.toCollection(ArrayList::new));

	    return result;
    }

	/**
	 * Convenience method, which returns a list of components along with
	 * their name.
	 *
	 * @return A map where the key is the class of the component and the
	 * value is its name.
	 */
	public BidiMap<Class<? extends Component>, String> getComponentsNamed() {
		return componentNames;
	}

	/**
	 * Convenience method, which returns a list of components along with
	 * their name.
	 *
	 * @return A map where the key is the class of the component and the
	 * value is its name.
	 */
	public BidiMap<Class<? extends Component>, String> getComponentsNamedShort() {
		return componentNamesShort;
	}

	/**
	 * Applies a config entry to a component. If the entry is not valid, the method
	 * prints an exception and returns false.
	 * @param <T> Type of the config option.
	 * @param component A component object.
	 * @param optionName the option name
	 * @param value the value to set
	 * @return True if the config entry could be applied succesfully, otherwise false.
	 */
	@Deprecated
	public static <T> boolean applyConfigEntry(AbstractComponent component, String optionName, T value) {
		List<AbstractComponent> childComponents = new LinkedList<>();
		for (Method m : component.getClass().getMethods()) {
			if (m.getName().equals("set" + optionName.substring(0, 1).toUpperCase() + optionName.substring(1))) {
				try {
					m.invoke(component, value);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					logger.debug("Error setting " + optionName + " to " + value + " on " + component + ": ", e);
					return false;
				}
				return true;
			} else if (m.getName().startsWith("get")
					&& AbstractComponent.class.isAssignableFrom(m.getReturnType())) {
				Object cc;
				try {
					cc = m.invoke(component);
					childComponents.add((AbstractComponent) cc);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					logger.trace("Error querying " + m.getName() + " for subcomponent in " + component, e);
				}
			}
		}
		for (AbstractComponent cc : childComponents) {
			if (cc != null) {
				boolean try_inv = applyConfigEntry(cc, optionName, value);
				if (try_inv) {
					return true;
				}
			}
		}
		return false;
	}

	public final static Class[] coreComponentClasses = {
		KnowledgeSource.class,
		LearningAlgorithm.class,
		AxiomLearningAlgorithm.class,
		ClassExpressionLearningAlgorithm.class,
		LearningProblem.class,
		ReasonerComponent.class,
		RefinementOperator.class,
		Heuristic.class,
		AccMethod.class
	};

	/**
	 * Convenience method to retrieve core types of a component. The main use case for this
	 * is for automatic documentation generation.
	 *
	 * @param component A component.
	 * @return The list of core interfaces the component implemnets.
	 */
	public static List<Class<? extends Component>> getCoreComponentTypes(Class<? extends Component> component) {
		List<Class<? extends Component>> types = new LinkedList<>();
		for(Class c : coreComponentClasses) {
			if(c.isAssignableFrom(component)) {
				types.add(c);
			}
		}
		return types;
	}

	/**
	 * Returns the name of a DL-Learner component.
	 * @param component the component
	 * @return Name of the component.
	 */
	public static String getName(Class<? extends Component> component){
		ComponentAnn ann = component.getAnnotation(ComponentAnn.class);
		if(ann == null) {
			throw new Error("Component " + component + " does not use component annotation.");
		}
		return ann.name();
	}

	/**
	 * Returns the name of a DL-Learner component.
	 * @param component the component
	 * @return Name of the component.
	 */
	public static String getName(Component component){
		return getName(component.getClass());
	}

	/**
	 * Returns the name of a DL-Learner component.
	 * @param component the component
	 * @return Name of the component.
	 */
	public static String getShortName(Class<? extends Component> component){
		ComponentAnn ann = component.getAnnotation(ComponentAnn.class);
		if(ann == null) {
			throw new Error("Component " + component + " does not use component annotation.");
		}
		return ann.shortName();
	}

	/**
	 * Returns the short name of a DL-Learner component.
	 * @param component the component
	 * @return Short name of the component.
	 */
	public static String getShortName(Component component){
		return getShortName(component.getClass());
	}

	/**
	 * Returns the name of a DL-Learner component.
	 * @param component the component
	 * @return Name of the component.
	 */
	public static String getDescription(Class<? extends Component> component){
		ComponentAnn ann = component.getAnnotation(ComponentAnn.class);
		return ann.description();
	}

	/**
	 * Returns the description of a DL-Learner component.
	 * @param component the component
	 * @return OWLClassExpression of the component.
	 */
	public static String getDescription(Component component){
		return getDescription(component.getClass());
	}
	
	/**
	 * Returns the config options of a DL-Learner component.
	 * @param component the component
	 * @return OWLClassExpression of the component.
	 */
	public static Set<Field> getConfigOptions(Class<? extends Component> component){
		Set<Field> set = new HashSet<>();
	    Class<?> c = component;
	    while (c != null) {
	        for (Field field : c.getDeclaredFields()) {
	            if (field.isAnnotationPresent(ConfigOption.class)) {
	                set.add(field);
	            }
	        }
	        c = c.getSuperclass();
	    }
	    return set;
	}

	/**
	 * Returns the version of a DL-Learner component.
	 * @param component the component
	 * @return Version of the component.
	 */
	public static double getVersion(Class<? extends Component> component){
		ComponentAnn ann = component.getAnnotation(ComponentAnn.class);
		return ann.version();
	}

	/**
	 * Returns the version of a DL-Learner component.
	 * @param component the component
	 * @return Version of the component.
	 */
	public static double getVersion(Component component){
		return getVersion(component.getClass());
	}

	public static boolean addComponentClassName(String e) {
		return componentClassNames.add(e);
	}

	/**
	 * Returns the name of a config option
	 * @param f the Reflection field of the option
	 * @return name of the option
	 */
	public static String getName(Field f) {
		f.getAnnotation(ConfigOption.class);
		return f.getName();
	}
}
