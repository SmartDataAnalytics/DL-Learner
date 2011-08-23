package org.dllearner.configuration.spring;

import org.dllearner.configuration.IConfiguration;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedSet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/18/11
 * Time: 9:12 PM
 * <p/>
 * Extending the Custom Property Override Configurer so that we can use our IConfiguration objects
 * in conjunction with a Spring Based Configuration.
 */
public class ConfigurationBasedPropertyOverrideConfigurer extends PropertyOverrideConfigurer {


    private final IConfiguration configuration;
    private boolean ignoreInvalidKeys = false;
    private List<String> componentKeyPrefixes = new ArrayList<String>();

    /**
     * Primary Constructor.
     *
     * @param configuration     The DL-Learner Configuration object.
     * @param ignoreInvalidKeys Consult the PropertyOverrideConfigurer documentation for the usage of this variable.
     */
    public ConfigurationBasedPropertyOverrideConfigurer(IConfiguration configuration, boolean ignoreInvalidKeys) {
        super();
        this.configuration = configuration;
        this.ignoreInvalidKeys = ignoreInvalidKeys;
        setIgnoreInvalidKeys(false);
    }

    @Override
    protected void applyPropertyValue(ConfigurableListableBeanFactory factory, String beanName, String property, String value) {

        BeanDefinition bd = getBeanDefinition(factory, beanName);

        Object obj = buildObject(beanName, property);

        applyPropertyValue(factory, beanName, property, bd, obj);


    }

    /**
     * Build the object represented by beanName.property as it is represented in the configuration.
     *
     * @param beanName The bean name of the object to build
     * @param property The property name of the object to build
     * @return The object represented by beanName.property
     */
    protected Object buildObject(String beanName, String property) {
        StringBuilder objKey = buildObjectKey(beanName, property);
        return configuration.getObjectValue(objKey.toString());
    }

    /**
     * Get the Bean Definition for a particular bean.
     *
     * @param factory The factory to get the bean from.
     * @param beanName The bean to get.
     * @return The bean definition of beanName
     */
    protected BeanDefinition getBeanDefinition(ConfigurableListableBeanFactory factory, String beanName) {
        BeanDefinition bd = factory.getBeanDefinition(beanName);
        while (bd.getOriginatingBeanDefinition() != null) {
            bd = bd.getOriginatingBeanDefinition();
        }
        return bd;
    }

    /**
     * Apply obj to a property value.
     * @param factory
     * @param beanName
     * @param property
     * @param bd
     * @param obj
     */
    private void applyPropertyValue(ConfigurableListableBeanFactory factory, String beanName, String property, BeanDefinition bd, Object obj) {
        /** Check if Object represents a ReferencedBean */
        String referencedBeanName = getReferencedBeanName(obj);
        if (referencedBeanName != null && !referencedBeanName.isEmpty()) {
            applyBeanReferencePropertyValue(factory, beanName, property, referencedBeanName);
        } else {
            //TODO I don't like this code - refactor to make it more elegant later
            if(obj instanceof Collection){
                /** Now check for a Collection of component references */
                Collection collection = (Collection) obj;

                /** TODO: Now we only work with Sets as that is what comes from the configuration - but we should probably support other collection types as well */
                /** The managed set is the key to getting this to work - there are other managed objects we could use in the future */
                Collection components = new ManagedSet();

                for (Object o : collection) {
                    if(o instanceof String){
                        referencedBeanName = getReferencedBeanName(o);
                        if(referencedBeanName != null && !referencedBeanName.isEmpty()){
                            components.add(new RuntimeBeanReference(referencedBeanName));
                        }
                    }
                }
                if (components.isEmpty()) {
                    applyRegularPropertyValue(property, bd, obj);
                } else {
                    bd.getPropertyValues().addPropertyValue(property, components);
                }

            }else{
                /** We have a regular property value */
                applyRegularPropertyValue(property, bd, obj);
            }
        }
    }

    private void applyRegularPropertyValue(String property, BeanDefinition bd, Object obj) {
        PropertyValue pv = new PropertyValue(property, obj);
        pv.setOptional(ignoreInvalidKeys);
        bd.getPropertyValues().addPropertyValue(pv);
    }

    /**
     * Apply a bean reference to beanName.property in the given factory.
     *
     * @param factory        The factory to use.
     * @param beanName       The bean name
     * @param property       The property name.
     * @param referencedBean The referenced bean to set as bean.property in factory.
     */
    private void applyBeanReferencePropertyValue(ConfigurableListableBeanFactory factory, String beanName, String property, String referencedBean) {
        BeanDefinition bd = getBeanDefinition(factory, beanName);
        /** You have to get the bean definition of the referenced bean here - don't try to get the bean itself or you'll get a objects which aren't completely initialized yet */
        Object obj = factory.getBeanDefinition(referencedBean);
        bd.getPropertyValues().addPropertyValue(property, obj);
    }

    /**
     * Build the object key which is just name beanName.property or beanName if property is null.
     * @param beanName First part of the key.
     * @param property Second part of the key (after the period).
     * @return The object key.
     */
    private StringBuilder buildObjectKey(String beanName, String property) {
        StringBuilder objName = new StringBuilder();
        objName.append(beanName);
        if (property != null && !property.isEmpty()) {
            objName.append(".");
            objName.append(property);
        }
        return objName;
    }

    /**
     * Determine if value is a name of a referenced bean.
     * <p/>
     * This will return the name of the referenced bean if it does.  If not, it will return null.
     *
     * @param object The object to check.
     * @return True if we need to do custom loading of this value, false if we can use the parent.
     */
    protected String getReferencedBeanName(Object object) {

        String result = null;

        boolean found = false;

        Iterator<String> itr = getComponentKeyPrefixes().iterator();

        if (object instanceof String) {
            String value = (String) object;
            while (!found && itr.hasNext()) {
                String prefix = itr.next();
                if (value.startsWith(prefix)) {
                    found = true;
                    result = value.substring(prefix.length());
                }
            }
        }
        return result;

    }

    /**
     * Get the list of prefixes that cause us to do custom loading.
     *
     * @return The list of prefixes that cause us to do custom loading.
     */
    public List<String> getComponentKeyPrefixes() {
        return componentKeyPrefixes;
    }

    /**
     * Set the list of prefixes that cause us to do custom loading.
     *
     * @param componentKeyPrefixes the list of prefixes that cause us to do custom loading.
     */
    public void setComponentKeyPrefixes(List<String> componentKeyPrefixes) {
        this.componentKeyPrefixes = componentKeyPrefixes;
    }
}
