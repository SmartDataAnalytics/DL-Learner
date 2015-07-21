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

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

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

	/**
	 * Registers a component instance in the pool.
	 * @param component The component to add to the pool.
	 */
	public void registerComponent(AbstractComponent component) {
		components.add(component);
		logger.debug("Component instance " + component + " added to component pool.");
	}

	/**
	 * Unregisters a component instance. This method should be used if the
	 * component will not be used anymore. It frees the memory for
	 * storing the component and its configuration options.
	 * @param component The component to remove from the pool.
	 */
	public void unregisterComponent(AbstractComponent component) {
		components.remove(component);
		logger.debug("Component instance " + component + " removed from component pool.");
	}

	/**
	 * Unregisters all components.
	 */
	protected void clearComponents() {
		components = new LinkedList<AbstractComponent>();
	}
	
	/**
	 * @return The components in this pool.
	 */
	public List<AbstractComponent> getComponents(){
		return components;
	}

}
