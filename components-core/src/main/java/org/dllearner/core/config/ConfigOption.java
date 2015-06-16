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

import java.beans.PropertyEditor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Annotation for all DL-Learner configuration options.
 * 
 * @author Chris Shellenbarger
 * @author Jens Lehmann
 * @author Lorenz Bühmann
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
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
    String description() default "no description available";

    /**
     * An implementation of the Property Editor to use.
     * 
     * @deprecated We currently do not encourage specifying the
     * property editor, because they might not be needed if we find a way
     * of auto-detecting appropriate editors.
     * 
     * @return
     */
    @Deprecated
    Class<? extends PropertyEditor> propertyEditorClass() default PropertyEditor.class;
    
    /**
     * Returns whether this option is required for initializing the component.
     * 
     * Maybe soon deprecated: Please put @Required in the corresponding set method in addition.
     * @return True if the option is required and false otherwise.
     */
    boolean required() default false;
    
    /**
     * Returns the default value of this config option. Default values should be set for all
     * optional values. 
     * It is an overhead to describe the default value both in the source code and in the
     * annotation. There are two reasons for this: a) the value of the field cannot easily be accessed 
     * without creating an instance of the component and b) for more complex structures the default
     * may only be created in the constructor or init method. 
     * @return The default value of this option.
     */
    String defaultValue() default "";
}
