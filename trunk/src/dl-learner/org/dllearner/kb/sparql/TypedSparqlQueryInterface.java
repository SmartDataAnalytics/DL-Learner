package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.Set;

import org.dllearner.utilities.StringTuple;

public interface TypedSparqlQueryInterface {

	public Set<StringTuple> query(URI u);
}
