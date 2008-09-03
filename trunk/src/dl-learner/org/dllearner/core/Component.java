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
import org.dllearner.core.config.DoubleConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;

/**
 * Base class of all components. See also http://dl-learner.org/wiki/Architecture.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class Component {
	
//protected Configurator configurator;
	
	//public Configurator<? extends Configurator> getConfigurator(){
		//return configurator;
	//}
	
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
	 * 
	 * @throws ComponentInitException This exception is thrown if any
	 * exceptions occur within the initialisation process of this
	 * component. As component developer, you are encouraged to
	 * rethrow occuring exception as ComponentInitException and 
	 * giving an error message as well as the actualy exception by
	 * using the constructor {@link ComponentInitException#ComponentInitException(String, Throwable)}. 
	 */
	public abstract void init() throws ComponentInitException;
	
	/**
	 * Applies a configuration option to this component. Implementations
	 * of components should use option and value of the config entry to
	 * perform an action (usually setting an internal variable to 
	 * an appropriate value).
	 * 
	 * @param <T> Type of the config entry (Integer, String etc.).
	 * @param entry A configuration entry.
	 * @throws InvalidConfigOptionValueException This exception is thrown if the
	 * value of the config entry is not valid. For instance, a config option
	 * may only accept values, which are within intervals 0.1 to 0.3 or 0.5 to 0.8.
	 * If the value is outside of those intervals, an exception is thrown. Note
	 * that many of the common cases are already caught in the constructor of
	 * ConfigEntry (for instance for a {@link DoubleConfigOption} you can specify
	 * an interval for the value). This means that, as a component developer, you
	 * often do not need to implement further validity checks.  
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
