/**
 * 
 */
package org.dllearner.configuration.spring.editors;

import java.beans.PropertyEditorSupport;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractPropertyEditor<T> extends PropertyEditorSupport {
	
	protected T value;
	
	@Override
	public void setValue(Object value) {
		this.value = (T) value;
	}

	@Override
	public Object getValue() {
		return value;
	}

}
