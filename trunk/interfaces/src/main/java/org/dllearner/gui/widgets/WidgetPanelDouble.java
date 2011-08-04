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
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.gui.Config;

/**
 * Panel for option Double, defined in
 * {@link org.dllearner.core.options.DoubleConfigOption}.
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelDouble extends AbstractWidgetPanel<Double> implements ActionListener {

	private static final long serialVersionUID = 5238903690721116289L;

	private JButton setButton = new JButton("Set");
	private JLabel problemLabel;

//	private Class<? extends Component> componentOption;

	private Double value;
	private JTextField doubleField = new JTextField(5);

	/**
	 * Provides a widget for double options.
	 * @param config Central config handler.
	 * @param component The component of this option.
	 * @param configOption The option to configure.
	 */
	public WidgetPanelDouble(Config config, AbstractComponent component, DoubleConfigOption configOption) {
		super(config, component, configOption);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			// TODO need better way for double parsing than throwing an
			// exception
			try {
				value = Double.valueOf(doubleField.getText());
				fireValueChanged(value);
				problemLabel.setText("");
			} catch(NumberFormatException e1) {
				problemLabel.setText("Please enter a valid double value.");
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
		doubleField = new JTextField(5);
		if (value == null) {
			value = 0.0;
		} else {
			doubleField.setText(value.toString());
//			setEntry();
		}		
		
		doubleField.setText(value.toString());
		doubleField.setToolTipText(configOption.getAllowedValuesDescription());
		setButton.addActionListener(this);
		add(doubleField);
		add(setButton);		
		
	}
}
