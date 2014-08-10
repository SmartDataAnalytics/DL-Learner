/**
 * 
 */
package org.dllearner.algorithms.isle.index.syntactic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
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
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class SolrSyntacticIndex implements Index{
	
	private static final Logger logger = Logger.getLogger(SolrSyntacticIndex.class.getName());
	
	private SolrServer solr;
	private AnnotationEntityTextRetriever textRetriever;
	private String searchField;
	private String typesField = "types";
	
	long totalNumberOfDocuments = -1;
	
	Map<Set<Entity>, Long> cache = Collections.synchronizedMap(new HashMap<Set<Entity>, Long>());
	private OWLOntology ontology;
	
	public SolrSyntacticIndex(OWLOntology ontology, String solrServerURL, String searchField) {
		this.ontology = ontology;
		this.searchField = searchField;
		solr = new HttpSolrServer(solrServerURL);
		textRetriever = new RDFSLabelEntityTextRetriever(ontology);
	}
	
	public void loadCache(File file) throws IOException{
		logger.info("Loading cache...");
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
			try {
				cache = Collections.synchronizedMap((Map<Set<Entity>, Long>) ois.readObject());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		logger.info("...done.");
		Entity e = df.getOWLClass(IRI.create("http://dbpedia.org/ontology/Comics");
		int i = 0;
		for (Set<Entity> entities : cache.keySet()) {
			if(entities.contains(e)){
				System.out.println(entities);
				i++;
			}
		}
		System.out.println(i);
	}
	
	public void buildIndex(Collection<OWLClass> classes){
		logger.info("Building cache...");
		logger.info("#Classes: " + classes.size());
		
		ExecutorService executor = Executors.newFixedThreadPool(6);
		
		final Set<OWLEntity> owlEntities = new TreeSet<OWLEntity>();
		owlEntities.addAll(ontology.getClassesInSignature());
		owlEntities.addAll(ontology.getDataPropertiesInSignature());
		owlEntities.addAll(ontology.getObjectPropertiesInSignature());
		
		final Map<Set<Entity>, Long> frequencyCache = Collections.synchronizedMap(new HashMap<Set<Entity>, Long>());
		
		//fA resp. fB
		final Set<Entity> otherEntities = OWLAPIConverter.getEntities(owlEntities);
		otherEntities.addAll(classes);
		for (final Entity entity : otherEntities) {
			executor.submit(new Runnable() {
				
				@Override
				public void run() {
					Set<Entity> entities = new HashSet<>();
					entities.add(entity);
					long f = getNumberOfDocumentsFor(entity);
					frequencyCache.put(entities, f);
				}
			});
		}
		//fAB
		for (final NamedClass cls : classes) {
			logger.info(cls);
			for (final Entity entity : otherEntities) {
				if(!cls.equals(entity)){
					executor.submit(new Runnable() {
						
						@Override
						public void run() {
							Set<Entity> entities = new HashSet<>();
							entities.add(cls);
							entities.add(entity);
							long fAB = getNumberOfDocumentsFor(cls, entity);
							frequencyCache.put(entities, fAB);
						}
					});
				}
			}
		}
		executor.shutdown();
        try {
			executor.awaitTermination(10, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        logger.info("Cache size: " + frequencyCache.size());
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("entity_frequencies.obj"));
			oos.writeObject(frequencyCache);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
	public synchronized long getNumberOfDocumentsFor(Entity entity) {
		HashSet<Entity> entitySet = Sets.newHashSet(entity);
		if(cache.containsKey(entitySet)){
			return cache.get(entitySet);
		}
		Map<String, Double> relevantText = textRetriever.getRelevantTextSimple(entity);
		
		String queryString = "(";
		Set<String> terms = new HashSet<>();
		for (Entry<String, Double> entry : relevantText.entrySet()) {
			String tokens = entry.getKey();
			String phrase = tokens;
			phrase.trim();
			terms.add(quotedString(phrase));
		}
		queryString += Joiner.on("OR").join(terms);
		queryString += ")";
		
		SolrQuery query = new SolrQuery(searchField + ":" + queryString);//System.out.println(query);
    	try {
			QueryResponse response = solr.query(query);
			SolrDocumentList list = response.getResults();
			cache.put(entitySet, list.getNumFound());
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
	public synchronized long getNumberOfDocumentsFor(Entity... entities) {
		Set<Entity> entitiesSet = Sets.newHashSet(entities);
		if(cache.containsKey(entitiesSet)){
			return cache.get(entitiesSet);
		}
		
		Set<String> queryStringParts = new HashSet<>();
		
		for (Entity entity : entities) {
			Map<String, Double> relevantText = textRetriever.getRelevantTextSimple(entity);
			
			String queryString = "(";
			Set<String> terms = new HashSet<>();
			for (Entry<String, Double> entry : relevantText.entrySet()) {
				String tokens = entry.getKey();
				String phrase = tokens;
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
			cache.put(entitiesSet, list.getNumFound());
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
		index.loadCache(new File("entity_frequencies.obj"));
		long n = index.getNumberOfDocumentsFor(df.getOWLClass(IRI.create("http://dbpedia.org/ontology/Comics"));
		System.out.println(n);
		n = index.getNumberOfDocumentsFor(df.getOWLClass(IRI.create("http://dbpedia.org/ontology/Comics"), df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/largestCity"));
		System.out.println(n);
	}

}
