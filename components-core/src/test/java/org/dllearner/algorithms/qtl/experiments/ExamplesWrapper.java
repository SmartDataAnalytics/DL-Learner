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
package org.dllearner.algorithms.qtl.experiments;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.List;
import java.util.SortedMap;

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