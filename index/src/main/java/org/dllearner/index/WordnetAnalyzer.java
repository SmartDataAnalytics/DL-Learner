/**
 * 
 */
package org.dllearner.index;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.apache.lucene.util.Version;

/**
 * @author Lorenz Buehmann
 *
 */
public class WordnetAnalyzer extends Analyzer{
	
	private String wordnetDirectory;

	public WordnetAnalyzer(String wordnetDirectory) {
		this.wordnetDirectory = wordnetDirectory;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.Analyzer#createComponents(java.lang.String, java.io.Reader)
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		Tokenizer source = new ClassicTokenizer(Version.LUCENE_4_9, reader);
	    TokenStream filter = new StandardFilter(Version.LUCENE_4_9, source);
	    filter = new LowerCaseFilter(Version.LUCENE_4_9,filter);
	    try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
			analyzer = new Analyzer(){
				  protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
				    final StandardTokenizer src = new StandardTokenizer(Version.LUCENE_4_9, reader);
				    src.setMaxTokenLength(255);
				    TokenStream tok = new StandardFilter(Version.LUCENE_4_9, src);
				    tok = new LowerCaseFilter(Version.LUCENE_4_9, tok);
//				    tok = new StopFilter(Version.LUCENE_4_9, tok, stopwords);
				    return new TokenStreamComponents(src, tok);
				  }
			};
			WordnetSynonymParser wordnetSynonymParser = new WordnetSynonymParser(true, true, analyzer);
			wordnetSynonymParser.parse(new FileReader(wordnetDirectory));
//			filter = new SynonymFilter(filter, wordnetSynonymParser.build(), false);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	    //Whatever other filter you want to add to the chain, being mindful of order.
	    return new TokenStreamComponents(source, filter);
	}

}
