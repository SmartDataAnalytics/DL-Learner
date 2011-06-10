package org.dllearner.autosparql.server.store;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.exception.SPARQLQuerySavingFailedException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;
import org.dllearner.autosparql.client.model.StoredSPARQLQuerySer;
import org.dllearner.autosparql.server.util.HTMLUtils;
import org.dllearner.utilities.Files;

/**
 * 
 * Stores the objects as serialized java object to a file, as given in filename
 * 
 */
public class SimpleFileStore implements Store {
	private static final Logger logger = Logger.getLogger(SimpleFileStore.class);
	private Map<String, StoredSPARQLQuerySer> question2QueryMap = null;
	private String filename;
	
	public SimpleFileStore(){
	}
	
	public SimpleFileStore(String filename){
		this.filename = filename;
		init();
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	protected void init() {
//		this.filename = getComponentConfig().getPathModifier() + this.filename;
		File f = new File(filename);
		question2QueryMap = new HashMap<String, StoredSPARQLQuerySer>();
		if (f.exists()) {
			try {
				question2QueryMap = (Map<String, StoredSPARQLQuerySer>) Files.readObjectfromFile(f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.debug("Loaded " + question2QueryMap.size() + " concepts from " + filename);
		} else {
			logger.warn("No saved concepts found in " + filename);
		}
	}

	@Override
	public List<StoredSPARQLQuery> getStoredSPARQLQueries() {
		List<StoredSPARQLQuery> storedQueries = new ArrayList<StoredSPARQLQuery>();
		for(StoredSPARQLQuerySer q : question2QueryMap.values()){
			storedQueries.add(q.toStoredSPARQLQuery());
		}
		return storedQueries;
	}

	@Override
	public void saveSPARQLQuery(String question, String query, String endpoint, List<Example> posExamples, List<Example> negExamples, Example lastSuggestedExample) throws SPARQLQuerySavingFailedException{
		put(question, query, endpoint, posExamples, negExamples, lastSuggestedExample);
//		put(question, query, endpoint, null, null, null);
	}

	@Override
	public void incrementHitCount(StoredSPARQLQuery query) {
		query.setHitCount(query.getHitCount() + 1);
		question2QueryMap.put(query.getQuestion(), query.toStoredSPARQLQuerySer());
		saveMap();
	}

	private void put(String question, String query, String endpoint, List<Example> posExamples, List<Example> negExamples, Example lastSuggestedExample) {
		try {
			StoredSPARQLQuery storedQuery = new StoredSPARQLQuery(question, query, HTMLUtils.encodeHTML(query), endpoint, posExamples, negExamples, lastSuggestedExample);
			question2QueryMap.put(question, storedQuery.toStoredSPARQLQuerySer());
			saveMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void saveMap(){
		File f = new File(filename);
		Files.writeObjectToFile(question2QueryMap, f);
		logger.debug("Saved " + question2QueryMap.size() + " queries to " + filename);
	}

}
