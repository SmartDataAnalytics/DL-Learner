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

public class GraphicalCoveragePanel extends JPanel {

	private static final long serialVersionUID = 855436961912515267L;
	private static final int height =250;
	private static final int width = 250;
	private static final int maxNumberOfIndividualPoints = 20;
	private static final int gap = 20;
	private int shiftXAxis;
	private int distortionOld;
	private Ellipse2D oldConcept;
	private Ellipse2D newConcept;

	private EvaluatedDescription eval;
	private DLLearnerModel model;
	private String conceptNew;
	private Vector<IndividualPoint> posCovIndVector;
	private Vector<IndividualPoint> posNotCovIndVector;
	private Vector<IndividualPoint> points;
	private GraphicalCoveragePanelHandler handler;
	private int adjustment;
	private MoreDetailForSuggestedConceptsPanel panel;

	/**
	 * This is the constructor for the GraphicalCoveragePanel.
	 */
	public GraphicalCoveragePanel(EvaluatedDescription desc, DLLearnerModel m, String concept, int w, int h, MoreDetailForSuggestedConceptsPanel p) {
		setPreferredSize(new Dimension(width, height));
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
		oldConcept = new Ellipse2D.Float(5, 25+adjustment, width-distortionOld, height-distortionOld);
		newConcept = new Ellipse2D.Float(5+shiftXAxis, 25, width, height);
		this.computeIndividualPoints();
		this.addMouseMotionListener(handler);
		this.addPropertyChangeListener(handler);
	}

	public void drawCoverageForLearnedClassDescription(
			Set<Individual> posCovInd, Set<Individual> posNotCovInd,
			Set<Individual> negCovInd) {
		

	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2D;
		g2D = (Graphics2D) g;
		g2D.setColor(Color.GREEN);
		g2D.draw (oldConcept);
		g2D.drawString(model.getOldConceptOWLAPI().toString(), 10, 15);
		g2D.setColor(Color.RED);
		g2D.draw (newConcept);
		g2D.drawString(conceptNew, 10 + width, 15);
		
		
		
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
		int add = ((EvaluatedDescriptionClass)eval).getAdditionalInstances().size();
		distortionOld = 0;
		adjustment = 0;
		double additional = ((EvaluatedDescriptionClass)eval).getAddition();
		double coverage = ((EvaluatedDescriptionClass)eval).getCoverage();
		shiftXAxis = (int) Math.round(width* (1-coverage));
		if(add != 0) {
			distortionOld = (int) Math.round(width*additional);
			newConcept = new Ellipse2D.Float(5+shiftXAxis, 25, width, height);
			adjustment = (int) Math.round(newConcept.getCenterY()/4);
			
		}
		 
	}
	
	private void computeIndividualPoints() {
		Set<Individual> posInd = ((EvaluatedDescriptionClass)eval).getCoveredInstances();

		int i = 0;
		double x = 20;
		double y = 20;
		boolean flag = true;
		for(Individual ind : posInd) {
			flag = true;
			if(i<maxNumberOfIndividualPoints) {
				while(flag) {
					if(x >= oldConcept.getMaxX()) {
						x = (int) oldConcept.getMinX();
						y = y + gap;
					}
				
					if(y >= oldConcept.getMaxY()) {
						y = (int) oldConcept.getMinY();
					}
				
					if(x >= newConcept.getMaxX()) {
						x = (int) newConcept.getMinX();
						y = y + gap;
					}
				
					if(y >= newConcept.getMaxY()) {
						y = (int) newConcept.getMinY();
					}
				
					while(x < newConcept.getMaxX()) {
					
						if(newConcept.contains(x, y) && oldConcept.contains(x, y)) {
							posCovIndVector.add(new IndividualPoint("+",(int)x,(int)y,ind.toString()));
							i++;
							flag = false;
							x = x + gap;
							break;
						} else {
							x = x + gap;
						}
					}
				}
			}
		}
		
		Set<Individual> posNotCovInd = ((EvaluatedDescriptionClass)eval).getAdditionalInstances();
		int j = 0;
		x = 20;
		y = 20;
		for(Individual ind : posNotCovInd) {
			flag = true;
			if(j<maxNumberOfIndividualPoints) {
				while(flag) {
					if(x >= newConcept.getMaxX()) {
						x = (int) oldConcept.getMinX();
						y = y + gap;
					}
				
					if(y >= newConcept.getMaxY()) {
						y = (int) oldConcept.getMinY();
					}
				
					while(x < newConcept.getMaxX()) {
					
						if(!oldConcept.contains(x, y) && newConcept.contains(x, y)) {
							posNotCovIndVector.add(new IndividualPoint("-",(int)x,(int)y,ind.toString()));
							j++;
							flag = false;
							x = x + gap;
							break;
						} else {
							x = x + gap;
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
			if(k<maxNumberOfIndividualPoints) {
				while(flag) {
					if(x >= oldConcept.getMaxX()) {
						x = (int) oldConcept.getMinX();
						y = y + gap;
					}
				
					if(y >= oldConcept.getMaxY()) {
						y = (int) oldConcept.getMinY();
					}
				
					while(x < oldConcept.getMaxX()) {
					
						if(oldConcept.contains(x, y) && !newConcept.contains(x, y)) {
							posNotCovIndVector.add(new IndividualPoint("-",(int)x,(int)y,ind.toString()));
							k++;
							flag = false;
							x = x + gap;
							break;
						} else {
							x = x + gap;
						}
					}
				}
			}
		}
		points.addAll(posCovIndVector);
		points.addAll(posNotCovIndVector);
	}
	
	public Vector<IndividualPoint> getIndividualVector() {
		return points;
	}
	
	public GraphicalCoveragePanel getGraphicalCoveragePanel() {
		return this;
	}
	
	public MoreDetailForSuggestedConceptsPanel getMoreDetailForSuggestedConceptsPanel() {
		return panel;
	}
	
	
	
}
