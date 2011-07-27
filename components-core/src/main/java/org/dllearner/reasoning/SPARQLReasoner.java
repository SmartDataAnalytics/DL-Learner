package org.dllearner.reasoning;

import java.util.HashMap;
import java.util.HashSet;
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
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.utilities.datastructures.SortedSetTuple;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class SPARQLReasoner implements SchemaReasoner, IndividualReasoner{
	
	private SparqlEndpointKS ks;
	
	public SPARQLReasoner(SparqlEndpointKS ks) {
		this.ks = ks;
	}

	@Override
	public Set<NamedClass> getTypes(Individual individual) {
		Set<NamedClass> types = new HashSet<NamedClass>();
		String query = 
			"SELECT ?class WHERE {" +
				inAngleBrackets(individual.getName()) + "a ?class.}";
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
		String query = String.format("ASK {%s a %s}",inAngleBrackets(individual.toString()), inAngleBrackets(((NamedClass)description).getName()));
		boolean hasType = executeAskQuery(query);
		return hasType;
	}

	@Override
	public SortedSet<Individual> hasType(Description description, Set<Individual> individuals) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedSet<Individual> getIndividuals(Description description) {
		if(!(description instanceof NamedClass)){
			throw new UnsupportedOperationException("Only named classes are supported.");
		}
		SortedSet<Individual> individuals = new TreeSet<Individual>();
		String query = String.format("SELECT ?ind WHERE {?ind a %s}", inAngleBrackets(((NamedClass)description).getName()));
		
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
		String query = String.format("SELECT ?ind WHERE {%s %s ?ind, FILTER(isIRI(?ind))}", inAngleBrackets(individual.getName()), inAngleBrackets(objectProperty.getName()));
		
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
		String query = String.format("select ?prop ?ind WHERE {" +
				"%s ?prop ?ind." +
				" FILTER(isIRI(?ind) && ?prop != %s && ?prop != %s)}", 
				inAngleBrackets(individual.getName()), inAngleBrackets(RDF.type.getURI()), inAngleBrackets(OWL.sameAs.getURI()));
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Individual, SortedSet<Constant>> getDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Individual, SortedSet<Double>> getDoubleDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Individual, SortedSet<Integer>> getIntDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Individual, SortedSet<Boolean>> getBooleanDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<Individual> getTrueDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<Individual> getFalseDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Individual, SortedSet<String>> getStringDatatypeMembers(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<NamedClass> getInconsistentClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Description getDomain(ObjectProperty objectProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Description getDomain(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Description getRange(ObjectProperty objectProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataRange getRange(DatatypeProperty datatypeProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSuperClassOf(Description superClass, Description subClass) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEquivalentClass(Description class1, Description class2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<Description> getAssertedDefinitions(NamedClass namedClass) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<Description> getSubClasses(Description description) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectPropertyHierarchy getObjectPropertyHierarchy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<ObjectProperty> getSuperProperties(ObjectProperty objectProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<ObjectProperty> getSubProperties(ObjectProperty objectProperty) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<DatatypeProperty> getSubProperties(DatatypeProperty dataProperty) {
		// TODO Auto-generated method stub
		return null;
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
	
	private String inAngleBrackets(String s){
		return "<" + s + ">";
	}

}
