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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 7/26/11
 * Time: 8:55 PM
 *
 * This is an example annotation class allowing one to configure a field with a name, description, and corresponding property editor.
 *
 * Note: Only put this on Setters that take the actual object you want to end up with as the  example expects it to be on the setter
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigOption {

    /**
     * The name of this config option.
     * @return The name of this config option.
     */
    String name();

    /**
     * The description of this config option
     * @return
     */
    String description();

    /**
     * An implementation of the Property Editor to use
     * @return
     */
    Class propertyEditorClass();
}
