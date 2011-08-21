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

package org.dllearner.utilities.examples;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentManager;
import org.dllearner.kb.sparql.SPARQLTasks;

public class AutomaticPositiveExampleFinderSPARQL {
	
	// LOGGER: ComponentManager
	private static Logger logger = Logger
	.getLogger(ComponentManager.class);

	
	private SPARQLTasks sparqltasks;
	
	private SortedSet<String> posExamples;
	
	public AutomaticPositiveExampleFinderSPARQL(SPARQLTasks st) {
		super();
		
		this.posExamples = new TreeSet<String>();
		this.sparqltasks = st;
	}
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromConcept(String conceptKBSyntax){
		logger.debug("making Positive Examples from Concept: "+conceptKBSyntax);	
		this.posExamples = sparqltasks.retrieveInstancesForClassDescription(conceptKBSyntax, 0);
		logger.debug("   pos Example size: "+posExamples.size());
	}
	
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromRoleAndObject(String role, String object){
		logger.debug("making Positive Examples from role: "+role+" and object: "+object);	
		this.posExamples = sparqltasks.retrieveDISTINCTSubjectsForRoleAndObject(role, object, 0);
		logger.debug("   pos Example size: "+posExamples.size());
	}
	
	//QUALITY resultsize is not accounted for
	public void makePositiveExamplesFromSKOSConcept(String SKOSConcept){
		logger.debug("making Positive Examples from SKOSConcept: "+SKOSConcept);	
		this.posExamples = sparqltasks.retrieveInstancesForSKOSConcept(SKOSConcept, 0);
		logger.debug("pos Example size: "+posExamples.size());
	}
	
	public void makePositiveExamplesFromDomain(String role, int resultLimit){
		logger.debug("making Positive Examples from Domain of : "+role);
		this.posExamples.addAll(sparqltasks.getDomainInstances(role, resultLimit));
		logger.debug("pos Example size: "+posExamples.size());
	}
	
	public void makePositiveExamplesFromRange(String role, int resultLimit){
		logger.debug("making Positive Examples from Range of : "+role);
		this.posExamples.addAll(sparqltasks.getRangeInstances(role, resultLimit));
		logger.debug("pos Example size: "+posExamples.size());
	}
	
	
	public SortedSet<String> getPosExamples() {
		return posExamples;
	}




	
}
