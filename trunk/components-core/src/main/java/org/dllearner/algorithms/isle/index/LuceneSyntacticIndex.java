/**
 * 
 */
package org.dllearner.algorithms.isle.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public class LuceneSyntacticIndex implements SyntacticIndex {
	
	private IndexSearcher searcher;
	private QueryParser parser;
	private IndexReader indexReader;
	private String searchField;

	public LuceneSyntacticIndex(IndexReader indexReader, String searchField) throws Exception {
		this.indexReader = indexReader;
		this.searchField = searchField;
		searcher = new IndexSearcher(indexReader);
		StandardAnalyzer analyzer = new StandardAnalyzer( Version.LUCENE_43);
		parser = new QueryParser( Version.LUCENE_43, searchField, analyzer );
	}
	
	public LuceneSyntacticIndex(Directory directory, String searchField) throws Exception {
		this(DirectoryReader.open(directory), searchField);
	}
	
	public LuceneSyntacticIndex(String indexDirectory, String searchField) throws Exception {
		this(DirectoryReader.open(FSDirectory.open(new File(indexDirectory))), searchField);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SyntacticIndex#getDocuments(java.lang.String)
	 */
	@Override
	public Set<org.dllearner.algorithms.isle.index.Document> getDocuments(String searchString) {
		Set<org.dllearner.algorithms.isle.index.Document> documents = new HashSet<org.dllearner.algorithms.isle.index.Document>();
		try {
			Query query = parser.parse(searchString);
			ScoreDoc[] result = searcher.search(query, getSize()).scoreDocs;
			for (int i = 0; i < result.length; i++) {
				Document doc = searcher.doc(result[i].doc);
				documents.add(new TextDocument(doc.get(searchField)));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return documents;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SyntacticIndex#getSize()
	 */
	@Override
	public int getSize() {
		return indexReader.numDocs();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SyntacticIndex#count(java.lang.String)
	 */
	@Override
	public int count(String searchString) {
		try {
			Query query = parser.parse(searchString);
			TotalHitCountCollector results = new TotalHitCountCollector();
			searcher.search(query, results);
			return results.getTotalHits();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
