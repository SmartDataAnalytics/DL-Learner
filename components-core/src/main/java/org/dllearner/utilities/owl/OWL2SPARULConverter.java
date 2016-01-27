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

import com.google.common.collect.Lists;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.rdf.model.AbstractTranslator;
import org.semanticweb.owlapi.util.IndividualAppearance;
import org.semanticweb.owlapi.util.OWLAnonymousIndividualsWithMultipleOccurrences;

import java.util.Collection;
import java.util.List;

public class OWL2SPARULConverter
		extends
		AbstractTranslator<RDFNode, RDFResource, RDFResourceIRI, RDFLiteral> {
	private StringBuilder sb;

	public OWL2SPARULConverter(OWLOntologyManager manager,
			OWLOntology ontology, boolean useStrongTyping, IndividualAppearance individualAppearance) {
		super(manager, ontology, useStrongTyping, individualAppearance);
	}
	
	public OWL2SPARULConverter(OWLOntology ontology, boolean useStrongTyping) {
		this(ontology.getOWLOntologyManager(), ontology, useStrongTyping, new OWLAnonymousIndividualsWithMultipleOccurrences());
	}
	
	public String convert(OWLOntology ontology) {
		return convert(ontology, true);
	}
	
	public String convert(OWLOntology ontology, boolean add) {
		return convert(ontology.getAxioms());
	}
	
	public String convert(Collection<OWLAxiom> axioms) {
		return convert(axioms, true);
	}
	
	public String convert(Collection<OWLAxiom> axioms, boolean add) {
		sb = new StringBuilder();
		for (OWLAxiom ax : axioms) {
			sb.append(add ? "INSERT DATA"
					: "DELETE DATA");
			sb.append("{");
			ax.accept(this);
			sb.append("}");
			sb.append("\n");
		}
		return sb.toString();
	}

	public String translate(List<OWLOntologyChange> changes) {
		sb = new StringBuilder();
		for (OWLOntologyChange change : changes) {
			sb.append(change instanceof RemoveAxiom ? "DELETE DATA"
					: "INSERT DATA");
			sb.append("{");
			change.getAxiom().accept(this);
			sb.append("}");
			sb.append("\n");
		}
		return sb.toString();
	}

	public String translate(OWLOntologyChange change) {
		return translate(Lists.newArrayList(change));
	}

	@Override
	protected void addTriple(RDFResource subject, RDFResourceIRI pred,
			RDFNode object) {
		sb.append(subject).append(" ").append(pred).append(" ").append(object);

	}

	@Override
	protected RDFResourceBlankNode getAnonymousNode(Object key) {
		return new RDFResourceBlankNode(System.identityHashCode(key), false, false);
	}

	@Override
	protected RDFResourceIRI getPredicateNode(IRI iri) {
		return new RDFResourceIRI(iri);
	}

	@Override
	protected RDFResource getResourceNode(IRI iri) {
		return new RDFResourceIRI(iri);
	}

	@Override
	protected RDFLiteral getLiteralNode(OWLLiteral literal) {
		return new RDFLiteral(literal);
	}
}