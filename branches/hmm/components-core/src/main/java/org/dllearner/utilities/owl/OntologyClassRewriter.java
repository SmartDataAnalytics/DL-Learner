/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.utilities.owl;

import java.net.MalformedURLException;
import java.net.URL;

import org.dllearner.core.owl.Description;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Utility class to replace a definition in an OWL file by a learned
 * definition.
 * 
 * TODO: Class is currently not working. There is still some KAON2 specific
 * code (commented out), which has to be converted to OWL API code.
 * 
 * @author Jens Lehmann
 *
 */
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

	@SuppressWarnings({"unused"})
	public static String rewriteOntology(String urlString, String className, String newConceptString) {
		
		try {
			// neue Definition in DL-Learner internes Format parsen
			// (Warnung für Web-Service: Parser ist momentan noch statisch, d.h. nicht thread safe)
			Description newConceptInternal = KBParser.parseConcept(newConceptString);
			
			// umwandeln in interne KAON2-Darstellung (bereits im DL-Learner implementiert)
			// Description newConceptKAON2 = KAON2Reasoner.getKAON2Description(newConceptInternal);
			// OWLDescription newConceptOWLAPI = OWLAPIReasoner.getOWLAPIDescription(newConceptInternal);
			OWLClassExpression newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(newConceptInternal);
			
			// Umwandlung Klassenname in atomate KAON2-Klasse
			// OWLClass classKAON2 = KAON2Manager.factory().owlClass(className);
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLClass classOWLAPI = factory.getOWLClass(IRI.create(className));
			
			// Test, ob es eine richtige URL ist (ansonsten wird Exception geworfen)
			new URL(urlString);
			
			// einlesen der Ontologie
			// DefaultOntologyResolver resolver = new DefaultOntologyResolver();
			// KAON2Connection connection = KAON2Manager.newConnection();
			// connection.setOntologyResolver(resolver);
			// Ontology ontology = connection.openOntology(urlString, new HashMap<String,Object>());			
			
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(urlString));
			
			// TODO
			
			// suchen von Äquivalenzaxiomen
//			Request<EquivalentClasses> equivalenceAxiomsRequest = ontology.createAxiomRequest(EquivalentClasses.class);
//			Set<EquivalentClasses> equivalenceAxioms = equivalenceAxiomsRequest.get();
//			
//			for(EquivalentClasses eq : equivalenceAxioms) {
//				Set<Description> eqDescriptions = eq.getDescriptions();
//				if(eqDescriptions.size() != 2)
//					System.out.println("Warning: Rewriting more than two equivalent descriptions not supported yet." +
//							" Possibly incorrect ontology returned.");
//				
//				// entfernen aller Äquivalenzaxiome, die die Klasse enthalten
//				if(eqDescriptions.contains(classKAON2))
//					ontology.removeAxiom(eq);
//			}
//			
//			// hinzufügen des neuen Äquivalenzaxioms
//			EquivalentClasses eqNew = KAON2Manager.factory().equivalentClasses(classKAON2, newConceptKAON2);
//			ontology.addAxiom(eqNew);
//			
//			// umwandeln der Ontologie in einen String
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			ontology.saveOntology(OntologyFileFormat.OWL_RDF,os,"ISO-8859-1");
//			
//			return os.toString();
			return "";
			
		// in einigen der folgenden Fälle sollten im Web-Service Exceptions geworfen
		// werden (throws ...) z.B. bei ParseException
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("New definition could not be parsed (probably a syntax error.");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Syntactically incorrect URL.");
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		throw new Error("Ontology could not be rewritten. Exiting.");
	}
	
}
