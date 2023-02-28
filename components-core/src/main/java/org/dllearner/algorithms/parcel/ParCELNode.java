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

	protected Set<OWLIndividual> coveredPositiveExamples = new HashSet<>();
	protected final Set<OWLIndividual> coveredNegativeExamples = new HashSet<>();

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
		this.coveredPositiveExamples.addAll(coveredPositiveExamples);
		this.coveredNegativeExamples.addAll(coveredNegativeExamples);
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
