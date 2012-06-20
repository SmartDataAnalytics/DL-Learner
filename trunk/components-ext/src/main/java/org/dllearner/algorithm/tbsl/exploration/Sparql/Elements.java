package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.dllearner.algorithm.tbsl.exploration.Utils.DebugMode;
import org.dllearner.algorithm.tbsl.exploration.Utils.ElementStorage;
import org.dllearner.algorithm.tbsl.exploration.Utils.ServerUtil;
import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;

/**
 * Creates Set of Classes and Resources with their properties
 * @author swalter
 *
 */
public class Elements {
	private ArrayList<ElementList> elements = new ArrayList<ElementList>();
	
	public ArrayList<ElementList> getElements() {
		return elements;
	}
	public void setElements(ArrayList<ElementList> resources) {
		this.elements = resources;
	}
	
	private void addElements(ElementList cl) {
		this.elements.add(cl);
	}
	
	public Elements(ArrayList<ArrayList<String>> condition, ArrayList<ArrayList<Hypothesis>> hypothesen){
		long start = System.currentTimeMillis();
		/*
		 * first all Classes!
		 */
		try {
			//this.setClasses(createElementsOfClasses(hypothesen));
			createElementsOfClasses(hypothesen);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * second all Resources
		 */
		try {
			//this.setResources(createElementsOfResources(hypothesen,condition));
			createElementsOfResources(hypothesen,condition);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(Setting.isDebugModus())DebugMode.debugPrint("Created Elements");
		long stop = System.currentTimeMillis();
		Setting.addTime_elements(stop-start);
	}
	
	private void createElementsOfClasses(ArrayList<ArrayList<Hypothesis>> hypothesenList) throws IOException{
		
		/*
		 * Iterate over all Hypothesis and look for an IsA
		 */
		for(ArrayList<Hypothesis> hl : hypothesenList){
			for(Hypothesis h : hl){
				/*
				 * if isA is found and if Class has uri, get Elements
				 */
				if(h.getType().contains("ISA")&&h.getUri().contains("http")){
					if(Setting.isDebugModus())DebugMode.debugPrint("Create Elements for Class: "+h.getName()+" Uri: "+h.getUri());
					/*
					 * Todo First Lookup an HashMap with if the resource is in it, if yes, take the results,
					 * if not, create new Elements
					 */
					if(ElementStorage.getStorage_classes().containsKey(h.getUri())){
						ElementList el = new ElementList(h.getName(),h.getUri(),ElementStorage.getStorage_classes().get(h.getUri()));
						this.addElements(el);
					}
					else{
						HashMap<String,String> tmp_hm = new HashMap<String,String>();
						tmp_hm=ServerUtil.getElementsForGivenClass(h.getUri());
						ElementList el = new ElementList(h.getName(),h.getUri(),tmp_hm);
						ElementStorage.addStorage_classes(h.getUri(), tmp_hm);
						this.addElements(el);
					}
					//ElementList el = new ElementList(h.getName(),h.getUri(),ServerUtil.getElementsForGivenClass(h.getUri()));
					//classes.add(el);
					//this.addElements(el);
				}
			}
		}
		
		//return classes;
	}
	
	
	private void createElementsOfResources(ArrayList<ArrayList<Hypothesis>> hypothesenList,ArrayList<ArrayList<String>> conditionList) throws IOException{
		/*
		 * Iterate over all Hypothesis and look for an resource
		 */
		for(ArrayList<Hypothesis> hl : hypothesenList){
			for(Hypothesis h : hl){
				if(h.getType().contains("RESOURCE")&&h.getUri().contains("http")){
					if(Setting.isDebugModus())DebugMode.debugPrint("Create Elements for Resource: "+h.getName()+" Uri: "+h.getUri());
					
					for(ArrayList<String> cl : conditionList){
						if(h.getVariable().equals(cl.get(0))) {
							if(ElementStorage.getStorage_resource_right().containsKey(h.getUri())){
								ElementList el = new ElementList(h.getName()+"RIGHT",h.getUri(),ElementStorage.getStorage_resource_right().get(h.getUri()));
								//resources.add(el);
								this.addElements(el);
							}
							else{
								HashMap<String,String> tmp_hm = new HashMap<String,String>();
								tmp_hm=ServerUtil.getPropertiesForGivenResource(h.getUri(), "RIGHT");
								ElementList el = new ElementList(h.getName()+"RIGHT",h.getUri(),tmp_hm);
								ElementStorage.addStorage_resource_right(h.getUri(), tmp_hm);
								//resources.add(el);
								this.addElements(el);
							}
								
					
						}
						if(h.getVariable().equals(cl.get(2))) {
							
							if(ElementStorage.getStorage_resource_left().containsKey(h.getUri())){
								ElementList el = new ElementList(h.getName()+"LEFT",h.getUri(),ElementStorage.getStorage_resource_left().get(h.getUri()));
								//resources.add(el);
								this.addElements(el);
							}
							else{
								HashMap<String,String> tmp_hm = new HashMap<String,String>();
								tmp_hm=ServerUtil.getPropertiesForGivenResource(h.getUri(), "LEFT");
								ElementList el = new ElementList(h.getName()+"LEFT",h.getUri(),tmp_hm);
								ElementStorage.addStorage_resource_left(h.getUri(), tmp_hm);
								//resources.add(el);
								this.addElements(el);
							}
								
						}
					}
					
					
				}
			}
		}
		
		//return resources;
	}

	
	public void printAll(){
		System.out.println("Elements: ");
		for(ElementList el: this.elements){
			el.printAll();
		}
	}
	
	public String printToString(){
		String result="";
		result+="Elements: \n";
		for(ElementList el: this.elements){
			result+=el.printToString()+"\n";
		}
		return result;
	}
	
	
	public boolean isElementEmty(){
		try {
			if(this.getElements().isEmpty()) return true;
			else return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return true;
		}
	}

}


