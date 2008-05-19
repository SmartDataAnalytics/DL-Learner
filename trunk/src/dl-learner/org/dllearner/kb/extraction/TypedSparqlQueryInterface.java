/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb.extraction;

import java.net.URI;
import java.util.Set;

import org.dllearner.utilities.StringTuple;

/**
 * 
 * Typed SPARQL query interface. The typing means that they all have the same
 * input and the same output: They are fn: resource -> ( a | b ) where a
 * normally is a predicate and b an object
 * 
 * @author Sebastian Hellmann
 * 
 */
public interface TypedSparqlQueryInterface {

	public Set<StringTuple> getTupelForResource(URI u);
}
