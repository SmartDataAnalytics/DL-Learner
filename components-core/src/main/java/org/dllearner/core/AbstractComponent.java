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

import java.util.Collection;
import java.util.LinkedList;

import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;

/**
 * Base class of all components. See also http://dl-learner.org/wiki/Architecture.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class AbstractComponent implements Component {
	
//	protected Configurator configurator;
	
	/**
	 * For each component, a configurator class is generated in package
	 * org.dllearner.core.configurators using the script 
	 * { org.dllearner.scripts.ConfigJavaGenerator}. The configurator
	 * provides set and get methods for the configuration options of 
	 * a component.
	 * @return An object allowing to configure this component.
	 */
//	public abstract Configurator getConfigurator();
	
	/**
	 * Returns the name of this component. By default, "unnamed 
	 * component" is returned, but all implementations of components
	 * are strongly encouraged to provide a static method returning 
	 * the name.
	 * 
	 * Use the DLComponent annotation instead of setting a name through this method.
	 * 
	 * @return The name of this component.
	 */
	@Deprecated
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
	 * Applies a configuration option to this component. Implementations
	 * of components should use option and value of the config entry to
	 * perform an action (usually setting an internal variable to 
	 * an appropriate value).
	 * 
	 * Since the availability of configurators, it is optional for 
	 * components to implement this method. Instead of using this method
	 * to take an action based on a configuration value, components can
	 * also use the getters defined in the components configurator. 
	 * 
	 * Important note: Never call this method directly. All calls are
	 * done via the {@link org.dllearner.core.ComponentManager}.
	 * 
	 * @see #getConfigurator()
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
	protected <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		
	}
	
}
