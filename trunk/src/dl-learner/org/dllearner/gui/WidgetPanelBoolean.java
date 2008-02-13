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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.Set;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;

/**
 * Panel for option Boolean, defined in
 * org.dllearner.core.config.BooleanConfigOption.
 * 
 * @author Tilo Hielscher
 */
public class WidgetPanelBoolean extends WidgetPanelAbstract implements
	ActionListener {

    private static final long serialVersionUID = -4800583253223939928L;
    private Config config;
    private ConfigOption<?> configOption;
    private JLabel nameLabel;
    private JPanel widgetPanel = new JPanel();
    private Component component;
    private Set<Component> oldComponentSet;
    private Class<? extends Component> componentOption;
    private Boolean value;
    private String[] kbBoxItems = { "false", "true" };
    private JComboBox cb = new JComboBox(kbBoxItems);

    public WidgetPanelBoolean(Config config, Component component,
	    Set<Component> oldComponentSet,
	    Class<? extends Component> componentOption,
	    ConfigOption<?> configOption) {
	this.config = config;
	this.configOption = configOption;
	this.component = component;
	this.oldComponentSet = oldComponentSet;
	this.componentOption = componentOption;

	showLabel(); // name of option and tooltip
	showThingToChange(); // textfield, setbutton
	add(widgetPanel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {

	setEntry();
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
	    // BooleanConfigOption
	    if (configOption.getClass().toString().contains(
		    "BooleanConfigOption")) {
		// previous set value
		if (configOption != null) {
		    value = (Boolean) config.getComponentManager()
			    .getConfigOptionValue(component,
				    configOption.getName());
		}
		// previous set value from old
		/*
		 * if (component != null && componentOld != null) { if
		 * (component.getClass().equals(componentOld.getClass())) {
		 * value = (Boolean) config.getComponentManager()
		 * .getConfigOptionValue(componentOld, configOption.getName());
		 */
		if (component != null && oldComponentSet != null) {
		    if (oldComponentSet.contains(component)) {
			System.out.println("oldComponentSet: "
				+ oldComponentSet);
			// value = (Boolean)
			// config.getComponentManager().getConfigOptionValue(componentOld,
			// configOption.getName());

			// set cb-index
			if (value == false)
			    cb.setSelectedIndex(0);
			else
			    cb.setSelectedIndex(1);
			setEntry();
		    }
		}
		// default value
		if (value != null && configOption.getDefaultValue() != null) {
		    value = (Boolean) configOption.getDefaultValue();
		}
		// value == null?
		if (value == null) {
		    value = false;
		}
		// set cb-index
		if (value == false)
		    cb.setSelectedIndex(0);
		else
		    cb.setSelectedIndex(1);
		cb.addActionListener(this);

		widgetPanel.add(cb);
	    }
	    // UNKNOWN
	    else {
		JLabel notImplementedLabel = new JLabel("not a boolean");
		notImplementedLabel.setForeground(Color.RED);
		widgetPanel.add(notImplementedLabel);
	    }
	} else { // configOption == NULL
	    JLabel noConfigOptionLabel = new JLabel("no init (Boolean)");
	    noConfigOptionLabel.setForeground(Color.MAGENTA);
	    widgetPanel.add(noConfigOptionLabel);
	}
    }

    @Override
    public void setEntry() {
	BooleanConfigOption specialOption;
	if (cb.getSelectedIndex() == 0)
	    value = false;
	else
	    value = true;
	specialOption = (BooleanConfigOption) config.getComponentManager()
		.getConfigOption(componentOption, configOption.getName());
	try {
	    ConfigEntry<Boolean> specialEntry = new ConfigEntry<Boolean>(
		    specialOption, value);
	    config.getComponentManager().applyConfigEntry(component,
		    specialEntry);
	    System.out.println("set Boolean: " + configOption.getName() + " = "
		    + value);
	} catch (InvalidConfigOptionValueException s) {
	    s.printStackTrace();
	}
    }

}
