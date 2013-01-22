package org.dllearner.algorithms.qtl.operations;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;

public class FilterVisitor extends OpVisitorBase {
	
	private List<Op> ops = new ArrayList<Op>();
	
	
	
	@Override
	public void visit(OpProject opProject) {
		opProject.getSubOp().visit(this) ;
	}
	
	@Override
	public void visit(OpBGP opBGP) {
		for (Triple t : opBGP.getPattern()){
			if(t.getObject().isURI()){
				System.out.println(t.getObject().toString());
			}
		}
	}
	
	@Override
	public void visit(OpFilter opFilter) {
		// TODO Auto-generated method stub
		super.visit(opFilter);
	}

}
