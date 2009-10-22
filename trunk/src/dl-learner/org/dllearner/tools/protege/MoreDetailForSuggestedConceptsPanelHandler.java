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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This is the Listener for the MoreDetailForSuggestedConceptsPanel.
 * @author Christian Koetteritzsch
 *
 */
public class MoreDetailForSuggestedConceptsPanelHandler implements PropertyChangeListener{
private final MoreDetailForSuggestedConceptsPanel panel;
	
	/**
	 * This is the constructor of the class.
	 * @param m MoreDetailForSuggestedConceptsPanel
	 */
	public MoreDetailForSuggestedConceptsPanelHandler(MoreDetailForSuggestedConceptsPanel m) {
		panel = m;
	}
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		panel.setInformation();
	}

}
