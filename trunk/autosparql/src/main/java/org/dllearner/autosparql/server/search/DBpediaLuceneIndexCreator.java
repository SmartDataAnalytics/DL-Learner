package org.dllearner.autosparql.server.search;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.ini4j.IniFile;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

public class DBpediaLuceneIndexCreator {
	
	public static final String imageProperty = "http://dbpedia.org/ontology/thumbnail";
	public static final String labelProperty = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String abstractProperty = "http://www.w3.org/2000/01/rdf-schema#comment";
	
	private Document doc = new Document();
	private Field uriField = new Field("uri", "", Field.Store.YES, Field.Index.NOT_ANALYZED);
	private Field labelField = new Field("label", "", Field.Store.YES, Field.Index.ANALYZED);
	private Field commentField = new Field("comment", "", Field.Store.YES, Field.Index.ANALYZED);
	private Field imageURLField = new Field("imageURL", "", Field.Store.YES, Field.Index.NOT_ANALYZED);
	private NumericField pagerankField = new NumericField("pagerank");
	
	private IndexWriter writer;
	
	private PreparedStatement ps;
	
	public DBpediaLuceneIndexCreator(String indexDirectory){
		try {
			Directory dir = FSDirectory.open(new File(indexDirectory));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			writer = new IndexWriter(dir, analyzer, MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initDocument();
		connect2Database();
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
			String newURI;
			@Override
			public void startRDF() throws RDFHandlerException {}
			
			@Override
			public void handleStatement(org.openrdf.model.Statement stmt) throws RDFHandlerException {
				if(first){
					uri = stmt.getSubject().stringValue();
					pageRank = getPageRank(uri);
					first = false;
				}
				
				newURI = stmt.getSubject().stringValue();
				if(!newURI.equals(uri)){
					write2Index(uri, label, abstr, imageURL, pageRank);
					uri = newURI;
					pageRank = getPageRank(uri);
					label = "";
					abstr = "";
					imageURL = "";
					cnt++;
					if(cnt == 100){
						try {
							writer.close();
						} catch (CorruptIndexException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.exit(0);
					}
					System.out.println(cnt);
				}
				if(stmt.getPredicate().stringValue().equals(labelProperty)){
					label = stmt.getObject().stringValue();
				} else if(stmt.getPredicate().stringValue().equals(abstractProperty)){
					abstr = stmt.getObject().stringValue();
				} else if(stmt.getPredicate().stringValue().equals(imageProperty)){
					imageURL = stmt.getObject().stringValue();
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
			
			writer.close();
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initDocument(){
		doc.add(uriField);
		doc.add(labelField);
		doc.add(commentField);
		doc.add(imageURLField);
		doc.add(pagerankField);
	}
	
	private void write2Index(String uri, String label, String comment, String imageURL, int pagerank){
		uriField.setValue(uri);
		labelField.setValue(label);
		commentField.setValue(comment);
		imageURLField.setValue(imageURL);
		pagerankField.setIntValue(pagerank);
		try {
			writer.addDocument(doc);
		} catch (CorruptIndexException e) {
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
				pageRank = rs.getInt("pagerank");
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
			ps = conn.prepareStatement("SELECT pagerank from pagerank WHERE uri = ?");
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

		if(args.length != 2){
			System.out.println("Usage: DBpediaLuceneIndexCreator <dataFile> <indexDirectory>");
			System.exit(0);
		}
		
		String dataFile = args[0];
		String indexDirectory = args[1];
		
		new DBpediaLuceneIndexCreator(indexDirectory).createIndex(dataFile);
		
		

	}

}
