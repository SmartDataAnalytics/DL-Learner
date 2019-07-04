package org.dllearner.utilities.owl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLWriter;
import org.semanticweb.owlapi.util.ShortFormProvider;

import javax.annotation.Nonnull;
import java.io.StringWriter;

/**
 * An OWL/XML syntax renderer that implements the interface {@link OWLObjectRenderer}, thus, can be used as
 * syntax in {@link org.dllearner.core.StringRenderer} helper class. Note, a short form is not implemented yet, thus,
 * full IRIs are used and the syntax is quite verbose.
 *
 * @author Lorenz Buehmann
 */
public class OWLXMLRenderer implements OWLObjectRenderer {

    private OWLXMLObjectRenderer delegate;
    private StringWriter sw;

    public OWLXMLRenderer() {
        try {
            OWLOntology ont = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://dl-learner.org/ontology/"));
            sw = new StringWriter();
            OWLXMLWriter oxw = new OWLXMLWriter(sw, ont);
            delegate = new OWLXMLObjectRenderer(oxw);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setShortFormProvider(@Nonnull ShortFormProvider shortFormProvider) {
    }

    @Nonnull
    @Override
    public String render(@Nonnull OWLObject object) {
        sw.getBuffer().setLength(0); // clear the StringWriter
        object.accept(delegate);
        return sw.toString();
    }
}