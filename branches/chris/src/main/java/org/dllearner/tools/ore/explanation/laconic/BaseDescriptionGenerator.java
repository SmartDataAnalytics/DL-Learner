
package org.dllearner.tools.ore.explanation.laconic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;



public abstract class BaseDescriptionGenerator
    implements OWLClassExpressionVisitorEx<Set<OWLClassExpression>>
{
	private OWLDataFactory factory;
    private static TopTester topChecker = new TopTester();
    private static BottomTester bottomChecker = new BottomTester();
    
    

    public BaseDescriptionGenerator(OWLDataFactory factory)
    {
        this.factory = factory;
    }

    public boolean isThing(OWLClassExpression description)
    {
        return ((Boolean)description.accept(topChecker)).booleanValue(); 
    }

    public boolean isNothing(OWLClassExpression description)
    {
        return ((Boolean)description.accept(bottomChecker)).booleanValue();
    }

    public OWLDataFactory getDataFactory()
    {
        return factory;
    }

    public Set<OWLClassExpression> computeTau(OWLClassExpression desc)
    {
        Tau gen = new Tau(factory);
        return desc.accept(gen);
    }

    public Set<OWLClassExpression> computeBeta(OWLClassExpression desc)
    {
        Beta gen = new Beta(factory);
        return (Set<OWLClassExpression>)desc.accept(gen);
    }

    private Set<Set<OWLClassExpression>> computeReplacements(Set<OWLClassExpression> operands)
    {
        Set<List<OWLClassExpression>> ps = new HashSet<List<OWLClassExpression>>();
        ps.add(new ArrayList<OWLClassExpression>());

        for(OWLClassExpression op : operands)
        {
            Set<List<OWLClassExpression>> pscopy = new HashSet<List<OWLClassExpression>>(ps);
            
            for(OWLClassExpression d : (Set<OWLClassExpression>)op.accept(this)) {
                for(List<OWLClassExpression> pselement : pscopy) {
                    ArrayList<OWLClassExpression> union = new ArrayList<OWLClassExpression>();
                    union.addAll(pselement);
                    union.add(d);
                    ps.remove(pselement);
                    ps.add(union);
                }
            }
        }

        Set<Set<OWLClassExpression>> result = new HashSet<Set<OWLClassExpression>>();
        
        for(List<OWLClassExpression> desc : ps ){
        	result.add(new HashSet<OWLClassExpression>(desc));
        }
        return result;
    }

    public Set<OWLClassExpression> visit(OWLObjectIntersectionOf desc)
    {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        Set<Set<OWLClassExpression>> conjunctions = computeReplacements(desc.getOperands());
        for(Set<OWLClassExpression> conjuncts : conjunctions){
        
        	for(Iterator<OWLClassExpression> i = conjuncts.iterator(); i.hasNext();){
        		OWLClassExpression conjunct = i.next();
        		if(isThing(conjunct)){
                    i.remove();
        		}
        	}
               
            if(conjuncts.isEmpty())
                descs.add(factory.getOWLThing());
            else
            if(conjuncts.size() != 1)
                descs.add(factory.getOWLObjectIntersectionOf(conjuncts));
            else
                descs.addAll(conjuncts);
        }

        descs.add(getLimit());
        return descs;
    }

    public Set<OWLClassExpression> visit(OWLObjectUnionOf desc)
    {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        Set<Set<OWLClassExpression>> disjunctions = computeReplacements(desc.getOperands());
        for(Set<OWLClassExpression> disjuncts : disjunctions){
        
//            for(OWLClassExpression disjunct : disjuncts){
//                if(isNothing(disjunct)){
//                    disjuncts.remove(disjunct);
//                }
//            } 
        	for(Iterator<OWLClassExpression> i = disjuncts.iterator(); i.hasNext();){
            	OWLClassExpression disjunct = i.next();
                if(isNothing(disjunct)){
                    i.remove();
                }
            } 
            if(disjuncts.size() != 1){
                descs.add(factory.getOWLObjectUnionOf(disjuncts));
            } else{
//                descs.add(disjuncts.iterator().next());
                descs.addAll(disjuncts);
            }
        }

        descs.add(getLimit());
        return descs;
    }

    public Set<OWLClassExpression> visit(OWLObjectSomeValuesFrom desc)
    {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        descs.add(desc);
        for(OWLClassExpression filler : desc.getFiller().accept(this)){
            if(!isNothing(filler))
                descs.add(factory.getOWLObjectSomeValuesFrom((OWLObjectPropertyExpression)desc.getProperty(), filler));
        }
        descs.add(getLimit());
        return descs;
    }

    public Set<OWLClassExpression> visit(OWLObjectAllValuesFrom desc)
    {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
       
        for(OWLClassExpression filler : desc.getFiller().accept(this)){
        	if(!isThing(filler))
                descs.add(factory.getOWLObjectAllValuesFrom((OWLObjectPropertyExpression)desc.getProperty(), filler));
        }
        descs.add(getLimit());
        return descs;
    }

    public Set<OWLClassExpression> visit(OWLObjectHasValue desc)
    {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        descs.add(desc);
        
//        for(OWLClassExpression filler : factory.getOWLObjectOneOf(new OWLIndividual[] {(OWLIndividual)desc.getValue()}).accept(this)){ 
//        	descs.add(factory.getOWLObjectSomeRestriction((OWLObjectPropertyExpression)desc.getProperty(), filler));
//        }
        OWLClassExpression d = factory.getOWLObjectSomeValuesFrom((OWLObjectPropertyExpression)desc.getProperty(), getLimit());
        if(!isNothing(d)){
        	descs.add(d);
        }

        descs.add(getLimit());
        return descs;
    }

    public Set<OWLClassExpression> visit(OWLObjectExactCardinality desc)
    {
        Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
        OWLClassExpression min = getDataFactory().getOWLObjectMinCardinality(desc.getCardinality(), (OWLObjectPropertyExpression)desc.getProperty(), (OWLClassExpression)desc.getFiller());
        result.addAll(min.accept(this));
        OWLClassExpression max = getDataFactory().getOWLObjectMaxCardinality(desc.getCardinality(), (OWLObjectPropertyExpression)desc.getProperty(), (OWLClassExpression)desc.getFiller());
        result.addAll(max.accept(this));
        result.add(getLimit());
        return result;
    }

    public Set<OWLClassExpression> visit(OWLObjectHasSelf desc)
    {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        descs.add(desc);
        descs.add(getLimit());
        return descs;
    }

    public Set<OWLClassExpression> visit(OWLObjectOneOf desc)
    {
        Set<OWLClassExpression> ops = new HashSet<OWLClassExpression>();
        if(desc.getIndividuals().size() == 1)
        {
            ops.add(desc);
            ops.add(getLimit());
            return ops;
        }
        
        for(OWLIndividual ind :  desc.getIndividuals()){
        	ops.add(factory.getOWLObjectOneOf(new OWLIndividual[] {ind}));
        }

        OWLClassExpression rewrite = factory.getOWLObjectUnionOf(ops);
        return rewrite.accept(this);
    }

    protected abstract OWLClass getLimit();

    protected abstract OWLDataRange getDataLimit();

    public Set<OWLClassExpression> visit(OWLDataSomeValuesFrom desc)
    {
        return Collections.singleton((OWLClassExpression)desc);
    }

    public Set<OWLClassExpression> visit(OWLDataAllValuesFrom desc)
    {
        return Collections.singleton((OWLClassExpression)desc);
    }

    public Set<OWLClassExpression> visit(OWLDataHasValue desc)
    {
        Set<OWLClassExpression> result = new HashSet<OWLClassExpression>(2);
        result.add(desc);
        result.add(getDataFactory().getOWLDataSomeValuesFrom((OWLDataPropertyExpression)desc.getProperty(), getDataLimit()));
        return result;
    }

    public Set<OWLClassExpression> visit(OWLDataMinCardinality desc)
    {
        return Collections.singleton((OWLClassExpression)desc);
    }

    public Set<OWLClassExpression> visit(OWLDataExactCardinality desc)
    {
        return Collections.singleton((OWLClassExpression)desc);
    }

    public Set<OWLClassExpression> visit(OWLDataMaxCardinality desc)
    {
        return Collections.singleton((OWLClassExpression)desc);
    }

  

    

}
