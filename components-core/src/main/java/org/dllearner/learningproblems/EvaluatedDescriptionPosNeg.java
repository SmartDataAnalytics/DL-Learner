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

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * This represents a class description, which has been
 * evaluated by the learning algorithm, i.e. it has been checked
 * which examples it covers. It can be used as return value for
 * learning algorithms to make it easier for applications to
 * assess how good an offered class description is and how it
 * classifies particular examples.
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionPosNeg extends EvaluatedDescription<ScorePosNeg> {
	
	private static final long serialVersionUID = -6962185910615506968L;
	private ScorePosNeg score2;
	
	/**
	 * Constructs an evaluated description using its score.
	 * @param description The description, which was evaluated.
	 * @param score The score of the description.
	 */
	public EvaluatedDescriptionPosNeg(OWLClassExpression description, ScorePosNeg score) {
		super(description, score);
		score2 = score;
	}
	
	
	/**
	 * @see org.dllearner.learningproblems.ScorePosNeg#getAccuracy()
	 * @return Accuracy of the description.
	 */
	@Override
	public double getAccuracy() {
		return score2.getAccuracy();
	}
	
	/**
	 * Gets the score of this description. This can be used to get
	 * further statistical values.
	 * @see org.dllearner.learningproblems.ScorePosNeg
	 * @return The score object associated with this evaluated description.
	 */
	@Override
	public ScorePosNeg getScore() {
		return score2;
	}

	/**
	 * @see org.dllearner.learningproblems.ScorePosNeg#getCoveredNegatives()
	 * @return Negative examples covered by the description.
	 */
	public Set<OWLIndividual> getCoveredNegatives() {
		return score2.getCoveredNegatives();
	}

	/**
	 * @see org.dllearner.learningproblems.ScorePosNeg#getCoveredPositives()
	 * @return Positive examples covered by the description.
	 */
	public Set<OWLIndividual> getCoveredPositives() {
		return score2.getCoveredPositives();
	}

	/**
	 * @see org.dllearner.learningproblems.ScorePosNeg#getNotCoveredNegatives()
	 * @return Negative examples not covered by the description.
	 */
	public Set<OWLIndividual> getNotCoveredNegatives() {
		return score2.getNotCoveredNegatives();
	}

	/**
	 * @see org.dllearner.learningproblems.ScorePosNeg#getNotCoveredPositives()
	 * @return Positive examples not covered by the description.
	 */
	public Set<OWLIndividual> getNotCoveredPositives() {
		return score2.getNotCoveredPositives();
	}
	
	/**
	 * This convenience method can be used to store and exchange evaluated
	 * descriptions by transforming them to a JSON string.
	 * @return A JSON representation of an evaluated description.
	 */
	@Override
	public String asJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put("descriptionManchesterSyntax", OWLAPIRenderers.toManchesterOWLSyntax(hypothesis));
			object.put("descriptionOWLXML", OWLAPIRenderers.toOWLXMLSyntax(hypothesis));
			object.put("accuracy", score2.getAccuracy());
			object.put("coveredPositives", getJSONArray(score2.getCoveredPositives()));
			object.put("coveredNegatives", getJSONArray(score2.getCoveredNegatives()));
			object.put("notCoveredPositives", getJSONArray(score2.getNotCoveredPositives()));
			object.put("notCoveredNegatives", getJSONArray(score2.getNotCoveredNegatives()));
			return object.toString(3);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String toString() {
		return hypothesis.toString() + "(accuracy: " + getAccuracy() + ")";
	}
	
	// we need to use this method instead of the standard JSON array constructor,
	// otherwise we'll get unexpected results (JSONArray does not take Individuals
	// as arguments and does not use toString)
	public static JSONArray getJSONArray(Set<OWLIndividual> individuals) {
		JSONArray j = new JSONArray();
		for(OWLIndividual i : individuals) {
			j.put(i.toStringID());
		}
		return j;
	}

}
