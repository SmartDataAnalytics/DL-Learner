package org.dllearner.test;

import java.util.List;

import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.ResultBinding;

public class TripleTypeTest {

	
	public static void main(String[] args) {
		String sparqlQueryString ="SELECT * WHERE { <http://dbpedia.org/resource/Angela_Merkel> ?predicate ?object. FILTER (isLiteral(?object))}";
		//sparqlQueryString ="SELECT * WHERE { <http://dbpedia.org/resource/Angela_Merkel> <http://dbpedia.org/property/hasPhotoCollection> ?object }";
		System.out.println(sparqlQueryString);
		
		SPARQLTasks st = new SPARQLTasks (Cache.getDefaultCache(), SparqlEndpoint.getEndpointDBpedia());
		
		ResultSetRewindable rsw = st.queryAsResultSet(sparqlQueryString);
		@SuppressWarnings("unchecked")
		List<ResultBinding> l = ResultSetFormatter.toList(rsw);
		
		for (ResultBinding binding : l) {
			//RDFNode pred = binding.get("predicate");
			RDFNode obj = binding.get("object");
			//System.out.println(pred.toString());
			//System.out.println(obj.toString());
			System.out.println(obj.isLiteral());
			System.out.println(obj.isAnon());
			System.out.println(obj.isResource());
			System.out.println(obj.isURIResource());
			Literal lit =(Literal) obj;
			System.out.println(lit.toString());
			System.out.println(lit.getLanguage());
		}
		
		
	}
}
