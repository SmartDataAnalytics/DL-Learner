/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import java.util.Collection;
import java.util.LinkedList;

import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;

/**
 * Base class of all components. See also http://dl-learner.org/wiki/Architecture.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class Component {
	
	/**
	 * Returns the name of this component. By default, "unnamed 
	 * component" is returned, but all implementations of components
	 * are strongly encouraged to overwrite this method.
	 * @return The name of this component.
	 */
	public static String getName() {
		return "unnamed component";
	}
	
	/**
	 * Returns all configuration options supported by this component.
	 * @return A list of supported configuration options for this
	 * component.
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		return new LinkedList<ConfigOption<?>>();
	}
	
	/**
	 * Method to be called after the component has been configured.
	 * Implementation of components can overwrite this method to
	 * perform setup and initialisation tasks for this component.
	 */
	public abstract void init() throws ComponentInitException;
	
	/**
	 * Applies a configuration option to this component. Implementations
	 * of components should use option and value of the config entry to
	 * perform an action (usually setting an internal variable to 
	 * an appropriate value).
	 * 
	 * @param entry A configuration entry.
	 */
	public abstract <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException;
	
	/**
	 * Gets the value of a configuration option of this component.
	 * 
	 * @param <T> Option type.
	 * @param option A configuration option of this component.
	 * @return Current value of the configuration option.
	 */
// now implemented in ComponentManager
//	public abstract <T> T getConfigValue(ConfigOption<T> option) throws UnknownConfigOptionException;
}
