package org.dllearner.autosparql.server.search;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DBpediaSchemaIndex {
	
	private IndexWriter writer;
	private IndexSearcher searcher;
	private QueryParser queryParser;
	private TopScoreDocCollector collector;
	private Directory dir;
	
	private int hitsPerPage = 10;
	
	public DBpediaSchemaIndex(String schemaFile){
		dir = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_29);
		try {
			writer = new IndexWriter(dir, analyzer, MaxFieldLength.UNLIMITED);
			createIndex(schemaFile);
			searcher = new IndexSearcher(dir, true);
			queryParser = new QueryParser(Version.LUCENE_29, "label", analyzer);
			collector = TopScoreDocCollector.create(hitsPerPage, true);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createIndex(String path2SchemaFile){
		Model model = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open(path2SchemaFile);
		model.read(in, null);
		StmtIterator iter = model.listStatements(null, RDFS.label, (RDFNode)null);
		while(iter.hasNext()){
			Statement st = iter.next();
			if(st.getObject().asLiteral().getLanguage().equals("en")){
				write2Index(st.getSubject().getURI(), st.getObject().asLiteral().getLexicalForm());
			}
		}
		

//		RdfXmlParser parser = new RdfXmlParser();
//		parser.setStatementHandler(new StatementHandler() {
//			
//			@Override
//			public void handleStatement(Resource subject, URI predicate, Value object)
//					throws StatementHandlerException {
//				if(predicate.stringValue().equals(INDEX_PROPERTY)){System.out.println(subject.stringValue().equals("http://dbpedia.org/ontology/Film"));
//					write2Index(subject.stringValue(), object.stringValue());
//				}
//				
//			}
//		});
//		try {
//			parser.parse(new BufferedInputStream(new FileInputStream(path2SchemaFile)), "http://dbpedia.org");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (org.openrdf.rio.ParseException e) {
//			e.printStackTrace();
//		} catch (StatementHandlerException e) {
//			e.printStackTrace();
//		}
		try {
			writer.close();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void write2Index(String uri, String label){
		Document doc = new Document();
		doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("label", label, Field.Store.YES, Field.Index.ANALYZED));
		try {
			writer.addDocument(doc);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setHitsPerPage(int hitsPerPage){
		this.hitsPerPage = hitsPerPage;
	}
	
	public List<String> getResources(String queryString){
		return getResources(queryString, 0);
	}
	
	public List<String> getResources(String queryString, int offset){
		List<String> resources = new ArrayList<String>();
		try {
			Query query = queryParser.parse(queryString);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs(offset).scoreDocs;
			
			for(ScoreDoc doc : hits) {
			    Document d = searcher.doc(doc.doc);
			    resources.add(d.get("uri"));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resources;
	}
	
	public static void main(String[] args){
		System.out.println(new DBpediaSchemaIndex("evaluation/dbpedia_schema.owl").getResources("Give me all films produced by Hal Roach"));
	}

}
