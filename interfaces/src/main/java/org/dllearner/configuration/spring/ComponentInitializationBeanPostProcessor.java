package org.dllearner.configuration.spring;

import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.dllearner.utilities.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/23/11
 * Time: 7:00 AM
 * <p/>
 * Post Processor to initialize our components.
 */
public class ComponentInitializationBeanPostProcessor implements BeanPostProcessor {

    private static Logger logger = LoggerFactory.getLogger(ComponentInitializationBeanPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof Component) {
            Component c = (Component) bean;

            String componentName = AnnComponentManager.getName(c);
            try {
            	
            	logger.info("Initializing component '{}' of type {} ...", beanName, componentName);
                long startTime = System.currentTimeMillis();
                c.init();
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;

                logger.info("... initialized component '{}' in {}. Status: OK", 
                		new String[]{beanName, Helper.prettyPrintMilliSeconds(elapsedTime)});
            } catch (ComponentInitException e) {
                throw new RuntimeException("Problem initializing the component \"" + componentName + "\" with beanName: " + beanName, e);
            } catch (Exception e) {
                /** Catch any exception as an init exception */
                logger.warn("Could not initialize component \"" + componentName + "\"");
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
