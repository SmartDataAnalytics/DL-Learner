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
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.reasoning.FastInstanceChecker;

/**
 * This class takes care of all events happening in the GraphicalCoveragePanel.
 * It renders the Informations for the individual points and sets the 
 * individuals for the popup component.
 * @author Christian Koetteritzsch
 * 
 */
public class GraphicalCoveragePanelHandler implements MouseMotionListener,
		MouseListener, PropertyChangeListener {

	private final GraphicalCoveragePanel panel;
	private final EvaluatedDescription description;
	private final DLLearnerModel model;
	private BasicComboPopup scrollPopup;
	private final Vector<String> individualComboBox;
	private JComboBox indiBox;

	/**
	 * This is the constructor for the handler.
	 * 
	 * @param p
	 *            GraphicalCoveragePanel
	 * @param eval
	 *            EvaluatedDescription
	 * @param m
	 *            DLLearnerModel
	 */
	public GraphicalCoveragePanelHandler(GraphicalCoveragePanel p,
			EvaluatedDescription eval, DLLearnerModel m) {
		this.panel = p;
		description = eval;
		model = m;
		individualComboBox = new Vector<String>();

	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void mouseDragged(MouseEvent arg0) {

	}

	@Override
	/**
	 * This methode renders the tool tip message when the mouse goes over
	 * the plus symbole. It also renders the the informations for the individual point.
	 */
	public void mouseMoved(MouseEvent m) {
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
		}
		
		Vector<IndividualPoint> v = panel.getIndividualVector();
		FastInstanceChecker reasoner = model.getReasoner();
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i).getXAxis() >= m.getX() - 5
					&& v.get(i).getXAxis() <= m.getX() + 5
					&& v.get(i).getYAxis() >= m.getY() - 5
					&& v.get(i).getYAxis() <= m.getY() + 5) {
				String individualInformation = "<html><body>" + v.get(i).getIndividualName().toString();
				Set<NamedClass> types = reasoner.getTypes(v.get(i).getDLLearnerIndividual());
				individualInformation += "<br><b>Types:</b><br>";
				for(NamedClass dlLearnerClass : types) {
					individualInformation += dlLearnerClass.toManchesterSyntaxString(v.get(i).getBaseUri(), null) + "<br>";
				}
				Map<ObjectProperty,Set<Individual>> objectProperties = reasoner.getObjectPropertyRelationships(v.get(i).getDLLearnerIndividual());
				Set<ObjectProperty> key = objectProperties.keySet();
				individualInformation += "<br><b>Objectproperties:</b><br>";
				for(ObjectProperty objectProperty: key) {
					Set<Individual> indiSet = objectProperties.get(objectProperty);
					individualInformation = individualInformation + objectProperty.toManchesterSyntaxString(v.get(i).getBaseUri(), null) + " ";
					for(Individual indi: indiSet) {
						individualInformation += indi.toManchesterSyntaxString(v.get(i).getBaseUri(), null);
						if(indiSet.size() > 1) {
							individualInformation += ", ";
						}
					}
					individualInformation += "<br>";
				}
				individualInformation += "</body></htlm>";
				panel.getGraphicalCoveragePanel().setToolTipText(individualInformation);
			}
		}
	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
	}

	@Override
	/**
	 * This methode renders the popup box and
	 * computes which individuals must be shown.
	 */
	public void mouseClicked(MouseEvent arg0) {
		if (panel.getEvaluateddescription() != null) {
			if (arg0.getX() >= panel.getX1() + panel.getShiftCovered()
					&& arg0.getX() <= panel.getX2() + panel.getShiftCovered()
					&& arg0.getY() >= panel.getY1()
					&& arg0.getY() <= panel.getY2()) {

				individualComboBox.clear();

				Set<Individual> covInd = ((EvaluatedDescriptionClass) description)
						.getCoveredInstances();
				int i = covInd.size();
				if (i > 0) {
					for (Individual ind : covInd) {
						Set<String> uriString = model.getOntologyURIString();
						for(String uri : uriString) {
							if(ind.toString().contains(uri)) {
								individualComboBox.add(ind.toManchesterSyntaxString(uri, null));
							}
						}
					}
					indiBox = new JComboBox(individualComboBox);
					scrollPopup = new BasicComboPopup(indiBox);
					scrollPopup.setAutoscrolls(true);
					scrollPopup.show(panel, arg0.getX(), arg0.getY());
				}
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

				individualComboBox.clear();
				Set<Individual> addInd = ((EvaluatedDescriptionClass) description)
						.getAdditionalInstances();
				int i = addInd.size();
				if (i > 0) {
					for (Individual ind : addInd) {
						Set<String> uriString = model.getOntologyURIString();
						for(String uri : uriString) {
							if(ind.toString().contains(uri)) {
								individualComboBox.add(ind.toManchesterSyntaxString(uri, null));
							}
						}
					}
					indiBox = new JComboBox(individualComboBox);
					scrollPopup = new BasicComboPopup(indiBox);
					scrollPopup.setAutoscrolls(true);
					scrollPopup.show(panel, arg0.getX(), arg0.getY());
				}
			}

			if (arg0.getX() >= panel.getX1() - panel.getShiftOldConcept()
					&& arg0.getX() <= panel.getX2()
							- panel.getShiftOldConcept()
					&& arg0.getY() >= panel.getY1()
					&& arg0.getY() <= panel.getY2()) {

				individualComboBox.clear();
				Set<Individual> notCovInd = model.getReasoner().getIndividuals(
						model.getCurrentConcept());
				notCovInd.removeAll(((EvaluatedDescriptionClass) description)
						.getCoveredInstances());
				int i = notCovInd.size();
				if (i > 0) {
					for (Individual ind : notCovInd) {
						Set<String> uriString = model.getOntologyURIString();
						for(String uri : uriString) {
							if(ind.toString().contains(uri)) {
								individualComboBox.add(ind.toManchesterSyntaxString(uri, null));
							}
						}
					}
					indiBox = new JComboBox(individualComboBox);
					scrollPopup = new BasicComboPopup(indiBox);
					scrollPopup.setAutoscrolls(true);
					scrollPopup.show(panel, arg0.getX(), arg0.getY());
				}
			}
		}
	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	/**
	 * Nothing happens here.
	 */
	public void mouseReleased(MouseEvent arg0) {

	}

}
