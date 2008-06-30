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
package org.dllearner.core;

import java.util.Set;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.learningproblems.ScoreTwoValued;
import org.dllearner.reasoning.OWLAPIDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owl.model.OWLDescription;

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
public class EvaluatedDescription {

	private Description description;
	private Score score;
	
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
	 * Constructs an evaluated description using example coverage.
	 * @param description The description, which was evaluated.
	 * @param posAsPos Positive examples classified as positive by (i.e. instance of) the description.
	 * @param posAsNeg Positive examples classified as negative by (i.e. not instance of) the description.
	 * @param negAsPos Negative examples classified as positive by (i.e. instance of) the description.
	 * @param negAsNeg Negative examples classified as negative by (i.e. not instance of) the description.
	 */
	public EvaluatedDescription(Description description, Set<Individual> posAsPos, Set<Individual> posAsNeg, Set<Individual> negAsPos, Set<Individual> negAsNeg) {
		this.description = description;
		// usually core methods should not depend on methods outside of the core package (except utilities)
		// in this case, this is just a convenience constructor
		score = new ScoreTwoValued(posAsPos, posAsNeg, negAsPos, negAsNeg);
	}

	/**
	 * Gets the description, which was evaluated.
	 * @return The underlying description.
	 */
	public Description getDescription() {
		return description;
	}
	
	/**
	 * @see org.dllearner.core.owl.Description#getLength()
	 */		
	public int getDescriptionLength() {
		return description.getLength();
	}
	
	/**
	 * @see org.dllearner.core.owl.Description#getDepth()
	 */	
	public int getDescriptionDepth() {
		return description.getDepth();
	}
	
	/**
	 * @see org.dllearner.core.Score#getAccuracy()
	 */
	public double getAccuracy() {
		return score.getAccuracy();
	}

	/**
	 * @see org.dllearner.core.Score#getCoveredNegatives()
	 */
	public Set<Individual> getCoveredNegatives() {
		return score.getCoveredNegatives();
	}

	/**
	 * @see org.dllearner.core.Score#getCoveredPositives()
	 */
	public Set<Individual> getCoveredPositives() {
		return score.getCoveredPositives();
	}

	/**
	 * @see org.dllearner.core.Score#getNotCoveredNegatives()
	 */
	public Set<Individual> getNotCoveredNegatives() {
		return score.getNotCoveredNegatives();
	}

	/**
	 * @see org.dllearner.core.Score#getNotCoveredPositives()
	 */
	public Set<Individual> getNotCoveredPositives() {
		return score.getNotCoveredPositives();
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
		return SparqlQueryDescriptionConvertVisitor.getSparqlQuery(description, limit);
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
			OWLDescription d = OWLAPIDescriptionConvertVisitor.getOWLDescription(description);
			object.put("descriptionOWLXML", OWLAPIRenderers.toOWLXMLSyntax(d));
			object.put("accuracy", score.getAccuracy());
			object.put("coveredPositives", getJSONArray(score.getCoveredPositives()));
			object.put("coveredNegatives", getJSONArray(score.getCoveredNegatives()));
			object.put("notCoveredPositives", getJSONArray(score.getNotCoveredPositives()));
			object.put("notCoveredNegatives", getJSONArray(score.getNotCoveredNegatives()));			
			return object.toString(3);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// we need to use this method instead of the standard JSON array constructor,
	// otherwise we'll get unexpected results (JSONArray does not take Individuals
	// as arguments and does not use toString)
	private static JSONArray getJSONArray(Set<Individual> individuals) {
		JSONArray j = new JSONArray();
		for(Individual i : individuals)
			j.put(i.getName());
		return j;
	}
}
