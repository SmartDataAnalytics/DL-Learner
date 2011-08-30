package org.dllearner.configuration.util;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.IConfigurationProperty;
import org.dllearner.core.Component;
import org.springframework.schema.beans.*;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/30/11
 * Time: 5:23 AM
 * <p/>
 * Implementation responsible for writing the IConfiguration object out to a Spring Beans Schema.
 */
public class SpringConfigurationXMLBeanConverter implements ConfigurationXMLBeanConverter {

    @Override
    public XmlObject convert(IConfiguration configuration) {
        BeansDocument document = BeansDocument.Factory.newInstance();
        BeansDocument.Beans beans = document.addNewBeans();

        Collection<String> beanNames = configuration.getBeanNames();
        for (String beanName : beanNames) {

            Collection<IConfigurationProperty> properties = configuration.getConfigurationProperties(beanName);

            BeanDocument.Bean bean = beans.addNewBean();
            Class configurationClass = configuration.getClass(beanName);
            bean.setClass1(configurationClass.getName());
            bean.setName(beanName);

            if (isComponentClass(configurationClass)) {
                bean.setInitMethod("init");
            }


            for (IConfigurationProperty property : properties) {

                PropertyType xmlProp = bean.addNewProperty();
                xmlProp.setName(property.getName());

                Object value = property.getValue();
                if (property.isBeanReference()) {
                    xmlProp.setRef2((String) value);
                } else {
                    if (value instanceof String) {
                        xmlProp.setValue2((String) value);
                    }

                    if(value instanceof Set){
                        processSet(beanNames, xmlProp, (Set) value);
                    }

                    //TODO For Map
                }

            }

        }
        return document;
    }

    private void processSet(Collection<String> beanNames, PropertyType xmlProp, Set value) {
        Set mySet = (Set) value;
        SetDocument.Set set = xmlProp.addNewSet();
        for (Object o : mySet) {

            if (beanNames.contains(o)) {
                RefDocument.Ref ref = set.addNewRef();
                ref.setBean((String) o);
            } else {
                ValueDocument.Value setValue = set.addNewValue();
                XmlString s = XmlString.Factory.newInstance();
                s.setStringValue((String) o);
                setValue.set(s);
            }
        }
    }

    private boolean isComponentClass(Class configurationClass) {
        boolean found = false;

        Class[] interfaces = configurationClass.getInterfaces();

        int ctr = 0;
        while(!found && ctr < interfaces.length) {
            found = interfaces[ctr].isAssignableFrom(Component.class);
            ctr++;
        }

        return found;
    }
}
