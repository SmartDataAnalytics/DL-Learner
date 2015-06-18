/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.core.config;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 7/26/11
 * Time: 9:42 PM
 * <p/>
 * Basic Property Editor for OWL class.  Doesn't have GUI support yet but we could add that later if we wanted.
 */
public class NamedClassEditor extends AbstractPropertyEditor<OWLClass> {

	private OWLDataFactory df = new OWLDataFactoryImpl();
    private OWLClass value;

    @Override
    public String getJavaInitializationString() {
        /** This returns the value needed to reconstitute the object from a string */
        return value.toStringID();
    }

    @Override
    public String getAsText() {
        /** Get the text value of this object - for displaying in GUIS, etc */
        return value.toStringID();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        value = df.getOWLClass(IRI.create(text));
    }
}
