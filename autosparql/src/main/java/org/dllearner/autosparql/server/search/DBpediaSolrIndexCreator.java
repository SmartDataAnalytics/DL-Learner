package org.dllearner.autosparql.server.search;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.ini4j.IniFile;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class DBpediaSolrIndexCreator {
	
	public static final String imageProperty = "http://dbpedia.org/ontology/thumbnail";
	public static final String labelProperty = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String abstractProperty = "http://www.w3.org/2000/01/rdf-schema#comment";
	public static final String redirectProperty = "http://dbpedia.org/ontology/wikiPageRedirects";
	
	private SolrInputField uriField = new SolrInputField("uri");
	private SolrInputField labelField = new SolrInputField("label");
	private SolrInputField commentField = new SolrInputField("comment");
	private SolrInputField imageURLField = new SolrInputField("imageURL");
	private SolrInputField pagerankField = new SolrInputField("pagerank");
	private SolrInputField typesField = new SolrInputField("types");
	
	private PreparedStatement ps;
	
	private SolrInputDocument doc;
	private SolrServer solr;
	private SolrCore core;
	private CoreContainer coreContainer;
	
	private Set<SolrInputDocument> docs;
	
	private static final String CORE_NAME = "dbpedia_resources";
	
	public DBpediaSolrIndexCreator(){
		try {
			solr = getEmbeddedSolrServer();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		initDocument();
		connect2Database();
		
		docs = new java.util.HashSet<SolrInputDocument>();
	}
	
	private SolrServer getRemoteSolrServer() throws MalformedURLException, SolrServerException{
		CommonsHttpSolrServer solr = new CommonsHttpSolrServer("http://localhost:8983/solr/dbpedia");
		solr.setRequestWriter(new BinaryRequestWriter());
		return solr;
	}
	
	private SolrServer getEmbeddedSolrServer() throws ParserConfigurationException, IOException, SAXException{
		File root = new File("/opt/solr3");
		coreContainer = new CoreContainer();
		SolrConfig config = new SolrConfig(root + File.separator + CORE_NAME,
				"solrconfig.xml", null);
		CoreDescriptor coreName = new CoreDescriptor(coreContainer,
				CORE_NAME, root + "/solr");
		core = new SolrCore(CORE_NAME, root
				+ File.separator + CORE_NAME + "/data", config, null, coreName);
		coreContainer.register(core, false);
		EmbeddedSolrServer solr = null;
		try {
			solr = new EmbeddedSolrServer(coreContainer, CORE_NAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return solr;
	}
	
	public void createIndex(String dataFile){
		RDFFormat format = RDFFormat.NTRIPLES;
		RDFParser parser = Rio.createParser(format);
		parser.setRDFHandler(new RDFHandler() {
			boolean first = true;
			int cnt = 1;
			String uri = "";
			String label = "";
			String imageURL = "";
			String abstr = "";
			int pageRank = 0;
			List<String> types = new ArrayList<String>();
			String newURI;
			boolean skip = false;
			@Override
			public void startRDF() throws RDFHandlerException {}
			
			@Override
			public void handleStatement(org.openrdf.model.Statement stmt) throws RDFHandlerException {
				if(first){
					uri = stmt.getSubject().stringValue();
					first = false;
				}
				
				newURI = stmt.getSubject().stringValue();
				
				if(!newURI.equals(uri)){
					if(!skip){
						pageRank = getPageRank(uri);
						addDocument(uri, label, abstr, imageURL, pageRank, types);
					}
					uri = newURI;
					label = "";
					abstr = "";
					imageURL = "";
					pageRank = 0;
					types.clear();
					skip = false;
					cnt++;
					if(cnt % 100 == 0){
						write2Index();
					}
					if(cnt % 10000000 == 0){
						System.out.println(cnt);
					}
					
				}
				if(stmt.getPredicate().stringValue().equals(labelProperty)){
					label = stmt.getObject().stringValue();
					if(label.toLowerCase().contains(" list of ")){
						skip = true;
					}
				} else if(stmt.getPredicate().stringValue().equals(abstractProperty)){
					abstr = stmt.getObject().stringValue();
				} else if(stmt.getPredicate().stringValue().equals(imageProperty)){
					imageURL = stmt.getObject().stringValue();
				} else if(stmt.getPredicate().stringValue().equals(redirectProperty) || stmt.getSubject().stringValue().startsWith("http://upload.")){
					skip = true;
				} else if(stmt.getPredicate().stringValue().equals(RDF.type.getURI())){
					if(!stmt.getObject().stringValue().equals(OWL.Thing.getURI())){
						types.add(stmt.getObject().stringValue());
					}
					
				}
				
				
				
			}
			
			@Override
			public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {}
			@Override
			public void handleComment(String arg0) throws RDFHandlerException {}
			@Override
			public void endRDF() throws RDFHandlerException {}
		});
		try {
			parser.parse(new BufferedInputStream(new FileInputStream(dataFile)), "http://dbpedia.org");
			write2Index();
			solr.commit();
			solr.optimize();
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} finally {
//			core.close();
			coreContainer.shutdown();
		}
	}
	
	private void printTestSearch(){
		System.out.println("Searching");
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", "January");
			params.set("rows", 10);
			params.set("start", 0);
			QueryResponse response = solr.query(params);
			for(SolrDocument d : response.getResults()){
				System.out.println(d.get("uri"));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
	
	private void initDocument(){
		doc = new SolrInputDocument();
		doc.put("uri", uriField);
		doc.put("label", labelField);
		doc.put("comment", commentField);
		doc.put("imageURL", imageURLField);
		doc.put("pagerank", pagerankField);
		doc.put("types", typesField);
	}
	
	private void addDocument(String uri, String label, String comment, String imageURL, int pagerank, List<String> types){
		doc = new SolrInputDocument();
		uriField = new SolrInputField("uri");
		labelField = new SolrInputField("label");
		commentField = new SolrInputField("comment");
		imageURLField = new SolrInputField("imageURL");
		pagerankField = new SolrInputField("pagerank");
		typesField = new SolrInputField("types");
		doc.put("uri", uriField);
		doc.put("label", labelField);
		doc.put("comment", commentField);
		doc.put("imageURL", imageURLField);
		doc.put("pagerank", pagerankField);
		doc.put("types", typesField);
		uriField.setValue(uri, 1.0f);
		labelField.setValue(label, 1.0f);
		commentField.setValue(comment, 1.0f);
		imageURLField.setValue(imageURL, 1.0f);
		pagerankField.setValue(pagerank, 1.0f);
		typesField.addValue(types, 1.0f);
		
		docs.add(doc);
	}
	
	private void write2Index(){
		try {
			solr.add(docs);
			docs.clear();
		}  catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int getPageRank(String uri){
		int pageRank = 0;
		
		try {
			ps.setString(1, uri);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				pageRank = rs.getInt("rank");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return pageRank;
	}
	
	private void connect2Database(){
		try {
			String iniFile = "settings.ini";
			Preferences prefs = new IniFile(new File(iniFile));
			String dbServer = prefs.node("database").get("server", null);
			String dbName = "pagerank";//prefs.node("database").get("name", null);
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);
			
			Class.forName("com.mysql.jdbc.Driver");
			String url =
	            "jdbc:mysql://"+dbServer+"/"+dbName;
			Connection conn = DriverManager.getConnection(url, dbUser, dbPass);
			ps = conn.prepareStatement("SELECT MAX(rank) as rank from pagerank2 WHERE uri = ?");
		} catch (BackingStoreException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.WARN);
		if(args.length != 1){
			System.out.println("Usage: DBpediaSolrIndexCreator <dataFile> ");
			System.exit(0);
		}
		
		String dataFile = args[0];
		
		new DBpediaSolrIndexCreator().createIndex(dataFile);
		
		

	}

}
