package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.Set;

import org.dllearner.utilities.StringTuple;

/**
 * 
 * Type SPARQL query interface.
 * 
 * @author Sebastian Hellmann
 *
 */
public interface TypedSparqlQueryInterface {

	public Set<StringTuple> query(URI u);
}
