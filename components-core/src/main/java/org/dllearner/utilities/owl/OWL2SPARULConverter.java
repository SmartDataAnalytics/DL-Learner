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

import com.google.common.collect.ComparisonChain;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.rdf.model.AbstractTranslator;
import org.semanticweb.owlapi.util.AlwaysOutputId;
import org.semanticweb.owlapi.util.AxiomAppearance;
import org.semanticweb.owlapi.util.IndividualAppearance;
import org.semanticweb.owlapi.util.OWLAnonymousIndividualsWithMultipleOccurrences;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

/**
 * A converter of OWL axioms or OWL ontology changes into SPARQL 1.1 Update commands.
 *
 * @author Lorenz Buehmann
 */
public class OWL2SPARULConverter
		extends
		AbstractTranslator<RDFNode, RDFResource, RDFResourceIRI, RDFLiteral> {
	private StringBuilder sb;

	public OWL2SPARULConverter(OWLOntologyManager manager,
							   OWLOntology ontology, boolean useStrongTyping, IndividualAppearance individualAppearance,
							   AxiomAppearance axiomAppearance, AtomicInteger nextNode, Map<Object, Integer> blankNodeMap) {
		super(manager, ontology, useStrongTyping, individualAppearance, axiomAppearance, nextNode, blankNodeMap);
	}

	public OWL2SPARULConverter(OWLOntology ontology, boolean useStrongTyping) {
		this(ontology.getOWLOntologyManager(), ontology, useStrongTyping,
			 new OWLAnonymousIndividualsWithMultipleOccurrences(), new AlwaysOutputId(), new AtomicInteger(), Collections.emptyMap());
	}

	/**
	 * Converts an OWL axioms to a SPARQL 1.1 Update command.
	 *
	 * @param axiom the OWL axiom
	 * @param add    whether the axiom has to be added('ADD') or removed('DELETE')
	 * @return the SPARQL 1.1 Update command
	 */
	public String convert(OWLAxiom axiom, boolean add) {
		return convert(Collections.singleton(axiom), add);
	}

	/**
	 * Converts a set of OWL axioms to a SPARQL 1.1 Update command.
	 *
	 * @param axioms the OWL axioms
	 * @param add    whether those axioms have to be added('ADD') or removed('DELETE')
	 * @return the SPARQL 1.1 Update command
	 */
	public String convert(Collection<OWLAxiom> axioms, boolean add) {
		sb = new StringBuilder();
		for (OWLAxiom ax : axioms) {
			sb.append(add ? "INSERT DATA" : "DELETE DATA");
			sb.append("{");
			ax.accept(this);
			sb.append("}");
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Converts an OWL ontology change to a SPARQL 1.1 Update command.
	 *
	 * @param change the OWL ontology change
	 * @return the SPARQL 1.1 Update command
	 */
	public String convert(OWLOntologyChange change) {
		return convert(Collections.singletonList(change));
	}

	/**
	 * Converts a list of OWL ontology changes to a SPARQL 1.1 Update command.
	 *
	 * @param changes the ontology changes
	 * @return the SPARQL 1.1 Update command
	 */
	public String convert(List<OWLOntologyChange> changes) {
		sb = new StringBuilder();

		// sort by type of change
		Collections.sort(changes, (o1, o2) -> ComparisonChain.start()
				.compareTrueFirst(o1.isAddAxiom(), o2.isAddAxiom())
				.compare(o1.getAxiom(), o2.getAxiom())
				.result());

		// convert to SPARQL 1.1 Update
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

	@Override
	protected void addTriple(@Nonnull RDFResource subject, @Nonnull RDFResourceIRI pred,
							 @Nonnull RDFNode object) {
		sb.append(subject).append(" ").append(pred).append(" ").append(object);

	}

	@Nonnull
	@Override
	protected RDFResourceBlankNode getAnonymousNode(@Nonnull Object key) {
		return new RDFResourceBlankNode(System.identityHashCode(key), false,false, false);
	}

	@Nonnull
	@Override
	protected RDFResource getAnonymousNodeForExpressions(@Nonnull Object key, boolean isAxiom) {
		checkNotNull(key, "key cannot be null");
		return new RDFResourceBlankNode(false, false, isAxiom);
	}

	@Nonnull
	@Override
	protected RDFResourceIRI getPredicateNode(@Nonnull IRI iri) {
		return new RDFResourceIRI(iri);
	}

	@Nonnull
	@Override
	protected RDFResource getResourceNode(@Nonnull IRI iri) {
		return new RDFResourceIRI(iri);
	}

	@Nonnull
	@Override
	protected RDFLiteral getLiteralNode(@Nonnull OWLLiteral literal) {
		return new RDFLiteral(literal);
	}
}