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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.options.URLConfigOption;
import org.dllearner.core.owl.NamedClass;
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

	private JComboBox comboBox;
	private List<NamedClass> classes;
	
	/**
	 * Provides a widget for URL options.
	 * 
	 * @param config
	 *            Central config handler.
	 * @param component
	 *            The component of this option.
	 * @param configOption
	 *            The option to configure.
	 */
	public WidgetPanelURL(Config config, AbstractComponent component, URLConfigOption configOption) {
		super(config, component, configOption);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == chooseLocalButton) {
			JFileChooser fc;
			if (component instanceof OWLFile) {
				fc = new ExampleFileChooser("owl","rdf","nt");
			} else {
				fc = new ExampleFileChooser("kb");
			}

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
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
		} else if (e.getSource() == setButton && !stringField.getText().isEmpty()) {
			String stringValue = stringField.getText();
			try {
				value = new URL(stringValue);
			} catch (MalformedURLException e1) {
				// TODO add error handling
				e1.printStackTrace();
			}
			fireValueChanged(value);
		} else if (e.getSource() == comboBox) {
			// pick the selected class
			NamedClass clazz = classes.get(comboBox.getSelectedIndex());
			try {
				value = new URL(clazz.getName());
			} catch (MalformedURLException e1) {
				// cannot happen
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
//		System.out.println(configOption + " " + value);
		
		// if the option value is an OWL class, we offer a dropdown box
		if(((URLConfigOption) configOption).refersToOWLClass()) {
			comboBox = new JComboBox();
			AbstractReasonerComponent reasoner = config.getReasoner();
			String baseURI = reasoner.getBaseURI();
			Map<String,String> prefixes = reasoner.getPrefixes();
			classes = new LinkedList<NamedClass>(reasoner.getNamedClasses());
			for(NamedClass clazz : classes) {
				comboBox.addItem(clazz.toManchesterSyntaxString(baseURI, prefixes));
			}
			comboBox.addActionListener(this);
			// selecting index 0 causes incorrect class to load !
			if(value == null) {
				comboBox.setSelectedIndex(0);
			} else {
				NamedClass valueNc = new NamedClass(value.toString());
				comboBox.setSelectedItem(valueNc.toManchesterSyntaxString(baseURI, prefixes));
				
			}
			add(comboBox);
		} else {
			// text field for strings
			stringField = new JTextField(35);
			if (value != null) {
				stringField.setText(value.toString());
			}
			stringField.setToolTipText(configOption.getAllowedValuesDescription());

			// set button (value is only updated when this button is pressed =>
			// would be better without set)
			setButton = new JButton("Set");
			setButton.addActionListener(this);

			add(stringField);
			add(setButton);

			// if the URL can refer to a file, we add the possibility to
			// choose a local file
			if (((URLConfigOption) configOption).refersToFile()) {
				chooseLocalButton = new JButton("Choose Local File");
				chooseLocalButton.addActionListener(this);
				add(new JLabel(" or "));
				add(chooseLocalButton);
			}			
		}
				
	}

}
