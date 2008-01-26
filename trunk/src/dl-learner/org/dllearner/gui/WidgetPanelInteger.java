package org.dllearner.gui;

/**
 * Copyright (C) 2007, Jens Lehmann
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

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;

import org.dllearner.core.Component;
//import org.dllearner.core.ComponentManager;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;


/**
 * WidgetPanel
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelInteger extends JPanel implements ActionListener {

	private static final long serialVersionUID = -1802111225835164644L;

	private Config config;
	private ConfigOption<?> configOption;
	private JLabel nameLabel;
	private JPanel centerPanel = new JPanel();
	private JButton setButton = new JButton("Set");
	private Component component;
	private Class<? extends Component> componentOption;
		
	public WidgetPanelInteger(Config config, Component component, Class<? extends Component> componentOption, ConfigOption<?> configOption) {
		this.config = config;
		this.configOption = configOption;
		this.component = component;
		this.componentOption = componentOption;
		
		System.out.println("1st: " + component);
		
		// default
		nameLabel = new JLabel(configOption.getName());
		setButton.addActionListener(this);
		
		// IntegerConfigOption
		if (configOption.toString().contains("IntegerConfigOption")) {
			JTextField integerField = new JTextField(3);
			integerField.setText("100"); 
			System.out.println(configOption.getDefaultValue());
			centerPanel.add(nameLabel);
			centerPanel.add(integerField);
			centerPanel.add(setButton);
		}
		// UNKNOWN
		else {
			nameLabel = new JLabel(configOption.getName());
			JLabel notImplementedLabel = new JLabel("not an integer");
			notImplementedLabel.setForeground(Color.RED);
			centerPanel.add(nameLabel);
			centerPanel.add(notImplementedLabel);
		}
		
		// default
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public JPanel getPanel() {
		return this;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			// INTEGER
			Integer value = 10;
			IntegerConfigOption specialOption;
			String name = configOption.getName();
			//Component component = config.getLearningAlgorithm();
			System.out.println("name: " + name + " & value: " + value);
			specialOption = (IntegerConfigOption) config.getComponentManager().getConfigOption(componentOption, name);
			try {
				ConfigEntry<Integer> specialEntry = new ConfigEntry<Integer>(specialOption, value);
				System.out.println("TEST specialEntry: " + specialEntry);
				System.out.println("TEST component: " + component);
				config.getComponentManager().applyConfigEntry(component, specialEntry);
			}
			catch (InvalidConfigOptionValueException s) {
				s.printStackTrace();
			}
		}
	}
	
	public void setComponent (Component component) {
		this.component = component;
	}
	
	public void setComponentOption (Class<? extends Component> componentOption) {
		this.componentOption = componentOption;
	}
}
