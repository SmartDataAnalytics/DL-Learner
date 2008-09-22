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
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;

/**
 * Wizard panel where atomic classes are shown in list.
 * @author Lorenz Buehmann
 *
 */
public class ClassPanelOWL extends JPanel{

	private static final long serialVersionUID = 3026319637264844550L;

	private javax.swing.JList conceptList;
	
	private JPanel contentPanel;
	
	private DefaultListModel model;
	private JXBusyLabel loadingLabel;	
	private JLabel statusLabel;
	
	/**
	 * Constructor.
	 */
	@SuppressWarnings("unchecked")
	public ClassPanelOWL() {
		
		super();
		
		model = new DefaultListModel();
		loadingLabel = new JXBusyLabel(new Dimension(15, 15));
		statusLabel = new JLabel();
		
	
		BusyPainter painter = new BusyPainter(
		new RoundRectangle2D.Float(0, 0, 6.0f, 2.6f, 10.0f, 10.0f),
		new Ellipse2D.Float(2.0f, 2.0f, 11.0f, 11.0f));
		painter.setTrailLength(2);
		painter.setPoints(7);
		painter.setFrame(-1);
		loadingLabel.setPreferredSize(new Dimension(15, 15));
		loadingLabel.setIcon(new EmptyIcon(15, 15));
		loadingLabel.setBusyPainter(painter);


				
		JPanel labelPanel = new JPanel();
		labelPanel.add(loadingLabel);
		labelPanel.add(statusLabel);
		
		
		contentPanel = getContentPanel();
		setLayout(new java.awt.BorderLayout());
		add(contentPanel, BorderLayout.CENTER);
		add(labelPanel, BorderLayout.SOUTH);
	}

	private JPanel getContentPanel() {
		
		JPanel contentPanel1 = new JPanel();
		JScrollPane scroll = new JScrollPane();
		
			
		conceptList = new JList(model);
		scroll.setPreferredSize(new Dimension(400, 400));
		scroll.setViewportView(conceptList);
		contentPanel1.add(scroll);
		
		

		return contentPanel1;
	}
	
	/**
	 * Returns list model for owl-classes.
	 * @return the list model
	 */
	public DefaultListModel getModel(){
		return model;
	}
	
	
	/**
	 * Adds list selection listener to atomic classes list.
	 * @param l the default list selection listener
	 */
	public void addSelectionListener(ListSelectionListener l){
		conceptList.addListSelectionListener(l);
	}
        
	/**
	 * Returns the list where atomic owl classes are the list elements.
	 * @return instance of JList
	 */
    public JList getList(){
    	return conceptList;
    }
    
    /**
     * Returns the label which reports the loading status.
     * @return instance of JLabel
     */
	public JLabel getStatusLabel() {
		return statusLabel;
	}

	/**
	 * Returns the animated label for loading action.
	 * @return instance of JXBusyLabel
	 */
	public JXBusyLabel getLoadingLabel() {
		return loadingLabel;
	}
    

 


	

}