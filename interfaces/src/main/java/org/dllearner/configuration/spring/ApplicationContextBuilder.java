package org.dllearner.configuration.spring;

import org.dllearner.configuration.IConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/23/11
 * Time: 4:57 AM
 * Interface for building an application context for use with DL-Learner interfaces.
 */
public interface ApplicationContextBuilder {

    /**
     * Create an application context for use with the DL-Learner CLI interface.
     * <p/>
     * Note: In case of multiple spring config file locations, later bean definitions will override ones defined in earlier loaded files. This can be leveraged to deliberately override certain bean definitions via an extra XML file.
     *
     * @param configuration                The DL-Learner Configuration object.
     * @param springConfigurationLocations An ordered list of Spring Configuration Files - beans in later files can override beans in earlier files.
     * @return An Application Context
     * @throws IOException If there's a problem reading any of the files.
     */
    ApplicationContext buildApplicationContext(IConfiguration configuration, List<Resource> springConfigurationLocations) throws IOException;

}
