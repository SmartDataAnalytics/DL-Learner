package org.dllearner.gui;

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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.DoubleConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;

/**
 * Panel for option Double, defined in
 * org.dllearner.core.config.DoubleConfigOption.
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelDouble extends WidgetPanelAbstract implements
	ActionListener {

    private static final long serialVersionUID = 5238903690721116289L;
    private Config config;
    private ConfigOption<?> configOption;
    private JLabel nameLabel;
    private JPanel widgetPanel = new JPanel();
    private JButton setButton = new JButton("Set");
    private Component component;
    private Component oldComponent;
    private Class<? extends Component> componentOption;

    private Double value;
    private JTextField doubleField = new JTextField(5);

    public WidgetPanelDouble(Config config, Component component,
	    Component oldComponent, Class<? extends Component> componentOption,
	    ConfigOption<?> configOption) {
	this.config = config;
	this.configOption = configOption;
	this.component = component;
	this.oldComponent = oldComponent;
	this.componentOption = componentOption;

	showLabel(); // name of option and tooltip
	showThingToChange(); // textfield, setbutton
	add(widgetPanel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == setButton) {
	    setEntry();
	}
    }

    @Override
    public void showLabel() {
	nameLabel = new JLabel(configOption.getName());
	nameLabel.setToolTipText(configOption.getDescription());
	widgetPanel.add(nameLabel);
    }

    @Override
    public void showThingToChange() {
	if (component != null) {
	    // DoubleConfigOption
	    if (configOption.getClass().toString().contains(
		    "DoubleConfigOption")) {
		// previous set value
		if (configOption != null) {
		    value = (Double) config.getComponentManager()
			    .getConfigOptionValue(component,
				    configOption.getName());
		}
		// previous set value from old
		if (component != null && oldComponent != null) {
		    if (oldComponent.getClass().equals(component.getClass())) {
			value = (Double) config.getComponentManager()
				.getConfigOptionValue(oldComponent,
					configOption.getName());
			if (value == null)
			    value = 0.0;
			else {
			    doubleField.setText(value.toString());
			    setEntry();
			}
		    }
		}
		// default value
		else if (configOption.getDefaultValue() != null) {
		    value = (Double) configOption.getDefaultValue();
		}
		// value == null
		if (value == null) {
		    value = 0.0;
		}
		doubleField.setText(value.toString());
		doubleField.setToolTipText(configOption
			.getAllowedValuesDescription());
		setButton.addActionListener(this);
		widgetPanel.add(doubleField);
		widgetPanel.add(setButton);
	    }
	    // UNKNOWN
	    else {
		JLabel notImplementedLabel = new JLabel("not a double");
		notImplementedLabel.setForeground(Color.RED);
		widgetPanel.add(notImplementedLabel);
	    }
	} else { // configOption == NULL
	    JLabel noConfigOptionLabel = new JLabel("no instance (Double)");
	    noConfigOptionLabel.setForeground(Color.MAGENTA);
	    widgetPanel.add(noConfigOptionLabel);
	}
    }

    @Override
    public void setEntry() {
	DoubleConfigOption specialOption;
	value = Double.parseDouble(doubleField.getText()); // get from input
	specialOption = (DoubleConfigOption) config.getComponentManager()
		.getConfigOption(componentOption, configOption.getName());
	try {
	    ConfigEntry<Double> specialEntry = new ConfigEntry<Double>(
		    specialOption, value);
	    config.getComponentManager().applyConfigEntry(component,
		    specialEntry);
	    System.out.println("set Double: " + configOption.getName() + " = "
		    + value);
	} catch (InvalidConfigOptionValueException s) {
	    s.printStackTrace();
	}
    }
}
