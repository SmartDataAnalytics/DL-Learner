
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

public class BottomTester implements OWLDescriptionVisitorEx<Boolean>
{

	@Override
	public Boolean visit(OWLClass owlClass) {
		
		return Boolean.valueOf(owlClass.isOWLNothing());
	}

	@Override
	public Boolean visit(OWLObjectIntersectionOf intersect) {
		for(OWLDescription desc : intersect.getOperands()){
			if (((Boolean) desc.accept(this)).booleanValue()) {
				return Boolean.valueOf(true);
			}
		}
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectUnionOf union) {
		for(OWLDescription desc : union.getOperands()){
			if (((Boolean) desc.accept(this)).booleanValue()) {
				return Boolean.valueOf(true);
			}
		}
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectComplementOf desc) {
		return Boolean.valueOf(desc.isOWLThing());
	}

	@Override
	public Boolean visit(OWLObjectSomeRestriction desc) {
		return (Boolean) ((OWLDescription) desc.getFiller()).accept(this);
	}

	@Override
	public Boolean visit(OWLObjectAllRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectValueRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectMinCardinalityRestriction desc) {
		return (Boolean) ((OWLDescription) desc.getFiller()).accept(this);
	}

	@Override
	public Boolean visit(OWLObjectExactCardinalityRestriction desc) {
		return (Boolean) ((OWLDescription) desc.getFiller()).accept(this);
	}

	@Override
	public Boolean visit(OWLObjectMaxCardinalityRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectSelfRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLObjectOneOf arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataSomeRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataAllRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataValueRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataMinCardinalityRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataExactCardinalityRestriction arg0) {
		return Boolean.valueOf(false);
	}

	@Override
	public Boolean visit(OWLDataMaxCardinalityRestriction arg0) {
		return Boolean.valueOf(false);
	}
   
}