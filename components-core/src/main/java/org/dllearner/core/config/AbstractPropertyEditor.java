/**
 * 
 */
package org.dllearner.core.config;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractPropertyEditor<T> implements PropertyEditor{
	
	protected T value;
	
	@Override
	public void setValue(Object value) {
		this.value = (T) value;
	}

	@Override
	public Object getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(PropertyChangeListener arg0) {
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#getCustomEditor()
	 */
	@Override
	public Component getCustomEditor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#getJavaInitializationString()
	 */
	@Override
	public String getJavaInitializationString() {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#getTags()
	 */
	@Override
	public String[] getTags() {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#isPaintable()
	 */
	@Override
	public boolean isPaintable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#paintValue(java.awt.Graphics, java.awt.Rectangle)
	 */
	@Override
	public void paintValue(Graphics arg0, Rectangle arg1) {
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void removePropertyChangeListener(PropertyChangeListener arg0) {
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditor#supportsCustomEditor()
	 */
	@Override
	public boolean supportsCustomEditor() {
		return false;
	}

}
