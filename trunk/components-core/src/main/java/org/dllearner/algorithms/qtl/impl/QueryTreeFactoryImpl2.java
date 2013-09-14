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
package org.dllearner.algorithms.qtl.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.filters.Filter;
import org.dllearner.algorithms.qtl.filters.Filters;
import org.dllearner.algorithms.qtl.filters.QuestionBasedStatementFilter;
import org.dllearner.algorithms.qtl.filters.QuestionBasedStatementFilter2;
import org.dllearner.algorithms.qtl.filters.ZeroFilter;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeFactoryImpl2 implements QueryTreeFactory<String> {
	
	private int nodeId;
	private Comparator<Statement> comparator;
	private Set<String> predicateFilters;
	
	private Filter predicateFilter = new ZeroFilter();
	private Filter objectFilter = new ZeroFilter();
	private Selector statementSelector = new SimpleSelector();
	private com.hp.hpl.jena.util.iterator.Filter<Statement> keepFilter;
	
	public QueryTreeFactoryImpl2(){
		comparator = new StatementComparator();
		predicateFilters = new HashSet<String>(Filters.getAllFilterProperties());
	}
	
	public void setPredicateFilter(Filter filter){
		this.predicateFilter = filter;
	}
	
	public void setObjectFilter(Filter filter){
		this.objectFilter = filter;
	}
	
	@Override
	public void setStatementSelector(Selector selector) {
		this.statementSelector = selector;
		
	}
	
	@Override
	public void setStatementFilter(com.hp.hpl.jena.util.iterator.Filter<Statement> statementFilter) {
		this.keepFilter = statementFilter;
		
	}
	
	@Override
	public QueryTreeImpl<String> getQueryTree(String example, Model model) {
		if(keepFilter == null){
			return createTree(model.getResource(example), model);
		} else {
			return createTreeOptimized(model.getResource(example), model);
		}
	}
	
	@Override
	public QueryTreeImpl<String> getQueryTree(String example, Model model, int maxEdges) {
		if(keepFilter == null){
			return createTree(model.getResource(example), model);
		} else {
			return createTreeOptimized(model.getResource(example), model, maxEdges);
		}
	}

	@Override
	public QueryTreeImpl<String> getQueryTree(Resource example, Model model) {
		return createTree(example, model);
	}
	
	@Override
	public QueryTreeImpl<String> getQueryTree(String example) {
		return new QueryTreeImpl<String>(example);
	}
	
	private QueryTreeImpl<String> createTreeOptimized(Resource s, Model model, int maxEdges){
		nodeId = 0;
		SortedMap<String, SortedSet<Statement>> resource2Statements = new TreeMap<String, SortedSet<Statement>>();
		
		fillMap(s, model, resource2Statements, null);	
	
		QuestionBasedStatementFilter filter = (QuestionBasedStatementFilter)keepFilter;
		Set<Statement> statements;
		int diff = valueCount(resource2Statements) - maxEdges;
		main:while(diff > 0){
			double oldThreshold = filter.getThreshold();
			statements = filter.getStatementsBelowThreshold(oldThreshold+0.1);
			for(SortedSet<Statement> set : resource2Statements.values()){
				for(Statement st : statements){
					if(set.remove(st)){
						diff--;
						if(diff == 0){
							break main;
						}
					}
				}
			}
		}
		
		
		QueryTreeImpl<String> tree = new QueryTreeImpl<String>(s.toString());
		fillTree(tree, resource2Statements);
				
		tree.setUserObject("?");
		return tree;
	}
	
	private int valueCount(SortedMap<String, SortedSet<Statement>> map){
		int cnt = 0;
		for(SortedSet<Statement> statements : map.values()){
			cnt += statements.size();
		}
		return cnt;
	}
	
	private QueryTreeImpl<String> createTreeOptimized(Resource s, Model model){
		nodeId = 0;
		SortedMap<String, SortedSet<Statement>> resource2Statements = new TreeMap<String, SortedSet<Statement>>();
		
		fillMap(s, model, resource2Statements, null);	
		
		QueryTreeImpl<String> tree = new QueryTreeImpl<String>(s.toString());
		fillTree(tree, resource2Statements);
				
		tree.setUserObject("?");
		return tree;
	}
	
	private void fillMap(Resource s, Model model, SortedMap<String, SortedSet<Statement>> resource2Statements, String oldSimilarToken){
		Iterator<Statement> it = model.listStatements(s, null, (RDFNode)null).filterKeep(keepFilter);
		Statement st;
		SortedSet<Statement> statements;
		while(it.hasNext()){
			st = it.next();
			String newSimilarToken = ((QuestionBasedStatementFilter2)keepFilter).getStatement2TokenMap().get(st);
			System.out.println(st);
			System.out.println(newSimilarToken);
			if(!newSimilarToken.equals(oldSimilarToken) || newSimilarToken.equals("ALL")){
				statements = resource2Statements.get(st.getSubject().toString());
				if(statements == null){
					statements = new TreeSet<Statement>(comparator);
					resource2Statements.put(st.getSubject().toString(), statements);
				}
				statements.add(st);
				if(st.getObject().isURIResource() && !resource2Statements.containsKey(st.getObject().asResource().getURI())){
					fillMap(st.getObject().asResource(), model, resource2Statements, newSimilarToken);
				}
			}
			
		}
	}
	
	private QueryTreeImpl<String> createTree(Resource s, Model model){
		nodeId = 0;
		SortedMap<String, SortedSet<Statement>> resource2Statements = new TreeMap<String, SortedSet<Statement>>();
		
		Statement st;
		SortedSet<Statement> statements;
		Iterator<Statement> it = model.listStatements(statementSelector);
		while(it.hasNext()){
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
			Property predicate;
			RDFNode object;
			for(Statement st : resource2Statements.get(tree.getUserObject())){
				predicate = st.getPredicate();
				object = st.getObject();
				if(!predicateFilter.isRelevantResource(predicate.getURI())){
					continue;
				}
				if(predicateFilters.contains(st.getPredicate().toString())){
					continue;
				}
				if(object.isLiteral()){
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
//					subTree = new QueryTreeImpl<String>(lit.toString());
					subTree.setId(nodeId++);
					subTree.setIsLiteralNode(true);
					tree.addChild(subTree, st.getPredicate().toString());
				} else if(objectFilter.isRelevantResource(object.asResource().getURI())){
					if(tree.getUserObjectPathToRoot().size() < 3 && 
							!tree.getUserObjectPathToRoot().contains(st.getObject().toString())){
						subTree = new QueryTreeImpl<String>(st.getObject().toString());
						subTree.setIsResourceNode(true);
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
