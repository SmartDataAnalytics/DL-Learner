/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.utilities.owl;

import com.google.common.collect.Maps;
import org.apache.jena.vocabulary.OWL;
import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public class SimpleOWLEntityChecker implements OWLEntityChecker{
	
	private static OWLDataFactory df = new OWLDataFactoryImpl();
	private AbstractReasonerComponent rc;
	
	IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
	
	private boolean allowShortForm = false;

	static Map<String, OWLDatatype> datatypeNames = Maps.newHashMap();
	static {
		datatypeNames.put("double", df.getDoubleOWLDatatype());
		datatypeNames.put("int", df.getIntegerOWLDatatype());
		datatypeNames.put("integer", df.getIntegerOWLDatatype());
		datatypeNames.put("date", df.getOWLDatatype(XSDVocabulary.DATE.getIRI()));
		for (OWL2Datatype o2d : OWL2Datatype.values()) {
			if (!o2d.getShortForm().toUpperCase().equals(o2d.getShortForm()))
				datatypeNames.put(o2d.getShortForm(), o2d.getDatatype(df));
		}
	}

	public SimpleOWLEntityChecker(AbstractReasonerComponent rc) {
		this.rc = rc;
	}

	private <T extends HasIRI> T find(String name, Collection<? extends T> c) {
		for (T x : c) {
			if(allowShortForm && sfp.getShortForm(x.getIRI()).equals(name) ||
					x.getIRI().toString().equals(name) ||
					x.getIRI().toQuotedString().equals(name)) {
				return x;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLClass(java.lang.String)
	 */
	@Override
	public OWLClass getOWLClass(String name) {
		if ("owl:Thing".equals(name) || IRI.create(OWL.NS + "Thing").toQuotedString().equals(name)) {
			return df.getOWLThing();
		}
		OWLClass cls = find(name, rc.getClasses());
		if (cls != null) {
			return cls;
		}
		if (allowShortForm && "Thing".equals(name)) {
			return df.getOWLThing();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLObjectProperty(java.lang.String)
	 */
	@Override
	public OWLObjectProperty getOWLObjectProperty(String name) {
		return find(name, rc.getObjectProperties());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLDataProperty(java.lang.String)
	 */
	@Override
	public OWLDataProperty getOWLDataProperty(String name) {
		return find(name, rc.getDatatypeProperties());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLIndividual(java.lang.String)
	 */
	@Override
	public OWLNamedIndividual getOWLIndividual(String name) {
		return find(name, (Set<OWLNamedIndividual>)(Set<?>)rc.getIndividuals());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLDatatype(java.lang.String)
	 */
	@Override
	public OWLDatatype getOWLDatatype(String name) {
		return datatypeNames.get(name);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.expression.OWLEntityChecker#getOWLAnnotationProperty(java.lang.String)
	 */
	@Override
	public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
		return null;
	}
	
	/**
	 * @param allowShortForm the allowShortForm to set
	 */
	public void setAllowShortForm(boolean allowShortForm) {
		this.allowShortForm = allowShortForm;
	}

}
