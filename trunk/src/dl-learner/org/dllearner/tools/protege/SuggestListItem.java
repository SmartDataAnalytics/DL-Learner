/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
/**
 * This Class represents an entry of the suggest list.
 * @author Christian Koetteritzsch
 *
 */
public class SuggestListItem {
	
	private final Color color;
    private String value;
    private double accuracy;
    /**
     * Constructor for the SuggestListItem.
     * @param c Color Color in which the text is painted.
     * @param s String text that is shown.
     * @param acc Accuracy of the concept
     */
    public SuggestListItem(
       Color c, String s, double acc) {
        this.color = c;
        this.value = s;
        this.accuracy = acc;
        
    }
    
    /**
     * This method returns the color of the current list item.
     * @return Color Color of the current list item
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * This Method returns the text of the current list item.
     * @return String Text of the current list item
     */
    public String getValue() {
        return value;
    }

	/**
	 * This method returns the accuracy of the current list item.
	 * @return accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}
}

