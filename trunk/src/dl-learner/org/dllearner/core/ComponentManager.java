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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Central manager class for DL-Learner.
 * 
 * @author Jens Lehmann
 *
 */
public class ComponentManager {

	// these variables are valid for the complete lifetime of DL-Learner
	private static String componentsFile = "lib/components.ini";
	private static ComponentManager cm = new ComponentManager();
	private static Set<Class<? extends Component>> components;
	
	// list of all configuration options of all components
	private Map<Class<? extends Component>,List<ConfigOption<?>>> componentOptions;
	private Map<Class<? extends Component>,Map<String,ConfigOption<?>>> componentOptionsByName;
	
	private Comparator<Class<?>> classComparator = new Comparator<Class<?>>() {

		public int compare(Class<?> c1, Class<?> c2) {
			return c1.getName().compareTo(c2.getName());
		}
		
	};
	
	@SuppressWarnings({"unchecked"})
	private ComponentManager() {
		// read in components file
		List<String> componentsString = readComponentsFile();
		
		// component list
		components = new TreeSet<Class<? extends Component>>(classComparator);
		
		// create classes from strings
		for(String componentString : componentsString) {
			try {
				Class<? extends Component> component = Class.forName(componentString).asSubclass(Component.class);
				components.add(component);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// read in all configuration options
		componentOptions = new HashMap<Class<? extends Component>,List<ConfigOption<?>>>();
		componentOptionsByName = new HashMap<Class<? extends Component>,Map<String,ConfigOption<?>>>();
		
		for(Class<? extends Component> component : components) {
			// unfortunately Java does not seem to offer a way to call
			// a static method given a class object directly, so we have
			// to use reflection
			try {
				Method createConfig = component.getMethod("createConfigOptions");
				List<ConfigOption<?>> options = (List<ConfigOption<?>>) createConfig.invoke(null);
				
				componentOptions.put(component, options);
				
				Map<String,ConfigOption<?>> byName = new HashMap<String,ConfigOption<?>>();
				for(ConfigOption<?> option : options)
					byName.put(option.getName(), option);
				componentOptionsByName.put(component, byName);				
				
				// componentOptionsByName.put(key, value)
			} catch (IllegalArgumentException e) {
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
			
		}
		
	}
	
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
				if(!(line.startsWith("#") || line.startsWith("//") || line.startsWith("%")))
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
	 * value is correct, it is preferable to create a ConfigEntry object and apply
	 * it to the component (no type checking necessary).
	 * @param component
	 * @param optionName
	 * @param value
	 */
	public <T> void applyConfigEntry(Component component, String optionName, T value) {
		// first we look whether the component is registered
		if(components.contains(component.getClass())) {

			// look for a config option with the specified name
			ConfigOption<?> option = (ConfigOption<?>) componentOptionsByName.get(component.getClass()).get(optionName);
			if(option!=null) {
				// check whether the given object has the correct type
				if(!option.checkType(value)) {
					System.out.println("Warning: value " + value + " is not valid for option " + optionName + " in component " + component);
					return;
				}
				
				// we have checked the type, hence it should now be safe to typecast and
				// create a ConfigEntry object
				try {
					@SuppressWarnings({"unchecked"})
					ConfigEntry<T> entry = new ConfigEntry<T>((ConfigOption<T>) option, value);
					component.applyConfigEntry(entry);
				} catch (InvalidConfigOptionValueException e) {
					System.out.println("Warning: value " + value + " is not valid for option " + optionName + " in component " + component);
				}
			} else
				System.out.println("Warning: undefined option " + optionName + " in component " + component);			
		} else
			System.out.println("Warning: unregistered component " + component);		
	}
	
	public KnowledgeSource knowledgeSource(Class<? extends KnowledgeSource> source) {
		try {
			Constructor<? extends KnowledgeSource> constructor = source.getConstructor();
			return constructor.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public <T extends ReasonerComponent> ReasoningService reasoningService(Class<T> reasoner, KnowledgeSource source) {
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		sources.add(source);
		return reasoningService(reasoner, sources);
	}
	
	public <T extends ReasonerComponent> ReasoningService reasoningService(Class<T> reasoner, Set<KnowledgeSource> sources) {
		try {
			Constructor<T> constructor = reasoner.getConstructor(Set.class);
			T reasonerInstance = constructor.newInstance(sources);
			return new ReasoningService(reasonerInstance);
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
	
	public <T extends LearningProblemNew> T learningProblem(Class<T> lp, ReasoningService reasoner) {
		try {
			Constructor<T> constructor = lp.getConstructor(ReasoningService.class);
			return constructor.newInstance(reasoner);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		}
		
		return null;
	}
	
	public <T extends LearningAlgorithmNew> T learningAlgorithm(Class<T> la, LearningProblemNew lp) {
		try {
			Constructor<T> constructor = la.getConstructor(LearningProblemNew.class);
			return constructor.newInstance(lp);
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
	
}
