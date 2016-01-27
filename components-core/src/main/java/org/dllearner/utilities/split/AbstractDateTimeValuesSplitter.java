/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
