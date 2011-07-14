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
import org.openrdf.vocabulary.RDFS;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.xml.sax.SAXException;

public class DBpediaPropertiesSolrIndexCreator {
	
	public static final String labelProperty = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String abstractProperty = "http://www.w3.org/2000/01/rdf-schema#comment";
	
	private SolrInputField uriField = new SolrInputField("uri");
	private SolrInputField labelField = new SolrInputField("label");
	private SolrInputField commentField = new SolrInputField("comment");
	private SolrInputField domainField = new SolrInputField("domain");
	private SolrInputField rangeField = new SolrInputField("range");
	
	private SolrInputDocument doc;
	private SolrServer solr;
	private CoreContainer coreContainer;
	
	private Set<SolrInputDocument> docs;
	
	private static final String CORE_NAME = "dbpedia_properties";
	
	public DBpediaPropertiesSolrIndexCreator(){
		solr = getRemoteSolrServer();
        initDocument();
        
        docs = new HashSet<SolrInputDocument>();

	}
	
	private SolrServer getRemoteSolrServer(){
		CommonsHttpSolrServer solr = null;
		try {
			solr = new CommonsHttpSolrServer("http://localhost:8983/solr/dbpedia");
			solr.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return solr;
	}
	
	private SolrServer getEmbeddedSolrServer(){
		EmbeddedSolrServer solr = null;
		try {
			File root = new File("/opt/solr");
			coreContainer = new CoreContainer();
			SolrConfig config = new SolrConfig(root + File.separator + CORE_NAME,
					"solrconfig.xml", null);
			CoreDescriptor coreName = new CoreDescriptor(coreContainer,
					CORE_NAME, root + "/solr");
			SolrCore core = new SolrCore(CORE_NAME, root
					+ File.separator + CORE_NAME + "/data", config, null, coreName);
			coreContainer.register(core, false);
			solr = new EmbeddedSolrServer(coreContainer, CORE_NAME);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
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
					addDocument(uri, label, comment, "", "");
					uri = newURI;
					label = "";
					comment = "";
					cnt++;
					if(cnt % 100 == 0){
						write2Index();
					}
					if(cnt % 10000000 == 0){
						System.out.println(cnt);
					}
					
				}
				if(stmt.getPredicate().stringValue().equals(RDFS.LABEL)){
					label = stmt.getObject().stringValue();
				} else if(stmt.getPredicate().stringValue().equals(RDFS.COMMENT)){
					comment = stmt.getObject().stringValue();
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
		} 
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
			String domain = "";
			String range = "";
			Set<OWLEntity> properties = new HashSet<OWLEntity>();
			properties.addAll(ont.getObjectPropertiesInSignature());
			properties.addAll(ont.getDataPropertiesInSignature());
			int cnt = 1;
			for(OWLEntity prop : properties){
				uri = prop.toStringID();
				label = "";
				comment = "";
				domain = "";
				range = "";
				for(OWLAnnotation lab : prop.getAnnotations(ont, labelProperty)){
					if(lab.getValue() instanceof OWLLiteral){
						OWLLiteral lit = (OWLLiteral)lab.getValue();
						if(lit.hasLang("en")){
							label = lit.getLiteral();
						}
					}
					
				}
				for(OWLAnnotation com : prop.getAnnotations(ont, commentProperty)){
					if(com.getValue() instanceof OWLLiteral){
						OWLLiteral lit = (OWLLiteral)com.getValue();
						if(lit.hasLang("en")){
							comment = lit.getLiteral();
						}
					}
				}
				if(prop instanceof OWLObjectProperty){
					Set<OWLClassExpression> domains = prop.asOWLObjectProperty().getDomains(ont);
					if(!domains.isEmpty()){
						domain = domains.iterator().next().asOWLClass().toStringID();
					}
					Set<OWLClassExpression> ranges = prop.asOWLObjectProperty().getRanges(ont);
					if(!ranges.isEmpty()){
						range = ranges.iterator().next().asOWLClass().toStringID();
					}
				} else if(prop instanceof OWLDataProperty){
					Set<OWLClassExpression> domains = prop.asOWLDataProperty().getDomains(ont);
					if(!domains.isEmpty()){
						domain = domains.iterator().next().asOWLClass().toStringID();
					}
					Set<OWLDataRange> ranges = prop.asOWLDataProperty().getRanges(ont);
					if(!ranges.isEmpty()){
						range = ranges.iterator().next().asOWLDatatype().toStringID();
					}
				}
				if(cnt % 100 == 0){
					write2Index();
				}
				addDocument(uri, label, comment, domain, range);
				cnt++;
			}
			write2Index();
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
	
	private void initDocument(){
		doc = new SolrInputDocument();
		doc.put("uri", uriField);
		doc.put("label", labelField);
		doc.put("comment", commentField);
		doc.put("domain", domainField);
		doc.put("range", rangeField);
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
	
	private void addDocument(String uri, String label, String comment, String domain, String range){
		doc = new SolrInputDocument();
		uriField = new SolrInputField("uri");
		labelField = new SolrInputField("label");
		commentField = new SolrInputField("comment");
		domainField = new SolrInputField("domain");
		rangeField = new SolrInputField("range");
		doc.put("uri", uriField);
		doc.put("label", labelField);
		doc.put("comment", commentField);
		doc.put("domain", domainField);
		doc.put("range", rangeField);
		uriField.setValue(uri, 1.0f);
		labelField.setValue(label, 1.0f);
		commentField.setValue(comment, 1.0f);
		domainField.setValue(domain, 1.0f);
		rangeField.setValue(range, 1.0f);
		
		docs.add(doc);
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.WARN);
		if(args.length == 0){
			System.out.println("Usage: DBpediaSolrPropertiesIndexCreator <dataFile_1> ...<dataFile_n> ");
			System.exit(0);
		}
		
		List<String> dataFiles = new ArrayList<String>();
		for(int i = 0; i < args.length; i++){
			dataFiles.add(args[i]);
		}
		
		new DBpediaPropertiesSolrIndexCreator().createIndex(dataFiles);
		
		

	}

}
