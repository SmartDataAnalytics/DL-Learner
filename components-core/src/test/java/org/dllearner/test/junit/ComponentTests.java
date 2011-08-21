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

import java.util.List;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentManager;
import org.junit.Test;
import static org.junit.Assert.*;

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
	 */
	@Test
	public void nameTest() {
		String defaultName = AbstractComponent.getName();
		ComponentManager cm = ComponentManager.getInstance();
		List<Class<? extends AbstractComponent>> components = cm.getComponents();
		for (Class<? extends AbstractComponent> component : components) {
			String componentName = cm.getComponentName(component);
			assertFalse(component + " does not overwrite getName().", componentName
					.equals(defaultName));
		}
	}

}
