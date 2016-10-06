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

package org.dllearner.configuration.spring.editors;

import com.google.common.collect.ObjectArrays;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigOption;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ConfigHelper {
	
	public final static Map<Class<?>, Class<?>> map = new HashMap<>();
	
	static {
		map.put(Boolean.class, boolean.class);
		map.put(Byte.class, byte.class);
		map.put(Short.class, short.class);
		map.put(Character.class, char.class);
		map.put(Integer.class, int.class);
		map.put(Long.class, long.class);
		map.put(Float.class, float.class);
		map.put(Double.class, double.class);
	}
	
	/**
	 * Configures the given component by setting the value for the appropriate config option.
	 * @param component the component to be configured
	 * @param configName the name of the config option
	 * @param configValue the value of the config option
	 */
	@Deprecated
	public static <T> void configure(Component component, String configName, T configValue){
		List<Field> fields = getAllFields(component);
        for(Field f : fields){
        	if(f.isAnnotationPresent(ConfigOption.class)){
        		if(AnnComponentManager.getName(f).equals(configName)){
        			try {
						PropertyEditor editor = PropertyEditor.class.newInstance();
						editor.setAsText(configValue.toString());
						Method method = component.getClass().getMethod("set" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1), getClassForObject(editor.getValue()));
						method.invoke(component, editor.getValue());
					} catch (IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
						e.printStackTrace();
					}
		        }
        		
        	}
        }
	}
	
	/**
	 * Returns all config options for the given component.
	 * @param component
	 * @return
	 */
	public static List<ConfigOption> getConfigOptions(Component component){
		return getConfigOptions(component.getClass());
	}
	

	/**
	 * 
	 * @param component The component to analyse.
	 * @return All config options of the component with their respective value.
	 */
	public static Map<Field,Object> getConfigOptionValues(Component component) {
		Map<Field,Object> optionValues = new HashMap<>();
		List<Field> fields = getAllFields(component);
		for(Field field : fields) {
			if(field.isAnnotationPresent(ConfigOption.class)) {
				try {
					// we invoke the public getter instead of accessing a private field (may cause problem with SecurityManagers)
					// use Spring BeanUtils TODO: might be unnecessarily slow because we already have the field?
					PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(
							component.getClass(),
							field.getName()
					);
					Method readMethod = propertyDescriptor.getReadMethod();
					if(readMethod == null) {
						throw new RuntimeException("Getter method is missing for field " + field + " in component " + component);
					}
					Object value = readMethod.invoke(component);

					optionValues.put(field, value);
				} catch (IllegalArgumentException | InvocationTargetException | BeansException | IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
		}
		return optionValues;
	}
	
	/**
	 * Returns all config options for the given component.
	 * @param component
	 * @return
	 */
	public static List<ConfigOption> getConfigOptions(Class<? extends Component> component){
		List<ConfigOption> options = new ArrayList<>();
		
		Field[] fields = component.getDeclaredFields();
		for(Field f : fields){
        	ConfigOption option = f.getAnnotation(ConfigOption.class);
        	if(option != null){
        		options.add(option);
        	}
        }
		
		return options;
	}
	
	/**
	 * Returns all config options for the given component.
	 * @param component
	 * @return
	 */
	public static Map<Field,Class<?>> getConfigOptionTypes(Class<?> component){
		return getConfigOptionTypes(component, true);
	}
	
	/**
	 * Returns all config options for the given component.
	 * @param component
	 * @return
	 */
	public static Map<Field,Class<?>> getConfigOptionTypes(Class<?> component, boolean useSuperTypes){
		Map<Field,Class<?>> optionTypes = new TreeMap<>((o1, o2) -> {
			return AnnComponentManager.getName(o1).compareTo(AnnComponentManager.getName(o2));
		});
		Field[] fields = component.getDeclaredFields();
		if(useSuperTypes) {
			if(useSuperTypes) {
				fields = ObjectArrays.concat(fields, component.getSuperclass().getDeclaredFields(), Field.class);
			}
		}
		for(Field f : fields){
        	if(f.isAnnotationPresent(ConfigOption.class)) {
        		optionTypes.put(f, f.getType());
        	}
        }
		return optionTypes;
	}
	
	/*
	 * returns the declared fields for the class and its superclass.
	 */
	private static List<Field> getAllFields(Component component){
		List<Field> fields = new ArrayList<>();
		fields.addAll(Arrays.asList(component.getClass().getDeclaredFields()));
		//check also the fields of the super class if exists
		if(component.getClass().getSuperclass() != null){
			fields.addAll(Arrays.asList(component.getClass().getSuperclass().getDeclaredFields()));
		}
		return fields;
	}
	
	private static Class<?> getClassForObject(Object obj){
		if(map.containsKey(obj.getClass())){
			return map.get(obj.getClass());
		} else {
			return obj.getClass();
		}
	}

}
