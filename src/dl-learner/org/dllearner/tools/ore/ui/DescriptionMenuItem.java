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

import javax.swing.JMenuItem;

import org.dllearner.core.owl.Description;

/**
 * Item for possible repair action.
 * @author Lorenz Buehmann
 *
 */
public class DescriptionMenuItem extends JMenuItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6784086889435854440L;

	private Description desc;
	private int action;
	
	public DescriptionMenuItem(int action, String text, Description d){
		super();
		if(action == 3){
			setText("remove class assertion to " + text);
		} else if(action == 0){
			setText(text);
		} else if(action == 2){
			setText("add class assertion to " + text);
		} else if(action == 4){
			setText(text);
		} else if(action == 6){
			setText("delete complete property " + text);
		}else if(action == 5){
			setText("remove all property assertions to " + text);
		} else if(action == 7){
			setText("remove all property assertions with range not in " + text);
		} else if(action == 1){
			setText(text);
		}
		
		this.desc = d;
		this.action = action;
	}
	
	/**
	 * Returns the description part where item is asserted to.
	 * @return description
	 */
	public Description getDescription(){
		return desc;
	}
	
	/**
	 * Returns action type represented as number..
	 * @return number
	 */
	public int getActionID(){
		return action;
	}
	
	
	
}
