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

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Jens Lehmann
 *
 */
public abstract class Component {
	
	/**
	 * 
	 * @return The name of this component.
	 */
	public static String getName() {
		return "unnamed component";
	}
	
	/**
	 * Returns all configuration options supported by this component.
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		return new LinkedList<ConfigOption<?>>();
	}
	
	/**
	 * Applies a configuration option to this component.
	 * 
	 * @param entry A configuration entry.
	 */
	public abstract <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException;
}
