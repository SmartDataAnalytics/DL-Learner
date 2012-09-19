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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.aquisitors.RDFBlankNode;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;

/**
 * Property node, has connection to a and b part
 * 
 * @author Sebastian Hellmann
 * 
 */

public class DatatypePropertyNode extends PropertyNode {

	//	 specialtypes like owl:symmetricproperty
	private SortedSet<String> specialTypes = new TreeSet<String>();
	private SortedSet<RDFNodeTuple> propertyInformation = new TreeSet<RDFNodeTuple>();	
	private List<BlankNode> blankNodes = new ArrayList<BlankNode>();
	
	public DatatypePropertyNode(String uri, Node a, LiteralNode b) {
		super(uri, a, b);
	}

	// Property Nodes are normally not expanded,
	// this function is never called
	@Override
	public List<Node> expand(TupleAquisitor tupelAquisitor, Manipulator manipulator) {
		return null;
	}

	// gets the types for properties recursively
	@Override
	public List<BlankNode>  expandProperties(TupleAquisitor tupelAquisitor, Manipulator manipulator, boolean dissolveBlankNodes) {
		List<BlankNode> ret =  new ArrayList<BlankNode>();
		//ret.addAll(b.expandProperties(tupelAquisitor, manipulator));
		SortedSet<RDFNodeTuple> newTypes = tupelAquisitor.getTupelForResource(uri);
		
		for (RDFNodeTuple tuple : newTypes) {
			try {
				if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)) {
					if(!tuple.b.toString().equals(OWLVocabulary.OWL_DATATYPPROPERTY)){
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
				logger.warn("resource "+uri+" with "+ tuple);
				e.printStackTrace();
			}
			
		}
		return ret;
	}
	
	@Override
	public LiteralNode getBPart(){
		return (LiteralNode)b;
	}
	
	public String getNTripleFormOfB() {
		return b.getNTripleForm();
	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> s = new TreeSet<String>();
		s.add(getNTripleForm()+"<" + OWLVocabulary.RDF_TYPE + "><"
				+ OWLVocabulary.OWL_DATATYPPROPERTY + ">.");

		return s;
	}
	
	@Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		

		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
		OWLDataProperty me =factory.getOWLDataProperty(getIRI());
	
		for (RDFNodeTuple one : propertyInformation) {
			
			
			if(one.aPartContains(OWLVocabulary.RDFS_range)){
				//System.out.println(me + one.b.toString());
				OWLDataRange o = factory.getOWLDatatype(IRI.create(one.b.toString()));
				OWLAxiom ax = factory.getOWLDataPropertyRangeAxiom(me, o);
				owlAPIOntologyCollector.addAxiom(ax);
				//XXX implement
				//OWLClass c = factory.getOWLClass(URI.create(one.b.toString()));
				//owlAPIOntologyCollector.addAxiom(factory.getOWLDataPropertyRangeAxiom(propery, owlDataRange)(me, c));
			}else if(one.aPartContains(OWLVocabulary.RDFS_domain)){
				OWLClass c = factory.getOWLClass(IRI.create(one.b.toString()));
				owlAPIOntologyCollector.addAxiom(factory.getOWLDataPropertyDomainAxiom(me, c));
			}
		}
		
		
		for (BlankNode bn : blankNodes) {
			OWLClassExpression target = bn.getAnonymousClass(owlAPIOntologyCollector);
			if(bn.getInBoundEdge().equals(OWLVocabulary.RDFS_range)){
			
				//XXX implement
				//owlAPIOntologyCollector.addAxiom(factory.getOWLObjectPropertyRangeAxiom(me, target));
			}else if(bn.getInBoundEdge().equals(OWLVocabulary.RDFS_domain)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLDataPropertyDomainAxiom(me, target));
				
			}
			//System.out.println(bn.getAnonymousClass(owlAPIOntologyCollector).toString());
		}
		
		
	}
	
	

}
