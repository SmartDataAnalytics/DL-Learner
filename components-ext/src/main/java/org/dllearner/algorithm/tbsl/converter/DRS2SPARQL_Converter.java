package org.dllearner.algorithm.tbsl.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sem.drs.Complex_DRS_Condition;
import org.dllearner.algorithm.tbsl.sem.drs.DRS;
import org.dllearner.algorithm.tbsl.sem.drs.DRS_Condition;
import org.dllearner.algorithm.tbsl.sem.drs.DRS_Quantifier;
import org.dllearner.algorithm.tbsl.sem.drs.DiscourseReferent;
import org.dllearner.algorithm.tbsl.sem.drs.Negated_DRS;
import org.dllearner.algorithm.tbsl.sem.drs.Simple_DRS_Condition;
import org.dllearner.algorithm.tbsl.sparql.Query;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Aggregate;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Filter;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_OrderBy;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Pair;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_PairType;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Prefix;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Property;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_QueryType;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Term;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Triple;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.SlotType;
import org.dllearner.algorithm.tbsl.sparql.Template;


public class DRS2SPARQL_Converter {

    // suppresses console output
    private boolean silent = true;
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
        q.setPrefixes(prefixes);
        
        template.setQuery(q);
        
        if (!isSilent()) {
            System.out.println("... done");
        }

