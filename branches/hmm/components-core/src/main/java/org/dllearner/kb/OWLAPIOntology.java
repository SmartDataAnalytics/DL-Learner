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

package org.dllearner.kb;

import java.io.File;
import java.net.URI;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.owl.KB;
import org.dllearner.utilities.owl.OntologyToByteConverter;
import org.dllearner.utilities.owl.SimpleOntologyToByteConverter;
import org.semanticweb.owlapi.model.*;

/**
 * This class provides a wrapper around a single OWL Ontology.  However, due to threading issues it is not safe
 * to allow access to ontologies created with an Ontology Manager which we do not control.
 */
public class OWLAPIOntology extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource{
	
    private byte[] ontologyBytes;
    private OntologyToByteConverter converter = new SimpleOntologyToByteConverter();

	
	public OWLAPIOntology(OWLOntology onto) {
        ontologyBytes = converter.convert(onto);
    }
	
	public static String getName() {
		return "OWL API Ontology";
	}

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {
        return converter.convert(ontologyBytes, manager);
    }

    @Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException 
	{
		
	}
	
	@Override
	public KB toKB()
	{
		throw new Error("OWL -> KB conversion not implemented yet.");
	}
	
	@Override
	public void init()
	{
		
	}
	
	@Override
	public void export(File file, OntologyFormat format)
	{
		
	}
	
	@Override
	public String toDIG(URI kbURI)
	{
		return null;
	}

    /**
     * Get the OntologyToByteConverter associated with this object.
     *
     * @return The OntologyToByteConverter associated with this object.
     */
    public OntologyToByteConverter getConverter() {
        return converter;
    }

    /**
     * Set the OntologyToByteConverter associated with this object.
     *
     * @param converter the OntologyToByteConverter to associate with this object.
     */
    public void setConverter(OntologyToByteConverter converter) {
        this.converter = converter;
    }
}
