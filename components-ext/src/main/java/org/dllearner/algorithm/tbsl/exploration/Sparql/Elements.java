package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.io.IOException;
import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.exploration.Utils.ServerUtil;

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
		
		System.out.println("Created Elements");
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
				if(h.getType().contains("isA")&&h.getUri().contains("http")){
					/*
					 * TODO: improver performance, using geschicktes zwischenspeichern
					 */
					ElementList el = new ElementList(h.getName(),h.getUri(),ServerUtil.getElementsForGivenClass(h.getUri()));
					//classes.add(el);
					this.addElements(el);
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
					for(ArrayList<String> cl : conditionList){
						if(h.getVariable().equals(cl.get(0))) {
							ElementList el = new ElementList(h.getName()+"RIGHT",h.getUri(),ServerUtil.getPropertiesForGivenResource(h.getUri(), "RIGHT"));
							//resources.add(el);
							this.addElements(el);
						}
						if(h.getVariable().equals(cl.get(2))) {
							/*
							 * TDO: Geht hier in die Schleife, aber die Liste wird nicht hinzugefügt....
							 */
							ElementList el_left = new ElementList(h.getName()+"LEFT",h.getUri(),ServerUtil.getPropertiesForGivenResource(h.getUri(), "LEFT"));
							//resources.add(el);
							//el_left.printAll();
							this.addElements(el_left);
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


