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

package org.dllearner.core.options;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentManager;

/**
 * @author Jens Lehmann
 *
 */
public class UnknownConfigOptionException extends Exception {

	private static final long serialVersionUID = -7808637210577591687L;

	public UnknownConfigOptionException(Class<? extends AbstractComponent> componentClass, String optionName) {
		super("Option " + optionName + " unknown in component " + ComponentManager.getInstance().getComponentName(componentClass) + "(" + componentClass.getName() + ")");
	}
	
	public UnknownConfigOptionException(Class<? extends AbstractComponent> componentClass, ConfigOption<?> option) {
		super("Option " + option.getName() + " unknown in component " + ComponentManager.getInstance().getComponentName(componentClass) + "(" + componentClass.getName() + ")");
	}
	
}
