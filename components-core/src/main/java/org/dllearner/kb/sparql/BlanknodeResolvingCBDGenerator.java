package org.dllearner.kb.sparql;

import java.util.Set;

import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Lorenz Buehmann
 *
 */
public class BlanknodeResolvingCBDGenerator implements ConciseBoundedDescriptionGenerator{
	
	private QueryExecutionFactoryModel qef;
	boolean resolveBlankNodes = true;

	public BlanknodeResolvingCBDGenerator(Model model) {
		String query = "prefix : <http://dl-learner.org/ontology/> "
				+ "construct { ?s ?p ?o ; ?type ?s .} "
				+ "where {  ?s ?p ?o .  bind( if(isIRI(?s),:sameIri,:sameBlank) as ?type )}";
		qef = new QueryExecutionFactoryModel(model);
		QueryExecution qe = qef.createQueryExecution(query);
		Model extendedModel = qe.execConstruct();
		qe.close();
		
		qef = new QueryExecutionFactoryModel(extendedModel);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI) {
		return getConciseBoundedDescription(resourceURI, 0);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth) {
		return getConciseBoundedDescription(resourceURI, depth, false);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs) {
		StringBuilder constructTemplate = new StringBuilder("?s0 ?p0 ?o0 .");
		for(int i = 1; i <= depth; i++){
			constructTemplate.append("?o").append(i-1).append(" ?p").append(i).append(" ?o").append(i).append(" .");
		}
		
		StringBuilder triplesTemplate = new StringBuilder("?s0 ?p0 ?o0 .");
		for(int i = 1; i <= depth; i++){
			triplesTemplate.append("OPTIONAL{").append("?o").append(i-1).append(" ?p").append(i).append(" ?o").append(i).append(" .");
		}
		if(resolveBlankNodes){
			triplesTemplate.append("?o").append(depth).append("((!<x>|!<y>)/:sameBlank)* ?x . ?x ?px ?ox .filter(!(?p in (:sameIri, :sameBlank)))");
		}
		for(int i = 1; i <= depth; i++){
			triplesTemplate.append("}");
		}


		ParameterizedSparqlString query = new ParameterizedSparqlString("prefix : <http://dl-learner.org/ontology/> " + "CONSTRUCT{" + constructTemplate + "}" + " WHERE {" + triplesTemplate + "}");
		query.setIri("s0", resourceURI);
		System.out.println(query);
		QueryExecution qe = qef.createQueryExecution(query.toString());
		Model cbd = qe.execConstruct();
		qe.close();
		return cbd;
	}
	
	public void addPropertiesToIgnore(Set<String> properties) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#setRestrictToNamespaces(java.util.List)
	 */
	@Override
	public void addAllowedPropertyNamespaces(Set<String> namespaces) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#setRecursionDepth(int)
	 */
	@Override
	public void setRecursionDepth(int maxRecursionDepth) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#addAllowedObjectNamespaces(java.util.Set)
	 */
	@Override
	public void addAllowedObjectNamespaces(Set<String> namespaces) {
	}

}
