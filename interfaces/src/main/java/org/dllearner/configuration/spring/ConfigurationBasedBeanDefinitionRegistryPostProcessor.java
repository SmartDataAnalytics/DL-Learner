package org.dllearner.configuration.spring;

import org.dllearner.configuration.IConfiguration;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/22/11
 * Time: 6:38 AM
 *
 * This class is used to insert BeanDefinitions that are declared in the configuration file that
 * do not exist in the existing registry (ie. they aren't declared in the spring XML file).
 */
public class ConfigurationBasedBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final IConfiguration configuration;

    public ConfigurationBasedBeanDefinitionRegistryPostProcessor(IConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Collection<String> beanNames = configuration.getBeanNames();

        for (String beanName : beanNames) {
            if(!registry.containsBeanDefinition(beanName)){
                Class beanClass = configuration.getClass(beanName);
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);

                BeanDefinition definition = builder.getBeanDefinition();

                /** Add Base Directory */
                if(beanClass.isAssignableFrom(KBFile.class)  || beanClass.isAssignableFrom(OWLFile.class)){
                    definition.getPropertyValues().addPropertyValue("baseDir",configuration.getBaseDir());
                }

                registry.registerBeanDefinition(beanName,definition);
            }
        }

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        /** Do nothing here */
    }
}
