package org.dllearner.kb.manipulator;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class DBPediaNavigatorCityLocatorRule extends Rule{
	
	Map<String,String> map= new HashMap<>();


	public DBPediaNavigatorCityLocatorRule(Months month){
		super(month);
		map.put("http://dbpedia.org/class/custom/City_in_Saxony", "http://dbpedia.org/class/custom/City_in_Europe");
		map.put("http://dbpedia.org/class/custom/City_in_Egypt", "http://dbpedia.org/class/custom/City_in_Africa");
		map.put("http://dbpedia.org/class/custom/City_in_Europe", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Asia", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Australia", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_North_America", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_South_America", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Africa", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_World", "http://dbpedia.org/class/yago/City108524735");
	}
	

	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		
		String uri;
		if(( uri = map.get(subject.getURIString()) ) == null) {
			return tuples;
		}else {
			tuples.add(new RDFNodeTuple(new ResourceImpl(OWLVocabulary.RDFS_SUBCLASS_OF), new ResourceImpl(uri)));
			return tuples;
		}
		
	
	}
	
	@Override
	public void logJamon(){
		
	}
	
	
	
	
	
}
