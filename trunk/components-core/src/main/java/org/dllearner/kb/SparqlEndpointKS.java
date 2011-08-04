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
 *
 */
package org.dllearner.kb;

import java.io.File;
import java.net.URI;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.configurators.SparqlEndpointKSConfigurator;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.sparql.SparqlEndpoint;

/**
 * SPARQL endpoint knowledge source (without fragment extraction),
 * in particular for those algorithms which work directly on an endpoint
 * without requiring an OWL reasoner.
 * 
 * @author Jens Lehmann
 *
 */
public class SparqlEndpointKS extends AbstractKnowledgeSource {

	private SparqlEndpoint endpoint;
	
	private SparqlEndpointKSConfigurator configurator ;
	
	@Override
	public SparqlEndpointKSConfigurator getConfigurator(){
		return configurator;
	}	
	
	public SparqlEndpointKS(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	@Override
	public KB toKB() {
		return null;
	}

	@Override
	public String toDIG(URI kbURI) {
		return null;
	}

	@Override
	public void export(File file, OntologyFormat format)
			throws OntologyFormatUnsupportedException {
	}

	@Override
	public void init() throws ComponentInitException {
	}
	
	public SparqlEndpoint getEndpoint() {
		return endpoint;
	}

}
