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
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.ui.StatsTable;
import org.jdesktop.swingx.JXTitledPanel;

/**
 * JPanel where to buttons are added to save and go back to class choose panel.
 * @author Lorenz Buehmann
 *
 */
public class SavePanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4301954036023325496L;
	private JButton saveExit;
	private JButton saveGoBack;
	private StatsTable changesTable;
	
	public SavePanel(){
		setLayout(new GridLayout(0,1));
		
		JXTitledPanel changesPanel = new JXTitledPanel("Ontology changes");
		changesPanel.getContentContainer().setLayout(new BorderLayout());
		changesTable = new StatsTable();
		changesPanel.getContentContainer().add(new JScrollPane(changesTable), BorderLayout.CENTER);
		add(changesPanel);
		
		JPanel buttonHolderPanel = new JPanel();
		buttonHolderPanel.setLayout(new BoxLayout(buttonHolderPanel, BoxLayout.X_AXIS));
		saveExit = new JButton("Save and Exit");
		buttonHolderPanel.add(saveExit);
		saveGoBack = new JButton("Save and go to class choose panel");
		buttonHolderPanel.add(saveGoBack);
		
		add(buttonHolderPanel);
	}
	
	/**
	 * Adds the action listener to both buttons.
	 * @param aL action listener
	 */
	public void addActionListeners(ActionListener aL){
		saveExit.addActionListener(aL);
		saveGoBack.addActionListener(aL);
	}
	
	public void updateChangesTable(){
		changesTable.setChanges(OREManager.getInstance().getModifier().getChanges());
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		frame.add(new SavePanel());
		frame.setSize(400, 400);
		frame.setVisible(true);
	}
}
