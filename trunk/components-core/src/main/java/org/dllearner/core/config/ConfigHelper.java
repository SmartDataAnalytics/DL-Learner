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

package org.dllearner.core.config;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.core.Component;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

public class ConfigHelper {
	
	public final static Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
	
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
	public static <T> void configure(Component component, String configName, T configValue){
		List<Field> fields = getAllFields(component);
        for(Field f : fields){
        	ConfigOption option = f.getAnnotation(ConfigOption.class);
        	if(option != null){
        		if(option.name().equals(configName)){
        			try {
						PropertyEditor editor = (PropertyEditor) option.propertyEditorClass().newInstance();
						editor.setAsText(configValue.toString());
						Method method = component.getClass().getMethod("set" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1), getClassForObject(editor.getValue()));
						method.invoke(component, editor.getValue());
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
	@Deprecated
	public static Map<ConfigOption,String> getConfigOptionValuesString(Component component) {
		Map<ConfigOption,String> optionValues = new HashMap<ConfigOption,String>();
		List<Field> fields = getAllFields(component);//getConfigOptions(component).getClass().getDeclaredFields();
		for(Field field : fields) {
			ConfigOption option = field.getAnnotation(ConfigOption.class);
			if(option != null) {
				Class<? extends PropertyEditor> editorClass = option.propertyEditorClass();
				PropertyEditor editor = null;
				try {
					editor = editorClass.newInstance();
					Object object = field.get(component);
					editor.setValue(object);
					String value = editor.getAsText();
					optionValues.put(option, value);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return optionValues;
	}	
	
	/**
	 * 
	 * @param component The component to analyse.
	 * @return All config options of the component with their respective value.
	 */
	public static Map<ConfigOption,Object> getConfigOptionValues(Component component) {
		Map<ConfigOption,Object> optionValues = new HashMap<ConfigOption,Object>();
		List<Field> fields = getAllFields(component);
		for(Field field : fields) {
			ConfigOption option = field.getAnnotation(ConfigOption.class);
			if(option != null) {
				try {
					// we invoke the public getter instead of accessing a private field (may cause problem with SecurityManagers)
					// use Spring BeanUtils TODO: might be unnecessarily slow because we already have the field?
					Object value = BeanUtils.getPropertyDescriptor(component.getClass(), field.getName()).getReadMethod().invoke(component);
					optionValues.put(option, value);
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (BeansException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
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
		List<ConfigOption> options = new ArrayList<ConfigOption>();
		
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
	public static Map<ConfigOption,Class<?>> getConfigOptionTypes(Class<? extends Component> component){
		Map<ConfigOption,Class<?>> optionTypes = new HashMap<ConfigOption,Class<?>>();
		Field[] fields = component.getDeclaredFields();
		for(Field f : fields){
        	ConfigOption option = f.getAnnotation(ConfigOption.class);
        	if(option != null){
        		optionTypes.put(option, f.getType());
        	}
        }
		return optionTypes;
	}	
	
	/*
	 * returns the declared fields for the class and its superclass.
	 */
	private static List<Field> getAllFields(Component component){
		List<Field> fields = new ArrayList<Field>();
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
	
	public static void main(String[] args) {
		ObjectPropertyDomainAxiomLearner l = new ObjectPropertyDomainAxiomLearner(null);
		ConfigHelper.configure(l, "maxExecutionTimeInSeconds", "11");
		System.out.println(l.getMaxExecutionTimeInSeconds());
		System.out.println(ConfigHelper.getConfigOptionValues(l));
	}

}
