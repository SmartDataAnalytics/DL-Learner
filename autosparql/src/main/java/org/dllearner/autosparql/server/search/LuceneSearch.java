package org.dllearner.autosparql.server.search;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.dllearner.autosparql.client.model.Example;

public class LuceneSearch implements Search{
	
	private static Logger logger = Logger.getLogger(LuceneSearch.class);
	
	private IndexSearcher searcher;
	private QueryParser queryParser;
	private TopScoreDocCollector collector;
	
	private int hitsPerPage = 10;

	public LuceneSearch(String indexDirectory){
		try {
			Directory dir = FSDirectory.open(new File(indexDirectory));//RAMDirectory();
			searcher = new IndexSearcher(dir, true);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_29);
			queryParser = new QueryParser(Version.LUCENE_29, "comment", analyzer);
			collector = TopScoreDocCollector.create(hitsPerPage, true);
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getResources(String queryString){
		return getResources(queryString, 0);
	}
	
	public List<String> getResources(String queryString, int offset){
		List<String> resources = new ArrayList<String>();
		try {
			Query query = queryParser.parse(queryString);
			searcher.search(query, collector);
//			System.out.println(searcher.search(query, null, 10, new Sort(new SortField("pagerank", SortField.INT))));
			ScoreDoc[] hits = collector.topDocs(offset).scoreDocs;
			
			for(ScoreDoc doc : hits) {
			    Document d = searcher.doc(doc.doc);
			    resources.add(d.get("uri"));
			}
		} catch (ParseException e) {
			logger.error("Error while parsing query.", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error while running search.", e);
			e.printStackTrace();
		}
		return resources;
	}
	
	public List<Example> getExamples(String queryString){
		return getExamples(queryString, 0);
	}
	
	public List<Example> getExamples(String queryString, int offset){
		List<Example> examples = new ArrayList<Example>();
		try {
			Query query = queryParser.parse(queryString);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs(offset).scoreDocs;
			
			for(ScoreDoc doc : hits) {
			    Document d = searcher.doc(doc.doc);
			    String uri = d.get("uri");
			    String label = d.get("label");
			    String comment = d.get("comment");
			    String imageURL = d.get("imageURL");
			    examples.add(new Example(uri, label, imageURL, comment));
			}
		} catch (ParseException e) {
			logger.error("Error while parsing query.", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error while running search.", e);
			e.printStackTrace();
		}
		return examples;
	}
	
	public int getTotalHits(){
		return collector.getTotalHits();
	}
	
	public int getTotalHits(String queryString){
		try {
			Query query = queryParser.parse(queryString);
			searcher.search(query, collector);
		} catch (ParseException e) {
			logger.error("Error while parsing query.", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("Error while running search.", e);
			e.printStackTrace();
		}
		return collector.getTotalHits();
	}
	
	public void setHitsPerPage(int hitsPerPage){
		this.hitsPerPage = hitsPerPage;
	}
	
	public void close(){
		try {
			searcher.close();
		} catch (IOException e) {
			logger.error("Error while closing IndexSearcher.", e);
			e.printStackTrace();
		}
	}
	
	public int getIndexSize(){
		try {
			return searcher.maxDoc();
		} catch (IOException e) {
			logger.error("Error while getting index size.", e);
			e.printStackTrace();
		}
		return -1;
	}
}
