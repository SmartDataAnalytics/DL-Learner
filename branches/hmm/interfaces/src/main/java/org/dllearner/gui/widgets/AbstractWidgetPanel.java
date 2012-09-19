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
package org.dllearner.gui.widgets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.gui.Config;

/**
 * Abstract superclass of all widgets. Each widget has an associated component and configuration option,
 * for which it allows modification by the user. 
 * 
 * @author Jens Lehmann
 * @param <T> The type of the value, which is configured by this
 * widget, e.g. String, Integer etc.
 */
public abstract class AbstractWidgetPanel<T> extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3631056807739752782L;

	/**
	 * The configuration option configured by this widget.
	 */
	protected ConfigOption<T> configOption;
	
	/**
	 * The central configuration handler.
	 */
	protected Config config;
	
	/**
	 * The component of the configured option.
	 */
	protected AbstractComponent component;
	
	/**
	 * Constructs a widget.
	 * @param config The status of all components and options (which may be updated by this widget).
	 * @param component The component potentially changed by this widget.
	 * @param optionOption The config option of the specified component, which is potentially changed by this widget.
	 */
	public AbstractWidgetPanel(Config config, AbstractComponent component, ConfigOption<T> optionOption) {
		this.config = config;
		this.component = component;
		this.configOption = optionOption;
		
		if(configOption == null || component == null || config == null) {
			System.out.println("| " + component + ", " + configOption + ", " + config + " |");
			throw new Error("Parameters must not be null.");
		}		
		
		buildWidgetPanel();
	}
	
	/**
	 * Convenience method returning a JLabel with the name of
	 * the option and its description as tooltip.
	 * @return The described JLabel.
	 */
	protected JLabel getLabel() {
		JLabel nameLabel = new JLabel(configOption.getName());
		nameLabel.setToolTipText(configOption.getDescription());
		return nameLabel;
	}
	
	/**
	 * Subclasses should call this method if a configuration option
	 * has changed. It redirects the event to the central configuration
	 * handler, which in turn may update the gui (enabling/disabling
	 * tabs, setting status messages etc.)
	 * @param value The new value for the configuration option
	 * configured by this widget.
	 */
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
	
	/**
	 * Subclasses should use this method to build the graphical representation of the widgets.
	 */
	public abstract void buildWidgetPanel();

}
