package org.dllearner.algorithms.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class PropertyDomainAxiomLearner extends Component implements AxiomLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(PropertyDomainAxiomLearner.class);
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	@ConfigOption(name="maxExecutionTimeInSeconds", description="", propertyEditorClass=IntegerEditor.class)
	private int maxExecutionTimeInSeconds;
	
	private SPARQLReasoner reasoner;
	private SparqlEndpointKS ks;
	
	public PropertyDomainAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}
	
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public ObjectProperty getPropertyToDescribe() {
		return propertyToDescribe;
	}

	public void setPropertyToDescribe(ObjectProperty propertyToDescribe) {
		this.propertyToDescribe = propertyToDescribe;
	}
	
	@Override
	public void start() {
		//get existing domains
		Description existingDomain = reasoner.getDomain(propertyToDescribe);
		logger.debug("Existing domain: " + existingDomain);
		
		//get subjects with types
		Map<Individual, Set<NamedClass>> individual2Types = getSubjectsWithTypes();
		
		//get subjects of property
		Map<Individual, SortedSet<Individual>> members = reasoner.getPropertyMembers(propertyToDescribe);
		

	}

	@Override
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Configurator getConfigurator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() throws ComponentInitException {
		reasoner = new SPARQLReasoner(ks);
		
	}
	
	private Map<Individual, Set<NamedClass>> getSubjectsWithTypes(){
		Map<Individual, Set<NamedClass>> individual2Types = new HashMap<Individual, Set<NamedClass>>();
		int limit = 1000;
		int offset = 135000;
		boolean executeAgain = true;
		
		while(executeAgain){
			String query = String.format("SELECT ?ind ?type WHERE {?ind %s ?o. ?ind a ?type.} LIMIT %d OFFSET %d", inAngleBrackets(propertyToDescribe.getURI().toString()), limit, offset);
			ResultSet rs = executeQuery(query);
			QuerySolution qs;
			Individual ind;
			Set<NamedClass> types;
			executeAgain = rs.hasNext();
			while(executeAgain && rs.hasNext()){
				qs = rs.next();
				ind = new Individual(qs.getResource("ind").getURI());
				types = individual2Types.get(ind);
				if(types == null){
					types = new HashSet<NamedClass>();
				}
				types.add(new NamedClass(qs.getResource("type").getURI()));
			}
			offset += 1000;
		}
		
		return individual2Types;
	}
	
	private String inAngleBrackets(String s){
		return "<" + s + ">";
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
		ResultSet resultSet = queryExecution.execSelect();
		return resultSet;
	}
	
	public static void main(String[] args) throws Exception{
		Map<String, String> propertiesMap = new HashMap<String, String>();
        propertiesMap.put("propertyToDescribe", "http://dbpedia.org/ontology/league");
        propertiesMap.put("maxExecutionTimeInSeconds", "20");
        
        PropertyDomainAxiomLearner l = new PropertyDomainAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia()));
        
        Field[] fields = l.getClass().getDeclaredFields();
        for(Field f : fields){
        	ConfigOption option = f.getAnnotation(ConfigOption.class);
        	if(option != null){
        		String configValue = propertiesMap.get(option.name());
        		PropertyEditor editor = (PropertyEditor) option.propertyEditorClass().newInstance();
        		editor.setAsText(configValue);
        		f.set(l, editor.getValue());
        	}
        }
        
        l.init();
        l.start();
	}

}
