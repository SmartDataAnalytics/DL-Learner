package org.dllearner.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import org.dllearner.core.dl.Concept;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.KAON2Reasoner;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.Request;
import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
import org.semanticweb.kaon2.api.owl.axioms.EquivalentClasses;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;

public class OntologyClassRewriter {

	public static void main(String[] args) {
		String rewrittenOntology =
		rewriteOntology(
				// Ontologie
				"http://localhost/jl/dllearnerws/v2/ontologies/father.owl",
				// Klasse, die umgeschrieben wird
				"http://example.com/father#male",
				// neue Definition in DL-Learner-Syntax(die hier ergibt keinen Sinn)
				"((\"http://example.com/father#male\" AND EXISTS \"http://example.com/father#hasChild\".TOP)" +
				"OR ALL \"http://example.com/father#hasChild\".\"http://example.com/father#female\")");
		System.out.println(rewrittenOntology);
	}

	public static String rewriteOntology(String urlString, String className, String newConceptString) {
		
		try {
			// neue Definition in DL-Learner internes Format parsen
			// (Warnung für Web-Service: Parser ist momentan noch statisch, d.h. nicht thread safe)
			Concept newConceptInternal = KBParser.parseConcept(newConceptString);
			
			// umwandeln in interne KAON2-Darstellung (bereits im DL-Learner implementiert)
			Description newConceptKAON2 = KAON2Reasoner.getKAON2Description(newConceptInternal);
			
			// Umwandlung Klassenname in atomate KAON2-Klasse
			OWLClass classKAON2 = KAON2Manager.factory().owlClass(className);
			
			// Test, ob es eine richtige URL ist (ansonsten wird Exception geworfen)
			new URL(urlString);
			
			// einlesen der Ontologie
			DefaultOntologyResolver resolver = new DefaultOntologyResolver();
			KAON2Connection connection = KAON2Manager.newConnection();
			connection.setOntologyResolver(resolver);
			Ontology ontology = connection.openOntology(urlString, new HashMap<String,Object>());			
			
			// suchen von Äquivalenzaxiomen
			Request<EquivalentClasses> equivalenceAxiomsRequest = ontology.createAxiomRequest(EquivalentClasses.class);
			Set<EquivalentClasses> equivalenceAxioms = equivalenceAxiomsRequest.get();
			
			for(EquivalentClasses eq : equivalenceAxioms) {
				Set<Description> eqDescriptions = eq.getDescriptions();
				if(eqDescriptions.size() != 2)
					System.out.println("Warning: Rewriting more than two equivalent descriptions not supported yet." +
							" Possibly incorrect ontology returned.");
				
				// entfernen aller Äquivalenzaxiome, die die Klasse enthalten
				if(eqDescriptions.contains(classKAON2))
					ontology.removeAxiom(eq);
			}
			
			// hinzufügen des neuen Äquivalenzaxioms
			EquivalentClasses eqNew = KAON2Manager.factory().equivalentClasses(classKAON2, newConceptKAON2);
			ontology.addAxiom(eqNew);
			
			// umwandeln der Ontologie in einen String
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ontology.saveOntology(OntologyFileFormat.OWL_RDF,os,"ISO-8859-1");
			return os.toString();
			
		// in einigen der folgenden Fälle sollten im Web-Service Exceptions geworfen
		// werden (throws ...) z.B. bei ParseException
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("New definition could not be parsed (probably a syntax error.");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Syntactically incorrect URL.");
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		throw new Error("Ontology could not be rewritten. Exiting.");
	}
	
}
