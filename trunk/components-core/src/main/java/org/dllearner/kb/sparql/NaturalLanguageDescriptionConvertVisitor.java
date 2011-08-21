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

package org.dllearner.kb.sparql;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.gp.ADC;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DatatypeExactCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMaxCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMinCardinalityRestriction;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.DatatypeValueRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DescriptionVisitor;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectExactCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectOneOf;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;

/**
 * Converter from DL-Learner descriptions to a corresponding natural
 * language description.
 * 
 * @author Sebastian Knappe
 * 
 *
 */
public class NaturalLanguageDescriptionConvertVisitor implements DescriptionVisitor{
	
	private static Logger logger = Logger.getLogger(ComponentManager.class);

	private String query="";
	
	private AbstractReasonerComponent service;
	
	public NaturalLanguageDescriptionConvertVisitor(AbstractReasonerComponent service)
	{
		//stack.push("subject");
		this.service=service;
	}
	
	private String getDescription()
	{	// for old function see below
		// it was using the object attribute in a strange way
		// QUALITY: what if this function is called several times?? should be private maybe?
		String tmpQuery=""+query;
		
		query = tmpQuery;
		return query;
	}
	
	public static String getNaturalLanguageDescription(Description description, AbstractReasonerComponent service)
	{
		NaturalLanguageDescriptionConvertVisitor visitor=new NaturalLanguageDescriptionConvertVisitor(service);
		description.accept(visitor);
		String ret = visitor.getDescription();
		return ret;
	}
	
	public static String getNaturalLanguageDescription(String descriptionKBSyntax, AbstractReasonerComponent service) throws ParseException
	{	
		Description d = KBParser.parseConcept(descriptionKBSyntax);
		NaturalLanguageDescriptionConvertVisitor visitor=new NaturalLanguageDescriptionConvertVisitor(service);
		d.accept(visitor);
		String ret = visitor.getDescription();
		return ret;
	}
	
	private String getLabelFromReasoner(Entity ent)
	{
		String label;
//		try{
			Set<Constant> set=service.getLabel(ent);
			if (set.size()>0){
				Iterator<Constant> iter=set.iterator();
				label=iter.next().getLiteral();
			}
			else label="";
//		}
//		catch (ReasoningMethodUnsupportedException e)
//		{
//			label="";
//		}
		
		return label;
	}
	
