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

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;
import org.dllearner.sparqlquerygenerator.util.Filter;

import com.hp.hpl.jena.rdf.model.Literal;
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
		Comparator<Statement> comparator = new StatementComparator();
		
		SortedMap<String, SortedSet<Statement>> resource2Statements = new TreeMap<String, SortedSet<Statement>>();
		
		Statement st;
		SortedSet<Statement> statements;
		for(Iterator<Statement> it = model.listStatements(); it.hasNext();){
			st = it.next();
			statements = resource2Statements.get(st.getSubject().toString());
			if(statements == null){
				statements = new TreeSet<Statement>(comparator);
				resource2Statements.put(st.getSubject().toString(), statements);
			}
			statements.add(st);
		}
		QueryTreeImpl<String> tree = new QueryTreeImpl<String>(s.toString());
		fillTree(tree, resource2Statements);
				
		tree.setUserObject("?");
		return tree;
	}
	
	private void fillTree(QueryTreeImpl<String> tree, SortedMap<String, SortedSet<Statement>> resource2Statements){
		if(resource2Statements.containsKey(tree.getUserObject())){
			QueryTreeImpl<String> subTree;
			for(Statement st : resource2Statements.get(tree.getUserObject())){
				if(Filter.getAllFilterProperties().contains(st.getPredicate().toString())){
					continue;
				}
				if(st.getObject().isLiteral()){
					Literal lit = st.getLiteral();
					StringBuilder sb = new StringBuilder();
					sb.append("\"").append(lit.getLexicalForm()).append("\"");
					if(lit.getDatatypeURI() != null){
						sb.append("^^<").append(lit.getDatatypeURI()).append(">");
					}
					if(!lit.getLanguage().isEmpty()){
						sb.append("@").append(lit.getLanguage());
					}
					tree.addChild(new QueryTreeImpl<String>(sb.toString()), st.getPredicate().toString());
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
	
	class StatementComparator implements Comparator<Statement>{

		@Override
		public int compare(Statement s1, Statement s2) {
			if(s1.getPredicate() == null && s2.getPredicate() == null){
				return 0;
			}
			return s1.getPredicate().toString().compareTo(s2.getPredicate().toString())
			+ s1.getObject().toString().compareTo(s2.getObject().toString());
		}

		
		
	}

}
