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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.refinementoperators.RefinementOperator;


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

    // the list of annotation based components (note that we save them as string here
    // instead of class objects in order not to have dependencies on the implementation
    // of components, which are not required to be in the core module - the class
    // objects are only created on invocation of the component manager);
    // components must be listed here if they should be supported in interfaces
    // (CLI, GUI, Web Service) and scripts (HTML documentation generator)
    private static List<String> componentClassNames = new ArrayList<String>  ( Arrays.asList(new String[]{
            "org.dllearner.algorithms.NaiveALLearner",
            "org.dllearner.algorithms.celoe.CELOE",
//            "org.dllearner.algorithms.celoe.PCELOE",
            "org.dllearner.algorithms.el.ELLearningAlgorithm",
            "org.dllearner.algorithms.el.ELLearningAlgorithmDisjunctive",
//            "org.dllearner.algorithms.fuzzydll.FuzzyCELOE",
//            "org.dllearner.algorithms.BruteForceLearner",
//            "org.dllearner.algorithms.RandomGuesser",
            "org.dllearner.algorithms.properties.DisjointObjectPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner",
            "org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner",
            "org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner",
            "org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.TransitiveObjectPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.FunctionalDataPropertyAxiomLearner",
            "org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner",
            "org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner",
            "org.dllearner.algorithms.properties.SubDataPropertyOfAxiomLearner",
            "org.dllearner.algorithms.DisjointClassesLearner",
            "org.dllearner.algorithms.SimpleSubclassLearner",
            "org.dllearner.algorithms.qtl.QTL2Disjunctive",
            "org.dllearner.kb.KBFile",
            "org.dllearner.kb.OWLFile",
            "org.dllearner.kb.SparqlEndpointKS",
            "org.dllearner.kb.LocalModelBasedSparqlEndpointKS",
            "org.dllearner.kb.sparql.SparqlKnowledgeSource",
            "org.dllearner.kb.sparql.simple.SparqlSimpleExtractor",
            "org.dllearner.learningproblems.PosNegLPStandard",
//            "org.dllearner.learningproblems.PosNegLPStrict",
//            "org.dllearner.learningproblems.FuzzyPosNegLPStandard",
            "org.dllearner.learningproblems.PosOnlyLP",
            "org.dllearner.learningproblems.ClassLearningProblem",
            "org.dllearner.learningproblems.PropertyAxiomLearningProblem",
            "org.dllearner.reasoning.FastInstanceChecker",
            "org.dllearner.reasoning.ClosedWorldReasoner",
            "org.dllearner.reasoning.OWLAPIReasoner",
            "org.dllearner.reasoning.SPARQLReasoner",
//            "org.dllearner.reasoning.fuzzydll.FuzzyOWLAPIReasoner",
            "org.dllearner.algorithms.ocel.OCEL",
            "org.dllearner.algorithms.ocel.MultiHeuristic",
            "org.dllearner.algorithms.celoe.OEHeuristicRuntime",
            "org.dllearner.refinementoperators.RhoDRDown",
//            "org.dllearner.refinementoperators.SynchronizedRhoDRDown",
            // just for testing
            // "org.dllearner.refinementoperators.ExampleOperator",
            "org.dllearner.utilities.semkernel.SemKernelWorkflow",
            "org.dllearner.utilities.semkernel.MPSemKernelWorkflow",
    } ));
    private static Collection<Class<? extends Component>> components;
    private static BidiMap<Class<? extends Component>, String> componentNames;
    private static BidiMap<Class<? extends Component>, String> componentNamesShort;

	private static AnnComponentManager cm = null;

	private AnnComponentManager() {
		// conversion of class strings to objects
		components = new HashSet<Class<? extends Component>>();
		componentNames = new DualHashBidiMap<Class<? extends Component>, String>();
		componentNamesShort = new DualHashBidiMap<Class<? extends Component>, String>();
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
     * Get registered components which are of the specified type.
     *
     * @param type The super type.
     * @return All sub classes of type.
     */
    public Collection<Class<? extends Component>> getComponentsOfType(Class type) {

        Collection<Class<? extends Component>> result = new ArrayList<Class<? extends Component>>();
        for (Class<? extends Component> component : components) {
            if (type.isAssignableFrom(component)) {
                result.add(component);
            }
        }

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

	public final static Class[] coreComponentClasses = {
		KnowledgeSource.class,
		LearningAlgorithm.class,
		AxiomLearningAlgorithm.class,
		ClassExpressionLearningAlgorithm.class,
		LearningProblem.class,
		ReasonerComponent.class,
		RefinementOperator.class,
		Heuristic.class
	};

	/**
	 * Convenience method to retrieve core types of a component. The main use case for this
	 * is for automatic documentation generation.
	 *
	 * @param component A component.
	 * @return The list of core interfaces the component implemnets.
	 */
	public static List<Class<? extends Component>> getCoreComponentTypes(Class<? extends Component> component) {
		List<Class<? extends Component>> types = new LinkedList<Class<? extends Component>>();
		for(Class c : coreComponentClasses) {
			if(c.isAssignableFrom(component)) {
				types.add(c);
			}
		}
		return types;
	}

	/**
	 * Returns the name of a DL-Learner component.
	 * @param component
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
	 * @param component
	 * @return Name of the component.
	 */
	public static String getName(Component component){
		return getName(component.getClass());
	}

	/**
	 * Returns the name of a DL-Learner component.
	 * @param component
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
	 * @param component
	 * @return Short name of the component.
	 */
	public static String getShortName(Component component){
		return getShortName(component.getClass());
	}

	/**
	 * Returns the name of a DL-Learner component.
	 * @param component
	 * @return Name of the component.
	 */
	public static String getDescription(Class<? extends Component> component){
		ComponentAnn ann = component.getAnnotation(ComponentAnn.class);
		return ann.description();
	}

	/**
	 * Returns the description of a DL-Learner component.
	 * @param component
	 * @return OWLClassExpression of the component.
	 */
	public static String getDescription(Component component){
		return getDescription(component.getClass());
	}
	
	/**
	 * Returns the config options of a DL-Learner component.
	 * @param component
	 * @return OWLClassExpression of the component.
	 */
	public static Set<ConfigOption> getConfigOptions(Class<? extends Component> component){
		Set<ConfigOption> set = new HashSet<>();
	    Class<?> c = component;
	    while (c != null) {
	        for (java.lang.reflect.Field field : c.getDeclaredFields()) {
	            if (field.isAnnotationPresent(ConfigOption.class)) {
	                set.add(field.getAnnotation(ConfigOption.class));
	            }
	        }
	        c = c.getSuperclass();
	    }
	    return set;
	}

	/**
	 * Returns the version of a DL-Learner component.
	 * @param component
	 * @return Version of the component.
	 */
	public static double getVersion(Class<? extends Component> component){
		ComponentAnn ann = component.getAnnotation(ComponentAnn.class);
		return ann.version();
	}

	/**
	 * Returns the version of a DL-Learner component.
	 * @param component
	 * @return Version of the component.
	 */
	public static double getVersion(Component component){
		return getVersion(component.getClass());
	}

	public static boolean addComponentClassName(String e) {
		return componentClassNames.add(e);
	}
}
