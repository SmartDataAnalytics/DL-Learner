package org.dllearner.utilities.owl;

import org.dllearner.core.owl.KB;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 6:00:45 PM
 *
 * Purposely not making this into a static or singleton.
 */
public class DefaultOWLUtilities implements IOWLUtilities{

    @Override
   	public void exportKBToOWL(File owlOutputFile, KB kb, IRI ontologyIRI) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
         OWLAPIDescriptionConvertVisitor descriptionConvertVisitor = new OWLAPIDescriptionConvertVisitor();
        descriptionConvertVisitor.setFactory(manager.getOWLDataFactory());

        OWLAPIAxiomConvertVisitor axiomConvertVisitor = new OWLAPIAxiomConvertVisitor();
        axiomConvertVisitor.setOwlAPIDescriptionConvertVisitor(descriptionConvertVisitor);

		//URI ontologyURI = URI.create("http://example.com");
		IRI physicalIRI = IRI.create(owlOutputFile.toURI());
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, physicalIRI);
		manager.addIRIMapper(mapper);
		OWLOntology ontology;
		try {
			ontology = manager.createOntology(ontologyIRI);
			// OWLAPIReasoner.fillOWLAPIOntology(manager, ontology, kb);
			axiomConvertVisitor.fillOWLOntology(manager, ontology, kb);
			manager.saveOntology(ontology);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
