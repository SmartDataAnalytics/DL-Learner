package org.dllearner.algorithm.tbsl.converter;

import java.util.*;

import org.dllearner.algorithm.tbsl.sem.drs.Complex_DRS_Condition;
import org.dllearner.algorithm.tbsl.sem.drs.DRS;
import org.dllearner.algorithm.tbsl.sem.drs.DRS_Condition;
import org.dllearner.algorithm.tbsl.sem.drs.DRS_Quantifier;
import org.dllearner.algorithm.tbsl.sem.drs.DiscourseReferent;
import org.dllearner.algorithm.tbsl.sem.drs.Negated_DRS;
import org.dllearner.algorithm.tbsl.sem.drs.Simple_DRS_Condition;
import org.dllearner.algorithm.tbsl.sparql.*;


public class DRS2SPARQL_Converter {

    private boolean silent = true; // suppresses console output
    private boolean oxford = true;
    private String inputstring = null;
    List<Slot> slots;
    Template template;
    List<Integer> usedInts;

    public DRS2SPARQL_Converter() {
    	template = new Template(new Query());
    	usedInts = new ArrayList<Integer>();
    }

    public DRS2SPARQL_Converter(boolean silent) {
        setSilent(silent);
        template = new Template(new Query());
        usedInts = new ArrayList<Integer>();
    }
    
