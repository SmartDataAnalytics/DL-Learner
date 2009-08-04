
package org.dllearner.tools.ore.explanation.laconic;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;


public class Tau extends BaseDescriptionGenerator
{

    public Tau(OWLDataFactory factory)
    {
        super(factory);
    }

    public Set<OWLDescription> visit(OWLClass desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        descs.add(desc);
        descs.add(getDataFactory().getOWLThing());
        return descs;
    }

    public Set<OWLDescription> visit(OWLObjectComplementOf desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        
        for(OWLDescription d : computeBeta(desc.getOperand())){
        	descs.add(getDataFactory().getOWLObjectComplementOf(d));
        }
        return descs;
    }

    public Set<OWLDescription> visit(OWLObjectMaxCardinalityRestriction desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        
        for(OWLDescription filler : computeBeta(desc.getFiller())){
        	descs.add(getDataFactory().getOWLObjectMaxCardinalityRestriction((OWLObjectPropertyExpression)desc.getProperty(), desc.getCardinality(), filler));
        }
        descs.add(getLimit());
        return descs;
    }

    public Set<OWLDescription> visit(OWLObjectMinCardinalityRestriction desc)
    {
        Set<OWLDescription> weakenedFillers = computeTau((OWLDescription)desc.getFiller());
        Set<OWLDescription> result = new HashSet<OWLDescription>();
        for(int n = desc.getCardinality(); n > 0; n--)
        {
           
            for(OWLDescription filler : weakenedFillers ){
            	result.add(getDataFactory().getOWLObjectMinCardinalityRestriction((OWLObjectPropertyExpression)desc.getProperty(), n, filler));
            }

        }

        result.add(getLimit());
        return result;
    }

    @Override
    protected OWLClass getLimit()
    {
        return getDataFactory().getOWLThing();
    }

    @Override
    protected OWLDataRange getDataLimit()
    {
        return getDataFactory().getTopDataType();
    }

   
}
