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

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.http.QueryExecutionFactoryHttp;
import org.aksw.commons.sparql.api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.commons.util.strings.StringUtils;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.IndividualReasoner;
import org.dllearner.core.SchemaReasoner;
import org.dllearner.core.config.BooleanEditor;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.Property;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.owl.ConceptComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clarkparsia.owlapiv3.XSD;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@ComponentAnn(name = "SPARQL Reasoner", shortName = "spr", version = 0.1)
public class SPARQLReasoner implements SchemaReasoner, IndividualReasoner{
	
	private static final Logger logger = LoggerFactory.getLogger(SPARQLReasoner.class);
	
	@ConfigOption(name = "useCache", description = "Whether to use a DB cache", defaultValue = "true", required = false, propertyEditorClass = BooleanEditor.class)
	private boolean useCache = true;
	
	private ExtractionDBCache cache;
	
	private SparqlEndpointKS ks;
	private ClassHierarchy hierarchy;
	private OntModel model;
	
	private Map<NamedClass, Integer> classPopularityMap;
	private Map<ObjectProperty, Integer> objectPropertyPopularityMap;
	private Map<DatatypeProperty, Integer> dataPropertyPopularityMap;
	
	
	public SPARQLReasoner(SparqlEndpointKS ks) {
		this.ks = ks;
		
		if(useCache){
			cache = new ExtractionDBCache("cache");
		}
		classPopularityMap = new HashMap<NamedClass, Integer>();
		objectPropertyPopularityMap = new HashMap<ObjectProperty, Integer>();
	}
	
	public SPARQLReasoner(SparqlEndpointKS ks, ExtractionDBCache cache) {
		this.ks = ks;
		this.cache = cache;
		
		classPopularityMap = new HashMap<NamedClass, Integer>();
		objectPropertyPopularityMap = new HashMap<ObjectProperty, Integer>();
	}
	
	public SPARQLReasoner(OntModel model) {
		this.model = model;
		
		classPopularityMap = new HashMap<NamedClass, Integer>();
		objectPropertyPopularityMap = new HashMap<ObjectProperty, Integer>();
	}
	
	public void precomputePopularity(){
		precomputeClassPopularity();
		precomputeDataPropertyPopularity();
		precomputeObjectPropertyPopularity();
	}
	
	public void precomputeClassPopularity(){
		logger.info("Precomputing class popularity ...");
		
		Set<NamedClass> classes = new SPARQLTasks(ks.getEndpoint()).getAllClasses();
		String queryTemplate = "SELECT (COUNT(*) AS ?cnt) WHERE {?s a <%s>}";
		
		ResultSet rs;
		for(NamedClass nc : classes){
			rs = executeSelectQuery(String.format(queryTemplate, nc.getName()));
			int cnt = rs.next().getLiteral("cnt").getInt();
			classPopularityMap.put(nc, cnt);
		}
	}
	
	public void precomputeObjectPropertyPopularity(){
		logger.info("Precomputing object property popularity ...");
		objectPropertyPopularityMap = new HashMap<ObjectProperty, Integer>();
		
		Set<ObjectProperty> properties = new SPARQLTasks(ks.getEndpoint()).getAllObjectProperties();
		String queryTemplate = "SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}";
		
		ResultSet rs;
		for(ObjectProperty op : properties){
			rs = executeSelectQuery(String.format(queryTemplate, op.getName()));
			int cnt = rs.next().getLiteral("cnt").getInt();
			objectPropertyPopularityMap.put(op, cnt);
		}
	}
	
	public void precomputeDataPropertyPopularity(){
		logger.info("Precomputing data property popularity ...");
		dataPropertyPopularityMap = new HashMap<DatatypeProperty, Integer>();
		
		Set<DatatypeProperty> properties = new SPARQLTasks(ks.getEndpoint()).getAllDataProperties();
		String queryTemplate = "SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}";
		
		ResultSet rs;
		for(DatatypeProperty dp : properties){
			rs = executeSelectQuery(String.format(queryTemplate, dp.getName()));
			int cnt = rs.next().getLiteral("cnt").getInt();
			dataPropertyPopularityMap.put(dp, cnt);
		}
	}
	
	public int getSubjectCountForProperty(Property p, long timeout){
		int cnt = -1;
		String query = String.format(
				"SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s <%s> ?o.}",
				p.getName());
		ResultSet rs = executeSelectQuery(query, timeout);
		if(rs.hasNext()){
			cnt = rs.next().getLiteral("cnt").getInt();
		}
		
		return cnt;
	}
	
