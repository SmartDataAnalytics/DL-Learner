/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.kb.manipulator;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.extraction.Node;
import org.dllearner.kb.manipulator.Rule.Months;
import org.dllearner.kb.manipulator.TypeFilterRule.Nodes;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

import com.jamonapi.Monitor;

/**
 * Used to manipulate retrieved tupels, identify blanknodes, etc.
 * 
 * @author Sebastian Hellmann
 * 
 */
public class Manipulator {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Manipulator.class);
	private List<Rule> rules = new ArrayList<Rule>();
	
	private Manipulator() {
	}

	public Manipulator(List<Rule> rules) {
		for (Rule rule : rules) {
			addRule(rule);
		}
	}

	/**
	 * this checks for consistency and manipulates the tuples, before they get
	 * triple
	 */
	public SortedSet<RDFNodeTuple> manipulate( Node node, SortedSet<RDFNodeTuple> tuples) {
		Monitor m = JamonMonitorLogger.getTimeMonitor(Manipulator.class, "Time for Rules").start();
		//logger.warn("before: "+tuples.size());
		for (Rule rule : rules) {
			tuples = rule.applyRule(node, tuples);
		}
		//logger.warn("after: "+tuples.size());
		m.stop();
		return tuples;
	}
	
	
	
	public static Manipulator getManipulatorByName(String predefinedManipulator)
	{   if (predefinedManipulator == null) {
			return getDefaultManipulator();
		}else if (predefinedManipulator.equalsIgnoreCase("DBPEDIA-NAVIGATOR")) {
			return getDBpediaNavigatorManipulator();

		} else if(predefinedManipulator.equalsIgnoreCase("DEFAULT")
				||predefinedManipulator.equalsIgnoreCase("STANDARD")){
			return getDefaultManipulator();
		} 
		else {
			//QUALITY maybe not the best, 
			return getDefaultManipulator();
		}
	}
	
	public static Manipulator getDBpediaNavigatorManipulator(){
		Manipulator m =  new Manipulator();
		//m.addRule(new DBPediaNavigatorCityLocatorRule(Months.JANUARY));
		//m.addRule(new DBpediaNavigatorOtherRule(Months.DECEMBER));
		m.addRule(new DBpediaNavigatorFilterRule(Months.JANUARY));
		return m;
	}
	
	public static Manipulator getDefaultManipulator(){
		Manipulator m =  new Manipulator();
		m.addDefaultRules(Months.DECEMBER);
		return m;
	}
	
			//
//			if(t.a.equals("http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag")) {
//				//hackGetLabel(t.b);
//				
//			}
			
			// GovTrack hack
			// => we convert a string literal to a URI
			// => : introduce an option for converting literals for certain
			// properties into URIs
//			String sp = "http://purl.org/dc/elements/1.1/subject";
//			if(t.a.equals(sp)) {
//				System.out.println(t);
//				System.exit(0);
//			}
			

	private void addDefaultRules(Months month){
		
	//	addRule(new TypeFilterRule(month, OWLVocabulary.RDF_TYPE, OWLVocabulary.OWL_CLASS,ClassNode.class.getCanonicalName() )) ;
	//	addRule(new TypeFilterRule(month, OWLVocabulary.RDF_TYPE, OWLVocabulary.OWL_THING,InstanceNode.class.getCanonicalName() )) ;
	//	addRule(new TypeFilterRule(month, "", OWLVocabulary.OWL_CLASS, ClassNode.class.getCanonicalName()) ) ;
		
		addRule(new TypeFilterRule(month, OWLVocabulary.RDF_TYPE, OWLVocabulary.OWL_THING, Nodes.INSTANCENODE )) ;
		
		addRule(new TypeFilterRule(month, OWLVocabulary.RDF_TYPE, OWLVocabulary.OWL_CLASS, Nodes.CLASSNODE)) ;
		addRule(new TypeFilterRule(month, "", OWLVocabulary.OWL_CLASS, Nodes.CLASSNODE) ) ;
		addRule(new TypeFilterRule(month, "", OWLVocabulary.RDFS_CLASS, Nodes.CLASSNODE) ) ;
	
	}
	
	public synchronized void addRule(Rule newRule){
		rules.add(newRule);
		List<Rule> l = new ArrayList<Rule>();
		
		for (Months month : Rule.MONTHS) {
			for (Rule rule : rules) {
				if(rule.month.equals(month)) {
					l.add(rule);
				}
			}
			
		}
		rules = l;
	}
	
	
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

}
