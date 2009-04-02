

package org.dllearner.tools.ore.explanation;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitorEx;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;



public class TopTester
    implements OWLDescriptionVisitorEx<Boolean>
{

    public TopTester()
    {
        bottomChecker = new BottomTester();
    }

    public Boolean visit(OWLClass desc)
    {
        return Boolean.valueOf(desc.isOWLThing());
    }

    public Boolean visit(OWLObjectIntersectionOf desc)
    {
        for(OWLDescription op : desc.getOperands()){
           if(!((Boolean)op.accept(this)).booleanValue()){
                return Boolean.valueOf(false);
           }
        }

        return Boolean.valueOf(true);
    }

    public Boolean visit(OWLObjectUnionOf desc)
    {
        for(OWLDescription op : desc.getOperands()){
           if(((Boolean)op.accept(this)).booleanValue()){
                return Boolean.valueOf(true);
           }
        }

        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLObjectComplementOf desc)
    {
        return (Boolean)desc.getOperand().accept(bottomChecker);
    }

    public Boolean visit(OWLObjectSomeRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLObjectAllRestriction desc)
    {
        return (Boolean)((OWLDescription)desc.getFiller()).accept(this);
    }

    public Boolean visit(OWLObjectValueRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLObjectMinCardinalityRestriction desc)
    {
        return Boolean.valueOf(desc.getCardinality() == 0);
    }

    public Boolean visit(OWLObjectExactCardinalityRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLObjectMaxCardinalityRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLObjectSelfRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLObjectOneOf desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLDataSomeRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLDataAllRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLDataValueRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLDataMinCardinalityRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLDataExactCardinalityRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    public Boolean visit(OWLDataMaxCardinalityRestriction desc)
    {
        return Boolean.valueOf(false);
    }

    

    private BottomTester bottomChecker;
}
