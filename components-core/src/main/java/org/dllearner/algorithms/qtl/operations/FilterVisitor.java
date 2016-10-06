/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.qtl.operations;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;

public class FilterVisitor extends OpVisitorBase {
	
	private List<Op> ops = new ArrayList<>();
	
	
	
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
