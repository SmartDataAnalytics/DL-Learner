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
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import org.dllearner.core.Component;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.gui.Config;
import org.dllearner.gui.ExampleFileChooser;
import org.dllearner.kb.OWLFile;

/**
 * Panel for option String, defined in
 * {@link org.dllearner.core.config.StringConfigOption}.
 * 
 * @author Jens Lehmann
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelString extends AbstractWidgetPanel<String> implements ActionListener {

	private static final long serialVersionUID = -2169739820989891226L;

//	private JPanel widgetPanel = new JPanel();
	private JButton setButton; // = new JButton("Set");

	private String value;
	private JTextField stringField; // = new JTextField(35);

	public WidgetPanelString(Config config, Component component, StringConfigOption configOption) {
		super(config, component, configOption);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			if (configOption.getName().equals("filename") || configOption.getName().equals("url")) {
				JFileChooser fc;
				if(component instanceof OWLFile) {
					fc = new ExampleFileChooser("owl");
				} else {
					fc = new ExampleFileChooser("kb");
				}	
				
				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					value = fc.getSelectedFile().toString();
					stringField.setText(value);
				}
			}
			value = stringField.getText();
			fireValueChanged(value);
//			setEntry();
			// if url and value not ""
			// necessary for init knowledge source
			if (configOption.getName().equalsIgnoreCase("url") && !value.equalsIgnoreCase("")) {
			}
		}
	}

	/*
	public void setEntry() {
		StringConfigOption specialOption;
		value = stringField.getText(); // get from input
		specialOption = (StringConfigOption) config.getComponentManager().getConfigOption(
				componentOption, configOption.getName());
		if (specialOption.isValidValue(value)) {
			try {
				ConfigEntry<String> specialEntry = new ConfigEntry<String>(specialOption, value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				// System.out.println("set String: " + configOption.getName() +
				// " = " + value);
			} catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
		} else
			System.out.println("String: not valid value");
	}*/

	@Override
	public void buildWidgetPanel() {
		add(getLabel());

		// get current value of this option for the given component
		value = config.getConfigOptionValue(component, configOption);
		// default values can be null, so we interpret this as empty string
		if (value == null) {
			value = "";
		}		
		
		// text field for strings
		stringField = new JTextField(35);
		stringField.setText(value);
		stringField.setToolTipText(configOption.getAllowedValuesDescription());
		
		// set button (value is only updated when this button is pressed => would better without set)
		setButton = new JButton("Set");
		setButton.addActionListener(this);
		
		add(stringField);
		add(setButton);
		
		// special handling for filename option
		if (configOption.getName().equals("filename"))
			setButton.setText("choose local file");
		
	}

}
