package org.dllearner.kb.sparql;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.gp.ADC;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.owl.DatatypeExactCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMaxCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMinCardinalityRestriction;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.DatatypeValueRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DescriptionVisitor;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectExactCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
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
	
	private SPARQLTasks tasks;
	
	public NaturalLanguageDescriptionConvertVisitor()
	{
		//stack.push("subject");
		tasks=new SPARQLTasks(new Cache("cache"),SparqlEndpoint.getEndpointDBpedia());
	}
	
	private String getDescription()
	{	// for old function see below
		// it was using the object attribute in a strange way
		// QUALITY: what if this function is called several times?? should be private maybe?
		String tmpQuery=""+query;
		
		query = tmpQuery;
		return query;
	}
	
	public static String getNaturalLanguageDescription(Description description)
	{
		NaturalLanguageDescriptionConvertVisitor visitor=new NaturalLanguageDescriptionConvertVisitor();
		description.accept(visitor);
		String ret = visitor.getDescription();
		return ret;
	}
	
	public static String getNaturalLanguageDescription(String descriptionKBSyntax) throws ParseException
	{	
		Description d = KBParser.parseConcept(descriptionKBSyntax);
		NaturalLanguageDescriptionConvertVisitor visitor=new NaturalLanguageDescriptionConvertVisitor();
		d.accept(visitor);
		String ret = visitor.getDescription();
		return ret;
	}
	
	/**
	 * Used for testing the Sparql Query converter.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SortedSet<String> s = new TreeSet<String>();
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
				result.put(kbsyntax,NaturalLanguageDescriptionConvertVisitor.getNaturalLanguageDescription(kbsyntax));
			}
			System.out.println("************************");
			for (String string : result.keySet()) {
				System.out.println("KBSyntayString: "+string);
				System.out.println("Query:\n"+result.get(string));
				System.out.println("************************");
			}
			System.out.println("Finished");
		} catch (ParseException e) {
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
		SortedSet<String> label=tasks.queryAsSet("SELECT ?label WHERE {<"+description.getRole().toString()+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label}", "label");
		if (label.size()>0) query+="all "+label.first()+" are ";
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
		SortedSet<String> label=tasks.queryAsSet("SELECT ?label WHERE {<"+description.getRole().toString()+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label}", "label");
		if (label.size()>0) query+="has "+label.first()+" which is ";
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
		query+="nothing";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Thing)
	 */
	public void visit(Thing description) {
		logger.trace("Thing");
		query+="anything";
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
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectExactCardinalityRestriction)
	 */
	public void visit(ObjectExactCardinalityRestriction description) {
		logger.trace("ObjectExactCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectMaxCardinalityRestriction)
	 */
	public void visit(ObjectMaxCardinalityRestriction description) {
		logger.trace("ObjectMaxCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectValueRestriction)
	 */
	public void visit(ObjectValueRestriction description) {
		logger.trace("ObjectValueRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeValueRestriction)
	 */
	public void visit(DatatypeValueRestriction description) {
		logger.trace("DatatypeValueRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.NamedClass)
	 */
	public void visit(NamedClass description) {
		
		logger.trace("NamedClass");
		SortedSet<String> label=tasks.queryAsSet("SELECT ?label WHERE {<"+description.getName()+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label}", "label");
		query+="a "+label.first();
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
}
