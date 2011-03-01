package org.dllearner.autosparql.server.cache;

import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DBModelCacheComplete extends DBModelCacheImpl implements DBModelCache {
	
	private String cacheDirectory;
	private SparqlEndpoint endpoint;
	private int recursionDepth;
	
	private String databaseDirectory = "cache";
	private String databaseName = "extraction";
	private boolean autoServerMode = true;
	private Connection conn;
	
	private static final int CHUNK_SIZE = 1000;
	
	private final Logger logger = Logger.getLogger(DBModelCacheComplete.class);
	
	public DBModelCacheComplete(String cacheDirectory, SparqlEndpoint endpoint, int recursionDepth){
		super(endpoint);
		
		this.cacheDirectory = cacheDirectory;
		this.endpoint = endpoint;
		this.recursionDepth = recursionDepth;
		
		databaseDirectory = cacheDirectory;
		
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
        stmt.execute("DROP TABLE IF EXISTS QUERY_CACHE");
        stmt.execute("CREATE TABLE IF NOT EXISTS QUERY_CACHE(QUERYHASH BINARY PRIMARY KEY, TRIPLES CLOB, STORE_TIME TIMESTAMP)");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void fillCache(){
		logger.info("Filling cache...");
		int cnt = getResourcesCount();
		logger.info("Number of resources: " + cnt);
		Set<String> resources;
		Model model;
		String modelStr;
//		ISparqlEndpoint endpoint = new HttpSparqlEndpoint(this.endpoint.getURL().toString(), new HashSet<String>(this.endpoint.getDefaultGraphURIs()));
		int j = 0;
		for(int i = 0; i <= 10; i++){
			resources = getResources(CHUNK_SIZE, i * CHUNK_SIZE);
			for(String resource : resources){
				j++;
				System.out.println(j);
				logger.info(resource);
				try {
					model = createModel(resource);
					modelStr = convertModel2String(model);
					write2DB(resource, modelStr);
					logger.info("Got " + model.size() + " triple.");
				} catch (Exception e) {
					logger.error("Could not write entry for resource " + resource, e);
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private String createConstructQuery(String resource, int limit, int offset){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		for(int i = 1; i < recursionDepth; i++){sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
		}
		sb.append("}");
		
		for(int i = 0; i < recursionDepth; i++){
			sb.append("FILTER (!regex (?p").append(i).append(", \"http://dbpedia.org/property/wikilink\"))");
		}
	
		sb.append("}\n");
		
		sb.append("ORDER BY ");
		for(int i = 0; i < recursionDepth; i++){
			sb.append("?p").append(i).append(" ").append("?o").append(i).append(" ");
		}
		
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
		Model model = qexec.execConstruct();
		all.add(model);
		qexec.close();
		int i = 1;
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
		return all;
	}
	
	private void write2DB(String key, String value){
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO QUERY_CACHE VALUES(?,?,?)");
			ps.setBytes(1, md5(key));
			ps.setClob(2, new StringReader(value));
			ps.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
}
