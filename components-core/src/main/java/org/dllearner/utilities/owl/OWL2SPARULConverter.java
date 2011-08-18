package org.dllearner.utilities.owl;

import java.util.Collection;
import java.util.List;

import org.coode.owlapi.rdf.model.AbstractTranslator;
import org.coode.owlapi.rdf.model.RDFLiteralNode;
import org.coode.owlapi.rdf.model.RDFNode;
import org.coode.owlapi.rdf.model.RDFResourceNode;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

public class OWL2SPARULConverter
		extends
		AbstractTranslator<RDFNode, RDFResourceNode, RDFResourceNode, RDFLiteralNode> {
	private StringBuilder sb;

	public OWL2SPARULConverter(OWLOntologyManager manager,
			OWLOntology ontology, boolean useStrongTyping) {
		super(manager, ontology, useStrongTyping);
	}
	
	public OWL2SPARULConverter(OWLOntology ontology, boolean useStrongTyping) {
		super(ontology.getOWLOntologyManager(), ontology, useStrongTyping);
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

	@Override
	protected void addTriple(RDFResourceNode subject, RDFResourceNode pred,
			RDFNode object) {
		sb.append(subject).append(" ").append(pred).append(" ").append(object);

	}

	@Override
	protected RDFResourceNode getAnonymousNode(Object key) {
		return new RDFResourceNode(System.identityHashCode(key));
	}

	@Override
	protected RDFResourceNode getPredicateNode(IRI iri) {
		return new RDFResourceNode(iri);
	}

	@Override
	protected RDFResourceNode getResourceNode(IRI iri) {
		return new RDFResourceNode(iri);
	}

	@Override
	protected RDFLiteralNode getLiteralNode(OWLLiteral literal) {
		if (literal.getDatatype() != null) {
			return new RDFLiteralNode(literal.toString(), literal.getDatatype()
					.getIRI());
		} else {
			return new RDFLiteralNode(literal.toString(), literal.getLang());
		}

	}
}
