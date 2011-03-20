package org.dllearner.algorithm.qtl.util;

import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.algorithm.qtl.datastructures.QueryTree;

public class TreeHelper {

	public static String getAbbreviatedTreeRepresentation(QueryTree tree,
			String baseURI, Map<String, String> prefixes) {
		String treeString = tree.getStringRepresentation();
		if (baseURI != null) {
			treeString = treeString.replace(baseURI, "");
		} 
		if(prefixes != null){
			for (Entry<String, String> prefix : prefixes.entrySet()) {
				treeString = treeString.replace(prefix.getValue(), prefix.getKey()+ ":");
			}
		}
		return treeString;
	}
}
