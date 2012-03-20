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
	private ArrayList<ElementList> resources = new ArrayList<ElementList>();
	private ArrayList<ElementList> classes = new ArrayList<ElementList>();
	public ArrayList<ElementList> getResources() {
		return resources;
	}
	public void setResources(ArrayList<ElementList> resources) {
		this.resources = resources;
	}
	public ArrayList<ElementList> getClasses() {
		return classes;
	}
	public void setClasses(ArrayList<ElementList> classes) {
		this.classes = classes;
	}
	
	private void addClasses(ElementList cl) {
		this.classes.add(cl);
	}
	
	private void addResources(ElementList cl) {
		this.resources.add(cl);
	}
	
	public Elements(ArrayList<ArrayList<String>> condition, ArrayList<ArrayList<Hypothesis>> hypothesen){
		
		/*
		 * first all Classes!
		 */
		try {
			this.setClasses(createElementsOfClasses(hypothesen));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * second all Resources
		 */
		try {
			this.setResources(createElementsOfResources(hypothesen,condition));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Created Elements");
	}
	
	private ArrayList<ElementList> createElementsOfClasses(ArrayList<ArrayList<Hypothesis>> hypothesenList) throws IOException{
		ArrayList<ElementList> classes = new ArrayList<ElementList>();
		
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
					classes.add(el);
				}
			}
		}
		
		return classes;
	}
	
	
	private ArrayList<ElementList> createElementsOfResources(ArrayList<ArrayList<Hypothesis>> hypothesenList,ArrayList<ArrayList<String>> conditionList) throws IOException{
		ArrayList<ElementList> resources = new ArrayList<ElementList>();
		/*
		 * Iterate over all Hypothesis and look for an resource
		 */
		for(ArrayList<Hypothesis> hl : hypothesenList){
			for(Hypothesis h : hl){
				if(h.getType().contains("RESOURCE")&&h.getUri().contains("http")){
					for(ArrayList<String> cl : conditionList){
						if(h.getVariable().equals(cl.get(0))) {
							ElementList el = new ElementList(h.getName()+"RIGHT",h.getUri(),ServerUtil.getPropertiesForGivenResource(h.getUri(), "RIGHT"));
							resources.add(el);
						}
						if(h.getVariable().equals(cl.get(2))) {
							ElementList el = new ElementList(h.getName()+"LEFT",h.getUri(),ServerUtil.getPropertiesForGivenResource(h.getUri(), "LEFT"));
							resources.add(el);
						}
					}
					
					
				}
			}
		}
		
		return resources;
	}

	
	public void printAll(){
		System.out.println("Resources: ");
		for(ElementList el: this.resources){
			el.printAll();
		}
		System.out.println("\nClasses: ");
		for(ElementList el: this.classes){
			el.printAll();
		}
	}
	
	public String printToString(){
		String result="";
		result+="Resources: \n";
		for(ElementList el: this.resources){
			result+=el.printToString()+"\n";
		}
		result+="\nClasses: \n";
		for(ElementList el: this.classes){
			result+=el.printToString()+"\n";
		}
		return result;
	}
	
	
	public boolean isElementEmty(){
		try {
			if(this.getClasses().isEmpty()||this.getResources().isEmpty()) return true;
			else return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return true;
		}
	}

}


