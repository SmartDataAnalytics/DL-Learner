package org.dllearner.index;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Sorted from highest to lowest score, then by uri. For instance, SortedSet.first() gives an item with the highest score. */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class IndexItem implements Comparable<IndexItem>
{	
	private String uri;
	private String label;
	private float score;
	
	@Override public int compareTo(IndexItem item)
	{
		int i = -Float.compare(score, item.score);
		if(i==0) {i=uri.compareTo(item.uri);}
		return i;
	}
}