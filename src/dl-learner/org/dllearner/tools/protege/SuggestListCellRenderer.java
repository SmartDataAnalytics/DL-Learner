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
package org.dllearner.tools.protege;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
/**
 * This is the class that is responsible for the rendering of the 
 * concepts that are shown in the SuggestPanel.
 * @author Christian Koetteritzsch
 *
 */
public class SuggestListCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 8040385703448641356L;
	/**
	 * Constructor for the Cell Renderer for the Suggest List.
	 */
	public SuggestListCellRenderer() {
		setOpaque(true);
	}
	
	/**
	 * Renderer for the entries of the SuggestPanel.
	 * @param list JList
	 * @param value Object
	 * @param arg2 int
	 * @param arg4 boolean
	 * @param iss boolean boolean if current element is selected.
	 * @return Component Returns the currently rendered component of the suggest list
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int arg2, boolean iss, boolean arg4) {
		// Set the text and
		// background color for rendering
		setText(((SuggestListItem) value).getValue() + "  " + "Accuracy: " + ((SuggestListItem) value).getAccuracy()+"%");
		setBackground(Color.WHITE);
		setForeground(((SuggestListItem) value).getColor());
		// Set a border if the list
		// item is selected
		if (iss) {
			setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		} else {
			setBorder(BorderFactory.createLineBorder(list.getBackground(), 2));
		}
		 setEnabled(list.isEnabled());

		return this;
	}

}
