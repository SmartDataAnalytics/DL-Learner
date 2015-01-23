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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * This class draws the graphical coverage of a learned concept.
 * 
 * @author Christian Koetteritzsch
 * 
 */
public class GraphicalCoveragePanel extends JPanel {

	private static final long serialVersionUID = 855436961912515267L;
	private static final int HEIGHT = 150;
	private static final int WIDTH = 150;
	private static final int ELLIPSE_X_AXIS = 5;
	private static final int ELLIPSE_Y_AXIS = 5;
	private static final int MAX_NUMBER_OF_INDIVIDUAL_POINTS = 20;
	private static final int PLUS_SIZE = 5;
	private static final int SUBSTRING_SIZE = 25;
	private static final int SPACE_SIZE = 7;
	private static final int MAX_RANDOM_NUMBER = 300;
	private static final String EQUI_STRING = "equivalent class";
	private final String id;
	private int shiftXAxis;
	private int distortionOld;
	private Ellipse2D oldConcept;
	private Ellipse2D newConcept;

	private EvaluatedDescription eval;
	private String conceptNew;
	private final Vector<IndividualPoint> posCovIndVector;
	private final Vector<IndividualPoint> posNotCovIndVector;
	private final Vector<IndividualPoint> additionalIndividuals;
	private final Vector<IndividualPoint> points;
	private final Vector<String> conceptVector;
	private final GraphicalCoveragePanelHandler handler;
	private int adjustment;
	private int shiftOldConcept;
	private int shiftNewConcept;
	private int shiftNewConceptX;
	private int shiftCovered;
	private int coveredIndividualSize;
	private int additionalIndividualSize;
	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private int centerX;
	private int centerY;
	private final Random random;
	private final Color darkGreen;
	private final Color darkRed;
	private int notCoveredInd;
	private OWLDataFactory factory;

