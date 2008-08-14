/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb.manipulator;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.dllearner.kb.extraction.ClassNode;
import org.dllearner.kb.extraction.InstanceNode;
import org.dllearner.kb.extraction.Node;
import org.dllearner.kb.manipulator.Rule.Months;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

/**
 * Used to manipulate retrieved tupels, identify blanknodes, etc.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class Manipulator {
	
	List<Rule> rules = new ArrayList<Rule>();
	//List<ReplacementRule> replacementRules = new ArrayList<ReplacementRule>();


	//public int breakSuperClassRetrievalAfter = 200;
	//public LinkedList<StringTuple> replacePredicate;
	//public LinkedList<StringTuple> replaceObject;

	// Set<String> classproperties;

	private Manipulator() {
		
		//this.replaceObject = replaceObject;
		//this.replacePredicate = replacePredicate;
		//this.breakSuperClassRetrievalAfter = breakSuperClassRetrievalAfter;
		// Set<String> classproperties = new HashSet<String>();
		// classproperties.add(subclass);

	}

	/**
	 * this checks for consistency and manipulates the tuples, before they get
	 * triple
	 */
	public SortedSet<RDFNodeTuple> manipulate( Node node, SortedSet<RDFNodeTuple> tuples) {
		
		for (Months month : Rule.MONTHS) {
			tuples = applyRulesOfTheMonth(month, node, tuples);
		}
		return tuples;
		/*SortedSet<RDFNodeTuple> keep = new TreeSet<RDFNodeTuple>();
		
		for (RDFNodeTuple currentTuple : tuples) {
			currentTuple = manipulateTuple(node.getURI().toString(), currentTuple);
			if(keepTuple(node, currentTuple)) {
				keep.add(currentTuple);
			}
			
		}
		return keep;*/
	}
	
	public SortedSet<RDFNodeTuple> applyRulesOfTheMonth(Months month, Node subject, SortedSet<RDFNodeTuple> tuples){
		for (Rule rule : rules) {
			if(rule.month.equals(month)) {
				tuples = rule.applyRule(subject, tuples);
			}
		}
		return tuples;
	}
	
	public static Manipulator getManipulatorByName(String predefinedManipulator)
	{
		if (predefinedManipulator.equalsIgnoreCase("DBPEDIA-NAVIGATOR")) {
			return getDBpediaNavigatorManipulator();
//			return new DBpediaNavigatorManipulator(blankNodeIdentifier,
			//breakSuperClassRetrievalAfter, replacePredicate, replaceObject);
	
		} else if(predefinedManipulator.equalsIgnoreCase("DEFAULT")){
			return getDefaultManipulator();
		}
		else {
			//QUALITY maybe not the best, should be Default
			return new Manipulator();
		}
	}
	
	public static Manipulator getDBpediaNavigatorManipulator(){
		Manipulator m =  new Manipulator();
		return m;
	}
	
	public static Manipulator getDefaultManipulator(){
		Manipulator m =  new Manipulator();
		m.addDefaultRules();
		return m;
	}
	
			//HACK
//			if(t.a.equals("http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag")) {
//				//hackGetLabel(t.b);
//				
//			}
			
			// GovTrack hack
			// => we convert a string literal to a URI
			// => TODO: introduce an option for converting literals for certain
			// properties into URIs
//			String sp = "http://purl.org/dc/elements/1.1/subject";
//			if(t.a.equals(sp)) {
//				System.out.println(t);
//				System.exit(0);
//			}
			

	private void addDefaultRules(){
		
		rules.add(new TypeFilterRule(Months.DECEMBER, OWLVocabulary.RDF_TYPE, OWLVocabulary.OWL_CLASS,ClassNode.class.getCanonicalName() )) ;
		rules.add(new TypeFilterRule(Months.DECEMBER,OWLVocabulary.RDF_TYPE, OWLVocabulary.OWL_THING,InstanceNode.class.getCanonicalName() )) ;
		rules.add(new TypeFilterRule(Months.DECEMBER,"", OWLVocabulary.OWL_CLASS, ClassNode.class.getCanonicalName()) ) ;
	}
	
	
	
	/*
	private RDFNodeTuple manipulateTuple(String subject, RDFNodeTuple tuple) {
		
		for (int i = 0; i < replacementRules.size(); i++) {
			ReplacementRule replace = replacementRules.get(i);
			tuple = replace.applyRule(subject, tuple);
		}
		return tuple;
	}*/
	
	/*private String hackGetLabel(String resname){
		String query="" +
				"SELECT ?o \n" +
				"WHERE { \n" +
				"<"+resname+"> "+ " <http://www.holygoat.co.uk/owl/redwood/0.1/tags/tagName> ?o " +
						"}";
		
		System.out.println(query);
		//http://dbtune.org/musicbrainz/sparql?query=
			//SELECT ?o WHERE { <http://dbtune.org/musicbrainz/resource/tag/1391>  <http://www.holygoat.co.uk/owl/redwood/0.1/tags/tagName> ?o }
		SparqlQuery s=new SparqlQuery(query,SparqlEndpoint.EndpointMusicbrainz());
		ResultSet rs=s.send();
		while (rs.hasNext()){
			rs.nextBinding();
		}
		//System.out.println("AAA"+s.getAsXMLString(s.send()) );
		return "";
	}*/
	
	/*private void replacePredicate(StringTuple t) {
	for (StringTuple rep : replacePredicate) {
		if (rep.a.equals(t.a)) {
			t.a = rep.b;
		}
	}
}

private void replaceObject(StringTuple t) {
	for (StringTuple rep : replaceObject) {
		if (rep.a.equals(t.a)) {
			t.a = rep.b;
		}
	}
}*/
	
	

	/*	
		// remove <rdf:type, owl:class>
		// this is done to avoid transformation to owl:subclassof
		if (t.a.equals(type) && t.b.equals(classns)
				&& node instanceof ClassNode) {
			toRemove.add(t);
		}

		// all with type class
		if (t.b.equals(classns) && node instanceof ClassNode) {
			toRemove.add(t);
		}

		// remove all instances with owl:type thing
		if (t.a.equals(type) && t.b.equals(thing)
				&& node instanceof InstanceNode) {
			toRemove.add(t);
		}

	}
	tuples.removeAll(toRemove);

	return tuples;
}
*/

}
