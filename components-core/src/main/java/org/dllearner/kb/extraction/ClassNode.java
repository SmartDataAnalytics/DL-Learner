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

import org.apache.log4j.Logger;
import org.dllearner.kb.aquisitors.RDFBlankNode;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

/**
 * Is a node in the graph, that is a class.
 * 
 * @author Sebastian Hellmann
 */
public class ClassNode extends Node {
	
	private static Logger logger = Logger
		.getLogger(ClassNode.class);
	
	List<ObjectPropertyNode> classProperties = new ArrayList<ObjectPropertyNode>();
	List<DatatypePropertyNode> datatypeProperties = new ArrayList<DatatypePropertyNode>();
	List<BlankNode> blankNodes = new ArrayList<BlankNode>();
	
	public ClassNode(String uri) {
		super(uri);
	}

	// expands all directly connected nodes
	@Override
	public List<Node> expand(TupleAquisitor tupelAquisitor, Manipulator manipulator) {

		SortedSet<RDFNodeTuple> newTuples = tupelAquisitor.getTupelForResource(this.uri);
		// see manipulator
		newTuples = manipulator.manipulate(this, newTuples);
			
		List<Node> newNodes = new ArrayList<Node>();
		Node tmp;
		for (RDFNodeTuple tuple : newTuples) {
			if((tmp = processTuple(tuple,tupelAquisitor.isDissolveBlankNodes()))!= null) {
				newNodes.add(tmp);
			}		
		}
		return newNodes;
	}
	
	private Node processTuple( RDFNodeTuple tuple, boolean dissolveBlankNodes) {
		
		try {
			String property = tuple.a.toString();
			if(tuple.b.isLiteral()) {
				datatypeProperties.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
				return null;
			}else if(tuple.b.isAnon()){
				if(dissolveBlankNodes){
					RDFBlankNode n = (RDFBlankNode) tuple.b;
					BlankNode tmp = new BlankNode( n, tuple.a.toString()); 
					//add it to the graph
					blankNodes.add(tmp);
					//return tmp;
					return tmp;
				}else{
					//do nothing
					return null;
				}
			 // substitute rdf:type with owl:subclassof
			}else if (property.equals(OWLVocabulary.RDF_TYPE) || 
					OWLVocabulary.isStringSubClassVocab(property)) {
				ClassNode tmp = new ClassNode(tuple.b.toString());
				classProperties.add(new ObjectPropertyNode( OWLVocabulary.RDFS_SUBCLASS_OF, this, 	tmp));
				return tmp;
			} else {
				// further expansion stops here
				ClassNode tmp = new ClassNode(tuple.b.toString());
				classProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, tmp));
				return tmp; //is missing on purpose
			}
		} catch (Exception e) {
			logger.warn("Problem with: " + this + " in tuple " + tuple);
			e.printStackTrace();
			
		} 
		return null;
	}

	// gets the types for properties recursively
	@Override
	public List<BlankNode>  expandProperties(TupleAquisitor tupelAquisitor, Manipulator manipulator, boolean dissolveBlankNodes) {
		return new ArrayList<BlankNode>();
	}
	


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.kb.sparql.datastructure.Node#toNTriple()
	 */
	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> returnSet = new TreeSet<String>();
		String subject = getNTripleForm();
		returnSet.add(subject+"<" + OWLVocabulary.RDF_TYPE + "><" + OWLVocabulary.OWL_CLASS + ">.");

		for (ObjectPropertyNode one : classProperties) {
			returnSet.add(subject + one.getNTripleForm() + 
					one.getBPart().getNTripleForm()+" .");
			returnSet.addAll(one.getBPart().toNTriple());
		}
		for (DatatypePropertyNode one : datatypeProperties) {
			returnSet.add(subject+ one.getNTripleForm() + one.getNTripleFormOfB()
					+ " .");
		}

		return returnSet;
	}
	
	@Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		try{
		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
		
		OWLClass me =factory.getOWLClass(getIRI());
		for (ObjectPropertyNode one : classProperties) {
			OWLClass c = factory.getOWLClass(one.getBPart().getIRI());
			if(OWLVocabulary.isStringSubClassVocab(one.getURIString())){
				owlAPIOntologyCollector.addAxiom(factory.getOWLSubClassOfAxiom(me, c));
			}else if(one.getURIString().equals(OWLVocabulary.OWL_DISJOINT_WITH)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLDisjointClassesAxiom(me, c));
			}else if(one.getURIString().equals(OWLVocabulary.OWL_EQUIVALENT_CLASS)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLEquivalentClassesAxiom(me, c));
			}else if(one.getURIString().equals(OWLVocabulary.RDFS_IS_DEFINED_BY)){
				logger.warn("IGNORING: "+OWLVocabulary.RDFS_IS_DEFINED_BY);
				continue;
			}else {
				tail(true, "in ontology conversion"+" object property is: "+one.getURIString()+" connected with: "+one.getBPart().getURIString());
				continue;
			}
			one.getBPart().toOWLOntology(owlAPIOntologyCollector);
		}
		for (DatatypePropertyNode one : datatypeProperties) {
			//FIXME add languages
			// watch for tail
			if(one.getURIString().equals(OWLVocabulary.RDFS_COMMENT)){
				OWLAnnotation annoComment = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLStringLiteral(one.getBPart().getLiteral().getString()));
				OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(me.getIRI(), annoComment);
				owlAPIOntologyCollector.addAxiom(ax);
				
			}else if(one.getURIString().equals(OWLVocabulary.RDFS_LABEL)) {
				OWLAnnotation annoLabel = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLStringLiteral(one.getBPart().getLiteral().getString()));
				OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(me.getIRI(), annoLabel);
				owlAPIOntologyCollector.addAxiom(ax);
			}else {
				tail(true, "in ontology conversion: no other datatypes, but annotation is allowed for classes."+" data property is: "+one.getURIString()+" connected with: "+one.getBPart().getNTripleForm());
				
			}
		
		}
		for (BlankNode bn : blankNodes) {
			OWLClassExpression target = bn.getAnonymousClass(owlAPIOntologyCollector);
			
			if(OWLVocabulary.isStringSubClassVocab(bn.getInBoundEdge())){
				owlAPIOntologyCollector.addAxiom(factory.getOWLSubClassOfAxiom(me, target));
			}else if(bn.getInBoundEdge().equals(OWLVocabulary.OWL_DISJOINT_WITH)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLDisjointClassesAxiom(me, target));
			}else if(bn.getInBoundEdge().equals(OWLVocabulary.OWL_EQUIVALENT_CLASS)){
				owlAPIOntologyCollector.addAxiom(factory.getOWLEquivalentClassesAxiom(me, target));
			}else {
				tail( "in ontology conversion"+" bnode is: "+bn.getInBoundEdge()+"||"+ bn );
				
			}
			
		}
		}catch (Exception e) {
			System.out.println("aaa"+getURIString());
			e.printStackTrace();
		}
	}
		

}
