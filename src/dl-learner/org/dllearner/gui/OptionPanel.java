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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.config.*;

/**
 * OptionPanel
 * 
 * @author Tilo Hielscher
 * 
 */
public class OptionPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -3053205578443575240L;
	private Config config;
	private Class<? extends Component> componentOption;
	private List<ConfigOption<?>> optionList;
	private JButton tableButton;
	private JButton normalButton;
	private JButton readOptionsButton;
	private DefaultTableModel optionModel = new DefaultTableModel();
	private JTable optionTable = new JTable(optionModel);
	private JPanel startPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private Component component;

	public OptionPanel(Config config, Component component, Class<? extends Component> componentOption) {
		super(new BorderLayout());
		
		this.config = config;
		this.component = component;
		this.componentOption = componentOption;
		
		tableButton = new JButton("show options as table");
		tableButton.addActionListener(this);
	
		normalButton = new JButton("show normal");
		normalButton.addActionListener(this);
	
		readOptionsButton = new JButton("read set option");
		readOptionsButton.addActionListener(this);
		
		optionList = ComponentManager.getConfigOptions(componentOption);
	
		startPanel.add(tableButton);
		startPanel.add(normalButton);
		startPanel.add(readOptionsButton);
		
		add(startPanel, BorderLayout.PAGE_START);
		add(centerPanel, BorderLayout.CENTER);

		// set JTable
		optionModel.addColumn("name");
		optionModel.addColumn("default");
		optionModel.addColumn("class");
		optionModel.addRow(new Object[] {"name","default","class"}); // header
		optionTable.setSize(400, 400);
		optionTable.updateUI();
		
	}
	
	public void actionPerformed(ActionEvent e) {
		// show as table
		if (e.getSource() == tableButton) {
			optionList = ComponentManager.getConfigOptions(componentOption);
			
			// clear
			centerPanel.removeAll();
	
			// clear JTable
			for (int i=optionModel.getRowCount()-1; i>0; i--) { // from last to first
				optionModel.removeRow(i); 
			}
			// new JTable
			for (int i=0; i<optionList.size(); i++) {
				optionModel.addRow(new Object[] {optionList.get(i).getName(), optionList.get(i).getDefaultValue(),
						optionList.get(i).getClass().getSimpleName()});
			}
			// update graphic
			centerPanel.add(optionTable);
			centerPanel.updateUI();
		}
		// show normal
		if (e.getSource() == normalButton) {
			optionList = ComponentManager.getConfigOptions(componentOption); // get class for options

			// clear
			centerPanel.removeAll();
			
			//get a WidgetPanel Example
			// optionList is the list of possible options for componentOption
			// each option can be type of IntegerConfigOption, StringConfigOption and so on
			WidgetPanelInteger firstPanel = new WidgetPanelInteger(config, component, componentOption, optionList.get(0));
			centerPanel.add(firstPanel);
			 
			// update graphic
			centerPanel.updateUI();
		}
		// read set options
		if (e.getSource() == readOptionsButton) {
			System.out.println("setOptions: " + config.getComponentManager().getConfigOption(componentOption, optionList.get(0).getName()));
		}
	}
	
	public void setComponent (Component component) {
		this.component = component;
	}
	
	public void setComponentOption (Class<? extends Component> componentOption) {
		this.componentOption = componentOption;
	}
	
}
