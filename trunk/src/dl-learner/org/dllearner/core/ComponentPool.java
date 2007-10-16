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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Stores all live components and the configuration options, which were
 * applied to them.
 * 
 * @author Jens Lehmann
 *
 */
public class ComponentPool {

	// stores all components, which are live (components which are 
	// no longer used have to be deregistered)
	private List<Component> components = new LinkedList<Component>();
	
	// stores the last value which was set for a particular 
	// config option
	private Map<Component,Map<ConfigOption<?>,Object>> lastValidConfigValue = new HashMap<Component,Map<ConfigOption<?>,Object>>();
	// complete history of all made config entries for a component
	private Map<Component,List<ConfigEntry<?>>> configEntryHistory = new HashMap<Component,List<ConfigEntry<?>>>();
	
	public void registerComponent(Component component) {
		components.add(component);
		Map<ConfigOption<?>,Object> emptyMap = new HashMap<ConfigOption<?>,Object>();
		lastValidConfigValue.put(component, emptyMap);
		configEntryHistory.put(component, new LinkedList<ConfigEntry<?>>());
	}

	public void unregisterComponent(Component component) {
		configEntryHistory.remove(component);
		lastValidConfigValue.remove(component);
		components.remove(component);
	}

	@SuppressWarnings({"unchecked"})
	public <T> T getLastValidConfigValue(Component component, ConfigOption<T> option) {
		return (T) lastValidConfigValue.get(component).get(option);
	}
	
	public void addConfigEntry(Component component, ConfigEntry<?> entry, boolean valid) {
		configEntryHistory.get(component).add(entry);
		if(valid)
			lastValidConfigValue.get(component).put(entry.getOption(), entry.getValue());
	}
	
}
