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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class LeftPanel extends JPanel{

	private static final long serialVersionUID = -1205252523136710091L;
	private JLabel[] jLabel;
	
	public LeftPanel(int i){
		
		jLabel = new JLabel[6];
		setBackground(new java.awt.Color(255, 255, 255));
    	JPanel panel2 = new JPanel();
    	panel2.setBackground(new java.awt.Color(255, 255, 255));
    	panel2.setLayout(new GridLayout(5,1,0,10));
    	jLabel[0] = new JLabel("1. Introduction");
		jLabel[1] = new JLabel("2. Knowledge Source");
		jLabel[2] = new JLabel("3. Choose Class");
		jLabel[3] = new JLabel("4. Learn");
		jLabel[4] = new JLabel("5. Repair");
		jLabel[5] = new JLabel("6. Save/Exit");
		jLabel[i].setFont(jLabel[i].getFont().deriveFont(Font.BOLD));
		
		for(JLabel current : jLabel)
			panel2.add(current);		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(140,500));
		add(panel2, BorderLayout.NORTH);
		
	}
	
	public void set(int i){
		removeAll();
	
		setBackground(new java.awt.Color(255, 255, 255));
    	JPanel panel2 = new JPanel();
    	panel2.setBackground(new java.awt.Color(255, 255, 255));
    	panel2.setLayout(new GridLayout(6,1,0,10));
    	jLabel[0] = new JLabel("1. Introduction");
		jLabel[1] = new JLabel("2. Knowledge Source");
		jLabel[2] = new JLabel("3. Choose Class");
		jLabel[3] = new JLabel("4. Learning");
		jLabel[4] = new JLabel("5. Repair");
		jLabel[5] = new JLabel("6. Save/Exit");
		
		jLabel[i].setFont(jLabel[i].getFont().deriveFont(Font.BOLD));
		
		for(JLabel current : jLabel)
			panel2.add(current);		
		setLayout(new BorderLayout());
		add(panel2, BorderLayout.NORTH);
	}
	
}
