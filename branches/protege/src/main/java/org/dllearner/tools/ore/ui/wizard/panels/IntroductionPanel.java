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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkListener;

/**
 * Wizard panel with introduction text.
 * @author Lorenz Buehmann
 *
 */
public class IntroductionPanel extends JPanel {
 
	private static final long serialVersionUID = 7184544803724152044L;
	
	
	private JTextPane instructionsField;
        
    private JLabel welcomeTitle;
    
    private final String titleText = "<html><b>Welcome to the DL-Learner ORE (Ontology Repair and Enrichment) Tool!<br>(Version 0.2)</b></html>";

    private final String introductionText = "<html><p>ORE is a tool for debugging and enriching OWL ontologies. It has the following features: </p>" +
    		"<UL>" + 
    		"<LI>detection of inconsistencies" +
    		"<LI>displaying explanations for those inconsistencies" +
    		"<LI>intelligent resolution of inconsistencies" +
    		"<LI>enrichment of an ontology by learning definitions and super class axioms" +
    		"<LI>guiding the user through potential consequences of adding those axioms" +
    		"</UL>" + 
    		"<p >In a later version, the tool will also support the detection of various potential modelling problems.</p>" +
    		"<p style=\"max-width:400px;\">ORE uses a wizard-style concept. On the left, you can see different steps in the wizard, where the current step is in bold. " +
    		"Each step contains an explanation of it in the main window. The wizard may omit steps if they are not necessary, e.g. " +
    		"if you load a consistent ontology, then the \"Debugging\" dialogue is skipped.</p>" +
    		"<p>Please read the <a href=\"http://dl-learner.org/wiki/ORE\">the ORE wiki page</a> and view the screencasts " +
    		"[<a href=\"http://dl-learner.org/files/screencast/ore/0.2/ore.htm\">1</a>, " +
    		"<a href=\"http://dl-learner.org/files/screencast/ore/0.2/ore2.htm\">2</a>] " +
    		"to get started.</p></html>"; 
    
    public IntroductionPanel() {
    	createUI();
    }
    
    private void createUI(){
    	setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 12));
    	setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridwidth = GridBagConstraints.REMAINDER;
    	c.fill = GridBagConstraints.HORIZONTAL;
    	c.weightx = 1.0;
    	c.anchor = GridBagConstraints.NORTH;
    	 	   
        welcomeTitle = new JLabel();
        welcomeTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));
        welcomeTitle.setText(titleText);
        add(welcomeTitle, c);
        
        instructionsField = new JTextPane();
        instructionsField.setContentType("text/html");
        Color color = UIManager.getColor("Panel.background");
        instructionsField.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue()));
        instructionsField.setOpaque(true);
        instructionsField.setEditable(false);
        instructionsField.setText(introductionText);
        add(instructionsField, c);
        
        c.weighty = 1.0;
        add(new JLabel(), c);
    }
    
    public void addHyperLinkListener(HyperlinkListener hL){
    	instructionsField.addHyperlinkListener(hL);
    }
 
}
