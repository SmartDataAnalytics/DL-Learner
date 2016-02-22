package org.dllearner.index;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

/** doesn't handle offsets > 0*/
public class WordNetIndex extends Index
{
	static final Dictionary d;
//	static final MorphologicalProcessor morphy;
	static
	{
		try	{d = Dictionary.getDefaultResourceInstance();}
		catch (JWNLException e) {throw new RuntimeException(e);}
	}

	final Index index;	

	protected static final float WORDNET_PENALTY_FACTOR = 0.9f; // wild guess, modify empirically later

	public WordNetIndex(Index index) {this.index=index;}

	@Override public IndexResultSet getResourcesWithScores(String queryString, int limit)
	{		
		IndexResultSet resources = index.getResourcesWithScores(queryString, limit);
		if(resources.size()>=limit&&resources.last().getScore()==1f) return resources; // already perfect, no wordnet necessary
		int MAX_ADDITIONS = resources.tailSet(new IndexItem("","",WORDNET_PENALTY_FACTOR)).size();
		// ugly construct to check if url is already there
		IndexResultSet itemsByUri = new TreeSet<IndexItem>
		(new Comparator<IndexItem>(){public int compare(IndexItem i1, IndexItem i2)
		{return i1.getUri().compareTo(i2.getUri());};});
		itemsByUri.addAll(resources);

		try
		{		
			Set<Synset> synsets = new HashSet<>();			

			for(IndexWord iw: d.lookupAllIndexWords(queryString).getIndexWordArray())
			{
				synsets.addAll(iw.getSenses());
			}	

			Set<String> lemmas = new HashSet<>();

			for(Synset synset: synsets)
			{for(Word word: synset.getWords()) lemmas.add(word.getLemma());}

			for(String lemma: lemmas)
			{
				IndexResultSet newResources = index.getResourcesWithScores(lemma, MAX_ADDITIONS);
				for(IndexItem item: newResources)
				{
					IndexItem newItem = new IndexItem(item.getUri(),item.getLabel(), item.getScore()*WORDNET_PENALTY_FACTOR);

					if(itemsByUri.contains(newItem))
					{
						// TODO: replace if higher score
					} else
					{
						resources.add(newItem);
						itemsByUri.add(newItem);
					}
				}
			}				
			// 
			return resources;
		}
		catch (JWNLException e) {throw new RuntimeException(e);}
	}

}