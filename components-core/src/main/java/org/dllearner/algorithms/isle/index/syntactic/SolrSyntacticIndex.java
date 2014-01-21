/**
 * 
 */
package org.dllearner.algorithms.isle.index.syntactic;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dllearner.algorithms.isle.TextDocumentGenerator;
import org.dllearner.algorithms.isle.index.AnnotatedDocument;
import org.dllearner.algorithms.isle.index.AnnotatedTextDocument;
import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.algorithms.isle.index.Token;
import org.dllearner.algorithms.isle.textretrieval.AnnotationEntityTextRetriever;
import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.base.Joiner;

/**
 * @author Lorenz Buehmann
 *
 */
public class SolrSyntacticIndex implements Index{
	
	private SolrServer solr;
	private AnnotationEntityTextRetriever textRetriever;
	private String searchField;
	private String typesField = "types";
	
	long totalNumberOfDocuments = -1;
	
	Map<Entity, Long> cache = new HashMap<>();
	
	public SolrSyntacticIndex(OWLOntology ontology, String solrServerURL, String searchField) {
		this.searchField = searchField;
		solr = new HttpSolrServer(solrServerURL);
		textRetriever = new RDFSLabelEntityTextRetriever(ontology);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getDocuments(org.dllearner.core.owl.Entity)
	 */
	@Override
	public Set<AnnotatedDocument> getDocuments(Entity entity) {
		Set<AnnotatedDocument> documents = new HashSet<AnnotatedDocument>();
		
		Map<List<Token>, Double> relevantText = textRetriever.getRelevantText(entity);
		
		for (Entry<List<Token>, Double> entry : relevantText.entrySet()) {
			List<Token> tokens = entry.getKey();
			for (Token token : tokens) {
				SolrQuery query = new SolrQuery(searchField + ":" + token.getRawForm());
				query.setRows(Integer.MAX_VALUE);//can be very slow
		    	try {
					QueryResponse response = solr.query(query);
					SolrDocumentList list = response.getResults();
					for (SolrDocument doc : list) {
						String uri = (String) doc.getFieldValue("uri");
						String comment = (String) doc.getFieldValue(searchField);
						
						documents.add(new AnnotatedTextDocument(
								TextDocumentGenerator.getInstance().generateDocument((String) doc.getFieldValue(searchField)), 
								Collections.EMPTY_SET));
					}
				} catch (SolrServerException e) {
					e.printStackTrace();
				}
			}
		}
		return documents;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getTotalNumberOfDocuments()
	 */
	@Override
	public long getTotalNumberOfDocuments() {
		if(totalNumberOfDocuments == -1){
			SolrQuery q = new SolrQuery("*:*");
		    q.setRows(0);  // don't actually request any data
		    try {
				totalNumberOfDocuments = solr.query(q).getResults().getNumFound();
			} catch (SolrServerException e) {
				e.printStackTrace();
			}
		}
		return totalNumberOfDocuments;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getNumberOfDocumentsFor(org.dllearner.core.owl.Entity)
	 */
	@Override
	public long getNumberOfDocumentsFor(Entity entity) {
		if(cache.containsKey(entity)){
			return cache.get(entity);
		}
		Map<List<Token>, Double> relevantText = textRetriever.getRelevantText(entity);
		
		String queryString = "(";
		Set<String> terms = new HashSet<>();
		for (Entry<List<Token>, Double> entry : relevantText.entrySet()) {
			List<Token> tokens = entry.getKey();
			String phrase = "";
			for (Token token : tokens) {
//				terms.add(token.getRawForm());
				phrase += token.getRawForm() + " ";
			}
			phrase.trim();
			terms.add(quotedString(phrase));
		}
		queryString += Joiner.on("OR").join(terms);
		queryString += ")";
		
		SolrQuery query = new SolrQuery(searchField + ":" + queryString);//System.out.println(query);
    	try {
			QueryResponse response = solr.query(query);
			SolrDocumentList list = response.getResults();
			cache.put(entity, list.getNumFound());
			return list.getNumFound();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getNumberOfDocumentsFor(org.dllearner.core.owl.Entity[])
	 */
	@Override
	public long getNumberOfDocumentsFor(Entity... entities) {
		
		Set<String> queryStringParts = new HashSet<>();
		
		for (Entity entity : entities) {
			Map<List<Token>, Double> relevantText = textRetriever.getRelevantText(entity);
			
			String queryString = "(";
			Set<String> terms = new HashSet<>();
			for (Entry<List<Token>, Double> entry : relevantText.entrySet()) {
				List<Token> tokens = entry.getKey();
				String phrase = "";
				for (Token token : tokens) {
//					terms.add(token.getRawForm());
					phrase += token.getRawForm() + " ";
				}
				phrase.trim();
				terms.add(quotedString(phrase));
			}
			queryString += Joiner.on("OR").join(terms);
			queryString += ")";
			queryStringParts.add(queryString);
		}
		
		String queryStringConjuction = "(" + Joiner.on("AND").join(queryStringParts) + ")";
		
		
		SolrQuery query = new SolrQuery(searchField + ":" + queryStringConjuction);//System.out.println(query);
    	try {
			QueryResponse response = solr.query(query);
			SolrDocumentList list = response.getResults();
			return list.getNumFound();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	
	public long getNumberOfDocumentsForTyped(NamedClass resourceClass, Entity entity) {
		
		
		Map<List<Token>, Double> relevantText = textRetriever.getRelevantText(entity);
		
		String queryString = "(";
		Set<String> terms = new HashSet<>();
		for (Entry<List<Token>, Double> entry : relevantText.entrySet()) {
			List<Token> tokens = entry.getKey();
			String phrase = "";
			for (Token token : tokens) {
//				terms.add(token.getRawForm());
				phrase += token.getRawForm() + " ";
			}
			phrase.trim();
			terms.add(quotedString(phrase));
		}
		queryString += Joiner.on("OR").join(terms);
		queryString += ")";System.out.println(queryString);
		
		SolrQuery query = new SolrQuery(
				searchField + ":" + queryString + " AND " + typesField + ":" + quotedString(resourceClass.getName()));//System.out.println(query);
    	try {
			QueryResponse response = solr.query(query);
			SolrDocumentList list = response.getResults();
			return list.getNumFound();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private String quotedString(String s){
		return "\"" + s.trim() + "\"";
	}
	
	public static void main(String[] args) throws Exception {
		String solrServerURL = "http://solr.aksw.org/en_dbpedia_resources/";
		String searchField = "comment";
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File("src/test/resources/org/dllearner/algorithms/isle/dbpedia_3.9.owl"));
		SolrSyntacticIndex index = new SolrSyntacticIndex(ontology, solrServerURL, searchField);
		long n = index.getNumberOfDocumentsFor(new NamedClass("http://dbpedia.org/ontology/Person"), new NamedClass("http://schema.org/Canal"));
		System.out.println(n);
		n = index.getNumberOfDocumentsForTyped(new NamedClass("http://dbpedia.org/ontology/Person"), new NamedClass("http://schema.org/Canal"));
		System.out.println(n);
		n = index.getNumberOfDocumentsForTyped(new NamedClass("http://dbpedia.org/ontology/Person"), new NamedClass("http://dbpedia.org/ontology/nationality"));
		System.out.println(n);
		n = index.getNumberOfDocumentsForTyped(new NamedClass("http://dbpedia.org/ontology/Person"), new NamedClass("http://dbpedia.org/ontology/birthPlace"));
		System.out.println(n);
	}

}
