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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.ORE;

/**
 * List cell renderer for 2 columns.
 * @author Lorenz Buehmann
 *
 */
public class ColumnListCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 3024913291199515567L;
	private ORE ore;
	
	
	public ColumnListCellRenderer(ORE ore) {
		this.ore = ore;
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		removeAll();

		JLabel cor = new JLabel();
		JLabel desc = new JLabel();
		setLayout(new GridBagLayout());
		desc.setText(((EvaluatedDescriptionClass) value).getDescription().toManchesterSyntaxString(ore.getBaseURI(), ore.getPrefixes()));
		//round accuracy to 2 digits
		double accuracy = ((EvaluatedDescriptionClass) value).getAccuracy();
		
		BigDecimal roundedAccuracy = new BigDecimal(accuracy * 100);
		roundedAccuracy = roundedAccuracy.setScale(2, BigDecimal.ROUND_HALF_UP);
		cor.setText(String.valueOf(roundedAccuracy));
		add(cor, new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));//, BorderLayout.WEST);
		add(desc, new GridBagConstraints(1, 0, 1, 1, 0.8, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));//, BorderLayout.EAST);

		Color background;
		Color foreground;
		
		if (index % 2 == 0 && !isSelected) {
			background = new Color(242, 242, 242);
			foreground = Color.BLACK;

		} else if(isSelected){
		 
             background = Color.LIGHT_GRAY;
             foreground = Color.WHITE;
        }else{
        	background = Color.WHITE;
            foreground = Color.BLACK;
        }
		
		setForeground(foreground);
		setBackground(background);

		return this;
	}

}