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
package org.dllearner.kb.sparql;

//stores all configuration settings
public class Configuration {
	
	/*
	 * this class colects all configuration information
	 * see the other classes, which are used as attributes here
	 * */
	
	private SpecificSparqlEndpoint specificSparqlEndpoint;
	private SparqlQueryType sparqlQueryType;
	private Manipulator manipulator;
	// the following needs to be moved to 
	// class extraction algorithm or manipulator
	private int recursiondepth = 2;
	private boolean getAllBackground = true;

	private Configuration() {
	}

	public Configuration(SpecificSparqlEndpoint specificSparqlEndpoint,
			SparqlQueryType sparqlQueryType, Manipulator manipulator, int recursiondepth,
			boolean getAllBackground) {
		this.specificSparqlEndpoint = specificSparqlEndpoint;
		this.sparqlQueryType = sparqlQueryType;
		this.manipulator = manipulator;
		this.recursiondepth = recursiondepth;
		this.getAllBackground = getAllBackground;

	}

	public Configuration changeQueryType(SparqlQueryType sqt) {
		// TODO must clone here
		return new Configuration(this.specificSparqlEndpoint, sqt, this.manipulator,
				this.recursiondepth, this.getAllBackground);

	}

	public Manipulator getManipulator() {
		return this.manipulator;
	}

	public SpecificSparqlEndpoint getSparqlEndpoint() {
		return specificSparqlEndpoint;
	}

	public SparqlQueryType getSparqlQueryType() {
		return sparqlQueryType;
	}

	public boolean isGetAllBackground() {
		return getAllBackground;
	}

	public int getRecursiondepth() {
		return recursiondepth;
	}

}
