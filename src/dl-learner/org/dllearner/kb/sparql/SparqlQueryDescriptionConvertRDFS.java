package org.dllearner.kb.sparql;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Union;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.ResultBinding;

//COMMENT: header
public class SparqlQueryDescriptionConvertRDFS {

	static Logger logger = Logger.getLogger(SparqlQueryDescriptionConvertRDFS.class);

	

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
	public static String conceptRewrite(String descriptionKBSyntax, SparqlEndpoint se, Cache c,
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
			subclasses = getSubClasses(currentconcept, se, c, simple);

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

	/**
	 * gets a SortedSet of all subclasses
	 * 
	 * @see conceptRewrite(String descriptionKBSyntax, SparqlEndpoint se, Cache
	 *      c, boolean simple )
	 * @param description
	 * @param se
	 * @param c
	 * @param simple
	 * @return
	 */
	private static SortedSet<String> getSubClasses(String description, SparqlEndpoint se, Cache c,
			boolean simple) {

		// ResultSet rs = null;
		// System.out.println(description);
		SortedSet<String> alreadyQueried = new TreeSet<String>();
		try {

			// initialisation get direct Subclasses
			LinkedList<String> remainingClasses = new LinkedList<String>();

			// collect remaining classes
			remainingClasses.addAll(getDirectSubClasses(description.replaceAll("\"", ""), se, c));

			// remainingClasses.addAll(alreadyQueried);

			// alreadyQueried = new TreeSet<String>();
			alreadyQueried.add(description.replaceAll("\"", ""));

			if (simple) {
				alreadyQueried.addAll(remainingClasses);
				return alreadyQueried;
			} else {

				logger
						.warn("Retrieval auf all subclasses via SPARQL is cost intensive and might take a while");
				while (remainingClasses.size() != 0) {
					SortedSet<String> tmpSet = new TreeSet<String>();
					String tmp = remainingClasses.removeFirst();
					alreadyQueried.add(tmp);

					tmpSet = getDirectSubClasses(tmp, se, c);
					for (String string : tmpSet) {
						if (!(alreadyQueried.contains(string))) {
							remainingClasses.add(string);
						}// if
					}// for
				}// while
			}// else

		} catch (Exception e) {

		}

		return alreadyQueried;
	}

	/**
	 * QUALITY: workaround for a sparql glitch {?a owl:subclassOf ?b} returns an
	 * empty set on some entpoints. returns all direct subclasses of String
	 * concept
	 * 
	 * @param concept
	 * @return SortedSet of direct subclasses as String
	 */
	private static SortedSet<String> getDirectSubClasses(String concept, SparqlEndpoint se, Cache c) {
		String query = "SELECT * \n";
		query += "WHERE {\n";
		query += " ?subject ?predicate  <" + concept + "> \n";
		query += "}\n";

		ResultSet rs = null;
		if (c == null) {
			rs = new SparqlQuery(query, se).send();
		} else {
			String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
			rs = SparqlQuery.JSONtoResultSet(JSON);
		}

		SortedSet<String> subClasses = new TreeSet<String>();
		@SuppressWarnings("unchecked")
		List<ResultBinding> l = ResultSetFormatter.toList(rs);
		String p = "", s = "";
		for (ResultBinding resultBinding : l) {

			s = ((resultBinding.get("subject").toString()));
			p = ((resultBinding.get("predicate").toString()));
			if (p.equalsIgnoreCase("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				subClasses.add(s);
			}
		}
		return subClasses;
	}

}
