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

package org.dllearner.kb.extraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.aquisitors.RDFBlankNode;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;



/**
 * Property node, has connection to a and b part
 * 
 * @author Sebastian Hellmann
 * 
 */

public class ObjectPropertyNode extends PropertyNode {

	
	// specialtypes like owl:symmetricproperty
	private SortedSet<String> specialTypes = new TreeSet<String>();
	private SortedSet<RDFNodeTuple> propertyInformation = new TreeSet<RDFNodeTuple>();
	private List<BlankNode> blankNodes = new ArrayList<BlankNode>();

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
	public List<BlankNode> expandProperties(TupleAquisitor tupelAquisitor, Manipulator manipulator, boolean dissolveBlankNodes) {
		List<BlankNode> ret =  new ArrayList<BlankNode>();
		ret.addAll(b.expandProperties(tupelAquisitor, manipulator, dissolveBlankNodes));
		SortedSet<RDFNodeTuple> newTypes = tupelAquisitor.getTupelForResource(uri);
		for (RDFNodeTuple tuple : newTypes) {
			try {
				
				if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)) {
					if(!tuple.b.toString().equals(OWLVocabulary.OWL_OBJECTPROPERTY)){
						specialTypes.add(tuple.b.toString());
					}
				}else if(tuple.b.isAnon()){
									
					if(dissolveBlankNodes){
						RDFBlankNode n = (RDFBlankNode) tuple.b;
						BlankNode tmp = new BlankNode( n, tuple.a.toString()); 
						//add it to the graph
						blankNodes.add(tmp);
						ret.add( tmp);
					}
					
				}else{
					
					propertyInformation.add(tuple);
					
				}
			} catch (Exception e) {
				tail("expand properties:  with tuple: "+ tuple);
				e.printStackTrace();
			}
			
		}
		return ret;
		

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
		OWLObjectProperty me =factory.getOWLObjectProperty(getIRI());
	
		for (RDFNodeTuple one : propertyInformation) {
			
			
			if(one.aPartContains(OWLVocabulary.RDFS_range)){
				OWLClass c = factory.getOWLClass(IRI.create(one.b.toString()));
				owlAPIOntologyCollector.addAxiom(factory.getOWLObjectPropertyRangeAxiom(me, c));
			}else if(one.aPartContains(OWLVocabulary.RDFS_domain)){
				OWLClass c = factory.getOWLClass(IRI.create(one.b.toString()));
				owlAPIOntologyCollector.addAxiom(factory.getOWLObjectPropertyDomainAxiom(me, c));
			}else if(one.aPartContains(OWLVocabulary.RDFS_SUB_PROPERTY_OF)){
				OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(one.b.toString()));
				owlAPIOntologyCollector.addAxiom(factory.getOWLSubObjectPropertyOfAxiom(me, p));
			}else if(one.aPartContains(OWLVocabulary.OWL_inverseOf)){
				OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(one.b.toString()));
				owlAPIOntologyCollector.addAxiom(factory.getOWLInverseObjectPropertiesAxiom(me, p));				
			}else if(one.aPartContains(OWLVocabulary.OWL_equivalentProperty)){
				OWLObjectProperty p = factory.getOWLObjectProperty(IRI.create(one.b.toString()));
				Set<OWLObjectProperty> tmp = new HashSet<OWLObjectProperty>();
				tmp.add(me);tmp.add(p);
				owlAPIOntologyCollector.addAxiom(factory.getOWLEquivalentObjectPropertiesAxiom(tmp));
				
			}else if(one.a.toString().equals(OWLVocabulary.RDFS_LABEL)){
				OWLAnnotation annoLabel = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLStringLiteral(one.b.toString()));
				OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(me.getIRI(), annoLabel);
				owlAPIOntologyCollector.addAxiom(ax);
			}else if(one.b.isLiteral()){
				// XXX comments
			}
			else {
				tail("conversion to ontology: property information: " + one);
			}
			
		}
		
		for (String one : specialTypes) {
			
			if(one.equals(OWLVocabulary.OWL_FunctionalProperty)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLFunctionalObjectPropertyAxiom(me));
			}else if(one.equals(OWLVocabulary.OWL_InverseFunctionalProperty)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLInverseFunctionalObjectPropertyAxiom(me));
			}else if(one.equals(OWLVocabulary.OWL_TransitiveProperty)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLTransitiveObjectPropertyAxiom(me));
			}else if(one.equals(OWLVocabulary.OWL_SymmetricProperty)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLSymmetricObjectPropertyAxiom(me));
			}else{
				tail("conversion to ontology: special types: " + one);
			}
		}
		for (BlankNode bn : blankNodes) {
			OWLClassExpression target = bn.getAnonymousClass(owlAPIOntologyCollector);
			if(bn.getInBoundEdge().equals(OWLVocabulary.RDFS_range)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLObjectPropertyRangeAxiom(me, target));
			}else if(bn.getInBoundEdge().equals(OWLVocabulary.RDFS_domain)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLObjectPropertyDomainAxiom(me, target));
			}
			//System.out.println(bn.getAnonymousClass(owlAPIOntologyCollector).toString());
		}
	}
	

	
}
