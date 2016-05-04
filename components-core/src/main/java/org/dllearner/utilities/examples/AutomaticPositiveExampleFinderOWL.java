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
package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

public class AutomaticPositiveExampleFinderOWL {
	
	// LOGGER: ComponentManager
	private static Logger logger = Logger
		.getLogger(AutomaticPositiveExampleFinderOWL.class);

	
	private AbstractReasonerComponent reasoningService;
	
	private SortedSet<OWLIndividual> posExamples;
	
	public AutomaticPositiveExampleFinderOWL(AbstractReasonerComponent reasoningService) {
	
		this.posExamples = new TreeSet<>();
		this.reasoningService = reasoningService;
	}
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromConcept(OWLClassExpression concept){
		logger.debug("making Positive Examples from Concept: "+concept);
		this.posExamples.clear();
		this.posExamples.addAll(reasoningService.getIndividuals(concept));
		//this.posExamples = sparqltasks.retrieveInstancesForClassDescription(conceptKBSyntax, 0);
		logger.debug("pos Example size: "+posExamples.size());
	}
	
	
	public SortedSet<OWLIndividual> getPosExamples() {
		return posExamples;
	}




	
}
