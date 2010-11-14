package org.dllearner.utilities.owl;

import org.dllearner.core.owl.KB;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 6:03:55 PM
 * <p/>
 * Interface to OWL Utility methods.  No state should be associated with any implementations.
 */
public interface IOWLUtilities {


    /**
     * Utility method used to export a KB to an OWL file.
     *
     * @param owlOutputFile The file to output to.
     * @param kb The kb to write to the file.
     * @param ontologyIRI The IRI of the ontology.
     */
    public void exportKBToOWL(File owlOutputFile, KB kb, IRI ontologyIRI);

}
