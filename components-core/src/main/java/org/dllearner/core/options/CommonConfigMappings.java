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

package org.dllearner.core.options;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.options.fuzzydll.FuzzyExample;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * @author Jens Lehmann
 *
 */
public class CommonConfigMappings {
	
	private static final OWLDataFactory df = new OWLDataFactoryImpl();

	
	public static SortedSet<OWLIndividual> getIndividualSet(Set<String> individuals) {
		SortedSet<OWLIndividual> set = new TreeSet<OWLIndividual>();
		for(String individual : individuals){
			set.add(df.getOWLNamedIndividual(IRI.create(individual)));
		}
		return set;
	}
	
	public static SortedSet<OWLClass> getAtomicConceptSet(Set<String> atomicConcepts) {
		SortedSet<OWLClass> set = new TreeSet<OWLClass>();
		for(String atomicConcept : atomicConcepts) {
			set.add(df.getOWLClass(IRI.create(atomicConcept)));
		}
		return set;
	}	
	
	public static SortedSet<OWLObjectProperty> getAtomicRoleSet(Set<String> atomicRoles) {
		SortedSet<OWLObjectProperty> set = new TreeSet<OWLObjectProperty>();
		for(String atomicRole : atomicRoles){
			set.add(df.getOWLObjectProperty(IRI.create(atomicRole)));
		}
		return set;
	}
	
	// added by Josue
	public static SortedSet<FuzzyIndividual> getFuzzyIndividualSet(Set<FuzzyExample> examples) {
		SortedSet<FuzzyIndividual> set = new TreeSet<FuzzyIndividual>();
		for(FuzzyExample example : examples){
			set.add(new FuzzyIndividual(example.getExampleName(), example.getFuzzyDegree()));
		}
		return set;
	}
}
