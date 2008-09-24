package org.dllearner.kb.extraction;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.aquisitors.BlankNodeCollector;
import org.dllearner.kb.aquisitors.RDFBlankNode;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;

public class BlankNode extends Node {

	RDFBlankNode bNode;
	
	String inboundEdge;
	
	private List<Node> connectedNodes =new ArrayList<Node>();
	
	private List<ObjectPropertyNode> objectProperties = new ArrayList<ObjectPropertyNode>();
	private List<DatatypePropertyNode> datatypeProperties = new ArrayList<DatatypePropertyNode>();

	
	public BlankNode(RDFBlankNode bNode, String inboundEdge){
		super(""+bNode.getBNodeId());
		this.bNode=bNode;
		this.inboundEdge = inboundEdge;
	}

	
	
	@Override
	public List<Node> expand(TupleAquisitor TupelAquisitor,
			Manipulator manipulator) {
		List<Node> newNodes = new ArrayList<Node>();
		SortedSet<RDFNodeTuple> s = BlankNodeCollector.getBlankNode(bNode.getBNodeId());
		for (RDFNodeTuple tuple : s) {
			if(tuple.b.isLiteral()) {
				datatypeProperties.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
				connectedNodes.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
			}else if(tuple.b.isAnon()){
				
				BlankNode tmp = new BlankNode( (RDFBlankNode)tuple.b, tuple.a.toString());
				objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, tmp ));
				connectedNodes.add(new BlankNode( (RDFBlankNode)tuple.b, tuple.a.toString()));
				newNodes.add(tmp);
			}else{
				objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, new ClassNode(tuple.b.toString()) ));
				connectedNodes.add(new ObjectPropertyNode(tuple.a.toString(), this, new ClassNode(tuple.b.toString()) ));
			}
		}
		return newNodes;
	}

	@Override
	public List<BlankNode>  expandProperties(TupleAquisitor TupelAquisitor,
			Manipulator manipulator) {
		return new ArrayList<BlankNode>();
	}

	@Override
	public SortedSet<String> toNTriple() {
		SortedSet<String> returnSet = new TreeSet<String>();
		String subject = getNTripleForm();
		for (ObjectPropertyNode one : objectProperties) {
			returnSet.add(subject + one.getNTripleForm() + one.getBPart().getNTripleForm()+" . ");
			returnSet.addAll(one.getBPart().toNTriple());
		}
		return returnSet;
	}
	
	@Override
	public String getNTripleForm(){
		return " "+uri+" ";
	}
	
	@Override
	public URI getURI(){
		return URI.create("http://www.empty.org/empty#empty");
	}
	
	@Override
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		//FIXME
	}
	
	public String getInBoundEdge(){
		return inboundEdge;
	}
	
	public OWLDescription getAnonymousClass(OWLAPIOntologyCollector owlAPIOntologyCollector){
		OWLDataFactory factory =  owlAPIOntologyCollector.getFactory();
		OWLDescription ret = factory.getOWLClass(URI.create("http://dummy.org/dummy"));
		
		for (Node n : connectedNodes) {
			System.out.println(n.toString());
		}
		
		if(containsDataTypeProperties()){
			//do nothing right now, add a return here;
		}
		
		Set<OWLDescription> l = new HashSet<OWLDescription>();
		for (Node n : connectedNodes) {
			if(n instanceof BlankNode){
				l.add(((BlankNode)n).getAnonymousClass(owlAPIOntologyCollector));
			}else{
				l.add(factory.getOWLClass(n.getURI()));
			}
		}
		
		if(isOfType(OWLVocabulary.OWL_intersectionOf)){
			ret = factory.getOWLObjectIntersectionOf(l);
			System.out.println("aaa");
		}else if(isOfType(OWLVocabulary.OWL_unionOf)){
			ret = factory.getOWLObjectUnionOf(l);
			System.out.println("aaa");
		}else if(isOfType(OWLVocabulary.OWL_complementOf)){
			ret = factory.getOWLObjectComplementOf(new ArrayList<OWLDescription>(l).remove(0));
			System.out.println("aaa");
		}
		return ret;
	}
	
	
	
	private boolean isOfType(String type){
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
	}

}
