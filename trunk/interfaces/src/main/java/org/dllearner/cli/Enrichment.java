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
package org.dllearner.cli;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.ResultSet;

/**
 * Command Line Interface for Enrichment.
 * 
 * @author Jens Lehmann
 * 
 */
public class Enrichment {

	public static void main(String[] args) throws IOException {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("h", "?", "help"), "Show help.");
		parser.acceptsAll(asList("v", "verbose"), "Verbosity level.");
		parser.acceptsAll(asList("e", "endpoint"), "SPARQL endpoint URL to be used.")
				.withRequiredArg().ofType(URL.class);
		parser.acceptsAll(asList("g", "graph"),
				"URI of default graph for queries on SPARQL endpoint.").withOptionalArg()
				.ofType(URL.class);
		parser.acceptsAll(asList("r", "resource"),
				"The resource for which enrichment axioms should be suggested.").withOptionalArg();
		parser.acceptsAll(asList("o", "output"), "Specify a file where the output can be written.")
				.withOptionalArg();
		parser.acceptsAll(asList("f", "format"),
				"Format of the generated output (plain, html, rdf).").withOptionalArg()
				.ofType(String.class).defaultsTo("plain");

		// parse options and display a message for the user in case of problems
		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage() + ". Use -? to get help.");
			System.exit(0);
		}

		// print help screen
		if (options.has("?")) {
			parser.printHelpOn(System.out);
			String addHelp = "Additional explanations: The resource specified should " +
			"be a class, object \nproperty or data property. DL-Learner will try to " +
			"automatically detect its \ntype. If no resource is specified, DL-Learner will " +
			"generate enrichment \nsuggestions for all detected classes and properties in " + 
			"the given endpoint \nand graph. This can take several hours.";
			System.out.println();
			System.out.println(addHelp);
			// main script
		} else {
			// create SPARQL endpoint object
			URL endpoint = (URL) options.valueOf("endpoint");
			URL graph = (URL) options.valueOf("graph");
			LinkedList<String> defaultGraphURIs = new LinkedList<String>();
			defaultGraphURIs.add(graph.toString());
			SparqlEndpoint se = new SparqlEndpoint(endpoint, defaultGraphURIs, null);
			
			// sanity check that endpoint/graph returns at least one triple
			String query = "SELECT * WHERE {?s ?p ?o} LIMIT 1";
			SparqlQuery sq = new SparqlQuery(query, se);
			ResultSet q = sq.send();
			while (q.hasNext()) {
				q.next();
			}
			
			// run an algorithm using the resource as input
			
			
			// TODO: detect type of the resource
			// TODO: run all possible algorithms
			// TODO: automatically run over all resources if no specific resource was specified
			
		}

	}

}
