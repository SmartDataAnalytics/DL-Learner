package org.dllearner.kb.sparql;

import com.hp.hpl.jena.rdf.model.Model;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 16, 2010
 * Time: 3:18:15 PM
 *
 *
 * This interface allows us to get a 'fragment' of a model to significantly reduce the learning space of a model.
 *
 * It is the interface to Jens' concept of fragmentation.
 *
 * The interface itself is simple, given a model and a set of instances, create an OWLOntology 'fragment'.  How this is
 * done will be up to the implementations.
 */
public interface IModelFragmenter {

        /**
     * Build a fragment from the model.
     * @param model The model to create the fragment from.
     * @param instanceURIs The uris that will be the base of the fragment.
     * @return
     */
    public OWLOntology buildFragment(Model model, Set<String> instanceURIs);


}
