/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.core;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.google.common.collect.ComparisonChain;

/**
 * An evaluated description is a description and its score (with some
 * convenience method and serialisation formats).
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescription implements Serializable, Comparable<EvaluatedDescription>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1106431570510815033L;
	protected OWLClassExpression description;
	protected Score score;
	
	protected static DecimalFormat dfPercent = new DecimalFormat("0.00%");
	
	/**
	 * Constructs an evaluated description using its score.
	 * @param description The description, which was evaluated.
	 * @param score The score of the description.
	 */
	public EvaluatedDescription(OWLClassExpression description, Score score) {
		this.description = description;
		this.score = score;
	}
	
	/**
	 * Gets the description, which was evaluated.
	 * @return The underlying description.
	 */
	public OWLClassExpression getDescription() {
		return description;
	}
	
	/**
	 * @return the score
	 */
	public Score getScore() {
		return score;
	}
	
	/**
	 * Used for rewriting (simplification, beautification) of 
	 * evaluated descriptions returned by the learning algorithm.
	 * @param description The description to set.
	 */
	public void setDescription(OWLClassExpression description) {
		this.description = description;
	}	
	
	/**
	 * @see org.dllearner.core.owl.Description#getLength()
	 * @return Length of the description.
	 */		
	public int getDescriptionLength() {
		return OWLClassExpressionUtils.getLength(description);
	}
	
	/**
	 * @see org.dllearner.core.owl.Description#getDepth()
	 * @return Depth of the description.
	 */	
	public int getDescriptionDepth() {
		return OWLClassExpressionUtils.getDepth(description);
	}
	
	/**
	 * @see org.dllearner.core.Score#getScoreValue()
	 * @return Value in this score system.
	 */
	public double getAccuracy() {
		return score.getAccuracy();
	}
	
	/**
	 * This convenience method can be used to store and exchange evaluated
	 * descriptions by transforming them to a JSON string.
	 * @return A JSON representation of an evaluated description.
	 */
	public String asJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put("descriptionManchesterSyntax", OWLAPIRenderers.toManchesterOWLSyntax(description));
			object.put("descriptionOWLXML", OWLAPIRenderers.toOWLXMLSyntax(description));
			object.put("scoreValue", score.getAccuracy());		
			return object.toString(3);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}	
	
	@Override
	public String toString() {
		return description.toString() + " " + dfPercent.format(getAccuracy());
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EvaluatedDescription o) {
		return ComparisonChain.start()
				.compare(score.getAccuracy(), o.score.getAccuracy())
				.compare(description, o.getDescription())
				.result();
	}

}
