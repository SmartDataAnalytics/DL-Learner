

package org.dllearner.tools.ore.explanation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectUnionOf;



public class BetaGenerator extends BaseDescriptionGenerator
{

    public BetaGenerator(OWLDataFactory factory)
    {
        super(factory);
    }

    public Set<OWLDescription> visit(OWLClass desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>(3);
        descs.add(desc);
        descs.add(getDataFactory().getOWLNothing());
        return descs;
    }

    public Set<OWLDescription> visit(OWLObjectComplementOf desc)
    {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        
        for(OWLDescription d : computeTau(desc.getOperand())){
        	descs.add(getDataFactory().getOWLObjectComplementOf(d));
        }
           
        return descs;
    }

    protected Set<OWLDescription> compute(OWLDescription description)
    {
        return computeBeta(description);
    }

    public Set<OWLDescription> visit(OWLObjectMaxCardinalityRestriction desc)
    {
        Set<OWLDescription> fillers = computeTau(desc.getFiller());
        Set<OWLDescription> result = new HashSet<OWLDescription>();
        for(int n = desc.getCardinality(); n > 0; n--)
        {
            for(OWLDescription filler : fillers){
                result.add(getDataFactory().getOWLObjectMinCardinalityRestriction((OWLObjectPropertyExpression)desc.getProperty(), n, filler));
            }
        }

        result.add(getLimit());
        return result;
    }

    public Set<OWLDescription> visit(OWLObjectExactCardinalityRestriction desc)
    {
        Set<OWLDescription> fillers = computeBeta((OWLDescription)desc.getFiller());
        Set<OWLDescription> result = new HashSet<OWLDescription>();
        
        for(OWLDescription filler : fillers){
            result.add(getDataFactory().getOWLObjectExactCardinalityRestriction((OWLObjectPropertyExpression)desc.getProperty(), desc.getCardinality(), filler));
        }
        result.add(getLimit());
        return result;
    }

    public Set<OWLDescription> visit(OWLObjectUnionOf desc)
    {
        return super.visit(desc);
    }

    public Set<OWLDescription> visit(OWLObjectMinCardinalityRestriction desc)
    {
        Set<OWLDescription> fillers = computeBeta((OWLDescription)desc.getFiller());
        Set<OWLDescription> result = new HashSet<OWLDescription>();
        
        for(OWLDescription filler : fillers){
        	result.add(getDataFactory().getOWLObjectMinCardinalityRestriction((OWLObjectPropertyExpression)desc.getProperty(), desc.getCardinality(), filler));
        }
        result.add(getLimit());
        return result;
    }

    protected OWLClass getLimit()
    {
        return getDataFactory().getOWLNothing();
    }

    protected OWLDataRange getDataLimit()
    {
        return getDataFactory().getOWLDataComplementOf(getDataFactory().getTopDataType());
    }

    public Set<OWLDescription> visit(OWLDataValueRestriction desc)
    {
        return Collections.singleton((OWLDescription)desc);
    }

    
}
