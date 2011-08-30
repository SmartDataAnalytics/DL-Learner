package org.dllearner.configuration.util;

import org.apache.xmlbeans.XmlObject;
import org.dllearner.configuration.IConfiguration;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/30/11
 * Time: 5:20 AM
 *
 * Interface designed to convert an IConfiguration object into an XML Bean Object.
 */
public interface ConfigurationXMLBeanConverter {

    /**
     * Convert configuration to XmlObject.
     *
     * @param configuration The configuration object to convert
     * @return The resulting xml bean object
     */
    XmlObject convert(IConfiguration configuration);
}
