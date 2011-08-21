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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;

/**
 * Stores all live components and the configuration options, which were applied
 * to them. This allows to detect, which components are currently active, which
 * values are assigned to specific options, and to collect statistics (e.g. in
 * a web service scenario).
 * 
 * @author Jens Lehmann
 * 
 */
public final class ComponentPool {

	private static Logger logger = Logger.getLogger(ComponentPool.class);	
	
	// stores all components, which are live (components which are
	// no longer used have to be deregistered)
	private List<AbstractComponent> components = new LinkedList<AbstractComponent>();

	// stores the last value which was set for a particular
	// config option
	private Map<AbstractComponent, Map<ConfigOption<?>, Object>> lastValidConfigValue = new HashMap<AbstractComponent, Map<ConfigOption<?>, Object>>();
	// complete history of all made config entries for a component
	private Map<AbstractComponent, List<ConfigEntry<?>>> configEntryHistory = new HashMap<AbstractComponent, List<ConfigEntry<?>>>();

	/**
	 * Registers a component instance in the pool. 
	 * @param component The component to add to the pool.
	 */
	public void registerComponent(AbstractComponent component) {
		components.add(component);
		Map<ConfigOption<?>, Object> emptyMap = new HashMap<ConfigOption<?>, Object>();
		lastValidConfigValue.put(component, emptyMap);
		configEntryHistory.put(component, new LinkedList<ConfigEntry<?>>());
		logger.debug("Component instance " + component + " added to component pool.");
	}

	/**
	 * Unregisters a component instance. This method should be used if the
	 * component will not be used anymore. It frees the memory for
	 * storing the component and its configuration options.  
	 * @param component The component to remove from the pool.
	 */
	public void unregisterComponent(AbstractComponent component) {
		configEntryHistory.remove(component);
		lastValidConfigValue.remove(component);
		components.remove(component);
		logger.debug("Component instance " + component + " removed from component pool.");
	}

	/**
	 * Gets the last valid config value set for this component.
	 * @param <T> The type of the value of the config option (String, Integer etc.).
	 * @param component The component to query.
	 * @param option The option for which one wants to get the value.
	 * @return The last value set for this option or null if the value hasn't been 
	 * set using the {@link ComponentManager}. In this case, the value is
	 * usually at the default value (or has been set internally surpassing the
	 * component architecture, which is not recommended).
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getLastValidConfigValue(AbstractComponent component, ConfigOption<T> option) {
		return (T) lastValidConfigValue.get(component).get(option);
	}

	/**
	 * Add a config entry change for the specified component.
	 * @param component The component, where the config entry has been set.
	 * @param entry The set config entry.
	 * @param valid A boolean value indicating whether the value was valid or not.
	 */
	protected void addConfigEntry(AbstractComponent component, ConfigEntry<?> entry, boolean valid) {
		configEntryHistory.get(component).add(entry);
		if (valid) {
			lastValidConfigValue.get(component).put(entry.getOption(), entry.getValue());
		}
		logger.trace("Config entry " + entry + " has been set for component " + component + " (validity: " + valid + ").");
	}

	/**
	 * Unregisters all components.
	 */
	protected void clearComponents() {
		components = new LinkedList<AbstractComponent>();
		lastValidConfigValue = new HashMap<AbstractComponent, Map<ConfigOption<?>, Object>>();
		configEntryHistory = new HashMap<AbstractComponent, List<ConfigEntry<?>>>();
	}
	
	/**
	 * @return The components in this pool.
	 */
	public List<AbstractComponent> getComponents(){
		return components;
	}

}
