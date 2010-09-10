
package org.dllearner.algorithms.isle;

import java.io.File;
import java.io.FileReader;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


public class LuceneDocument {

	public static Document Document( File f ) throws java.io.FileNotFoundException {
		Document doc = new Document();
		doc.add( new Field( "path", f.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
		doc.add( new Field( "modified",
						   DateTools.timeToString(f.lastModified(), DateTools.Resolution.MINUTE),
						   Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add( new Field( "contents", new FileReader(f) ) );
		return doc;
	}
}
    
