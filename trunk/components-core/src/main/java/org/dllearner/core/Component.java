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

/**
 * Base interface of all components. See also http://dl-learner.org/wiki/Architecture.
 * 
 * @author Jens Lehmann
 *
 */
public interface Component {

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
	
}
