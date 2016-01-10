package org.dllearner.kb.manipulator;

import java.net.URLEncoder;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class StringToResource extends Rule{
	
	String namespace;
	int limit ;


	/**
	 * @param month the month
	 * @param resourceNamespace ns for the created uris
	 * @param limit does not convert strings that are longer than a specific value, zero means convert all
	 */
	public StringToResource(Months month, String resourceNamespace, int limit) { 
		super(month);
		String slash = "";
		if(!resourceNamespace.endsWith("/")) {
			slash="/";
		}
		
		this.namespace = resourceNamespace+slash;
		this.limit = limit;
	}
	
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<>();
		for (RDFNodeTuple tuple : tuples) {
			
			// do nothing if the object contains http://
			if(!tuple.b.isURIResource()){
				boolean replace = true;
				
				//check for numbers 
				if(((Literal) tuple.b).getDatatypeURI()!= null){
						replace = false; 
							
				}
				// do nothing if limit is exceeded
				if(limit != 0 && tuple.b.toString().length()>limit){
					replace = false;
				}
				
				if(tuple.b.toString().startsWith("http://")){
					//System.out.println(tuple.b.toString());
					if(
						tuple.b.toString().startsWith("http://ru.wikipedia.org/wiki/Ð¡ÐµÑ") ||
						tuple.bPartContains(" ")
							
					){
						continue;
						
					}
					tuple.b = new ResourceImpl(tuple.b.toString());
					replace= false;
				}
				
				
				if (replace){
				
					String tmp = tuple.b.toString();
					try{
						//encode
					tmp = URLEncoder.encode(tmp, "UTF-8");
					}catch (Exception e) {
						e.printStackTrace();
						System.exit(0);
					}
					
					tuple.b = new ResourceImpl(namespace+tmp);
					JamonMonitorLogger.increaseCount(StringToResource.class, "convertedToURI");
				}else {
					// do nothing
				}
			}
			
			keep.add(tuple);
		}
		return  keep;
	}

	@Override
	public void logJamon(){
		JamonMonitorLogger.increaseCount(StringToResource.class, "replacedObjects");
	}
	
	
	
}
