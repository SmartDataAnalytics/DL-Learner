/**
 * 
 */
package org.dllearner.utilities.split;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractDateTimeValuesSplitter extends AbstractValuesSplitter{

	public AbstractDateTimeValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
		super(reasoner, dataFactory);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.AbstractValuesSplitter#getDataProperties()
	 */
	@Override
	public Set<OWLDataProperty> getDataProperties() {
		Set<OWLDataProperty> dataProperties = new HashSet<>();
		for (OWLDataProperty dp : reasoner.getDatatypeProperties()) {
			OWLDatatype datatype = reasoner.getDatatype(dp);
			if(datatype != null && getDatatypes().contains(datatype)) {
				dataProperties.add(dp);
			}
		}
		return dataProperties;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.AbstractValuesSplitter#getDatatypes()
	 */
	@Override
	public Set<OWLDatatype> getDatatypes() {
		return OWLAPIUtils.dtDatatypes;
	}

}
