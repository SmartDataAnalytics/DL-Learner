package org.dllearner.index;

import static org.junit.Assert.assertTrue;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import org.dllearner.index.SPARQLModelIndex;
import org.dllearner.index.WordNetIndex;
import org.junit.Test;

public class SPARQLModelIndexTest
{

	@Test public void testCreateClassIndex()
	{
		SPARQLModelIndex index = SPARQLModelIndex.createClassIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org",0);
		assertTrue(index.getResources("City").contains("http://linkedgeodata.org/ontology/City"));
	}

	@Test public void testCreatePropertyIndex()
	{
		SPARQLModelIndex index = SPARQLModelIndex.createPropertyIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org",0);
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
		SPARQLModelIndex index = SPARQLModelIndex.createClassIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org",0);
		WordNetIndex wIndex = new WordNetIndex(index);		
		assertTrue(wIndex.getResources("cities").contains("http://linkedgeodata.org/ontology/City"));
		assertTrue(wIndex.getResources("aerodromes").contains("http://linkedgeodata.org/ontology/Airport"));
	}
	
	@Test public void testFuzzy()
	{ 
		SPARQLModelIndex index = SPARQLModelIndex.createClassIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org",0.5f);
		assertTrue(index.getResources("mirporg").get(0).equals("http://linkedgeodata.org/ontology/Airport"));
	}
}