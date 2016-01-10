package org.dllearner.utilities.datastructures;

import com.google.common.collect.ComparisonChain;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.util.NodeComparator;
import org.dllearner.kb.extraction.LiteralNode;

/**
 * A container which can hold two Strings, mainly used as a helper.
 * Also used as pre form, if you want to create triple, that have the same subject
 * @author Sebastian Hellmann
 */
public class RDFNodeTuple implements Comparable<RDFNodeTuple>{

	public RDFNode a;
	public RDFNode b;

	public RDFNodeTuple(RDFNode a, RDFNode b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return "<" + a.toString() + "|" + b.toString() + ">";
	}

	public boolean equals(RDFNodeTuple t) {
		return b.equals(t.b) && a.equals(t.a);
	}

	public int compareTo(RDFNodeTuple t) {
		NodeComparator comparator = new NodeComparator();
		return ComparisonChain.start().
				compare(a.asNode(), t.a.asNode(), comparator).
				compare(b.asNode(), t.b.asNode(), comparator).
				result();
	}
	
	public boolean aPartContains(String partOf) {
		return a.toString().contains(partOf);
	}
	
	public boolean bPartContains(String partOf) {
		return b.toString().contains(partOf);
	}
	
	
	public String getNTriple (String subject){
		String ret = "<"+subject+"> ";
		ret+="<"+a.toString()+"> ";
		if(b.isLiteral()){
			ret+=new LiteralNode(b).getNTripleForm();
			
		}else{
			ret+="<"+b.toString()+"> ";
		}
		ret+=".";
		return ret;
	}

}
