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


import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Wizard panel with introduction text.
 * @author Lorenz Buehmann
 *
 */
public class IntroductionPanel extends JPanel {
 
	private static final long serialVersionUID = 7184544803724152044L;
	
	
	private JTextArea instructionsField;
    private JScrollPane jScrollPane1;
        
    private JLabel welcomeTitle;

     
    
    
    public IntroductionPanel() {
    	setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridwidth = GridBagConstraints.REMAINDER;
    	c.fill = GridBagConstraints.HORIZONTAL;
    	c.weightx = 1.0;
    	c.anchor = GridBagConstraints.NORTH;
    	
    	
        //setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));
        
        welcomeTitle = new JLabel();
        welcomeTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));
        welcomeTitle.setFont(new java.awt.Font("MS Sans Serif", Font.BOLD, 14));
        welcomeTitle.setText("Welcome to the DL-Learner ORE-Tool!");
        add(welcomeTitle, c);
        
        jScrollPane1 = new JScrollPane();
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jScrollPane1.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        instructionsField = new JTextArea();
        Color color = UIManager.getColor("Panel.background");
        instructionsField.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue()));
        instructionsField.setOpaque(true);
        instructionsField.setColumns(20);
        instructionsField.setEditable(false);
        instructionsField.setLineWrap(true);
        instructionsField.setRows(5);    
        instructionsField.setFont(new Font("Serif", Font.PLAIN, 14));
        instructionsField.setText("This is a tool for debugging end enriching OWL-ontologies. " 
        							+ "You are able to check ontologies for inconsistency and unsatisfiable classes. " 
        							+ "If some of that is detected, helpful explanations can be generated to find out the relevant axioms. " 
        							+ "Another feature is to get equivalent class expressions for atomic classes, using an intelligent and efficient " 
        							+ "machine learning algorithm. TODO...Jens ");
        instructionsField.setWrapStyleWord(true);
        jScrollPane1.setViewportView(instructionsField);
        add(jScrollPane1, c);
        
        c.weighty = 1.0;
        add(new JLabel(), c);
    }
 
}
