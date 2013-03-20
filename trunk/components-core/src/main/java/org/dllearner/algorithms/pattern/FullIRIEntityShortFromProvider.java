package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class FullIRIEntityShortFromProvider implements ShortFormProvider{

	@Override
	public void dispose() {
	}

	@Override
	public String getShortForm(OWLEntity entity) {
		return entity.toStringID();
	}

}
