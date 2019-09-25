package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * @author Lorenz Buehmann
 */
public class PatternConstants {

	public static final String NS = "http://dl-learner.org/pattern/";

	public static final OWLDatatype USER_DEFINED_DATATYPE = new OWLDatatypeImpl(IRI.create(NS + "used_defined_datatype"));
	public static final OWLDatatype BUILT_IN_DATATYPE = new OWLDatatypeImpl(IRI.create(NS + "built_in_datatype"));
	public static final OWLIndividual INDIVIDUAL_SET = new OWLNamedIndividualImpl(IRI.create(NS + "individual_set"));
	public static final OWLClass CLASS_SET = new OWLClassImpl(IRI.create(NS + "class_set"));
}
