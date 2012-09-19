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
    
