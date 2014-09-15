//package org.dllearner.common.index;
//
//import java.util.ArrayList;
//import java.util.List;
//import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.query.Syntax;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//
//public class CopyOfSPARQLModelIndex extends Index{
//
//	private Model model;
//
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
//
//	public CopyOfSPARQLModelIndex(Model model) {
//		this.model = model;
//	}
//
//	@Override
//	public List<String> getResources(String searchTerm, int limit, int offset) {
//		List<String> resources = new ArrayList<String>();
//
//		String query = String.format(queryTemplate, searchTerm, limit, offset);
//
//		ResultSet rs = executeSelect(query);
//
//		QuerySolution qs;
//		while(rs.hasNext()){
//			qs = rs.next();
//			RDFNode uriNode = qs.get("uri");
//			if(uriNode.isURIResource()){
//				resources.add(uriNode.asResource().getURI());
//			}
//		}
//		return resources;
//	}
//
//	@Override
//	public IndexResultSet getResourcesWithScores(String searchTerm, int limit, int offset) {
//		IndexResultSet irs = new IndexResultSet();
//
//		String query = String.format(queryWithLabelTemplate, searchTerm, limit, offset);
//
//		ResultSet rs = executeSelect(query);
//
//		QuerySolution qs;
//		while(rs.hasNext()){
//			qs = rs.next();
//			RDFNode uriNode = qs.get("uri");
//			if(uriNode.isURIResource()){
//				RDFNode labelNode = qs.get("label");
//
//				String uri = uriNode.asResource().getURI();
//				String label = labelNode.asLiteral().getLexicalForm();
//				irs.addItem(new IndexResultItem(uri, label, 1f));
//			}
//		}
//
//		return irs;
//	}
//
//	private ResultSet executeSelect(String query){
//		ResultSet rs;
//		rs = QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model).execSelect();		
//		return rs;
//	}
//
//	public Model getModel() {
//		return model;
//	}
//
//}