package org.dllearner.cli;

import java.io.File;
import java.io.IOException;

import org.dllearner.core.AbstractReasonerComponent;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.springframework.context.ApplicationContext;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

public class NLP2RDFCLITest {

	@Test
	public void sampleTest() throws IOException {
		File f = new File("../test/nlp2rdf/sample/sample1.conf");
//		File f = new File("../examples/nlp2rdf/learning_initial_6/dbpedia_spotlight_plus/copper17_vs_gold35.conf");
		CLI cli = new CLI(f);
		cli.init();
		cli.run();
		ApplicationContext context = cli.getContext();
		AbstractReasonerComponent rc = context.getBean(AbstractReasonerComponent.class);
		
		OWLIndividual i = new OWLNamedIndividualImpl(
				IRI.create("http://test.de/TOPICS/copper/COPPER+MEETING+AGREES+GOALS+OF+STUDY+GROUP#offset_592_596_U.S."));
		System.out.println("all information about " + i + ": ");
		System.out.println(rc.getObjectPropertyRelationships(i));
		
		OWLObjectProperty op = new OWLObjectPropertyImpl(IRI.create("http://ns.aksw.org/scms/means"));
		System.out.println("all relationships with scms:means:");
		System.out.println(rc.getPropertyMembers(op));
		
		System.out.println("all object properties:");
		System.out.println(rc.getObjectProperties());
	}
}
