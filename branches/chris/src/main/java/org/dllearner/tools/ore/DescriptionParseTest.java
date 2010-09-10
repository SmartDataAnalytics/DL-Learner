package org.dllearner.tools.ore;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxClassFrameParser;
import org.coode.owlapi.manchesterowlsyntax.OntologyAxiomPair;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import com.clarkparsia.owlapi.explanation.io.manchester.BlockWriter;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxObjectRenderer;
import com.clarkparsia.owlapi.explanation.io.manchester.TextBlockWriter;

public class DescriptionParseTest {
	
	public static void main(String[] args) throws OWLOntologyChangeException, OWLOntologyCreationException, ParserException {
		
		System.out.println(System.getProperty("os.name"));
		System.out.println(System.getProperty("os.version"));
		System.out.println(System.getProperty("os.arch"));
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		OWLClass class1 = factory.getOWLClass(IRI.create("Class1"));
		OWLClass class2 = factory.getOWLClass(IRI.create("Class2"));
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(class1, class2);
		OWLOntology ontology = manager.createOntology(Collections.singleton(axiom));
		
		//rendering
		StringWriter buffer = new StringWriter();
		BlockWriter writer = new TextBlockWriter(buffer);
		ManchesterSyntaxObjectRenderer renderer = new ManchesterSyntaxObjectRenderer(writer);
		axiom.accept(renderer);
		String manSyntaxString = buffer.toString();
		System.out.println(manSyntaxString);

		//parsing
		ManchesterOWLSyntaxClassFrameParser parser = new ManchesterOWLSyntaxClassFrameParser(
				manager.getOWLDataFactory(), new ShortFormEntityChecker(
						new BidirectionalShortFormProviderAdapter(manager, Collections
								.singleton(ontology), new SimpleShortFormProvider())));
		Set<OntologyAxiomPair> axioms = parser.parse(manSyntaxString);

		System.out.println(axioms);
	}
}
