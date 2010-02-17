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

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
/**
 * JPanel where all ontology changes are shown.
 * @author Lorenz Buehmann
 *
 */
public class ChangesPanel extends JPanel{

	private static final long serialVersionUID = -7538532926820669891L;

	/**
	 * Constructor.
	 */
	public ChangesPanel(){
		super();
		setLayout(new GridLayout(0, 1));
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
	}
	

	/**
	 * Refresh the actual panel.
	 * @param cont gui container for changes
	 */
	public void updatePanel(Container cont){
		remove(cont);
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	
}
