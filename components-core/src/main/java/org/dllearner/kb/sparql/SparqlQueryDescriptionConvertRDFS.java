package org.dllearner.kb.sparql;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;


/**
 * @author Sebastian Hellmann
 * Enables RDFS reasoning for the DL2SPARQL class
 * by concept rewriting
 * 
 */
public class SparqlQueryDescriptionConvertRDFS {

	//LOGGER: SparqlQueryDescriptionConvertVisitor
	static Logger logger = Logger.getLogger(SparqlQueryDescriptionConvertRDFS.class);

	/**
	 * 
	 * replaces each String representing a concept in descriptionKBSyntax with a
	 * union of the subclasses ex: (c sub b); (b sub a ) then: (a AND b) will be
	 * ((a OR b OR c) AND (b OR a))
	 * 
	 * @param descriptionKBSyntax the description in KB syntax
	 * @param maxDepth
	 *            determines the depth of retrieval, if 1 classes are replaced by direct subclasses only,
	 *            1 is HIGHLY RECOMMENDED FOR LARGE HIERARCHIES)
	 * @return the altered String
	 */
	public static String conceptRewrite(String descriptionKBSyntax, SPARQLTasks st,
			int maxDepth) {
		String quote = "\"";
		String returnValue = "";
		String currentconcept = "";
		int lastPos = 0;
		SortedSet<String> subclasses = new TreeSet<>();

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
			subclasses = st.getSubClasses(currentconcept, maxDepth);

			// if only one then keep
			if (subclasses.size() == 1)
				currentconcept = "\"" + currentconcept + "\"";
			// replace with union
			else {
				Set<OWLClassExpression> nc = new HashSet<>();
				for (String one : subclasses) {
					nc.add(new OWLClassImpl(IRI.create(one)));
				}
				currentconcept = new OWLObjectUnionOfImpl(nc).toString();
			}

			returnValue = currentconcept + returnValue;
			// ret+=description;
		}
		returnValue = descriptionKBSyntax + returnValue;
		// System.out.println(ret);
		return returnValue;
	}

	

}
