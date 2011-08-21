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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.aquisitors.RDFBlankNode;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class BlankNode extends Node {
	private static Logger logger = Logger
	.getLogger(BlankNode.class);
	
	RDFBlankNode bNode;
	
	String inboundEdge;
	
	
	private List<BlankNode> blankNodes =new ArrayList<BlankNode>();
	private SortedSet<StringTuple> otherNodes = new TreeSet<StringTuple> ();
	private List<DatatypePropertyNode> datatypeProperties = new ArrayList<DatatypePropertyNode>();
	
	//private List<ObjectPropertyNode> objectProperties = new ArrayList<ObjectPropertyNode>();
	//private List<DatatypePropertyNode> datatypeProperties = new ArrayList<DatatypePropertyNode>();

	
	public BlankNode(RDFBlankNode bNode, String inboundEdge){
		super(""+bNode.getBNodeId());
		this.bNode=bNode;
		this.inboundEdge = inboundEdge;
	}

	
	
	@Override
	public List<Node> expand(TupleAquisitor tupleAquisitor,
			Manipulator manipulator) {
		List<Node> newNodes = new ArrayList<Node>();
		SortedSet<RDFNodeTuple> s = tupleAquisitor.getBlankNode(bNode.getBNodeId());
		//System.out.println("entering "+bNode.getBNodeId());
		
	
		for (RDFNodeTuple tuple : s) {
			if(tuple.b.isLiteral()) {
				//System.out.println("adding dtype: "+tuple);
				datatypeProperties.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
				//connectedNodes.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
			}else if(tuple.b.isAnon()){
				//System.out.println("adding bnode: "+tuple);
				BlankNode tmp = new BlankNode( (RDFBlankNode)tuple.b, tuple.a.toString());
				//objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, tmp ));
				//connectedNodes.add(new BlankNode( (RDFBlankNode)tuple.b, tuple.a.toString()));
				blankNodes.add(tmp);
				newNodes.add(tmp);
			}else{
				//System.out.println("adding other: "+tuple);
				otherNodes.add(new StringTuple(tuple.a.toString(), tuple.b.toString()));
				//objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, new ClassNode(tuple.b.toString()) ));
				//connectedNodes.add(new ObjectPropertyNode(tuple.a.toString(), this, new ClassNode(tuple.b.toString()) ));
			}
		}
		
		//System.out.println("finished");
	
		return newNodes;
	}

	@Override
	public List<BlankNode>  expandProperties(TupleAquisitor TupelAquisitor,
			Manipulator manipulator, boolean dissolveBlankNodes) {
		return new ArrayList<BlankNode>();
	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> returnSet = new TreeSet<String>();
		//String subject = getNTripleForm();
		/*for (ObjectPropertyNode one : objectProperties) {
			returnSet.add(subject + one.getNTripleForm() + one.getBPart().getNTripleForm()+" . ");
			returnSet.addAll(one.getBPart().toNTriple());
		}*/
		return returnSet;
	}
	
	@Override
	public String getNTripleForm(){
		return " "+uri+" ";
	}
	
	@Override
	public String toString(){
		return "id: "+bNode.getBNodeId()+" inbound: "+getInBoundEdge();
		
	}
	
	@Override
	public IRI getIRI(){
		return IRI.create("http://www.empty.org/empty#empty");
	}
	
	@Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		logger.error("toOWLOntology called in blanknodes ");
		//FIXME robably not needed
	}
	
	public String getInBoundEdge(){
		return inboundEdge;
	}
		
	public OWLClassExpression getAnonymousClass(OWLAPIOntologyCollector owlAPIOntologyCollector){
		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
		OWLClassExpression ret = factory.getOWLClass(IRI.create("http://dummy.org/dummy"));
		
		//System.out.println(inboundEdge);
		
		if(
			(inboundEdge.equals(OWLVocabulary.OWL_intersectionOf))||
			(inboundEdge.equals(OWLVocabulary.OWL_complementOf))||
			(inboundEdge.equals(OWLVocabulary.OWL_unionOf))
		 ){
			Set<OWLClassExpression> target = new HashSet<OWLClassExpression>();
			List<BlankNode> tmp = new ArrayList<BlankNode>();
			tmp.add(this);
			while(!tmp.isEmpty()){
				BlankNode next = tmp.remove(0);
				//next.printAll();
				
				if(next.otherNodes.contains(new StringTuple(OWLVocabulary.RDF_REST, OWLVocabulary.RDF_NIL))){
					for(StringTuple t : next.otherNodes){
						if(t.a.equals(OWLVocabulary.RDF_FIRST)){
							target.add(factory.getOWLClass(IRI.create(t.b)));
							//System.out.println("added "+t.b);
						}
					}
					//System.out.println("nil found");
					//do nothing
				}else{
					StringTuple firstOtherNodes = null;
					try{
						firstOtherNodes = next.otherNodes.first();
						if(firstOtherNodes.a.equals(OWLVocabulary.RDF_FIRST)){
							target.add(factory.getOWLClass(IRI.create(firstOtherNodes.b)));
							tmp.add(next.blankNodes.get(0));
							//System.out.println("bnode added");
						}else{
							
							tail("double nesting not supported yet");
							
						}
						
						
					}catch (NoSuchElementException e) {
						logger.warn("something strange happened here: "+firstOtherNodes);
						logger.warn("and here: "+next.otherNodes);
						e.printStackTrace();
					}
					
				}
			}//end while
			
			if(inboundEdge.equals(OWLVocabulary.OWL_intersectionOf)){
				return factory.getOWLObjectIntersectionOf(target);
			}else if(inboundEdge.equals(OWLVocabulary.OWL_unionOf)){
				return factory.getOWLObjectUnionOf(target);
			}else if(inboundEdge.equals(OWLVocabulary.OWL_complementOf)){
				if(target.size()>1) {
					tail("more than one complement"+target);
					
				}else{
					return factory.getOWLObjectComplementOf(new ArrayList<OWLClassExpression>(target).remove(0));
				}
			}else{
				printAll();
				tail("bnode: wrong type: " +inboundEdge+ this);
			}
		}
		
		// restriction
		if(otherNodes.contains(
				new StringTuple(OWLVocabulary.RDF_TYPE, OWLVocabulary.OWL_RESTRICTION))){
			return getRestriction( owlAPIOntologyCollector);
			
		}
		
		if(!blankNodes.isEmpty()){
			return blankNodes.get(0).getAnonymousClass(owlAPIOntologyCollector);
		}
		
		
		return ret;
		
	}
	
	public void printAll(){
		logger.debug(this);
		
		logger.debug("otherNodes");
		for (StringTuple t : otherNodes) {
			logger.debug(""+t);
		}
		logger.debug("***************");
		logger.debug("dtype ");
		for (DatatypePropertyNode d : datatypeProperties) {
			logger.debug(d.getURIString()+" "+d.getNTripleFormOfB());
		}
		logger.debug("***************");
		logger.debug("other bnodes");
		for (BlankNode b : blankNodes) {
			logger.debug(b);
		}
		logger.debug("***************");
		
	}
	
	private OWLClassExpression getRestriction(OWLAPIOntologyCollector owlAPIOntologyCollector){
		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
		OWLObjectProperty property = null;
		OWLClassExpression concept = null;
		OWLClassExpression dummy = factory.getOWLClass(IRI.create("http://dummy.org/dummy"));
		
		int total = otherNodes.size()+blankNodes.size()+datatypeProperties.size();
		if(total >=4 ){
			logger.info("qualified p restrictions not supported currently");
		}
		
		// get Objectproperty
		for(StringTuple n : otherNodes) {
			if(n.a.equals(OWLVocabulary.OWL_ON_PROPERTY)){
				property = factory.getOWLObjectProperty(IRI.create(n.b)); 
			}
		}
		
		// has an Integer value
		if(!datatypeProperties.isEmpty()){
			DatatypePropertyNode d = datatypeProperties.get(0);
			String p = d.getURIString();
			if( p.equals(OWLVocabulary.OWL_cardinality)){
				return factory.getOWLObjectExactCardinality(d.getBPart().getLiteral().getInt(), property);
			}else if(p.equals(OWLVocabulary.OWL_maxCardinality)){
				return factory.getOWLObjectMaxCardinality(d.getBPart().getLiteral().getInt(), property);
			}else if(p.equals(OWLVocabulary.OWL_minCardinality)){
				return factory.getOWLObjectMinCardinality(d.getBPart().getLiteral().getInt(), property);
			}else {
				tail(p+d+" in "+this);
			}
		}
		
		if(!blankNodes.isEmpty()){
			concept = blankNodes.get(0).getAnonymousClass(owlAPIOntologyCollector);
		}else{
			for(StringTuple n : otherNodes) {
				String p = n.a;
				String o = n.b;
				if(
					(p.equals(OWLVocabulary.OWL_ALL_VALUES_FROM)) ||
					(p.equals(OWLVocabulary.OWL_SOME_VALUES_FROM)) ||
					(p.equals(OWLVocabulary.OWL_HAS_VALUE))
				  ){
					concept = factory.getOWLClass(IRI.create(o));
				}
			}
		}
		
		for(StringTuple n : otherNodes) {
			String p = n.a;
			if(p.equals(OWLVocabulary.OWL_ALL_VALUES_FROM)){
				return factory.getOWLObjectAllValuesFrom(property, concept);
			}else if(p.equals(OWLVocabulary.OWL_SOME_VALUES_FROM)){
				return factory.getOWLObjectSomeValuesFrom(property, concept);
			}else if(p.equals(OWLVocabulary.OWL_HAS_VALUE)){
				logger.warn("OWL_hasValue not implemented yet");
				return dummy;
			}
		}
		return dummy;
	}
	
	
	
	
	/*private boolean isOfType(String type){
		for (Node n : connectedNodes) {
			if((n  instanceof BlankNode )
					&& 
				((BlankNode)n).getInBoundEdge().equals(type)) {
				return true;
			}else if((n  instanceof ObjectPropertyNode )
					&& 
				((ObjectPropertyNode)n).getAPart().toString().equals(type)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsDataTypeProperties(){
		for (Node n : connectedNodes) {
			if(n  instanceof DatatypePropertyNode) {
				return true;
			}
		}
		return false;
	}*/

}
