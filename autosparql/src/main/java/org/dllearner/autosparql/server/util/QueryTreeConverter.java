package org.dllearner.autosparql.server.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

public class QueryTreeConverter {
	
	private static int cnt;
	
	public static String getSPARQLQuery(QueryTree tree, String baseURI, Map<String, String> prefixes){
		if(tree.getChildCount() == 0){
    		return "SELECT ?x0 WHERE {?x0 ?y ?z.}";
    	}
    	cnt = 0;
    	Map<String, String> usedPrefixes = new HashMap<String, String>();
    	StringBuilder sb = new StringBuilder();
    	sb.append("SELECT ?x0 WHERE {\n");
    	buildSPARQLQueryString(tree, sb, prefixes, usedPrefixes);
    	sb.append("}");
		
		return sb.toString();
	}
	
	private static void buildSPARQLQueryString(QueryTree tree, StringBuilder sb, 
			Map<String, String> prefixes, Map<String, String> usedPrefixes){
		String subject = (String) tree.getUserObject();
    	if(tree.getUserObject().equals("?")){
    		subject = "?x" + cnt++;
    	} else {
    		boolean replaced = false;
			for(Entry<String, String> entry : prefixes.entrySet()){
				if(subject.startsWith(entry.getValue())){
					replaced = true;
					subject = entry.getKey() + ":" + subject.replace(entry.getValue(), "");
					usedPrefixes.put(entry.getKey(), entry.getValue());
					break;
				} 
			}
			if(!replaced){
				subject = "<" + subject + ">";
			}
    	}
    	String predicate;
    	String object;
    	if(!tree.isLeaf()){
    		for(Object child : tree.getChildren()){
        		predicate = (String) tree.getEdge((QueryTree) child);
        		
        		boolean replaced = false;
        		for(Entry<String, String> entry : prefixes.entrySet()){
        			if(predicate.startsWith(entry.getValue())){
        				replaced = true;
        				predicate = entry.getKey() + ":" + predicate.replace(entry.getValue(), "");
        				usedPrefixes.put(entry.getKey(), entry.getValue());
        				break;
        			} 
        		}
        		if(!replaced){
        			predicate = "<" + predicate + ">";
        		}
        		
        		object = (String) ((QueryTree) child).getUserObject();
        		boolean objectIsResource = !object.equals("?");
        		if(!objectIsResource){
        			object = "?x" + cnt;
        		} else if(((String)object).startsWith("http://")){
        			replaced = false;
        			for(Entry<String, String> entry : prefixes.entrySet()){
        				if(object.startsWith(entry.getValue())){
        					replaced = true;
        					object = entry.getKey() + ":" + object.replace(entry.getValue(), "");
        					usedPrefixes.put(entry.getKey(), entry.getValue());
        					break;
        				} 
        			}
        			if(!replaced){
        					object = "<" + object + ">";
        			}
        			
        		}
        		sb.append(subject).append(" ").append(predicate).append(" ").append(object).append(".\n");
        		if(!objectIsResource){
        			buildSPARQLQueryString((QueryTree) child, sb, prefixes, usedPrefixes);
        		}
        	}
    	} 
    }

}
