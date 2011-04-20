package org.dllearner.autosparql.server.store;

import java.util.List;

import org.dllearner.autosparql.client.exception.SPARQLQuerySavingFailedException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

public interface Store{

	public void saveSPARQLQuery(String question, String query, String endpoint) throws SPARQLQuerySavingFailedException;

	public List<StoredSPARQLQuery> getStoredSPARQLQueries();

	public void incrementHitCount(StoredSPARQLQuery storedQuery);


}
