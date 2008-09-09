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
package org.dllearner.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Status panel showing help messages, exceptions etc.
 * 
 * @author Jens Lehmann
 *
 */
public class StatusPanel extends JPanel {

	private static final long serialVersionUID = 2426470148153461670L;
	
	private String initText = "Please fill in the mandatory config options and proceed to the next tab.";
	
	private JLabel statusLabel = new JLabel(initText);
	
	public StatusPanel() {
		super();
		add(statusLabel);
	}
	
	public void setStatus(String message) {
		statusLabel.setText(message);
	}

}
