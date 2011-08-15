package org.dllearner.reasoning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.IndividualReasoner;
import org.dllearner.core.SchemaReasoner;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clarkparsia.owlapiv3.XSD;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SPARQLReasoner implements SchemaReasoner, IndividualReasoner{
	
	private static final Logger logger = LoggerFactory.getLogger(SPARQLReasoner.class);
	
	private SparqlEndpointKS ks;
	
	public SPARQLReasoner(SparqlEndpointKS ks) {
		this.ks = ks;
	}

	@Override
	public Set<NamedClass> getTypes(Individual individual) {
		Set<NamedClass> types = new HashSet<NamedClass>();
		String query = String.format("SELECT ?class WHERE {<%s> a ?class.}", individual.getName());
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
				" FILTER(isLiteral(?o) && DATATYPE(?o) = <%s> && ?o = <%s>)}", 
				datatypeProperty.getName(), XSD.BOOLEAN.toStringID(),
				"\"true\"^^<" + XSD.BOOLEAN.toStringID() + ">");
		
		ResultSet rs = executeQuery(query);
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
				" FILTER(isLiteral(?o) && DATATYPE(?o) = <%s> && ?o = <%s>)}", 
				datatypeProperty.getName(), XSD.BOOLEAN.toStringID(),
				"\"false\"^^<"+XSD.BOOLEAN.toStringID() + ">");
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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
		
		ResultSet rs = executeQuery(query);
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

	@Override
	public DataRange getRange(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
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
		ResultSet rs = executeQuery(query);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<Description> getSuperClasses(Description description) {
		if(!(description instanceof NamedClass)){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		SortedSet<Description> superClasses = new TreeSet<Description>();
		String query = String.format("SELECT ?sup {<%s> <%s> ?sup. FILTER(isIRI(?sup))}", 
				((NamedClass)description).getURI().toString(),
				RDFS.subClassOf.getURI()
		);
		ResultSet rs = executeQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			superClasses.add(new NamedClass(qs.getResource("sup").getURI()));
		}
		return superClasses;
	}

	@Override
	public SortedSet<Description> getSubClasses(Description description) {
		if(!(description instanceof NamedClass)){
			throw new IllegalArgumentException("Only named classes are supported.");
		}
		SortedSet<Description> subClasses = new TreeSet<Description>();
		String query = String.format("SELECT ?sub {?sub <%s> <%s>. FILTER(isIRI(?sub))}", 
				RDFS.subClassOf.getURI(),
				((NamedClass)description).getURI().toString()
				
		);
		ResultSet rs = executeQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subClasses.add(new NamedClass(qs.getResource("sub").getURI()));
		}
		return subClasses;
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
		ResultSet rs = executeQuery(query);
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
		ResultSet rs = executeQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			subProperties.add(new ObjectProperty(qs.getResource("sub").getURI()));
		}
		return subProperties;
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
		ResultSet rs = executeQuery(query);
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
		ResultSet rs = executeQuery(query);
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
	
	private ResultSet executeQuery(String query){
		logger.info("Sending query \n {}", query);
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
		for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		ResultSet resultset = queryExecution.execSelect();
		return resultset;
	}
	
	private boolean executeAskQuery(String query){
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(ks.getEndpoint().getURL().toString(), query);
		for (String dgu : ks.getEndpoint().getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : ks.getEndpoint().getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		boolean ret = queryExecution.execAsk();
		return ret;
	}
	
	
	public static void main(String[] args) {
		String NS = "http://dbpedia.org/ontology/";
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		SPARQLReasoner r = new SPARQLReasoner(ks);
		
//		ObjectProperty oP = new ObjectProperty(NS + "league");
//		for(Entry<Individual, SortedSet<Individual>> entry : r.getPropertyMembers(oP).entrySet()){
//			System.out.println(entry.getKey());
//			System.out.println(entry.getValue());
//		}
//		
//		DatatypeProperty dP = new DatatypeProperty(NS+ "areaLand");
//		for(Entry<Individual, SortedSet<Double>> entry : r.getDoubleDatatypeMembers(dP).entrySet()){
//			System.out.println(entry.getKey());
//			System.out.println(entry.getValue());
//		}
		
		DatatypeProperty dP = new DatatypeProperty(NS+ "internationally");
		for(Individual ind : r.getTrueDatatypeMembers(dP)){
			System.out.println(ind);
		}
		
	}

}
