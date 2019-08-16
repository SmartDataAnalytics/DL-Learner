/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
 */
package org.dllearner.learningproblems;

import java.util.Set;

import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * The score class is used to store how well a class description did
 * on a learning problem.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class ScorePosNeg<T extends OWLEntity> extends Score {
	
	private static final long serialVersionUID = -4646131678864109469L;

	public abstract double getScoreValue();
	
	// example coverage
	public abstract Set<T> getCoveredPositives();
	public abstract Set<T> getCoveredNegatives();
	public abstract Set<T> getNotCoveredPositives();
	public abstract Set<T> getNotCoveredNegatives();	
	
	/**
	 * The score of a concept depends on how good it classifies the
	 * examples of a learning problem and the length of the concept
	 * itself. If a given concept is known to have equal classification
	 * properties than the concept this score object is based on, then
	 * this method can be used to calculate its score value by using the
	 * length of this concept as parameter.
	 * 
	 * @param newLength Length of the concept.
	 * @return Score.
	 */
	public abstract ScorePosNeg<T> getModifiedLengthScore(int newLength);

	public String printConfusionMatrix() {

		int[][] matrix = new int[2][2];
		matrix[0][0] = getCoveredPositives().size();
		matrix[0][1] = getCoveredNegatives().size();
		matrix[1][0] = getNotCoveredPositives().size();
		matrix[1][1] = getNotCoveredNegatives().size();



		StringBuilder sb = new StringBuilder();

		sb.append("          pos        neg     \n");

		for(int i = 0; i < matrix.length; i++){
			String cls = i==0 ? "pos" : "neg";
			sb.append(cls + "  |");
			for(int j = 0; j < matrix.length; j++){
				sb.append(String.format("%8d |", matrix[i][j]));
			}
			sb.append("\n");
		}

		return sb.toString();//.trim();
	}
	
}