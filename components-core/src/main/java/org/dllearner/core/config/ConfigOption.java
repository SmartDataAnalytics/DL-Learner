package org.dllearner.core.config;

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
