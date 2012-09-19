package org.dllearner.confparser3;

import org.apache.commons.collections15.BidiMap;
import org.dllearner.cli.ConfFileOption2;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.IConfigurationProperty;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/27/11
 * Time: 7:21 AM
 * <p/>
 * Conf Parser Based implementation.
 */
public class ConfParserConfiguration implements IConfiguration {

    private final ConfParser parser;
    private final String baseDir;
    private final String typeProperty = "type";


    public ConfParserConfiguration(Resource source) {
        try {
//          baseDir = source.getFile().getAbsoluteFile().getParent();
        	if(!(source instanceof InputStreamResource)){
        		baseDir = source.getFile().getParentFile().toURI().toString();
        	} else {
        		baseDir = null;
        	}
            parser = new ConfParser(source.getInputStream());
            parser.Start();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<String> getBeanNames() {
        Set<String> result = new HashSet<String>();
        Map<String,List<ConfFileOption2>> beans = parser.getConfOptionsByBean();
        result.addAll(beans.keySet());
        return result;
    }

    @Override
    public Class getClass(String beanName) {

        List<ConfFileOption2> confOptions = parser.getConfOptionsByBean(beanName);

        ConfFileOption2 option = null;
        for (ConfFileOption2 confOption : confOptions) {
            if(typeProperty.equalsIgnoreCase(confOption.getPropertyName())){
                option = confOption;
            }
        }

        if(option == null){
            throw new RuntimeException("No type property set for bean: " + beanName);
        }

        Class<?> result = null;

        String value = (String) option.getValue();
        // first option: use long name of @ComponentAnn annotation (case insensitive)
        BidiMap<Class<? extends Component>, String> componentsNamed = AnnComponentManager.getInstance().getComponentsNamed();
        for(Entry<Class<? extends Component>, String> entry : componentsNamed.entrySet()) {
        	if(entry.getValue().equalsIgnoreCase(value)) {
        		return entry.getKey();
        	}
        }
        // second option: use short name of @ComponentAnn annotation 
        // by convention, short names should always be lower case, but we still do it case insensitive
        BidiMap<Class<? extends Component>, String> componentsNamedShort = AnnComponentManager.getInstance().getComponentsNamedShort();
        for(Entry<Class<? extends Component>, String> entry : componentsNamedShort.entrySet()) {
        	if(entry.getValue().equalsIgnoreCase(value)) {
        		return entry.getKey();
        	}
        }
        // third option: use specified class name
        try {
            result = Class.forName(value);
        } catch (ClassNotFoundException e) {
        	// if all methods fail, throw an exception
            throw new RuntimeException("Problem getting class type for bean: " + beanName + " - trying to instantiate class: " + value,e);
        }
        return result;
    }

    @Override
    public String getBaseDir() {
        return baseDir;
    }

    @Override
    public Collection<IConfigurationProperty> getConfigurationProperties(String beanName) {
        List<ConfFileOption2> confFileOptions = parser.getConfOptionsByBean(beanName);
        Collection<IConfigurationProperty> result = new ArrayList<IConfigurationProperty>();

        for (ConfFileOption2 confFileOption : confFileOptions) {

            if (!typeProperty.equalsIgnoreCase(confFileOption.getPropertyName())) {
                result.add(confFileOption);
            }
        }
        return result;
    }
}
