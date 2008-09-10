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
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.dllearner.core.Component;
import org.dllearner.core.config.URLConfigOption;
import org.dllearner.gui.Config;
import org.dllearner.gui.ExampleFileChooser;
import org.dllearner.kb.OWLFile;

/**
 * Widget panel for URLs.
 * 
 * @author Jens Lehmann
 *
 */
public class WidgetPanelURL extends AbstractWidgetPanel<URL> implements ActionListener {

	private static final long serialVersionUID = -2169739820989891226L;

	private JButton setButton;
	private JButton chooseLocalButton;

	private URL value;
	private JTextField stringField;

	public WidgetPanelURL(Config config, Component component, URLConfigOption configOption) {
		super(config, component, configOption);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == chooseLocalButton) {
//			String stringValue;
			JFileChooser fc;
			if(component instanceof OWLFile) {
				fc = new ExampleFileChooser("owl");
			} else {
				fc = new ExampleFileChooser("kb");
			}	
			
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				stringValue = fc.getSelectedFile().toString();
//				stringField.setText(stringValue);
				try {
					// get file URI, add it into text field and fire a
					// value changed event
					value = fc.getSelectedFile().toURI().toURL();
					stringField.setText(value.toString());
					fireValueChanged(value);
				} catch (MalformedURLException e1) {
					// should never happen, because an actual file was selected
					e1.printStackTrace();
				}				
			}
		} else if(e.getSource() == setButton) {
			String stringValue = stringField.getText();
			try {
				value = new URL(stringValue);
			} catch (MalformedURLException e1) {
				// TODO add error handling
				e1.printStackTrace();
			}
			fireValueChanged(value);
		}
	}

	@Override
	public void buildWidgetPanel() {
		add(getLabel());

		// get current value of this option for the given component
		value = config.getConfigOptionValue(component, configOption);
		// default values can be null, so we interpret this as empty string
//		if (value == null) {
//			value = "";
//		}		
		
		// text field for strings
		stringField = new JTextField(35);
		if(value != null)
			stringField.setText(value.toString());
		stringField.setToolTipText(configOption.getAllowedValuesDescription());
		
		// set button (value is only updated when this button is pressed => would better without set)
		setButton = new JButton("Set");
		setButton.addActionListener(this);
		
		chooseLocalButton = new JButton("Choose Local File");
		chooseLocalButton.addActionListener(this);
		
		add(stringField);
		add(setButton);
		add(new JLabel(" or "));
		add(chooseLocalButton);
		
	}

}
