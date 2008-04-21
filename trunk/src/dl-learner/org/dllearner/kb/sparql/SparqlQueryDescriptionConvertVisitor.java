package org.dllearner.kb.sparql;


import java.util.HashMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.dllearner.algorithms.gp.ADC;
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

	public static boolean debug_flag=false;
	
	private Stack<String> stack = new Stack<String>();
	
	private String query="";
	
	private int currentObject=0;
	
	public SparqlQueryDescriptionConvertVisitor()
	{
		stack.push("subject");
	}
	
	public String getSparqlQuery()
	{
		query="SELECT ?subject\nWHERE {"+query;
		query+="}\n";
		query+="LIMIT 5";
		return query;
	}
	
	public static String getSparqlQuery(String description) throws ParseException
	{
		Description d = KBParser.parseConcept(description);
		SparqlQueryDescriptionConvertVisitor visitor=new SparqlQueryDescriptionConvertVisitor();
		d.accept(visitor);
		return visitor.getSparqlQuery();
	}
	
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
		print("Negation");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectAllRestriction)
	 */
	public void visit(ObjectAllRestriction description) {
		print("ObjectAllRestriction");		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectSomeRestriction)
	 */
	public void visit(ObjectSomeRestriction description) {
		print("ObjectSomeRestriction");
		query+="?"+stack.peek()+" <"+description.getRole()+"> ?object"+currentObject+".";
		stack.push("object"+currentObject);
		currentObject++;
		description.getChild(0).accept(this);
		stack.pop();
		print(description.getRole().toString());
		print(description.getChild(0).toString());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Nothing)
	 */
	public void visit(Nothing description) {
		print("Nothing");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Thing)
	 */
	public void visit(Thing description) {
		print("Thing");
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Intersection)
	 */
	public void visit(Intersection description) {
		print("Intersection");
		description.getChild(0).accept(this);
		query+=".";
		description.getChild(1).accept(this);
		query+=".";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Union)
	 */
	public void visit(Union description) {
		print("Union");
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
		print("ObjectMinCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectExactCardinalityRestriction)
	 */
	public void visit(ObjectExactCardinalityRestriction description) {
		print("ObjectExactCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectMaxCardinalityRestriction)
	 */
	public void visit(ObjectMaxCardinalityRestriction description) {
		print("ObjectMaxCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectValueRestriction)
	 */
	public void visit(ObjectValueRestriction description) {
		print("ObjectValueRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeValueRestriction)
	 */
	public void visit(DatatypeValueRestriction description) {
		print("DatatypeValueRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.NamedClass)
	 */
	public void visit(NamedClass description) {
		print("NamedClass");
		query+="?"+stack.peek()+" a <"+description.getName()+">";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.algorithms.gp.ADC)
	 */
	public void visit(ADC description) {
		print("ADC");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMinCardinalityRestriction)
	 */
	public void visit(DatatypeMinCardinalityRestriction description) {
		print("DatatypeMinCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeExactCardinalityRestriction)
	 */
	public void visit(DatatypeExactCardinalityRestriction description) {
		print("DatatypeExactCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMaxCardinalityRestriction)
	 */
	public void visit(DatatypeMaxCardinalityRestriction description) {
		print("DatatypeMaxCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeSomeRestriction)
	 */
	public void visit(DatatypeSomeRestriction description) {
		print("DatatypeSomeRestriction");
	}
	
	/**
	 * TODO should be replaced by logger.debug or sth like that
	 * @param str
	 */
	public void print(String str){ 
		if(debug_flag)System.out.println(str);
	}
	
	
}
