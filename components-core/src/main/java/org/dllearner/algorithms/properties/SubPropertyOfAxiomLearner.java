package org.dllearner.algorithms.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.propertyeditors.CustomNumberEditor;

public class SubPropertyOfAxiomLearner extends Component implements AxiomLearningAlgorithm {
	
	@ConfigOption(name="propertyToDescribe", description="", propertyEditorClass=ObjectPropertyEditor.class)
	private ObjectProperty propertyToDescribe;
	@ConfigOption(name="maxExecutionTimeInSeconds", description="", propertyEditorClass=IntegerEditor.class)
	private int maxExecutionTimeInSeconds;
	
	
	private SparqlEndpointKS ks;
	
	public SubPropertyOfAxiomLearner(SparqlEndpointKS ks){
		this.ks = ks;
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		
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
	
	public static void main(String[] args) throws Exception{
		Map<String, String> propertiesMap = new HashMap<String, String>();
        propertiesMap.put("propertyToDescribe", "http://dbpedia.org/ontology/locatedIn");
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
        
        System.out.println(l.getPropertyToDescribe());
        System.out.println(l.getMaxExecutionTimeInSeconds());
	}
	
}
