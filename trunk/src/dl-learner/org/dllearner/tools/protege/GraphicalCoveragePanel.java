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
 * @author Christian Koetteritzsch
 *
 */
public class GraphicalCoveragePanel extends JPanel {

	private static final long serialVersionUID = 855436961912515267L;
	private static final int HEIGHT =250;
	private static final int WIDTH = 250;
	private static final int MAX_NUMBER_OF_INDIVIDUAL_POINTS = 20;
	private static final int GAP = 20;
	private int shiftXAxis;
	private int distortionOld;
	private final Ellipse2D oldConcept;
	private Ellipse2D newConcept;

	private final EvaluatedDescription eval;
	private final DLLearnerModel model;
	private final String conceptNew;
	private final Vector<IndividualPoint> posCovIndVector;
	private final Vector<IndividualPoint> posNotCovIndVector;
	private final Vector<IndividualPoint> points;
	private final GraphicalCoveragePanelHandler handler;
	private int adjustment;
	private final MoreDetailForSuggestedConceptsPanel panel;

	/**
	 * 
	 * This is the constructor for the GraphicalCoveragePanel.
	 *
	 * @param desc EvaluatedDescription
	 * @param m DLLearnerModel
	 * @param concept String
	 * @param w width
	 * @param h height
	 * @param p MoreDetailForSuggestedConceptsPanel
	 */
	public GraphicalCoveragePanel(EvaluatedDescription desc, DLLearnerModel m, String concept, int w, int h, MoreDetailForSuggestedConceptsPanel p) {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setVisible(true);
		setForeground(Color.GREEN);
		repaint();
		eval = desc;
		model = m;
		panel = p;
		conceptNew = concept;
		posCovIndVector = new Vector<IndividualPoint>();
		posNotCovIndVector = new Vector<IndividualPoint>();
		points = new Vector<IndividualPoint>();
		this.computeGraphics();
		handler = new GraphicalCoveragePanelHandler(this);
		oldConcept = new Ellipse2D.Float(5, 25+adjustment, WIDTH-distortionOld, HEIGHT-distortionOld);
		newConcept = new Ellipse2D.Float(5+shiftXAxis, 25, WIDTH, HEIGHT);
		this.computeIndividualPoints();
		this.addMouseMotionListener(handler);
		this.addPropertyChangeListener(handler);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2D;
		g2D = (Graphics2D) g;
		g2D.setColor(Color.GREEN);
		g2D.draw(oldConcept);
		g2D.drawString(model.getOldConceptOWLAPI().toString(), 10, 15);
		g2D.setColor(Color.RED);
		g2D.draw(newConcept);
		g2D.drawString(conceptNew, 10 + WIDTH, 15);
		
		
		
		for(int i = 0; i < posCovIndVector.size(); i++) {
			g2D.setColor(Color.BLACK);
			g2D.drawString(posCovIndVector.get(i).getPoint(), posCovIndVector.get(i).getXAxis(), posCovIndVector.get(i).getYAxis());
		}
		
		for(int i = 0; i < posNotCovIndVector.size(); i++) {
			g2D.setColor(Color.BLACK);
			g2D.drawString(posNotCovIndVector.get(i).getPoint(), posNotCovIndVector.get(i).getXAxis(), posNotCovIndVector.get(i).getYAxis());
		}

	}
	
	private void computeGraphics(){
		int add = ((EvaluatedDescriptionClass) eval).getAdditionalInstances().size();
		distortionOld = 0;
		adjustment = 0;
		double additional = ((EvaluatedDescriptionClass) eval).getAddition();
		double coverage = ((EvaluatedDescriptionClass) eval).getCoverage();
		shiftXAxis = (int) Math.round(WIDTH* (1-coverage));
		if(add != 0) {
			distortionOld = (int) Math.round(WIDTH*additional);
			newConcept = new Ellipse2D.Float(5+shiftXAxis, 25, WIDTH, HEIGHT);
			adjustment = (int) Math.round(newConcept.getCenterY()/4);
			
		}
		 
	}
	
	private void computeIndividualPoints() {
		Set<Individual> posInd = ((EvaluatedDescriptionClass) eval).getCoveredInstances();

		int i = 0;
		double x = 20;
		double y = 20;
		boolean flag = true;
		for(Individual ind : posInd) {
			flag = true;
			if(i<MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
				while(flag) {
					if(x >= oldConcept.getMaxX()) {
						x = (int) oldConcept.getMinX();
						y = y + GAP;
					}
				
					if(y >= oldConcept.getMaxY()) {
						y = (int) oldConcept.getMinY();
					}
				
					if(x >= newConcept.getMaxX()) {
						x = (int) newConcept.getMinX();
						y = y + GAP;
					}
				
					if(y >= newConcept.getMaxY()) {
						y = (int) newConcept.getMinY();
						break;
					}
				
					while(x < newConcept.getMaxX()) {
					
						if(newConcept.contains(x, y) && oldConcept.contains(x, y)) {
							posCovIndVector.add(new IndividualPoint("+", (int) x, (int) y, ind.toString()));
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
		
		Set<Individual> posNotCovInd = ((EvaluatedDescriptionClass) eval).getAdditionalInstances();
		int j = 0;
		x = 20;
		y = 20;
		for(Individual ind : posNotCovInd) {
			flag = true;
			if(j<MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
				while(flag) {
					if(x >= newConcept.getMaxX()) {
						x = (int) oldConcept.getMinX();
						y = y + GAP;
					}
				
					if(y >= newConcept.getMaxY()) {
						y = (int) oldConcept.getMinY();
						break;
					}
				
					while(x < newConcept.getMaxX()) {
					
						if(!oldConcept.contains(x, y) && newConcept.contains(x, y)) {
							posNotCovIndVector.add(new IndividualPoint("-", (int) x, (int) y, ind.toString()));
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
		
		Set<Individual> notCovInd = model.getReasoner().getIndividuals(model.getCurrentConcept());
		notCovInd.removeAll(posInd);
		int k = 0;
		x = 20;
		y = 20;
		for(Individual ind : notCovInd) {
			flag = true;
			if(k < MAX_NUMBER_OF_INDIVIDUAL_POINTS) {
				while(flag) {
					if(x >= oldConcept.getMaxX()) {
						x = (int) oldConcept.getMinX();
						y = y + GAP;
					}
				
					if(y >= oldConcept.getMaxY()) {
						y = (int) oldConcept.getMinY();
						break;
					}
				
					while(x < oldConcept.getMaxX()) {
					
						if(oldConcept.contains(x, y) && !newConcept.contains(x, y)) {
							posNotCovIndVector.add(new IndividualPoint("-", (int) x, (int) y, ind.toString()));
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
	
	/**
	 * This method returns a Vector of all individuals that are drawn
	 * in the panel.
	 * @return Vector of Individuals
	 */
	public Vector<IndividualPoint> getIndividualVector() {
		return points;
	}
	
	/**
	 * This method returns the GraphicalCoveragePanel.
	 * @return GraphicalCoveragePanel
	 */
	public GraphicalCoveragePanel getGraphicalCoveragePanel() {
		return this;
	}
	
	/**
	 * This method returns the MoreDetailForSuggestedConceptsPanel.
	 * @return MoreDetailForSuggestedConceptsPanel
	 */
	public MoreDetailForSuggestedConceptsPanel getMoreDetailForSuggestedConceptsPanel() {
		return panel;
	}
	
	
	
}
