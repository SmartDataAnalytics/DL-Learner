package org.dllearner.configuration.spring;

import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.IConfigurationProperty;
import org.dllearner.kb.KBFile;
import org.dllearner.kb.OWLFile;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.ManagedSet;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/22/11
 * Time: 6:38 AM
 * <p/>
 * This class is used to insert BeanDefinitions that are declared in the configuration file that
 * do not exist in the existing registry (ie. they aren't declared in the spring XML file).
 */
public class ConfigurationBasedBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final IConfiguration configuration;

    public ConfigurationBasedBeanDefinitionRegistryPostProcessor(IConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Ensure that BeanDefinitions exist for all referenced beans in the configuration.
     *
     * @param registry The Bean registry to use.
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        final Collection<String> beanNames = configuration.getBeanNames();

        for (String beanName : beanNames) {
            if (!registry.containsBeanDefinition(beanName)) {
                Class beanClass = configuration.getClass(beanName);
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);

                BeanDefinition definition = builder.getBeanDefinition();

                registry.registerBeanDefinition(beanName, definition);
            }
        }

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        final Collection<String> beanNames = configuration.getBeanNames();


        for (String beanName : beanNames) {

            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            Collection<IConfigurationProperty> properties = configuration.getConfigurationProperties(beanName);

            for (IConfigurationProperty property : properties) {

                Object value = property.getValue();
                //Process Single Bean References
                if (property.isBeanReference()) {
                    value = new RuntimeBeanReference((String)property.getValue());
                }

                //Process collections of bean references
                if(property.isBeanReferenceCollection()){
                    Collection<RuntimeBeanReference> beanReferences = new ManagedSet<RuntimeBeanReference>();
                    Collection<String> referencedBeanNames = (Collection<String>)property.getValue();
                    for (String referencedBeanName : referencedBeanNames) {
                         beanReferences.add(new RuntimeBeanReference(referencedBeanName));
                    }
                    value = beanReferences;
                }

                addBaseDirectoryIfNeeded(beanDefinition);

                beanDefinition.getPropertyValues().add(property.getName(), value);
            }
        }
    }

    /**
     * Add Base Directory Value to Beans which need it.
     *
     * @param beanDefinition The curren Bean Definition
     */
    private void addBaseDirectoryIfNeeded(BeanDefinition beanDefinition) {
        Class beanClass = null;
        try {
            beanClass = Class.forName(beanDefinition.getBeanClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find class " + beanDefinition.getBeanClassName());
        }
        /** Add Base Directory */
        if (beanClass.isAssignableFrom(KBFile.class) || beanClass.isAssignableFrom(OWLFile.class)) {
            beanDefinition.getPropertyValues().add("baseDir", configuration.getBaseDir());
        }
    }

}
