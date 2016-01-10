package org.dllearner.core;

import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * An evaluated class expression is a class expression and its score (with some
 * convenience method and serialisation formats).
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescription<S extends Score> extends EvaluatedHypothesis<OWLClassExpression, S>{

	/**
	 * Constructs an evaluated class expression using its score.
	 * @param description The class expression, which was evaluated.
	 * @param score The score of the class expression.
	 */
	public EvaluatedDescription(OWLClassExpression description, S score) {
		super(description, score);
	}
	
	/**
	 * @see OWLClassExpressionUtils#getLength(OWLClassExpression)
	 * @return Length of the description.
	 */		
	public int getDescriptionLength() {
		return OWLClassExpressionUtils.getLength(hypothesis);
	}
	
	/**
	 * @see OWLClassExpressionUtils#getDepth(OWLClassExpression)
	 * @return Depth of the description.
	 */	
	public int getDescriptionDepth() {
		return OWLClassExpressionUtils.getDepth(hypothesis);
	}
	
	/**
	 * This convenience method can be used to store and exchange evaluated
	 * descriptions by transforming them to a JSON string.
	 * @return A JSON representation of an evaluated description.
	 */
	public String asJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put("descriptionManchesterSyntax", OWLAPIRenderers.toManchesterOWLSyntax(hypothesis));
			object.put("descriptionOWLXML", OWLAPIRenderers.toOWLXMLSyntax(hypothesis));
			object.put("scoreValue", score.getAccuracy());	
			return object.toString(3);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}	
}
