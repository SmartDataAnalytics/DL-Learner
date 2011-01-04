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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private int nodeId;
	private Comparator<Statement> comparator;
	
	public QueryTreeFactoryImpl(){
		comparator = new StatementComparator();
	}
	
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
		nodeId = 0;
		
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
		tree.setId(nodeId++);
		if(resource2Statements.containsKey(tree.getUserObject())){
			QueryTreeImpl<String> subTree;
			for(Statement st : resource2Statements.get(tree.getUserObject())){
				if(Filter.getAllFilterProperties().contains(st.getPredicate().toString())){
					continue;
				}
				if(st.getObject().isLiteral()){
					Literal lit = st.getLiteral();
					String escapedLit = lit.getLexicalForm().replace("\"", "\\\"");
					StringBuilder sb = new StringBuilder();
					sb.append("\"").append(escapedLit).append("\"");
					if(lit.getDatatypeURI() != null){
						sb.append("^^<").append(lit.getDatatypeURI()).append(">");
					}
					if(!lit.getLanguage().isEmpty()){
						sb.append("@").append(lit.getLanguage());
					}
					subTree = new QueryTreeImpl<String>(sb.toString());
					subTree.setId(nodeId++);
					subTree.setLiteralNode(true);
					tree.addChild(subTree, st.getPredicate().toString());
				} else {
					if(tree.getUserObjectPathToRoot().size() < 3 && 
							!tree.getUserObjectPathToRoot().contains(st.getObject().toString())){
						subTree = new QueryTreeImpl<String>(st.getObject().toString());
						subTree.setResourceNode(true);
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
//			if(s1.getPredicate() == null && s2.getPredicate() == null){
//				return 0;
//			}
//			return s1.getPredicate().toString().compareTo(s2.getPredicate().toString())
//			+ s1.getObject().toString().compareTo(s2.getObject().toString());
			if(s1.getPredicate() == null && s2.getPredicate() == null){
				return 0;
			}
			
			if(s1.getPredicate().toString().compareTo(s2.getPredicate().toString()) == 0){
				return s1.getObject().toString().compareTo(s2.getObject().toString());
			} else {
				return s1.getPredicate().toString().compareTo(s2.getPredicate().toString());
			}
			
		}

		
		
	}
	
	public static String encode(String s) {
        char [] htmlChars = s.toCharArray();
        StringBuffer encodedHtml = new StringBuffer();
        for (int i=0; i<htmlChars.length; i++) {
            switch(htmlChars[i]) {
            case '<':
                encodedHtml.append("&lt;");
                break;
            case '>':
                encodedHtml.append("&gt;");
                break;
            case '&':
                encodedHtml.append("&amp;");
                break;
            case '\'':
                encodedHtml.append("&#39;");
                break;
            case '"':
                encodedHtml.append("&quot;");
                break;
            case '\\':
                encodedHtml.append("&#92;");
                break;
            case (char)133:
                encodedHtml.append("&#133;");
                break;
            default:
                encodedHtml.append(htmlChars[i]);
                break;
            }
        }
        return encodedHtml.toString();
    }

}
