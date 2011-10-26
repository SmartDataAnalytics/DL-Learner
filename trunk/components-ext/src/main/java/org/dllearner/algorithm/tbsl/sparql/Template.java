package org.dllearner.algorithm.tbsl.sparql;

import java.util.ArrayList;
import java.util.List;

public class Template {

	Query query;
	List<Slot> slots;

	public Template(Query q) {
		query = q;
		slots = new ArrayList<Slot>();
	}
	
	public Query getQuery() {
		return query;
	}
	
	public void setQuery(Query q) {
		query = q;
	}
	
	public void addSlot(Slot s) {
		slots.add(s);
	}
	
	public String toString() {
		
		String out = ">> QUERY:\n\n" + query.toString() + "\n>> SLOTS:\n";
		for (Slot s : slots) {
			out += s.toString() + "\n";
		}
		return out;
	}
	
	public List<Slot> getSlots(){
		return slots;
	}
	
	public List<String> getLexicalAnswerType(){
		if(query.getQt() == SPARQL_QueryType.SELECT){
			String variable = query.getAnswerTypeVariable();
			for(Slot slot : slots){
				if(slot.getAnchor().equals(variable)){
					return slot.getWords();
				}
			}
		}
		return null;
	}
	
}
