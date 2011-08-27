package org.dllearner.confparser2;

import org.dllearner.cli.ConfFileOption;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.IConfigurationProperty;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.dllearner.utilities.datastructures.StringTuple;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/19/11
 * Time: 2:30 PM
 *
 * Conf Parser based implementation of the IConfiguration interface.
 *
 * We use the ConfParser to read DL-Learn conf files.
 */
public class ConfParserConfiguration implements IConfiguration {

    private final ConfParser parser;
    private final String baseDir;

    public ConfParserConfiguration(Resource source){
        try {
            baseDir = source.getFile().getAbsoluteFile().getParent();
            parser = new ConfParser(source.getInputStream());
            parser.Start();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getObjectValue(String name) {

        Object result = null;
        try {
            ConfFileOption confOption = parser.getConfOptionsByName(name);

            result = confOption.getValue();
            result = convert(result);
        } catch (Exception e) {
            throw new RuntimeException("Problem with creating object named: " + name, e);
        }
        return result;
    }

    /**
     * Convert if we have certain types.
     *
     * @param input The object to convert if necessary.
     * @return The converted version of input
     */
    protected Object convert(Object input) {
        Object result = input;

        /** Convert to a map if we have a list of String Tuples */
        if(input instanceof List){
            List set = (List) input;
            Iterator iterator = set.iterator();
            if(iterator.hasNext()){
                Object firstElement = iterator.next();
                if(firstElement instanceof StringTuple){
                    result = convertToMap(iterator, (StringTuple) firstElement);
                }
            }

        }
        return result;
    }

    private Map<String,String> convertToMap(Iterator iterator, StringTuple firstElement) {

        /** Convert to map */
        Map<String,String> result = new HashMap<String, String>();
        result.put(firstElement.a,firstElement.b);

        /** Add the rest of the tuples */
        while(iterator.hasNext()){
            StringTuple nextTuple = (StringTuple)iterator.next();
            result.put(nextTuple.a,nextTuple.b);
        }
        return result;
    }

    @Override
    public Properties getProperties() {
        Properties props = new Properties();

        List<ConfFileOption> options = parser.getConfOptions();
        for (ConfFileOption option : options) {
            if (!excludedFromProperties(option)) {
                String fullName = option.getFullName();
                String stringValue = option.getValue().toString();
                try {
                    props.setProperty(fullName, stringValue);
                } catch (Exception e) {
                    throw new RuntimeException("Problem with property name: " + fullName + " and value " + stringValue,e);
                }
            }
        }
        return props;
    }


    /**
     * Determine if this option should be excluded from the properties list.
     *
     * @param option The option to test
     * @return True if it should be excluded
     */
    private boolean excludedFromProperties(ConfFileOption option){
       boolean result = false;

       if(option.isStringOption()){
           String subOption = option.getSubOption();
           /** Exclude where suboption = true */
           if(subOption.equals("type")){
               result = true;
           }
       }

        return result;
    }

    @Override
    public Set<String> getPositiveExamples() {
        return parser.getPositiveExamples();
    }

    @Override
    public Set<String> getNegativeExamples() {
        return parser.getNegativeExamples();
    }

    @Override
    public Collection<String> getBeanNames() {
        Collection<String> beanNames = new HashSet<String>();

        List<ConfFileOption> options = parser.getConfOptions();
        for (ConfFileOption option : options) {
            beanNames.add(option.getOption());
        }
        return beanNames;
    }

    @Override
    public Class<?> getClass(String beanName) {
        Class<?> result = null;
        ConfFileOption option = parser.getConfOptionsByName(beanName + ".type");
        String value = (String) option.getValue();
        // first option: use long name of @ComponentAnn annotation
        Class<? extends Component> classFromName = AnnComponentManager.getInstance().getComponentsNamed().getKey(value);
        if(classFromName != null) {
        	return classFromName;
        }
        // second option: use short name of @ComponentAnn annotation
        Class<? extends Component> classFromShortName = AnnComponentManager.getInstance().getComponentsNamedShort().getKey(value);
        if(classFromShortName != null) {
        	return classFromShortName;
        }
        // third option: use specified class name
        try {
            result = Class.forName(value);
        } catch (ClassNotFoundException e) {
        	// if all methods fail, throw an exception
            throw new RuntimeException("Problem getting class type for bean: " + beanName + " - trying to instantiate class: " + option.getValue());
        }
        return result;

    }

    @Override
    public String getBaseDir() {
        return baseDir;
    }

    @Override
    public Collection<IConfigurationProperty> getConfigurationOptions(String beanName) {
        throw new RuntimeException("Don't use this class - use the one in the confparser3 package.");
    }
}
