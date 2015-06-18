/**
 * 
 */
package org.dllearner.utilities.owl;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class SimpleOWLEntityChecker implements OWLEntityChecker{
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private AbstractReasonerComponent rc;
	
	IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();

	public SimpleOWLEntityChecker(AbstractReasonerComponent rc) {
		this.rc = rc;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLClass(java.lang.String)
	 */
	@Override
	public OWLClass getOWLClass(String name) {
//		OWLClass cls = df.getOWLClass(IRI.create(name));
//		if(ontology.getClassesInSignature(true).contains(cls)) {
//			return cls;
//		}
		for (OWLClass cls : rc.getClasses()) {
			if(sfp.getShortForm(cls.getIRI()).equals(name) || cls.getIRI().toQuotedString().equals(name)) {
				return cls;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLObjectProperty(java.lang.String)
	 */
	@Override
	public OWLObjectProperty getOWLObjectProperty(String name) {
		OWLObjectProperty p = df.getOWLObjectProperty(IRI.create(name));
		if(rc.getObjectProperties().contains(p)) {
			return p;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLDataProperty(java.lang.String)
	 */
	@Override
	public OWLDataProperty getOWLDataProperty(String name) {
		OWLDataProperty p = df.getOWLDataProperty(IRI.create(name));
		
		if(rc.getDatatypeProperties().contains(p)) {
			return p;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLIndividual(java.lang.String)
	 */
	@Override
	public OWLNamedIndividual getOWLIndividual(String name) {
		OWLNamedIndividual ind = df.getOWLNamedIndividual(IRI.create(name));
		
		if(rc.getIndividuals().contains(ind)) {
			return ind;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLDatatype(java.lang.String)
	 */
	@Override
	public OWLDatatype getOWLDatatype(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLAnnotationProperty(java.lang.String)
	 */
	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
		return null;
	}

}
