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

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;


public class SavePanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4301954036023325496L;
	private JButton saveExit;
	private JButton saveGoBack;
	
	public SavePanel(){
		super();
		saveExit = new JButton("Save and Exit");
		saveGoBack = new JButton("Save and go to class choose panel");
		add(saveExit);
		add(saveGoBack);
	}
	
	public void addActionListeners(ActionListener aL){
		saveExit.addActionListener(aL);
		saveGoBack.addActionListener(aL);
	}
}
