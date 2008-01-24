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
//import java.util.List;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;

//import javax.swing.JTable;
//import javax.swing.table.DefaultTableModel;


import org.dllearner.core.config.ConfigOption;


/**
 * WidgetPanel
 * 
 * @author Tilo Hielscher
 * 
 */
public class WidgetPanelInteger extends JPanel implements ActionListener {

	private static final long serialVersionUID = -1802111225835164644L;

	//private Config config;
	private ConfigOption<?> configOption;
	private JLabel nameLabel;
	private JPanel centerPanel = new JPanel();
	private JButton setButton = new JButton("Set");

	
	public WidgetPanelInteger(Config config, ConfigOption<?> configOption) {
		//this.config = config;
		this.configOption = configOption;
		
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
			int number = 10;
			System.out.println(number);
			System.out.println(configOption);
		}
	}
}
