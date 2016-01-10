package org.dllearner.core;



/**
 * Base class of all components. See also http://dl-learner.org/wiki/Architecture.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class AbstractComponent implements Component {
	
	protected boolean initialized = false;
	
	/**
	 * @return true if component has been initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}
	
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
	
}
