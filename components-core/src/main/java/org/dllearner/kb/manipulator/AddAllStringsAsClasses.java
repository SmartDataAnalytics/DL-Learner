package org.dllearner.kb.manipulator;

import java.net.URLEncoder;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class AddAllStringsAsClasses extends Rule{
	String namespace;


	/**
	 * @param month the month
	 * @param resourceNamespace ns for the created uris
	 */
	public AddAllStringsAsClasses(Months month, String resourceNamespace) { 
		super(month);
		String slash = "";
		if(!resourceNamespace.endsWith("/")) {
			slash="/";
		}
		
		this.namespace = resourceNamespace+slash;
		
	}
	
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<>();
		for (RDFNodeTuple tuple : tuples) {
			//System.out.println(tuple);
			//System.exit(0);
			if(tuple.b.isURIResource()){
				//System.out.println("added");
				keep.add(tuple);
				continue;
			}
		
			//RDFNode b = null;
			if(!tuple.b.isURIResource()){
				boolean replace = true;
				
				//check for numbers 
				if(((Literal) tuple.b).getDatatypeURI()!= null){
						replace = false; 
				}
							
				//if string is an uri
				if(tuple.b.toString().startsWith("http://")){
					//System.out.println(tuple.b.toString());
					if(	tuple.b.toString().startsWith("http://ru.wikipedia.org/wiki/Ð¡ÐµÑ") ||
						tuple.bPartContains(" ")
					){	
						//filter
						continue; 
					}
					
					tuple.b = new ResourceImpl(tuple.b.toString());
					replace = false;
				}
				
				
				if (replace){
					
					String tmp = tuple.b.toString();
					//System.out.println("replaced: "+tmp);
					try{
						//encode
					tmp = URLEncoder.encode(tmp, "UTF-8");
					}catch (Exception e) {
						e.printStackTrace();
						System.exit(0);
					}
						tmp = namespace+tmp;
						tmp = tmp.replaceAll("%", "_");
						tmp = "c"+tmp;
						keep.add(new RDFNodeTuple(new ResourceImpl(OWLVocabulary.RDF_TYPE),new ResourceImpl(tmp)));
					
				}else {
					// do nothing
				}//end else
			
			}//end if
			keep.add(tuple);
		}
		return  keep;
	}

	@Override
	public void logJamon(){
		JamonMonitorLogger.increaseCount(AddAllStringsAsClasses.class, "replacedObjects");
	}
	
	
	
}
