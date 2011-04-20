package org.dllearner.autosparql.server.cache;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.rdf.model.Model;

public interface SPARQLQueryCache {
	
	Model executeConstructQuery(SparqlEndpoint endpoint, String query) throws SQLException, UnsupportedEncodingException;
	
	String executeSelectQuery(SparqlEndpoint endpoint, String query);
	

}
