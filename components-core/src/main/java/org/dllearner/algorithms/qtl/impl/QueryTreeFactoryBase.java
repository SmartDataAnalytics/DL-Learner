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
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class QueryTreeFactoryBase implements QueryTreeFactory {
	
	private int nodeId;
	private Comparator<Statement> comparator = new StatementComparator();
	
	private int maxDepth = 3;
	
	private Set<String> allowedNamespaces = Sets.newHashSet(RDF.getURI());
	private Set<String> ignoredProperties = Sets.newHashSet(OWL.sameAs.getURI());
	
	public QueryTreeFactoryBase(){
	}
	
	/**
	 * @param maxDepth the maximum depth of the generated query trees.
	 */
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	
	public RDFResourceTree getQueryTree(String example, Model model) {
		return createTreeOptimized(model.getResource(example), model);
	}

	public RDFResourceTree getQueryTree(Resource resource, Model model) {
		return createTreeOptimized(resource, model);
	}
	
	private RDFResourceTree createTreeOptimized(Resource resource, Model model){
		nodeId = 0;
		SortedMap<Resource, SortedSet<Statement>> resource2Statements = new TreeMap<Resource, SortedSet<Statement>>();
		
		fillMap(resource, model, resource2Statements);	
		
		RDFResourceTree tree = new RDFResourceTree("?", NodeType.VARIABLE);
		int depth = 0;
		fillTree(resource, tree, resource2Statements, depth);
				
		return tree;
	}
	
	private void fillMap(Resource s, Model model, SortedMap<Resource, SortedSet<Statement>> resource2Statements){
		
		// get all statements with subject s
		Iterator<Statement> it = model.listStatements(s, null, (RDFNode)null);
		
		SortedSet<Statement> statements = resource2Statements.get(s);
		if(statements == null){
			statements = new TreeSet<Statement>(comparator);
			resource2Statements.put(s, statements);
		}
		
		while(it.hasNext()){
			Statement st = it.next();
			
			statements.add(st);
			if((st.getObject().isResource()) && !resource2Statements.containsKey(st.getObject().toString())){
				fillMap(st.getObject().asResource(), model, resource2Statements);
			}
		}
	}
	
	private void fillTree(String root, RDFResourceTree tree, SortedMap<String, SortedSet<Statement>> resource2Statements, int depth){
		depth++;
			if(resource2Statements.containsKey(root)){
				RDFResourceTree subTree;
				Property predicate;
				RDFNode object;
				for(Statement st : resource2Statements.get(root)){
					predicate = st.getPredicate();
					object = st.getObject();
					
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
						subTree = new RDFResourceTree(sb.toString(), NodeType.LITERAL);
//						subTree = new RDFResourceTree(lit.toString());
						subTree.setId(nodeId++);
						if(lit.getDatatype() == XSDDatatype.XSDinteger 
								|| lit.getDatatype() == XSDDatatype.XSDdouble 
								|| lit.getDatatype() == XSDDatatype.XSDdate
								|| lit.getDatatype() == XSDDatatype.XSDint
								|| lit.getDatatype() == XSDDatatype.XSDdecimal){
							subTree.addLiteral(lit);
						} else {
							subTree.addLiteral(lit);
						}
						
						tree.addChild(subTree, st.getPredicate().toString());
					} else if(objectFilter.isRelevantResource(object.asResource().getURI())){
						if(!tree.getUserObjectPathToRoot().contains(st.getObject().toString())){
							subTree = new RDFResourceTree(st.getObject().toString(), NodeType.RESOURCE);
							subTree.setId(nodeId++);
							tree.addChild(subTree, st.getPredicate().toString());
							if(depth < maxDepth){
								fillTree(st.getObject().toString(), subTree, resource2Statements, depth);
							}
							if(object.isAnon()){
								subTree.setIsBlankNode(true);
							}
							
						}
					} else if(object.isAnon()){
						if(depth < maxDepth &&
								!tree.getUserObjectPathToRoot().contains(st.getObject().toString())){
							subTree = new RDFResourceTree(st.getObject().toString(), NodeType.RESOURCE);
							subTree.setIsResourceNode(true);
							subTree.setId(nodeId++);
							tree.addChild(subTree, st.getPredicate().toString());
							fillTree(st.getObject().toString(), subTree, resource2Statements, depth);
						}
					}
				}
			}
		depth--;
	}
	
	class StatementComparator implements Comparator<Statement> {

		@Override
		public int compare(Statement s1, Statement s2) {
			return ComparisonChain
					.start()
					.compare(s1.getPredicate().getURI(),
							s2.getPredicate().getURI())
					.compare(s1.getObject().toString(),
							s2.getObject().toString()).result();
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
	
	public static void main(String[] args) throws Exception {
		QueryTreeFactoryBase factory = new QueryTreeFactoryBase();
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint.getEndpointDBpediaLOD2Cloud());
		String resourceURI = "http://dbpedia.org/resource/Athens";
		Model cbd = cbdGen.getConciseBoundedDescription(resourceURI, 0);
		RDFResourceTree queryTree = factory.getQueryTree(resourceURI, cbd);
		System.out.println(queryTree.toSPARQLQueryString());
	}

}
