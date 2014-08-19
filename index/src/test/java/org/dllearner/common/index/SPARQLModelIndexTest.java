package org.dllearner.common.index;

import static org.junit.Assert.assertTrue;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.junit.Test;

public class SPARQLModelIndexTest
{

	@Test public void testCreateClassIndex()
	{
		SPARQLModelIndex index = SPARQLModelIndex.createClassIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");
		assertTrue(index.getResources("City").contains("http://linkedgeodata.org/ontology/City"));
	}

	@Test public void testCreatePropertyIndex()
	{
		SPARQLModelIndex index = SPARQLModelIndex.createPropertyIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");
//		System.out.println(index.getResources("Restaurant"));
		assertTrue(index.getResources("Restaurant").contains("http://linkedgeodata.org/ontology/Restaurant"));
	}
	
	@Test public void extJwnlTest() throws JWNLException
	{
		Dictionary d = Dictionary.getDefaultResourceInstance();
//		
		IndexWord iw = d.getIndexWord(POS.NOUN, "airport");
		for(Synset sense: iw.getSenses())
		{
			System.out.println(sense);
			for(Word word: sense.getWords()) System.out.println(word.getLemma());	
		}
//		System.out.println(d.getSynsetAt(POS.NOUN, "airport"));
	}
	
	@Test public void testWordNetIndex()
	{ 
		SPARQLModelIndex index = SPARQLModelIndex.createClassIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");
		WordNetIndex wIndex = new WordNetIndex(index);
		System.out.println(wIndex.getResources("cities"));
		System.out.println(wIndex.getResources("aerodromes"));
	}

}