package org.dllearner.configuration.spring;

import org.dllearner.core.Component;
import org.dllearner.core.ComponentInitException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/23/11
 * Time: 7:00 AM
 *
 * Post Processor to initialize our components.
 */
public class ComponentInitializationBeanPostProcessor implements BeanPostProcessor{

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if(bean instanceof Component){
            Component c = (Component) bean;
            try {
                c.init();
            } catch (ComponentInitException e) {
                throw new RuntimeException("Problem initializing the component with beanName: " + beanName,e);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
