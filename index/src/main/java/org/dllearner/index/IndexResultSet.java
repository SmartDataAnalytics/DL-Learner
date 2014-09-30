package org.dllearner.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;

/** @author Konrad Höffner
 *  
 */
@AllArgsConstructor
@NoArgsConstructor
//@Getter
@ToString
public class IndexResultSet extends TreeSet<IndexItem>
{
	private static final long	serialVersionUID	= 1L;

	Map<String,IndexItem> uriToItem = new TreeMap<>();

	public boolean contains(String uri) {return uriToItem.containsKey(uri);}
	public boolean contains(IndexItem item) {return this.contains(item.getUri());}

	@Override public boolean contains(Object o)
	{
		if(o instanceof String) {return this.contains((String)o);}
		if(o instanceof IndexItem) {return this.contains((IndexItem)o);}
		return false;
	}

	public IndexItem get(String uri) {return uriToItem.get(uri);}
	public IndexItem get(IndexItem item) {return uriToItem.get(item.getUri());}

	/** Replaces an existing item with the same URI, if the new score is higher 
	 * @see java.util.TreeSet#add(java.lang.Object)*/
	@Override public boolean add(IndexItem item)
	{		
		IndexItem existing;
		if((existing=get(item))==null||existing.getScore()<item.getScore())
		{
			if(existing!=null) {remove(existing);}			 
			uriToItem.put(item.getUri(),item);
			return super.add(item);
		}
		return false;
	}

	@Synchronized boolean remove(String uri)
	{
		IndexItem item = uriToItem.remove(uri);
		return super.remove(item);
	}

	@Synchronized public boolean remove(IndexItem item)
	{
		uriToItem.remove(item.getUri());
		return super.remove(item);
	}

	@Override @Synchronized public boolean remove(Object o)
	{
		if(o instanceof String) {return remove((String)o);}
		if(o instanceof IndexItem) {return remove((IndexItem)o);}
		return false;
	}

	@Override public boolean removeAll(Collection<?> c)
	{	
		int startSize = c.size();
		for(Object o:c) {remove(o);}
		return(startSize==c.size());
	}

	@Override public void clear()
	{
		uriToItem.clear();
		super.clear();
	}
	
	@Override public boolean addAll(Collection<? extends IndexItem> c)
	{
		boolean modified=false;
		for(IndexItem item: c) {modified=modified|add(item);}
		return modified;
	}

	public boolean retainAll(Collection<?> c) {throw new UnsupportedOperationException();}

	@Override public Iterator<IndexItem> iterator()
	{
		throw new UnsupportedOperationException();
	}
}