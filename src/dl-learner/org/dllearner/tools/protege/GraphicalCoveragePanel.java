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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;

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
	private static final String EQUI_STRING = "equivalent class";
	private final String id;
	private int shiftXAxis;
	private int distortionOld;
	private final Ellipse2D oldConcept;
	private Ellipse2D newConcept;

	private EvaluatedDescription eval;
	private final DLLearnerModel model;
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
	private final MoreDetailForSuggestedConceptsPanel panel;

	/**
	 * 
	 * This is the constructor for the GraphicalCoveragePanel.
	 * 
	 * @param desc
	 *            EvaluatedDescription
	 * @param m
	 *            DLLearnerModel
	 * @param concept
	 *            String
	 * @param p
	 *            MoreDetailForSuggestedConceptsPanel
	 */
	public GraphicalCoveragePanel(EvaluatedDescription desc, DLLearnerModel m,
			String concept, MoreDetailForSuggestedConceptsPanel p) {
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT + 100));
		this.setVisible(false);
		this.setForeground(Color.GREEN);
		this.repaint();
		eval = desc;
		model = m;
		panel = p;
		id = model.getID();
		darkGreen = new Color(0, 100, 0);
		darkRed = new Color(205, 0, 0);
		random = new Random();
		conceptNew = concept;
		conceptVector = new Vector<String>();
		posCovIndVector = new Vector<IndividualPoint>();
		posNotCovIndVector = new Vector<IndividualPoint>();
		additionalIndividuals = new Vector<IndividualPoint>();
		points = new Vector<IndividualPoint>();
		this.computeGraphics();
		handler = new GraphicalCoveragePanelHandler(this, desc, model);
		oldConcept = new Ellipse2D.Float(ELLIPSE_X_AXIS + (2 * adjustment),
				ELLIPSE_Y_AXIS, WIDTH, HEIGHT);
		newConcept = new Ellipse2D.Float(ELLIPSE_X_AXIS + shiftXAxis
				+ adjustment, ELLIPSE_Y_AXIS, WIDTH + distortionOld, HEIGHT
				+ distortionOld);
		this.computeIndividualPoints();
		this.addMouseMotionListener(handler);
		this.addMouseListener(handler);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (eval != null) {
			Graphics2D g2D;
			g2D = (Graphics2D) g;

			AlphaComposite ac = AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f);
			g2D.setColor(Color.BLACK);
			g2D.drawString(model.getOldConceptOWLAPI().toString(), 320, 10);
			g2D.setColor(Color.ORANGE);
			g2D.fillOval(310, 20, 9, 9);
			g2D.setColor(Color.black);
			int p = 30;
			for (int i = 0; i < conceptVector.size(); i++) {
				g2D.drawString(conceptVector.get(i), 320, p);
				p = p + 20;
			}

			g2D.setColor(Color.YELLOW);
			g2D.fill(oldConcept);
			g2D.fillOval(310, 0, 9, 9);
			g2D.setColor(Color.ORANGE);
			g2D.setComposite(ac);
			g2D.fill(newConcept);
			g2D.setColor(Color.BLACK);

			// Plus 1
			if (coveredIndividualSize != model.getReasoner().getIndividuals(
					model.getCurrentConcept()).size()
					&& coveredIndividualSize != 0) {
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
			if (coveredIndividualSize != model.getReasoner().getIndividuals(
					model.getCurrentConcept()).size()) {
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
				g2D.drawString(posCovIndVector.get(i).getPoint(),
						posCovIndVector.get(i).getXAxis(), posCovIndVector.get(
								i).getYAxis());
			}

			for (int i = 0; i < posNotCovIndVector.size(); i++) {
				g2D.setColor(darkRed);
				g2D.drawString(posNotCovIndVector.get(i).getPoint(),
						posNotCovIndVector.get(i).getXAxis(),
						posNotCovIndVector.get(i).getYAxis());
			}

			for (int i = 0; i < additionalIndividuals.size(); i++) {
				g2D.setColor(Color.BLACK);
				g2D.drawString(additionalIndividuals.get(i).getPoint(),
						additionalIndividuals.get(i).getXAxis(),
						additionalIndividuals.get(i).getYAxis());
			}
			this.setVisible(true);
			panel.repaint();
		}
	}

	private void computeGraphics() {
		if (eval != null) {
			this.setVisible(true);
			panel.repaint();
			additionalIndividualSize = ((EvaluatedDescriptionClass) eval)
					.getAdditionalInstances().size();
			distortionOld = 0;
			adjustment = 0;
			Ellipse2D old = new Ellipse2D.Float(ELLIPSE_X_AXIS, ELLIPSE_Y_AXIS,
					WIDTH, HEIGHT);
			x1 = (int) old.getCenterX() - PLUS_SIZE;
			x2 = (int) old.getCenterX() + PLUS_SIZE;
			y1 = (int) old.getCenterY() - PLUS_SIZE;
			y2 = (int) old.getCenterY() + PLUS_SIZE;
			centerX = (int) old.getCenterX();
			centerY = (int) old.getCenterY();
			double coverage = ((EvaluatedDescriptionClass) eval).getCoverage();
			shiftXAxis = (int) Math.round(WIDTH * (1 - coverage));
			if (additionalIndividualSize != 0) {
				distortionOld = (int) Math.round(WIDTH * 0.3);
				newConcept = new Ellipse2D.Float(ELLIPSE_X_AXIS + shiftXAxis,
						ELLIPSE_Y_AXIS, WIDTH, HEIGHT);
				adjustment = (int) Math.round(newConcept.getCenterY() / 4);
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
				shiftNewConcept = (int) Math.round((WIDTH / 2.0) * newConcepts);
			} else if (additionalIndividualSize != coveredIndividualSize) {
				shiftNewConcept = (int) Math.round((WIDTH / 2.0)
						* (newConcepts + (1 - oldConcepts)));
				shiftOldConcept = (int) Math.round((WIDTH / 2.0) * oldConcepts);
				shiftCovered = (int) Math.round((WIDTH / 2.0)
						* (1 - oldConcepts));
			}
			if (((EvaluatedDescriptionClass) eval).getAddition() != 1.0) {
				shiftCovered = (int) Math.round((WIDTH / 2.0) * 0.625);
				shiftNewConceptX = shiftCovered;
				shiftNewConcept = 2 * shiftNewConceptX;
			}
		}

		int i = conceptNew.length();
		while (i > 0) {
			int sub = conceptNew.indexOf(" ");
			String subString = conceptNew.substring(0, sub) + " ";
			conceptNew = conceptNew.replace(conceptNew.substring(0, sub+1), "");
			while(sub < SUBSTRING_SIZE) {
				if(conceptNew.length() > 0 && conceptNew.contains(" ")) {
				sub = conceptNew.indexOf(" ");
				if(subString.length() + sub < SUBSTRING_SIZE) {
				subString = subString +  conceptNew.substring(0, sub) + " ";
				conceptNew = conceptNew.replace(conceptNew.substring(0, sub+1), "");
				System.out.println("string: " + subString + " lenght: " + subString.length());
				sub = subString.length();
				} else {
					break;
				}
				} else {
					if(subString.length() + conceptNew.length() > SUBSTRING_SIZE + SPACE_SIZE) {
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
		if (eval != null) {
			Set<Individual> posInd = ((EvaluatedDescriptionClass) eval)
					.getCoveredInstances();
			int i = 0;
			double x = random.nextInt(300);
			double y = random.nextInt(300);
			boolean flag = true;
			for (Individual ind : posInd) {
				flag = true;
				if (i < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (this.isInCircle(x, y, newConcept)
								&& this.isInCircle(x, y, oldConcept)
								&& !(x >= this.getX1() + this.getShiftCovered()
										- 5
										&& x <= this.getX2()
												+ this.getShiftCovered() + 5
										&& y >= this.getY1() - 5 && y <= this
										.getY2() + 5)) {
							posCovIndVector.add(new IndividualPoint("*",
									(int) x, (int) y, ind.toString()));
							i++;
							flag = false;

							x = random.nextInt(300);
							y = random.nextInt(300);
							break;
						} else {
							x = random.nextInt(300);
							y = random.nextInt(300);
						}

					}
				}
			}

			Set<Individual> posNotCovInd = ((EvaluatedDescriptionClass) eval)
					.getAdditionalInstances();
			int j = 0;
			x = random.nextInt(300);
			y = random.nextInt(300);
			for (Individual ind : posNotCovInd) {
				flag = true;
				if (j < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (!this.isInCircle(x, y, oldConcept)
								&& this.isInCircle(x, y, newConcept)
								&& !(x >= this.getX1()
										+ this.getShiftNewConcept() - 5
										&& x <= this.getX2()
												+ this.getShiftNewConcept() + 5
										&& y >= this.getY1() - 5 && y <= this
										.getY2() + 5)
								&& !(x >= this.getX1()
										+ this.getShiftNewConceptX() - 5
										&& x <= this.getX2()
												+ this.getShiftNewConceptX()
												+ 5
										&& y >= this.getY1()
												+ this.getShiftNewConcept() - 5 && y <= this
										.getY2()
										+ 5 + this.getShiftNewConcept())) {
							if (id.equals(EQUI_STRING)) {
								posNotCovIndVector.add(new IndividualPoint("*",
										(int) x, (int) y, ind.toString()));
							} else {
								additionalIndividuals.add(new IndividualPoint(
										"*", (int) x, (int) y, ind.toString()));
							}
							j++;
							flag = false;
							x = random.nextInt(300);
							y = random.nextInt(300);
							break;
						} else {
							x = random.nextInt(300);
							y = random.nextInt(300);
						}

					}
				}
			}

			Set<Individual> notCovInd = model.getReasoner().getIndividuals(
					model.getCurrentConcept());
			notCovInd.removeAll(posInd);
			int k = 0;
			x = random.nextInt(300);
			y = random.nextInt(300);
			for (Individual ind : notCovInd) {
				flag = true;
				if (k < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (this.isInCircle(x, y, oldConcept)
								&& !this.isInCircle(x, y, newConcept)
								&& !(x >= this.getX1() - 5
										- this.getShiftOldConcept()
										&& x <= this.getX2() + 5
												- this.getShiftOldConcept()
										&& y >= this.getY1() - 5 && y <= this
										.getY2() + 5)) {
							posNotCovIndVector.add(new IndividualPoint("*",
									(int) x, (int) y, ind.toString()));
							k++;
							flag = false;
							x = random.nextInt(300);
							y = random.nextInt(300);
							break;
						} else {
							x = random.nextInt(300);
							y = random.nextInt(300);
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
	 * This method returns the MoreDetailForSuggestedConceptsPanel.
	 * 
	 * @return MoreDetailForSuggestedConceptsPanel
	 */
	public MoreDetailForSuggestedConceptsPanel getMoreDetailForSuggestedConceptsPanel() {
		return panel;
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
	 * 
	 * @return
	 */
	public int getShiftOldConcept() {
		return shiftOldConcept;
	}

	/**
	 * 
	 * @return
	 */
	public int getShiftCovered() {
		return shiftCovered;
	}

	/**
	 * 
	 * @return
	 */
	public int getShiftNewConcept() {
		return shiftNewConcept;
	}

	/**
	 * 
	 * @return
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

	public boolean isInCircle(double x, double y, Ellipse2D ell) {
		int r = (WIDTH / 2) * (WIDTH / 2);
		int n = (int) ((x - ell.getCenterX()) * (x - ell.getCenterX()))
				+ (int) ((y - ell.getCenterY()) * (y - ell.getCenterY()));
		if (n <= r) {
			return true;
		} else {
			return false;
		}
	}
}
