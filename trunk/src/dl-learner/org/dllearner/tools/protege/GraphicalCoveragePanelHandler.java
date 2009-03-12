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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.dllearner.algorithms.EvaluatedDescriptionClass;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Individual;

/**
 * This class takes care of all events happening in the GraphicalCoveragePanel.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class GraphicalCoveragePanelHandler implements MouseMotionListener,
		MouseListener, PropertyChangeListener {

	private final GraphicalCoveragePanel panel;
	private final EvaluatedDescription description;
	private final DLLearnerModel model;
	private final JPopupMenu popup;

	/**
	 * This is the constructor for the handler.
	 * 
	 * @param p
	 *            GraphicalCoveragePanel
	 */
	public GraphicalCoveragePanelHandler(GraphicalCoveragePanel p,
			EvaluatedDescription eval, DLLearnerModel m) {
		this.panel = p;
		description = eval;
		model = m;
		popup = new JPopupMenu();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent m) {
		panel.getMoreDetailForSuggestedConceptsPanel().repaint();
		if (m.getX() >= panel.getX1() + panel.getShiftCovered()
				&& m.getX() <= panel.getX2() + panel.getShiftCovered()
				&& m.getY() >= panel.getY1() && m.getY() <= panel.getY2()
				|| m.getX() >= panel.getX1() + panel.getShiftNewConcept()
				&& m.getX() <= panel.getX2() + panel.getShiftNewConcept()
				&& m.getY() >= panel.getY1() && m.getY() <= panel.getY2()
				|| m.getX() >= panel.getX1() + panel.getShiftNewConceptX()
				&& m.getX() <= panel.getX2() + panel.getShiftNewConceptX()
				&& m.getY() >= panel.getY1() + panel.getShiftNewConcept()
				&& m.getY() <= panel.getY2() + panel.getShiftNewConcept()
				|| m.getX() >= panel.getX1() - panel.getShiftOldConcept()
				&& m.getX() <= panel.getX2() - panel.getShiftOldConcept()
				&& m.getY() >= panel.getY1() && m.getY() <= panel.getY2()) {
			panel.getGraphicalCoveragePanel().setToolTipText(
					"To view all Individuals please click on the plus");
		} else {
			panel.getGraphicalCoveragePanel().setToolTipText(null);
		}
		Vector<IndividualPoint> v = panel.getIndividualVector();
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i).getXAxis() >= m.getX() - 5
					&& v.get(i).getXAxis() <= m.getX() + 5
					&& v.get(i).getYAxis() >= m.getY() - 5
					&& v.get(i).getYAxis() <= m.getY() + 5) {
				panel.getGraphicalCoveragePanel().setToolTipText(
						v.get(i).getIndividualName());
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		panel.getMoreDetailForSuggestedConceptsPanel().repaint();
		panel.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (panel.getEvaluateddescription() != null) {
			if (arg0.getX() >= panel.getX1() + panel.getShiftCovered()
					&& arg0.getX() <= panel.getX2() + panel.getShiftCovered()
					&& arg0.getY() >= panel.getY1()
					&& arg0.getY() <= panel.getY2()) {
				popup.removeAll();
				panel.getMoreDetailForSuggestedConceptsPanel().repaint();
				Set<Individual> covInd = ((EvaluatedDescriptionClass) description)
						.getCoveredInstances();
				for (Individual ind : covInd) {
					popup.add(new JMenuItem(ind.toString()));
				}
				popup.show(panel, arg0.getX(), arg0.getY());
				panel.getMoreDetailForSuggestedConceptsPanel().repaint();
			}

			if (arg0.getX() >= panel.getX1() + panel.getShiftNewConcept()
					&& arg0.getX() <= panel.getX2()
							+ panel.getShiftNewConcept()
					&& arg0.getY() >= panel.getY1()
					&& arg0.getY() <= panel.getY2()
					|| arg0.getX() >= panel.getX1()
							+ panel.getShiftNewConceptX()
					&& arg0.getX() <= panel.getX2()
							+ panel.getShiftNewConceptX()
					&& arg0.getY() >= panel.getY1()
							+ panel.getShiftNewConcept()
					&& arg0.getY() <= panel.getY2()
							+ panel.getShiftNewConcept()) {
				popup.removeAll();
				panel.getMoreDetailForSuggestedConceptsPanel().repaint();
				Set<Individual> addInd = ((EvaluatedDescriptionClass) description)
						.getAdditionalInstances();
				for (Individual ind : addInd) {
					popup.add(new JMenuItem(ind.toString()));
				}
				popup.show(panel, arg0.getX(), arg0.getY());
				panel.getMoreDetailForSuggestedConceptsPanel().repaint();
			}

			if (arg0.getX() >= panel.getX1() - panel.getShiftOldConcept()
					&& arg0.getX() <= panel.getX2()
							- panel.getShiftOldConcept()
					&& arg0.getY() >= panel.getY1()
					&& arg0.getY() <= panel.getY2()) {
				popup.removeAll();
				panel.getMoreDetailForSuggestedConceptsPanel().repaint();
				Set<Individual> notCovInd = model.getReasoner().getIndividuals(
						model.getCurrentConcept());
				notCovInd.removeAll(((EvaluatedDescriptionClass) description)
						.getCoveredInstances());
				for (Individual ind : notCovInd) {
					popup.add(new JMenuItem(ind.toString()));
				}
				popup.show(panel, arg0.getX(), arg0.getY());
				panel.getMoreDetailForSuggestedConceptsPanel().repaint();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
