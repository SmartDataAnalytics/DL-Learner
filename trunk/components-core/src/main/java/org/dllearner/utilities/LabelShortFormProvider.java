package org.dllearner.utilities;

import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.RDFS;

public class LabelShortFormProvider implements ShortFormProvider{
	
	private final ParameterizedSparqlString queryTemplate = new ParameterizedSparqlString(
			"SELECT ?label WHERE {?entity ?labelProperty ?label. FILTER(LANGMATCHES(LANG(?label),'en'))} LIMIT 1");
	private String labelProperty = RDFS.label.getURI();
	
	private final SimpleIRIShortFormProvider fallback = new SimpleIRIShortFormProvider();
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	
	public LabelShortFormProvider(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public LabelShortFormProvider(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	public void setLabelProperty(String labelProperty) {
		this.labelProperty = labelProperty;
	}

	@Override
	public void dispose() {
	}

	@Override
	public String getShortForm(OWLEntity entity) {
		queryTemplate.clearParams();
		queryTemplate.setIri("entity", entity.toStringID());
		queryTemplate.setIri("labelProperty", labelProperty);
		Query query = queryTemplate.asQuery();
		ResultSet rs = executeSelect(query);
		String label = null;
		if(rs.hasNext()){
			label = rs.next().getLiteral("label").asLiteral().getLexicalForm();
		} else {
			label = fallback.getShortForm(entity.getIRI());
		}
		return label;
	}
	
	protected ResultSet executeSelect(Query query){
		ResultSet rs = null;
		if(cache != null){
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query.toString()));
		} else {
			QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			for(String uri : endpoint.getDefaultGraphURIs()){
				qe.addDefaultGraph(uri);
			}
			rs = qe.execSelect();
		}
		return rs;
	}

	

}