        return template;
    }

    private Query convert(DRS drs, Query query, boolean negate) {
    	
//        System.out.println("--- DRS (before): " + drs); // DEBUG   	
        redundantEqualRenaming(drs); 
        restructureEmpty(drs);
//        System.out.println("--- DRS (after) : " + drs); // DEBUG
            
        for (DiscourseReferent referent : drs.getDRs()) {
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
            for (Slot s : slots) {
        		if (s.getAnchor().equals(referent.toString())) {
        			template.addSlot(s);
        			break;
        		}
        	}
        }
        
        Set<SPARQL_Triple> statements = new HashSet<SPARQL_Triple>();

        for (DRS_Condition condition : drs.getConditions()) {
            Set<SPARQL_Triple> scondition = convertCondition(condition, query).getConditions();
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

	private Query convertCondition(DRS_Condition condition, Query query) {
        if (condition.isComplexCondition()) {
            if (!isSilent()) {
                System.out.print("|complex:" + condition.toString());
            }
            Complex_DRS_Condition complex = (Complex_DRS_Condition) condition;

            DRS restrictor = complex.getRestrictor();
            DRS_Quantifier quant = complex.getQuantifier();
            DRS scope = complex.getScope();

            // call recursively
            for (DRS_Condition cond : restrictor.getConditions()) {
                query = convertCondition(cond, query);
            }
            for (DRS_Condition cond : scope.getConditions()) {
                query = convertCondition(cond, query);
            }
            // add the quantifier at last
            DiscourseReferent ref = complex.getReferent();
            String sref = ref.getValue();
            String fresh;
            if (!isSilent()) {
                System.out.print("|quantor:" + quant);
            }
            switch (quant) {
                case HOWMANY:
                    query.addSelTerm(new SPARQL_Term(sref, SPARQL_Aggregate.COUNT));
                    break;
                case EVERY:
                    // probably save to ignore // TODO unless in cases like "which actor starred in every movie by spielberg?"
                    // query.addFilter(new SPARQL_Filter(new SPARQL_Term(sref)));
                    break;
                case NO:
                    SPARQL_Filter f = new SPARQL_Filter();
                    f.addNotBound(new SPARQL_Term(sref));
                    query.addFilter(f);
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
                    query.addSelTerm(new SPARQL_Term(sref, SPARQL_Aggregate.COUNT,fresh));
                    query.addOrderBy(new SPARQL_Term(fresh, SPARQL_OrderBy.ASC));
                    query.setLimit(1);
                    break;
                case THEMOST:
                	fresh = "c"+createFresh();
                    query.addSelTerm(new SPARQL_Term(sref, SPARQL_Aggregate.COUNT,fresh));
                    query.addOrderBy(new SPARQL_Term(fresh, SPARQL_OrderBy.DESC));
                    query.setLimit(1);
                    break;
            }
        } else if (condition.isNegatedCondition()) {
            if (!isSilent()) {
                System.out.print("|negation:" + condition.toString());
            }
            Negated_DRS neg = (Negated_DRS) condition;
            query = convert(neg.getDRS(), query, true);

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
            prop.setIsVariable(true);
            
            boolean literal = false; 
            if (simple.getArguments().size() > 1 && simple.getArguments().get(1).getValue().matches("\\d+")) {
            	literal = true;
            }

            if (predicate.equals("count")) {
            	// COUNT(?x) AS ?c
                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_Aggregate.COUNT, simple.getArguments().get(1).getValue()));
                return query;
            } else if (predicate.equals("sum")) {
                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(1).getValue(), SPARQL_Aggregate.SUM));
                return query;
            } else if (predicate.equals("greater")) {
                query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.GT)));
                return query;
            } else if (predicate.equals("greaterorequal")) {
                query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.GTEQ)));
                return query;
            } else if (predicate.equals("less")) {
                query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.LT)));
                return query;
            } else if (predicate.equals("lessorequal")) {
                query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.LTEQ)));
                return query;
            } else if (predicate.equals("maximum")) {
//                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(),false));
                query.addOrderBy(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_OrderBy.DESC));
                query.setLimit(1);
                return query;
            } else if (predicate.equals("minimum")) {
                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(),false));
                query.addOrderBy(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_OrderBy.ASC));
                query.setLimit(1);
                return query;
            } else if (predicate.equals("equal")) {
                query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),true),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),literal),
                        SPARQL_PairType.EQ)));
                return query;
            }
            else if (predicate.equals("DATE")) {
            	query.addFilter(new SPARQL_Filter(
            			new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term("'^"+simple.getArguments().get(1).getValue()+"'",true),
                        SPARQL_PairType.REGEX)));
            }
            else {
	            if (arity == 1) {
	            	SPARQL_Term term = new SPARQL_Term(simple.getArguments().get(0).getValue(),false);term.setIsVariable(true);
	            	query.addCondition(new SPARQL_Triple(term,new SPARQL_Property("type",new SPARQL_Prefix("rdf","")),prop));
	            }
	            else if (arity == 2) {
	            	String arg1 = simple.getArguments().get(0).getValue();SPARQL_Term term1 = new SPARQL_Term(arg1,false);term1.setIsVariable(true);
	            	String arg2 = simple.getArguments().get(1).getValue();SPARQL_Term term2 = new SPARQL_Term(arg2,false);term2.setIsVariable(true);
	            	query.addCondition(new SPARQL_Triple(term1, prop, term2));
	            }
	            else if (arity > 2) {
	            	// TODO
	            }
            }
        }
        return query;
    }

    public void redundantEqualRenaming(DRS drs) {

        Set<Simple_DRS_Condition> equalsConditions = new HashSet<Simple_DRS_Condition>();
        for (Simple_DRS_Condition c : drs.getAllSimpleConditions()) {
        	if(c.getPredicate().equals("equal")) {
        		equalsConditions.add(c);
        	}
        }
        
        DiscourseReferent firstArg;
        DiscourseReferent secondArg;
        boolean firstIsURI;
        boolean secondIsURI;
        
        for (Simple_DRS_Condition c : equalsConditions) {
        
        	firstArg = c.getArguments().get(0);
            secondArg = c.getArguments().get(1);
            firstIsURI = isUri(firstArg.getValue());
            secondIsURI = isUri(secondArg.getValue());

            boolean oneArgIsInt = firstArg.toString().matches("[0..9]") || secondArg.toString().matches("[0..9]");

            drs.removeCondition(c);
            if (firstIsURI) {
                drs.replaceEqualRef(secondArg, firstArg, false);
                for (Slot s : slots) {
                	if (s.getAnchor().equals(secondArg.getValue())) {
                		s.setAnchor(firstArg.getValue());
                	}
                }
            } else if (secondIsURI) {
                drs.replaceEqualRef(firstArg, secondArg, false);
                for (Slot s : slots) {
                	if (s.getAnchor().equals(firstArg.getValue())) {
                		s.setAnchor(secondArg.getValue());
                	}
                }
            } else if (!oneArgIsInt) {
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
    

    private void restructureEmpty(DRS drs) {
    	
    	Set<Simple_DRS_Condition> emptyConditions = new HashSet<Simple_DRS_Condition>();
        for (Simple_DRS_Condition c : drs.getAllSimpleConditions()) {
        	if(c.getPredicate().equals("empty")) {
        		emptyConditions.add(c);
        	}
        }
        
        for (Simple_DRS_Condition c : emptyConditions) {
        	String nounToExpand = c.getArguments().get(1).getValue();
        	for (Simple_DRS_Condition sc : drs.getAllSimpleConditions()) {
        		if (sc.getArguments().size() == 1 && sc.getArguments().get(0).getValue().equals(nounToExpand)) {
        			List<DiscourseReferent> newargs = new ArrayList<DiscourseReferent>();
        			newargs.add(c.getArguments().get(0));
        			newargs.add(sc.getArguments().get(0));
        			sc.setArguments(newargs);
        			for (Slot s : slots) {
        				if (s.getAnchor().equals(sc.getPredicate())) {
        					s.setSlotType(SlotType.PROPERTY); 
        					break;
        				}
        			}
        			break;
        		}
        	}
        }
        
        for (Simple_DRS_Condition c : emptyConditions) {
        	drs.removeCondition(c);
        }
		
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
}
