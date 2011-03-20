package org.dllearner.autosparql.server.cache;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class DBModelCacheExtended extends DBModelCacheImpl implements DBModelCache {
	
	private String cacheDirectory;
	private SparqlEndpoint endpoint;
	private int recursionDepth;
	
	private String databaseDirectory = "cache";
	private String databaseName = "extraction";
	private boolean autoServerMode = true;
	private Connection conn;
	
	private static final int CHUNK_SIZE = 1000;
	
	private ModelGenerator modelGen;
	
	private final Logger logger = Logger.getLogger(DBModelCacheExtended.class);
	
	private Monitor monitor = MonitorFactory.getTimeMonitor("Cache creation monitor");
	private Monitor queryMonitor = MonitorFactory.getTimeMonitor("Query monitor");
	private Monitor dbMonitor = MonitorFactory.getTimeMonitor("DB monitor");
	
	private boolean useMySQL = false;
	
	public DBModelCacheExtended(String cacheDirectory, SparqlEndpoint endpoint){
		this(cacheDirectory, endpoint, false);
	}
	
	public DBModelCacheExtended(String cacheDirectory, SparqlEndpoint endpoint, boolean useMySQL){
		super(endpoint);
		
		this.cacheDirectory = cacheDirectory;
		this.endpoint = endpoint;
		this.useMySQL = useMySQL;
		
		databaseDirectory = cacheDirectory;
		
		modelGen = new ModelGenerator(endpoint);
		
		connect2Database();
	}
	
	public void setUseMySQL(boolean useMySQL){
		this.useMySQL = useMySQL;
	}
	
	private void connect2Database(){
		try {
			// load driver
			if(useMySQL){
				Class.forName("com.mysql.jdbc.Driver");
			} else {
				Class.forName("org.h2.Driver");
			}
			
			String jdbcString = "";
			if(autoServerMode) {
				jdbcString = ";AUTO_SERVER=TRUE";
			}
			
			// connect to database (created automatically if not existing)
			if(useMySQL){
				conn = DriverManager.getConnection("jdbc:mysql://localhost/dbpedia_cache", "root", "root");
			} else {
				conn = DriverManager.getConnection("jdbc:h2:" + databaseDirectory + "/" + databaseName + jdbcString, "sa", "");
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Model getModel(String uri) {
		return getModel(uri, 0);
	}

	@Override
	public Model getModel(String uri, int recursionDepth) {
		Model model = ModelFactory.createDefaultModel();
		
		byte[] md5 = md5(uri);	 
		
		try {
			//Get the ID for the resource
			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM RESOURCE_CACHE WHERE URI_HASH=? LIMIT 1");
			ps.setBytes(1, md5);
			java.sql.ResultSet rs = ps.executeQuery();
			rs.next();
			int id = rs.getInt("ID");
			
			Set<Integer> ids = new HashSet<Integer>();
			ids.add(id);
			
			//Retrieve all IDs
			Set<Integer> tmp = new HashSet<Integer>();
			tmp.add(id);
			for(int i = 1; i <= recursionDepth; i++){
				ps = conn.prepareStatement("SELECT ID2 FROM RESOURCE2RESOURCE WHERE ID1 IN(" + Joiner.on(',').join(tmp) + ")");
				tmp.clear();
				rs = ps.executeQuery();
				while(rs.next()){
					tmp.add(rs.getInt("ID2"));
				}
				ids.addAll(tmp);
				
			}
		
			//Retrieve the Triples for all IDs
			ps = conn.prepareStatement("SELECT TRIPLES FROM RESOURCE_CACHE WHERE ID IN(" + Joiner.on(',').join(ids) + ")");
			rs = ps.executeQuery();
			Clob clob;
			while(rs.next()){
				clob = rs.getClob("TRIPLES");
				model.read(clob.getAsciiStream(), null, "N-TRIPLE");
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return model;
	}
	
	public void createCache(){
	logger.info("Creating cache...");
		try {
		
        // create cache table if it does not exist
        Statement stmt = conn.createStatement();
        if(useMySQL){
        	stmt.execute("CREATE TABLE IF NOT EXISTS RESOURCE_CACHE(ID INT AUTO_INCREMENT PRIMARY KEY, URI_HASH BINARY(16), TRIPLES LONGTEXT, STORE_TIME TIMESTAMP)");
        } else {
        	stmt.execute("CREATE TABLE IF NOT EXISTS RESOURCE_CACHE(ID INT AUTO_INCREMENT PRIMARY KEY, URI_HASH BINARY, TRIPLES CLOB, STORE_TIME TIMESTAMP)");
        }
        stmt.execute("CREATE TABLE IF NOT EXISTS RESOURCE2RESOURCE (ID1 INT, ID2 INT, PRIMARY KEY(ID1, ID2))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteCache(){
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS RESOURCE_CACHE");
			stmt.execute("DROP TABLE IF EXISTS RESOURCE2RESOURCE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void fillCache(int limit){
		fillCache(limit, Collections.<String>emptySet());
	}
	
	public void fillCache(){
		fillCache(-1, Collections.<String>emptySet());
	}
	
	public void fillCache(Set<String> resourceFilters){
		fillCache(-1, resourceFilters);
	}
	
//	public void fillCache(int limit){
//		monitor.reset();
//		monitor.start();
//		logger.info("Filling cache...");
//		Model model;
//		com.hp.hpl.jena.rdf.model.Statement st;
//		String objectURI;
//		String modelStr;
//		int i = 0;
//		logger.info("Fetching resources from " + i*CHUNK_SIZE + " to " + i*CHUNK_SIZE+CHUNK_SIZE);
//		List<String> resources = getResources(CHUNK_SIZE, i * CHUNK_SIZE);
//		logger.info("Got resources:\n" + resources);
//		while (!resources.isEmpty()) {
//			for (String resource : resources) {
//				logger.info("Fetching triples for resource: " + resource);
//				queryMonitor.start();
//				model = createModel(resource);
//				queryMonitor.stop();
//				logger.info("Got " + model.size() + " triples in " + queryMonitor.getLastValue()/1000 + "s.");
//				modelStr = convertModel2String(model);
//				logger.info("Writing triples to DB");
//				dbMonitor.start();
//				writeTriples2DB(resource, modelStr);
//				int id = getResourceID(resource);
//				writeKey2KeyIntoDB(id, id);
//				if(id != -1){
//					for (StmtIterator iter = model.listStatements(); iter.hasNext();) {
//						st = iter.next();
//						if (st.getObject().isURIResource()) {
//							objectURI = st.getObject().asResource().getURI();
//							if (objectURI
//									.startsWith("http://dbpedia.org/resource/")) {
//								logger.info("Writing to DB key-key entry for resources "
//										+ resource + " and " + objectURI);
//								logger.info("Database ID for " + resource + " is "
//										+ id);
//								writeKey2KeyIntoDB(id, objectURI);
//							}
//						}
//					}
//				} else {
//					logger.info("Something went wrong for resource " + resource);
//				}
//				
//				dbMonitor.stop();
//			}
//			i++;
//
//			if (limit != -1 && i * CHUNK_SIZE >= limit) {
//				break;
//			}
//			logger.info("Fetching resources from " + i*CHUNK_SIZE + " to " + i*CHUNK_SIZE+CHUNK_SIZE);
//			resources = getResources(CHUNK_SIZE, i * CHUNK_SIZE);
//			logger.info("Got resources:\n" + resources);
//		}
//		monitor.stop();
//		logger.info("Time to fetch the triples: " + queryMonitor.getTotal()/1000 + "s");
//		logger.info("Time to write to database: " + dbMonitor.getTotal()/1000 + "s");
//		logger.info("Overall time needed: " + monitor.getTotal()/1000 + "s");
//		
//		try {
//			conn.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
	public void fillCache(int limit, Set<String> resourceFilters){
		monitor.reset();
		monitor.start();
		logger.info("Filling cache...");
		Model model;
		com.hp.hpl.jena.rdf.model.Statement st;
		String objectURI;
		String modelStr;
		List<String> resources;
		int count = getResourcesCount(resourceFilters);
		logger.info("Resource count: " + count);
		int i = 0;
		while(i*CHUNK_SIZE <= count){
			try {
				logger.info("Fetching resources from " + i*CHUNK_SIZE + " to " + (i*CHUNK_SIZE+CHUNK_SIZE));
				resources = getResources(CHUNK_SIZE, i * CHUNK_SIZE, resourceFilters);
				logger.info("Got resources:\n" + resources);
				
				for (String resource : resources) {
					logger.info("Fetching triples for resource: " + resource);
					queryMonitor.start();
					model = createModel(resource);
					queryMonitor.stop();
					logger.info("Got " + model.size() + " triples in " + queryMonitor.getLastValue()/1000 + "s.");
					modelStr = convertModel2String(model);
					logger.info("Writing triples to DB");
					dbMonitor.start();
					writeTriples2DB(resource, modelStr);
					int id = getResourceID(resource);
					writeKey2KeyIntoDB(id, id);
					if(id != -1){
						for (StmtIterator iter = model.listStatements(); iter.hasNext();) {
							st = iter.next();
							if (st.getObject().isURIResource()) {
								objectURI = st.getObject().asResource().getURI();
								if (objectURI
										.startsWith("http://dbpedia.org/resource/")) {
									logger.info("Writing to DB key-key entry for resources "
											+ resource + " and " + objectURI);
									logger.info("Database ID for " + resource + " is "
											+ id);
									writeKey2KeyIntoDB(id, objectURI);
								}
							}
						}
					} else {
						logger.info("Something went wrong for resource " + resource);
					}
					
					dbMonitor.stop();
				}
				logger.info("Successfully added resources from " + i*CHUNK_SIZE + " to " + (i*CHUNK_SIZE+CHUNK_SIZE));
			} catch (Exception e) {
				logger.error("An error occured while writing resources from " + i*CHUNK_SIZE + " to " + (i*CHUNK_SIZE+CHUNK_SIZE));
				logger.error(e);
			}
			
			i++;
		}
		
		monitor.stop();
		logger.info("Time to fetch the triples: " + queryMonitor.getTotal()/1000 + "s");
		logger.info("Time to write to database: " + dbMonitor.getTotal()/1000 + "s");
		logger.info("Overall time needed: " + monitor.getTotal()/1000 + "s");
		
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void fillCacheBatched(){
		fillCacheBatched(-1);
	}
	
	public void fillCacheBatched(int limit){
		monitor.reset();
		monitor.start();
		logger.info("Filling cache...");
		Model model;
		String resource;
		Map<String, Model> resources2ModelMap;
		com.hp.hpl.jena.rdf.model.Statement st;
		String objectURI;
		String modelStr;
		int i = 0;
		List<String> resources = getResources(CHUNK_SIZE, i * CHUNK_SIZE);
		while(!resources.isEmpty()){
			resources2ModelMap = getTriplesPerResource(resources);
			for(Entry<String, Model> entry : resources2ModelMap.entrySet()){
				resource = entry.getKey();
				model = entry.getValue();
				logger.info("Resource: " + resource);
				logger.info("Got " + model.size() + " triples:");
				modelStr = convertModel2String(model);
				logger.info("Writing triples to DB");
				writeTriples2DB(resource, modelStr);
				int id = getResourceID(resource);
				writeKey2KeyIntoDB(id, id);
				for(StmtIterator iter = model.listStatements(); iter.hasNext();){
					st = iter.next();
					if(st.getObject().isURIResource()){
						objectURI = st.getObject().asResource().getURI();
						if(objectURI.startsWith("http://dbpedia.org/resource/")){
							logger.info("Writing to DB key-key entry for resources " + resource + " and " + objectURI);
							logger.info("Database ID for " + resource + " is " + id);
							writeKey2KeyIntoDB(id, objectURI);
						}
					}
				}
			}
			i++;
			
			if(limit != -1 && i * CHUNK_SIZE >= limit){
				break;
			}
			
			resources = getResources(CHUNK_SIZE, i * CHUNK_SIZE);
			
		}
		monitor.stop();
		logger.info("Overall time needed: " + monitor.getTotal()/1000 + "s");
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private Model getTriples(List<String> resources){
		logger.info("Fetching triple for resources:\n" + resources);
		String query = createConstructQuery(resources, 1000, 0);
		logger.info("Sending query:\n" + query);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				endpoint.getURL().toString(),
				query,
				endpoint.getDefaultGraphURIs(),
				endpoint.getNamedGraphURIs());
		Model all = ModelFactory.createDefaultModel();
		try {
			Model model = qexec.execConstruct();
			all.add(model);
			qexec.close();
			int i = 1;
			while(model.size() != 0){
				query = createConstructQuery(resources, 1000, i * 1000);
				logger.info("Sending query:\n" + query);
				qexec = QueryExecutionFactory.sparqlService(
						endpoint.getURL().toString(),
						query,
						endpoint.getDefaultGraphURIs(),
						endpoint.getNamedGraphURIs());
				model = qexec.execConstruct();
				all.add(model);
				qexec.close();
				i++;
			}
		} catch (Exception e) {
			logger.error("An error occured while trying to create the JENA Model for resources " + resources, e);
		}
		
		return all;
	}
	
	private Map<String, Model> getTriplesPerResource(List<String> resources){
		Model allTriplesModel = getTriples(resources);
		
		Map<String, Model> resource2Triples = new HashMap<String, Model>();
		Model model;
		for(String resource : resources){
			model = ModelFactory.createDefaultModel();
			model.add(allTriplesModel.listStatements(allTriplesModel.createResource(resource), (Property)null, (RDFNode)null));
			resource2Triples.put(resource, model);
		}
		
		return resource2Triples;
	}
	
	private int getResourceID(String resource){
		int id = -1;
		try {
			
			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM RESOURCE_CACHE WHERE URI_HASH=? LIMIT 1");
			ps.setBytes(1, md5(resource));
			java.sql.ResultSet rs = ps.executeQuery();
			if(rs.next()){
				id = rs.getInt("ID");
			}
		} catch (SQLException e) {
			logger.error("An error occured while trying to get ID for resource " + resource + "from DB", e);
			e.printStackTrace();
		}
		return id;
	}
	
	private String createConstructQuery(String resource, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p ").append("?o").append(".\n");
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p ").append("?o").append(".\n");
		sb.append("FILTER (!regex (?p, \"http://dbpedia.org/ontology/wikiPageWikiLink\"))");
		sb.append("FILTER (!regex (?p, \"http://dbpedia.org/property/wikiPageUsesTemplate\"))");
		sb.append("}\n");
		
		sb.append("\n");
		
		sb.append("LIMIT ").append(limit).append("\n");
		
		sb.append("OFFSET ").append(offset);
		
		return sb.toString();
	}
	
	private String createConstructQuery(List<String> resources, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		int i = 0;
		for(String resource : resources){
			sb.append("<").append(resource).append("> ").append("?p").append(i).append(" ?o").append(i).append(".\n");
			i++;
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		i = 0;
		for(String resource : resources){
			sb.append("{<").append(resource).append("> ").append("?p").append(i).append(" ?o").append(i).append(".}\n");
			if(i < resources.size()-1){
				sb.append("UNION");
			}
			i++;
		}
		sb.append("}\n");
		
		sb.append("LIMIT ").append(limit).append("\n");
		
		sb.append("OFFSET ").append(offset);
		
		return sb.toString();
	}
	
	private Model createModel(String resource){
		String query = createConstructQuery(resource, CHUNK_SIZE, 0);
		logger.info("Sending query:\n" + query);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				endpoint.getURL().toString(),
				query,
				endpoint.getDefaultGraphURIs(),
				endpoint.getNamedGraphURIs());
		Model all = ModelFactory.createDefaultModel();
		try {
			Model model = qexec.execConstruct();
			all.add(model);
			qexec.close();
			int i = 1;
			while(model.size() != 0){
				query = createConstructQuery(resource, CHUNK_SIZE, i * CHUNK_SIZE);
				logger.info("Sending query:\n" + query);
				qexec = QueryExecutionFactory.sparqlService(
						endpoint.getURL().toString(),
						query,
						endpoint.getDefaultGraphURIs(),
						endpoint.getNamedGraphURIs());
				model = qexec.execConstruct();
				all.add(model);
				qexec.close();
				i++;
			}
		} catch (Exception e) {
			logger.error("An error occured while trying to create the JENA Model for resource " + resource, e);
		}
		return all;
	}
	
	private void writeTriples2DB(String key, String value){
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM RESOURCE_CACHE WHERE URI_HASH=? LIMIT 1");
			ps.setBytes(1, md5(key));
			java.sql.ResultSet rs = ps.executeQuery();
			if(rs.next()){
				int id = rs.getInt("ID");
				ps = conn.prepareStatement("UPDATE RESOURCE_CACHE SET TRIPLES=?, STORE_TIME=? WHERE ID=?");
				ps.setClob(1, new StringReader(value));
				ps.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
				ps.setInt(3, id);
				ps.executeUpdate();
			} else {
				ps = conn.prepareStatement("INSERT INTO RESOURCE_CACHE VALUES(null,?,?,?)");
				ps.setBytes(1, md5(key));
				ps.setClob(2, new StringReader(value));
				ps.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			logger.error("An error occured while writing triples for resource " + key + " to DB.", e);
		} 
	}
	
	private void writeTriples2Batch(String key, String value, PreparedStatement ps){
		try {
			ps.setBytes(1, md5(key));
			ps.setClob(2, new StringReader(value));
			ps.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
			ps.addBatch();
		} catch (SQLException e) {
			logger.error("An error occured while writing triples for resource " + key + " to DB.", e);
		} 
	}
	
	private int getTripleCount(Property predicate, RDFNode object){
		int cnt;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(DISTINCT ?x) WHERE {?x ");
		sb.append("<").append(predicate.getURI()).append("> ");
		if(object.isLiteral()){
			sb.append(object.asLiteral().getValue());
		} else if(object.isURIResource()){
			sb.append("<").append(object.asResource().getURI()).append(">");
		}
		sb.append("}");
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), sb.toString());
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}	
		ResultSet rs = queryExecution.execSelect();
		cnt = rs.next().getLiteral(rs.getResultVars().get(0)).getInt();
		return cnt;
	}
	
	private void filterModel(Model model){
		com.hp.hpl.jena.rdf.model.Statement st;
		for(StmtIterator it = model.listStatements(); it.hasNext();){
			st = it.nextStatement();
			int cnt = getTripleCount(st.getPredicate(), st.getObject());
			if(cnt == 1){
				st.changeObject("?");
			}
		}
	}
	
	private void writeKey2KeyIntoDB(int id1, String resource2){
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM RESOURCE_CACHE WHERE URI_HASH=? LIMIT 1");
			ps.setBytes(1, md5(resource2));
			java.sql.ResultSet rs = ps.executeQuery();
			int id2 = 0;
			if(rs.next()){
				id2 = rs.getInt("ID");
			} else {
				ps = conn.prepareStatement("INSERT INTO RESOURCE_CACHE VALUES(null,?,?,?)");
				ps.setBytes(1, md5(resource2));
				ps.setClob(2, new StringReader(""));
				ps.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
				ps.executeUpdate();
				id2 = getResourceID(resource2);
			}
			ps = conn.prepareStatement("INSERT INTO RESOURCE2RESOURCE VALUES(?,?)");
			ps.setInt(1, id1);
			ps.setInt(2, id2);
			ps.executeUpdate();
		} catch (SQLException e) {
			if(useMySQL){
				if(!(e.getErrorCode() == 1062)){
					logger.error("An error occured while writing key-key entry to DB.", e);
				}
			} else {
				if(!(e.getErrorCode() == 23001)){
					logger.error("An error occured while writing key-key entry to DB.", e);
				}
			}
			
		} 
	}
	
	private void writeKey2KeyIntoDB(int id1, int id2){
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO RESOURCE2RESOURCE VALUES(?,?)");
			ps.setInt(1, id1);
			ps.setInt(2, id2);
			ps.executeUpdate();
		} catch (SQLException e) {
			if(!(e.getErrorCode() == 1062)){
				logger.error("An error occured while writing key-key entry to DB.", e);
			}
//			if(!(e.getErrorCode() == 23001)){
//				logger.error("An error occured while writing key-key entry to DB.", e);
//			}
		} 
	}
	
	public static void main(String[] args) throws IOException{
		String resource = "http://dbpedia.org/resource/Leipzig";
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		FileAppender fileAppender = new FileAppender( layout, "log/dbpedia_cache_creation.log", false );
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.INFO);		
		Logger.getLogger(DBModelCacheExtended.class).setLevel(Level.INFO);
		DBModelCacheExtended cache = new DBModelCacheExtended("cache", SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		cache.fillCacheBatched();
//		long startTime = System.nanoTime();
//		Model model = cache.getModel(resource, 2);
//		long endTime = System.nanoTime();
//		System.out.println(model.size());
//		System.out.println((endTime - startTime) / 1000000 + "s");
//		for(StmtIterator iter = model.listStatements();iter.hasNext();){
//			System.out.println(iter.next());
//		}
//		
//		ModelGenerator modelGen = new ModelGenerator(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
//		startTime = System.nanoTime();
//		model = modelGen.createModel(resource, Strategy.INCREMENTALLY, 2);
//		endTime = System.nanoTime();
//		System.out.println(model.size());
//		System.out.println((endTime - startTime) / 1000000 + "s");
//		for(StmtIterator iter = model.listStatements();iter.hasNext();){
//			System.out.println(iter.next());
//		}
	}

}
