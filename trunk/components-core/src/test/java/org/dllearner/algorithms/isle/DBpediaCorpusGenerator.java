/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * This class loads a set of English labels for a given number of instances for each class in the DBpedia ontology.
 * @author Lorenz Buehmann
 *
 */
public class DBpediaCorpusGenerator {
	
	/**
	 * Loads DBpedia ontology from remote URL.
	 */
	private static OWLOntology loadDBpediaOntology(){
		try {
			URL url = new URL("http://downloads.dbpedia.org/3.9/dbpedia_3.9.owl.bz2");
			InputStream is = new BufferedInputStream(url.openStream());
			 CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			 OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(in);
			 return ontology;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public static Set<String> getDBpediaCorpusSample(String textProperty, int maxNrOfInstancesPerClass){
		Set<String> documents = new HashSet<>();
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		String cacheDirectory = "cache";
		File corpusDirectory = new File("tmp/dbpedia-corpus");
		corpusDirectory.mkdirs();
		
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		try {
			long timeToLive = TimeUnit.DAYS.toMillis(30);
			CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
			CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
			qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//load the DBpedia ontology
		OWLOntology ontology = loadDBpediaOntology();
		
		//get a random set of instances for each class and their English label
		for (OWLClass cls : ontology.getClassesInSignature()) {
			String query = "SELECT ?s ?text WHERE {"
					+ "?s a <" + cls.toStringID() + ">. "
							+ "?s <" + textProperty + "> ?text. "
							+ "FILTER(LANGMATCHES(LANG(?text),'en'))} LIMIT " + maxNrOfInstancesPerClass;
			QueryExecution qe = qef.createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				
				String uri = qs.getResource("s").getURI();
				String text = qs.getLiteral("text").getLexicalForm();
				
				documents.add(text);
				
				//save to disk
				try {
					Files.write(text, new File(corpusDirectory, URLEncoder.encode(uri, "UTF-8")), Charsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return documents;
	}
	
	public static Set<String> getDBpediaCorpusSample(String textProperty, Set<NamedClass> classes, int maxNrOfInstancesPerClass){
		Set<String> documents = new HashSet<>();
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		String cacheDirectory = "cache";
		File corpusDirectory = new File("tmp/dbpedia-corpus");
		corpusDirectory.mkdirs();
		
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		try {
			long timeToLive = TimeUnit.DAYS.toMillis(30);
			CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
			CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
			qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//get a random set of instances for each class and their English label
		for (NamedClass cls : classes) {
			String query = "SELECT ?s ?text WHERE {"
					+ "?s a <" + cls.getName() + ">. "
							+ "?s <" + textProperty + "> ?text. "
							+ "FILTER(LANGMATCHES(LANG(?text),'en'))} LIMIT " + maxNrOfInstancesPerClass;
			QueryExecution qe = qef.createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				
				String uri = qs.getResource("s").getURI();
				String text = qs.getLiteral("text").getLexicalForm();
				
				documents.add(text);
				
				//save to disk
				try {
					Files.write(text, new File(corpusDirectory, URLEncoder.encode(uri, "UTF-8")), Charsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return documents;
	}
	
}