	public int getObjectCountForProperty(ObjectProperty p, long timeout){
		int cnt = -1;
		String query = String.format(
				"SELECT (COUNT(DISTINCT ?o) AS ?cnt) WHERE {?s <%s> ?o.}",
				p.getName());
		ResultSet rs = executeSelectQuery(query, timeout);
		if(rs.hasNext()){
			cnt = rs.next().getLiteral("cnt").getInt();
		}
		
		return cnt;
	}
	
	public int getPopularity(NamedClass nc){
		if(classPopularityMap != null && classPopularityMap.containsKey(nc)){
			return classPopularityMap.get(nc);
		} else {
			System.out.println("Cache miss: " + nc);
			String queryTemplate = "SELECT (COUNT(*) AS ?cnt) WHERE {?s a <%s>}";
			
			ResultSet rs = executeSelectQuery(String.format(queryTemplate, nc.getName()));
			int cnt = rs.next().getLiteral("cnt").getInt();
			classPopularityMap.put(nc, cnt);
			return cnt;
		}
		
	}
	
	public int getPopularity(ObjectProperty op){
		if(objectPropertyPopularityMap != null && objectPropertyPopularityMap.containsKey(op)){
			return objectPropertyPopularityMap.get(op);
		} else {
			System.out.println("Cache miss: " + op);
			String queryTemplate = "SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}";
			
			ResultSet rs = executeSelectQuery(String.format(queryTemplate, op.getName()));
			int cnt = rs.next().getLiteral("cnt").getInt();
			objectPropertyPopularityMap.put(op, cnt);
			return cnt;
		}
		
	}
	
	public int getPopularity(DatatypeProperty dp){
		if(dataPropertyPopularityMap.containsKey(dp)){
			return dataPropertyPopularityMap.get(dp);
		} else {
			System.out.println("Cache miss: " + dp);
			String queryTemplate = "SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}";
			
			ResultSet rs = executeSelectQuery(String.format(queryTemplate, dp.getName()));
			int cnt = rs.next().getLiteral("cnt").getInt();
			dataPropertyPopularityMap.put(dp, cnt);
			return cnt;
		}
		
	}
	
	public final ClassHierarchy prepareSubsumptionHierarchy() {
		logger.info("Preparing subsumption hierarchy ...");
		long startTime = System.currentTimeMillis();
		ConceptComparator conceptComparator = new ConceptComparator();
		TreeMap<Description, SortedSet<Description>> subsumptionHierarchyUp = new TreeMap<Description, SortedSet<Description>>(
				conceptComparator);
		TreeMap<Description, SortedSet<Description>> subsumptionHierarchyDown = new TreeMap<Description, SortedSet<Description>>(
				conceptComparator);

		// parents/children of top ...
		SortedSet<Description> tmp = getSubClasses(Thing.instance);
		subsumptionHierarchyUp.put(Thing.instance, new TreeSet<Description>(conceptComparator));
		subsumptionHierarchyDown.put(Thing.instance, tmp);

		// ... bottom ...
		tmp = getSuperClasses(Nothing.instance);
		subsumptionHierarchyUp.put(Nothing.instance, tmp);
		subsumptionHierarchyDown.put(Nothing.instance, new TreeSet<Description>(conceptComparator));
		
		// ... and named classes
		Set<NamedClass> atomicConcepts;
		if(ks.isRemote()){
			atomicConcepts = new SPARQLTasks(ks.getEndpoint()).getAllClasses();
		} else {
			atomicConcepts = new TreeSet<NamedClass>();
			for(OntClass cls :  ((LocalModelBasedSparqlEndpointKS)ks).getModel().listClasses().toList()){
				if(!cls.isAnon()){
					atomicConcepts.add(new NamedClass(cls.getURI()));
				}
			}
		}
		
		for (NamedClass atom : atomicConcepts) {
			tmp = getSubClasses(atom);
			// quality control: we explicitly check that no reasoner implementation returns null here
			if(tmp == null) {
				logger.error("Class hierarchy: getSubClasses returned null instead of empty set."); 
			}			
			subsumptionHierarchyDown.put(atom, tmp);

			tmp = getSuperClasses(atom);
			// quality control: we explicitly check that no reasoner implementation returns null here
			if(tmp == null) {
				logger.error("Class hierarchy: getSuperClasses returned null instead of empty set."); 
			}			
			subsumptionHierarchyUp.put(atom, tmp);
		}		
		logger.info("... done in {}ms", (System.currentTimeMillis()-startTime));
		hierarchy = new ClassHierarchy(subsumptionHierarchyUp, subsumptionHierarchyDown);
		return hierarchy;
	}
	
