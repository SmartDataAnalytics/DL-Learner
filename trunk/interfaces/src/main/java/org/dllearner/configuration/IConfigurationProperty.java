package org.dllearner.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/27/11
 * Time: 8:57 AM
 *
 * This Represents a Configuration Option within a DL Learner Configuration.
 *
 * @see IConfiguration
 */
public interface IConfigurationProperty {

    /**
     * Get the Name of this Property
     *
     * @return The Name of this property.
     */
    public String getName();


    /**
     * Get the String representation of the value of this property.
     *
     * @return The String representation of the value of this property.
     */
    public Object getValue();


    /**
     * Does this property represent a bean reference?
     *
     * @return True if it does.
     */
    public boolean isBeanReference();

    /**
     * Does this property represent a collection of bean references?
     *
     * @return True if it does.
     */
    public boolean isBeanReferenceCollection();
}
