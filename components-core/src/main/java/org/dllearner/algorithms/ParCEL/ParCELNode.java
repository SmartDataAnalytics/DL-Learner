package org.dllearner.algorithms.ParCEL;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a node in the search tree used in the ParCEL<br>
 * A node includes description and its corresponding properties such as: correctness, accuracy,
 * distance between the description the leaning problem, parent node of the description. It also
 * contains a flag which indicates the node is processed or not.
 * 
 * @author An C. Tran
 * 
 */
public class ParCELNode extends OENode {

	private double correctness = -1.0;
	private double completeness = -1.0;

	protected Set<OWLIndividual> coveredPositiveExamples = new HashSet<>();
	protected Set<OWLIndividual> coveredNegativeExamples = new HashSet<>();

	private DecimalFormat dfPercent = new DecimalFormat("0.00%");

	/*
	public PADCELNode(OENode parentNode, Description description, double accuracy) {
		super(parentNode, description, accuracy);
	}

	public PADCELNode(OENode parentNode, Description description, double accuracy, double correctness) {
		super(parentNode, description, accuracy);
		this.correctness = correctness;
	}
	*/

	public ParCELNode(OENode parentNode, OWLClassExpression description, double accuracy,
					  double correctness, double completeness) {
		super(description, accuracy);
		this.correctness = correctness;
		this.completeness = completeness;
		setParent(parentNode);
	}

	/*
	public PADCELNode(OENode parentNode, Description description) {
		super(parentNode, description, 0);
	}
	*/

	public ParCELNode(OENode parentNode, OWLClassExpression description,
			Set<OWLIndividual> coveredPositiveExamples, Set<OWLIndividual> coveredNegativeExamples) {
		super(description, 0);
		this.coveredPositiveExamples.addAll(coveredPositiveExamples);
		this.coveredNegativeExamples.addAll(coveredNegativeExamples);
		setParent(parentNode);
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

	public Set<OWLIndividual> getCoveredPositiveExamples() {
		return this.coveredPositiveExamples;
	}

	public Set<OWLIndividual> getCoveredNegativeExamples() {
		return this.coveredNegativeExamples;
	}

	public void setCoveredPositiveExamples(Set<OWLIndividual> coveredPositiveExamples) {
		if (coveredPositiveExamples != null)
			this.coveredPositiveExamples.addAll(coveredPositiveExamples);
		else
			this.coveredPositiveExamples.clear();
	}

	public void setCoveredNegativeExamples(Set<OWLIndividual> coveredNegativeExamples) {
		if (coveredNegativeExamples != null)
			this.coveredNegativeExamples.addAll(coveredNegativeExamples);
		else
			this.coveredNegativeExamples.clear();
	}

	@Override
	public String toString() {
		String ret = OWLAPIRenderers.toDLSyntax(description);
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
