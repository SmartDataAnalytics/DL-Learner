package org.dllearner.autosparql.server.cache;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.dllearner.utilities.JamonMonitorLogger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
	
	public DBModelCacheExtended(String cacheDirectory, SparqlEndpoint endpoint){
		super(endpoint);
		
		this.cacheDirectory = cacheDirectory;
		this.endpoint = endpoint;
		
		databaseDirectory = cacheDirectory;
		
		modelGen = new ModelGenerator(endpoint);
		
		createCache();
		fillCache();
	}

	@Override
	public Model getModel(String uri) {
		Model model = ModelFactory.createDefaultModel();
		
		byte[] md5 = md5(uri);	 
		
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM QUERY_CACHE WHERE QUERYHASH=? LIMIT 1");
			ps.setBytes(1, md5);
			java.sql.ResultSet rs = ps.executeQuery();
			
			Clob clob = rs.getClob("TRIPLES");
			
			model.read(clob.getAsciiStream(), null, "N-TRIPLE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return model;
	}

	@Override
	public Model getModel(String uri, int recursionDepth) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void createCache(){
	logger.info("Creating cache...");
		try {
		
		// load driver
		Class.forName("org.h2.Driver");
		
		String jdbcString = "";
		if(autoServerMode) {
			jdbcString = ";AUTO_SERVER=TRUE";
		}
		
		// connect to database (created automatically if not existing)
        conn = DriverManager.getConnection("jdbc:h2:" + databaseDirectory + "/" + databaseName + jdbcString, "sa", "");

        // create cache table if it does not exist
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS RESOURCE_CACHE");
        stmt.execute("DROP TABLE IF EXISTS RESOURCE2RESOURCE");
        stmt.execute("CREATE TABLE IF NOT EXISTS RESOURCE_CACHE(ID INT AUTO_INCREMENT PRIMARY KEY, URI_HASH BINARY, TRIPLES CLOB, STORE_TIME TIMESTAMP)");
        stmt.execute("CREATE TABLE IF NOT EXISTS RESOURCE2RESOURCE (ID1 INT, ID2 INT, PRIMARY KEY(ID1, ID2))");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void fillCache(){
		monitor.reset();
		monitor.start();
		logger.info("Filling cache...");
		int cnt = getResourcesCount();
		logger.info("Number of resources: " + cnt);
		Set<String> resources;
		Model model;
		com.hp.hpl.jena.rdf.model.Statement st;
		String objectURI;
		String modelStr;
		for(int i = 0; i <= 1; i++){
			resources = getResources(CHUNK_SIZE, i * CHUNK_SIZE);
			for(String resource : resources){
				logger.info("Fetching triples for resource " + resource);
				model = createModel(resource);
				logger.info("Got " + model.size() + " triples");
				modelStr = convertModel2String(model);
				logger.info("Writing to DB triples for resource " + resource);
				writeTriples2DB(resource, modelStr);
				int id = getResourceID(resource);
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
		}
		monitor.stop();
		logger.info("Overall time needed: " + monitor.getTotal()/1000 + "s");
		
	}
	
	private int getResourceID(String resource){
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM RESOURCE_CACHE WHERE URI_HASH=? LIMIT 1");
			ps.setBytes(1, md5(resource));
			java.sql.ResultSet rs = ps.executeQuery();
			rs.next();
			int id = rs.getInt("ID");
			
			return id;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	private String createConstructQuery(String resource, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p ").append("?o").append(".\n");
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p ").append("?o").append(".\n");
//		sb.append("FILTER (!regex (?p, \"http://dbpedia.org/property/wikilink\"))");
		sb.append("FILTER (!regex (?p, \"http://dbpedia.org/ontology/wikiPageWikiLink\"))");
		sb.append("FILTER (!regex (?p, \"http://dbpedia.org/property/wikiPageUsesTemplate\"))");
		sb.append("}\n");
		
		sb.append("ORDER BY ?p ?o");
		
		sb.append("\n");
		
		sb.append("LIMIT ").append(limit).append("\n");
		
		sb.append("OFFSET ").append(offset);
		
		return sb.toString();
	}
	
	private Model createModel(String resource){
		String query = createConstructQuery(resource, CHUNK_SIZE, 0);
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
			int i = 0;
			while(model.size() != 0){
				query = createConstructQuery(resource, CHUNK_SIZE, i * CHUNK_SIZE);
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
			logger.info("Database ID for " + resource2 + " is " + id2);
			ps = conn.prepareStatement("INSERT INTO RESOURCE2RESOURCE VALUES(?,?)");
			ps.setInt(1, id1);
			ps.setInt(2, id2);
			ps.executeUpdate();
		} catch (SQLException e) {
			if(!(e.getErrorCode() == 23001)){
				logger.error("An error occured while writing key-key entry to DB.", e);
			}
		} 
	}
	
	public static void main(String[] args) throws IOException{
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		FileAppender fileAppender = new FileAppender( layout, "log/dbpedia_cache_creation.log", false );
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.INFO);		
		Logger.getLogger(DBModelCacheExtended.class);
		DBModelCacheExtended cache = new DBModelCacheExtended("cache", SparqlEndpoint.getEndpointDBpediaLiveAKSW());
	}

}
