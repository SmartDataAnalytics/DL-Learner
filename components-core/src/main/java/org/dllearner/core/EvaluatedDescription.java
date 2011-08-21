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

import org.dllearner.core.owl.Description;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * An evaluated description is a description and its score (with some
 * convenience method and serialisation formats).
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescription implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1106431570510815033L;
	protected Description description;
	protected Score score;
	
	protected static DecimalFormat dfPercent = new DecimalFormat("0.00%");
	
	/**
	 * Constructs an evaluated description using its score.
	 * @param description The description, which was evaluated.
	 * @param score The score of the description.
	 */
	public EvaluatedDescription(Description description, Score score) {
		this.description = description;
		this.score = score;
	}
	
	/**
	 * Gets the description, which was evaluated.
	 * @return The underlying description.
	 */
	public Description getDescription() {
		return description;
	}
	
	/**
	 * Used for rewriting (simplification, beautification) of 
	 * evaluated descriptions returned by the learning algorithm.
	 * @param description The description to set.
	 */
	public void setDescription(Description description) {
		this.description = description;
	}	
	
	/**
	 * @see org.dllearner.core.owl.Description#getLength()
	 * @return Length of the description.
	 */		
	public int getDescriptionLength() {
		return description.getLength();
	}
	
	/**
	 * @see org.dllearner.core.owl.Description#getDepth()
	 * @return Depth of the description.
	 */	
	public int getDescriptionDepth() {
		return description.getDepth();
	}
	
	/**
	 * @see org.dllearner.core.Score#getScoreValue()
	 * @return Value in this score system.
	 */
	public double getAccuracy() {
		return score.getAccuracy();
	}
	
	/**
	 * Returns a SPARQL query to get instances of this description
	 * from a SPARQL endpoint. Of course, results may be incomplete,
	 * because no inference is done. The SPARQL query is a straightforward
	 * translation without any attempts to perform e.g. subclass 
	 * inferencing.
	 * 
	 * @param limit The maximum number of results. Corresponds to LIMIT
	 * in SPARQL.
	 * @return A SPARQL query of the underlying description.
	 */
	public String getSparqlQuery(int limit) {
		return SparqlQueryDescriptionConvertVisitor.getSparqlQuery(description, limit, false, false);
	}	
	
	/**
	 * This convenience method can be used to store and exchange evaluated
	 * descriptions by transforming them to a JSON string.
	 * @return A JSON representation of an evaluated description.
	 */
	public String asJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put("descriptionManchesterSyntax", description.toManchesterSyntaxString(null, null));
			OWLClassExpression c = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(description);
			object.put("descriptionOWLXML", OWLAPIRenderers.toOWLXMLSyntax(c));
			object.put("descriptionKBSyntax", description.toKBSyntaxString());
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

}
