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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.LearningManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.LearningManager.LearningMode;
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
	private JRadioButton noLearningButton;
	
	private JPanel currentPanel;
	private JPanel manualLearnPanel;
	private JPanel autoLearnPanel;
	
	private static final String HELP_TEXT = "<html>You can choose whether you want to learn class " +
			"descriptions for a single, manually selected class,<br>" +
			"or you can learn class descriptions step by step for all classes with a minimum " +
			"number of instances.</html>";
	
	private LearningOptionsPanel learningOptionsPanel;
	
	/**
	 * Constructor.
	 */
	public ClassChoosePanel() {
		createUI();
	}
	
	private void createUI(){
		setLayout(new BorderLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		JPanel optionsPanel = new JPanel(new GridLayout(0, 1));
		
		autoLearnButton = new JRadioButton("Automatic learning mode");
		autoLearnButton.setActionCommand("auto");
		
		manualLearnButton = new JRadioButton("Manual learning mode");
		manualLearnButton.setActionCommand("manual");
		
		noLearningButton = new JRadioButton("Skip learning");
		noLearningButton.setActionCommand("skip");
		
		
		ButtonGroup learningType = new ButtonGroup();
		learningType.add(manualLearnButton);
		learningType.add(autoLearnButton);
		learningType.add(noLearningButton);
		autoLearnButton.setSelected(true);
		optionsPanel.add(autoLearnButton);
		optionsPanel.add(manualLearnButton);
		optionsPanel.add(noLearningButton);
		
		HelpablePanel optionsHelpPanel = new HelpablePanel(optionsPanel);
		optionsHelpPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		optionsHelpPanel.setHelpText(HELP_TEXT);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(optionsHelpPanel, BorderLayout.NORTH);
		
		createAutoLearnPanel();
		createManualLearnPanel();
		
		Dimension size = new Dimension(500, 500);
		autoLearnPanel.setPreferredSize(size);
		manualLearnPanel.setPreferredSize(size);
		
		currentPanel = autoLearnPanel;
		add(currentPanel, BorderLayout.CENTER);
		
	}
	
	private void createAutoLearnPanel(){
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		JPanel minInstancesCountPanel = new JPanel();
		minInstancesCountPanel.add(new JLabel("Min. instance count per class: "));
		minInstanceCountSpinner = new JSpinner();
		minInstanceCountSpinner.setEnabled(true);
	    javax.swing.SpinnerModel spinnerModel = new SpinnerNumberModel(3, 1, 500, 1);
	    minInstanceCountSpinner.setModel(spinnerModel);
	    minInstancesCountPanel.add(minInstanceCountSpinner);
	    panel.add(minInstancesCountPanel, c);
	    
	    learningOptionsPanel = new LearningOptionsPanel();
		panel.add(learningOptionsPanel, c);
		
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
	    javax.swing.SpinnerModel spinnerModel = new SpinnerNumberModel(3, 1, 500, 1);
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
		noLearningButton.addActionListener(aL);
	}
	
	public void refreshLearningPanel(){
		LearningMode mode = LearningManager.getInstance().getLearningMode();
		if(mode == LearningMode.AUTO){
			remove(manualLearnPanel);
			add(autoLearnPanel);
		} else if(mode == LearningMode.MANUAL){
			remove(autoLearnPanel);
			add(manualLearnPanel);
		} else {
			remove(autoLearnPanel);
			remove(manualLearnPanel);
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
    	minInstanceCountSpinner.setValue(Integer.valueOf(3));
    	autoLearnButton.setSelected(true);
    	LearningManager.getInstance().setLearningMode(LearningMode.AUTO);
    	refreshLearningPanel();
    }
    
    public boolean isAutoLearnMode(){
    	return autoLearnButton.isSelected();
    }
    
    public void setLearningSupported(boolean value){
    	if(!value){
    		autoLearnButton.setEnabled(false);
    		manualLearnButton.setEnabled(false);
    		noLearningButton.setSelected(true);
    	} else {
    		autoLearnButton.setEnabled(true);
    		manualLearnButton.setEnabled(true);
    	}
    }
    
    public void setLearningOptions(){
    	LearningManager.getInstance().setMaxExecutionTimeInSeconds(learningOptionsPanel.getMaxExecutionTime());
    	LearningManager.getInstance().setMaxNrOfResults(learningOptionsPanel.getNrOfConcepts());
    	LearningManager.getInstance().setNoisePercentage(learningOptionsPanel.getMinAccuracy());
    	LearningManager.getInstance().setThreshold(learningOptionsPanel.getThreshold());
    	LearningManager.getInstance().setMinInstanceCount(((Integer)(minInstanceCountSpinner.getValue())).intValue());
    	
    }
    
    public static void main(String[] args){
		JFrame frame = new JFrame();
		
		
		frame.add(new ClassChoosePanel());
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}