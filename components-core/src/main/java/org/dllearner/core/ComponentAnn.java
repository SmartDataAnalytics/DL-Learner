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
     * An optional OWLClassExpression of the component. This can be shown in tool tips,
     * help etc.
     * @return The OWLClassExpression of the component.
     */
    String description() default "";
}
