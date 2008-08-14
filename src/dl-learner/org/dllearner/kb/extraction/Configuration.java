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

import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQueryMaker;

/**
 * Stores all configuration settings. this class collects all configuration
 * information see the other classes, which are used as attributes here
 *  
 * @author Sebastian Hellmann
 */
public class Configuration {

	private SPARQLTasks sparqlTasks;
	
	private SparqlEndpoint endpoint;
	private SparqlQueryMaker sparqlQueryMaker;
	private Manipulators manipulator;
	// the following needs to be moved to
	// class extraction algorithm or manipulator
	private int recursiondepth;
	private boolean getAllSuperClasses = true;
	private boolean closeAfterRecursion = true;
	public int numberOfUncachedSparqlQueries = 0;
	public int numberOfCachedSparqlQueries = 0;
	public String cacheDir="cache";

	public Configuration(SparqlEndpoint specificSparqlEndpoint,
			SparqlQueryMaker sparqlQueryMaker, Manipulators manipulator,
			int recursiondepth, boolean getAllSuperClasses,
			boolean closeAfterRecursion, String cacheDir) {
		this.endpoint = specificSparqlEndpoint;
		this.sparqlQueryMaker = sparqlQueryMaker;
		this.manipulator = manipulator;
		this.recursiondepth = recursiondepth;
		this.getAllSuperClasses = getAllSuperClasses;
		this.closeAfterRecursion = closeAfterRecursion;
		this.cacheDir=cacheDir;

	}

	public Configuration changeQueryType(SparqlQueryMaker sqm) {
		// TODO must clone here
		return new Configuration(this.endpoint, sqm, this.manipulator,
				this.recursiondepth, this.getAllSuperClasses,
				this.closeAfterRecursion, this.cacheDir);

	}

	public Manipulators getManipulator() {
		return this.manipulator;
	}

	public SparqlEndpoint getSparqlEndpoint() {
		return endpoint;
	}

	public SparqlQueryMaker getSparqlQueryMaker() {
		return sparqlQueryMaker;
	}

	public boolean isGetAllSuperClasses() {
		return getAllSuperClasses;
	}

	public boolean isCloseAfterRecursion() {
		return closeAfterRecursion;
	}

	public int getRecursiondepth() {
		return recursiondepth;
	}
	
	public SPARQLTasks getSparqlTasks() {
		return sparqlTasks;
	}

	
	/*public void increaseNumberOfuncachedSparqlQueries() {
		numberOfUncachedSparqlQueries++;
	}

	public void increaseNumberOfCachedSparqlQueries() {
		numberOfCachedSparqlQueries++;
	}*/

}
