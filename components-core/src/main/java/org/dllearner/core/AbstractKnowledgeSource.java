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

package org.dllearner.core;

import java.io.File;
import java.net.URI;

import org.dllearner.core.owl.KB;

/**
 * Represents a knowledge source component, e.g. OWL files, SPARQL Endpoints,
 * Linked Data.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class AbstractKnowledgeSource extends AbstractComponent implements KnowledgeSource {
	
	/**
	 * Transforms this knowledge source into an internal knowledge base.
	 * @return An internal Knowledge base or null if this knowledge source
	 * does not support a conversion to an internal knowledge base.
	 */
	public abstract KB toKB();
	
	/**
	 * Transforms this knowledge source to DIG 1.1 code according to
	 * <a href="http://dl.kr.org/dig/">the specification</a>. DIG is used
	 * for communicating with reasoners.
	 * 
	 * @param kbURI The URI which is assigned to the knowledge base. The URI 
	 * is used to refer to the knowledge base in queries (DIG supports using
	 * several knowledge bases).
	 * @return The DIG XML code.
	 */
	public abstract String toDIG(URI kbURI);

	/**
	 * Export the knowledge source to the specified file in the specified format.
	 * @param file File to store the knowledge base.
	 * @param format Format of the knowledge base, e.g. N-Triples.
	 * @throws OntologyFormatUnsupportedException Thrown if the conversion
	 * to the specified format is not supported by this knowledge source.
	 */
	public abstract void export(File file, OntologyFormat format) throws OntologyFormatUnsupportedException;
	
}
