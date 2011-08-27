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

package org.dllearner.configuration;

import java.util.Collection;
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
     * Get a collection of all the bean names defined in the configuration.
     *
     * @return a collection of all the bean names defined in the configuration.
     */
    public Collection<String> getBeanNames();

    /**
     * Get the class for the given bean.
     *
     * @param beanName The name of the bean to get the class for.
     * @return The class for the given bean.
     */
    public Class getClass(String beanName);

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

    /**
     * Get the Base Directory where this configuration should be running out of.
     *
     * @return The Base Directory where this configuration should be running out of.
     */
    public String getBaseDir();


    public Collection<IConfigurationProperty> getConfigurationOptions(String beanName);


}
