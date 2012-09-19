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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.gui.Config;

/**
 * Panel for option String, defined in
 * {@link org.dllearner.core.options.StringConfigOption}.
 * 
 * @author Jens Lehmann
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelString extends AbstractWidgetPanel<String> implements ActionListener {

	private static final long serialVersionUID = -2169739820989891226L;

	private JButton setButton;

	private String value;
	private JTextField stringField;
	private JComboBox comboBox;

	/**
	 * Provides a widget for string options.
	 * @param config Central config handler.
	 * @param component The component of this option.
	 * @param configOption The option to configure.
	 */
	public WidgetPanelString(Config config, AbstractComponent component, StringConfigOption configOption) {
		super(config, component, configOption);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			// fire value changed event
			value = stringField.getText();
			fireValueChanged(value);
		} else if(e.getSource() == comboBox) {
			value = (String) comboBox.getSelectedItem();
			fireValueChanged(value);
		}
	}

	@Override
	public void buildWidgetPanel() {
		add(getLabel());

		// get current value of this option for the given component
		value = config.getConfigOptionValue(component, configOption);
		// default values can be null, so we interpret this as empty string
		if (value == null) {
			value = "";
		}

		StringConfigOption option = (StringConfigOption) configOption; 
		if(option.getAllowedValues().size() == 0) {
		
			// text field for strings
			stringField = new JTextField(35);
			stringField.setText(value);
			stringField.setToolTipText(configOption.getAllowedValuesDescription());
	
			// set button (value is only updated when this button is pressed =>
			// would better without set)
			setButton = new JButton("Set");
			setButton.addActionListener(this);
	
			add(stringField);
			add(setButton);
		
		// if there is a fixed set of strings available as options, we
		// only offer those
		} else {
			comboBox = new JComboBox(option.getAllowedValues().toArray());
			comboBox.setSelectedItem(option.getDefaultValue());
			comboBox.addActionListener(this);
			add(comboBox);
		}
	}

}
