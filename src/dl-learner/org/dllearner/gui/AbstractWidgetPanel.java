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
//import java.util.List;

//import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JTable;
//import javax.swing.table.DefaultTableModel;

//import org.dllearner.core.Component;
//import org.dllearner.core.ComponentManager;
//import org.dllearner.core.config.ConfigOption;
//import org.dllearner.core.ComponentManager;
import org.dllearner.core.config.ConfigOption;


/**
 * AbstractWidgetPanel
 * 
 * @author Tilo Hielscher
 * 
 */
public class AbstractWidgetPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -1802111225835164644L;

	//private Config config;
	private ConfigOption<?> configOption;
	private JLabel nameLabel;
	private JPanel centerPanel = new JPanel();
	
	public AbstractWidgetPanel(Config config, ConfigOption<?> configOption) {
		super(new BorderLayout());
		//this.config = config;
		this.configOption = configOption;
		
		//show info
		nameLabel = new JLabel(this.configOption.getName());
		centerPanel.add(nameLabel);
		
		//layout
		add(centerPanel, BorderLayout.CENTER);
		
		
	}
	
	public void actionPerformed(ActionEvent e) {
		
	}
}
