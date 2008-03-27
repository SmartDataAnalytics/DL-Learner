package org.dllearner.kb.sparql;


import java.util.Stack;

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
			String d = "EXISTS \"http://dbpedia.org/property/disambiguates\".TOP";
			String query = SparqlQueryDescriptionConvertVisitor.getSparqlQuery(d);
			System.out.println(d);
			System.out.println(query);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Negation)
	 */
	public void visit(Negation description) {
		System.out.println("Negation");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectAllRestriction)
	 */
	public void visit(ObjectAllRestriction description) {
		System.out.println("ObjectAllRestriction");		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectSomeRestriction)
	 */
	public void visit(ObjectSomeRestriction description) {
		System.out.println("ObjectSomeRestriction");
		query+="?"+stack.peek()+" <"+description.getRole()+"> ?object"+currentObject+".";
		stack.push("object"+currentObject);
		currentObject++;
		description.getChild(0).accept(this);
		stack.pop();
		System.out.println(description.getRole());
		System.out.println(description.getChild(0));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Nothing)
	 */
	public void visit(Nothing description) {
		System.out.println("Nothing");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Thing)
	 */
	public void visit(Thing description) {
		System.out.println("Thing");
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Intersection)
	 */
	public void visit(Intersection description) {
		System.out.println("Intersection");
		description.getChild(0).accept(this);
		query+=".";
		description.getChild(1).accept(this);
		query+=".";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.Union)
	 */
	public void visit(Union description) {
		System.out.println("Union");
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
		System.out.println("ObjectMinCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectExactCardinalityRestriction)
	 */
	public void visit(ObjectExactCardinalityRestriction description) {
		System.out.println("ObjectExactCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectMaxCardinalityRestriction)
	 */
	public void visit(ObjectMaxCardinalityRestriction description) {
		System.out.println("ObjectMaxCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.ObjectValueRestriction)
	 */
	public void visit(ObjectValueRestriction description) {
		System.out.println("ObjectValueRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeValueRestriction)
	 */
	public void visit(DatatypeValueRestriction description) {
		System.out.println("DatatypeValueRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.NamedClass)
	 */
	public void visit(NamedClass description) {
		System.out.println("NamedClass");
		query+="?"+stack.peek()+" a <"+description.getName()+">";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.algorithms.gp.ADC)
	 */
	public void visit(ADC description) {
		System.out.println("ADC");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMinCardinalityRestriction)
	 */
	public void visit(DatatypeMinCardinalityRestriction description) {
		System.out.println("DatatypeMinCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeExactCardinalityRestriction)
	 */
	public void visit(DatatypeExactCardinalityRestriction description) {
		System.out.println("DatatypeExactCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeMaxCardinalityRestriction)
	 */
	public void visit(DatatypeMaxCardinalityRestriction description) {
		System.out.println("DatatypeMaxCardinalityRestriction");
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DescriptionVisitor#visit(org.dllearner.core.owl.DatatypeSomeRestriction)
	 */
	public void visit(DatatypeSomeRestriction description) {
		System.out.println("DatatypeSomeRestriction");
	}
}
