package org.dllearner.autosparql.server.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
 * Stores the queries as serialized java object to a file, as given in filename
 * 
 */
public class SimpleFileStore implements Store {
	private static final String FILENAME = "stored_queries.txt";
	
	private static final Logger logger = Logger.getLogger(SimpleFileStore.class);
	private Map<String, StoredSPARQLQuerySer> question2QueryMap = null;
	private String path;
	
	public SimpleFileStore(){
	}
	
	public SimpleFileStore(String directory){
		new File(directory).mkdirs();
		path = directory + File.separator + FILENAME;
		init();
	}

	public void setFilename(String directory) {
		new File(directory).mkdirs();
	}
	

	protected void init() {
//		this.filename = getComponentConfig().getPathModifier() + this.filename;
		File f = new File(path);
		question2QueryMap = new HashMap<String, StoredSPARQLQuerySer>();
		if (f.exists()) {
			try {
				question2QueryMap = (Map<String, StoredSPARQLQuerySer>) Files.readObjectfromFile(f);
			} catch (Exception e) {
				logger.error("Error while deserializing stored queries", e);
				e.printStackTrace();
			}
			logger.debug("Loaded " + question2QueryMap.size() + " concepts from " + path);
		} else {
			logger.warn("Found no file " + path);
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
			StoredSPARQLQuery storedQuery = new StoredSPARQLQuery(question, query, HTMLUtils.encodeHTML(query), endpoint, posExamples, negExamples, lastSuggestedExample, new Date(), 0);
			question2QueryMap.put(question, storedQuery.toStoredSPARQLQuerySer());
			saveMap();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void saveMap(){
		try {
			File f = new File(path);
			Files.writeObjectToFile(question2QueryMap, f);
			logger.debug("Saved " + question2QueryMap.size() + " queries to " + path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
