package org.dllearner.configuration.spring;

import org.dllearner.confparser2.ConfParserConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
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
    public ApplicationContext buildApplicationContext(Resource confFile, List<String> componentKeyPrefixes, List<Resource> springConfigurationLocations) throws IOException{
        ConfigurableApplicationContext context = null;
        ConfParserConfiguration configuration = new ConfParserConfiguration(confFile);

        BeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor = new ConfigurationBasedBeanDefinitionRegistryPostProcessor(configuration);

        ConfigurationBasedPropertyOverrideConfigurer configurer = new ConfigurationBasedPropertyOverrideConfigurer(configuration, false);
        configurer.setProperties(configuration.getProperties());
        configurer.getComponentKeyPrefixes().addAll(componentKeyPrefixes);

        String[] springConfigurationFiles = new String[springConfigurationLocations.size()];
        int ctr = 0;
        for (Resource springConfigurationLocation : springConfigurationLocations) {
           springConfigurationFiles[ctr] = springConfigurationLocation.getFile().toURI().toString();
        }
        context = new ClassPathXmlApplicationContext(springConfigurationFiles, false);

        context.addBeanFactoryPostProcessor(beanDefinitionRegistryPostProcessor);
        context.addBeanFactoryPostProcessor(configurer);

        context.refresh();
        return context;
    }
}
