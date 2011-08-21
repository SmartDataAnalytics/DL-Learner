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

package org.dllearner.reasoning;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dllearner.core.OntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.w3c.dom.Document;


public class OWLAPIDIGConverter {

	public static String getTellsString(URL file, OntologyFormat format, URI kbURI) {
		
		String ret = "";
		try {

			// Load an ontology from a physical URI
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			// the next function could return an ontology
			manager.loadOntologyFromOntologyDocument(IRI.create(file));
//TODO: OWLAPI3 conversion
//			DIGTranslatorImpl dig = new DIGTranslatorImpl(manager);
//			Document doc = dig.createTellsDocument(kbURI.toString());
//			dig.translateToDIG(manager.getOntologies(), doc, doc.getDocumentElement());
//			ret = xml2string(doc);

		}catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static String xml2string(Document d) {
		// XML als String erzeugen (ziemlich umst�ndlich)
		String tellString = "";
		try {
			// transformer erzeugen mit identity transformation
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			// Quelle ist das DOM-Objekt
			DOMSource source = new DOMSource(d);
			// Resultat ist ein OutputStream, leider kann man das Resultat nicht
			// direkt als String bekommen
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			// Transformation ausf�hren
			transformer.transform(source, result);
			// String aus OutputStream generieren
			tellString = os.toString();

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return tellString;
	}
}
