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
import java.util.Set;
import java.util.Vector;

import javax.swing.JPanel;

import org.dllearner.algorithms.EvaluatedDescriptionClass;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Individual;

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
	private static final int ELLIPSE_Y_AXIS = 25;
	private static final int MAX_NUMBER_OF_INDIVIDUAL_POINTS = 20;
	private static final int PLUS_SIZE = 5;
	private static final int GAP = 20;
	private int shiftXAxis;
	private int distortionOld;
	private final Ellipse2D oldConcept;
	private Ellipse2D newConcept;

	private EvaluatedDescription eval;
	private final DLLearnerModel model;
	private final String conceptNew;
	private final Vector<IndividualPoint> posCovIndVector;
	private final Vector<IndividualPoint> posNotCovIndVector;
	private final Vector<IndividualPoint> points;
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
	 * @param w
	 *            width
	 * @param h
	 *            height
	 * @param p
	 *            MoreDetailForSuggestedConceptsPanel
	 */
	public GraphicalCoveragePanel(EvaluatedDescription desc, DLLearnerModel m,
			String concept, int w, int h, MoreDetailForSuggestedConceptsPanel p) {
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT + 100));
		this.setVisible(false);
		this.setForeground(Color.GREEN);
		this.repaint();
		eval = desc;
		model = m;
		panel = p;
		conceptNew = concept;
		posCovIndVector = new Vector<IndividualPoint>();
		posNotCovIndVector = new Vector<IndividualPoint>();
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
		// this.addPropertyChangeListener(handler);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (eval != null) {
			Graphics2D g2D;
			g2D = (Graphics2D) g;

			AlphaComposite ac = AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f);
			g2D.setColor(Color.YELLOW);
			g2D.fill(oldConcept);
			g2D.setColor(Color.BLACK);
			g2D.drawString(model.getOldConceptOWLAPI().toString(), 10, 15);
			g2D.setColor(Color.BLACK);
			g2D.drawString(conceptNew, 10 + WIDTH, 15);
			g2D.setComposite(ac);
			g2D.setColor(Color.ORANGE);
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

			if (((EvaluatedDescriptionClass) eval).getAddition() != 1.0) {
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
				g2D.setColor(Color.GREEN);
				g2D.drawString(posCovIndVector.get(i).getPoint(),
						posCovIndVector.get(i).getXAxis(), posCovIndVector.get(
								i).getYAxis());
			}

			for (int i = 0; i < posNotCovIndVector.size(); i++) {
				g2D.setColor(Color.RED);
				g2D.drawString(posNotCovIndVector.get(i).getPoint(),
						posNotCovIndVector.get(i).getXAxis(),
						posNotCovIndVector.get(i).getYAxis());
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
			double newConcept = ((EvaluatedDescriptionClass) eval)
					.getAddition();
			double oldConcept = ((EvaluatedDescriptionClass) eval)
					.getCoverage();
			shiftNewConcept = 0;
			shiftOldConcept = 0;
			shiftNewConceptX = 0;
			shiftCovered = 0;
			if (coveredIndividualSize == 0) {
				shiftNewConcept = (int) Math.round((WIDTH / 2.0) * newConcept);
			} else if (additionalIndividualSize != coveredIndividualSize) {
				shiftNewConcept = (int) Math.round((WIDTH / 2.0)
						* (newConcept + (1 - oldConcept)));
				shiftOldConcept = (int) Math.round((WIDTH / 2.0) * oldConcept);
				shiftCovered = (int) Math.round((WIDTH / 2.0)
						* (1 - oldConcept));
			}
			if (((EvaluatedDescriptionClass) eval).getAddition() != 1.0) {
				shiftCovered = (int) Math.round((WIDTH / 2.0) * 0.625);
				shiftNewConceptX = shiftCovered;
				shiftNewConcept = 2 * shiftNewConceptX;
			}
		}
	}

	private void computeIndividualPoints() {
		if (eval != null) {
			Set<Individual> posInd = ((EvaluatedDescriptionClass) eval)
					.getCoveredInstances();
			int i = 0;
			double x = 100;
			double y = 100;
			boolean flag = true;
			for (Individual ind : posInd) {
				flag = true;
				if (i < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (x >= oldConcept.getMaxX()) {
							x = (int) oldConcept.getMinX();
							y = y + GAP;
						}

						if (y >= oldConcept.getMaxY()) {
							y = (int) oldConcept.getMinY();
						}

						if (x >= newConcept.getMaxX()) {
							x = (int) newConcept.getMinX();
							y = y + GAP;
						}

						if (y >= newConcept.getMaxY()) {
							y = (int) newConcept.getMinY();
							break;
						}

						while (x < newConcept.getMaxX()) {

							if (newConcept.contains(x, y)
									&& oldConcept.contains(x, y)) {
								posCovIndVector.add(new IndividualPoint("*",
										(int) x, (int) y, ind.toString()));
								i++;
								flag = false;
								x = x + GAP;
								break;
							} else {
								x = x + GAP;
							}
						}
					}
				}
			}

			Set<Individual> posNotCovInd = ((EvaluatedDescriptionClass) eval)
					.getAdditionalInstances();
			int j = 0;
			x = 100;
			y = 100;
			for (Individual ind : posNotCovInd) {
				flag = true;
				if (j < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (x >= newConcept.getMaxX()) {
							x = (int) oldConcept.getMinX();
							y = y + GAP;
						}

						if (y >= newConcept.getMaxY()) {
							y = (int) oldConcept.getMinY();
							break;
						}

						while (x < newConcept.getMaxX()) {

							if (!oldConcept.contains(x, y)
									&& newConcept.contains(x, y)) {
								posNotCovIndVector.add(new IndividualPoint("*",
										(int) x, (int) y, ind.toString()));
								j++;
								flag = false;
								x = x + GAP;
								break;
							} else {
								x = x + GAP;
							}
						}
					}
				}
			}

			Set<Individual> notCovInd = model.getReasoner().getIndividuals(
					model.getCurrentConcept());
			notCovInd.removeAll(posInd);
			int k = 0;
			x = 100;
			y = 100;
			for (Individual ind : notCovInd) {
				flag = true;
				if (k < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
					while (flag) {
						if (x >= oldConcept.getMaxX()) {
							x = (int) oldConcept.getMinX();
							y = y + GAP;
						}

						if (y >= oldConcept.getMaxY()) {
							y = (int) oldConcept.getMinY();
							break;
						}

						while (x < oldConcept.getMaxX()) {

							if (oldConcept.contains(x, y)
									&& !newConcept.contains(x, y)) {
								posNotCovIndVector.add(new IndividualPoint("*",
										(int) x, (int) y, ind.toString()));
								k++;
								flag = false;
								x = x + GAP;
								break;
							} else {
								x = x + GAP;
							}
						}
					}
				}
			}
			points.addAll(posCovIndVector);
			points.addAll(posNotCovIndVector);
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

	public int getX1() {
		return x1;
	}

	public int getX2() {
		return x2;
	}

	public int getY1() {
		return y1;
	}

	public int getY2() {
		return y2;
	}

	public int getShiftOldConcept() {
		return shiftOldConcept;
	}

	public int getShiftCovered() {
		return shiftCovered;
	}

	public int getShiftNewConcept() {
		return shiftNewConcept;
	}

	public int getShiftNewConceptX() {
		return shiftNewConceptX;
	}

	public void unsetPanel() {
		this.removeAll();
		eval = null;
	}
	
	public EvaluatedDescription getEvaluateddescription() {
		return eval;
	}
}
