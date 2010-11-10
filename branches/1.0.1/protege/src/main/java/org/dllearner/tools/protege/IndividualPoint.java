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

import java.awt.geom.Ellipse2D;

import org.dllearner.core.owl.Individual;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * This class is a datastructure for one individual shown in
 * the GraphicalCoveragePanel.
 * @author Christian Koetteritzsch
 *
 */
public class IndividualPoint {
	
	private String point;
	private int xAxis;
	private int yAxis;
	private final String individual;
	private final Ellipse2D circlePoint;
	private Individual individualDLLearner;
	private String baseUri;
	private OWLIndividual individualOWL; 
	
	/**
	 * Constructor of the class.
	 * @param p display String 
	 * @param x coordinate on the x axis
	 * @param y coordinate on the y axis
	 * @param ind Name of the Individual
	 */
	@Deprecated
	public IndividualPoint(String p, int x, int y, String ind) {
		this.point = p;
		this.xAxis = x;
		this.yAxis = y;
		this.circlePoint = new Ellipse2D.Double(x - 1, y - 1, 4, 4);
		this.individual = ind;
	}
	
	/**
	 * This is the second Constructor of the class. This should be used if more 
	 * details for the shown Individuals should be displayed. 
	 * @param p display String
	 * @param x coordinate on the x axis
	 * @param y coordinate on the y axis
	 * @param ind Name of the Individual
	 * @param indi DLLearner Indivudal
	 * @param base base uri of the individual.
	 */
	public IndividualPoint(String p, int x, int y, String ind, Individual indi, String base) {
		this.point = p;
		this.xAxis = x;
		this.yAxis = y;
		this.circlePoint = new Ellipse2D.Double(x - 1, y - 1, 4, 4);
		this.individual = ind;
		this.individualDLLearner = indi;
		this.baseUri = base;
	}
	
	/**
	 * 
	 * @param p
	 * @param x
	 * @param y
	 * @param ind
	 * @param indi
	 * @param base
	 */
	public IndividualPoint(String p, int x, int y, String ind, OWLIndividual indi, Individual indiDLLearner, String base) {
		this.point = p;
		this.xAxis = x;
		this.yAxis = y;
		this.circlePoint = new Ellipse2D.Double(x-1, y-1, 4, 4);
		this.individual = ind;
		this.individualOWL = indi;
		this.individualDLLearner = indiDLLearner;
		this.baseUri = base;
		
	}

	/**
	 * This method returns the display string of the individual.
	 * @return the point
	 */
	public String getPoint() {
		return point;
	}

	/**
	 * This method returns the x axis coordinate.
	 * @return the xAxis
	 */
	public int getXAxis() {
		return xAxis;
	}

	/**
	 * This method returns the y axis coordinate.
	 * @return the yAxis
	 */
	public int getYAxis() {
		return yAxis;
	}
	
	/**
	 * This method returns the name of the Individual.
	 * @return name of the Individual
	 */
	public String getIndividualName() {
		return individual;
	}
	
	/**
	 * This method returns an ellipse of the individual for the GraphicalCoveragePanel.
	 * @return individual point of the individual
	 */
	public Ellipse2D getIndividualPoint() {
		return circlePoint;
	}
	
	/**
	 * This method returns the DLLearner Individual.
	 * @return DLLearner Individual
	 */
	public Individual getDLLearnerIndividual() {
		return individualDLLearner;
	}
	
	/**
	 * This method returns the base uri of the Individual.
	 * @return base uri of the individual
	 */
	public String getBaseUri() {
		return baseUri;
	}

	/**
	 * 
	 * @return
	 */
	public OWLIndividual getIndividualOWL() {
		return individualOWL;
	}
}
