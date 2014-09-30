package org.dllearner.index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SPARQLModelIndex extends Index{

	//	private Model model;
	private Dataset dataset;

	final float minSimilarity;

	public static final ParameterizedSparqlString queryTemplate = new ParameterizedSparqlString(
			"PREFIX text: <http://jena.apache.org/text#>"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
					//					+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
					+ "SELECT ?s ?label { ?s text:query (rdfs:label ?l 10) ;     rdfs:label ?label}");

	private static final float	FUZZY_MULTIPLIER	= 0.8f; 

	//	protected String queryTemplate = "SELECT DISTINCT ?uri WHERE {\n" +
	//			"?uri a ?type.\n" + 
	//			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label\n" +
	//			"FILTER(REGEX(STR(?label), '%s'))}\n" +
	//			"LIMIT %d OFFSET %d";
	//
	//	protected String queryWithLabelTemplate = "SELECT DISTINCT ?uri ?label WHERE {\n" +
	//			"?uri a ?type.\n" + 
	//			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label\n" +
	//			"FILTER(REGEX(STR(?label), '%s'))}\n" +
	//			"LIMIT %d OFFSET %d";

	/** Create an index using your own model saving you the time needed to import the model when using an endpoint.
	 * If you only have an endpoint or want to index a subset of the triples,
	 * use the static methods {@link #createIndex(String, String, List)}, {@link #createClassIndex(String, String)} or {@link #createPropertyIndex(String, String)}.
	 * All triples (uri,rdfs:label,label) will be put into the index.   
	 * @param model the jena model containing the rdf:label statements that you want to index. Changes to the model after the construtor call are probably not indexed.
	 * @param minSimilarity Between 0 (maximum fuzzyness) and 1f (no fuzzy matching).
	 */
	public SPARQLModelIndex(Model model,float minSimilarity)
	{
		this.minSimilarity=minSimilarity;
		Dataset ds1 = DatasetFactory.createMem() ;

		EntityDefinition entDef = new EntityDefinition("uri", "text", RDFS.label) ;
		// Lucene, in memory.
		Directory dir =  new RAMDirectory();
		// Join together into a dataset
		dataset = TextDatasetFactory.createLucene(ds1, dir, entDef);
		//		ds.setDefaultModel(model);

		synchronized(model)
		{
			dataset.begin(ReadWrite.WRITE);
			try {
				dataset.getDefaultModel().add(model);
				dataset.commit();
			} finally {
				dataset.end();
			}
		}
		//		this.model = model;
	}

	private Map<String,IndexItem> getResourceMap(String searchTerm, int limit, float scoreAssignement)
	{
		Map<String,IndexItem> items = new HashMap<>();
		queryTemplate.setLiteral("l", searchTerm);
		ResultSet rs = executeSelect(queryTemplate.toString());

		QuerySolution qs;
		while(rs.hasNext())
		{
			qs = rs.next();
			RDFNode uriNode = qs.get("s");
			if(uriNode.isURIResource()){
				RDFNode labelNode = qs.get("label");

				String uri = uriNode.asResource().getURI();
				String label = labelNode.asLiteral().getLexicalForm();
				items.put(uri,new IndexItem(uri, label, scoreAssignement));
			}
		}
		return items;
	}

	@Override
	public IndexResultSet getResourcesWithScores(String searchTerm, int limit)
	{
		Map<String,IndexItem> itemMap = getResourceMap(searchTerm, limit, offset,1f);
		IndexResultSet items = new TreeSet<IndexItem>(itemMap.values());
		if(minSimilarity<1f)
		{
			Map<String,IndexItem> fuzzyItems = getResourceMap(searchTerm+'~', limit, offset, FUZZY_MULTIPLIER);
			Set<String> newUris = fuzzyItems.keySet();
			newUris.removeAll(itemMap.keySet());			
			for(String newUri: newUris) {items.add(fuzzyItems.get(newUri));}
		}
		return items;
	}

	private ResultSet executeSelect(String query) {
		dataset.begin(ReadWrite.READ);
		try {
			ResultSet rs = QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), dataset).execSelect();
			return rs;
		} finally {
			dataset.end();
		}
	}
	

	//	public Model getModel() {
	//		return model;
	//	}

	//	static SPARQLModelIndex createOldClassIndex(String endpoint)
	//	{
	//		QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp(endpoint);
	//
	//		ListService<Concept,Node> classService = new ListServiceConcept(qef);
	//		List<Node> classNodes = classService.fetchData(ConceptUtils.listDeclaredClasses,null,null);
	//		Concept concept = Concept.create("?s ?p ?l . Filter(?p = <http://www.w3.org/2000/01/rdf-schema#label>)", "s");
	//
	//		Var l = Var.alloc("l");
	//		Query query = concept.asQuery();
	//		query.getProject().add(l);
	//		MappedConcept mappedConcept = new MappedConcept(concept, new AggLiteral(new ExprVar( l )));
	//		LookupService<Node, NodeValue> labelService = LookupServiceUtils.createGeoLookupService(qef, mappedConcept);
	//		labelService = LookupServicePartition.create(labelService, 30);
	//
	//		Map<Node, NodeValue> classLabels = labelService.lookup(classNodes);
	//
	//		Model model = ModelFactory.createDefaultModel();
	//		for(Entry<Node, NodeValue> entry:classLabels.entrySet())
	//		{
	//			model.add(model.asStatement(new Triple(entry.getKey(), RDFS.label.asNode(), entry.getValue().asNode())));
	//		}
	//		return new SPARQLModelIndex(model);
	//	}

	public static SPARQLModelIndex createPropertyIndex(String endpoint, String defaultGraph,float minSimilarity)
	{
		return createIndex(endpoint, defaultGraph, Lists.newArrayList(RDF.Property,OWL.DatatypeProperty,OWL.ObjectProperty),minSimilarity);
	}

	public static SPARQLModelIndex createClassIndex(String endpoint, String defaultGraph,float minSimilarity)
	{		
		return createIndex(endpoint, defaultGraph, Lists.newArrayList(OWL.Class,RDFS.Class),minSimilarity);		
	}

	public static SPARQLModelIndex createIndex(String endpoint, String defaultGraph,List<Resource> types,float minSimilarity)
	{
		return createIndex(new org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp(endpoint, defaultGraph),types,minSimilarity);
	}

	/**
	 * @param type fully qualified type without prefixes, e.g. http://www.w3.org/2002/07/owl#Class
	 */
	static SPARQLModelIndex createIndex(org.aksw.jena_sparql_api.core.QueryExecutionFactory qef,List<Resource> types,float minSimilarity)
	{
		// filter for the label properties
		List<Property> labelProperties = Lists.newArrayList(RDFS.label);
		String labelValues = "<" + labelProperties.get(0) + ">";
		for (int i = 1; i < labelProperties.size(); i++) {
			labelValues += "," + "<" + labelProperties.get(i) + ">";
		}

		String typeValues = "<" + types.get(0) + ">";
		for (int i = 1; i < types.size(); i++) {
			typeValues += "," + "<" + types.get(i) + ">";
		}

		// filter for the languages
		List<String> languages = Lists.newArrayList("en");
		String languagesFilter = "FILTER(";
		languagesFilter += "LANGMATCHES(LANG(?l),'" + languages.get(0) + "') ";
		for (int i = 1; i < labelProperties.size(); i++) {
			languagesFilter += "|| LANGMATCHES(LANG(?l),'" + languages.get(i) + "') ";
		}
		languagesFilter += ")";

		//SPARQL 1.1 VALUES based
		String languageValues = "VALUES ?lang {";
		for (String lang : languages) {
			languageValues += "\"" + lang + "\" ";
		}
		languageValues += "}";
		//		languagesFilter = languageValues + " FILTER(LANGMATCHES(LANG(?l), ?lang))";

		String query = "CONSTRUCT {?s a ?type .?s ?p_label ?l .}"
				+ " WHERE "
				+ "{?s a ?type . FILTER(?type IN ("+typeValues+"))"
				+ "OPTIONAL{?s ?p_label ?l . FILTER(?p_label IN (" + labelValues + "))"
				+ languagesFilter + "}}";
		System.out.println(QueryFactory.create(query));

		QueryExecution qe = qef.createQueryExecution(query);
		Model model = qe.execConstruct();
		qe.close();

		return new SPARQLModelIndex(model,minSimilarity);
	}

}