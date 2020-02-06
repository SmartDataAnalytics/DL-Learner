package org.dllearner.cli.parcel;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.semanticweb.owlapi.model.OWLIndividual;


/**
 * 
 * @author An C. Tran
 *
 */
public class ParCELExtraTestingNode {
	
	protected ParCELExtraNode extraNode;
	
	protected Set<OWLIndividual> coveredPositiveExamplesTestSet = new HashSet<>();
	protected Set<OWLIndividual> coveredNegativeExamplestestSet = new HashSet<>();
	

	public ParCELExtraTestingNode(ParCELExtraNode node) {
		extraNode = node;
	}
	
	
	public ParCELExtraTestingNode(ParCELExtraNode node, Set<OWLIndividual> coveredPositiveExamplesTestSet,
			Set<OWLIndividual> coveredNegativeExamplesTestSet) {
		this.extraNode = node;
		this.coveredPositiveExamplesTestSet.addAll(coveredPositiveExamplesTestSet);
		this.coveredNegativeExamplestestSet.addAll(coveredNegativeExamplesTestSet);
	}


	public ParCELExtraNode getExtraNode() {
		return extraNode;
	}


	public void setExtraNode(ParCELExtraNode extraNode) {
		this.extraNode = extraNode;
	}


	public Set<OWLIndividual> getCoveredPositiveExamplesTestSet() {
		return coveredPositiveExamplesTestSet;
	}


	public void setCoveredPositiveExamplesTestSet(Set<OWLIndividual> coveredPositiveExamplesTestSet) {
		this.coveredPositiveExamplesTestSet = coveredPositiveExamplesTestSet;
	}


	public Set<OWLIndividual> getCoveredNegativeExamplestestSet() {
		return coveredNegativeExamplestestSet;
	}


	public void setCoveredNegativeExamplestestSet(Set<OWLIndividual> coveredNegativeExamplestestSet) {
		this.coveredNegativeExamplestestSet = coveredNegativeExamplestestSet;
	}
	
	
	
}
