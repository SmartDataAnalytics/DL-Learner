package org.dllearner.configuration.spring;

import org.dllearner.configuration.IConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/23/11
 * Time: 4:59 AM
 *
 * Default implementation of the ApplicationContextBuilder
 */
public class DefaultApplicationContextBuilder implements ApplicationContextBuilder{

    @Override
    public ApplicationContext buildApplicationContext(IConfiguration configuration, List<String> componentKeyPrefixes, List<Resource> springConfigurationLocations) throws IOException{
        ConfigurableApplicationContext context = null;
        // Post Processors
        BeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor = new ConfigurationBasedBeanDefinitionRegistryPostProcessor(configuration);

        //These files need to be loaded first
        List<Resource> allSpringConfigFiles = new ArrayList<Resource>();
        allSpringConfigFiles.add(new ClassPathResource("/org/dllearner/configuration/spring/bean-post-processor-configuration.xml"));
        allSpringConfigFiles.addAll(springConfigurationLocations);

        String[] springConfigurationFiles = new String[allSpringConfigFiles.size()];
        int ctr = 0;
        for (Resource springConfigurationLocation : allSpringConfigFiles) {
           springConfigurationFiles[ctr] = springConfigurationLocation.getFile().toURI().toString();
           ctr++;
        }
        context = new ClassPathXmlApplicationContext(springConfigurationFiles, false);

        // These post processors run before object instantiation
        context.addBeanFactoryPostProcessor(beanDefinitionRegistryPostProcessor);

        //Instantiate and initialize the beans.
        context.refresh();
        return context;
    }
}
