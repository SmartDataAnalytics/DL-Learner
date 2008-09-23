/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.kb.extraction;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLObjectProperty;



/**
 * Property node, has connection to a and b part
 * 
 * @author Sebastian Hellmann
 * 
 */

public class ObjectPropertyNode extends PropertyNode {

	
	// specialtypes like owl:symmetricproperty
	private SortedSet<String> specialTypes = new TreeSet<String>();
	@SuppressWarnings("unused")
	private SortedSet<RDFNodeTuple> propertyInformation = new TreeSet<RDFNodeTuple>();

	public ObjectPropertyNode(String propertyURI, Node a, Node b) {
		super(propertyURI, a, b);		
	}

	// Property Nodes are normally not expanded,
	// this function is never called
	@Override
	public List<Node> expand(TupleAquisitor tupelAquisitor, Manipulator manipulator) {
		return null;
	}

	// gets the types for properties recursively
	@Override
	public void expandProperties(TupleAquisitor tupelAquisitor, Manipulator manipulator) {
		b.expandProperties(tupelAquisitor, manipulator);
		SortedSet<RDFNodeTuple> newTypes = tupelAquisitor.getTupelForResource(uri);
		for (RDFNodeTuple tuple : newTypes) {
			try {
				if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)) {
					specialTypes.add(tuple.b.toString());
				}else if(tuple.b.isAnon()){
					logger.warn("blanknodes currently not implemented in this tuple aquisitor");
				}else{
					propertyInformation.add(tuple);
					
				}
			} catch (Exception e) {
				logger.warn("resource "+uri+" with "+ tuple);
				e.printStackTrace();
			}
		}

	}
	
	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> s = new TreeSet<String>();
		s.add(getNTripleForm()+"<" + OWLVocabulary.RDF_TYPE + "><"
				+ OWLVocabulary.OWL_OBJECTPROPERTY + ">.");
		for (String one : specialTypes) {
			s.add(getNTripleForm()+"<" + OWLVocabulary.RDF_TYPE + "><"
					+ one + ">.");
		}
		
		for (RDFNodeTuple one : propertyInformation) {
			s.add(one.getNTriple(uri));
		}

		return s;
	}
	
	@Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		//FIXME Property information

		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
		OWLObjectProperty me =factory.getOWLObjectProperty(getURI());
	
		
		for (String one : specialTypes) {
			if(one.equals(OWLVocabulary.OWL_FunctionalProperty)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLFunctionalObjectPropertyAxiom(me));
			}else if(one.equals(OWLVocabulary.OWL_InverseFunctionalProperty)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLInverseFunctionalObjectPropertyAxiom(me));
			}else if(one.equals(OWLVocabulary.OWL_TransitiveProperty)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLTransitiveObjectPropertyAxiom(me));
			}else if(one.equals(OWLVocabulary.OWL_SymmetricProperty)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLSymmetricObjectPropertyAxiom(me));
			}
		}
	}
	

	
}
