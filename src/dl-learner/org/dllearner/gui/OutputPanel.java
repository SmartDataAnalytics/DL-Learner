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

import javax.swing.*;

/**
 * OutputPanel
 * 
 * @author Tilo Hielscher
 * 
 */

public class OutputPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1643304576470046636L;
	
	private JButton showButton;
	private JTextArea infoArea;
	
	OutputPanel() {
		super(new BorderLayout());
		
		showButton = new JButton("Show Variables");
		showButton.addActionListener(this);
		infoArea = new JTextArea(20,40);		
		
		JPanel showPanel = new JPanel();
		showPanel.add(showButton);
		JPanel infoPanel = new JPanel();
		infoPanel.add(infoArea);
		
		add(showPanel, BorderLayout.PAGE_START);
		add(infoPanel, BorderLayout.CENTER);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == showButton) {
			if (StartGUI.myconfig.getStatus(1)) {  // file is selected and exist?
				infoArea.append("SourceClass: " + StartGUI.myconfig.getKnowledgeSource().toString() + "\n");
				infoArea.append("FILE: " + StartGUI.myconfig.getFile().toString() + "\n");
			}
			if (StartGUI.myconfig.getStatus(2)) {
				
			}
			
		}
	}
}
