
package org.dllearner.tools.ore.explanation.laconic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitorEx;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;



public abstract class BaseDescriptionGenerator
    implements OWLDescriptionVisitorEx<Set<OWLDescription>>
{
	private OWLDataFactory factory;
    private static TopTester topChecker = new TopTester();
    private static BottomTester bottomChecker = new BottomTester();

    public BaseDescriptionGenerator(OWLDataFactory factory)
    {
        this.factory = factory;
    }

    public boolean isThing(OWLDescription description)
    {
        return ((Boolean)description.accept(topChecker)).booleanValue(); 
    }

    public boolean isNothing(OWLDescription description)
    {
        return ((Boolean)description.accept(bottomChecker)).booleanValue();
    }

    public OWLDataFactory getDataFactory()
    {
        return factory;
    }

    public Set<OWLDescription> computeTau(OWLDescription desc)
    {
        Tau gen = new Tau(factory);
        return desc.accept(gen);
    }

    public Set<OWLDescription> computeBeta(OWLDescription desc)
    {
        Beta gen = new Beta(factory);
        return (Set<OWLDescription>)desc.accept(gen);
    }

    private Set<Set<OWLDescription>> computeReplacements(Set<OWLDescription> operands)
    {
        Set<List<OWLDescription>> ps = new HashSet<List<OWLDescription>>();
        ps.add(new ArrayList<OWLDescription>());

        for(OWLDescription op : operands)
        {
            Set<List<OWLDescription>> pscopy = new HashSet<List<OWLDescription>>(ps);
            
            for(OWLDescription d : (Set<OWLDescription>)op.accept(this)) {
                for(List<OWLDescription> pselement : pscopy) {
                    ArrayList<OWLDescription> union = new ArrayList<OWLDescription>();
                    union.addAll(pselement);
                    union.add(d);
                    ps.remove(pselement);
                    ps.add(union);
                }
            }
        }

        Set<Set<OWLDescription>> result = new HashSet<Set<OWLDescription>>();
        
        for(List<OWLDescription> desc : ps ){
        	result.add(new HashSet<OWLDescription>(desc));
        }
        return result;
    }

    public Set<OWLDescription> visit(OWLObjectIntersectionOf desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        Set<Set<OWLDescription>> conjunctions = computeReplacements(desc.getOperands());
        for(Set<OWLDescription> conjuncts : conjunctions){
        
        	for(OWLDescription conjunct : conjuncts){
        		if(isThing(conjunct)){
                    conjuncts.remove(conjunct);
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

    public Set<OWLDescription> visit(OWLObjectUnionOf desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        Set<Set<OWLDescription>> disjunctions = computeReplacements(desc.getOperands());
        for(Set<OWLDescription> disjuncts : disjunctions){
        
            for(OWLDescription disjunct : disjuncts){
                if(isNothing(disjunct)){
                    disjuncts.remove(disjunct);
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

    public Set<OWLDescription> visit(OWLObjectSomeRestriction desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        descs.add(desc);
        for(OWLDescription filler : desc.getFiller().accept(this)){
            if(!isNothing(filler))
                descs.add(factory.getOWLObjectSomeRestriction((OWLObjectPropertyExpression)desc.getProperty(), filler));
        }
        descs.add(getLimit());
        return descs;
    }

    public Set<OWLDescription> visit(OWLObjectAllRestriction desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
       
        for(OWLDescription filler : desc.getFiller().accept(this)){
        	if(!isThing(filler))
                descs.add(factory.getOWLObjectAllRestriction((OWLObjectPropertyExpression)desc.getProperty(), filler));
        }
        descs.add(getLimit());
        return descs;
    }

    public Set<OWLDescription> visit(OWLObjectValueRestriction desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        descs.add(desc);
        
        for(OWLDescription filler : factory.getOWLObjectOneOf(new OWLIndividual[] {(OWLIndividual)desc.getValue()}).accept(this)){ 
        	descs.add(factory.getOWLObjectSomeRestriction((OWLObjectPropertyExpression)desc.getProperty(), filler));
        }
            

        descs.add(getLimit());
        return descs;
    }

    public Set<OWLDescription> visit(OWLObjectExactCardinalityRestriction desc)
    {
        Set<OWLDescription> result = new HashSet<OWLDescription>();
        OWLDescription min = getDataFactory().getOWLObjectMinCardinalityRestriction((OWLObjectPropertyExpression)desc.getProperty(), desc.getCardinality(), (OWLDescription)desc.getFiller());
        result.addAll(min.accept(this));
        OWLDescription max = getDataFactory().getOWLObjectMaxCardinalityRestriction((OWLObjectPropertyExpression)desc.getProperty(), desc.getCardinality(), (OWLDescription)desc.getFiller());
        result.addAll(max.accept(this));
        result.add(getLimit());
        return result;
    }

    public Set<OWLDescription> visit(OWLObjectSelfRestriction desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        descs.add(desc);
        descs.add(getLimit());
        return descs;
    }

    public Set<OWLDescription> visit(OWLObjectOneOf desc)
    {
        Set<OWLDescription> ops = new HashSet<OWLDescription>();
        if(desc.getIndividuals().size() == 1)
        {
            ops.add(desc);
            ops.add(getLimit());
            return ops;
        }
        
        for(OWLIndividual ind :  desc.getIndividuals()){
        	ops.add(factory.getOWLObjectOneOf(new OWLIndividual[] {ind}));
        }

        OWLDescription rewrite = factory.getOWLObjectUnionOf(ops);
        return rewrite.accept(this);
    }

    protected abstract OWLClass getLimit();

    protected abstract OWLDataRange getDataLimit();

    public Set<OWLDescription> visit(OWLDataSomeRestriction desc)
    {
        return Collections.singleton((OWLDescription)desc);
    }

    public Set<OWLDescription> visit(OWLDataAllRestriction desc)
    {
        return Collections.singleton((OWLDescription)desc);
    }

    public Set<OWLDescription> visit(OWLDataValueRestriction desc)
    {
        Set<OWLDescription> result = new HashSet<OWLDescription>(2);
        result.add(desc);
        result.add(getDataFactory().getOWLDataSomeRestriction((OWLDataPropertyExpression)desc.getProperty(), getDataLimit()));
        return result;
    }

    public Set<OWLDescription> visit(OWLDataMinCardinalityRestriction desc)
    {
        return Collections.singleton((OWLDescription)desc);
    }

    public Set<OWLDescription> visit(OWLDataExactCardinalityRestriction desc)
    {
        return Collections.singleton((OWLDescription)desc);
    }

    public Set<OWLDescription> visit(OWLDataMaxCardinalityRestriction desc)
    {
        return Collections.singleton((OWLDescription)desc);
    }

  

    

}
