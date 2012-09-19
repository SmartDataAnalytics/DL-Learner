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

package org.dllearner.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * Annotation for DL-Learner components. Each component has to implement the interface as
 * well as use this notation.
 * 
 * @author Jens Lehmann
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentAnn {

    /**
     * The name of this component.
     * @see ComponentManager#getName(Component)
     * @return The name of this component.
     */
    String name();
	
    /**
     * The short name of this component, which should exclusively consist of
     * lower case ASCII symbols and "_" without whitespace.
     * @return The short name of this component.
     */
    String shortName();
    
    /**
     * The version of this component. 1.0 indicates a stable component. Developers
     * should increase the version number in case of major implementation changes. 
     * @return A version number of this component.
     */
    double version();
    
    /**
     * An optional description of the component. This can be shown in tool tips,
     * help etc.
     * @return The description of the component.
     */
    String description() default "";
}
