package org.dllearner.algorithm.tbsl.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Property;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.reasoning.SPARQLReasoner;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class SPARQLEndpointMetrics {
	
	private static final Logger log = Logger.getLogger(SPARQLEndpointMetrics.class);
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache;
	private SPARQLReasoner reasoner;
	
	public SPARQLEndpointMetrics(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
		
		this.reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), cache);
	}
	
	/**
	 * Computes the directed Pointwise Mutual Information(PMI) measure. Formula: log( (f(prop, cls) * N) / (f(cls) * f(prop) ) )
	 * @param cls
	 * @param prop
	 * @return
	 */
	public double getDirectedPMI(ObjectProperty prop, NamedClass cls){
		log.debug(String.format("Computing PMI(%s, %s)", prop, cls));
		
		double classOccurenceCnt = getOccurencesInObjectPosition(cls);
		double propertyOccurenceCnt = getOccurences(prop);
		double coOccurenceCnt = getOccurencesPredicateObject(prop, cls);
		double total = getTotalTripleCount();
		
		double pmi = 0;
		if(coOccurenceCnt > 0 && classOccurenceCnt > 0 && propertyOccurenceCnt > 0){
			pmi = Math.log( (coOccurenceCnt * total) / (classOccurenceCnt * propertyOccurenceCnt) );
		}
		log.info(String.format("PMI(%s, %s) = %f", prop, cls, pmi));
		return pmi;
	}
	
	/**
	 * Computes the directed Pointwise Mutual Information(PMI) measure. Formula: log( (f(cls,prop) * N) / (f(cls) * f(prop) ) )
	 * @param cls
	 * @param prop
	 * @return
	 */
	public double getDirectedPMI(NamedClass cls, Property prop){
		log.debug(String.format("Computing PMI(%s, %s)...", cls, prop));
		
		double classOccurenceCnt = getOccurencesInSubjectPosition(cls);
		double propertyOccurenceCnt = getOccurences(prop);
		double coOccurenceCnt = getOccurencesSubjectPredicate(cls, prop);
		double total = getTotalTripleCount();
		
		double pmi = 0;
		if(coOccurenceCnt > 0 && classOccurenceCnt > 0 && propertyOccurenceCnt > 0){
			pmi = Math.log( (coOccurenceCnt * total) / (classOccurenceCnt * propertyOccurenceCnt) );
		}
		log.info(String.format("PMI(%s, %s) = %f", cls, prop, pmi));
		return pmi;
	}
	
	/**
	 * Computes the directed Pointwise Mutual Information(PMI) measure. Formula: log( (f(cls,prop) * N) / (f(cls) * f(prop) ) )
	 * @param cls
	 * @param prop
	 * @return
	 */
	public double getPMI(NamedClass subject, NamedClass object){
		log.debug(String.format("Computing PMI(%s, %s)", subject, object));
		
		double coOccurenceCnt = getOccurencesSubjectObject(subject, object);
		double subjectOccurenceCnt = getOccurencesInSubjectPosition(subject);
		double objectOccurenceCnt = getOccurencesInObjectPosition(object);
		double total = getTotalTripleCount();
		
		double pmi = 0;
		if(coOccurenceCnt > 0 && subjectOccurenceCnt > 0 && objectOccurenceCnt > 0){
			pmi = Math.log( (coOccurenceCnt * total) / (subjectOccurenceCnt * objectOccurenceCnt) );
		}
		log.info(String.format("PMI(%s, %s) = %f", subject, object, pmi));
		return pmi;
	}
	
	/**
	 * Returns the direction of the given triple, computed by calculating the PMI values of each combination.
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return -1 if the given triple should by reversed, else 1.
	 */
	public int getDirection(NamedClass subject, ObjectProperty predicate, NamedClass object){
		log.info(String.format("Computing direction between [%s, %s, %s]", subject, predicate, object));
		double pmi_obj_pred = getDirectedPMI(object, predicate);
		double pmi_pred_subj = getDirectedPMI(predicate, subject);
		double pmi_subj_pred = getDirectedPMI(subject, predicate);
		double pmi_pred_obj = getDirectedPMI(predicate, object);
		
		double threshold = 2.0;
		
		double value = ((pmi_obj_pred + pmi_pred_subj) - (pmi_subj_pred + pmi_pred_obj));
		log.info("(PMI(OBJECT, PREDICATE) + PMI(PREDICATE, SUBJECT)) - (PMI(SUBJECT, PREDICATE) + PMI(PREDICATE, OBJECT)) = " + value);
		
		if( value > threshold){
			log.info(object + "---" + predicate + "--->" + subject);
			return -1;
		} else {
			log.info(subject + "---" + predicate + "--->" + object);
			return 1;
		}
	}
	
	public Map<ObjectProperty, Integer> getMostFrequentProperties(NamedClass cls1, NamedClass cls2){
		Map<ObjectProperty, Integer> prop2Cnt = new HashMap<ObjectProperty, Integer>();
		String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?x1 a <%s>. ?x2 a <%s>. ?x1 ?p ?x2} GROUP BY ?p", cls1, cls2);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			ObjectProperty p = new ObjectProperty(qs.getResource("p").getURI());
			int cnt = qs.getLiteral("cnt").getInt();
			prop2Cnt.put(p, cnt);
		}
		return prop2Cnt;
	}
	
	/**
	 * Returns the number of triples with the given property as predicate and where the subject belongs to the given class.
	 * @param cls
	 * @return
	 */
	public int getOccurencesSubjectPredicate(NamedClass cls, Property prop){
		String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s a <%s>. ?s <%s> ?o}", cls.getName(), prop.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int cnt = rs.next().getLiteral("cnt").getInt();
		return cnt;
	}
	
	/**
	 * Returns the number of triples with the given property as predicate and where the object belongs to the given class.
	 * @param cls
	 * @return
	 */
	public int getOccurencesPredicateObject(Property prop, NamedClass cls){
		String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?o a <%s>. ?s <%s> ?o}", cls.getName(), prop.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int cnt = rs.next().getLiteral("cnt").getInt();
		return cnt;
	}
	
	/**
	 * Returns the number of triples with the first given class as subject and the second given class as object.
	 * @param cls
	 * @return
	 */
	public int getOccurencesSubjectObject(NamedClass subject, NamedClass object){
		String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s a <%s>. ?s ?p ?o. ?o a <%s>}", subject.getName(), object.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int cnt = rs.next().getLiteral("cnt").getInt();
		return cnt;
	}
	
	/**
	 * Returns the number of triples where the subject belongs to the given class.
	 * @param cls
	 * @return
	 */
	public int getOccurencesInSubjectPosition(NamedClass cls){
		String query  = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {?s a <%s>. ?s ?p ?o.}", cls.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int classOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		return classOccurenceCnt;
	}
	
	/**
	 * Returns the number of triples where the object belongs to the given class.
	 * @param cls
	 * @return
	 */
	public int getOccurencesInObjectPosition(NamedClass cls){
		String query  = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {?o a <%s>. ?s ?p ?o.}", cls.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int classOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		return classOccurenceCnt;
	}
	
	/**
	 * Returns the number triples with the given property as predicate.
	 * @param prop
	 * @return
	 */
	public int getOccurences(Property prop){
		String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}", prop.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int propOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		return propOccurenceCnt;
	}
	
	/**
	 * Returns the number of triples where the subject or object belongs to the given class.
	 * (This is not the same as computing the number of instances of the given class {@link SPARQLEndpointMetrics#getPopularity(NamedClass)})
	 * @param cls
	 * @return
	 */
	public int getOccurences(NamedClass cls){
		String query  = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {?s a <%s>.{?s ?p1 ?o1.} UNION {?o2 ?p2 ?s} }", cls.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int classOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		return classOccurenceCnt;
	}
	
	/**
	 * Returns the number of instances of the given class.
	 * @param cls
	 * @return
	 */
	public int getPopularity(NamedClass cls){
		String query  = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {?s a <%s>.{?s ?p1 ?o1.} UNION {?o2 ?p2 ?s} }", cls.getName());
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int classOccurenceCnt = rs.next().getLiteral("cnt").getInt();
		return classOccurenceCnt;
	}
	
	/**
	 * Returns the total number of triples in the endpoint. For now we return a fixed number 275494030(got from DBpedia Live 18. July 14:00).
	 * @return
	 */
	public int getTotalTripleCount(){
		return 275494030;
		/*String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o}");
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int cnt = rs.next().getLiteral("cnt").getInt();
		return cnt;*/
	}
	
	public double getGoodness(NamedClass subject, ObjectProperty predicate, NamedClass object){
		
		double pmi_subject_predicate = getDirectedPMI(subject, predicate);
		double pmi_preciate_object = getDirectedPMI(predicate, object);
		double pmi_subject_object = getPMI(subject, object);
		
		double goodness = pmi_subject_predicate + pmi_preciate_object + 2*pmi_subject_object;
		
		return goodness;
	}
	
	public double getGoodness(Individual subject, ObjectProperty predicate, NamedClass object){
		//this is independent of the subject types
		double pmi_preciate_object = getDirectedPMI(predicate, object);
		
		double goodness = Double.MIN_VALUE;
		//get all asserted classes of subject and get the highest value
		//TODO inference
		Set<NamedClass> types = reasoner.getTypes(subject);
		for(NamedClass type : types){
			double pmi_subject_predicate = getDirectedPMI(type, predicate);
			double pmi_subject_object = getPMI(type, object);
			double tmpGoodness = pmi_subject_predicate + pmi_preciate_object + 2*pmi_subject_object;
			if(tmpGoodness >= goodness){
				goodness = tmpGoodness;
			}
		}
		return goodness;
	}
	
	public double getGoodness(NamedClass subject, ObjectProperty predicate, Individual object){
		//this is independent of the object types
		double pmi_subject_predicate = getDirectedPMI(subject, predicate);
		
		double goodness = Double.MIN_VALUE;
		//get all asserted classes of subject and get the highest value
		//TODO inference
		Set<NamedClass> types = reasoner.getTypes(object);
		for(NamedClass type : types){
			double pmi_preciate_object = getDirectedPMI(predicate, type);
			double pmi_subject_object = getPMI(subject, type);
			double tmpGoodness = pmi_subject_predicate + pmi_preciate_object + 2*pmi_subject_object;
			if(tmpGoodness >= goodness){
				goodness = tmpGoodness;
			}
		}
		return goodness;
	}
	
	public double getGoodnessConsideringSimilarity(NamedClass subject, ObjectProperty predicate, NamedClass object, 
			double subjectSim, double predicateSim, double objectSim){
		
		double pmi_subject_predicate = getDirectedPMI(subject, predicate);
		double pmi_preciate_object = getDirectedPMI(predicate, object);
		double pmi_subject_object = getPMI(subject, object);
		
		double goodness = pmi_subject_predicate * subjectSim * predicateSim
				+ pmi_preciate_object * objectSim * predicateSim
				+ 2 * pmi_subject_object * subjectSim * objectSim;
		
		return goodness;
	}
	
	public void precompute(){
		precompute(Collections.<String>emptySet());
	}
	
	public void precompute(Collection<String> namespaces){
		log.info("Precomputing...");
		long startTime = System.currentTimeMillis();
		SortedSet<NamedClass> classes = new TreeSet<NamedClass>();
		String query = "SELECT DISTINCT ?class WHERE {?s a ?class.";
		for(String namespace : namespaces){
			query += "FILTER(REGEX(STR(?class),'" + namespace + "'))";
		}
		query += "}";
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			classes.add(new NamedClass(qs.getResource("class").getURI()));
		}
		
		SortedSet<ObjectProperty> objectProperties = new TreeSet<ObjectProperty>();
		query = "SELECT DISTINCT ?prop WHERE {?prop a owl:ObjectProperty. ";
		for(String namespace : namespaces){
			query += "FILTER(REGEX(STR(?prop),'" + namespace + "'))";
		}
		query += "}";
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		while(rs.hasNext()){
			qs = rs.next();
			objectProperties.add(new ObjectProperty(qs.getResource("prop").getURI()));
		}
		
		for(NamedClass cls : classes){
			for(ObjectProperty prop : objectProperties){
				log.info("Processing class " + cls + " and property " + prop);
				getDirectedPMI(cls, prop);
				getDirectedPMI(prop, cls);
			}
		}
		
		for(NamedClass cls1 : classes){
			for(NamedClass cls2 : classes){
				log.info("Processing class " + cls1 + " and class " + cls2);
				getPMI(cls1, cls2);
				getPMI(cls2, cls1);
			}
		}
		log.info("Done in " + ((System.currentTimeMillis() - startTime)/1000d) + "s");
	}
	
	public static void main(String[] args) {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
		ExtractionDBCache cache = new ExtractionDBCache("/opt/tbsl/cache");
		String NS = "http://dbpedia.org/ontology/";
		String NS_Res = "http://dbpedia.org/resource/";
		
		NamedClass person = new NamedClass(NS + "Person");
		NamedClass writer = new NamedClass(NS + "Writer");
		NamedClass book = new NamedClass(NS + "Book");
		NamedClass film = new NamedClass(NS + "Film");
		NamedClass actor = new NamedClass(NS + "Actor");
		ObjectProperty pAuthor = new ObjectProperty(NS + "author");
		ObjectProperty pWriter = new ObjectProperty(NS + "writer");
		ObjectProperty pStarring = new ObjectProperty(NS + "starring");
		Individual bradPitt = new Individual(NS_Res + "Brad_Pitt");
		Individual bradPittBoxer = new Individual(NS_Res + "Brad_Pitt_%28boxer%29");
		Individual danBrown = new Individual(NS_Res + "Dan_Brown");
		Individual danBrowne = new Individual(NS_Res + "Dan_Browne");
		
		SPARQLEndpointMetrics pmiGen = new SPARQLEndpointMetrics(endpoint, cache);
		pmiGen.precompute(Arrays.asList(new String[]{"http://dbpedia.org/ontology/"}));
		
		System.out.println(pmiGen.getDirectedPMI(pAuthor, person));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirectedPMI(pAuthor, writer));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirectedPMI(book, pAuthor));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirection(writer, pAuthor, book));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirection(person, pStarring, film));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(person, film));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(film, actor));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(film, person));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getOccurences(book));
		System.out.println(pmiGen.getOccurencesInObjectPosition(book));
		System.out.println(pmiGen.getOccurencesInSubjectPosition(book));
		
		System.out.println("#########################################");
		
		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, person));
		System.out.println("Goodness: " + pmiGen.getGoodness(person, pAuthor, book));
		System.out.println("Goodness: " + pmiGen.getGoodness(person, pWriter, book));
		System.out.println("Goodness: " + pmiGen.getGoodness(book, pAuthor, person));
		System.out.println("Goodness: " + pmiGen.getGoodness(book, pWriter, person));
		
		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, bradPitt));
		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, bradPittBoxer));
		System.out.println("Goodness: " + pmiGen.getGoodness(book, pAuthor, danBrown));
		System.out.println("Goodness: " + pmiGen.getGoodness(book, pAuthor, danBrowne));
		
		
		
	}

}
