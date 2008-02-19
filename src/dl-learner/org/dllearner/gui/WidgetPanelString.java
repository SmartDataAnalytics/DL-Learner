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
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.dllearner.core.Component;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;

/**
 * Panel for option String, defined in
 * org.dllearner.core.config.StringConfigOption.
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelString extends WidgetPanelAbstract implements ActionListener {

	private static final long serialVersionUID = -2169739820989891226L;
	private Config config;
	private ConfigOption<?> configOption;
	private JLabel nameLabel;
	private JPanel widgetPanel = new JPanel();
	private JButton setButton = new JButton("Set");
	private Component component;
	private Component oldComponent;
	private Class<? extends Component> componentOption;

	private String value;
	private JTextField stringField = new JTextField(35);

	public WidgetPanelString(Config config, Component component, Component oldComponent,
			Class<? extends Component> componentOption, ConfigOption<?> configOption) {
		this.config = config;
		this.configOption = configOption;
		this.component = component;
		this.oldComponent = oldComponent;
		this.componentOption = componentOption;

		showLabel(); // name of option and tooltip
		showThingToChange(); // textfield, setbutton
		add(widgetPanel, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			if (checkForFilename()) {
				// file dialog
				JFileChooser fc = new JFileChooser(new File("examples/"));
				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					value = fc.getSelectedFile().toString();
					stringField.setText(value);
				}
			}
			setEntry();
			// if url and value not ""
			// necessary for init knowledge source
			if (configOption.getName().equalsIgnoreCase("url") && !value.equalsIgnoreCase("")) {
			}
		}
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
			// StringConfigOption
			if (configOption.getClass().toString().contains("StringConfigOption")) {
				// previous set value
				if (configOption != null) {
					value = (String) config.getComponentManager().getConfigOptionValue(component,
							configOption.getName());
				}
				// previous set value from old
				if (component != null && oldComponent != null) {
					if (oldComponent.getClass().equals(component.getClass())) {
						value = (String) config.getComponentManager().getConfigOptionValue(
								oldComponent, configOption.getName());
						if (value != null) {
							stringField.setText(value.toString());
							setEntry();
						}
					}
				}
				// default value
				else if (configOption.getDefaultValue() != null) {
					value = (String) configOption.getDefaultValue();
				}
				// value == null
				if (value == null) {
					value = "";
				}
				stringField.setText(value.toString());
				stringField.setToolTipText(configOption.getAllowedValuesDescription());
				setButton.addActionListener(this);
				widgetPanel.add(stringField);
				widgetPanel.add(setButton);
				if (checkForFilename())
					setButton.setText("choose local file");
			}
			// UNKNOWN
			else {
				JLabel notImplementedLabel = new JLabel("not a string");
				notImplementedLabel.setForeground(Color.RED);
				widgetPanel.add(notImplementedLabel);
			}
		} else { // configOption == NULL
			JLabel noConfigOptionLabel = new JLabel("no instance (String)");
			noConfigOptionLabel.setForeground(Color.MAGENTA);
			widgetPanel.add(noConfigOptionLabel);
		}
	}

	@Override
	public void setEntry() {
		StringConfigOption specialOption;
		value = stringField.getText(); // get from input
		specialOption = (StringConfigOption) config.getComponentManager().getConfigOption(
				componentOption, configOption.getName());
		if (specialOption.isValidValue(value)) {
			try {
				ConfigEntry<String> specialEntry = new ConfigEntry<String>(specialOption, value);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
				System.out.println("set String: " + configOption.getName() + " = " + value);
			} catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
		} else
			System.out.println("String: not valid value");
	}

	/**
	 * Widget filename getName() == filename you should open a file dialog in
	 * ActionPerformed
	 */
	private Boolean checkForFilename() {
		return configOption.getName().equalsIgnoreCase("filename");
	}

}
