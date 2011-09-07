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
import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Aggregate;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Filter;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_OrderBy;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Pair;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_PairType;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Property;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_QueryType;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Term;
import org.dllearner.algorithm.tbsl.sparql.Slot;


public class DRS2BasicSPARQL_Converter {

    List<Slot> slots;
    BasicQueryTemplate query;
    List<Integer> usedInts;

    public DRS2BasicSPARQL_Converter() {
    	query = new BasicQueryTemplate();
    	usedInts = new ArrayList<Integer>();
    }
    
    public void setSlots(List<Slot> ls) {
    	slots = ls;
    }

    // TODO ??
    public List<SPARQL_Property> getProperties(Complex_DRS_Condition cond) {
        List<SPARQL_Property> retVal = new ArrayList<SPARQL_Property>();

        return retVal;
    }

    public BasicQueryTemplate convert(DRS drs,List<Slot> ls) {
        
        query = new BasicQueryTemplate();
        slots = ls;
        
        return convert(drs, new BasicQueryTemplate(), false);
    }

    private BasicQueryTemplate convert(DRS drs, BasicQueryTemplate query, boolean negate) {
    	
        redundantEqualRenaming(drs); 
            
        for (DRS_Condition condition : drs.getConditions()) {
            convertCondition(condition,query);
            if (negate) {
            	for (SPARQL_Term term : query.getSelTerms()) {
            		SPARQL_Filter f = new SPARQL_Filter();
                    f.addNotBound(term);
                    query.addFilter(f);
            	}
            }
        }
        
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
        		if (s.getAnchor().equals(referent.getValue())) {
        			query.addSlot(s);
        			break;
        		}
        	}
        }
        
        if (query.getSelTerms().size() == 0)
        	query.setQt(SPARQL_QueryType.ASK);

        return query;
    }

    private BasicQueryTemplate convertCondition(DRS_Condition condition, BasicQueryTemplate query) {

    	if (condition.isComplexCondition()) {

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
            Negated_DRS neg = (Negated_DRS) condition;
            query = convert(neg.getDRS(), query, true);

        } else {
            Simple_DRS_Condition simple = (Simple_DRS_Condition) condition;
            
            String predicate = simple.getPredicate();
            if (predicate.startsWith("SLOT")) {
            	for (Slot s : slots) {
            		if (s.getAnchor().equals(predicate)) {
            			s.setToken(predicate);
            			predicate = "p" + createFresh();
           				s.setAnchor(simple.getArguments().get(0).getValue());
            			break;
            		}
            		else if (s.getToken().equals(predicate)) {
            			predicate = s.getAnchor();
            		}
            	}
            }
            
            SPARQL_Property prop = new SPARQL_Property(predicate);
            prop.setIsVariable(true);
            
            boolean noliteral = true; 
            if (simple.getArguments().size() > 1 && simple.getArguments().get(1).getValue().matches("\\d+")) {
            	noliteral = false;
            }

            if (predicate.equals("p")) {
            	query.addConditions(simple.toString());
            }
            else if (predicate.equals("count")) {
            	// COUNT(?x) AS ?c
            	if (noliteral) {
            		query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_Aggregate.COUNT, simple.getArguments().get(1).getValue()));
            		return query;
            	}
            	else {
            		String fresh = "c"+createFresh();
            		query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_Aggregate.COUNT, fresh));
            		query.addFilter(new SPARQL_Filter(
            				new SPARQL_Pair(
            				new SPARQL_Term(fresh),
            				new SPARQL_Term(simple.getArguments().get(1).getValue(),true),
            				SPARQL_PairType.EQ)));
            		return query;
            	}
            } else if (predicate.equals("sum")) {
                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(1).getValue(), SPARQL_Aggregate.SUM));
                return query;
            } else if (predicate.equals("greater")) {
            	query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),noliteral),
                        SPARQL_PairType.GT)));
                return query;
            } else if (predicate.equals("greaterorequal")) {
            	query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),noliteral),
                        SPARQL_PairType.GTEQ)));
                return query;
            } else if (predicate.equals("less")) {
            	query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),noliteral),
                        SPARQL_PairType.LT)));
                return query;
            } else if (predicate.equals("lessorequal")) {
            	query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),noliteral),
                        SPARQL_PairType.LTEQ)));
                return query;
            } else if (predicate.equals("maximum")) {
                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue()));
                query.addOrderBy(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_OrderBy.DESC));
                query.setLimit(1);                
                return query;
            } else if (predicate.equals("minimum")) {
                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue()));
                query.addOrderBy(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_OrderBy.ASC));
                query.setLimit(1);  
                return query;
            } else if (predicate.equals("countmaximum")) {
                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_Aggregate.COUNT, "c"));
                query.addOrderBy(new SPARQL_Term("c", SPARQL_OrderBy.DESC)); 
                query.setLimit(1);
            	return query;
            } else if (predicate.equals("countminimum")) {
                query.addSelTerm(new SPARQL_Term(simple.getArguments().get(0).getValue(), SPARQL_Aggregate.COUNT, "c"));
                query.addOrderBy(new SPARQL_Term("c", SPARQL_OrderBy.DESC));
                query.setLimit(1);
            	return query;
            } else if (predicate.equals("equal")) {
            	query.addFilter(new SPARQL_Filter(
                        new SPARQL_Pair(
                        new SPARQL_Term(simple.getArguments().get(0).getValue(),false),
                        new SPARQL_Term(simple.getArguments().get(1).getValue(),noliteral),
                        SPARQL_PairType.EQ)));
                return query;
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
        
        for (Simple_DRS_Condition c : equalsConditions) {
        	
        	firstArg = c.getArguments().get(0);
            secondArg = c.getArguments().get(1);

            boolean oneArgIsInt = firstArg.getValue().matches("\\d+") || secondArg.getValue().matches("\\d+");

            drs.removeCondition(c);
            if (!oneArgIsInt) {
                drs.replaceEqualRef(firstArg, secondArg, false);
            } else {
            	drs.replaceEqualRef(firstArg, secondArg, true);
            }
            for (Slot s : slots) {
             	if (s.getAnchor().equals(firstArg.getValue())) {
               		s.setAnchor(secondArg.getValue());
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
    
	private int createFresh() {
		
		int fresh = 0;
		for (int i = 0; usedInts.contains(i); i++) {
			fresh = i+1 ;
		}
		usedInts.add(fresh);
		return fresh;
	}
}
