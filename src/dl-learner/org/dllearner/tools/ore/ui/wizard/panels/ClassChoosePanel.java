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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.ui.ClassesTable;

/**
 * Wizard panel where atomic classes are shown in list.
 * @author Lorenz Buehmann
 *
 */
public class ClassChoosePanel extends JPanel{

	private static final long serialVersionUID = 3026319637264844550L;

	private ClassesTable classesTable;
	
	private JPanel contentPanel;

	/**
	 * Constructor.
	 */

	public ClassChoosePanel() {
		
		super();
		
		contentPanel = getContentPanel();
		setLayout(new java.awt.BorderLayout());
		add(contentPanel, BorderLayout.CENTER);
	}

	private JPanel getContentPanel() {
		contentPanel = new JPanel();
		
		classesTable = new ClassesTable();
		JScrollPane scroll = new JScrollPane(classesTable);
		contentPanel.add(scroll);
		scroll.setPreferredSize(new Dimension(400, 400));

		return contentPanel;
	}
	
	/**
	 * Adds list selection listener to atomic classes table.
	 * @param l the default list selection listener
	 */
	public void addSelectionListener(ListSelectionListener l){
		classesTable.getSelectionModel().addListSelectionListener(l);
	}
        
	/**
	 * Returns the table where atomic owl classes are the table elements.
	 * @return instance of ClassesTable
	 */
    public ClassesTable getClassesTable(){
    	return classesTable;
    }

}