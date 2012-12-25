package org.dllearner.kb.sparql;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class ConciseBoundedDescriptionGeneratorImpl implements ConciseBoundedDescriptionGenerator{
	
	private static final Logger logger = Logger.getLogger(ConciseBoundedDescriptionGeneratorImpl.class);
	
	private int chunkSize = 0;
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	private Model baseModel;
	
	private List<String> namespaces;
	private int maxRecursionDepth = 2;
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint, ExtractionDBCache cache, int maxRecursionDepth) {
		this.endpoint = endpoint;
		this.cache = cache;
		this.maxRecursionDepth = maxRecursionDepth;
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint endpoint) {
		this(endpoint, null);
	}
	
	public ConciseBoundedDescriptionGeneratorImpl(Model model) {
		this.baseModel = model;
	}
	
	public Model getConciseBoundedDescription(String resourceURI){
		return getConciseBoundedDescription(resourceURI, maxRecursionDepth);
	}
	
	public Model getConciseBoundedDescription(String resourceURI, int depth){
		return getModelChunked(resourceURI, depth);
	}
	
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	private Model getModelChunked(String resource, int depth){
		String query = makeConstructQueryOptional(resource, chunkSize, 0, depth);
		Model all = ModelFactory.createDefaultModel();
		try {
			Model model;
			if(cache == null){
				model = getModel(query);
			} else {
				model = cache.executeConstructQuery(endpoint, query);
			}
			all.add(model);
			int i = 1;
			do{
//			while(model.size() == CHUNK_SIZE){
				query = makeConstructQueryOptional(resource, chunkSize, i * chunkSize, depth);
				if(cache == null){
					model = getModel(query);
				} else {
					model = cache.executeConstructQuery(endpoint, query);
				}
				all.add(model);
				i++;
			} while(chunkSize > 0 && model.size() != 0);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		}
		
		return all;
	}
	
	@Override
	public void setRestrictToNamespaces(List<String> namespaces) {
		this.namespaces = namespaces;
	}
	
	@Override
	public void setRecursionDepth(int maxRecursionDepth) {
		this.maxRecursionDepth = maxRecursionDepth;
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param example The example resource for which a CONSTRUCT query is created.
	 * @return The JENA ARQ Query object.
	 */
	private String makeConstructQueryOptional(String resource, int limit, int offset, int depth){
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
//		sb.append("?p0 a ?type0.\n");
		for(int i = 1; i < depth; i++){
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
//			sb.append("?p").append(i).append(" ").append("a").append(" ").append("?type").append(i).append(".\n");
		}
		sb.append("}\n");
		sb.append("WHERE {\n");
		sb.append("<").append(resource).append("> ").append("?p0 ").append("?o0").append(".\n");
		sb.append(createNamespacesFilter("?p0"));
//		sb.append("?p0 a ?type0.\n");
		for(int i = 1; i < depth; i++){
			sb.append("OPTIONAL{\n");
			sb.append("?o").append(i-1).append(" ").append("?p").append(i).append(" ").append("?o").append(i).append(".\n");
//			sb.append("?p").append(i).append(" ").append("a").append(" ").append("?type").append(i).append(".\n");
			sb.append(createNamespacesFilter("?p" + i));
		}
		for(int i = 1; i < depth; i++){
			sb.append("}");
		}
		sb.append("}\n");
		if(chunkSize > 0){
			sb.append("LIMIT ").append(limit).append("\n");
			sb.append("OFFSET ").append(offset);
		}
		return sb.toString();
	}
	
	private String createNamespacesFilter(String targetVar){
		String filter = "";
		if(namespaces != null){
			filter += "FILTER(";
			for(Iterator<String> iter = namespaces.iterator(); iter.hasNext();){
				String ns = iter.next();
				filter += "(REGEX(STR(" + targetVar + "),'" + ns + "'))";
				if(iter.hasNext()){
					filter += " || ";
				}
			}
			filter += ")";
		}
		return filter;
	}
	
	private Model getModel(String query) throws UnsupportedEncodingException, SQLException{
		if(logger.isDebugEnabled()){
			logger.debug("Sending SPARQL query ...");
			logger.debug("Query:\n" + query.toString());
		}

		Model model;
		if(baseModel == null){
			if(cache == null){
				QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
				for (String dgu : endpoint.getDefaultGraphURIs()) {
					queryExecution.addDefaultGraph(dgu);
				}
				for (String ngu : endpoint.getNamedGraphURIs()) {
					queryExecution.addNamedGraph(ngu);
				}			
				model = queryExecution.execConstruct();
			} else {
				model = cache.executeConstructQuery(endpoint, query);
			}
		} else {
			model = QueryExecutionFactory.create(query, baseModel).execConstruct();
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("Got " + model.size() + " new triples in.");
		}
		return model;
	}
	
	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint.getEndpointDBpedia());
		cbdGen = new CachingConciseBoundedDescriptionGenerator(cbdGen);
//		cbdGen.setRestrictToNamespaces(Arrays.asList(new String[]{"http://dbpedia.org/ontology/", RDF.getURI(), RDFS.getURI()}));
		Model cbd = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Leipzig", 3);
		System.out.println(cbd.size());
	}

}
