package org.dllearner.kb.sparql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

public class CachingConciseBoundedDescriptionGenerator implements ConciseBoundedDescriptionGenerator{
	
	private Map<String, Model> cache;
	private ConciseBoundedDescriptionGenerator cbdGen;
	
	public CachingConciseBoundedDescriptionGenerator(ConciseBoundedDescriptionGenerator cbdGen) {
		this.cbdGen = cbdGen;
		cache = new HashMap<String, Model>();
	}
	
	public Model getConciseBoundedDescription(String resourceURI){
		Model cbd = cache.get(resourceURI);
		if(cbd == null){
			cbd = cbdGen.getConciseBoundedDescription(resourceURI);
			cache.put(resourceURI, cbd);
		}
		return cbd;
	}
	
	public Model getConciseBoundedDescription(String resourceURI, int depth){
		Model cbd = cache.get(resourceURI);
		if(cbd == null){
			cbd = cbdGen.getConciseBoundedDescription(resourceURI, depth);
			cache.put(resourceURI, cbd);
		}
		return cbd;
	}

	@Override
	public void setRestrictToNamespaces(List<String> namespaces) {
		cbdGen.setRestrictToNamespaces(namespaces);
	}
	
	@Override
	public void setRecursionDepth(int maxRecursionDepth) {
		cbdGen.setRecursionDepth(maxRecursionDepth);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	@Override
	public Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs) {
		Model cbd = cache.get(resourceURI);
		if(cbd == null){
			cbd = cbdGen.getConciseBoundedDescription(resourceURI, depth, withTypesForLeafs);
			cache.put(resourceURI, cbd);
		}
		return cbd;
	}

}
