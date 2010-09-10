package org.dllearner.tools.ore.ui.rendering;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class OWLEntityShortFormProvider implements ShortFormProvider{

	@Override
	public void dispose() {
	}

	@Override
	public String getShortForm(OWLEntity entity) {
		IRI iri = entity.getIRI();
		String rendering = iri.getFragment();
		
		if(rendering == null){
			String path = iri.toURI().getPath();
			if (path == null) {
				return iri.toQuotedString();
			}
			return iri.toURI().getPath().substring(path.lastIndexOf("/") + 1);
			
		} else if (rendering.indexOf(' ') != -1 
				|| rendering.indexOf('(') != -1
				|| rendering.indexOf(')') != -1) {
			return "'" + rendering + "'";
		} else {
			return rendering;
		}
		
		
	}

}
