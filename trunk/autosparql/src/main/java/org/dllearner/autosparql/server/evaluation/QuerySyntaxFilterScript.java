package org.dllearner.autosparql.server.evaluation;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

public class QuerySyntaxFilterScript {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String queryString = "SELECT ?var0 WHERE {<s> <p> ?var0;<p1> <o1>.}";
		Query query = QueryFactory.create(queryString);
		Element queryPattern = query.getQueryPattern();
//		System.out.println(queryPattern);
		if(queryPattern instanceof ElementGroup){
			for(Element element : ((ElementGroup) queryPattern).getElements()){
				if(element instanceof ElementTriplesBlock){
					BasicPattern triples = ((ElementTriplesBlock) element).getPattern();
					for(Triple triple : triples){
						System.out.println(triple);
						if(triple.getObject().isVariable()){
							System.out.println(triple.getObject().getName());
							System.out.println("Has to be filtered.");
						} else {
							
							System.out.println(triple.getObject().isVariable());
						}
						
					}
				}
			}
		}

	}

}
