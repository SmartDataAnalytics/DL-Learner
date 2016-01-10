package org.dllearner.test.junit;

import java.util.Collection;

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
	 */
	@Test
	public void nameTest() {
		AnnComponentManager cm = AnnComponentManager.getInstance();
		Collection<Class<? extends Component>> components = cm.getComponents();
		for (Class<? extends Component> component : components) {
			AnnComponentManager.getName(component);
		}
	}

}
