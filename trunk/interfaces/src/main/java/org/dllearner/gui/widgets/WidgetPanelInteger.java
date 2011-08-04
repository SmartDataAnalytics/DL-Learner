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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.gui.Config;

/**
 * Panel for option Integer, defined in
 * org.dllearner.core.config.IntegerConfigOption.
 * 
 * @author Tilo Hielscher
 * @author Jens Lehmann
 * 
 */
public class WidgetPanelInteger extends AbstractWidgetPanel<Integer> implements ActionListener {

	private static final long serialVersionUID = -1802111225835164644L;

	private JButton setButton; // = new JButton("Set");
	private JLabel problemLabel ; //= new JLabel();

	private Integer value;
	private JTextField integerField; // = new JTextField(3);

	/**
	 * Provides a widget for integer options.
	 * @param config Central config handler.
	 * @param component The component of this option.
	 * @param configOption The option to configure.
	 */
	public WidgetPanelInteger(Config config, AbstractComponent component, IntegerConfigOption configOption) {
		super(config, component, configOption);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {

			// TODO need better way for integer parsing than throwing an
			// exception
			try {
				value = Integer.valueOf(integerField.getText());
				fireValueChanged(value);
				problemLabel.setText("");
			} catch(NumberFormatException e1) {
				problemLabel.setText("Please enter a valid integer value.");
			}
		}
	}

	@Override
	public void buildWidgetPanel() {
		add(getLabel());
		problemLabel = new JLabel();
		problemLabel.setForeground(Color.RED);
		
		value = config.getConfigOptionValue(component, configOption);
		
		setButton = new JButton("Set");
		integerField = new JTextField(3);		
		if (value == null) {
			value = 0;
		} else {
			integerField.setText(value.toString());
//			setEntry();
		}
		
		integerField.setText(value.toString());
		integerField.setToolTipText(configOption.getAllowedValuesDescription());
		setButton.addActionListener(this);
		add(integerField);
		add(setButton);	
		add(problemLabel);
	}
}
