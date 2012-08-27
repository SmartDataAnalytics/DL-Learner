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
            for (Slot slot : slots) if (slot.anchor.equals("SLOT_arg")) argslots.add(slot);
            
            for (Slot slot : argslots) {
                String var = slot.words.get(0);
                // check for clash (v=LITERAL && v=RESOURCE)
                for (Slot s : slots) {
                    if ((s.words.get(0).equals(slot.words.get(0)) || s.anchor.equals(slot.words.get(0)))
                            && ((slot.type.equals(SlotType.RESOURCE) && isLiteral(s.type)) || (s.type.equals(SlotType.RESOURCE) && isLiteral(slot.type)))) // !s.type.equals(slot.type)) 
                        return null;
                }
                // check for clash (v=LITERAL && p(...,v)=OBJECTPROPERTY) || (v=RESOURCE && p(...,v)=DATATYPEPROPERTY)
                SlotType clashing = null;
                if (isLiteral(slot.type)) clashing = SlotType.OBJECTPROPERTY;
                else if (slot.type.equals(SlotType.RESOURCE)) clashing = SlotType.DATATYPEPROPERTY;
                for (Slot s : slots) {
                    if (clashing != null && s.type.equals(clashing)) {
                        for (SPARQL_Triple triple : query.conditions) {
                            if (triple.property.toString().equals("?"+s.anchor)) {
                                if (triple.value.toString().equals("?"+var))
                                	return null;
                            }
                        }
                    }
                }
                // check for clashes with FILTERS
                for (SPARQL_Filter filter : query.filter) {
                    for (SPARQL_Pair ts : filter.getTerms()) {
                        if (ts.a.getName().equals(var) && (isIntegerType(ts.type) || ts.type.equals(SPARQL_PairType.REGEX))) {
                            // clash 1: counting a literal
                            for (SPARQL_Term sel : query.selTerms) {
                                if (sel.name.equals(var) && sel.aggregate.equals(SPARQL_Aggregate.COUNT)) 
                                    return null; 
                            }
                            // clash 2: FILTER regex(?var,...) and FILTER (?var > ...)
                            for (SPARQL_Filter f : query.filter) {
                                if (!f.equals(filter)) {
                                    for (SPARQL_Pair p : f.getTerms()) {
                                        if (p.a.name.equals(var) && (p.type.equals(SPARQL_PairType.REGEX) && isIntegerType(ts.type)) || (ts.type.equals(SPARQL_PairType.REGEX) && isIntegerType(p.type)))
                                            return null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            for (Slot slot : slots) {
                // check for clashes    
                if (slot.type.equals(SlotType.CLASS)) {
                    for (SPARQL_Triple triple : query.conditions) {
                        if (triple.property.toString().equals("rdf:type") && triple.value.toString().equals("?"+slot.anchor)) {
                            for (Slot s : argslots) {
                                if (s.words.contains(triple.variable.toString().replace("?","")) && isLiteral(s.type)) 
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
                                if (isLiteral(s.type)) slot.type = SlotType.DATATYPEPROPERTY;
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
//            slots.removeAll(argslots); // removes all (argslots + resource slots)
//            for (Slot sl : argslots) slots.remove(sl); // removes resource slots
            List<Slot> keep = new ArrayList<Slot>();
            for (Slot s : slots) {
                if (!s.anchor.startsWith("SLOT_arg"))
                    keep.add(s);
            }
            slots = keep; 
            
            // additionally, filter out those templates that count a var that does not occur in the triples 
            // (these templates should not be built in the first place, but they are...)
            for (SPARQL_Term t : query.selTerms) {
                if (t.aggregate.equals(SPARQL_Aggregate.COUNT)) {
                    String v = t.name;
                    boolean fine = false;
                    for (SPARQL_Triple triple : query.conditions) {
                        if ((triple.variable.name.equals(v) || triple.value.name.equals(v))) {
                            fine = true; break;
                        }
                    }
                    if (!fine) return null;
                }
            }
            
            return this;
        }
        private boolean isLiteral(SlotType st) {
            return st.equals(SlotType.STRING) || st.equals(SlotType.INTEGER) || st.equals(SlotType.LITERAL);
        }
        private boolean isIntegerType(SPARQL_PairType p) {
            return p.equals(SPARQL_PairType.GT) || p.equals(SPARQL_PairType.LT) || p.equals(SPARQL_PairType.GTEQ) || p.equals(SPARQL_PairType.LTEQ);
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
