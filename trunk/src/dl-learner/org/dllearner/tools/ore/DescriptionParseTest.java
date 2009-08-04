package org.dllearner.tools.ore;

import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxClassFrameParser;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.expression.ParserException;
import org.semanticweb.owl.expression.ShortFormEntityChecker;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owl.util.SimpleShortFormProvider;

import com.clarkparsia.explanation.io.manchester.BlockWriter;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxObjectRenderer;
import com.clarkparsia.explanation.io.manchester.TextBlockWriter;

public class DescriptionParseTest {
	
	public static void main(String[] args) throws OWLOntologyChangeException, OWLOntologyCreationException, ParserException {
		
		System.out.println(System.getProperty("os.name"));
		System.out.println(System.getProperty("os.version"));
		System.out.println(System.getProperty("os.arch"));
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		OWLClass class1 = factory.getOWLClass(URI.create("Class1"));
		OWLClass class2 = factory.getOWLClass(URI.create("Class2"));
		OWLAxiom axiom = factory.getOWLSubClassAxiom(class1, class2);
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
		Set<OWLAxiom> axioms = parser.parse(manSyntaxString);

		System.out.println(axioms);
	}
}
