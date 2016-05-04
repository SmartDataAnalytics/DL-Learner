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
package org.dllearner.algorithms.ocel;

import java.util.Comparator;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class SubsumptionComparator implements Comparator<OWLClassExpression> {

	private AbstractReasonerComponent rs;
	
	public SubsumptionComparator(AbstractReasonerComponent rs) {
		this.rs = rs;
	}
	
	public int compare(ExampleBasedNode arg0, ExampleBasedNode arg1) {
		OWLClassExpression concept1 = arg0.getConcept();
		OWLClassExpression concept2 = arg1.getConcept();
		return compare(concept1, concept2);
	}

	@Override
	public int compare(OWLClassExpression concept1, OWLClassExpression concept2) {
		// return true if concept1 is a super concept of concept2
		boolean value1 = rs.isSuperClassOf(concept1, concept2);
		if(value1)
			return 1;
		
		boolean value2 = rs.isSuperClassOf(concept2, concept1);
		if(value2)
			return -1;
		
//		System.out.println("Incomparable: " + concept1 + " " + concept2);
		
		// both concepts are incomparable => order them syntactically
		return concept1.compareTo(concept2);
	}

}