    public void setInputString(String s) {
        inputstring = s;
    }
    public void setSlots(List<Slot> ls) {
    	slots = ls;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public List<SPARQL_Property> getProperties(Complex_DRS_Condition cond) {
        List<SPARQL_Property> retVal = new ArrayList<SPARQL_Property>();

        return retVal;
    }

    public Template convert(DRS drs,List<Slot> ls) {
    	
        Set<SPARQL_Prefix> prefixes = new HashSet<SPARQL_Prefix>();
        prefixes.add(new SPARQL_Prefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
        prefixes.add(new SPARQL_Prefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#"));

        if (!isSilent()) {
            System.out.print("Converting DRS{" + drs.toString() + "}...");
        }
        
        template = new Template(new Query());
        slots = ls;
        
        Query q = convert(drs, new Query(), false);
        if (q == null) return null;
        q.setPrefixes(prefixes);
        
        template.setQuery(q);
        
        if (!isSilent()) {
            System.out.println("... done");
        }

        usedInts = new ArrayList<Integer>();
        return template;
    }

    private Query convert(DRS drs, Query query, boolean negate) {
    	
//        System.out.println("\n--- DRS (before): " + drs); // DEBUG   	
        redundantEqualRenaming(drs); 
        if (!restructureEmpty(drs) || !replaceRegextoken(drs)) {
           return null;
        }
//       System.out.println("--- DRS (after) : " + drs); // DEBUG
            
        for (DiscourseReferent referent : drs.collectDRs()) {
            if (referent.isMarked()) {
            	SPARQL_Term term = new SPARQL_Term(referent.toString().replace("?",""));
            	term.setIsVariable(true);
            	query.addSelTerm(term);
            }
            if (referent.isNonexistential()) {
            	SPARQL_Term term = new SPARQL_Term(referent.getValue());
            	term.setIsVariable(true);
            	SPARQL_Filter f = new SPARQL_Filter();
            	f.addNotBound(term);
            	query.addFilter(f);
            }
            
//            System.out.println("--- referent: " + referent.toString()); // DEBUG
            for (Slot s : slots) {
//           	System.out.println("--- slot: " + s.toString()); // DEBUG
                if (s.getAnchor().equals(referent.getValue()) || s.getAnchor().equals(referent.toString())) {
//        			System.out.println("    fits!"); // DEBUG
                    template.addSlot(s);
                    break;
                }
            }
        }
        
        for (Slot s : slots) if (s.getAnchor().equals("SLOT_arg")) template.addSlot(s);
        
        Set<SPARQL_Triple> statements = new HashSet<SPARQL_Triple>();

        for (DRS_Condition condition : drs.getConditions()) {
            Set<SPARQL_Triple> scondition = convertCondition(condition,query,false).getConditions();
            statements.addAll(scondition);
            if (negate) {
                for (int i = 0; i < scondition.size(); ++i) {
                    SPARQL_Term term = ((SPARQL_Triple) scondition.toArray()[i]).getVariable();
                    if (query.isSelTerm(term)) {
                        SPARQL_Filter f = new SPARQL_Filter();
                        f.addNotBound(term);
                        query.addFilter(f);
                    }
                }
            }
        }
        
        if (query.getSelTerms().size() == 0)
        	query.setQt(SPARQL_QueryType.ASK);

        query.setConditions(statements);

        return query;
    }

	private Query convertCondition(DRS_Condition condition, Query query, boolean unionMode) {
            
            Query out;
            if (unionMode) out = new Query();
            else out = query;
            
        if (condition.isComplexCondition()) {
            if (!isSilent()) System.out.print("|complex:" + condition.toString());
            
            Complex_DRS_Condition complex = (Complex_DRS_Condition) condition;

            DRS restrictor = complex.getRestrictor();
            DRS_Quantifier quant = complex.getQuantifier();
            DRS scope = complex.getScope();

            if (quant.equals(DRS_Quantifier.OR)) {
               Set<SPARQL_Triple> conds_res = new HashSet<SPARQL_Triple>();
               Set<SPARQL_Triple> conds_scope = new HashSet<SPARQL_Triple>();
               Set<SPARQL_Filter> filter_res = new HashSet<SPARQL_Filter>();
               Set<SPARQL_Filter> filter_scope = new HashSet<SPARQL_Filter>();
                // call recursively
               Query dummy;
                for (DRS_Condition cond : restrictor.getConditions()) {
                    dummy = convertCondition(cond,out,true);
                    conds_res.addAll(dummy.getConditions());
                    filter_res.addAll(dummy.getFilters());
                    query.getPrefixes().addAll(dummy.getPrefixes());
                }
                for (DRS_Condition cond : scope.getConditions()) {
                    dummy = convertCondition(cond,out,true);
                    conds_scope.addAll(dummy.getConditions());
                    filter_scope.addAll(dummy.getFilters());
                    query.getPrefixes().addAll(dummy.getPrefixes());
                } 
                query.addUnion(new SPARQL_Union(conds_res,filter_res,conds_scope,filter_scope));
                // TODO also inherit order by, limit, offset, and so on?
                return query;
            } 
            else {
                // call recursively
                for (DRS_Condition cond : restrictor.getConditions()) {
                    out = convertCondition(cond,out,false);
                }
                for (DRS_Condition cond : scope.getConditions()) {
                    out = convertCondition(cond,out,false);
                }
            
                // add the quantifier at last
                DiscourseReferent ref = complex.getReferent();
                String sref = ref.getValue();
                String fresh;
                if (!isSilent()) System.out.print("|quantor:" + quant);

                switch (quant) {
                    case HOWMANY:
                        out.addSelTerm(new SPARQL_Term(sref, SPARQL_Aggregate.COUNT));
                        break;
                    case EVERY:
                        // probably save to ignore // TODO unless in cases like "which actor starred in every movie by spielberg?"
                        // query.addFilter(new SPARQL_Filter(new SPARQL_Term(sref)));
                        break;
                    case NO:
                        SPARQL_Filter f = new SPARQL_Filter();
                        f.addNotBound(new SPARQL_Term(sref));
                        out.addFilter(f);
                        break;
                    case FEW: //
                        break;
                    case MANY: //
                        break;
                    case MOST: //
                        break;
                    case SOME: //
                        break;
                    case THELEAST:
                        fresh = "c"+createFresh();
                        out.addSelTerm(new SPARQL_Term(sref, SPARQL_Aggregate.COUNT,fresh));
                        out.addOrderBy(new SPARQL_Term(fresh, SPARQL_OrderBy.ASC));
                        out.setLimit(1);
                        break;
                    case THEMOST:
                        fresh = "c"+createFresh();
                        out.addSelTerm(new SPARQL_Term(sref, SPARQL_Aggregate.COUNT,fresh));
                        out.addOrderBy(new SPARQL_Term(fresh, SPARQL_OrderBy.DESC));
                        out.setLimit(1);
                        break;
                }
            }
        } else if (condition.isNegatedCondition()) {
            if (!isSilent()) {
                System.out.print("|negation:" + condition.toString());
            }
            Negated_DRS neg = (Negated_DRS) condition;
            out = convert(neg.getDRS(), out, true);

        } else {
            Simple_DRS_Condition simple = (Simple_DRS_Condition) condition;

            if (!isSilent()) {
                System.out.print(isSilent() + "|simple:" + condition.toString());
            }
            
            int arity = simple.getArguments().size(); 
            String predicate = simple.getPredicate();
            if (predicate.startsWith("SLOT")) {
            	for (Slot s : slots) {
            		if (s.getAnchor().equals(predicate)) {
            			s.setToken(predicate);
            			predicate = "p" + createFresh();
            			s.setAnchor(predicate);
            			template.addSlot(s);
            			break;
            		}
            		else if (s.getToken().equals(predicate)) {
            			predicate = s.getAnchor();
            		}
            	}
            }
            SPARQL_Property prop = new SPARQL_Property(predicate);
            if (!predicate.contains(":")) prop.setIsVariable(true);
            
            boolean literal = false; 
            if (simple.getArguments().size() > 1 && (simple.getArguments().get(1).getValue().startsWith("\'") || simple.getArguments().get(1).getValue().matches("[0-9]+"))) {
            	literal = true;
            }

            if (predicate.equals("count")) {
            	// COUNT(?x) AS ?c
                if (simple.getArguments().get(1).getValue().matches("[0-9]+")) {
                    String fresh = "v"+createFresh();
                    out.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_Aggregate.COUNT, fresh));
                    out.addHaving(new SPARQL_Having("?"+fresh + " = " + simple.getArguments().get(1).getValue()));
                } else {
                    out.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_Aggregate.COUNT, simple.getArguments().get(1).getValue()));
                }
                return out;
            } else if (predicate.equals("sum")) {
                out.addSelTerm(new SPARQL_Term(simple.getArguments().get(1).getValue(), SPARQL_Aggregate.SUM));
                return out;
            } else if (predicate.equals("greater")) {
                out.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.GT)));
                return out;
            } else if (predicate.equals("greaterorequal")) {
                out.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.GTEQ)));
                return out;
            } else if (predicate.equals("less")) {
                out.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.LT)));
                return out;
            } else if (predicate.equals("lessorequal")) {
                out.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.LTEQ)));
                return out;
            } else if (predicate.equals("maximum")) {
//                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(),false));
                out.addOrderBy(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_OrderBy.DESC));
                out.setLimit(1);
                return out;
            } else if (predicate.equals("minimum")) {
                out.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(),false));
                out.addOrderBy(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_OrderBy.ASC));
                out.setLimit(1);
                return out;
            } else if (predicate.equals("equals")) {
                out.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.EQ)));
                return out;
            }
            else if (predicate.equals("DATE")) {
            	out.addFilter(new SPARQL_Filter(
            			new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term("'^"+simple.getArguments().get(1).getValue()+"'",false),
                        SPARQL_PairType.REGEX)));
            }
            else if (predicate.equals("regex")) {
            	out.addFilter(new SPARQL_Filter(
            			new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue().replace("_","").trim(),false),
                        SPARQL_PairType.REGEX)));
            }
            else {
	            if (arity == 1) {
	            	SPARQL_Term term = new SPARQL_Term(simple.getArguments().get(0).getValue(),false);term.setIsVariable(true);
	            	out.addCondition(new SPARQL_Triple(term,new SPARQL_Property("type",new SPARQL_Prefix("rdf","")),prop));
	            }
	            else if (arity == 2) {
	            	String arg1 = simple.getArguments().get(0).getValue();                   
                        SPARQL_Term term1 = new SPARQL_Term(arg1,arg1.contains(":"),!arg1.matches("(\\?)?[0-9]+"));
	            	String arg2 = simple.getArguments().get(1).getValue();
                        SPARQL_Term term2 = new SPARQL_Term(arg2,arg2.contains(":"),!arg2.matches("(\\?)?[0-9]+"));
	            	out.addCondition(new SPARQL_Triple(term1,prop,term2));
	            }
	            else if (arity > 2) {
	            	// TODO
	            }
            }
        }
        
        // TODO this is a hack in order to avoid ASK queries if DP is parsed
        if (oxford) {
            Hashtable<String,Integer> vs = new Hashtable<String,Integer>();
            String v1; String v2;
            for (SPARQL_Triple c : out.getConditions()) {
                v1 = c.getVariable().toString().replace("?","");
                v2 = c.getValue().toString().replace("?","");
                // is it a slot variable?
                boolean v1isSlotVar = false;
                boolean v2isSlotVar = false;
                for (Slot s : slots) {
                    if (s.getAnchor().equals(v1)) v1isSlotVar = true; 
                    if (s.getAnchor().equals(v2)) v2isSlotVar = true;
                }
                if (!v1isSlotVar && !v1.matches("(\\?)?[0-9]+") && !v1.contains("count")) {
                    if (vs.containsKey(v1)) vs.put(v1,vs.get(v1)+1);
                    else vs.put(v1,1);
                }
                if (!v2isSlotVar && !v2.matches("(\\?)?[0-9]+") && !v2.contains("count")) {
                    if (vs.containsKey(v2)) vs.put(v2,vs.get(v2)+1);
                    else vs.put(v2,1);
                }
            }
            
            int max = 0; String maxvar = null;
            for (String var : vs.keySet()) {
                if (vs.get(var) > max) {
                    max = vs.get(var);
                    maxvar = var;
                }
            }
            if (maxvar != null) {
                    SPARQL_Term term = new SPARQL_Term(maxvar);
                    term.setIsVariable(true);
                    out.addSelTerm(term);
                }
        }
        
        return out;
    }

    public void redundantEqualRenaming(DRS drs) {
        
        Set<Simple_DRS_Condition> equalsConditions = new HashSet<Simple_DRS_Condition>();
        for (Simple_DRS_Condition c : drs.getAllSimpleConditions()) {
        	if(c.getPredicate().equals("equal"))
                    equalsConditions.add(c);
        }
        
        DiscourseReferent firstArg;
        DiscourseReferent secondArg;
        boolean firstIsURI;
        boolean secondIsURI;
        boolean firstIsInt;
        boolean secondIsInt;
        
        for (Simple_DRS_Condition c : equalsConditions) {
        
            firstArg = c.getArguments().get(0);
            secondArg = c.getArguments().get(1);
            firstIsURI = isUri(firstArg.getValue());
            secondIsURI = isUri(secondArg.getValue());
            firstIsInt = firstArg.getValue().matches("(\\?)?[0..9]+"); 
            secondIsInt = secondArg.getValue().matches("(\\?)?[0..9]+");

            drs.removeCondition(c);
            if (firstIsURI) { //  firstIsURI || firstIsInt
                drs.replaceEqualRef(secondArg, firstArg, true);
                for (Slot s : slots) {
                	if (s.getAnchor().equals(secondArg.getValue()))
                            s.setAnchor(firstArg.getValue());
                	if (s.getWords().contains(secondArg.getValue())) {
                            s.getWords().remove(secondArg.getValue());
                            s.getWords().add(firstArg.getValue());
                        }
                }
            } else if (secondIsURI) { // secondIsURI || secondIsInt
                drs.replaceEqualRef(firstArg, secondArg, true);
                for (Slot s : slots) {
                	if (s.getAnchor().equals(firstArg.getValue()))
                            s.setAnchor(secondArg.getValue());
                	if (s.getWords().contains(firstArg.getValue())) {
                            s.getWords().remove(firstArg.getValue());
                            s.getWords().add(secondArg.getValue());
                        }
                }
            } else {
                drs.replaceEqualRef(firstArg, secondArg, false);
                for (Slot s : slots) {
                	if (s.getAnchor().equals(firstArg.getValue())) {
                		s.setAnchor(secondArg.getValue());
                	}
                }
            }
        }
        
        // finally remove all conditions that ended up of form equal(y,y)
        Set<Simple_DRS_Condition> equalEqualsConditions = new HashSet<Simple_DRS_Condition>();
        for (Simple_DRS_Condition c : drs.getAllSimpleConditions()) {
        	if(c.getPredicate().equals("equal") && c.getArguments().get(0).getValue().equals(c.getArguments().get(1).getValue())) {
        		equalEqualsConditions.add(c);
        	}
        }
        for (Simple_DRS_Condition c : equalEqualsConditions) {
        	drs.removeCondition(c);
        }
    }
    
    private boolean replaceRegextoken(DRS drs) {
                        
        Set<Simple_DRS_Condition> cs = new HashSet<Simple_DRS_Condition>();
        for (Simple_DRS_Condition c : drs.getAllSimpleConditions()) {
            if(c.getPredicate().equals("regextoken")) {
                for (DiscourseReferent arg : c.getArguments()) {
                    if (arg.getValue().matches("[1-9]+")) return false;
                    else cs.add(c);
                }
            }
        }
        
        String var;
        String newvar;
        List<String> regexs = new ArrayList<String>();
        String[] forbidden = {"regextoken","regex","count","minimum","maximum","greater","less","greaterorequal","lessorequal","equal","sum","location","description"};
        Set<Simple_DRS_Condition> used = new HashSet<Simple_DRS_Condition>();
        
        for (Simple_DRS_Condition c : cs) {
            var = c.getArguments().get(1).getValue();
            newvar = c.getArguments().get(0).getValue();
            for (Simple_DRS_Condition cond : drs.getAllSimpleConditions()) {
                boolean takeit = false;
                for (DiscourseReferent dr : cond.getArguments()) {
                    if (dr.getValue().equals(var)) { 
                        takeit = true;
                        for (String f : forbidden) if (cond.getPredicate().contains(f)) takeit= false;
                    }
                }
                if (takeit) {
                    for (String s : cond.getPredicate().replace("SLOT","").replaceAll("_"," ").trim().split(" ")) {
                        regexs.add(s);
                    }
                    used.add(cond);
                }
                else if (!cond.getPredicate().equals("regextoken")) {
                    for (DiscourseReferent dr : cond.getArguments()) {
                        if (dr.getValue().equals(var)) dr.setValue(newvar);
                    }
                }
            }
            if (!regexs.isEmpty()) {
                c.getArguments().remove(1);
                c.getArguments().add(new DiscourseReferent("'"+orderedRegex(regexs)+"'"));
                c.setPredicate("regex");
            }
            else { used.add(c); } // TODO should not happen!
            for (Slot s : slots) {
                if (s.getWords().contains(var)) {
                    s.getWords().remove(var);
                    s.getWords().add(newvar);
                }
            }
        }
        for (Simple_DRS_Condition cond : used) drs.removeCondition(cond);
        
        // postprocessing
        boolean success = false;
        Set<Simple_DRS_Condition> oldconds = new HashSet<Simple_DRS_Condition>();
        for (Simple_DRS_Condition c : drs.getAllSimpleConditions()) {
            String d = "";
            String d2 = "";
            List<String> regextokens = new ArrayList<String>();
            if (c.getPredicate().equals("SLOT_description")) {
                d = c.getArguments().get(0).getValue();
                d2 = c.getArguments().get(1).getValue();
                for (Simple_DRS_Condition cond : drs.getAllSimpleConditions()) {
                    if (cond.getPredicate().equals("SLOT_description") && cond.getArguments().get(1).getValue().equals(d)) {
                        oldconds.add(c);
                        success = true;
                        break;
                    }
                }
            }
            if (success) {
                for (Simple_DRS_Condition cond : drs.getAllSimpleConditions()) {
                    if (cond.getPredicate().equals("regex") && 
                            (cond.getArguments().get(0).getValue().equals(d) || cond.getArguments().get(0).getValue().equals(d2))) {
                        for (String s : cond.getArguments().get(1).getValue().replaceAll("'","").replaceAll("_"," ").trim().split(" ")) {
                            regextokens.add(s);
                        }
                        oldconds.add(cond);
                    }
                }
                for (Simple_DRS_Condition cond : oldconds) drs.removeCondition(cond);
                List<DiscourseReferent> newrefs = new ArrayList<DiscourseReferent>();
                newrefs.add(new DiscourseReferent(d));
                newrefs.add(new DiscourseReferent("'"+orderedRegex(regextokens)+"'"));
                drs.addCondition(new Simple_DRS_Condition("regex",newrefs));
                break;
            }
        }
        return true;
    }
    

    private boolean restructureEmpty(DRS drs) {
 
    	Set<Simple_DRS_Condition> emptyConditions = new HashSet<Simple_DRS_Condition>();
        for (Simple_DRS_Condition c : drs.getAllSimpleConditions()) {
        	if(c.getPredicate().equals("empty") || c.getPredicate().equals("empty_data")) {
                    for (DiscourseReferent arg : c.getArguments()) {
                        if (arg.getValue().matches("[1-9]+")) drs.removeCondition(c);
                        else emptyConditions.add(c);
                    }
        	}
        }
        if (emptyConditions.isEmpty()) {
        	return true;
        }
        
        boolean globalsuccess = false;
        for (Simple_DRS_Condition c : emptyConditions) {
        	String nounToExpand; 
        	String fallbackNoun; 
                boolean datatype = false;
                if (c.getPredicate().equals("empty")) {
                    nounToExpand = c.getArguments().get(1).getValue();
                    fallbackNoun = c.getArguments().get(0).getValue();
                } else {
                    nounToExpand = c.getArguments().get(0).getValue(); 
                    fallbackNoun = c.getArguments().get(1).getValue(); // TODO das ist quark...
                    datatype = true;
                }
        	boolean success = false;
        	loop: 
        	for (Simple_DRS_Condition sc : drs.getAllSimpleConditions()) {
        		if (sc.getArguments().size() == 1 && sc.getArguments().get(0).getValue().equals(nounToExpand)) {
        			for (Slot s : slots) {
        				if (s.getAnchor().equals(sc.getPredicate())) {
        					if (s.getSlotType().equals(SlotType.CLASS)) {
        						s.setSlotType(SlotType.PROPERTY); 
        						List<DiscourseReferent> newargs = new ArrayList<DiscourseReferent>();
                    			newargs.add(c.getArguments().get(0));
                                        if (datatype) newargs.add(c.getArguments().get(1));
                                        else newargs.add(sc.getArguments().get(0));
                    			sc.setArguments(newargs);
                    			success = true;
                    			globalsuccess = true;
//                    			break loop;
        					}
        				}	
        			}
        		}
        	}
        	if (!success) { // do the same for fallbackNoun
        		loop: 
                for (Simple_DRS_Condition sc : drs.getAllSimpleConditions()) {
                	if (sc.getArguments().size() == 1 && sc.getArguments().get(0).getValue().equals(fallbackNoun)) {
                		for (Slot s : slots) {
                			if (s.getAnchor().equals(sc.getPredicate())) {
                				if (s.getSlotType().equals(SlotType.CLASS)) {
                					s.setSlotType(SlotType.PROPERTY); 
                					List<DiscourseReferent> newargs = new ArrayList<DiscourseReferent>();
                           			newargs.add(c.getArguments().get(1));
                           			newargs.add(sc.getArguments().get(0));
                           			sc.setArguments(newargs);
                           			success = true;
                           			globalsuccess = true;
                           			break loop;
                				}
                			}	
                		}
                	}
                }
        	}
        }
        
        if (globalsuccess) {
        	for (Simple_DRS_Condition c : emptyConditions) {
        		drs.removeCondition(c);
        	}
        }
        return globalsuccess;
	}

    private boolean isUri(String arg) {
        return false; // TODO
    }
    
    private int createFresh() {
		
        int fresh = 0;
	for (int i = 0; usedInts.contains(i); i++) {
            fresh = i+1 ;
	}
	usedInts.add(fresh);
	return fresh;
    }
    
    private String orderedRegex(List<String> regextokens) {
        
        String newregex = "";
        if (inputstring != null) {
            String[] inputparts = inputstring.split(" ");
            TreeMap<Integer,String> regexparts = new TreeMap<Integer,String>();
            for (String s : regextokens) {
                for (int i = 0; i < inputparts.length; i++) {
                    if (inputparts[i].matches(s+"(/\\w+)?")) {
                        regexparts.put(i,s);
                        break;
                    }
                }
            }
            for (int n : regexparts.descendingKeySet()) {
                newregex = regexparts.get(n) + " " + newregex;
            }
         } 
         else for (String s : regextokens) newregex += s + " ";
        
        return newregex.trim();
        }
    
}
