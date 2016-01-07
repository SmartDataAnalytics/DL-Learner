package org.dllearner.algorithms.qtl.experiments;

import java.util.List;
import java.util.SortedMap;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.semanticweb.owlapi.model.OWLIndividual;

public class ExamplesWrapper {
	List<String> correctPosExamples;
	List<String> falsePosExamples;
	List<String> correctNegExamples;
	SortedMap<OWLIndividual, RDFResourceTree> posExamplesMapping;
	SortedMap<OWLIndividual, RDFResourceTree> negExamplesMapping;

	public ExamplesWrapper(List<String> correctPosExamples,
			List<String> falsePosExamples, List<String> correctNegExamples,
			SortedMap<OWLIndividual, RDFResourceTree> posExamplesMapping,
			SortedMap<OWLIndividual, RDFResourceTree> negExamplesMapping) {
		this.correctPosExamples = correctPosExamples;
		this.falsePosExamples = falsePosExamples;
		this.correctNegExamples = correctNegExamples;
		this.posExamplesMapping = posExamplesMapping;
		this.negExamplesMapping = negExamplesMapping;
	}
}