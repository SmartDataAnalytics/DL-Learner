package org.dllearner.autosparql.server.search;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.xml.sax.SAXException;

public class DBpediaClassesSolrIndexCreator {
	
	private SolrInputField uriField = new SolrInputField("uri");
	private SolrInputField labelField = new SolrInputField("label");
	private SolrInputField commentField = new SolrInputField("comment");
	
	private SolrInputDocument doc;
	private SolrServer solr;
	private CoreContainer coreContainer;
	
	private Set<SolrInputDocument> docs;
	
	private static final String CORE_NAME = "dbpedia_classes";
	
	public DBpediaClassesSolrIndexCreator(){
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
		
		docs = new HashSet<SolrInputDocument>();
	}
	
	private SolrServer getRemoteSolrServer() throws MalformedURLException, SolrServerException{
		CommonsHttpSolrServer solr = new CommonsHttpSolrServer("http://localhost:8983/solr/dbpedia");
		solr.setRequestWriter(new BinaryRequestWriter());
		return solr;
	}
	
	private SolrServer getEmbeddedSolrServer() throws ParserConfigurationException, IOException, SAXException{
		File root = new File("/opt/solr");
		coreContainer = new CoreContainer();
		SolrConfig config = new SolrConfig(root + File.separator + CORE_NAME,
				"solrconfig.xml", null);
		CoreDescriptor coreName = new CoreDescriptor(coreContainer,
				CORE_NAME, root + "/solr");
		SolrCore core = new SolrCore(CORE_NAME, root
				+ File.separator + CORE_NAME + "/data", config, null, coreName);
		coreContainer.register(core, false);
		EmbeddedSolrServer solr = new EmbeddedSolrServer(coreContainer, CORE_NAME);
		return solr;
	}
	
	public void createIndex(List<String> dataFiles){
		for(String file : dataFiles){
			if(file.endsWith(".nt")){
				createIndexFromNTriplesFile(file);
			} else if(file.endsWith(".owl")){
				createIndexFromOWLFile(file);
			}
		}
		coreContainer.shutdown();
	}
	
	public void createIndexFromOWLFile(String owlFile){
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLAnnotationProperty labelProperty = man.getOWLDataFactory().getRDFSLabel();
			OWLAnnotationProperty commentProperty = man.getOWLDataFactory().getRDFSComment();
			OWLOntology ont = man.loadOntologyFromOntologyDocument(new File(owlFile));
			String uri = "";
			String label = "";
			String comment = "";
			for(OWLClass cls : ont.getClassesInSignature()){
				uri = cls.toStringID();
				label = "";
				comment = "";
				for(OWLAnnotation lab : cls.getAnnotations(ont, labelProperty)){
					if(lab.getValue() instanceof OWLLiteral){
						OWLLiteral lit = (OWLLiteral)lab.getValue();
						if(lit.hasLang("en")){
							label = lit.getLiteral();
						}
					}
					
				}
				for(OWLAnnotation com : cls.getAnnotations(ont, commentProperty)){
					if(com.getValue() instanceof OWLLiteral){
						OWLLiteral lit = (OWLLiteral)com.getValue();
						if(lit.hasLang("en")){
							comment = lit.getLiteral();
						}
					}
				}
				addDocument(uri, label, comment);
				write2Index();
			}
			solr.commit();
			solr.optimize();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createIndexFromNTriplesFile(String dataFile){
		RDFFormat format = RDFFormat.NTRIPLES;
		RDFParser parser = Rio.createParser(format);
		parser.setRDFHandler(new RDFHandler() {
			boolean first = true;
			int cnt = 1;
			String uri = "";
			String label = "";
			String comment = "";
			String newURI;
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
					addDocument(uri, label, comment);
					uri = newURI;
					label = "";
					cnt++;
					if(cnt % 100 == 0){
						write2Index();
					}
					if(cnt % 10000000 == 0){
						System.out.println(cnt);
					}
					
				}
				label = stmt.getObject().stringValue();
				
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
	}
	
	private void addDocument(String uri, String label, String comment){
		doc = new SolrInputDocument();
		uriField = new SolrInputField("uri");
		labelField = new SolrInputField("label");
		commentField = new SolrInputField("comment");
		doc.put("uri", uriField);
		doc.put("label", labelField);
		doc.put("comment", commentField);
		uriField.setValue(uri, 1.0f);
		labelField.setValue(label, 1.0f);
		commentField.setValue(comment, 1.0f);
		
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
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.WARN);
		if(args.length != 1){
			System.out.println("Usage: DBpediaSolrClassesIndexCreator <dataFile> ");
			System.exit(0);
		}
		
		List<String> dataFiles = new ArrayList<String>();
		for(int i = 0; i < args.length; i++){
			dataFiles.add(args[i]);
		}
		
		new DBpediaClassesSolrIndexCreator().createIndex(dataFiles);
		
		

	}

}
