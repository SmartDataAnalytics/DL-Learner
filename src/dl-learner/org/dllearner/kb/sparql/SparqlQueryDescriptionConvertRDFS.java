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

import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Union;


/**
 * @author Sebastian Hellmann
 * Enables RDFS reasoning for the DL2SPARQL class
 * by concept rewriting
 * //QUALITY use SPARQLtasks
 */
public class SparqlQueryDescriptionConvertRDFS {

	//LOGGER: SparqlQueryDescriptionConvertVisitor
	static Logger logger = Logger.getLogger(SparqlQueryDescriptionConvertVisitor.class);

	/**
	 * 
	 * replaces each String representing a concept in descriptionKBSyntax with a
	 * union of the subclasses ex: (c sub b); (b sub a ) then: (a AND b) will be
	 * ((a OR b OR c) AND (b OR a))
	 * 
	 * @param descriptionKBSyntax
	 * @param se
	 * @param c
	 *            a cache object, makes only sense if you use this function
	 *            often
	 * @param simple
	 *            if true then only direct subclasses will be used (TRUE HIGHLY
	 *            RECOMMENDED for large hierarchies)
	 * @return the altered String
	 */
	public static String conceptRewrite(String descriptionKBSyntax, SPARQLTasks st,
			boolean simple) {
		String quote = "\"";
		String returnValue = "";
		String currentconcept = "";
		int lastPos = 0;
		SortedSet<String> subclasses = new TreeSet<String>();

		// searches for everything in "", but backwards
		while ((lastPos = descriptionKBSyntax.lastIndexOf(quote)) != -1) {
			returnValue = descriptionKBSyntax.substring(lastPos + 1, descriptionKBSyntax.length())
					+ returnValue;
			descriptionKBSyntax = descriptionKBSyntax.substring(0, lastPos);
			// System.out.println(description);
			lastPos = descriptionKBSyntax.lastIndexOf(quote);
			currentconcept = descriptionKBSyntax.substring(lastPos + 1, descriptionKBSyntax
					.length());
			descriptionKBSyntax = descriptionKBSyntax.substring(0, lastPos);
			// replace
			// currentconcept="\"blabla\"";
			// System.out.println(currentconcept);

			// subclasses are retrieved
			subclasses = st.getSubClasses(currentconcept, simple);

			// if only one then keep
			if (subclasses.size() == 1)
				currentconcept = "\"" + currentconcept + "\"";
			// replace with union
			else {
				LinkedList<Description> nc = new LinkedList<Description>();
				for (String one : subclasses) {
					nc.add(new NamedClass(one));
				}
				currentconcept = new Union(nc).toKBSyntaxString();
			}

			returnValue = currentconcept + returnValue;
			// ret+=description;
		}
		returnValue = descriptionKBSyntax + returnValue;
		// System.out.println(ret);
		return returnValue;
	}

	

}
