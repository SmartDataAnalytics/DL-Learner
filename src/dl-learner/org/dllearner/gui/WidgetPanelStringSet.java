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
import java.util.Set;

//import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.StringSetConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;

/**
 * WidgetPanelStringSet
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelStringSet extends AbstractWidgetPanel implements
	ActionListener {

    private static final long serialVersionUID = 7832726987046601916L;
    private Config config;
    private ConfigOption<?> configOption;
    private JLabel nameLabel;
    private JPanel widgetPanel = new JPanel();
    private JButton setButton = new JButton("Set");
    private Component component;
    private Class<? extends Component> componentOption;

    private Set<String> value;
    //private JTextField stringField = new JTextField(15);

    public WidgetPanelStringSet(Config config, Component component,
	    Class<? extends Component> componentOption,
	    ConfigOption<?> configOption) {
	this.config = config;
	this.configOption = configOption;
	this.component = component;
	this.componentOption = componentOption;

	showLabel(); // name of option and tooltip
	showThingToChange(); // textfield, setbutton
	add(widgetPanel, BorderLayout.CENTER);
    }

    public JPanel getPanel() {
	return this;
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == setButton) {
	    setEntry();
	}
    }

    @Override
    protected void showLabel() {
	nameLabel = new JLabel(configOption.getName());
	nameLabel.setToolTipText(configOption.getDescription());
	widgetPanel.add(nameLabel);
    }

    @Override
    protected void showThingToChange() {
	if (component != null) {
	    // StringSetConfigOption
	    if (configOption.getClass().toString().contains(
		    "StringSetConfigOption")) {
		setButton.addActionListener(this);
		//widgetPanel.add(stringField);
		widgetPanel.add(setButton);
	    }
	    // UNKNOWN
	    else {
		JLabel notImplementedLabel = new JLabel("not a stringSet");
		notImplementedLabel.setForeground(Color.RED);
		widgetPanel.add(notImplementedLabel);
	    }
	} else { // configOption == NULL
	    JLabel noConfigOptionLabel = new JLabel("no instance (StringSet)");
	    noConfigOptionLabel.setForeground(Color.MAGENTA);
	    widgetPanel.add(noConfigOptionLabel);
	}
	
	//System.out.println("value: " + value);

	
    }

    @Override
    protected void setEntry() {
	StringSetConfigOption specialOption;
	//value = stringField.getText(); // get from input
	specialOption = (StringSetConfigOption) config.getComponentManager()
		.getConfigOption(componentOption, configOption.getName());
	try {
	    ConfigEntry<Set<String>> specialEntry = new ConfigEntry<Set<String>>(
		    specialOption, value);
	    config.getComponentManager().applyConfigEntry(component,
		    specialEntry);
	    System.out.println("set String: " + configOption.getName() + " = "
		    + value);
	} catch (InvalidConfigOptionValueException s) {
	    s.printStackTrace();
	}

	
    }


}
