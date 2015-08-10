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

package org.dllearner.test.junit;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.junit.Test;

/**
 * A suite of JUnit tests related to the DL-Learner component architecture.
 * 
 * @author Jens Lehmann
 * 
 */
public class ComponentTests {

	/**
	 * Checks whether all components implement the getName() method. While it
	 * cannot be enforced to implement a static method, it should be done (e.g.
	 * to be used as label for the component in GUIs).
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	@Test
	public void nameTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String defaultName = AbstractComponent.getName();
		AnnComponentManager cm = AnnComponentManager.getInstance();
		Collection<Class<? extends Component>> components = cm.getComponents();
		for (Class<? extends Component> component : components) {
			String componentName = (String) component.getMethod("getName").invoke(null);
			assertFalse(component + " does not overwrite getName().", componentName
					.equals(defaultName));
		}
	}

}
