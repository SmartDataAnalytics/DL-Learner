package org.dllearner.configuration;

import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/18/11
 * Time: 9:45 PM
 *
 * This interface defines our interaction with a DL-Learner specific configuration.
 *
 */
public interface IConfiguration {

    /**
     * Get the object for the given key.
     *
     * @param key The key of the object to retrieve.
     * @return The Object representation of the value keyed by key.
     */
    public Object getObjectValue(String key);

    /**
     * Get a Properties object describing all of the properties which this configuration object
     * knows about.
     *
     * As Properties are basically Map<String,String> objects, you can use getObjectValue(String key).  To
     * get the Object.
     *
     * @return A Properties Object.
     */
    public Properties getProperties();

    /**
     * Get the set of positive examples associated with this configuration.
     *
     * Never returns null, only an empty set if there are no positive examples.
     *
     * @return The set of positive examples associated with this configuration.
     */
    public Set<String> getPositiveExamples();

    /**
     * Get the set of negative examples associated with this configuration.
     *
     * Never returns null, only an empty set if there are no negative examples.
     *
     * @return The set of negative examples associated with this configuration.
     */
    public Set<String> getNegativeExamples();



}
