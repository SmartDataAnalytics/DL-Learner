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

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;


/**
 * This class takes care of all events happening in the GraphicalCoveragePanel.
 * @author Christian Koetteritzsch
 *
 */
public class GraphicalCoveragePanelHandler implements MouseMotionListener, PropertyChangeListener {

	private final GraphicalCoveragePanel panel;

	/**
	 * This is the constructor for the handler.
	 * @param p GraphicalCoveragePanel
	 */
	public GraphicalCoveragePanelHandler(GraphicalCoveragePanel p) {
		this.panel = p;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent m) {
		Vector<IndividualPoint> v = panel.getIndividualVector();
		//System.out.println("hier: " + m.getX() + " " + m.getY());
		//System.out.println("bla: " + v.get(0).getXAxis() + " "
		//		+ v.get(0).getYAxis());
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i).getXAxis() >= m.getX() - 5
					&& v.get(i).getXAxis() <= m.getX() + 5
					&& v.get(i).getYAxis() >= m.getY() - 5
					&& v.get(i).getYAxis() <= m.getY() + 5) {
				panel.getGraphicalCoveragePanel().setToolTipText(v.get(i).getIndividualName());
			}

		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		panel.getMoreDetailForSuggestedConceptsPanel().repaint();	
	}


}
