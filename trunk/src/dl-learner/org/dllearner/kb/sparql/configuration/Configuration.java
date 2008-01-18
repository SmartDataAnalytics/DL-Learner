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
package org.dllearner.kb.sparql.configuration;

import org.dllearner.kb.sparql.Manipulator;

/**
 * Stores all configuration settings.
 * 
 * @author Sebastian Hellmann
 */
public class Configuration {
	
	/*
	 * this class colects all configuration information
	 * see the other classes, which are used as attributes here
	 * */
	
	private SparqlEndpoint specificSparqlEndpoint;
	private SparqlQueryType sparqlQueryType;
	private Manipulator manipulator;
	// the following needs to be moved to 
	// class extraction algorithm or manipulator
	private int recursiondepth = 2;
	private boolean getAllSuperClasses = true;
	private boolean closeAfterRecursion = true;
	public  int numberOfUncachedSparqlQueries=0; 
	public  int numberOfCachedSparqlQueries=0; 

	public Configuration(SparqlEndpoint specificSparqlEndpoint,
			SparqlQueryType sparqlQueryType, Manipulator manipulator, int recursiondepth,
			boolean getAllSuperClasses, boolean closeAfterRecursion) {
		this.specificSparqlEndpoint = specificSparqlEndpoint;
		this.sparqlQueryType = sparqlQueryType;
		this.manipulator = manipulator;
		this.recursiondepth = recursiondepth;
		this.getAllSuperClasses = getAllSuperClasses;
		this.closeAfterRecursion=closeAfterRecursion;

	}

	public Configuration changeQueryType(SparqlQueryType sqt) {
		// TODO must clone here
		return new Configuration(this.specificSparqlEndpoint, sqt, this.manipulator,
				this.recursiondepth, this.getAllSuperClasses,this.closeAfterRecursion);

	}

	public Manipulator getManipulator() {
		return this.manipulator;
	}

	public SparqlEndpoint getSparqlEndpoint() {
		return specificSparqlEndpoint;
	}

	public SparqlQueryType getSparqlQueryType() {
		return sparqlQueryType;
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
	
	public void increaseNumberOfuncachedSparqlQueries(){
		numberOfUncachedSparqlQueries++;
	}
	public void increaseNumberOfCachedSparqlQueries(){
		numberOfCachedSparqlQueries++;
	}

}
