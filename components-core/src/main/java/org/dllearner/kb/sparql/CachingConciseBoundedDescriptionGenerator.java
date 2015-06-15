package org.dllearner.kb.sparql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;

public class CachingConciseBoundedDescriptionGenerator implements ConciseBoundedDescriptionGenerator{
	
	private Map<String, Model> cache;
	private ConciseBoundedDescriptionGenerator delegatee;
	
	public CachingConciseBoundedDescriptionGenerator(ConciseBoundedDescriptionGenerator cbdGen) {
		this.delegatee = cbdGen;
		cache = new HashMap<String, Model>();
	}
	
	public Model getConciseBoundedDescription(String resourceURI){
		Model cbd = cache.get(resourceURI);
		if(cbd == null){
			cbd = delegatee.getConciseBoundedDescription(resourceURI);
			cache.put(resourceURI, cbd);
		}
		return cbd;
	}
	
	public Model getConciseBoundedDescription(String resourceURI, int depth){
		Model cbd = cache.get(resourceURI);
		if(cbd == null){
			cbd = delegatee.getConciseBoundedDescription(resourceURI, depth);
			cache.put(resourceURI, cbd);
		}
		return cbd;
	}

	@Override
	public void addAllowedPropertyNamespaces(Set<String> namespaces) {
		delegatee.addAllowedPropertyNamespaces(namespaces);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#addAllowedObjectNamespaces(java.util.Set)
	 */
	@Override
	public void addAllowedObjectNamespaces(Set<String> namespaces) {
		delegatee.addAllowedObjectNamespaces(namespaces);
	}
	
	@Override
	public void setRecursionDepth(int maxRecursionDepth) {
		delegatee.setRecursionDepth(maxRecursionDepth);
	}
	
	public void addPropertiesToIgnore(Set<String> properties) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs) {
		Model cbd = cache.get(resourceURI);
		if(cbd == null){
			cbd = delegatee.getConciseBoundedDescription(resourceURI, depth, withTypesForLeafs);
			cache.put(resourceURI, cbd);
		}
		return cbd;
	}

	

}
