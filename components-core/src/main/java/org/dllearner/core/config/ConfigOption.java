package org.dllearner.core.config;

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
    String name() default "ZZZZZZZZ";

    /**
     * The description of this config option
     * @return The description of this config option
     */
    String description() default "no description available";

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
    
    /**
     * An example value for this option that can be displayed in the configuration options documentation.
     * @return A valid example value for this option.
     */
    String exampleValue() default "";
}
