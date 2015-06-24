/**
 * 
 */
package org.dllearner.utilities.split;

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
public abstract class AbstractNumericValuesSplitter extends AbstractValuesSplitter{

	public AbstractNumericValuesSplitter(AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
		super(reasoner, dataFactory);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.AbstractValuesSplitter#getDataProperties()
	 */
	@Override
	public Set<OWLDataProperty> getDataProperties() {
		return reasoner.getNumericDataProperties();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.utilities.split.AbstractValuesSplitter#getDatatypes()
	 */
	@Override
	public Set<OWLDatatype> getDatatypes() {
		return OWLAPIUtils.numericDatatypes;
	}

}
