package org.dllearner.core;

import javax.annotation.PostConstruct;

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
	 * re-throw occurring exception as ComponentInitException and
	 * giving an error message as well as the actually exception by
	 * using the constructor {@link ComponentInitException#ComponentInitException(String, Throwable)}.
	 */
	@PostConstruct
	void init() throws ComponentInitException;
	
}
