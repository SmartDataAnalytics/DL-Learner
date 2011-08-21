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

package org.dllearner.examples;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 7/26/11
 * Time: 10:17 PM
 * <p/>
 * Making a basic extension so I can get a default constructor so that using reflection is simpler.
 *
 * I'm extending a Spring provided class for this.
 */
public class OurStringTrimmerEditor extends org.springframework.beans.propertyeditors.StringTrimmerEditor {


    /**
     * Default Constructor
     */
    public OurStringTrimmerEditor() {
        super(true);
    }
}
