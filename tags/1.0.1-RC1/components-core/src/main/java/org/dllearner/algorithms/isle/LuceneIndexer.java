
package org.dllearner.algorithms.isle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class LuceneIndexer {

	static final File INDEX = new File( "index" );

	public static void main( String[] args ) {
		if( INDEX.exists() ) 
		{
			System.out.println("<delete index!>");
			System.exit(1);
		}
//		final File docDir = new File( args[0] );
//		LuceneIndexer indexer = new LuceneIndexer( docDir );
	}
	
	@SuppressWarnings("deprecation")
	public LuceneIndexer( File docDir ){
		System.out.println( "LuceneIndex: "+ docDir );
		Date start = new Date();
		try {

			IndexWriter writer = new IndexWriter( FSDirectory.open( INDEX ), 
										new StandardAnalyzer( Version.LUCENE_CURRENT ), true, IndexWriter.MaxFieldLength.LIMITED );
			System.out.println( "Creating index ..." );
			index( writer, docDir );
			System.out.println( "Optimizing index ..." );
			writer.optimize();
			writer.close();
			Date end = new Date();
			System.out.println( end.getTime() - start.getTime() + " total milliseconds" );
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void index( IndexWriter writer, File file ) throws IOException {
		// System.out.println( "LuceneIndexer.index: "+ file );
		if( file.canRead() ) 
		{
			if( file.isDirectory() ) 
			{
				String[] files = file.list();
				if( files != null )
				{
					for( int i = 0; i < files.length; i++ ) {
						index( writer, new File( file, files[i] ) );
					}
				}
			} 
			else {
				// System.out.println( "Indexer.index: adding " + file );
				try {
					writer.addDocument( LuceneDocument.Document( file ) );
				}
				catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace();
				}
			}
		}
		else {
			System.out.println( "<cannot read file!>" );
		}
	}
  
}
