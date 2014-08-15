package org.dllearner.common.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceConcept;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServicePartition;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.AggLiteral;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SPARQLModelIndex extends Index{

	private Model model;
	private Dataset dataset;

	public static final ParameterizedSparqlString queryTemplate = new ParameterizedSparqlString(
			"PREFIX text: <http://jena.apache.org/text#>"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
					+ "SELECT ?s { ?s text:query (rdfs:label ?l 10) ;     rdfs:label ?label}"); 

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

	public SPARQLModelIndex(Model model) {
		Dataset ds1 = DatasetFactory.createMem() ;
		EntityDefinition entDef = new EntityDefinition("uri", "text", RDFS.label) ;
		// Lucene, in memory.
		Directory dir =  new RAMDirectory();
		// Join together into a dataset
		Dataset ds = TextDatasetFactory.createLucene(ds1, dir, entDef);
		ds.setDefaultModel(model);		

		this.model = model;
	}

	@Override
	public List<String> getResources(String searchTerm, int limit, int offset) {
		List<String> resources = new ArrayList<String>();

		queryTemplate.setLiteral("l", searchTerm);
		ResultSet rs = executeSelect(queryTemplate.toString());

		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			RDFNode uriNode = qs.get("uri");
			if(uriNode.isURIResource()){
				resources.add(uriNode.asResource().getURI());
			}
		}
		return resources;
	}

	@Override
	public IndexResultSet getResourcesWithScores(String searchTerm, int limit, int offset) {
		IndexResultSet irs = new IndexResultSet();

		queryTemplate.setLiteral("l", searchTerm);
		ResultSet rs = executeSelect(queryTemplate.toString());

		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			RDFNode uriNode = qs.get("uri");
			if(uriNode.isURIResource()){
				RDFNode labelNode = qs.get("label");

				String uri = uriNode.asResource().getURI();
				String label = labelNode.asLiteral().getLexicalForm();
				irs.addItem(new IndexResultItem(uri, label, 1f));
			}
		}
		return irs;
	}

	private ResultSet executeSelect(String query){
		ResultSet rs;
		rs = QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), dataset).execSelect();		
		return rs;
	}

	public Model getModel() {
		return model;
	}

	static SPARQLModelIndex createClassIndex(String endpoint)
	{
		QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp(endpoint);

		ListService<Concept,Node> classService = new ListServiceConcept(qef);
		List<Node> classNodes = classService.fetchData(ConceptUtils.listDeclaredClasses,null,null);
		Concept concept = Concept.create("?s ?p ?l . Filter(?p = <http://www.w3.org/2000/01/rdf-schema#label>)", "s");

		Var l = Var.alloc("l");
		Query query = concept.asQuery();
		query.getProject().add(l);
		MappedConcept mappedConcept = new MappedConcept(concept, new AggLiteral(new ExprVar( l )));
		LookupService<Node, NodeValue> labelService = LookupServiceUtils.createGeoLookupService(qef, mappedConcept);
		labelService = LookupServicePartition.create(labelService, 30);

		Map<Node, NodeValue> classLabels = labelService.lookup(classNodes);

		Model model = ModelFactory.createDefaultModel();
		for(Entry<Node, NodeValue> entry:classLabels.entrySet())
		{
			model.add(model.asStatement(new Triple(entry.getKey(), RDFS.label.asNode(), entry.getValue().asNode())));
		}
		return new SPARQLModelIndex(model);
	}

}