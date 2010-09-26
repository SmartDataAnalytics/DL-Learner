/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.sparqlquerygenerator.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeFactoryImpl implements QueryTreeFactory<String> {
	
	@Override
	public QueryTreeImpl<String> getQueryTree(String example, Model model) {
		return createTree(model.getResource(example), model);
	}

	@Override
	public QueryTreeImpl<String> getQueryTree(Resource example, Model model) {
		return createTree(example, model);
	}
	
	@Override
	public QueryTreeImpl<String> getQueryTree(String example) {
		return new QueryTreeImpl<String>(example);
	}
	
	private QueryTreeImpl<String> createTree(Resource s, Model model){
		Map<String, Set<Statement>> resource2Statements = new HashMap<String, Set<Statement>>();
		Statement st;
		Set<Statement> statements;
		for(Iterator<Statement> it = model.listStatements(); it.hasNext();){
			st = it.next();
			statements = resource2Statements.get(st.getSubject().toString());
			if(statements == null){
				statements = new HashSet<Statement>();
				resource2Statements.put(st.getSubject().toString(), statements);
			}
			statements.add(st);
		}
		QueryTreeImpl<String> tree = new QueryTreeImpl<String>(s.toString());
		fillTree(tree, resource2Statements);
				
		return tree;
	}
	
	private void fillTree(QueryTreeImpl<String> tree, Map<String, Set<Statement>> resource2Statements){
		if(resource2Statements.containsKey(tree.getUserObject())){
			QueryTreeImpl<String> subTree;
			for(Statement st : resource2Statements.get(tree.getUserObject())){
				if(st.getObject().isLiteral()){
					tree.addChild(new QueryTreeImpl<String>(st.getObject().toString()), st.getPredicate().toString());
				} else {
					if(!tree.getUserObjectPathToRoot().contains(st.getObject().toString())){
						subTree = new QueryTreeImpl<String>(st.getObject().toString());
						tree.addChild(subTree, st.getPredicate().toString());
						fillTree(subTree, resource2Statements);
					}
				}
			}
		}
	}

}
