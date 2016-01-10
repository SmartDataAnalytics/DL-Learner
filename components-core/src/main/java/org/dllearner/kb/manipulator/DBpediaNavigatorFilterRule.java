package org.dllearner.kb.manipulator;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;


public class DBpediaNavigatorFilterRule extends Rule{
	
	
	public DBpediaNavigatorFilterRule(Months month){
		super(month);
	}
	// Set<String> classproperties;
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
//		RDFNode clazz = null;
		RDFNodeTuple typeTuple = null;
		List<RDFNodeTuple> toRemove= new LinkedList<>();
		for (RDFNodeTuple tuple : tuples) {
						
			if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)){
//				clazz = tuple.b;
				typeTuple = tuple;
			}
			
			if (tuple.a.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !(tuple.b.toString().startsWith("http://dbpedia.org/class/yago"))){
				toRemove.add(typeTuple);
			}
			/*if (tuple.a.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !(tuple.b.toString().startsWith("http://dbpedia.org/ontology"))){
				toRemove.add(typeTuple);
			}*/
		}//end for
		for (RDFNodeTuple tuple : toRemove)
			tuples.remove(tuple);
		return tuples;
	}

	@Override
	public void logJamon(){
		
	}

}
