/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.gui;

import javax.swing.JTree;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.algorithms.ocel.ExampleBasedNode;

/**
 * Own JTree implemenation with improved formatting.
 * 
 * @author Jens Lehmann
 *
 */
public class SearchTree extends JTree {

	private static final long serialVersionUID = 4509903171856747400L;

	private int nrOfNegativeExamples;
	private int nrOfPositiveExamples;
	private String baseURI;
	
	// CELOE constructor
	public SearchTree(EBNodeTreeModel model, String baseURI) {
		super(model);
		this.baseURI = baseURI;
	}
	
	// OCEL constructor
	public SearchTree(EBNodeTreeModel model, int nrOfPositiveExamples, int nrOfNegativeExamples, String baseURI) {
		super(model);
		this.nrOfPositiveExamples = nrOfPositiveExamples;
		this.nrOfNegativeExamples = nrOfNegativeExamples;
		this.baseURI = baseURI;
//		setRowHeight(0);
	}
	
	@Override
	public String convertValueToText(Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
		if(value instanceof OENode) {
			return ((OENode)value).getShortDescription(baseURI);
		} else {
			ExampleBasedNode node = (ExampleBasedNode) value;
			return node.getShortDescriptionHTML(nrOfPositiveExamples, nrOfNegativeExamples, baseURI);
		}
	}
}
