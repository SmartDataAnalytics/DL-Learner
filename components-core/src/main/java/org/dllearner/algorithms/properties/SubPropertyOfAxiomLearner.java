package org.dllearner.algorithms.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.core.AxiomLearningAlgorithm;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.IntegerEditor;
import org.dllearner.core.config.ObjectPropertyEditor;
import org.dllearner.core.configurators.Configurator;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class SubPropertyOfAxiomLearner extends Component implements AxiomLearningAlgorithm {
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	@ConfigOption(name="maxExecutionTimeInSeconds", description="", propertyEditorClass=IntegerEditor.class)
	private int maxExecutionTimeInSeconds;
	
	private SPARQLReasoner reasoner;
	private SparqlEndpointKS ks;
	
	public SubPropertyOfAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}
	
	@Override
	public void start() {
		//get
		Set<ObjectProperty> properties = new HashSet<ObjectProperty>();
		String query = String.format("SELECT ?p ?p1 WHERE {?s %s ?o. ?s ?p ?o1. ?s1 ?p1 ?o.}", inAngleBrackets(propertyToDescribe.getURI().toString()));
		ResultSet rs = executeQuery(query);
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			properties.add(new ObjectProperty(qs.getResource("p").getURI()));
			properties.add(new ObjectProperty(qs.getResource("p1").getURI()));
		}
		System.out.println(properties);
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
	
	private String inAngleBrackets(String s){
		return "<" + s + ">";
	}
	
	private ResultSet executeQuery(String query){
		System.out.println(query);
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
	
	public static void main(String[] args) throws Exception{
		Map<String, String> propertiesMap = new HashMap<String, String>();
        propertiesMap.put("propertyToDescribe", "http://dbpedia.org/ontology/league");
        propertiesMap.put("maxExecutionTimeInSeconds", "20");
        
        SubPropertyOfAxiomLearner l = new SubPropertyOfAxiomLearner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
        
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
