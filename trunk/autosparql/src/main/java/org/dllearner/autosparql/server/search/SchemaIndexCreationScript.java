package org.dllearner.autosparql.server.search;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

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
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;
import org.openrdf.rio.rdfxml.RdfXmlParser;

public class SchemaIndexCreationScript {
	
	public static void addIndexEntry(IndexWriter w, String uri, String label) throws CorruptIndexException, IOException{
		Document doc = new Document();
		doc.add(new Field("uri", uri, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("label", label, Field.Store.YES, Field.Index.ANALYZED));
		w.addDocument(doc);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws org.openrdf.rio.ParseException 
	 * @throws StatementHandlerException 
	 */
	public static void main(String[] args) throws IOException, ParseException, org.openrdf.rio.ParseException, StatementHandlerException {
		String path = "/home/lorenz/arbeit/workspace/NL2SPARQL/dbpedia_3.6.owl";
		
		final String labelProperty = "http://www.w3.org/2000/01/rdf-schema#label";
		
		Directory dir = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
		final IndexWriter writer = new IndexWriter(dir, analyzer, MaxFieldLength.UNLIMITED);
		
		RdfXmlParser parser = new RdfXmlParser();
		parser.setStatementHandler(new StatementHandler() {
			
			@Override
			public void handleStatement(Resource subject, URI predicate, Value object)
					throws StatementHandlerException {
				if(predicate.stringValue().equals(labelProperty)){
					try {
						addIndexEntry(writer, subject.stringValue(), object.stringValue());
					} catch (CorruptIndexException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println(object);
				
			}
		});
		parser.parse(new BufferedInputStream(new FileInputStream(path)), "http://dbpedia.org");
		writer.close();
		
		IndexSearcher searcher = new IndexSearcher(dir, true);
		String queryString = "car";
		Query query = new QueryParser(Version.LUCENE_30, "label", analyzer).parse(queryString);
		int hitsPerPage = 10;
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		System.out.println("Found " + hits.length + " hits.");
		for(int i=0;i<hits.length;++i) {
		    int docId = hits[i].doc;
		    Document d = searcher.doc(docId);
		    System.out.println((i + 1) + ". " + d.get("uri") + "\t" + hits[i].score);
		}


	

	}

}
