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

import java.awt.Dimension;

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
	
	private String tabInitText = "<html>Please fill in the mandatory config options and proceed to the next tab.<br />(Move the mouse over an option name to view a short description of it.)</html>";
	
	private String tabCompleteText = "All mandatory options filled in. You can continue to the next tab.";
	
	private String runPanelText = "<html><p>Choose \"run\" to start the algorithm. For unsolvable learning problems, " +
			"there is no guarantee that the algorithm will terminate. You can choose \"stop\" to stop the" +
			" execution of the algorithm.</p></html>";
	
	private JLabel statusLabel = new JLabel(tabInitText);
	
	private String message = tabInitText;
	
	private String oldMessage;
	
	// specifies whether the message is an exception
	private boolean isException = false;
	
	public StatusPanel() {
		super();
		statusLabel.setPreferredSize(new Dimension(600, 50));
		add(statusLabel);
	}
	
	private void updateMessage(String newMessage) {
		oldMessage = this.message;
		this.message = newMessage;
		statusLabel.setText(newMessage);
//		repaint();
	}
	
	public void setStatus(String newMessage) {
		if(!isException) {
			updateMessage(newMessage);
		}
	}
	
	public void setExceptionMessage(String newMessage) {
		updateMessage(newMessage);
		isException = true;
	}	
	
	public void setTabInitMessage() {
		updateMessage(tabInitText);
	}

	public void setTabCompleteMessage() {
		updateMessage(tabCompleteText);
	}	
	
	public void setRunPanelMessage() {
		updateMessage(runPanelText);
	}	
	
	public void extendMessage(String addition) {
		message += addition;
		statusLabel.setText(message);
	}
	
	public void revertToPreviousMessage() {
		updateMessage(oldMessage);
	}
}
