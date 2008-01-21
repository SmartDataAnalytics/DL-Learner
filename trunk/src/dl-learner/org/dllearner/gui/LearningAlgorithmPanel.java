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

import javax.swing.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.ComponentManager;



/**
 * LearningAlgorithmPanel
 * 
 * @author Tilo Hielscher
 * 
 */

public class LearningAlgorithmPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 8721490771860452959L;
    private Config config;
    private List<Class<? extends LearningAlgorithm>> learners;
	private JPanel choosePanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel initPanel = new JPanel();
    private JButton initButton, testButton;
    private String[] cbItems = {};
	private JComboBox cb = new JComboBox(cbItems);
	private int choosenClassIndex;
	private List<ConfigOption<?>> optionList;
	private DefaultTableModel optionModel = new DefaultTableModel();
	private JTable optionTable = new JTable(optionModel);
	
	
	
	LearningAlgorithmPanel(Config config) {
		super(new BorderLayout());

		this.config = config;
		
		initButton = new JButton("Init LearingAlgorithm");
		initButton.addActionListener(this);
		testButton = new JButton("Test");
		testButton.addActionListener(this);
		
		initPanel.add(initButton);
		initPanel.add(testButton);

		choosePanel.add(cb);

		centerPanel.add(optionTable);
		
		add(choosePanel, BorderLayout.PAGE_START);
		add(centerPanel, BorderLayout.CENTER);	
		add(initPanel, BorderLayout.PAGE_END);	
		
		// add into comboBox
		learners = config.getComponentManager().getLearningAlgorithms();
		for (int i=0; i<learners.size(); i++) {
			//cb.addItem(learners.get(i).getSimpleName());
			//System.out.println(learners.get(i).getSimpleName());
			cb.addItem(config.getComponentManager().getComponentName(learners.get(i)));
		}
		
		// set JTable
		optionModel.addColumn("name");
		optionModel.addColumn("default");
		optionModel.addColumn("class");

		// first row - where is header?
		optionModel.addRow(new Object[] {"name","default","class"});

		//optionTable.setSize(400, 400);
		//System.out.println("optionModel.getSize(): " + optionTable.getSize());
		optionTable.updateUI();
	}

	public void actionPerformed(ActionEvent e) {
		// read selected Class
        choosenClassIndex = cb.getSelectedIndex();
        
		if (e.getSource() == testButton) {
			// TEST
			//available options for selected class
			optionList = ComponentManager.getConfigOptions(learners.get(cb.getSelectedIndex()));
			//System.out.println(optionList + "\n");
			//System.out.println("option 0:\n" + optionList.get(0));
			//System.out.println("size: " + optionList.size() + "\n"); // size
/*			for (int i=0; i<optionList.size(); i++) {
				System.out.println("name: " + optionList.get(i).getName()); // name
				System.out.println("default value: " + optionList.get(i).getDefaultValue()); // default value
				System.out.println("class: " + optionList.get(i).getClass()); // class
				System.out.println("description: " + optionList.get(i).getDescription()); // description
				System.out.println("allowed value description: " + optionList.get(i).getAllowedValuesDescription()); // allowed value description
				System.out.println();
			}
*/			

			// show Options
			// clear JTable
			for (int i=optionModel.getRowCount()-1; i>0; i--) {
				// from last to first 
				optionModel.removeRow(i);
			}
			// new JTable
			for (int i=0; i<optionList.size(); i++) {
				optionModel.addRow(new Object[] {optionList.get(i).getName(), optionList.get(i).getDefaultValue(),
						optionList.get(i).getClass().getSimpleName()});
				// System.out.println("v2 name: " + optionList.get(i).getName()); // name

			}
			// update graphic
			centerPanel.updateUI();

		}

		// init
		if (e.getSource() == initButton) {
			if (config.getStatus(6)) {
				config.setLearningAlgorithm(config.getComponentManager().learningAlgorithm(learners.get(choosenClassIndex), config.getLearningProblem(), config.getReasoningService()));
				config.getLearningAlgorithm().init();
			}
			if (config.getStatus(5)) {  // examples are set
				System.out.println("LearningAlgorithm: " + config.getLearningAlgorithm() + "\n");
			}
		}
	}
}
