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
	
	public NaturalLanguageDescriptionConvertVisitor(String endpoint)
	{
		//stack.push("subject");
		tasks=new SPARQLTasks(new Cache("cache"),SparqlEndpoint.getEndpointByName(endpoint));
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
		NaturalLanguageDescriptionConvertVisitor visitor=new NaturalLanguageDescriptionConvertVisitor("DBpedia");
		description.accept(visitor);
		String ret = visitor.getDescription();
		return ret;
	}
	
	public static String getNaturalLanguageDescription(String descriptionKBSyntax, String endpoint) throws ParseException
	{	
		Description d = KBParser.parseConcept(descriptionKBSyntax);
		NaturalLanguageDescriptionConvertVisitor visitor=new NaturalLanguageDescriptionConvertVisitor(endpoint);
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
				result.put(kbsyntax,NaturalLanguageDescriptionConvertVisitor.getNaturalLanguageDescription(kbsyntax,"DBPEDIA"));
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
		if (description.getName().equals("http://dbpedia.org/class/yago/Entity100001740")) {
			if (query.endsWith("which is ")) query=query.substring(0, query.length()-10);
			if (query.endsWith("are ")) query=query.substring(0, query.length()-5);
			if (query.endsWith("and ")) query=query.substring(0, query.length()-5);
			if (query.endsWith("or ")) query=query.substring(0, query.length()-4);
		}
		SortedSet<String> label=tasks.queryAsSet("SELECT ?label WHERE {<"+description.getName()+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label}", "label");
		String l=label.first();
		String l2=description.getName().substring(description.getName().lastIndexOf("/")+1, description.getName().length()).replace('_', ' ');
		if (l.length()+5<l2.length()&&!l2.matches(".*[0-9]")) l=l2;
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
}
