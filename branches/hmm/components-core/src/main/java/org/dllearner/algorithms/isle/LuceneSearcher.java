/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.dllearner.algorithms.isle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneSearcher {
	
	private String INDEX = "index";
	private String FIELD = "contents";
	
	private IndexReader m_reader = null;
	private Searcher m_searcher = null;
	private Analyzer m_analyzer = null;
	private QueryParser m_parser = null;
	
	private Map<Document,Float> m_results = null;
	

	public static void main( String[] args ) throws Exception {
		String sQuery = args[0];
		LuceneSearcher searcher = new LuceneSearcher();
		List<Document> docs = searcher.search( sQuery );
		System.out.println( "\nquery='"+ sQuery +"' all="+ searcher.indexSize() +" hits="+ docs.size() );
		for( Document doc : docs )
		{
//			String sDoc = doc.toString();
			float score = searcher.getScore( doc );
			System.out.println( "score="+ score +" doc="+ doc );
		}
	}
	
	@SuppressWarnings("deprecation")
	public LuceneSearcher() throws Exception {
		m_reader = IndexReader.open( FSDirectory.open( new File( INDEX ) ), true );
		m_searcher = new IndexSearcher( m_reader );
		m_analyzer = new StandardAnalyzer( Version.LUCENE_CURRENT );
		m_parser = new QueryParser( Version.LUCENE_CURRENT, FIELD, m_analyzer );
	}
	
	public void close() throws Exception {
		m_reader.close();
	}
	
	public int indexSize(){
		return m_reader.numDocs();
	}
	
	public List<Document> search( String sQuery ) throws Exception {	
		m_results = new HashMap<Document,Float>();
		Query query = m_parser.parse( sQuery );
		search( query );
		// m_reader.close();
		return getDocuments();
	}
	
	public int count( String sQuery ) throws Exception {
		return search( sQuery ).size();
	}
	
	public List<Document> getDocuments(){
		List<Document> docs = new ArrayList<Document>();
		for( Document doc: m_results.keySet() ){
			docs.add( doc );
		}
		Collections.sort( docs, new Comparator<Document>(){
			public int compare( Document d1, Document d2 ){
				float s1 = getScore( d1 );
				float s2 = getScore( d2 );
				if( s1 > s2 ) return -1;
				else if( s1 < s2 ) return 1;
				return 0;
			}
			@Override
			public boolean equals( Object obj ){
				return false;
			}
		} );
		return docs;
	}
	
	public float getScore( Document doc ){
		return m_results.get( doc );
	}

	private void search( Query query ) throws IOException {
		@SuppressWarnings("unused")
		Collector collector = new Collector() 
		{
			private Scorer scorer;
			private int docBase;
			private Map<Document,Float> results = new HashMap<Document,Float>();
      
			@Override
			public void collect(int doc) throws IOException {
				// System.out.println("doc=" + doc + docBase + " score=" + scorer.score());
				m_results.put( m_searcher.doc( doc ), scorer.score() );
			}
			@Override
			public boolean acceptsDocsOutOfOrder() {
				return true;
			}
			@Override
			public void setNextReader( IndexReader reader, int docBase ) throws IOException {
				this.docBase = docBase;
			}
			@Override
			public void setScorer(Scorer scorer) throws IOException {
				this.scorer = scorer;
			}
		};
		m_searcher.search( query, collector );
	}
}
