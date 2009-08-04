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

package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.tools.ore.ORE;

/**
 * List cell renderer for colored lines to provide better view on list values.
 * @author Lorenz Buehmann
 *
 */
public class ColorListCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -7592805113197759247L;
	private ORE ore;
	
	public ColorListCellRenderer(ORE ore) {
		setOpaque(true);
		this.ore = ore;
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		if(value instanceof NamedClass){
			setText(((NamedClass) value).toManchesterSyntaxString(ore.getBaseURI(), ore.getPrefixes()));
		} else if(value instanceof Individual){
			setText(((Individual) value).toManchesterSyntaxString(ore.getBaseURI(), ore.getPrefixes()));
		}
		Color background;
		Color foreground;
		
		if (index % 2 == 0 && !isSelected) {
			background = new Color(242, 242, 242);
			foreground = Color.BLACK;

		} else if(isSelected){
		 
             background = Color.LIGHT_GRAY;
             foreground = Color.BLACK;
        }else{
        	background = Color.WHITE;
            foreground = Color.BLACK;
        }
		
		setForeground(foreground);
		setBackground(background);

		return this;
	}

}
