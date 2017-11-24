package org.dllearner.configuration.spring.editors;

import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.regex.Pattern;

/**
 * Basic Property Editor for OWL class expressions.  
 * Doesn't have GUI support yet but we could add that later if we wanted.
 * @author Lorenz Buehmann
 *
 */
public class ClassExpressionPropertyEditor extends AbstractPropertyEditor<OWLClassExpression>{
	
	@Override
	public String getAsText() {
		return OWLAPIRenderers.toManchesterOWLSyntax(value);
	}
	

	final static Pattern whitespace = Pattern.compile("\\s");
	@Override
	public void setAsText(String s) throws IllegalArgumentException {
		if (!whitespace.matcher(s).find() && Pattern.compile(":").matcher(s).find()) {
			// it already is a full URI
			value = new OWLClassImpl(IRI.create(s));
		} else {
			// quote IRIs
			s = s.replaceAll("(?<=^|\\s|\\()((?:([^:/?#\\s]*):)(?://([^/?#]*?))?([^?#]*?)(?:\\?([^#]*?))?(?:#(.*?))?)(?=\\)|\\s|$)", "<$1>");
			// Bad hack to allow unparsed Manchester expressions. You need to decode this IRI and use the Manchester Parser once you have the Ontology
			IRI iri = IRI.create(OWLAPIUtils.UNPARSED_OCE + s);
			value = new OWLClassImpl(iri);
		}
	}
}
