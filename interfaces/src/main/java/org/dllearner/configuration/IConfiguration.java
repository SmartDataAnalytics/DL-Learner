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

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/18/11
 * Time: 9:45 PM
 * <p/>
 * This interface represents on complete instance of a DL-Learner Configuration.
 * <p/>
 * Once an implementation of this interface is fully instantiated, it can be passed to an ApplicationContextBuilder in order
 * to instantiate a Spring Application Context.
 * <p/>
 * Once the application context has been created, learning algorithms can be extracted and then executed.
 *
 * @see org.dllearner.configuration.spring.ApplicationContextBuilder
 */
public interface IConfiguration {

    /**
     * Get a collection of all the bean names defined in the configuration.
     *
     * @return a collection of all the bean names defined in the configuration.
     */
    Collection<String> getBeanNames();

    /**
     * Get the class for the given bean.
     *
     * @param beanName The name of the bean to get the class for.
     * @return The class for the given bean.
     */
    Class getClass(String beanName);

    /**
     * Get the Base Directory where this configuration should be running out of.
     *
     * @return The Base Directory where this configuration should be running out of.
     */
    String getBaseDir();

    /**
     * Get the configuration properties for the specified bean.
     *
     * @param beanName The bean to get properties for.
     * @return A collection of properties
     */
    Collection<IConfigurationProperty> getConfigurationProperties(String beanName);
}
