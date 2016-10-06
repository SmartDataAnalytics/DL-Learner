/**
 * 
 */
package org.dllearner.algorithms.isle.index.syntactic;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Creates a Lucene Index for the labels if classes and properties.
 * @author Lorenz Buehmann
 *
 */
public class NTriplesFileLuceneSyntacticIndexCreator {

	public NTriplesFileLuceneSyntacticIndexCreator(InputStream nTriplesStream, String indexPath, String searchField) throws IOException {
		//setup the index
		Directory directory = FSDirectory.open(Paths.get(indexPath));
		
		//setup the index analyzer
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setRAMBufferSizeMB(1024.0);
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
		
		System.out.println( "Creating index ..." );
		
		// setup the index fields, here two fields, for URI and text
		FieldType stringType = new FieldType(StringField.TYPE_STORED);
		stringType.setStoreTermVectors(false);
		FieldType textType = new FieldType(TextField.TYPE_STORED);
		textType.setStoreTermVectors(false);
		
		Set<Document> documents = new HashSet<>();
		
		Iterator<Triple> iterator = RDFDataMgr.createIteratorTriples(nTriplesStream, Lang.NTRIPLES, null);

		Triple triple;
		String text;
		String uri;
		Document doc;
		int i = 0;
		while(iterator.hasNext()){
			triple = iterator.next();
			
			uri = triple.getSubject().getURI();
			text = triple.getObject().getLiteralLexicalForm();
			
			doc = new Document();
			doc.add(new Field("uri", uri, stringType));
			doc.add(new Field(searchField, text, textType));
			
			writer.addDocument(doc);
			if(i++ % 10000 == 0){
//				writer.commit();
				System.out.println(i);
			}
			
		}
		
		writer.commit();
		writer.close();
	}
	
	public static void main(String[] args) throws Exception {
		String indexFile = "/home/me/Documents/short_abstracts_en.nt";
//		indexFile = "/tmp/test.nt";
		String indexPath = "/home/me/Documents/dbpedia/short_abstracts_index";
//		indexPath = "/tmp/index";
		String field = "text";
		new NTriplesFileLuceneSyntacticIndexCreator(new FileInputStream(indexFile), indexPath, field);

		Directory directory = FSDirectory.open(Paths.get(indexPath));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
		Query query = parser.parse("film AND direction");
		
		TopDocs docs = searcher.search(query, 10);
		ScoreDoc[] scoreDocs = docs.scoreDocs;

		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			System.out.println(doc.get(field));

		}
	}
	

}
