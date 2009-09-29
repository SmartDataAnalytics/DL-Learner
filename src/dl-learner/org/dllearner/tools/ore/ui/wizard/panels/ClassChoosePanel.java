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

package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.ui.ClassesTable;
import org.dllearner.tools.ore.ui.HelpablePanel;
import org.dllearner.tools.ore.ui.LearningOptionsPanel;

/**
 * Wizard panel where atomic classes are shown in list.
 * @author Lorenz Buehmann
 *
 */
public class ClassChoosePanel extends JPanel{

	private static final long serialVersionUID = 3026319637264844550L;

	private ClassesTable classesTable;
	private JSpinner minInstanceCountSpinner;
	private JRadioButton autoLearnButton;
	private JRadioButton manualLearnButton;
	
	private JPanel currentPanel;
	private JPanel manualLearnPanel;
	private JPanel autoLearnPanel;
	
	private LearningOptionsPanel learningOptionsPanel;
	
	/**
	 * Constructor.
	 */
	public ClassChoosePanel() {
		createUI();
	}
	
	private void createUI(){
		setLayout(new GridBagLayout());
		JPanel optionsPanel = new JPanel(new GridLayout(0, 1));
		autoLearnButton = new JRadioButton("Automatic learning mode");
		autoLearnButton.setActionCommand("auto");
		manualLearnButton = new JRadioButton("Manual learning mode");
		manualLearnButton.setActionCommand("manual");
		ButtonGroup learningType = new ButtonGroup();
		learningType.add(manualLearnButton);
		learningType.add(autoLearnButton);
		autoLearnButton.setSelected(true);
		optionsPanel.add(autoLearnButton);
		optionsPanel.add(manualLearnButton);
		HelpablePanel optionsHelpPanel = new HelpablePanel(optionsPanel);
		optionsHelpPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		add(optionsHelpPanel);
		createAutoLearnPanel();
		createManualLearnPanel();
		currentPanel = autoLearnPanel;
		add(currentPanel);
		
	}
	
	private void createAutoLearnPanel(){
		JPanel panel = new JPanel(new GridBagLayout());
		JPanel minInstancesCountPanel = new JPanel();
		minInstancesCountPanel.add(new JLabel("Min. instance count per class: "));
		minInstanceCountSpinner = new JSpinner();
		minInstanceCountSpinner.setEnabled(true);
	    javax.swing.SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 500, 1);
	    minInstanceCountSpinner.setModel(spinnerModel);
	    minInstancesCountPanel.add(minInstanceCountSpinner);
	    panel.add(minInstancesCountPanel);
	    learningOptionsPanel = new LearningOptionsPanel();
		panel.add(learningOptionsPanel);
		
		autoLearnPanel = panel;
	}
	
	private void createManualLearnPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		JPanel minInstancesCountPanel = new JPanel();
		minInstancesCountPanel.add(new JLabel("Showing classes with at least "));
		minInstanceCountSpinner = new JSpinner();
		minInstanceCountSpinner.setEnabled(true);
	    javax.swing.SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 500, 1);
	    minInstanceCountSpinner.setModel(spinnerModel);
	    minInstancesCountPanel.add(minInstanceCountSpinner);
	    minInstancesCountPanel.add(new JLabel(" instances"));
	    panel.add(minInstancesCountPanel, c);
		
		
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1.0;
		classesTable = new ClassesTable();
		JScrollPane scroll = new JScrollPane(classesTable);
		scroll.setPreferredSize(new Dimension(400, 400));
		panel.add(scroll, c);
		
		manualLearnPanel = panel;
	}
	
	/**
	 * Adds list selection listener to atomic classes table.
	 * @param l the default list selection listener
	 */
	public void addSelectionListener(ListSelectionListener l){
		classesTable.getSelectionModel().addListSelectionListener(l);
	}
	
	public void addChangeListener(ChangeListener cL){
		minInstanceCountSpinner.addChangeListener(cL);
	}
	
	public void addActionsListeners(ActionListener aL){
		autoLearnButton.addActionListener(aL);
		manualLearnButton.addActionListener(aL);
	}
	
	public void setAutoLearningPanel(boolean value){
		if(value){
			remove(manualLearnPanel);
			add(autoLearnPanel);
		} else {
			remove(autoLearnPanel);
			add(manualLearnPanel);
		}
		validate();
		repaint();
	}
        
	/**
	 * Returns the table where atomic owl classes are the table elements.
	 * @return instance of ClassesTable
	 */
    public ClassesTable getClassesTable(){
    	return classesTable;
    }
    
    public void reset(){
    	classesTable.clear();
    	minInstanceCountSpinner.setValue(new Integer(1));
    }
    
    public boolean isAutoLearnMode(){
    	return autoLearnButton.isSelected();
    }

}