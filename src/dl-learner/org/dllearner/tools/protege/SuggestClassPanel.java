/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.tools.protege;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * This class is the panel for the suggest list.
 * It shows the descriptions made by the DL-Learner.
 * @author Christian Koetteritzsch
 *
 */
public class SuggestClassPanel extends JPanel {
	
	private static final long serialVersionUID = 724628423947230L;
	
	 // Description List
	 
	private final JList descriptions;
	
	 // Panel for the description list
	 
	private final JPanel suggestPanel;
	
	 // Date for the description list
	 
	private final DefaultListModel model;
	
	 //Scroll panel if the suggestions are longer than the Panel itself

	private JScrollPane suggestScroll;
	/**
	 * This is the constructor for the suggest panel.
	 * It creates a new Scroll panel and puts the Suggest List in it. 
	 */
	public SuggestClassPanel() {
		super();
		suggestScroll = new JScrollPane();
		//renders scroll bars if necessary
		suggestScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		model = new DefaultListModel();
		descriptions = new JList(model);
		descriptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		suggestPanel = new JPanel();
		descriptions.setVisible(true);
		suggestPanel.add(descriptions);
		suggestScroll.setPreferredSize(new Dimension(490, 108));
		suggestScroll.setViewportView(descriptions);
		descriptions.setCellRenderer(new SuggestListCellRenderer());
		add(suggestScroll);
	}
	
	/**
	 * this method adds an new Scroll Panel and returns the updated SuggestClassPanel.
	 * @return updated SuggestClassPanel
	 */
	public SuggestClassPanel updateSuggestClassList() {
		add(suggestScroll);
		return this;
		
	}
	/**
	 * This method is called after the model for the suggest list is updated.
	 *  
	 * @param desc List model of descriptions made by the DL-Learner
	 */
	public void setSuggestList(DefaultListModel desc) {
		descriptions.setModel(desc);
		repaint();
	}
	/**
	 * This method returns the current Description list.
	 * @return JList of Descriptions
	 */
	public JList getSuggestList() {
		return descriptions;
	}
	
	/**
	 * This method adds the suggest list to the Mouse Listener.
	 * @param action ActionHandler
	 */
	public void addSuggestPanelMouseListener(ActionHandler action) {
		descriptions.addMouseListener(action);
		
	}
	

}
