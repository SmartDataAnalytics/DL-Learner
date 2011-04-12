package org.dllearner.autosparql.server.store;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dllearner.autosparql.client.exception.SPARQLQuerySavingFailedException;
import org.dllearner.autosparql.client.model.Endpoint;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;
import org.dllearner.autosparql.server.util.HTMLUtils;
import org.dllearner.utilities.Files;

/**
 * 
 * Stores the objects as serialized java object to a file, as given in filename
 * 
 */
public class SimpleFileStore implements Store {
	private static final Logger logger = Logger.getLogger(SimpleFileStore.class);
	private Map<String, StoredSPARQLQuery> question2QueryMap = null;
	private String filename;
	
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
		question2QueryMap = new HashMap<String, StoredSPARQLQuery>();
		if (f.exists()) {
			question2QueryMap = (Map<String, StoredSPARQLQuery>) Files.readObjectfromFile(f);
			logger.debug("Loaded " + question2QueryMap.size() + " concepts from " + filename);
		} else {
			logger.warn("No saved concepts found in " + filename);
		}
	}

	public List<StoredSPARQLQuery> getStoredSPARQLQueries() {
		return new ArrayList<StoredSPARQLQuery>(question2QueryMap.values());
	}

	public void saveSPARQLQuery(String question, String query, Endpoint endpoint) throws SPARQLQuerySavingFailedException{
		put(question, query, endpoint);
	}

	public void incrementHitCount(StoredSPARQLQuery query) {
		query.setHitCount(query.getHitCount() + 1);
	}

	private void put(String question, String query, Endpoint endpoint) {
		StoredSPARQLQuery storedQuery = new StoredSPARQLQuery(question, query, HTMLUtils.encodeHTML(query), endpoint);
		question2QueryMap.put(question, storedQuery);
		saveMap();
	}
	
	private void saveMap(){
		File f = new File(filename);
		Files.writeObjectToFile(question2QueryMap, f);
		logger.debug("Saved " + question2QueryMap.size() + " queries to " + filename);
	}

}
