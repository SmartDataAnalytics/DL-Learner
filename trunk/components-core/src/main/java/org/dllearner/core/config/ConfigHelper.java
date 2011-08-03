package org.dllearner.core.config;

import java.beans.PropertyEditor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.core.Component;

public class ConfigHelper {
	
	/**
	 * Configures the given component by setting the value for the appropriate config option.
	 * @param component the component to be configured
	 * @param configName the name of the config option
	 * @param configValue the value of the config option
	 */
	public static void configure(Component component, String configName, String configValue){
		Field[] fields = component.getClass().getDeclaredFields();
        for(Field f : fields){
        	ConfigOption option = f.getAnnotation(ConfigOption.class);
        	if(option != null){
        		if(option.name().equals(configName)){
        			try {
						PropertyEditor editor = (PropertyEditor) option.propertyEditorClass().newInstance();
						editor.setAsText(configValue);
						f.set(component, editor.getValue());
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
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
		List<ConfigOption> options = new ArrayList<ConfigOption>();
		
		Field[] fields = component.getClass().getDeclaredFields();
		for(Field f : fields){
        	ConfigOption option = f.getAnnotation(ConfigOption.class);
        	if(option != null){
        		options.add(option);
        	}
        }
		
		return options;
	}

}
