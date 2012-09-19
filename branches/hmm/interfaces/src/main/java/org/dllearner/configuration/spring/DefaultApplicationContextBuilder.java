package org.dllearner.configuration.spring;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.configuration.IConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/23/11
 * Time: 4:59 AM
 *
 * Default implementation of the ApplicationContextBuilder
 */
public class DefaultApplicationContextBuilder implements ApplicationContextBuilder{

    private static Logger logger = LoggerFactory.getLogger(DefaultApplicationContextBuilder.class);

    @Override
    public ApplicationContext buildApplicationContext(IConfiguration configuration, List<Resource> springConfigurationLocations) throws IOException{
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
//           springConfigurationFiles[ctr] = springConfigurationLocation.getFile().toURI().toString();//this works not if packaged as jar file
        	 try {
				springConfigurationFiles[ctr] = springConfigurationLocation.getURL().toURI().toString();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
           ctr++;
        }
        context = new ClassPathXmlApplicationContext(springConfigurationFiles, false);

        // These post processors run before object instantiation
        context.addBeanFactoryPostProcessor(beanDefinitionRegistryPostProcessor);

        //Instantiate and initialize the beans.
        try {
            context.refresh();
        } catch (BeanCreationException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("There was a problem initializing the components...shutting down.");
            throw new RuntimeException(e);
        }
        return context;
    }
}
