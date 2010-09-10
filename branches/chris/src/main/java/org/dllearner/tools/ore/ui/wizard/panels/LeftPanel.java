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
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * Navigation panel where it's shown the actual wizard step and former and following steps.
 * @author Lorenz Buehmann
 *
 */
public class LeftPanel extends JPanel{

	private static final long serialVersionUID = -1205252523136710091L;
	private JLabel[] jLabel;
//	private ImageIcon currentStepIcon = new ImageIcon("src/dl-learner/org/dllearner/tools/ore/untoggled.gif");
	/**
	 * Constructor instantiating JLabels with wizard step names.
	 * @param i step number printed bold
	 */
	public LeftPanel(int i){
		
		jLabel = new JLabel[7];
//		setBackground(new java.awt.Color(255, 255, 255));
    	JPanel panel2 = new JPanel();
//    	panel2.setBackground(new java.awt.Color(255, 255, 255));
    	panel2.setLayout(new GridLayout(7, 1, 0, 10));
    	jLabel[0] = new JLabel("1. Introduction");
		jLabel[1] = new JLabel("2. Knowledge Source");
		jLabel[2] = new JLabel("3. Debugging");
		jLabel[3] = new JLabel("4. Choose Class");
		jLabel[4] = new JLabel("5. Learn");
		jLabel[5] = new JLabel("6. Repair");
		jLabel[6] = new JLabel("7. Save/Exit");
		
		jLabel[i].setFont(jLabel[i].getFont().deriveFont(Font.BOLD));
		
		for(JLabel current : jLabel){
			panel2.add(current);
		}
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(165, 500));
		add(panel2, BorderLayout.NORTH);
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout());
		holderPanel.add(panel2, BorderLayout.NORTH);
		add(holderPanel);
		add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);
		
	}
	
	/**
	 * Sets the actual step, that has to be printed bold in the navigation panel.
	 * @param i number of the step
	 */
	public void set(int i){
		
		for(int j = 0; j < jLabel.length; j++){
			jLabel[j].setFont(jLabel[j].getFont().deriveFont(Font.PLAIN));
		}
//		jLabel[i].setIcon(currentStepIcon);
		jLabel[i].setFont(jLabel[i].getFont().deriveFont(Font.BOLD));
		validate();
	}
	
}
