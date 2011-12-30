package org.dllearner.utilities;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;

public interface RootClassFinder {
	Set<OWLClass> getRootUnsatisfiableClasses();
	Set<OWLClass> getDerivedUnsatisfiableClasses();
	Set<OWLClass> getDependentChildClasses(OWLClass unsatClass);

}
