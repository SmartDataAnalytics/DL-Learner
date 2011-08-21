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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

import org.dllearner.core.owl.DatatypeProperty;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 7/26/11
 * Time: 9:42 PM
 * <p/>
 * Basic Property Editor for the Object Property DL-Learner class.  Doesn't have GUI support yet but we could add that later if we wanted.
 */
public class DataPropertyEditor implements PropertyEditor {


    private DatatypeProperty value;

    @Override
    public void setValue(Object value) {
        this.value = (DatatypeProperty) value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean isPaintable() {
        /** Not right now, we're doing non gui work */
        return false;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {

    }

    @Override
    public String getJavaInitializationString() {
        /** This returns the value needed to reconstitute the object from a string */
        return value.getName();
    }

    @Override
    public String getAsText() {
        /** Get the text value of this object - for displaying in GUIS, etc */
        return value.getName();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        value = new DatatypeProperty(text);
    }

    @Override
    public String[] getTags() {
        /** If there was a known set of values it had to have, we could add that list here */
        return new String[0];
    }

    @Override
    public Component getCustomEditor() {
        /** GUI stuff, if you wanted to edit it a custom way */
        return null;
    }

    @Override
    public boolean supportsCustomEditor() {
        /** We don't support this right now, but maybe later */
        return false;

    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        /** More gui stuff, we don't need this for our basic example */
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        /** More gui stuff, we don't need this for our basic example */
    }
}
