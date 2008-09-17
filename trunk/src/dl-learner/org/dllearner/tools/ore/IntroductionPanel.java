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

package org.dllearner.tools.ore;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;


public class IntroductionPanel extends JPanel {
 
	private static final long serialVersionUID = 7184544803724152044L;
	
	
	private JTextArea instructionsField;
    private JScrollPane jScrollPane1;
        
    private JLabel welcomeTitle;
    private JPanel contentPanel;
     
    
    
    public IntroductionPanel() {
        
        contentPanel = getContentPanel();
    
        setLayout(new java.awt.BorderLayout());

     
        JPanel secondaryPanel = new JPanel();
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    
    private JPanel getContentPanel() {
        
        JPanel contentPanel1 = new JPanel();
        
        JPanel jPanel1 = new JPanel();
        
        contentPanel1.setLayout(new java.awt.BorderLayout());
        
        jScrollPane1 = new JScrollPane();
        instructionsField = new JTextArea();
        
        
        //setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));
        jScrollPane1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jScrollPane1.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        instructionsField.setBackground(UIManager.getDefaults().getColor("control"));
        instructionsField.setColumns(20);
        instructionsField.setEditable(false);
        instructionsField.setLineWrap(true);
        instructionsField.setRows(5);
        instructionsField.setFont(new Font("Serif",Font.PLAIN,14));
        instructionsField.setText("This is an test of a wizard dialog, which allows a knowledge engineer to select " +
        							"a class of an ontology which should be (re)learned.\n" +
        							"On the next page, choose a OWL file or a SPARQL-URL, that contains an ontology. After that " +
        							"you might be able to select a class in the ontology to learn. When the class you selected is learned" +
        							", you are able to add the axiom to the ontology and after all you might be able to repair if necessary. " );
        instructionsField.setWrapStyleWord(true);
        jScrollPane1.setViewportView(instructionsField);
              
        welcomeTitle = new JLabel();
        welcomeTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));
        welcomeTitle.setFont(new java.awt.Font("MS Sans Serif", Font.BOLD, 14));
        welcomeTitle.setText("Welcome to the DL-Learner ORE-Tool!");
        contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);
        
        jPanel1.setLayout(new java.awt.GridLayout(0, 1,0,0));
        jPanel1.add(jScrollPane1);
        contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);
        
      

        return contentPanel1;
        
    }
 
}
