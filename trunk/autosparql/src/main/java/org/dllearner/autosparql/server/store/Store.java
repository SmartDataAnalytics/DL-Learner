package org.dllearner.autosparql.server.store;

import java.util.List;

import org.dllearner.autosparql.client.exception.SPARQLQuerySavingFailedException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;

public interface Store{

	public void saveSPARQLQuery(String question, String query, String endpoint, List<Example> posExamples, List<Example> negExamples, Example lastSuggestedExample) throws SPARQLQuerySavingFailedException;

	public List<StoredSPARQLQuery> getStoredSPARQLQueries();

	public void incrementHitCount(StoredSPARQLQuery storedQuery);


}
