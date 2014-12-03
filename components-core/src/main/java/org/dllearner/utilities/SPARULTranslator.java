package org.dllearner.utilities;

import java.util.List;

import org.semanticweb.owlapi.io.RDFLiteral;
import org.semanticweb.owlapi.io.RDFNode;
import org.semanticweb.owlapi.io.RDFResource;
import org.semanticweb.owlapi.io.RDFResourceBlankNode;
import org.semanticweb.owlapi.io.RDFResourceIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.rdf.model.AbstractTranslator;

public class SPARULTranslator extends AbstractTranslator<RDFNode, RDFResource, RDFResource, RDFLiteral> {

	private StringBuilder sb;
	
	public SPARULTranslator(OWLOntologyManager manager, OWLOntology ontology,
			boolean useStrongTyping) {
		super(manager, ontology, useStrongTyping);
		// TODO Auto-generated constructor stub
	}
	
	public String translate(OWLOntologyChange change){
		sb = new StringBuilder();
		sb.append(change instanceof RemoveAxiom ? "DELETE DATA" : "INSERT DATA");
		sb.append("{");
		change.getAxiom().accept(this);
		sb.append("}");
		sb.append("\n");
		
		return sb.toString();
	}
	
	public String translate(List<OWLOntologyChange> changes){
		sb = new StringBuilder();
		for(OWLOntologyChange change : changes){
			sb.append(change instanceof RemoveAxiom ? "DELETE DATA" : "INSERT DATA");
			sb.append("{");
			change.getAxiom().accept(this);
			sb.append("}");
			sb.append("\n");
		}
		
		return sb.toString();
	}

	@Override
	protected void addTriple(RDFResource subject, RDFResource pred,
			RDFNode object) {
		sb.append(subject).append(" ").append(pred).append(" ").append(object).append("\n");
		
	}

	@Override
	protected RDFResource getAnonymousNode(Object key) {
		return new RDFResourceBlankNode(System.identityHashCode(key));
	}

	@Override
	protected RDFResource getPredicateNode(IRI iri) {
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