	/**
	 * 
	 * This is the constructor for the GraphicalCoveragePanel.
	 * 
	 * @param desc
	 *            EvaluatedDescription
	 * @param m
	 *            DLLearnerModel
	 */
	public GraphicalCoveragePanel(EvaluatedDescription desc) {
		this.setForeground(Color.GREEN);
		eval = desc;
//		id = model.getID();
		id = EQUI_STRING;
		darkGreen = new Color(0, 100, 0);
		darkRed = new Color(205, 0, 0);
		random = new Random();
//		for(String uri : model.getOntologyURIString()) {
//			if(eval.getDescription().toString().contains(uri)) {
//				conceptNew = eval.getDescription().toManchesterSyntaxString(uri, null);
//			}
//		}
		if(eval != null){
			conceptNew = Manager.getInstance().getRendering(eval.getDescription());
		} else{
			conceptNew = "";
		}
		
		
		conceptVector = new Vector<String>();
		posCovIndVector = new Vector<IndividualPoint>();
		posNotCovIndVector = new Vector<IndividualPoint>();
		additionalIndividuals = new Vector<IndividualPoint>();
		points = new Vector<IndividualPoint>();
		this.computeGraphics();
		handler = new GraphicalCoveragePanelHandler(this, desc);
		
		factory = Manager.getInstance().getActiveOntology().getOWLOntologyManager().getOWLDataFactory();
		this.computeIndividualPoints();
		this.addMouseMotionListener(handler);
		this.addMouseListener(handler);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (eval != null) {
			Graphics2D g2D;
			g2D = (Graphics2D) g;
			g2D.clearRect(0, 0, getWidth(), getHeight());
			AlphaComposite ac = AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f);
			
			g2D.setColor(Color.YELLOW);
			g2D.fill(oldConcept);
			g2D.setColor(Color.ORANGE);
			g2D.setComposite(ac);
			g2D.fill(newConcept);
			g2D.setColor(Color.BLACK);

			// Plus 1
			if (coveredIndividualSize != Manager.getInstance().getIndividuals().size()
					&& notCoveredInd != 0) {
				g2D.drawLine(x1 - 1 - shiftOldConcept, y1 - 1, x2 + 1
						- shiftOldConcept, y1 - 1);
				g2D.drawLine(x1 - shiftOldConcept, centerY - 1, x2
						- shiftOldConcept, centerY - 1);
				g2D.drawLine(x1 - shiftOldConcept, centerY, x2
						- shiftOldConcept, centerY);
				g2D.drawLine(x1 - shiftOldConcept, centerY + 1, x2
						- shiftOldConcept, centerY + 1);
				g2D.drawLine(x1 - 1 - shiftOldConcept, y2 + 1, x2 + 1
						- shiftOldConcept, y2 + 1);

				g2D.drawLine(x1 - 1 - shiftOldConcept, y1 - 1, x1 - 1
						- shiftOldConcept, y2 + 1);
				g2D.drawLine(centerX - 1 - shiftOldConcept, y1, centerX - 1
						- shiftOldConcept, y2);
				g2D.drawLine(centerX - shiftOldConcept, y1, centerX
						- shiftOldConcept, y2);
				g2D.drawLine(centerX + 1 - shiftOldConcept, y1, centerX + 1
						- shiftOldConcept, y2);
				g2D.drawLine(x2 + 1 - shiftOldConcept, y1 - 1, x2 + 1
						- shiftOldConcept, y2 + 1);
			}
			// Plus 2

			g2D.drawLine(x1 - 1 + shiftCovered, y1 - 1, x2 + 1 + shiftCovered,
					y1 - 1);
			g2D.drawLine(x1 + shiftCovered, centerY - 1, x2 + shiftCovered,
					centerY - 1);
			g2D
					.drawLine(x1 + shiftCovered, centerY, x2 + shiftCovered,
							centerY);
			g2D.drawLine(x1 + shiftCovered, centerY + 1, x2 + shiftCovered,
					centerY + 1);
			g2D.drawLine(x1 - 1 + shiftCovered, y2 + 1, x2 + 1 + shiftCovered,
					y2 + 1);

			g2D.drawLine(x1 - 1 + shiftCovered, y1 - 1, x1 - 1 + shiftCovered,
					y2 + 1);
			g2D.drawLine(centerX - 1 + shiftCovered, y1, centerX - 1
					+ shiftCovered, y2);
			g2D
					.drawLine(centerX + shiftCovered, y1, centerX
							+ shiftCovered, y2);
			g2D.drawLine(centerX + 1 + shiftCovered, y1, centerX + 1
					+ shiftCovered, y2);
			g2D.drawLine(x2 + 1 + shiftCovered, y1 - 1, x2 + 1 + shiftCovered,
					y2 + 1);

			// Plus 3
			if (coveredIndividualSize != Manager.getInstance().getIndividuals().size() && ((EvaluatedDescriptionClass) eval).getAdditionalInstances().size() != 0) {
				g2D.drawLine(x1 - 1 + shiftNewConcept, y1 - 1, x2 + 1
						+ shiftNewConcept, y1 - 1);
				g2D.drawLine(x1 + shiftNewConcept, centerY - 1, x2
						+ shiftNewConcept, centerY - 1);
				g2D.drawLine(x1 + shiftNewConcept, centerY, x2
						+ shiftNewConcept, centerY);
				g2D.drawLine(x1 + shiftNewConcept, centerY + 1, x2
						+ shiftNewConcept, centerY + 1);
				g2D.drawLine(x1 - 1 + shiftNewConcept, y2 + 1, x2 + 1
						+ shiftNewConcept, y2 + 1);

				g2D.drawLine(x1 - 1 + shiftNewConcept, y1 - 1, x1 - 1
						+ shiftNewConcept, y2 + 1);
				g2D.drawLine(centerX - 1 + shiftNewConcept, y1, centerX - 1
						+ shiftNewConcept, y2);
				g2D.drawLine(centerX + shiftNewConcept, y1, centerX
						+ shiftNewConcept, y2);
				g2D.drawLine(centerX + 1 + shiftNewConcept, y1, centerX + 1
						+ shiftNewConcept, y2);
				g2D.drawLine(x2 + 1 + shiftNewConcept, y1 - 1, x2 + 1
						+ shiftNewConcept, y2 + 1);
			}
			//Plus 4
			if (((EvaluatedDescriptionClass) eval).getAddition() != 1.0
					&& ((EvaluatedDescriptionClass) eval).getCoverage() == 1.0) {
				g2D.drawLine(x1 - 1 + shiftNewConceptX, y1 - 1
						+ shiftNewConcept, x2 + 1 + shiftNewConceptX, y1 - 1
						+ shiftNewConcept);
				g2D.drawLine(x1 + shiftNewConceptX, centerY - 1
						+ shiftNewConcept, x2 + shiftNewConceptX, centerY - 1
						+ shiftNewConcept);
				g2D.drawLine(x1 + shiftNewConceptX, centerY + shiftNewConcept,
						x2 + shiftNewConceptX, centerY + shiftNewConcept);
				g2D.drawLine(x1 + shiftNewConceptX, centerY + 1
						+ shiftNewConcept, x2 + shiftNewConceptX, centerY + 1
						+ shiftNewConcept);
				g2D.drawLine(x1 - 1 + shiftNewConceptX, y2 + 1
						+ shiftNewConcept, x2 + 1 + shiftNewConceptX, y2 + 1
						+ shiftNewConcept);

				g2D.drawLine(x1 - 1 + shiftNewConceptX, y1 - 1
						+ shiftNewConcept, x1 - 1 + shiftNewConceptX, y2 + 1
						+ shiftNewConcept);
				g2D.drawLine(centerX - 1 + shiftNewConceptX, y1
						+ shiftNewConcept, centerX - 1 + shiftNewConceptX, y2
						+ shiftNewConcept);
				g2D.drawLine(centerX + shiftNewConceptX, y1 + shiftNewConcept,
						centerX + shiftNewConceptX, y2 + shiftNewConcept);
				g2D.drawLine(centerX + 1 + shiftNewConceptX, y1
						+ shiftNewConcept, centerX + 1 + shiftNewConceptX, y2
						+ shiftNewConcept);
				g2D.drawLine(x2 + 1 + shiftNewConceptX, y1 - 1
						+ shiftNewConcept, x2 + 1 + shiftNewConceptX, y2 + 1
						+ shiftNewConcept);
			}

			for (int i = 0; i < posCovIndVector.size(); i++) {
				g2D.setColor(darkGreen);
				g2D.fill(posCovIndVector.get(i).getIndividualPoint());
			}

			for (int i = 0; i < posNotCovIndVector.size(); i++) {
				g2D.setColor(darkRed);
				g2D.fill(posNotCovIndVector.get(i).getIndividualPoint());
			}

			for (int i = 0; i < additionalIndividuals.size(); i++) {
				g2D.setColor(Color.BLACK);
				g2D.fill(additionalIndividuals.get(i).getIndividualPoint());
			}
		}
	}
	
	public void setDescription(EvaluatedDescription desc){
		this.eval = desc;
		handler.setDescription(desc);
		
		computeGraphics();
		computeIndividualPoints();
		repaint();
		
		
	}

	private void computeGraphics() {
		if (eval != null) {
			additionalIndividualSize = ((EvaluatedDescriptionClass) eval)
					.getAdditionalInstances().size();
			distortionOld = 0;
			adjustment = 0;
			Ellipse2D old = new Ellipse2D.Double(ELLIPSE_X_AXIS, ELLIPSE_Y_AXIS,
					WIDTH, HEIGHT);
			x1 = (int) old.getCenterX() - PLUS_SIZE;
			x2 = (int) old.getCenterX() + PLUS_SIZE;
			y1 = (int) old.getCenterY() - PLUS_SIZE;
			y2 = (int) old.getCenterY() + PLUS_SIZE;
			centerX = (int) old.getCenterX();
			centerY = (int) old.getCenterY();
			double coverage = ((EvaluatedDescriptionClass) eval).getCoverage();
			shiftXAxis = (int) Math.round((WIDTH) * (1 - coverage));
			
			
			
			if (additionalIndividualSize != 0 && ((EvaluatedDescriptionClass) eval).getCoverage() == 1.0 && ((EvaluatedDescriptionClass) eval).getAddition() < 1.0) {
				distortionOld = (int) Math.round((WIDTH) * 0.3);
				Ellipse2D newer = new Ellipse2D.Double(ELLIPSE_X_AXIS + shiftXAxis,
						ELLIPSE_Y_AXIS, (WIDTH), HEIGHT);
				adjustment = (int) Math.round(newer.getCenterY() / 4);
			}
			if (shiftXAxis == 0) {
				oldConcept = new Ellipse2D.Double(ELLIPSE_X_AXIS
						+ (2 * adjustment) + 3, ELLIPSE_Y_AXIS + 3, WIDTH,
						HEIGHT);
			} else {
				oldConcept = new Ellipse2D.Double(ELLIPSE_X_AXIS
						+ (2 * adjustment), ELLIPSE_Y_AXIS, WIDTH, HEIGHT);
			}

			if (shiftXAxis == 0) {
				newConcept = new Ellipse2D.Double(ELLIPSE_X_AXIS + shiftXAxis
						+ adjustment, ELLIPSE_Y_AXIS,
						WIDTH + distortionOld + 6, HEIGHT + distortionOld + 6);
			} else {
				newConcept = new Ellipse2D.Double(ELLIPSE_X_AXIS + shiftXAxis
						+ adjustment, ELLIPSE_Y_AXIS, WIDTH + distortionOld,
						HEIGHT + distortionOld);
			}
			this.renderPlus();
		}
	}

	private void renderPlus() {
		if (eval != null) {
			coveredIndividualSize = ((EvaluatedDescriptionClass) eval)
					.getCoveredInstances().size();
			double newConcepts = ((EvaluatedDescriptionClass) eval)
					.getAddition();
			double oldConcepts = ((EvaluatedDescriptionClass) eval)
					.getCoverage();
			shiftNewConcept = 0;
			shiftOldConcept = 0;
			shiftNewConceptX = 0;
			shiftCovered = 0;
			if (coveredIndividualSize == 0) {
				shiftNewConcept = (int) Math.round(((WIDTH) / 2.0) * newConcepts);
			} else if (additionalIndividualSize != coveredIndividualSize) {
				shiftNewConcept = (int) Math.round(((WIDTH) / 2.0)
						* (1.0 + (1.0 - oldConcepts)));
				shiftOldConcept = (int) Math.round(((WIDTH) / 2.0) * oldConcepts);
				shiftCovered = (int) Math.round(((WIDTH) / 2.0)
						* (1 - oldConcepts));
			}
			if (((EvaluatedDescriptionClass) eval).getAddition() != 1.0 && ((EvaluatedDescriptionClass) eval)
					.getCoverage() == 1.0) {
				shiftCovered = (int) Math.round(((WIDTH) / 2.0) * 0.625);
				shiftNewConceptX = shiftCovered;
				shiftNewConcept = 2 * shiftNewConceptX;
			}
		}

		int i = conceptNew.length();
		while (i > 0) {
			int sub = 0;
			String subString = "";
			if(conceptNew.contains(" ")) {
			sub = conceptNew.indexOf(" ");
			subString = conceptNew.substring(0, sub) + " ";
			conceptNew = conceptNew.replace(conceptNew.substring(0, sub + 1),
					"");
			} else {
				subString = conceptNew;
				conceptNew = "";
			}
			while (sub < SUBSTRING_SIZE) {
				if (conceptNew.length() > 0 && conceptNew.contains(" ")) {
					sub = conceptNew.indexOf(" ");
					if (subString.length() + sub < SUBSTRING_SIZE) {
						subString = subString + conceptNew.substring(0, sub)
								+ " ";
						conceptNew = conceptNew.replace(conceptNew.substring(0,
								sub + 1), "");
						sub = subString.length();
					} else {
						break;
					}
				} else {
					if (subString.length() + conceptNew.length() > SUBSTRING_SIZE
							+ SPACE_SIZE) {
						conceptVector.add(subString);
						subString = conceptNew;
						conceptNew = "";
						break;
					} else {
						subString = subString + conceptNew;
						conceptNew = "";
						break;
					}
				}
			}
			conceptVector.add(subString);
			i = conceptNew.length();
		}
	}

	private void computeIndividualPoints() {
		posCovIndVector.clear();
		posNotCovIndVector.clear();
		additionalIndividuals.clear();
		points.clear();
		if (eval != null) {
			Set<OWLIndividual> posInd = ((EvaluatedDescriptionClass) eval)
					.getCoveredInstances();
			int i = 0;
			double x = random.nextInt(MAX_RANDOM_NUMBER);
			double y = random.nextInt(MAX_RANDOM_NUMBER);
			boolean flag = true;
			for (OWLIndividual ind : posInd) {
				flag = true;
				if (i < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (newConcept.contains(x, y)
								&& oldConcept.contains(x, y)
								&& !(x >= this.getX1() + this.getShiftCovered()
										&& x <= this.getX2()
												+ this.getShiftCovered()
										&& y >= this.getY1() && y <= this
										.getY2())) {
							posCovIndVector.add(new IndividualPoint("*",
									(int) x, (int) y, Manager.getInstance().getRendering(ind), ind, ""));
							i++;
							flag = false;

							x = random.nextInt(MAX_RANDOM_NUMBER);
							y = random.nextInt(MAX_RANDOM_NUMBER);
							break;
						} else {
							x = random.nextInt(MAX_RANDOM_NUMBER);
							y = random.nextInt(MAX_RANDOM_NUMBER);
						}

					}
				}
			}

			Set<OWLIndividual> posNotCovInd = ((EvaluatedDescriptionClass) eval)
					.getAdditionalInstances();
			int j = 0;
			x = random.nextInt(MAX_RANDOM_NUMBER);
			y = random.nextInt(MAX_RANDOM_NUMBER);
			for (OWLIndividual ind : posNotCovInd) {
				flag = true;
				if (j < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (!oldConcept.contains(x, y)
								&& newConcept.contains(x, y)
								&& !(x >= this.getX1()
										+ this.getShiftNewConcept()
										&& x <= this.getX2()
												+ this.getShiftNewConcept()
										&& y >= this.getY1() && y <= this
										.getY2())
								&& !(x >= this.getX1()
										+ this.getShiftNewConceptX()
										&& x <= this.getX2()
												+ this.getShiftNewConceptX()
										&& y >= this.getY1()
												+ this.getShiftNewConcept() && y <= this
										.getY2()
										+ this.getShiftNewConcept())) {
							if (id.equals(EQUI_STRING)) {
								posNotCovIndVector.add(new IndividualPoint("*",
										(int) x, (int) y, Manager.getInstance().getRendering(ind), ind, ""));
							} else {
								additionalIndividuals.add(new IndividualPoint("*",
										(int) x, (int) y, Manager.getInstance().getRendering(ind), ind, ""));
							}
							j++;
							flag = false;
							x = random.nextInt(MAX_RANDOM_NUMBER);
							y = random.nextInt(MAX_RANDOM_NUMBER);
							break;
						} else {
							x = random.nextInt(MAX_RANDOM_NUMBER);
							y = random.nextInt(MAX_RANDOM_NUMBER);
						}

					}
				}
			}

			Set<OWLIndividual> notCovInd = Manager.getInstance().getIndividuals();
			notCovInd.removeAll(posInd);
			notCoveredInd = notCovInd.size();
			int k = 0;
			x = random.nextInt(MAX_RANDOM_NUMBER);
			y = random.nextInt(MAX_RANDOM_NUMBER);
			for (OWLIndividual ind : notCovInd) {
				flag = true;
				if (k < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (oldConcept.contains(x, y)
								&& !newConcept.contains(x, y)
								&& !(x >= this.getX1()
										- this.getShiftOldConcept()
										&& x <= this.getX2()
												- this.getShiftOldConcept()
										&& y >= this.getY1() && y <= this
										.getY2())) {
							posNotCovIndVector.add(new IndividualPoint("*",
									(int) x, (int) y, Manager.getInstance().getRendering(ind), ind, ""));
							k++;
							flag = false;
							x = random.nextInt(MAX_RANDOM_NUMBER);
							y = random.nextInt(MAX_RANDOM_NUMBER);
							break;
						} else {
							x = random.nextInt(MAX_RANDOM_NUMBER);
							y = random.nextInt(MAX_RANDOM_NUMBER);
						}

					}
				}
			}
			points.addAll(posCovIndVector);
			points.addAll(posNotCovIndVector);
			points.addAll(additionalIndividuals);
		}
	}

	/**
	 * This method returns a Vector of all individuals that are drawn in the
	 * panel.
	 * 
	 * @return Vector of Individuals
	 */
	public Vector<IndividualPoint> getIndividualVector() {
		return points;
	}

	/**
	 * This method returns the GraphicalCoveragePanel.
	 * 
	 * @return GraphicalCoveragePanel
	 */
	public GraphicalCoveragePanel getGraphicalCoveragePanel() {
		return this;
	}

	/**
	 * Returns the min. x value of the plus.
	 * 
	 * @return int min X Value
	 */
	public int getX1() {
		return x1;
	}

	/**
	 * Returns the max. x value of the plus.
	 * 
	 * @return int max X Value
	 */
	public int getX2() {
		return x2;
	}

	/**
	 * Returns the min. y value of the plus.
	 * 
	 * @return int min Y Value
	 */
	public int getY1() {
		return y1;
	}

	/**
	 * Returns the max. y value of the plus.
	 * 
	 * @return int max Y Value
	 */
	public int getY2() {
		return y2;
	}

	/**
	  * This method returns how much the old concept must be shifted. 
	 * @return shift of the old concept
	 */
	public int getShiftOldConcept() {
		return shiftOldConcept;
	}

	/**
	  * This method returns how much the plus in the middle must be shifted. 
	 * @return shift of the middle plus
	 */
	public int getShiftCovered() {
		return shiftCovered;
	}

	/**
	 * This method returns how much the new concept must be shifted. 
	 * @return shift of the new concept
	 */
	public int getShiftNewConcept() {
		return shiftNewConcept;
	}

	/**
	 * This method returns how much the new concept must be shifted. 
	 * @return shift of the new concept
	 */
	public int getShiftNewConceptX() {
		return shiftNewConceptX;
	}

	/**
	 * Unsets the panel after plugin is closed.
	 */
	public void unsetPanel() {
		this.removeAll();
		eval = null;
	}

	/**
	 * Returns the currently selected evaluated description.
	 * 
	 * @return EvaluatedDescription
	 */
	public EvaluatedDescription getEvaluateddescription() {
		return eval;
	}
	
}
