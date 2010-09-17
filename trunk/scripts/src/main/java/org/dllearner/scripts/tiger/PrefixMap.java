package org.dllearner.scripts.tiger;

import java.util.HashMap;
import java.util.Map;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;


public class PrefixMap {

	static String prefix = "http://nlp2rdf.org/ontology/";
	static Map<String,String> m = getPrefixMap();
	
	private static Map<String,String> getPrefixMap(){
		Map<String,String> m = new HashMap<String, String>();
		m.put("stts", "http://nachhalt.sfb632.uni-potsdam.de/owl/stts.owl#");
		m.put("tiger", "http://nachhalt.sfb632.uni-potsdam.de/owl/tiger-syntax.owl#");
		return m;
	}
	
	public static String toKBSyntaxString(EvaluatedDescription d){
		return toKBSyntaxString(d.getDescription());
	}
	
	public static String toKBSyntaxString(Description d){
		return d.toKBSyntaxString(prefix, m);
	}
	
	public static String toManchesterSyntaxString(Description d){
		return d.toManchesterSyntaxString(prefix, m);
	}
	
	public static String toManchesterSyntaxString(EvaluatedDescription d){
		return toManchesterSyntaxString(d.getDescription());
	}
	
	
}
