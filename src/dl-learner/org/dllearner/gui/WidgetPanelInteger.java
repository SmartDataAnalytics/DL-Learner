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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.IntegerConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;

/**
 * Panel for option Integer, defined in
 * org.dllearner.core.config.IntegerConfigOption.
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelInteger extends AbstractWidgetPanel<Integer> implements ActionListener {

	private static final long serialVersionUID = -1802111225835164644L;

	private JButton setButton = new JButton("Set");

	private Integer value;
	private JTextField integerField = new JTextField(3);

	public WidgetPanelInteger(Config config, Component component, IntegerConfigOption configOption) {
		super(config, component, configOption);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			setEntry();
		}
	}

	public void setEntry() {
		IntegerConfigOption specialOption;
		value = Integer.parseInt(integerField.getText()); // get from input
		specialOption = (IntegerConfigOption) config.getComponentManager().getConfigOption(
				component.getClass(), configOption.getName());
		if (specialOption.isValidValue(value)) {
			try {
				ConfigEntry<Integer> specialEntry = new ConfigEntry<Integer>(specialOption, value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				// System.out.println("set Integer: " + configOption.getName() +
				// " = " + value);
			} catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
		} else
			System.out.println("Integer: not valid value");
	}

	@Override
	public void buildWidgetPanel() {
		add(getLabel());
		
		value = config.getConfigOptionValue(component, configOption);
		
		setButton = new JButton("Set");
		integerField = new JTextField(3);		
		if (value == null)
			value = 0;
		else {
			integerField.setText(value.toString());
			setEntry();
		}
		
		integerField.setText(value.toString());
		integerField.setToolTipText(configOption.getAllowedValuesDescription());
		setButton.addActionListener(this);
		add(integerField);
		add(setButton);		
	}
}
