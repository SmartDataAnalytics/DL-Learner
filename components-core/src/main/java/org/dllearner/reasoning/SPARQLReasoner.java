/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.reasoning;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactoryImpl;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.IndividualReasoner;
import org.dllearner.core.SchemaReasoner;
import org.dllearner.core.config.BooleanEditor;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Hierarchy;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SPARQLQueryUtils;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.clarkparsia.owlapiv3.XSD;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@ComponentAnn(name = "SPARQL Reasoner", shortName = "spr", version = 0.1)
public class SPARQLReasoner extends AbstractReasonerComponent implements SchemaReasoner, IndividualReasoner {

	private static final Logger logger = LoggerFactory.getLogger(SPARQLReasoner.class);
	
	public enum PopularityType {
		CLASS, OBJECT_PROPERTY, DATA_PROPERTY;
	}
	
	private static final ParameterizedSparqlString CLASS_POPULARITY_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(*) AS ?cnt) WHERE {?s a ?entity .}");
	
	private static final ParameterizedSparqlString PROPERTY_POPULARITY_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(*) AS ?cnt) WHERE {?s ?entity ?o .}");
	
	private static final ParameterizedSparqlString INDIVIDUAL_POPULARITY_QUERY = new ParameterizedSparqlString(
			"SELECT (COUNT(*) AS ?cnt) WHERE {?entity ?p ?o .}");


	@ConfigOption(name = "useCache", description = "Whether to use a DB cache", defaultValue = "true", required = false, propertyEditorClass = BooleanEditor.class)
	private boolean useCache = true;

	private QueryExecutionFactory qef;

	private SparqlEndpointKS ks;
	private ClassHierarchy hierarchy;
	private Model model;

	private Map<OWLEntity, Integer> entityPopularityMap = new HashMap<OWLEntity, Integer>();
	private Map<OWLClass, Integer> classPopularityMap = new HashMap<OWLClass, Integer>();
	private Map<OWLObjectProperty, Integer> objectPropertyPopularityMap = new HashMap<OWLObjectProperty, Integer>();
	private Map<OWLDataProperty, Integer> dataPropertyPopularityMap = new HashMap<OWLDataProperty, Integer>();
	private Map<OWLIndividual, Integer> individualPopularityMap = new HashMap<OWLIndividual, Integer>();
	private boolean batchedMode = true;
	private Set<PopularityType> precomputedPopularityTypes = new HashSet<SPARQLReasoner.PopularityType>();
	
	private boolean prepared = false;
	
	private OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();

	private OWLDataFactory df = new OWLDataFactoryImpl(false, false);
	
	public SPARQLReasoner() {
	}

	public SPARQLReasoner(SparqlEndpointKS ks) {
		this(ks, (String)null);
	}
	
	public SPARQLReasoner(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	public SPARQLReasoner(QueryExecutionFactory qef, boolean useCache) {
		this.qef = qef;
		this.useCache = useCache;
	}
	
	public SPARQLReasoner(SparqlEndpointKS ks, String cacheDirectory) {
		this.ks = ks;
		
		if(ks.isRemote()){
			qef = ks.getQueryExecutionFactory();
		} else {
			qef = new QueryExecutionFactoryModel(((LocalModelBasedSparqlEndpointKS)ks).getModel());
		}
	}
	
	public SPARQLReasoner(SparqlEndpoint endpoint, String cacheDirectory) {
		this(new SparqlEndpointKS(endpoint), cacheDirectory);
	}
	
	public SPARQLReasoner(SparqlEndpointKS ks, CacheBackend cacheBackend) {
		this(ks, new CacheFrontendImpl(cacheBackend));
	}
	
	public SPARQLReasoner(SparqlEndpointKS ks, CacheFrontend cache) {
		this.ks = ks;
		
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ks.getEndpoint();
			qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
			qef = new QueryExecutionFactoryDelay(qef, 50);
			qef = new QueryExecutionFactoryCacheEx(qef, cache);
			
//			qef = new QueryExecutionFactoryPaginated(qef, 10000);
		} else {
			qef = new QueryExecutionFactoryModel(((LocalModelBasedSparqlEndpointKS)ks).getModel());
		}
	}
	
	public SPARQLReasoner(SparqlEndpointKS ks, ExtractionDBCache cache) {
		this(ks, cache.getCacheDirectory());
	}

	public SPARQLReasoner(Model model) {
		this.model = model;
		qef = new QueryExecutionFactoryModel(model);
	}
	
	public QueryExecutionFactory getQueryExecutionFactory() {
		return qef;
	}
	
	public void precomputePopularities(PopularityType... popularityTypes){
		for (PopularityType popularityType : popularityTypes) {
			switch (popularityType) {
			case CLASS:precomputeClassPopularity();break;
			case OBJECT_PROPERTY:precomputeObjectPropertyPopularity();break;
			case DATA_PROPERTY:precomputeDataPropertyPopularity();break;
			default:
				break;
			}
		}
	}

	public void precomputePopularity(){
		precomputeClassPopularity();
		precomputeDataPropertyPopularity();
		precomputeObjectPropertyPopularity();
	}
	
	public boolean isPrecomputed(PopularityType popularityType){
		return precomputedPopularityTypes.contains(popularityType);
	}

	public void precomputeClassPopularity() {
		if(isPrecomputed(PopularityType.CLASS)){
			return;
		}
		logger.info("Precomputing class popularity ...");

		long start = System.currentTimeMillis();
		
		if (batchedMode) {
			String query = "PREFIX owl:<http://www.w3.org/2002/07/owl#>  SELECT ?type (COUNT(?s) AS ?cnt) WHERE {?s a ?type . ?type a owl:Class .} GROUP BY ?type";
			ResultSet rs = executeSelectQuery(query);
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				if (qs.get("type").isURIResource()) {
					OWLClass cls = df.getOWLClass(IRI.create(qs.getResource("type").getURI()));
					int cnt = qs.getLiteral("cnt").getInt();
					classPopularityMap.put(cls, cnt);
				}
			}
		} else {
			Set<OWLClass> classes = getOWLClasses();
			String queryTemplate = "SELECT (COUNT(?s) AS ?cnt) WHERE {?s a <%s>}";
			for (OWLClass cls : classes) {
				ResultSet rs = executeSelectQuery(String.format(queryTemplate, cls.toStringID()));
				int cnt = rs.next().getLiteral("cnt").getInt();
				classPopularityMap.put(cls, cnt);
			}
		}
		precomputedPopularityTypes.add(PopularityType.CLASS); 
		
		long end = System.currentTimeMillis();
		
		logger.info("... done in " + (end - start) + "ms.");
	}

	public void precomputeObjectPropertyPopularity(){
		if(isPrecomputed(PopularityType.OBJECT_PROPERTY)){
			return;
		}
		logger.info("Precomputing object property popularity ...");

		long start = System.currentTimeMillis();
		
		if (batchedMode) {
			String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s ?p ?o . ?p a owl:ObjectProperty .} GROUP BY ?p";
			ResultSet rs = executeSelectQuery(query);
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				if (qs.get("p").isURIResource()) {
					OWLObjectProperty property = df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI()));
					int cnt = qs.getLiteral("cnt").getInt();
					objectPropertyPopularityMap.put(property, cnt);
				}
			}
		} else {
			Set<OWLObjectProperty> properties = getOWLObjectProperties();
			String queryTemplate = "SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}";
			for (OWLObjectProperty property : properties) {
				ResultSet rs = executeSelectQuery(String.format(queryTemplate, property.toStringID()));
				int cnt = rs.next().getLiteral("cnt").getInt();
				objectPropertyPopularityMap.put(property, cnt);
			}
		}
		precomputedPopularityTypes.add(PopularityType.OBJECT_PROPERTY); 
		
		long end = System.currentTimeMillis();
		
		logger.info("... done in " + (end - start) + "ms.");
	}

	public void precomputeDataPropertyPopularity(){
		if(isPrecomputed(PopularityType.DATA_PROPERTY)){
			return;
		}
		logger.info("Precomputing data property popularity ...");

		long start = System.currentTimeMillis();
		
		if (batchedMode) {
			String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p (COUNT(*) AS ?cnt) WHERE {?s ?p ?o . ?p a owl:DatatypeProperty .} GROUP BY ?p";
			ResultSet rs = executeSelectQuery(query);
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				if (qs.get("p").isURIResource()) {
					OWLDataProperty property = df.getOWLDataProperty(IRI.create(qs.getResource("p").getURI()));
					int cnt = qs.getLiteral("cnt").getInt();
					dataPropertyPopularityMap.put(property, cnt);
				}
			}
		} else {
			Set<OWLDataProperty> properties = getOWLDataProperties();
			String queryTemplate = "SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}";
			for (OWLDataProperty property : properties) {
				ResultSet rs = executeSelectQuery(String.format(queryTemplate, property.toStringID()));
				int cnt = rs.next().getLiteral("cnt").getInt();
				dataPropertyPopularityMap.put(property, cnt);
			}
		}
		precomputedPopularityTypes.add(PopularityType.DATA_PROPERTY); 
		
		long end = System.currentTimeMillis();
		
		logger.info("... done in " + (end - start) + "ms.");
	}

	public int getSubjectCountForProperty(OWLProperty p, long timeout){
		int cnt = -1;
		String query = String.format(
				"SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s <%s> ?o.}",
				p.toString());
		ResultSet rs = executeSelectQuery(query, timeout);
		if(rs.hasNext()){
			cnt = rs.next().getLiteral("cnt").getInt();
		}

		return cnt;
	}
	
	public int getSubjectCountForProperty(OWLProperty p){
		int cnt = -1;
		String query = String.format(
				"SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s <%s> ?o.}",
				p.toStringID());
		ResultSet rs = executeSelectQuery(query);
		if(rs.hasNext()){
			cnt = rs.next().getLiteral("cnt").getInt();
		}

		return cnt;
	}

	public int getObjectCountForProperty(OWLObjectProperty p, long timeout){
		int cnt = -1;
		String query = String.format(
				"SELECT (COUNT(DISTINCT ?o) AS ?cnt) WHERE {?s <%s> ?o.}",
				p.toStringID());
		ResultSet rs = executeSelectQuery(query, timeout);
		if(rs.hasNext()){
			cnt = rs.next().getLiteral("cnt").getInt();
		}

		return cnt;
	}
	
	public int getObjectCountForProperty(OWLObjectProperty p){
		int cnt = -1;
		String query = String.format(
				"SELECT (COUNT(DISTINCT ?o) AS ?cnt) WHERE {?s <%s> ?o.}",
				p.toStringID());
		ResultSet rs = executeSelectQuery(query);
		if(rs.hasNext()){
			cnt = rs.next().getLiteral("cnt").getInt();
		}

		return cnt;
	}
	
	public <T extends OWLEntity> int getPopularity(T entity){
		Integer popularity = null;
		
		if(useCache){
			popularity = entityPopularityMap.get(entity);
		} 
		
		if(popularity == null){
			ParameterizedSparqlString queryTemplate;
			if(entity.isOWLClass()){
				queryTemplate = CLASS_POPULARITY_QUERY;
			} else if(entity.isOWLObjectProperty() || entity.isOWLDataProperty()){
				queryTemplate = PROPERTY_POPULARITY_QUERY;
			} else if(entity.isOWLNamedIndividual()){
				queryTemplate = INDIVIDUAL_POPULARITY_QUERY;
			} else {
				throw new IllegalArgumentException("Popularity computation not supported for entity type " + entity.getEntityType().getName());
			}

			queryTemplate.setIri("entity", entity.toStringID());
			ResultSet rs = executeSelectQuery(queryTemplate.toString());
			
			popularity = rs.next().getLiteral("cnt").getInt();
			
			entityPopularityMap.put(entity, popularity);
		}
		return popularity.intValue();
	}

	
	
	public int getPopularityOf(OWLClassExpression description){
		if(classPopularityMap != null && classPopularityMap.containsKey(description)){
			return classPopularityMap.get(description);
		} else {
			String query = converter.asCountQuery(description).toString();
			ResultSet rs = executeSelectQuery(query);
			int cnt = rs.next().getLiteral("cnt").getInt();
			return cnt;
		}
	}

	public final ClassHierarchy prepareSubsumptionHierarchy() {
		if(!prepared){
			logger.info("Preparing subsumption hierarchy ...");
			long startTime = System.currentTimeMillis();
			TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyUp = new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>(
					);
			TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyDown = new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>(
					);

			// parents/children of top ...
			SortedSet<OWLClassExpression> tmp = getSubClassesImpl(df.getOWLThing());
			subsumptionHierarchyUp.put(df.getOWLThing(), new TreeSet<OWLClassExpression>());
			subsumptionHierarchyDown.put(df.getOWLThing(), tmp);

			// ... bottom ...
			tmp = getSuperClassesImpl(df.getOWLNothing());
			subsumptionHierarchyUp.put(df.getOWLNothing(), tmp);
			subsumptionHierarchyDown.put(df.getOWLNothing(), new TreeSet<OWLClassExpression>());

			// ... and named classes
			Set<OWLClass> atomicConcepts;
			if(ks.isRemote()){
				atomicConcepts = new SPARQLTasks(ks.getEndpoint()).getAllClasses();
			} else {
				atomicConcepts = new TreeSet<OWLClass>();
				for(OntClass cls :  ((LocalModelBasedSparqlEndpointKS)ks).getModel().listClasses().toList()){
					if(!cls.isAnon()){
						atomicConcepts.add(df.getOWLClass(IRI.create(cls.getURI())));
					}
				}
			}

			for (OWLClass atom : atomicConcepts) {
				tmp = getSubClassesImpl(atom);
				// quality control: we explicitly check that no reasoner implementation returns null here
				if(tmp == null) {
					logger.error("Class hierarchy: getSubClasses returned null instead of empty set."); 
				}			
				subsumptionHierarchyDown.put(atom, tmp);

				tmp = getSuperClassesImpl(atom);
				// quality control: we explicitly check that no reasoner implementation returns null here
				if(tmp == null) {
					logger.error("Class hierarchy: getSuperClasses returned null instead of empty set."); 
				}			
				subsumptionHierarchyUp.put(atom, tmp);
			}		
			logger.info("... done in {}ms", (System.currentTimeMillis()-startTime));
			hierarchy = new ClassHierarchy(subsumptionHierarchyUp, subsumptionHierarchyDown);
			prepared = true;
		}
		return hierarchy;
	}
	
	public boolean isFunctional(OWLObjectProperty property){
		String query = "ASK {<" + property + "> a <" + OWL.FunctionalProperty.getURI() + ">}";
		return qef.createQueryExecution(query).execAsk();
	}
	
	public boolean isInverseFunctional(OWLObjectProperty property){
		String query = "ASK {<" + property + "> a <" + OWL.InverseFunctionalProperty.getURI() + ">}";
		return qef.createQueryExecution(query).execAsk();
	}
	
	public boolean isAsymmetric(OWLObjectProperty property){
		String query = "ASK {<" + property + "> a <" + OWL2.AsymmetricProperty.getURI() + ">}";
		return qef.createQueryExecution(query).execAsk();
	}
	
	public boolean isSymmetric(OWLObjectProperty property){
		String query = "ASK {<" + property + "> a <" + OWL2.SymmetricProperty.getURI() + ">}";
		return qef.createQueryExecution(query).execAsk();
	}
	
	public boolean isIrreflexive(OWLObjectProperty property){
		String query = "ASK {<" + property + "> a <" + OWL2.IrreflexiveProperty.getURI() + ">}";
		return qef.createQueryExecution(query).execAsk();
	}
	
	public boolean isReflexive(OWLObjectProperty property){
		String query = "ASK {<" + property + "> a <" + OWL2.ReflexiveProperty.getURI() + ">}";
		return qef.createQueryExecution(query).execAsk();
	}
	
	public boolean isTransitive(OWLObjectProperty property){
		String query = "ASK {<" + property + "> a <" + OWL2.TransitiveProperty.getURI() + ">}";
		return qef.createQueryExecution(query).execAsk();
	}
	
	public boolean isFunctional(OWLDataProperty property){
		String query = "ASK {<" + property + "> a <" + OWL.FunctionalProperty.getURI() + ">}";
		return qef.createQueryExecution(query).execAsk();
	}

	public final Hierarchy prepareSubsumptionHierarchyFast() {
		logger.info("Preparing subsumption hierarchy ...");
		long startTime = System.currentTimeMillis();
		TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyUp = new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>(
				);
		TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyDown = new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>(
				);

		String queryTemplate = "SELECT * WHERE {?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sup} LIMIT <%d> OFFSET <%d>";
		int limit = 1000;
		int offset = 0;
		boolean repeat = true;
		while(repeat){
			repeat = false;
			String query = String.format(queryTemplate, limit, offset);
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			while(rs.hasNext()){
				repeat = true;
				qs = rs.next();
				if(qs.get("sub").isURIResource() && qs.get("sup").isURIResource()){
					OWLClassExpression sub = df.getOWLClass(IRI.create(qs.get("sub").asResource().getURI()));
					OWLClassExpression sup = df.getOWLClass(IRI.create(qs.get("sup").asResource().getURI()));
					//add subclasses
					SortedSet<OWLClassExpression> subClasses = subsumptionHierarchyDown.get(sup);
					if(subClasses == null){
						subClasses = new TreeSet<OWLClassExpression>();
						subsumptionHierarchyDown.put(sup, subClasses);
					}
					subClasses.add(sub);
					//add superclasses
					SortedSet<OWLClassExpression> superClasses = subsumptionHierarchyUp.get(sub);
					if(superClasses == null){
						superClasses = new TreeSet<OWLClassExpression>();
						subsumptionHierarchyUp.put(sub, superClasses);
					}
					superClasses.add(sup);
				}
			}
			offset += limit;
		}

		logger.info("... done in {}ms", (System.currentTimeMillis()-startTime));
		hierarchy = new ClassHierarchy(subsumptionHierarchyUp, subsumptionHierarchyDown);
		return hierarchy;
	}

	public Model loadSchema(){
		return loadSchema(null);
	}
	
	public Model loadSchema(String namespace){
		Model model = ModelFactory.createDefaultModel();

		//load class hierarchy
		String query = String.format("CONSTRUCT {?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o} WHERE " +
				"{?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o." + (namespace != null ? "FILTER(REGEX(STR(?s), '^" + namespace + "'))}" : ""));
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2002/07/owl#equivalentClass> ?o} WHERE {?s <http://www.w3.org/2002/07/owl#equivalentClass> ?o}";
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2002/07/owl#disjointWith> ?o} WHERE {?s <http://www.w3.org/2002/07/owl#disjointWith> ?o}";
		model.add(loadIncrementally(query));
		//load domain axioms
		query = "CONSTRUCT {?s <http://www.w3.org/2000/01/rdf-schema#domain> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty> } " +
				"WHERE {?s <http://www.w3.org/2000/01/rdf-schema#domain> ?o.?s a <http://www.w3.org/2002/07/owl#ObjectProperty>}";
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2000/01/rdf-schema#domain> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>} " +
				"WHERE {?s <http://www.w3.org/2000/01/rdf-schema#domain> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>}";
		model.add(loadIncrementally(query));
		//load range axioms
		query = "CONSTRUCT {?s <http://www.w3.org/2000/01/rdf-schema#range> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>} " +
				"WHERE {?s <http://www.w3.org/2000/01/rdf-schema#range> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>}";
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2000/01/rdf-schema#range> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>} " +
				"WHERE {?s <http://www.w3.org/2000/01/rdf-schema#range> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>}";
		model.add(loadIncrementally(query));
		//load property hierarchy
		query = "CONSTRUCT {?s <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>} " +
				"WHERE {?s <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>}";
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>} " +
				"WHERE {?s <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>}";
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2002/07/owl#equivalentProperty> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>} " +
				"WHERE {?s <http://www.w3.org/2002/07/owl#equivalentProperty> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>}";
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2002/07/owl#equivalentProperty> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>} " +
				"WHERE {?s <http://www.w3.org/2002/07/owl#equivalentProperty> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>}";
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2002/07/owl#propertyDisjointWith> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>} " +
				"WHERE {?s <http://www.w3.org/2002/07/owl#propertyDisjointWith> ?o. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>}";
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s <http://www.w3.org/2002/07/owl#propertyDisjointWith> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>} " +
				"WHERE {?s <http://www.w3.org/2002/07/owl#propertyDisjointWith> ?o. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>}";
		model.add(loadIncrementally(query));
		//load inverse relation
		query = "CONSTRUCT {?s <http://www.w3.org/2002/07/owl#inverseOf> ?o} WHERE {?s <http://www.w3.org/2002/07/owl#inverseOf> ?o}";
		model.add(loadIncrementally(query));
		//load property characteristics
		Set<Resource> propertyCharacteristics = new HashSet<Resource>();
		//		propertyCharacteristics.add(OWL.FunctionalProperty);
		propertyCharacteristics.add(OWL.InverseFunctionalProperty);
		propertyCharacteristics.add(OWL.SymmetricProperty);
		propertyCharacteristics.add(OWL.TransitiveProperty);
		propertyCharacteristics.add(OWL2.ReflexiveProperty);
		propertyCharacteristics.add(OWL2.IrreflexiveProperty);
		propertyCharacteristics.add(OWL2.AsymmetricProperty);

		for(Resource propChar : propertyCharacteristics){
			query = "CONSTRUCT {?s a <%s>. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>} WHERE {?s a <%s>.}".replaceAll("%s", propChar.getURI());
			model.add(loadIncrementally(query));
		}
		//for functional properties we have to distinguish between data and object properties, 
		//i.e. we have to keep the property type information, otherwise conversion to OWLAPI ontology makes something curious
		query = "CONSTRUCT {?s a <%s>. ?s a <http://www.w3.org/2002/07/owl#ObjectProperty>} WHERE {?s a <%s>.?s a <http://www.w3.org/2002/07/owl#ObjectProperty>}".
				replaceAll("%s", OWL.FunctionalProperty.getURI());
		model.add(loadIncrementally(query));
		query = "CONSTRUCT {?s a <%s>. ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>} WHERE {?s a <%s>.?s a <http://www.w3.org/2002/07/owl#DatatypeProperty>}".
				replaceAll("%s", OWL.FunctionalProperty.getURI());
		model.add(loadIncrementally(query));


		return model;
	}
	
	/**
	 * Gets all logical axioms according to entities of type owl:Class, owl:ObjectProperty and owl:DatatypeProperty.
	 * @return
	 */
	public Model loadOWLSchema(){
		Model schema = ModelFactory.createDefaultModel();
		String prefixes = 
				"PREFIX owl:<http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";
		//axioms according to owl:Class entities
		String query = prefixes +
				"CONSTRUCT {" +
				"?s a owl:Class." +
				"?s rdfs:subClassOf ?sup." +
				"?s owl:equivalentClass ?equiv." +
				"?s owl:disjointWith ?disj." +
				"} WHERE {" +
				"?s a owl:Class. " +
				"OPTIONAL{?s rdfs:subClassOf ?sup.} " +
				"OPTIONAL{?s owl:equivalentClass ?equiv.} " +
				"OPTIONAL{?s owl:disjointWith ?disj.}" +
				"}";
		schema.add(loadIncrementally(query));
		//axioms according to owl:ObjectProperty entities
		query = prefixes +
				"CONSTRUCT {" +
				"?s a owl:ObjectProperty." +
				"?s a ?type." +
				"?s rdfs:domain ?domain." +
				"?s rdfs:range ?range." +
				"} WHERE {" +
				"?s a owl:ObjectProperty." +
				"?s a ?type. " +
				"OPTIONAL{?s rdfs:domain ?domain.} " +
				"OPTIONAL{?s rdfs:range ?range.}" +
				"}";
		schema.add(loadIncrementally(query));

		//axioms according to owl:ObjectProperty entities
		query = prefixes +
				"CONSTRUCT {" +
				"?s a owl:DatatypeProperty." +
				"?s a ?type." +
				"?s rdfs:domain ?domain." +
				"?s rdfs:range ?range." +
				"} WHERE {" +
				"?s a owl:DatatypeProperty." +
				"?s a ?type. " +
				"OPTIONAL{?s rdfs:domain ?domain.} " +
				"OPTIONAL{?s rdfs:range ?range.}" +
				"}";		
		schema.add(loadIncrementally(query));
		
		return schema;
	}

	private Model loadIncrementally(String query){
		QueryExecutionFactory old = qef;
		qef = new QueryExecutionFactoryPaginated(qef, 10000);
		QueryExecution qe = qef.createQueryExecution(query);
		Model model = qe.execConstruct();
		qe.close();
		qef = old;
		return model;
	}

	@Override
	public Set<OWLClass> getTypesImpl(OWLIndividual individual) {
		Set<OWLClass> types = new HashSet<OWLClass>();
		String query = String.format(SPARQLQueryUtils.SELECT_INSTANCE_TYPES_QUERY, individual.toStringID());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			types.add(df.getOWLClass(IRI.create(qs.getResource("var1").getURI())));
		}
		return types;
	}
	
	public Set<OWLClass> getTypes(OWLIndividual individual, String namespace) {
		Set<OWLClass> types = new HashSet<OWLClass>();
		String query = "SELECT DISTINCT ?class WHERE {<" + individual.toStringID() + "> a ?class.";
		if(namespace != null){
			query += "FILTER(REGEX(STR(?class),'^" + namespace + "'))";
		}
		query += "}";
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			types.add(df.getOWLClass(IRI.create(qs.getResource("class").getURI())));
		}
		return types;
	}
	
	public Set<OWLClass> getMostSpecificTypes(OWLIndividual individual) {
		Set<OWLClass> types = new HashSet<OWLClass>();
		String query = String.format(
				"SELECT ?type WHERE {<%s> a ?type . "
				+ "FILTER NOT EXISTS{<%s> a ?moreSpecificType ."
				+ "?moreSpecificType <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?type.}}", individual.toStringID(), individual.toStringID());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			types.add(df.getOWLClass(IRI.create(qs.getResource("type").getURI())));
		}
		return types;
	}
	
	
	/**
	 * Returns the entity type of the given resource IRI, i.e. whether the resource
	 * is a owl:Class, owl:ObjectProperty,owl:DatatypeProperty or owl:NamedIndividual.
	 * @param iri
	 * @return
	 */
	public EntityType<? extends OWLEntity> getOWLEntityType(String iri) {
		ParameterizedSparqlString query = new ParameterizedSparqlString("SELECT ?type WHERE {?s a ?type .}");
		query.setIri("s", iri);
		ResultSet rs = executeSelectQuery(query.toString());
		Set<EntityType<? extends OWLEntity>> entityTypes = new HashSet<EntityType<? extends OWLEntity>>();
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			String uri = qs.getResource("type").getURI();
			for(EntityType<? extends OWLEntity> entityType : EntityType.values()){
				if(entityType.getIRI().toString().equals(uri)){
					entityTypes.add(entityType);
					break;
				}
			}
		}
		if(entityTypes.size() == 1){
			return entityTypes.iterator().next();
		}
		return null;
	}

	public Set<OWLClass> getTypes(String namespace) {
		Set<OWLClass> types = new TreeSet<OWLClass>();
		String query = String.format("SELECT DISTINCT ?class WHERE {[] a ?class." + (namespace != null ? ("FILTER(REGEX(?class,'^" + namespace + "'))") : "") + "}");
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			types.add(df.getOWLClass(IRI.create(qs.getResource("class").getURI())));
		}
		return types;
	}
	
	public Set<OWLProperty> getProperties(boolean inferType, String namespace) {
		Set<OWLProperty> properties = new HashSet<OWLProperty>();
		String query = "SELECT DISTINCT ?p ?type WHERE {?s ?p ?o."
						+ (namespace != null ? ("FILTER(REGEX(?p,'^" + namespace + "'))") : "")
						+ "OPTIONAL{?p a ?type.}}";
		ResultSet rs = executeSelectQuery(query);
		Multimap<String, String> uri2Types = HashMultimap.create();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			String uri = qs.getResource("p").getURI();
			String type = "";
			if(qs.getResource("type") != null){
				type = qs.getResource("type").getURI();
			}
			uri2Types.put(uri, type);
		}
		for (Entry<String, Collection<String>> entry : uri2Types.asMap().entrySet()) {
			String uri = entry.getKey();
			Collection<String> types = entry.getValue();
			if(types.contains(OWL.ObjectProperty.getURI()) && !types.contains(OWL.DatatypeProperty.getURI())){
				properties.add(df.getOWLObjectProperty(IRI.create(uri)));
			} else if(!types.contains(OWL.ObjectProperty.getURI()) && types.contains(OWL.DatatypeProperty.getURI())){
				properties.add(df.getOWLDataProperty(IRI.create(uri)));
			} else {
				//infer the type by values
				query = "SELECT ?o WHERE {?s <" + uri + "> ?o. } LIMIT 100";
				rs = executeSelectQuery(query);
				boolean op = true;
				boolean dp = true;
				RDFNode node;
				while(rs.hasNext()){
					node = rs.next().get("o");
					op = node.isResource();
					dp = node.isLiteral();
				}
				if(op && !dp){
					properties.add(df.getOWLObjectProperty(IRI.create(uri)));
				} else if(!op && dp){
					properties.add(df.getOWLDataProperty(IRI.create(uri)));
				} else {
					//not possible to decide
				}
			}
		}
		return properties;
	}
	
	public Set<OWLProperty> getProperties(boolean inferType) {
		Set<OWLProperty> properties = new TreeSet<OWLProperty>();
		String query = "SELECT DISTINCT ?p ?type WHERE {?s ?p ?o. OPTIONAL{?p a ?type.}}";
		ResultSet rs = executeSelectQuery(query);
		Multimap<String, String> uri2Types = HashMultimap.create();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			String uri = qs.getResource("p").getURI();
			String type = "";
			if(qs.getResource("type") != null){
				type = qs.getResource("type").getURI();
			}
			uri2Types.put(uri, type);
		}
		for (Entry<String, Collection<String>> entry : uri2Types.asMap().entrySet()) {
			String uri = entry.getKey();
			Collection<String> types = entry.getValue();
			if(types.contains(OWL.ObjectProperty.getURI()) && !types.contains(OWL.DatatypeProperty.getURI())){
				properties.add(df.getOWLObjectProperty(IRI.create(uri)));
			} else if(!types.contains(OWL.ObjectProperty.getURI()) && types.contains(OWL.DatatypeProperty.getURI())){
				properties.add(df.getOWLDataProperty(IRI.create(uri)));
			} else {
				//infer the type by values
				query = "SELECT ?o WHERE {?s <" + uri + "> ?o. } LIMIT 100";
				rs = executeSelectQuery(query);
				boolean op = true;
				boolean dp = true;
				RDFNode node;
				while(rs.hasNext()){
					node = rs.next().get("o");
					op = node.isResource();
					dp = node.isLiteral();
				}
				if(op && !dp){
					properties.add(df.getOWLObjectProperty(IRI.create(uri)));
				} else if(!op && dp){
					properties.add(df.getOWLDataProperty(IRI.create(uri)));
				} else {
					//not possible to decide
				}
			}
		}
		return properties;
	}
	
	public Set<OWLClass> getNonEmptyOWLClasses() {
		Set<OWLClass> types = new TreeSet<OWLClass>();
		String query = String.format("SELECT DISTINCT ?class WHERE {?class a <%s>. FILTER EXISTS{?a a ?class}}",OWL.Class.getURI());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			types.add(df.getOWLClass(IRI.create(qs.getResource("class").getURI())));
		}
		return types;
	}

	public SortedSet<OWLClass> getOWLClasses() {
		return getOWLClasses(null);
	}
	
	public SortedSet<OWLClass> getOWLClasses(String namespace) {
		SortedSet<OWLClass> types = new TreeSet<OWLClass>();
		String query = SPARQLQueryUtils.SELECT_CLASSES_QUERY;
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			Resource resource = qs.getResource("class");
			if(resource.isURIResource()) {
				types.add(df.getOWLClass(IRI.create(resource.getURI())));
			}
		}
		return types;
	}
	
	public SortedSet<OWLObjectProperty> getOWLObjectProperties() {
		return getOWLObjectProperties(null);
	}
	
	public SortedSet<OWLObjectProperty> getOWLObjectProperties(String namespace) {
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>();
		String query = SPARQLQueryUtils.SELECT_OBJECT_PROPERTIES_QUERY;
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			properties.add(df.getOWLObjectProperty(IRI.create(qs.getResource("var1").getURI())));
		}
		return properties;
	}
	
	public SortedSet<OWLDataProperty> getOWLDataProperties() {
		return getOWLDataProperties(null);
	}
	
	public SortedSet<OWLDataProperty> getOWLDataProperties(String namespace) {
		SortedSet<OWLDataProperty> properties = new TreeSet<OWLDataProperty>();
		String query = SPARQLQueryUtils.SELECT_DATA_PROPERTIES_QUERY;
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			properties.add(df.getOWLDataProperty(IRI.create(qs.getResource("var1").getURI())));
		}
		return properties;
	}

	/**
	 * Returns a set of classes which are siblings, i.e. on the same level
	 * in the class hierarchy.
	 * @param cls
	 * @param limit
	 * @return
	 */
	public Set<OWLClass> getSiblingClasses(OWLClass cls) {
		Set<OWLClass> siblings = new TreeSet<OWLClass>();
		String query = "SELECT ?sub WHERE { <" + cls.toStringID() + "> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?super .";
		query += "?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?super .";
//		query += "FILTER NOT EXISTS{?sub2 <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?super. ?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?super.}";
		query += "FILTER( !SAMETERM(?sub, <" + cls.toStringID() + ">) && !SAMETERM(?super, <http://www.w3.org/2000/01/rdf-schema#Resource>)) . }";
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("sub").isURIResource()){
				siblings.add(df.getOWLClass(IRI.create(qs.getResource("sub").getURI())));
			}
		}
		return siblings;
	}

	/**
	 * Returns a set of classes which are Parent of current class
	 * in the class hierarchy.
	 * @param cls
	 * @param limit
	 * @return
	 */
	public Set<OWLClass> getParentClasses(OWLClass cls) {
		Set<OWLClass> parents = new HashSet<OWLClass>();
		String query = "SELECT DISTINCT ?parentClass WHERE { <" + cls.toStringID() + "> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parentClass }";
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			parents.add(df.getOWLClass(IRI.create(qs.getResource("parentClass").getURI())));
		}
		return parents;
	}

	/**
	 * Returns a set of classes which are children of current class
	 * in the class hierarchy.
	 * @param cls
	 * @param limit
	 * @return
	 */
	public Set<OWLClass> getChildClasses(OWLClass cls) {
		Set<OWLClass> children = new HashSet<OWLClass>();
		String query = "SELECT DISTINCT ?childClass WHERE { ?childClass <http://www.w3.org/2000/01/rdf-schema#subClassOf> <" + cls.toStringID() + ">}";
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			children.add(df.getOWLClass(IRI.create(qs.getResource("childClass").getURI())));
		}
		return children;
	}

	@Override
	public boolean hasTypeImpl(OWLClassExpression description, OWLIndividual individual) {
		if(!description.isAnonymous()){
			throw new UnsupportedOperationException("Only named classes are supported.");
		}
		String query = String.format("ASK {<%s> a <%s>}", individual.toString(), ((OWLClass)description).toStringID());
		boolean hasType = executeAskQuery(query);
		return hasType;
	}

	@Override
	public SortedSet<OWLIndividual> hasTypeImpl(OWLClassExpression description, Set<OWLIndividual> individuals) {
		SortedSet<OWLIndividual> allIndividuals = getIndividuals(description);
		allIndividuals.retainAll(individuals);
		return allIndividuals;
	}

	@Override
	public SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression description) {
		return getIndividuals(description, 0);
	}

	public SortedSet<OWLIndividual> getIndividuals(OWLClassExpression description, int limit) {
		OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
		
//		if(!(OWLClassExpression instanceof NamedClass)){
//			throw new UnsupportedOperationException("Only named classes are supported.");
//		}
		SortedSet<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
//		String query = String.format("SELECT DISTINCT ?ind WHERE {?ind a <%s>}", ((OWLClass)description).toStringID());
		String query = converter.asQuery("?ind", description, false).toString();//System.out.println(query);
		if(limit != 0) {
			query += " LIMIT " + limit;
		}
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("ind").isURIResource()){
				individuals.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI())));
			}
		}
		return individuals;
	}	

	/**
	 * @param wantedClass
	 * @param excludeClass
	 * @param limit
	 * @return get individual of class wantedClass excluding all individual of type excludeClass
	 * @author sherif
	 */
	public SortedSet<OWLIndividual> getIndividualsExcluding(OWLClassExpression wantedClass, OWLClassExpression excludeClass, int limit) {
		if(wantedClass.isAnonymous()){
			throw new UnsupportedOperationException("Only named classes are supported.");
		}
		SortedSet<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
		String query = 
				"SELECT DISTINCT ?ind WHERE {" +
						"?ind a <"+((OWLClass)wantedClass).toStringID() + "> . " +
						"FILTER NOT EXISTS { ?ind a <" + ((OWLClass)excludeClass).toStringID() + "> } }";
		if(limit != 0) {
			query += " LIMIT " + limit;
		}
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("ind").isURIResource()){
				individuals.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI())));
			}
		}
		return individuals;
	}	
	
	/**
	 * @param cls
	 * @param limit
	 * @return Random Individuals not including any of the input class individuals
	 * @author sherif
	 */
	public SortedSet<OWLIndividual> getRandomIndividuals(OWLClass cls, int limit) {
		SortedSet<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
		String query = 
				" SELECT DISTINCT ?ind WHERE {"+
						"?ind ?p ?o ."+
						"FILTER(NOT EXISTS { ?ind a <" + cls.toStringID() + "> } ) }";
		if(limit != 0) {
			query += " LIMIT " + limit;
		}
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("ind").isURIResource()){
				individuals.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI())));
			}
		}
		return individuals;
	}	
	
	/**
	 * @param cls
	 * @param limit
	 * @return Random Individuals not including any of the input classes individuals
	 * @author sherif
	 */
	public SortedSet<OWLIndividual> getRandomIndividuals(Set<OWLClass> cls, int limit) {
		SortedSet<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
		
		String filterStr="";
		for(OWLClass nc : cls){
			filterStr = filterStr.concat("FILTER(NOT EXISTS { ?ind a <").concat(nc.toStringID()).concat("> } ) ");
		}
		
		String query = 
				" SELECT DISTINCT ?ind WHERE {"+
						"?ind a ?o .?o <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.w3.org/2002/07/owl#Class>"+
						filterStr+ " }";
		if(limit != 0) {
			query += " LIMIT " + limit;
		}
		
		System.out.println("!!!!!!!!!!!!!!!!!!!! "+query);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("ind").isURIResource()){
				individuals.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI())));
			}
		}
		return individuals;
	}

	/**
	 * @param cls
	 * @param limit
	 * @return Super class of the input class Individuals not including any of the input class individuals
	 * @author sherif
	 */
	public SortedSet<OWLIndividual> getSuperClassIndividuals(OWLClass cls, int limit) {
		SortedSet<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
		Set<OWLClass> parentClasses = getParentClasses(cls);

		for(OWLClass parentClass : parentClasses){
			String query = 
					" SELECT DISTINCT ?ind WHERE { "+
							"?ind a <" + parentClass.toStringID() + "> ."+
							"FILTER(NOT EXISTS { ?ind a <" + cls.toStringID() + "> } ) }";
			
			
			if(limit != 0) {
				query += " LIMIT " + limit/parentClasses.size();
			}
			
			System.out.println("----------------------------------------------  "+query);
			
			ResultSet rs = executeSelectQuery(query);
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				if(qs.get("ind").isURIResource()){
					individuals.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI())));
				}
			}
			System.out.println(individuals.size());
			System.out.println(individuals);
		}
		
		return individuals;
	}

	@Override
	public SortedSetTuple<OWLIndividual> doubleRetrievalImpl(OWLClassExpression description) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<OWLIndividual> getRelatedIndividualsImpl(OWLIndividual individual, OWLObjectProperty objectProperty) {
		Set<OWLIndividual> individuals = new HashSet<OWLIndividual>();
		String query = String.format("SELECT ?ind WHERE {<%s> <%s> ?ind, FILTER(isIRI(?ind))}", individual.toStringID(), objectProperty.toStringID());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			individuals.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI())));
		}
		return individuals;
	}

	@Override
	public Set<OWLLiteral> getRelatedValuesImpl(OWLIndividual individual, OWLDataProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OWLObjectProperty, Set<OWLIndividual>> getObjectPropertyRelationshipsImpl(OWLIndividual individual) {
		Map<OWLObjectProperty, Set<OWLIndividual>> prop2individuals = new HashMap<OWLObjectProperty, Set<OWLIndividual>>();
		String query = String.format("SELECT ?prop ?ind WHERE {" +
				"<%s> ?prop ?ind." +
				" FILTER(isIRI(?ind) && ?prop != <%s> && ?prop != <%s>)}", 
				individual.toStringID(), RDF.type.getURI(), OWL.sameAs.getURI());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		Set<OWLIndividual> individuals;
		OWLObjectProperty property;
		OWLIndividual ind;
		while(rs.hasNext()){
			qs = rs.next();
			ind = df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI()));
			property = df.getOWLObjectProperty(IRI.create(qs.getResource("prop").getURI()));
			individuals = prop2individuals.get(property);
			if(individuals == null){
				individuals = new HashSet<OWLIndividual>();
				prop2individuals.put(property, individuals);
			}
			individuals.add(ind);

		}
		return prop2individuals;
	}

	@Override
	public Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembersImpl(OWLObjectProperty objectProperty) {
		Map<OWLIndividual, SortedSet<OWLIndividual>> subject2objects = new HashMap<OWLIndividual, SortedSet<OWLIndividual>>();
		String query = String.format("SELECT ?s ?o WHERE {" +
				"?s <%s> ?o." +
				" FILTER(isIRI(?o))}", 
				objectProperty.toStringID());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		OWLIndividual sub;
		OWLIndividual obj;
		SortedSet<OWLIndividual> objects;
		while(rs.hasNext()){
			qs = rs.next();
			sub = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			obj = df.getOWLNamedIndividual(IRI.create(qs.getResource("o").getURI()));
			objects = subject2objects.get(sub);
			if(objects == null){
				objects = new TreeSet<OWLIndividual>();
				subject2objects.put(sub, objects);
			}
			objects.add(obj);

		}
		return subject2objects;
	}

	@Override
	public Map<OWLIndividual, SortedSet<OWLLiteral>> getDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OWLIndividual, SortedSet<Double>> getDoubleDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		Map<OWLIndividual, SortedSet<Double>> subject2objects = new HashMap<OWLIndividual, SortedSet<Double>>();
		String query = String.format("SELECT ?s ?o WHERE {" +
				"?s <%s> ?o." +
				" FILTER(DATATYPE(?o) = <%s>)}", 
				datatypeProperty.toStringID(), XSD.DOUBLE.toStringID());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		OWLIndividual sub;
		Double obj;
		SortedSet<Double> objects;
		while(rs.hasNext()){
			qs = rs.next();
			sub = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			obj = qs.getLiteral("o").getDouble();
			objects = subject2objects.get(sub);
			if(objects == null){
				objects = new TreeSet<Double>();
				subject2objects.put(sub, objects);
			}
			objects.add(obj);

		}
		return subject2objects;
	}

	@Override
	public Map<OWLIndividual, SortedSet<Integer>> getIntDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		Map<OWLIndividual, SortedSet<Integer>> subject2objects = new HashMap<OWLIndividual, SortedSet<Integer>>();
		String query = String.format("SELECT ?s ?o WHERE {" +
				"?s <%s> ?o." +
				" FILTER(DATATYPE(?o) = <%s>)}", 
				datatypeProperty.toStringID(), XSD.INT.toStringID());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		OWLIndividual sub;
		Integer obj;
		SortedSet<Integer> objects;
		while(rs.hasNext()){
			qs = rs.next();
			sub = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			obj = qs.getLiteral("o").getInt();
			objects = subject2objects.get(sub);
			if(objects == null){
				objects = new TreeSet<Integer>();
				subject2objects.put(sub, objects);
			}
			objects.add(obj);

		}
		return subject2objects;
	}

	@Override
	public Map<OWLIndividual, SortedSet<Boolean>> getBooleanDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		Map<OWLIndividual, SortedSet<Boolean>> subject2objects = new HashMap<OWLIndividual, SortedSet<Boolean>>();
		String query = String.format("SELECT ?s ?o WHERE {" +
				"?s <%s> ?o." +
				" FILTER(DATATYPE(?o) = <%s>)}", 
				datatypeProperty.toStringID(), XSD.BOOLEAN.toStringID());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		OWLIndividual sub;
		Boolean obj;
		SortedSet<Boolean> objects;
		while(rs.hasNext()){
			qs = rs.next();
			sub = df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI()));
			obj = qs.getLiteral("o").getBoolean();
			objects = subject2objects.get(sub);
			if(objects == null){
				objects = new TreeSet<Boolean>();
				subject2objects.put(sub, objects);
			}
			objects.add(obj);

		}
		return subject2objects;
	}

	@Override
	public SortedSet<OWLIndividual> getTrueDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		SortedSet<OWLIndividual> members = new TreeSet<OWLIndividual>();
		String query = String.format("SELECT ?ind WHERE {" +
				"?ind <%s> ?o." +
				" FILTER(isLiteral(?o) && DATATYPE(?o) = <%s> && ?o = %s)}", 
				datatypeProperty.toStringID(), XSD.BOOLEAN.toStringID(),
				"\"true\"^^<" + XSD.BOOLEAN.toStringID() + ">");

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			members.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI())));

		}
		return members;
	}

	@Override
	public SortedSet<OWLIndividual> getFalseDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		SortedSet<OWLIndividual> members = new TreeSet<OWLIndividual>();
		String query = String.format("SELECT ?ind WHERE {" +
				"?ind <%s> ?o." +
				" FILTER(isLiteral(?o) && DATATYPE(?o) = <%s> && ?o = %s)}", 
				datatypeProperty.toStringID(), XSD.BOOLEAN.toStringID(),
				"\"false\"^^<"+XSD.BOOLEAN.toStringID() + ">");

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			members.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("ind").getURI())));

		}
		return members;
	}

	@Override
	public Map<OWLIndividual, SortedSet<String>> getStringDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClass> getInconsistentClassesImpl() {
		throw new UnsupportedOperationException();
	}

	@Override
	public OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
		String query = String.format("SELECT ?domain WHERE {" +
				"<%s> <%s> ?domain. FILTER(isIRI(?domain))" +
				"}", 
				objectProperty.toStringID(), RDFS.domain.getURI());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		SortedSet<OWLClassExpression> domains = new TreeSet<OWLClassExpression>();
		while(rs.hasNext()){
			qs = rs.next();
			domains.add(df.getOWLClass(IRI.create(qs.getResource("domain").getURI())));

		}
		if(domains.size() == 1){
			return domains.first();
		} else if(domains.size() > 1){
			return df.getOWLObjectIntersectionOf(domains);
		} 
		return null;
	}
	
	public Set<OWLObjectProperty> getObjectPropertiesWithDomain(OWLClass domain) {
		Set<OWLObjectProperty> properties = new TreeSet<>();
		
		String query = "SELECT ?p WHERE {?p <http://www.w3.org/2000/01/rdf-schema#domain> <" + domain + ">.}";
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			properties.add(df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI())));
		}
		
		return properties;
	}
	
	public Set<OWLObjectProperty> getObjectProperties(OWLClass cls) {
		Set<OWLObjectProperty> properties = new TreeSet<>();
		
		String query = "SELECT DISTINCT ?p WHERE {?s a <" + cls + ">. ?s ?p ?o}";
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			properties.add(df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI())));
		}
		
		return properties;
	}
	
	public SortedSet<OWLClass> getDomains(OWLObjectProperty objectProperty) {
		String query = String.format("SELECT ?domain WHERE {" +
				"<%s> <%s> ?domain. FILTER(isIRI(?domain))" +
				"}", 
				objectProperty.toStringID(), RDFS.domain.getURI());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		SortedSet<OWLClass> domains = new TreeSet<OWLClass>();
		while(rs.hasNext()){
			qs = rs.next();
			domains.add(df.getOWLClass(IRI.create(qs.getResource("domain").getURI())));

		}
		return domains;
	}

	@Override
	public OWLClassExpression getDomainImpl(OWLDataProperty datatypeProperty) {
		String query = String.format("SELECT ?domain WHERE {" +
				"<%s> <%s> ?domain. FILTER(isIRI(?domain))" +
				"}", 
				datatypeProperty.toStringID(), RDFS.domain.getURI());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		SortedSet<OWLClassExpression> domains = new TreeSet<OWLClassExpression>();
		while(rs.hasNext()){
			qs = rs.next();
			domains.add(df.getOWLClass(IRI.create(qs.getResource("domain").getURI())));

		}
		if(domains.size() == 1){
			return domains.first();
		} else if(domains.size() > 1){
			return df.getOWLObjectIntersectionOf(domains);
		} 
		return null;
	}

	@Override
	public OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
		String query = String.format("SELECT ?range WHERE {" +
				"<%s> <%s> ?range. FILTER(isIRI(?range))" +
				"}", 
				objectProperty.toStringID(), RDFS.range.getURI());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		SortedSet<OWLClassExpression> domains = new TreeSet<OWLClassExpression>();
		while(rs.hasNext()){
			qs = rs.next();
			domains.add(df.getOWLClass(IRI.create(qs.getResource("range").getURI())));

		}
		if(domains.size() == 1){
			return domains.first();
		} else if(domains.size() > 1){
			return df.getOWLObjectIntersectionOf(domains);
		} 
		return null;
	}
	
	public SortedSet<OWLClass> getRanges(OWLObjectProperty objectProperty) {
		String query = String.format("SELECT ?range WHERE {" +
				"<%s> <%s> ?range. FILTER(isIRI(?range))" +
				"}", 
				objectProperty.toStringID(), RDFS.range.getURI());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		SortedSet<OWLClass> ranges = new TreeSet<OWLClass>();
		while(rs.hasNext()){
			qs = rs.next();
			ranges.add(df.getOWLClass(IRI.create(qs.getResource("range").getURI())));
		}
		return ranges;
	}

	public boolean isObjectProperty(String propertyURI){
		String query = String.format("ASK {<%s> a <%s>}", propertyURI, OWL.ObjectProperty.getURI());
		boolean isObjectProperty = executeAskQuery(query);
		return isObjectProperty;
	}

	public boolean isObjectProperty(String propertyURI, boolean analyzeData){
		String query = String.format("ASK {<%s> a <%s>}", propertyURI, OWL.ObjectProperty.getURI());
		boolean isObjectProperty = executeAskQuery(query);
		if(!isObjectProperty && analyzeData){
			query = String.format("ASK {?s <%s> ?o.FILTER(isURI(?o))}", propertyURI);
			isObjectProperty = executeAskQuery(query);
		}
		return isObjectProperty;
	}

	public boolean isDataProperty(String propertyURI){
		if(propertyURI.equals("http://www.w3.org/2000/01/rdf-schema#label")) return true;
		String query = String.format("ASK {<%s> a <%s>}", propertyURI, OWL.DatatypeProperty.getURI());
		boolean isDataProperty = executeAskQuery(query);
		return isDataProperty;
	}

	public boolean isDataProperty(String propertyURI, boolean analyzeData){
		if(propertyURI.equals("http://www.w3.org/2000/01/rdf-schema#label")) return true;
		String query = String.format("ASK {<%s> a <%s>}", propertyURI, OWL.DatatypeProperty.getURI());
		boolean isDataProperty = executeAskQuery(query);
		if(!isDataProperty && analyzeData){
			query = String.format("ASK {?s <%s> ?o.FILTER(isLITERAL(?o))}", propertyURI);
			isDataProperty = executeAskQuery(query);
		}
		return isDataProperty;
	}

	public int getIndividualsCount(OWLClass cls){
		String query = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {?s a <%s>.}", cls.toStringID());
		ResultSet rs = executeSelectQuery(query);
		int cnt = rs.next().get(rs.getResultVars().get(0)).asLiteral().getInt();
		return cnt;

	}

	public int getPropertyCount(OWLObjectProperty property){
		String query = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o.}", property.toStringID());
		ResultSet rs = executeSelectQuery(query);
		int cnt = rs.next().get(rs.getResultVars().get(0)).asLiteral().getInt();
		return cnt;

	}

	public SortedSet<OWLObjectProperty> getInverseObjectProperties(OWLObjectProperty property){
		SortedSet<OWLObjectProperty> inverseObjectProperties = new TreeSet<OWLObjectProperty>();
		String query = "SELECT ?p WHERE {" +
				"{<%p> <%ax> ?p.} UNION {?p <%ax> <%p>}}".replace("%p", property.toStringID()).replace("%ax", OWL.inverseOf.getURI());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			inverseObjectProperties.add(df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI())));

		}
		return inverseObjectProperties;
	}

	@Override
	public OWLDataRange getRangeImpl(OWLDataProperty datatypeProperty) {
		String query = String.format("SELECT ?range WHERE {" +
				"<%s> <%s> ?range. FILTER(isIRI(?range))" +
				"}", 
				datatypeProperty.toStringID(), RDFS.range.getURI());

		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		OWLDataRange range = null;
		while(rs.hasNext()){
			qs = rs.next();
			range = df.getOWLDatatype(IRI.create(qs.getResource("range").getURI()));

		}
		return range;
	}

	@Override
	public boolean isSuperClassOfImpl(OWLClassExpression superClass, OWLClassExpression subClass) {
		if(subClass.isAnonymous() || superClass.isAnonymous()){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		String query = String.format("ASK {<%s> <%s> <%s>.}", 
				((OWLClass)subClass).toStringID(),
				RDFS.subClassOf.getURI(),
				((OWLClass)superClass).toStringID());
		boolean superClassOf = executeAskQuery(query);
		return superClassOf;
	}

	@Override
	public boolean isEquivalentClassImpl(OWLClassExpression class1, OWLClassExpression class2) {
		if(class1.isAnonymous() || class2.isAnonymous()){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		String query = String.format("ASK {<%s> <%s> <%s>.}", 
				((OWLClass)class1).toStringID(),
				OWL.equivalentClass.getURI(),
				((OWLClass)class2).toStringID());
		boolean equivalentClass = executeAskQuery(query);
		return equivalentClass;
	}

	@Override
	public Set<OWLClassExpression> getAssertedDefinitions(OWLClass namedClass) {
		Set<OWLClassExpression> definitions = new HashSet<OWLClassExpression>();
		String query = String.format("SELECT ?class { {<%s> <%s> ?class. FILTER(isIRI(?class))} UNION {?class <%s> <%s>. FILTER(isIRI(?class))} }", 
				namedClass.toStringID(),
				OWL.equivalentClass.getURI(),
				OWL.equivalentClass.getURI(),
				namedClass.toStringID()	
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			definitions.add(df.getOWLClass(IRI.create(qs.getResource("class").getURI())));
		}
		return definitions;
	}

	@Override
	public Set<OWLClassExpression> isSuperClassOfImpl(Set<OWLClassExpression> superClasses, OWLClassExpression subClasses) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public SortedSet<OWLClassExpression> getMostGeneralClasses() {
		return hierarchy.getMostGeneralClasses();
	}
	
	public SortedSet<OWLClass> getMostSpecificClasses() {
		SortedSet<OWLClass> classes = new TreeSet<>();
		String query = "SELECT ?cls WHERE {?cls a <http://www.w3.org/2002/07/owl#Class>. "
				+ "FILTER NOT EXISTS{?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?cls. FILTER(?sub != <http://www.w3.org/2002/07/owl#Nothing>)}}";
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			classes.add(df.getOWLClass(IRI.create(qs.getResource("cls").getURI())));
		}
		return classes;
	}

	@Override
	public SortedSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression description) {
		if(description.isAnonymous()){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		SortedSet<OWLClassExpression> superClasses = new TreeSet<OWLClassExpression>();
		String query = String.format("SELECT ?sup {<%s> <%s> ?sup. FILTER(isIRI(?sup))}", 
				description.asOWLClass().toStringID(),
				RDFS.subClassOf.getURI()
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		String uri = null;
		while(rs.hasNext()){
			qs = rs.next();
			uri = qs.getResource("sup").getURI();
			if(uri.equals(df.getOWLThing().toStringID())){
				superClasses.add(df.getOWLThing());
			} else {
				superClasses.add(df.getOWLClass(IRI.create(uri)));
			}
		}
		superClasses.remove(description);
		return superClasses;
	}

	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression description, boolean direct){
		if(description.isAnonymous()){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		SortedSet<OWLClassExpression> superClasses = new TreeSet<OWLClassExpression>();
		String query;
		if(direct){
			query = String.format("SELECT ?sup {<%s> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sup. FILTER(isIRI(?sup))}", 
					description.asOWLClass().toStringID());
		} else {
			query = String.format("SELECT ?sub {<%s> <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?sup. FILTER(isIRI(?sup))}", 
					description.asOWLClass().toStringID());
		}
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superClasses.add(df.getOWLClass(IRI.create(qs.getResource("sub").getURI())));
		}
		superClasses.remove(description);

		return superClasses;
	}

	@Override
	public SortedSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression description) {
		return getSubClasses(description, true);
	}

	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression description, boolean direct) {
		if(description.isAnonymous()){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		SortedSet<OWLClassExpression> subClasses = new TreeSet<OWLClassExpression>();
		String query;
		if(direct){
			query = String.format("SELECT ?sub {?sub <%s> <%s>. FILTER(isIRI(?sub))}", 
					RDFS.subClassOf.getURI(), description.asOWLClass().toStringID());
		} else {
			query = String.format("SELECT ?sub {?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <%s>. }", 
					description.asOWLClass().toStringID());
		}
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subClasses.add(df.getOWLClass(IRI.create(qs.getResource("sub").getURI())));
		}
		subClasses.remove(description);
		return subClasses;
	}

	@Override
	public SortedSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty objectProperty) {
		SortedSet<OWLObjectProperty> superProperties = new TreeSet<OWLObjectProperty>();
		String query = String.format("SELECT ?sup {<%s> <%s> ?sup. FILTER(isIRI(?sup))}", 
				objectProperty.toStringID(),
				RDFS.subPropertyOf.getURI()
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superProperties.add(df.getOWLObjectProperty(IRI.create(qs.getResource("sup").getURI())));
		}
		return superProperties;
	}

	@Override
	public SortedSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty objectProperty) {
		SortedSet<OWLObjectProperty> subProperties = new TreeSet<OWLObjectProperty>();
		String query = String.format("SELECT ?sub {?sub <%s> <%s>. FILTER(isIRI(?sub))}", 
				RDFS.subPropertyOf.getURI(),
				objectProperty.toStringID()

				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subProperties.add(df.getOWLObjectProperty(IRI.create(qs.getResource("sub").getURI())));
		}
		return subProperties;
	}

	public SortedSet<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty objectProperty) {
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>();
		String query = String.format("SELECT ?equ {<%s> <%s> ?equ. FILTER(isIRI(?equ))}", 
				objectProperty.toStringID(),
				OWL.equivalentProperty.getURI()
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			properties.add(df.getOWLObjectProperty(IRI.create(qs.getResource("equ").getURI())));
		}
		return properties;
	}
	
	public SortedSet<OWLObjectProperty> getDisjointProperties(OWLObjectProperty objectProperty) {
		SortedSet<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>();
		String query = String.format("SELECT ?equ {<%s> <%s> ?p. FILTER(isIRI(?p))}", 
				objectProperty.toStringID(),
				OWL2.propertyDisjointWith.getURI()
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			properties.add(df.getOWLObjectProperty(IRI.create(qs.getResource("p").getURI())));
		}
		return properties;
	}

	public SortedSet<OWLDataProperty> getEquivalentProperties(OWLDataProperty objectProperty) {
		SortedSet<OWLDataProperty> superProperties = new TreeSet<OWLDataProperty>();
		String query = String.format("SELECT ?equ {<%s> <%s> ?equ. FILTER(isIRI(?equ))}", 
				objectProperty.toStringID(),
				OWL.equivalentProperty.getURI()
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superProperties.add(df.getOWLDataProperty(IRI.create(qs.getResource("equ").getURI())));
		}
		return superProperties;
	}

	@Override
	public SortedSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty dataProperty) {
		SortedSet<OWLDataProperty> superProperties = new TreeSet<OWLDataProperty>();
		String query = String.format("SELECT ?sup {<%s> <%s> ?sup. FILTER(isIRI(?sup))}", 
				dataProperty.toStringID(),
				RDFS.subPropertyOf.getURI()
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superProperties.add(df.getOWLDataProperty(IRI.create(qs.getResource("sup").getURI())));
		}
		return superProperties;
	}

	@Override
	public SortedSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty dataProperty) {
		SortedSet<OWLDataProperty> subProperties = new TreeSet<OWLDataProperty>();
		String query = String.format("SELECT ?sub {?sub <%s> <%s>. FILTER(isIRI(?sub))}", 
				RDFS.subPropertyOf.getURI(),
				dataProperty.toStringID()
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subProperties.add(df.getOWLDataProperty(IRI.create(qs.getResource("sub").getURI())));
		}
		return subProperties;
	}
	
	public SortedSet<OWLDataProperty> getDisjointProperties(OWLDataProperty objectProperty) {
		SortedSet<OWLDataProperty> properties = new TreeSet<OWLDataProperty>();
		String query = String.format("SELECT ?equ {<%s> <%s> ?p. FILTER(isIRI(?p))}", 
				objectProperty.toStringID(),
				OWL2.propertyDisjointWith.getURI()
				);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			properties.add(df.getOWLDataProperty(IRI.create(qs.getResource("p").getURI())));
		}
		return properties;
	}

	protected ResultSet executeSelectQuery(String queryString){
		logger.trace("Sending query \n {}", queryString);
		Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
		QueryExecution qe = qef.createQueryExecution(query);
		try
		{
		ResultSet rs = qe.execSelect();
		return rs;
		}
		catch(QueryExceptionHTTP e)
		{
			throw new QueryExceptionHTTP("Error sending query \""+query+"\" to endpoint "+ qef.getId(),e);
		}
	}

	protected ResultSet executeSelectQuery(String query, long timeout){
		logger.trace("Sending query \n {}", query);
		QueryExecution qe = qef.createQueryExecution(query);
		qe.setTimeout(timeout);
		ResultSet rs = qe.execSelect();
		return rs;
	}
	
	protected boolean executeAskQuery(String query){
		logger.trace("Sending query \n {}", query);
		QueryExecution qe = qef.createQueryExecution(query);
		boolean ret = qe.execAsk();
		return ret;
	}

	/**
	 * Returns TRUE if the class hierarchy was computed before.
	 * @return
	 */
	public boolean isPrepared(){
		return hierarchy != null;
	}

	public void setCache(ExtractionDBCache cache) {
//		this.cache = cache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
	
	public boolean supportsSPARQL1_1(){
		try {
			String query = "SELECT ?s WHERE {?s a <http://foo.org/A> . FILTER NOT EXISTS {?s a <http://foo.org/B>}} LIMIT 1";
			ResultSet rs = executeSelectQuery(query);
			return true;
		} catch (Exception e) {
			logger.error("Endpoint does not support SPARQL 1.1, e.g. FILTER NOT EXISTS", e);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#getNamedClasses()
	 */
	@Override
	public SortedSet<OWLClass> getClasses() {
		SortedSet<OWLClass> classes = new TreeSet<OWLClass>();
		String query = "SELECT DISTINCT ?cls WHERE {?s a ?cls .}";
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			classes.add(df.getOWLClass(IRI.create(qs.getResource("cls").getURI())));
		}
		return classes;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#getObjectProperties()
	 */
	@Override
	public Set<OWLObjectProperty> getObjectProperties() {
		return getOWLObjectProperties();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#getIndividuals()
	 */
	@Override
	public SortedSet<OWLIndividual> getIndividuals() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#getBaseURI()
	 */
	@Override
	public String getBaseURI() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#getPrefixes()
	 */
	@Override
	public Map<String, String> getPrefixes() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		classPopularityMap = new HashMap<OWLClass, Integer>();
		objectPropertyPopularityMap = new HashMap<OWLObjectProperty, Integer>();
		dataPropertyPopularityMap = new HashMap<OWLDataProperty, Integer>();
		individualPopularityMap = new HashMap<OWLIndividual, Integer>();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractReasonerComponent#getReasonerType()
	 */
	@Override
	public ReasonerType getReasonerType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractReasonerComponent#releaseKB()
	 */
	@Override
	public void releaseKB() {
	}

}