	public final ClassHierarchy prepareSubsumptionHierarchyFast() {
		logger.info("Preparing subsumption hierarchy ...");
		long startTime = System.currentTimeMillis();
		ConceptComparator conceptComparator = new ConceptComparator();
		TreeMap<Description, SortedSet<Description>> subsumptionHierarchyUp = new TreeMap<Description, SortedSet<Description>>(
				conceptComparator);
		TreeMap<Description, SortedSet<Description>> subsumptionHierarchyDown = new TreeMap<Description, SortedSet<Description>>(
				conceptComparator);
		
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
				Description sub = new NamedClass(qs.get("sub").asResource().getURI());
				Description sup = new NamedClass(qs.get("sup").asResource().getURI());
				//add subclasses
				SortedSet<Description> subClasses = subsumptionHierarchyDown.get(sup);
				if(subClasses == null){
					subClasses = new TreeSet<Description>(conceptComparator);
					subsumptionHierarchyDown.put(sup, subClasses);
				}
				subClasses.add(sub);
				//add superclasses
				SortedSet<Description> superClasses = subsumptionHierarchyUp.get(sub);
				if(superClasses == null){
					superClasses = new TreeSet<Description>(conceptComparator);
					subsumptionHierarchyUp.put(sub, superClasses);
				}
				superClasses.add(sup);
			}
			offset += limit;
		}
		
		logger.info("... done in {}ms", (System.currentTimeMillis()-startTime));
		hierarchy = new ClassHierarchy(subsumptionHierarchyUp, subsumptionHierarchyDown);
		return hierarchy;
	}
	
	public Model loadSchema(){
		Model model = ModelFactory.createDefaultModel();
		
		//load class hierarchy
		String query = "CONSTRUCT {?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o} WHERE {?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o.FILTER(!REGEX(STR(?s), 'http://dbpedia.org/class/yago/'))}";
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
	
	private Model loadIncrementally(String query){
		System.out.println(StringUtils.md5Hash(query));
//		try {
			QueryExecutionFactory f = new QueryExecutionFactoryHttp(ks.getEndpoint().getURL().toString(), ks.getEndpoint().getDefaultGraphURIs());
//			f = new QueryExecutionFactoryCache(f, new CacheImpl(CacheCoreH2.create("cache", 60 * 24 *5)));
//			System.out.println("SPARQLReasoner.loadIncrementally needs to be rewritten for aksw-commons 0.1");
//			System.exit(0);
			f = new QueryExecutionFactoryPaginated(f, 1000);
			Model model = f.createQueryExecution(query).execConstruct();
			System.out.println(query);
			System.out.println("Got " + model.size() + " triple.");
			return model;
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return null;
	}

	@Override
	public Set<NamedClass> getTypes(Individual individual) {
		Set<NamedClass> types = new HashSet<NamedClass>();
		String query = String.format("SELECT ?class WHERE {<%s> a ?class.}", individual.getName());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			types.add(new NamedClass(qs.getResource("class").getURI()));
		}
		return types;
	}

	@Override
	public boolean hasType(Description description, Individual individual) {
		if(!(description instanceof NamedClass)){
			throw new UnsupportedOperationException("Only named classes are supported.");
		}
		String query = String.format("ASK {<%s> a <%s>}", individual.toString(), ((NamedClass)description).getName());
		boolean hasType = executeAskQuery(query);
		return hasType;
	}

	@Override
	public SortedSet<Individual> hasType(Description description, Set<Individual> individuals) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedSet<Individual> getIndividuals(Description description) {
		return getIndividuals(description, 0);
	}

	public SortedSet<Individual> getIndividuals(Description description, int limit) {
		if(!(description instanceof NamedClass)){
			throw new UnsupportedOperationException("Only named classes are supported.");
		}
		SortedSet<Individual> individuals = new TreeSet<Individual>();
		String query = String.format("SELECT ?ind WHERE {?ind a <%s>}", ((NamedClass)description).getName());
		if(limit != 0) {
			query += " LIMIT " + limit;
		}
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			individuals.add(new Individual(qs.getResource("ind").getURI()));
		}
		return individuals;
	}	
	
	@Override
	public SortedSetTuple<Individual> doubleRetrieval(Description description) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Individual> getRelatedIndividuals(Individual individual, ObjectProperty objectProperty) {
		Set<Individual> individuals = new HashSet<Individual>();
		String query = String.format("SELECT ?ind WHERE {<%s> <%s> ?ind, FILTER(isIRI(?ind))}", individual.getName(), objectProperty.getName());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			individuals.add(new Individual(qs.getResource("ind").getURI()));
		}
		return individuals;
	}

	@Override
	public Set<Constant> getRelatedValues(Individual individual, DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<ObjectProperty, Set<Individual>> getObjectPropertyRelationships(Individual individual) {
		Map<ObjectProperty, Set<Individual>> prop2individuals = new HashMap<ObjectProperty, Set<Individual>>();
		String query = String.format("SELECT ?prop ?ind WHERE {" +
				"<%s> ?prop ?ind." +
				" FILTER(isIRI(?ind) && ?prop != <%s> && ?prop != <%s>)}", 
				individual.getName(), RDF.type.getURI(), OWL.sameAs.getURI());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		Set<Individual> individuals;
		ObjectProperty property;
		Individual ind;
		while(rs.hasNext()){
			qs = rs.next();
			ind = new Individual(qs.getResource("ind").getURI());
			property = new ObjectProperty(qs.getResource("prop").getURI());
			individuals = prop2individuals.get(property);
			if(individuals == null){
				individuals = new HashSet<Individual>();
				prop2individuals.put(property, individuals);
			}
			individuals.add(ind);
			
		}
		return prop2individuals;
	}

	@Override
	public Map<Individual, SortedSet<Individual>> getPropertyMembers(ObjectProperty objectProperty) {
		Map<Individual, SortedSet<Individual>> subject2objects = new HashMap<Individual, SortedSet<Individual>>();
		String query = String.format("SELECT ?s ?o WHERE {" +
				"?s <%s> ?o." +
				" FILTER(isIRI(?o))}", 
				objectProperty.getName());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		Individual sub;
		Individual obj;
		SortedSet<Individual> objects;
		while(rs.hasNext()){
			qs = rs.next();
			sub = new Individual(qs.getResource("s").getURI());
			obj = new Individual(qs.getResource("o").getURI());
			objects = subject2objects.get(sub);
			if(objects == null){
				objects = new TreeSet<Individual>();
				subject2objects.put(sub, objects);
			}
			objects.add(obj);
			
		}
		return subject2objects;
	}

	@Override
	public Map<Individual, SortedSet<Constant>> getDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Individual, SortedSet<Double>> getDoubleDatatypeMembers(DatatypeProperty datatypeProperty) {
		Map<Individual, SortedSet<Double>> subject2objects = new HashMap<Individual, SortedSet<Double>>();
		String query = String.format("SELECT ?s ?o WHERE {" +
				"?s <%s> ?o." +
				" FILTER(DATATYPE(?o) = <%s>)}", 
				datatypeProperty.getName(), XSD.DOUBLE.toStringID());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		Individual sub;
		Double obj;
		SortedSet<Double> objects;
		while(rs.hasNext()){
			qs = rs.next();
			sub = new Individual(qs.getResource("s").getURI());
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
	public Map<Individual, SortedSet<Integer>> getIntDatatypeMembers(DatatypeProperty datatypeProperty) {
		Map<Individual, SortedSet<Integer>> subject2objects = new HashMap<Individual, SortedSet<Integer>>();
		String query = String.format("SELECT ?s ?o WHERE {" +
				"?s <%s> ?o." +
				" FILTER(DATATYPE(?o) = <%s>)}", 
				datatypeProperty.getName(), XSD.INT.toStringID());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		Individual sub;
		Integer obj;
		SortedSet<Integer> objects;
		while(rs.hasNext()){
			qs = rs.next();
			sub = new Individual(qs.getResource("s").getURI());
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
	public Map<Individual, SortedSet<Boolean>> getBooleanDatatypeMembers(DatatypeProperty datatypeProperty) {
		Map<Individual, SortedSet<Boolean>> subject2objects = new HashMap<Individual, SortedSet<Boolean>>();
		String query = String.format("SELECT ?s ?o WHERE {" +
				"?s <%s> ?o." +
				" FILTER(DATATYPE(?o) = <%s>)}", 
				datatypeProperty.getName(), XSD.BOOLEAN.toStringID());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		Individual sub;
		Boolean obj;
		SortedSet<Boolean> objects;
		while(rs.hasNext()){
			qs = rs.next();
			sub = new Individual(qs.getResource("s").getURI());
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
	public SortedSet<Individual> getTrueDatatypeMembers(DatatypeProperty datatypeProperty) {
		SortedSet<Individual> members = new TreeSet<Individual>();
		String query = String.format("SELECT ?ind WHERE {" +
				"?ind <%s> ?o." +
				" FILTER(isLiteral(?o) && DATATYPE(?o) = <%s> && ?o = %s)}", 
				datatypeProperty.getName(), XSD.BOOLEAN.toStringID(),
				"\"true\"^^<" + XSD.BOOLEAN.toStringID() + ">");
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			members.add(new Individual(qs.getResource("ind").getURI()));
			
		}
		return members;
	}

	@Override
	public SortedSet<Individual> getFalseDatatypeMembers(DatatypeProperty datatypeProperty) {
		SortedSet<Individual> members = new TreeSet<Individual>();
		String query = String.format("SELECT ?ind WHERE {" +
				"?ind <%s> ?o." +
				" FILTER(isLiteral(?o) && DATATYPE(?o) = <%s> && ?o = %s)}", 
				datatypeProperty.getName(), XSD.BOOLEAN.toStringID(),
				"\"false\"^^<"+XSD.BOOLEAN.toStringID() + ">");
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			members.add(new Individual(qs.getResource("ind").getURI()));
			
		}
		return members;
	}

	@Override
	public Map<Individual, SortedSet<String>> getStringDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<NamedClass> getInconsistentClasses() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Description getDomain(ObjectProperty objectProperty) {
		String query = String.format("SELECT ?domain WHERE {" +
				"<%s> <%s> ?domain. FILTER(isIRI(?domain))" +
				"}", 
				objectProperty.getName(), RDFS.domain.getURI());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		List<Description> domains = new ArrayList<Description>();
		while(rs.hasNext()){
			qs = rs.next();
			domains.add(new NamedClass(qs.getResource("domain").getURI()));
			
		}
		if(domains.size() == 1){
			return domains.get(0);
		} else if(domains.size() > 1){
			return new Intersection(domains);
		} 
		return null;
	}

	@Override
	public Description getDomain(DatatypeProperty datatypeProperty) {
		String query = String.format("SELECT ?domain WHERE {" +
				"<%s> <%s> ?domain. FILTER(isIRI(?domain))" +
				"}", 
				datatypeProperty.getName(), RDFS.domain.getURI());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		List<Description> domains = new ArrayList<Description>();
		while(rs.hasNext()){
			qs = rs.next();
			domains.add(new NamedClass(qs.getResource("domain").getURI()));
			
		}
		if(domains.size() == 1){
			return domains.get(0);
		} else if(domains.size() > 1){
			return new Intersection(domains);
		} 
		return null;
	}

	@Override
	public Description getRange(ObjectProperty objectProperty) {
		String query = String.format("SELECT ?range WHERE {" +
				"<%s> <%s> ?range. FILTER(isIRI(?range))" +
				"}", 
				objectProperty.getName(), RDFS.range.getURI());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		List<Description> ranges = new ArrayList<Description>();
		while(rs.hasNext()){
			qs = rs.next();
			ranges.add(new NamedClass(qs.getResource("range").getURI()));
			
		}
		if(ranges.size() == 1){
			return ranges.get(0);
		} else if(ranges.size() > 1){
			return new Intersection(ranges);
		} 
		return null;
	}
	
	public boolean isObjectProperty(String propertyURI){
		String query = String.format("ASK {<%s> a <%s>}", propertyURI, OWL.ObjectProperty.getURI());
		boolean isObjectProperty = executeAskQuery(query);
		return isObjectProperty;
	}
	
	public boolean isDataProperty(String propertyURI){
		String query = String.format("ASK {<%s> a <%s>}", propertyURI, OWL.DatatypeProperty.getURI());
		boolean isObjectProperty = executeAskQuery(query);
		return isObjectProperty;
	}
	
	public int getIndividualsCount(NamedClass nc){
		String query = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {" +
				"?s a <%s>." +
				"}", 
				nc.getURI());
		ResultSet rs = executeSelectQuery(query);
		int cnt = rs.next().get(rs.getResultVars().get(0)).asLiteral().getInt();
		return cnt;
		
	}
	
	public int getPropertyCount(ObjectProperty property){
		String query = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o.}", property.getName());
		ResultSet rs = executeSelectQuery(query);
		int cnt = rs.next().get(rs.getResultVars().get(0)).asLiteral().getInt();
		return cnt;
		
	}
	
	public SortedSet<ObjectProperty> getInverseObjectProperties(ObjectProperty property){
		SortedSet<ObjectProperty> inverseObjectProperties = new TreeSet<ObjectProperty>();
		String query = "SELECT ?p WHERE {" +
				"{<%p> <%ax> ?p.} UNION {?p <%ax> <%p>}}".replace("%p", property.getName()).replace("%ax", OWL.inverseOf.getURI());
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			inverseObjectProperties.add(new ObjectProperty(qs.getResource("p").getURI()));
			
		}
		return inverseObjectProperties;
	}

	@Override
	public DataRange getRange(DatatypeProperty datatypeProperty) {
		String query = String.format("SELECT ?range WHERE {" +
				"<%s> <%s> ?range. FILTER(isIRI(?range))" +
				"}", 
				datatypeProperty.getName(), RDFS.range.getURI());
		
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		DataRange range = null;
		while(rs.hasNext()){
			qs = rs.next();
			range = new Datatype(qs.getResource("range").getURI());
			
		}
		return range;
	}

	@Override
	public boolean isSuperClassOf(Description superClass, Description subClass) {
		if(!(superClass instanceof NamedClass) && !(subClass instanceof NamedClass)){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		String query = String.format("ASK {<%s> <%s> <%s>.}", 
				((NamedClass)subClass).getURI().toString(),
						RDFS.subClassOf.getURI(),
						((NamedClass)superClass).getURI().toString());
		boolean superClassOf = executeAskQuery(query);
		return superClassOf;
	}

	@Override
	public boolean isEquivalentClass(Description class1, Description class2) {
		if(!(class1 instanceof NamedClass) && !(class2 instanceof NamedClass)){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		String query = String.format("ASK {<%s> <%s> <%s>.}", 
				((NamedClass)class1).getURI().toString(),
						OWL.equivalentClass.getURI(),
						((NamedClass)class2).getURI().toString());
		boolean equivalentClass = executeAskQuery(query);
		return equivalentClass;
	}

	@Override
	public Set<Description> getAssertedDefinitions(NamedClass namedClass) {
		Set<Description> definitions = new HashSet<Description>();
		String query = String.format("SELECT ?class { {<%s> <%s> ?class. FILTER(isIRI(?class))} UNION {?class. <%s> <%s>. FILTER(isIRI(?class))} }", 
				namedClass.getURI().toString(),
				OWL.equivalentClass.getURI(),
				OWL.equivalentClass.getURI(),
				namedClass.getURI().toString()	
		);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			definitions.add(new NamedClass(qs.getResource("class").getURI()));
		}
		return definitions;
	}

	@Override
	public Set<Description> isSuperClassOf(Set<Description> superClasses, Description subClasses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassHierarchy getClassHierarchy() {
		return hierarchy;
	}

	@Override
	public SortedSet<Description> getSuperClasses(Description description) {
		if(!(description instanceof NamedClass || description instanceof Thing || description instanceof Nothing)){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		if(description instanceof Nothing){
			description = new NamedClass("http://www.w3.org/2002/07/owl#Nothing");
		}
		SortedSet<Description> superClasses = new TreeSet<Description>();
		String query = String.format("SELECT ?sup {<%s> <%s> ?sup. FILTER(isIRI(?sup))}", 
				((NamedClass)description).getURI().toString(),
				RDFS.subClassOf.getURI()
		);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superClasses.add(new NamedClass(qs.getResource("sup").getURI()));
		}
		superClasses.remove(description);
		return superClasses;
	}
	
	public SortedSet<Description> getSuperClasses(Description description, boolean direct){
		if(!(description instanceof NamedClass)){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		SortedSet<Description> superClasses = new TreeSet<Description>();
		//this query is virtuoso specific
		String query = String.format("SELECT DISTINCT ?y WHERE {" +
				"{ SELECT ?x ?y WHERE { ?x rdfs:subClassOf ?y } }" +
				"OPTION ( TRANSITIVE, T_DISTINCT, t_in(?x), t_out(?y), t_step('path_id') as ?path, t_step(?x) as ?route, t_step('step_no') AS ?jump, T_DIRECTION 3 )" +
				"FILTER ( ?x = <%s> )}", ((NamedClass)description).getURI().toString());
				
		
		
		return superClasses;
	}

	@Override
	public SortedSet<Description> getSubClasses(Description description) {
		return getSubClasses(description, false);
	}
	
	public SortedSet<Description> getSubClasses(Description description, boolean useVirtuoso) {
		if(!(description instanceof NamedClass || description instanceof Thing)){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		if(description instanceof Thing){
			description = new NamedClass(Thing.instance.getURI());
		}
		SortedSet<Description> subClasses = new TreeSet<Description>();
		String query;
		if(useVirtuoso){
			query = getAllSubClassesVirtuosoQuery(description);
		} else {
			query = String.format("SELECT ?sub {?sub <%s> <%s>. FILTER(isIRI(?sub))}", 
					RDFS.subClassOf.getURI(),
					((NamedClass)description).getURI().toString()
					
			);
		}
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subClasses.add(new NamedClass(qs.getResource("sub").getURI()));
		}
		subClasses.remove(description);
		return subClasses;
	}
	
	private String getAllSubClassesVirtuosoQuery(Description description){
		String query = String.format(
				"SELECT DISTINCT ?sub WHERE {"+
				"{SELECT ?sub ?o where {?sub rdfs:subClassOf ?o.}}"+
				"OPTION ( TRANSITIVE, t_distinct, t_in(?sub), t_out(?o), t_min (1), t_max (8), t_step ('step_no') as ?dist ) ."+
				"FILTER(?o = <%s>)}", description.toString());
		return query;
	}

	@Override
	public ObjectPropertyHierarchy getObjectPropertyHierarchy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<ObjectProperty> getSuperProperties(ObjectProperty objectProperty) {
		SortedSet<ObjectProperty> superProperties = new TreeSet<ObjectProperty>();
		String query = String.format("SELECT ?sup {<%s> <%s> ?sup. FILTER(isIRI(?sup))}", 
				objectProperty.getURI().toString(),
				RDFS.subPropertyOf.getURI()
		);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superProperties.add(new ObjectProperty(qs.getResource("sup").getURI()));
		}
		return superProperties;
	}

	@Override
	public SortedSet<ObjectProperty> getSubProperties(ObjectProperty objectProperty) {
		SortedSet<ObjectProperty> subProperties = new TreeSet<ObjectProperty>();
		String query = String.format("SELECT ?sub {?sub <%s> <%s>. FILTER(isIRI(?sub))}", 
				RDFS.subPropertyOf.getURI(),
				objectProperty.getURI().toString()
				
		);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subProperties.add(new ObjectProperty(qs.getResource("sub").getURI()));
		}
		return subProperties;
	}
	
	public SortedSet<ObjectProperty> getEquivalentProperties(ObjectProperty objectProperty) {
		SortedSet<ObjectProperty> superProperties = new TreeSet<ObjectProperty>();
		String query = String.format("SELECT ?equ {<%s> <%s> ?equ. FILTER(isIRI(?equ))}", 
				objectProperty.getURI().toString(),
				OWL.equivalentProperty.getURI()
		);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superProperties.add(new ObjectProperty(qs.getResource("equ").getURI()));
		}
		return superProperties;
	}
	
	public SortedSet<DatatypeProperty> getEquivalentProperties(DatatypeProperty objectProperty) {
		SortedSet<DatatypeProperty> superProperties = new TreeSet<DatatypeProperty>();
		String query = String.format("SELECT ?equ {<%s> <%s> ?equ. FILTER(isIRI(?equ))}", 
				objectProperty.getURI().toString(),
				OWL.equivalentProperty.getURI()
		);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superProperties.add(new DatatypeProperty(qs.getResource("equ").getURI()));
		}
		return superProperties;
	}

	@Override
	public TreeSet<ObjectProperty> getMostGeneralProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TreeSet<ObjectProperty> getMostSpecialProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedSet<DatatypeProperty> getSuperProperties(DatatypeProperty dataProperty) {
		SortedSet<DatatypeProperty> superProperties = new TreeSet<DatatypeProperty>();
		String query = String.format("SELECT ?sup {<%s> <%s> ?sup. FILTER(isIRI(?sup))}", 
				dataProperty.getURI().toString(),
				RDFS.subPropertyOf.getURI()
		);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superProperties.add(new DatatypeProperty(qs.getResource("sup").getURI()));
		}
		return superProperties;
	}

	@Override
	public SortedSet<DatatypeProperty> getSubProperties(DatatypeProperty dataProperty) {
		SortedSet<DatatypeProperty> subProperties = new TreeSet<DatatypeProperty>();
		String query = String.format("SELECT ?sub {?sub <%s> <%s>. FILTER(isIRI(?sub))}", 
				RDFS.subPropertyOf.getURI(),
				dataProperty.getURI().toString()
				
		);
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subProperties.add(new DatatypeProperty(qs.getResource("sub").getURI()));
		}
		return subProperties;
	}

	@Override
	public TreeSet<DatatypeProperty> getMostGeneralDatatypeProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TreeSet<DatatypeProperty> getMostSpecialDatatypeProperties() {
		throw new UnsupportedOperationException();
	}
	
	private ResultSet executeSelectQuery(String query){
		logger.debug("Sending query \n {}", query);
		ResultSet rs = null;
		if(ks.isRemote()){
			if(useCache){
				rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(ks.getEndpoint(), query));
			} else {
				QueryEngineHTTP queryExecution = new QueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
				for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
					queryExecution.addDefaultGraph(dgu);
				}
				for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
					queryExecution.addNamedGraph(ngu);
				}			
				rs = queryExecution.execSelect();
			}
		} else {
			QueryExecution qExec = com.hp.hpl.jena.query.QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			rs = qExec.execSelect();
			
		}
		return rs;
	}
	
	private ResultSet executeSelectQuery(String query, long timeout){
		logger.debug("Sending query \n {}", query);
		ResultSet rs = null;
		if(ks.isRemote()){
			SparqlEndpoint endpoint = ((SparqlEndpointKS) ks).getEndpoint();
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(),
					query);
			queryExecution.setTimeout(timeout);
			queryExecution.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			queryExecution.setNamedGraphURIs(endpoint.getNamedGraphURIs());
			try {
				rs = queryExecution.execSelect();
			} catch (QueryExceptionHTTP e) {
				if(e.getCause() instanceof SocketTimeoutException){
					logger.warn("Got timeout");
				} else {
					logger.error("Exception executing query", e);
				}
				rs = new ResultSetMem();
			}
		} else {
			QueryExecution qExec = com.hp.hpl.jena.query.QueryExecutionFactory.create(query, ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			rs = qExec.execSelect();
		}
		return rs;
	}
	
	/**
	 * Returns TRUE if the class hierarchy was computed before.
	 * @return
	 */
	public boolean isPrepared(){
		return hierarchy != null;
	}
	
	public void setCache(ExtractionDBCache cache) {
		this.cache = cache;
	}
	
	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
	
	private boolean executeAskQuery(String query){
		boolean ret;
		if(ks.isRemote()){
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
			for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
				queryExecution.addDefaultGraph(dgu);
			}
			for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
				queryExecution.addNamedGraph(ngu);
			}			
			ret = queryExecution.execAsk();
			
		} else {
			QueryExecution qExec = com.hp.hpl.jena.query.QueryExecutionFactory.create(query,  ((LocalModelBasedSparqlEndpointKS)ks).getModel());
			ret = qExec.execAsk();
		}
		
		return ret;
	}
	
	
	public static void main(String[] args) throws Exception{
//		QueryEngineHTTP e = new QueryEngineHTTP("http://bibleontology.com/sparql/index.jsp",
//				"SELECT DISTINCT ?type WHERE {?s a ?type) LIMIT 10");
//		e.addParam("type1", "xml");System.out.println(e.toString());
//		e.execSelect();
		
		
		SparqlEndpointKS ks = new SparqlEndpointKS(new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql/")));
		SPARQLReasoner r = new SPARQLReasoner(ks);
		long startTime = System.currentTimeMillis();
		ClassHierarchy h = r.prepareSubsumptionHierarchyFast();
		System.out.println(h.toString(false));
//		Model schema = r.loadSchema();
//		for(Statement st : schema.listStatements().toList()){
//			System.out.println(st);
//		}
		System.out.println(h.getSubClasses(new NamedClass("http://dbpedia.org/ontology/Bridge"), false));
		System.out.println("Time needed: " + (System.currentTimeMillis()-startTime) + "ms");
		
	}

}
