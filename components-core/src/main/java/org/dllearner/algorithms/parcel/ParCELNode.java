package org.dllearner.algorithms.parcel;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Represents a node in the search tree used in the ParCEL<br>
 * A node includes description and its corresponding properties such as: correctness, accuracy,
 * distance between the description the leaning problem, its parent node, etc. 
 * 
 * @author An C. Tran
 * 
 */
public class ParCELNode extends OENode {

	private double correctness = -1.0;
	private double completeness = -1.0;

	private final DecimalFormat dfPercent = new DecimalFormat("0.00%");


	public ParCELNode(OENode parentNode, OWLClassExpression description, double accuracy,
			double correctness, double completeness) {
		super(description, accuracy);
		setParent(parentNode);
		this.correctness = correctness;
		this.completeness = completeness;
	}


	public ParCELNode(OENode parentNode, OWLClassExpression description,
					  Set<OWLIndividual> coveredPositiveExamples, Set<OWLIndividual> coveredNegativeExamples) {
		super(description, 0);
		setParent(parentNode);
		setCoveredPositiveExamples(coveredPositiveExamples);
		setCoveredNegativeExamples(coveredNegativeExamples);
	}

	public void setCorrectness(double cor) {
		this.correctness = cor;
	}

	public double getCorrectness() {
		return this.correctness;
	}

	public void setCompleteness(double comp) {
		this.completeness = comp;
	}

	public double getCompleteness() {
		return this.completeness;
	}

	public void setAccuracy(double acc) {
		this.accuracy = acc;
	}

	@Override
	public String toString() {
		String ret = OWLAPIRenderers.toManchesterOWLSyntax(this.getDescription());
		ret += " [acc:" + dfPercent.format(this.getAccuracy());
		ret += ", cor:" + dfPercent.format(this.getCorrectness());
		ret += ", comp:" + dfPercent.format(this.completeness);
		ret += ", horz:" + this.horizontalExpansion + "]";
		return ret;

	}

	public void setDescription(OWLClassExpression newDescription) {
		this.description = newDescription;
	}

}
