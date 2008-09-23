package org.dllearner.kb.extraction;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.aquisitors.BlankNodeCollector;
import org.dllearner.kb.aquisitors.RDFBlankNode;
import org.dllearner.kb.aquisitors.TupleAquisitor;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

public class BlankNode extends Node {

	RDFBlankNode bNode;
	
	private List<ObjectPropertyNode> objectProperties = new ArrayList<ObjectPropertyNode>();
	private List<DatatypePropertyNode> datatypeProperties = new ArrayList<DatatypePropertyNode>();

	
	public BlankNode(RDFBlankNode bNode){
		super("_:internal_"+bNode.getBNodeId());
		this.bNode=bNode;
	}

	
	
	@Override
	public List<Node> expand(TupleAquisitor TupelAquisitor,
			Manipulator manipulator) {
		List<Node> newNodes = new ArrayList<Node>();
		SortedSet<RDFNodeTuple> s = BlankNodeCollector.getBlankNode(bNode.getBNodeId());
		for (RDFNodeTuple tuple : s) {
			if(tuple.b.isLiteral()) {
				datatypeProperties.add(new DatatypePropertyNode(tuple.a.toString(), this, new LiteralNode(tuple.b) ));
			}else if(tuple.b.isAnon()){
				
				BlankNode tmp = new BlankNode( (RDFBlankNode)tuple.b);
				objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, tmp ));
				newNodes.add(tmp);
			}else{
				objectProperties.add(new ObjectPropertyNode(tuple.a.toString(), this, new ClassNode(tuple.b.toString()) ));
			}
		}
		return newNodes;
	}

	@Override
	public void expandProperties(TupleAquisitor TupelAquisitor,
			Manipulator manipulator) {
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
	public void toOWLOntology( OWLAPIOntologyCollector owlAPIOntologyCollector){
		//FIXME
	}

}
