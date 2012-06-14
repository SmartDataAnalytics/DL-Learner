package org.dllearner.algorithm.tbsl.sparql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Template implements Serializable, Comparable<Template>{

	private static final long serialVersionUID = -3925093269596915997L;
	
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
        
        public Template checkandrefine() {
            
            Set<Slot> argslots = new HashSet<Slot>();
            for (Slot slot : slots) if (slot.anchor.equals("SLOT_arg")) {
                String var = slot.words.get(0);
                // check for clash (v=LITERAL && v=RESOURCE)
                for (Slot s : argslots) {
                    if (s.words.get(0).equals(slot.words.get(0)) && !s.type.equals(slot.type)) 
                        return null;
                }
                // check for clash (v=LITERAL && p(...,v)=OBJECTPROPERTY) || (v=RESOURCE && p(...,v)=DATATYPEPROPERTY)
                SlotType clashing = null;
                if (slot.type.equals(SlotType.LITERAL)) clashing = SlotType.OBJECTPROPERTY;
                else if (slot.type.equals(SlotType.RESOURCE)) clashing = SlotType.DATATYPEPROPERTY;
                for (Slot s : slots) {
                    if (clashing != null && s.type.equals(clashing)) {
                        for (SPARQL_Triple triple : query.conditions) {
                            if (triple.property.toString().equals("?"+s.anchor)) {
                                if (triple.value.toString().equals("?"+var)) return null;
                            }
                        }
                    }
                }
                argslots.add(slot);
            }
            
            for (Slot slot : slots) {
                // check for clashes    
                if (slot.type.equals(SlotType.CLASS)) {
                    for (SPARQL_Triple triple : query.conditions) {
                        if (triple.property.toString().equals("rdf:type") && triple.value.toString().equals("?"+slot.anchor)) {
                            for (Slot s : argslots) {
                                if (s.words.contains(triple.variable.toString().replace("?","")) && s.type.equals(SlotType.LITERAL)) 
                                    return null;
                            }
                        }
                    }
                }
                // refine property if possible
                if (slot.type.equals(SlotType.PROPERTY) || slot.type.equals(SlotType.SYMPROPERTY)) {
                    Set<String> args = new HashSet<String>();
                    for (SPARQL_Triple triple : query.conditions) {
                        if (triple.property.toString().equals("?"+slot.anchor))
                            args.add(triple.value.toString());
                    }
                    for (String arg : args) {
                        for (Slot s : argslots) {
                            if (s.words.contains(arg.replace("?",""))) {
                                if (s.type.equals(SlotType.LITERAL)) slot.type = SlotType.DATATYPEPROPERTY;
                                else if (s.type.equals(SlotType.RESOURCE)) slot.type = SlotType.OBJECTPROPERTY;
                            }
                        }
                        if (slot.type.equals(SlotType.PROPERTY) || slot.type.equals(SlotType.SYMPROPERTY)) { // still
                            Set<String> values = new HashSet<String>();
                            for (SPARQL_Triple triple : query.conditions) {
                                if (triple.property.toString().equals("?"+slot.anchor)) values.add(triple.value.toString());
                            }
                            for (SPARQL_Triple triple : query.conditions) {
                                for (String val : values) {
                                if (triple.variable.toString().equals(val) && triple.property.toString().equals("rdf:type"))
                                    slot.type = SlotType.OBJECTPROPERTY;
                                }
                            }
                        }
                    }
                }
            }
            
            // finally remove all argslots
            slots.removeAll(argslots);
            
            return this;
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

	@Override
	public int compareTo(Template o) {
		return getQuery().toString().compareTo(o.getQuery().toString());
	}
	
}
