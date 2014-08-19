package org.dllearner.common.index;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import lombok.AllArgsConstructor;

/** doesn't handle offsets > 0*/
public class WordNetIndex extends Index
{
	static final Dictionary d;
	static
	{
		try {d = Dictionary.getDefaultResourceInstance();}
		catch (JWNLException e) {throw new RuntimeException(e);}
	}

	final Index index;	

	protected static final float WORDNET_PENALTY = 0.67f; // wild guess, modify empirically later

	public WordNetIndex(Index index) {this.index=index;}


	@Override public SortedSet<IndexItem> getResourcesWithScores(String queryString, int limit, int offset)
	{		
		SortedSet<IndexItem> resources = index.getResourcesWithScores(queryString, limit, offset);
		if(resources.size()>=limit&&resources.last().getScore()==1f) return resources; // already perfect, no wordnet necessary
		int MAX_ADDITIONS = resources.tailSet(new IndexItem("","",WORDNET_PENALTY)).size();
		// ugly construct to check if url is already there
		SortedSet<IndexItem> itemsByUri = new TreeSet<IndexItem>
		(new Comparator<IndexItem>(){public int compare(IndexItem i1, IndexItem i2)
		{return i1.getUri().compareTo(i2.getUri());};});
		itemsByUri.addAll(resources);

		try
		{		
			Set<Synset> synsets = new HashSet<>();

			for(POS pos : new POS[] {POS.NOUN,POS.VERB})
			{synsets.addAll(d.getIndexWord(pos, queryString).getSenses());}

			Set<String> lemmas = new HashSet<>();

			for(Synset synset: synsets)
			{for(Word word: synset.getWords()) lemmas.add(word.getLemma());}

			for(String lemma: lemmas)
			{
				SortedSet<IndexItem> newResources = index.getResourcesWithScores(lemma, MAX_ADDITIONS, offset);
				for(IndexItem item: newResources)
				{
					IndexItem newItem = new IndexItem(item.getUri(),item.getLabel(), item.getScore()*WORDNET_PENALTY);

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

			//		MorphologicalProcessor x = new DefaultMorphologicalProcessor(d, params);
			// TODO Auto-generated method stub
			return resources;
		}
		catch (JWNLException e) {throw new RuntimeException(e);}
	}

}