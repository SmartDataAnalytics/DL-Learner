package org.dllearner.gui.widgets;

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
import org.dllearner.core.config.DoubleConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.gui.Config;

/**
 * Panel for option Double, defined in
 * {@link org.dllearner.core.config.DoubleConfigOption}.
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelDouble extends AbstractWidgetPanel<Double> implements ActionListener {

	private static final long serialVersionUID = 5238903690721116289L;

	private JButton setButton = new JButton("Set");

//	private Class<? extends Component> componentOption;

	private Double value;
	private JTextField doubleField = new JTextField(5);

	public WidgetPanelDouble(Config config, Component component, DoubleConfigOption configOption) {
		super(config, component, configOption);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			setEntry();
		}
	}

	public void setEntry() {
		DoubleConfigOption specialOption;
		value = Double.parseDouble(doubleField.getText()); // get from input
		specialOption = (DoubleConfigOption) config.getComponentManager().getConfigOption(
				component.getClass(), configOption.getName());
		if (specialOption.isValidValue(value)) {
			try {
				ConfigEntry<Double> specialEntry = new ConfigEntry<Double>(specialOption, value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				// System.out.println("set Double: " + configOption.getName() +
				// " = " + value);
			} catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
		} else
			System.out.println("Double: not valid value");
	}

	@Override
	public void buildWidgetPanel() {
		add(getLabel());

		value = config.getConfigOptionValue(component, configOption);
		
		setButton = new JButton("Set");
		doubleField = new JTextField(5);
		if (value == null)
			value = 0.0;
		else {
			doubleField.setText(value.toString());
			setEntry();
		}		
		
		doubleField.setText(value.toString());
		doubleField.setToolTipText(configOption.getAllowedValuesDescription());
		setButton.addActionListener(this);
		add(doubleField);
		add(setButton);		
		
	}
}
