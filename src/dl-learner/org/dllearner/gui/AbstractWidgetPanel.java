/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;

/**
 * Abstract superclass of all widgets. Each widget has an associated component and configuration option,
 * for which it allows modification by the user. 
 * 
 * @author Jens Lehmann
 */
public abstract class AbstractWidgetPanel<T> extends JPanel {
	
	protected ConfigOption<T> configOption;
	protected Config config;
	protected Component component;
	
	/**
	 * Constructs a widget.
	 * @param config The status of all components and options (which may be updated by this widget).
	 * @param component The component potentially changed by this widget.
	 * @param optionOption The config option of the specified component, which is potentially changed by this widget.
	 */
	public AbstractWidgetPanel(Config config, Component component, ConfigOption<T> optionOption) {
		this.config = config;
		this.component = component;
		this.configOption = optionOption;
		
		if(configOption == null || component == null || config == null) {
			System.out.println("| " + component + ", " + configOption + ", " + config + " |");
			throw new Error("Parameters must not be null.");
		}		
		
		buildWidgetPanel();
	}
	
	// convenience method
	protected JLabel getLabel() {
		JLabel nameLabel = new JLabel(configOption.getName());
		nameLabel.setToolTipText(configOption.getDescription());
		return nameLabel;
	}
	
	// subclasses should call this method if a configuration option has changed
	public void fireValueChanged(T value) {
		ConfigEntry<T> entry = null;
		try {
			entry = new ConfigEntry<T>(configOption, value);
		} catch (InvalidConfigOptionValueException e) {
			// TODO display a message on the status bar (where the init
			// has been before)
			e.printStackTrace();
		}
		// notify config that a value has changed -> it decides what to do
		config.applyConfigEntry(component, entry);
	}
	
	// subclasses should use this method to build the graphical representation of the widgets
	public abstract void buildWidgetPanel();

}
