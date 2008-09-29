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
import org.semanticweb.owl.model.OWLAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLCommentAnnotation;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLLabelAnnotation;
import org.semanticweb.owl.model.OWLObjectProperty;

import com.hp.hpl.jena.rdf.model.Literal;

/**
 * A node in the graph that is an instance.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class InstanceNode extends Node {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger
		.getLogger(InstanceNode.class);

	private List<ClassNode> classes = new ArrayList<ClassNode>();
	//SortedSet<StringTuple> datatypes = new TreeSet<StringTuple>();
	private List<ObjectPropertyNode> objectProperties = new ArrayList<ObjectPropertyNode>();
	private List<DatatypePropertyNode> datatypeProperties = new ArrayList<DatatypePropertyNode>();

	public InstanceNode(String uri) {
		super(uri);
	}

	// expands all directly connected nodes
	@Override
	public List<Node> expand(TupleAquisitor tupelAquisitor, Manipulator manipulator) {

		SortedSet<RDFNodeTuple> newTuples = tupelAquisitor.getTupelForResource(uri);
		// see Manipulator
		newTuples = manipulator.manipulate(this, newTuples);
		
		List<Node> newNodes = new ArrayList<Node>();

		Node tmp;
		for (RDFNodeTuple tuple : newTuples) {
			
			if((tmp = processTuple(tuple))!= null) {
				newNodes.add(tmp);
			}			
		}//endfor
		expanded = true;
		return newNodes;
	}
	
	public Node processTuple( RDFNodeTuple tuple) {
		try {
			if(tuple.b.isLiteral()) {
				datatypeProperties.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
				return null;
			}else if(tuple.b.isAnon()){
				@SuppressWarnings("unused")
				RDFBlankNode n = (RDFBlankNode) tuple.b;
				logger.warn("encountered Bnode in InstanceNode"+ this +"in tuple" + tuple);
				return null;
			
			// basically : if p is rdf:type then o is a class
			// else it is an instance
			}else if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)) {
				ClassNode tmp = new ClassNode(tuple.b.toString());
				classes.add(tmp);
				return tmp;
			} else {
				InstanceNode tmp = new InstanceNode(tuple.b.toString());
				objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, tmp));
				return tmp;
			}
		} catch (Exception e) {
			System.out.println("Problem with: " + tuple);
			e.printStackTrace();
			return null;
		}
	}
	
	// gets the types for properties recursively
	@Override
	public List<BlankNode> expandProperties(TupleAquisitor tupelAquisitor, Manipulator manipulator) {
		List<BlankNode> ret =  new ArrayList<BlankNode>();
		for (ObjectPropertyNode one : objectProperties) {
			ret.addAll(one.expandProperties(tupelAquisitor, manipulator));
		}
		
		for (DatatypePropertyNode one : datatypeProperties) {
			ret.addAll(one.expandProperties(tupelAquisitor, manipulator));
		}
		return ret;

	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> returnSet = new TreeSet<String>();
		returnSet.add("<" + uri + "><" + OWLVocabulary.RDF_TYPE + "><" + OWLVocabulary.OWL_THING + ">.");
		for (ClassNode one : classes) {
			returnSet.add("<" + uri + "><" + OWLVocabulary.RDF_TYPE + "><" + one.getURIString() + ">.");
			returnSet.addAll(one.toNTriple());
		}
		for (ObjectPropertyNode one : objectProperties) {
			returnSet.add("<" + uri + "><" + one.getURIString() + "><" + one.getBPart().getURIString()
					+ ">.");
			returnSet.addAll(one.toNTriple());
			returnSet.addAll(one.getBPart().toNTriple());
		}
		
		for (DatatypePropertyNode one : datatypeProperties) {
			returnSet.add("<" + uri + "><" + one.getURIString() + "> " + one.getNTripleFormOfB()
					+ " .");
		}

		return returnSet;
	}
	
	@Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
	
		
		OWLIndividual me = factory.getOWLIndividual(getURI());
		
		for (ClassNode one : classes) {
			//create Axiom
			OWLClass c = factory.getOWLClass(one.getURI());
			OWLAxiom ax = factory.getOWLClassAssertionAxiom(me, c);
			//collect
			owlAPIOntologyCollector.addAxiom(ax);
			//handover
			one.toOWLOntology(owlAPIOntologyCollector);
		}
		for (ObjectPropertyNode one : objectProperties) {
			OWLAxiom ax = null;
			if(one.getURIString().equals(OWLVocabulary.OWL_DIFFERENT_FROM)){
				OWLIndividual o = factory.getOWLIndividual(one.getBPart().getURI());
				
				ax = factory.getOWLDifferentIndividualsAxiom(new OWLIndividual[]{me,o});
			}else{
			
				//create axiom
				OWLIndividual o = factory.getOWLIndividual(one.getBPart().getURI());
				OWLObjectProperty p = factory.getOWLObjectProperty(one.getURI());
				ax = factory.getOWLObjectPropertyAssertionAxiom(me, p, o);
			}
			//collect
			owlAPIOntologyCollector.addAxiom(ax);
			
			//handover
			one.toOWLOntology(owlAPIOntologyCollector);
			one.getBPart().toOWLOntology(owlAPIOntologyCollector);
		}
		
		for (DatatypePropertyNode one : datatypeProperties) {
			OWLDataProperty p = factory.getOWLDataProperty(one.getURI());
			Literal ln = one.getBPart().getLiteral();
			
			if(one.getURIString().equals(OWLVocabulary.RDFS_COMMENT)){
				//skip
				//OWLCommentAnnotation comment = factory.getOWL(one.b.toString());
				//owlAPIOntologyCollector.addAxiom(factory.getOWLEntityAnnotationAxiom(me, label));
			}else if(one.getURIString().equals(OWLVocabulary.RDFS_LABEL)){
				OWLLabelAnnotation label = factory.getOWLLabelAnnotation(one.b.toString());
				owlAPIOntologyCollector.addAxiom(factory.getOWLEntityAnnotationAxiom(me, label));
			}else{
			
			try{
				
				if(one.getBPart().isFloat()){
					owlAPIOntologyCollector.addAxiom(
							factory.getOWLDataPropertyAssertionAxiom(me, p, ln.getFloat()));
				} else if(one.getBPart().isDouble()){
					owlAPIOntologyCollector.addAxiom(
							factory.getOWLDataPropertyAssertionAxiom(me, p, ln.getDouble()));
				} else if(one.getBPart().isInt()){
					owlAPIOntologyCollector.addAxiom(
							factory.getOWLDataPropertyAssertionAxiom(me, p, ln.getInt()));
				} else if(one.getBPart().isBoolean()){
					owlAPIOntologyCollector.addAxiom(
							factory.getOWLDataPropertyAssertionAxiom(me, p, ln.getBoolean()));
				}else if(one.getBPart().isString()){
					//System.out.println(ln.getString()+" "+one.getBPart().isBoolean());
					owlAPIOntologyCollector.addAxiom(
					factory.getOWLDataPropertyAssertionAxiom(me, p, ln.getString()));
					
				}
				
				else {
					tail(getURIString()+"||"+one.getURIString());
				}
				
				//handover
				one.toOWLOntology(owlAPIOntologyCollector);
			
			}catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			}
			//factory.getOWLDataPropertyAssertionAxiom()
			//returnSet.add("<" + uri + "><" + one.getURI() + "> " + one.getNTripleFormOfB()
			//		+ " .");
		}
	}


	public List<ObjectPropertyNode> getObjectProperties() {
		return objectProperties;
	}
	
	public List<DatatypePropertyNode> getDatatypePropertyNode() {
		return datatypeProperties;
	}
	
	

}
