package org.dllearner.kb.sparql;


import java.util.HashMap;
import java.util.SortedSet;
import java.util.Stack;
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
 * Converter from DL-Learner descriptions to a corresponding SPARQL query
 * to get all instances that are described by this description.
 * @author Sebastian Knappe
 *
 */
public class SparqlQueryDescriptionConvertVisitor implements DescriptionVisitor{
	
	//private SparqlEndpoint se = null;
	//private boolean RDFSReasoning = false;
	

	private static Logger logger = Logger.getLogger(ComponentManager.class);

	
	private Stack<String> stack = new Stack<String>();
	
	private String query="";
	
	private int currentObject=0;
	
	public SparqlQueryDescriptionConvertVisitor()
	{
		stack.push("subject");
	}
	
	/*public SparqlQueryDescriptionConvertVisitor(SparqlEndpoint se, boolean RDFSReasoning)
	{
		stack.push("subject");
		this.se = se;
		this.RDFSReasoning = RDFSReasoning;
	}*/
	
	public String getSparqlQuery()
	{
		query="SELECT ?subject\nWHERE {"+query;
		query+="}\n";
		query+="LIMIT 5";
		return query;
	}
	
	public String getSparqlQuery(int limit)
	{	if(limit==0)limit=99999;
		query="SELECT ?subject\nWHERE {"+query;
		query+="}\n";
		query+="LIMIT "+limit;
		return query;
	}
	
	public static String getSparqlSubclassQuery(String description)
	{	String ret = "SELECT ?subject \n";
		ret+= "WHERE {\n";
		ret+=" ?subject <http://www.w3.org/2000/01/rdf-schema#subClassOf>  <"+description+"> \n";
		ret+="}\n";
		
		return ret;
	}
	
	public static String getSparqlQuery(String description) throws ParseException
	{
		Description d = KBParser.parseConcept(description);
		SparqlQueryDescriptionConvertVisitor visitor=new SparqlQueryDescriptionConvertVisitor();
		d.accept(visitor);
		
		//TODO ERROR see replace HACK
		String ret = visitor.getSparqlQuery();
		while (ret.contains("..")) {
			 ret = ret.replace("..", ".");
		}
		return ret;
	}
	
	public static String getSparqlQuery(String description, int limit) throws ParseException
	{	if(limit==0)limit=99999;
		Description d = KBParser.parseConcept(description);
		SparqlQueryDescriptionConvertVisitor visitor=new SparqlQueryDescriptionConvertVisitor();
		d.accept(visitor);
		//TODO ERROR see replace HACK
		String ret = visitor.getSparqlQuery(limit);
		while (ret.contains("..")) {
			 ret = ret.replace("..", ".");
		}
		return ret;
	}
	
	/**
	 * includes subclasses, costly function, because subclasses habe to be recieved first.
	 * @param description
	 * @param limit
	 * @param se
	 * @return
	 * @throws ParseException
	 */
	/*public static String getSparqlQueryIncludingSubclasses(String description, int limit, SparqlEndpoint se) throws ParseException
	{	if(limit==0)limit=99999;
		Description d = KBParser.parseConcept(description);
		SparqlQueryDescriptionConvertVisitor visitor=new SparqlQueryDescriptionConvertVisitor(se, true);
		d.accept(visitor);
		return visitor.getSparqlQuery(limit);
	}*/
	
	public static String getSparqlQuery(Description description)
	{
		SparqlQueryDescriptionConvertVisitor visitor=new SparqlQueryDescriptionConvertVisitor();
		description.accept(visitor);
		return visitor.getSparqlQuery();
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
				result.put(kbsyntax,SparqlQueryDescriptionConvertVisitor.getSparqlQuery(kbsyntax));
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
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectAllRestriction)
	 */
	public void visit(ObjectAllRestriction description) {
		logger.trace("ObjectAllRestriction");		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectSomeRestriction)
	 */
	public void visit(ObjectSomeRestriction description) {
		logger.trace("ObjectSomeRestriction");
		query+="?"+stack.peek()+" <"+description.getRole()+"> ?object"+currentObject+".";
		stack.push("object"+currentObject);
		currentObject++;
		description.getChild(0).accept(this);
		stack.pop();
		logger.trace(description.getRole().toString());
		logger.trace(description.getChild(0).toString());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Nothing)
	 */
	public void visit(Nothing description) {
		logger.trace("Nothing");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Thing)
	 */
	public void visit(Thing description) {
		logger.trace("Thing");
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Intersection)
	 */
	public void visit(Intersection description) {
		//TODO ERROR see replace HACK
		
		logger.trace("Intersection");
		description.getChild(0).accept(this);
		query+=".";
		description.getChild(1).accept(this);
		query+=".";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Union)
	 */
	public void visit(Union description) {
		logger.trace("Union");
		query+="{";
		description.getChild(0).accept(this);
		query+="} UNION {";
		description.getChild(1).accept(this);
		query+="}";
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
		query+="?"+stack.peek()+" a <"+description.getName()+">";
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