	/**
	 * Used for testing the Sparql Query converter.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			/*SortedSet<String> s = new TreeSet<String>();
			HashMap<String,String> result = new HashMap<String,String>();
			String conj="(\"http://dbpedia.org/class/yago/Person100007846\" AND \"http://dbpedia.org/class/yago/Head110162991\")";
			s.add("EXISTS \"http://dbpedia.org/property/disambiguates\".TOP");
			s.add("EXISTS \"http://dbpedia.org/property/successor\".\"http://dbpedia.org/class/yago/Person100007846\"");
			s.add("EXISTS \"http://dbpedia.org/property/successor\"."+conj);
			s.add("ALL \"http://dbpedia.org/property/disambiguates\".TOP");
			s.add("ALL \"http://dbpedia.org/property/successor\".\"http://dbpedia.org/class/yago/Person100007846\"");
			s.add("\"http://dbpedia.org/class/yago/Person100007846\"");
			s.add(conj);
			s.add("(\"http://dbpedia.org/class/yago/Person100007846\" OR \"http://dbpedia.org/class/yago/Head110162991\")");
			s.add("NOT \"http://dbpedia.org/class/yago/Person100007846\"");
			s.add("(\"http://dbpedia.org/class/yago/HeadOfState110164747\" AND (\"http://dbpedia.org/class/yago/Negotiator110351874\" AND \"http://dbpedia.org/class/yago/Representative110522035\"))");
			for (String kbsyntax : s) {
				result.put(kbsyntax,NaturalLanguageDescriptionConvertVisitor.getNaturalLanguageDescription(kbsyntax,"DBPEDIA"));
			}
			System.out.println("************************");
			for (String string : result.keySet()) {
				System.out.println("KBSyntayString: "+string);
				System.out.println("Query:\n"+result.get(string));
				System.out.println("************************");
			}
			System.out.println("Finished");*/
			//String conj="EXISTS \"http://xmlns.com/foaf/0.1/page\".<= 0 \"http://www.w3.org/2004/02/skos/core#subject\".TOP";
			//String conj="(\"Male\" AND (\"hasDog\" = 18))";
//			ObjectValueRestriction rest=new ObjectValueRestriction(new ObjectProperty("hasAge"),new Individual("18"));
			//System.out.println(NaturalLanguageDescriptionConvertVisitor.getNaturalLanguageDescription(rest));
		} catch (/*Parse*/Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Negation)
	 */
	public void visit(Negation description) {
		logger.trace("Negation");
		query+="not ";
		description.getChild(0).accept(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectAllRestriction)
	 */
	public void visit(ObjectAllRestriction description) {
		logger.trace("ObjectAllRestriction");
		String label=getLabelFromReasoner((ObjectProperty)description.getRole());
		if (label.length()>0) query+="all "+label+" are ";
		else query+="all "+description.getRole().toString().substring(description.getRole().toString().lastIndexOf("/")+1)+" are ";
		description.getChild(0).accept(this);
		logger.trace(description.getRole().toString());
		logger.trace(description.getChild(0).toString());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectSomeRestriction)
	 */
	public void visit(ObjectSomeRestriction description) {
		logger.trace("ObjectSomeRestriction");
		String label=getLabelFromReasoner((ObjectProperty)description.getRole());
		if (label.length()>0) query+="has "+label+" which is ";
		else query+="has "+description.getRole().toString().substring(description.getRole().toString().lastIndexOf("/")+1)+" which is ";
		description.getChild(0).accept(this);
		logger.trace(description.getRole().toString());
		logger.trace(description.getChild(0).toString());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Nothing)
	 */
	public void visit(Nothing description) {
		logger.trace("Nothing");
		if (query.endsWith("which is ")) query=query.substring(0, query.length()-10);
		if (query.endsWith("are ")) query=query.substring(0, query.length()-5);
		if (query.endsWith("and ")) query=query.substring(0, query.length()-5);
		if (query.endsWith("or ")) query=query.substring(0, query.length()-4);
		//query+="nothing";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Thing)
	 */
	public void visit(Thing description) {
		logger.trace("Thing");
		if (query.endsWith("which is ")) query=query.substring(0, query.length()-10);
		if (query.endsWith("are ")) query=query.substring(0, query.length()-5);
		if (query.endsWith("and ")) query=query.substring(0, query.length()-5);
		if (query.endsWith("or ")) query=query.substring(0, query.length()-4);
		//query+="anything";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Intersection)
	 */
	public void visit(Intersection description) {
		// HACK see replace hacks in other functions
		logger.trace("Intersection");
		description.getChild(0).accept(this);
		query+=" and ";
		description.getChild(1).accept(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Union)
	 */
	public void visit(Union description) {
		// HACK see replace hacks in other functions
		logger.trace("Union");
		description.getChild(0).accept(this);
		query+=" or ";
		description.getChild(1).accept(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectMinCardinalityRestriction)
	 */
	public void visit(ObjectMinCardinalityRestriction description) {
		logger.trace("ObjectMinCardinalityRestriction");
		if (query.endsWith("which is ")) query=query.substring(0, query.length()-3)+"has ";
		query+="at least "+description.getCardinality()+" "+description.getRole().toString()+" which is ";
		description.getChild(0).accept(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectExactCardinalityRestriction)
	 */
	public void visit(ObjectExactCardinalityRestriction description) {
		logger.trace("ObjectExactCardinalityRestriction");
		if (query.endsWith("which is ")) query=query.substring(0, query.length()-3)+"has ";
		query+="exactly "+description.getCardinality()+" "+description.getRole().toString()+" which is ";
		description.getChild(0).accept(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectMaxCardinalityRestriction)
	 */
	public void visit(ObjectMaxCardinalityRestriction description) {
		logger.trace("ObjectMaxCardinalityRestriction");
		if (query.endsWith("which is ")) query=query.substring(0, query.length()-3)+"has ";
		query+="at most "+description.getCardinality()+" "+description.getRole().toString()+" which is ";
		description.getChild(0).accept(this);		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectValueRestriction)
	 */
	public void visit(ObjectValueRestriction description) {
		ObjectProperty op = (ObjectProperty) description.getRestrictedPropertyExpression();
		Individual ind = description.getIndividual();
		String label=getLabelFromReasoner(ind);
		String indLabel;
		if (label.length()>0)
			indLabel =label;
		else 
			indLabel =ind.getName();
		label=getLabelFromReasoner(op);
		String propLabel;
		if (label.length()>0)
			propLabel =label;
		else 
			propLabel =op.getName();		
		query += propLabel + " is " + indLabel;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeValueRestriction)
	 */
	public void visit(DatatypeValueRestriction description) {
		logger.trace("DatatypeValueRestriction");
		//if (query.endsWith("which is ")) query=query.substring(0, query.length()-3)+"has ";
		query+=description.getRestrictedPropertyExpression().toString()+" has the value "+description.getValue();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.NamedClass)
	 */
	public void visit(NamedClass description) {
		
		logger.trace("NamedClass");
		if (description.getName().equals("http://dbpedia.org/class/yago/Entity100001740")) {
			if (query.endsWith("which is ")) query=query.substring(0, query.length()-10);
			if (query.endsWith("are ")) query=query.substring(0, query.length()-5);
			if (query.endsWith("and ")) query=query.substring(0, query.length()-5);
			if (query.endsWith("or ")) query=query.substring(0, query.length()-4);
		}
		//SortedSet<String> label=tasks.queryAsSet("SELECT ?label WHERE {<"+description.getName()+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label}", "label");
		String l=getLabelFromReasoner(description);
		String l2=description.getName().substring(description.getName().lastIndexOf("/")+1, description.getName().length()).replace('_', ' ');
		if ((l.length()==0)||(l.length()+5<l2.length()&&!l2.matches(".*[0-9]"))) l=l2;
		
		//replacements
		l=l.replaceAll("Cities", "City");
		l=l.replaceAll("Players", "Player");
		
		l=l.replaceAll("([^-\040])([A-Z])([^A-Z])", "$1 $2$3");
				
		if (l.toLowerCase().startsWith("a")||l.toLowerCase().startsWith("e")||l.toLowerCase().startsWith("i")||l.toLowerCase().startsWith("o")||l.toLowerCase().startsWith("u")) query+="an "+l;
		else query+="a "+l;
	}
	
	

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.algorithms.gp.ADC)
	 */
	public void visit(ADC description) {
		logger.trace("ADC");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMinCardinalityRestriction)
	 */
	public void visit(DatatypeMinCardinalityRestriction description) {
		logger.trace("DatatypeMinCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeExactCardinalityRestriction)
	 */
	public void visit(DatatypeExactCardinalityRestriction description) {
		logger.trace("DatatypeExactCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMaxCardinalityRestriction)
	 */
	public void visit(DatatypeMaxCardinalityRestriction description) {
		logger.trace("DatatypeMaxCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeSomeRestriction)
	 */
	public void visit(DatatypeSomeRestriction description) {
		logger.trace("DatatypeSomeRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectOneOf)
	 */
	@Override
	public void visit(ObjectOneOf description) {
		logger.trace("ObjectOneOf");
	}
}
