package org.dllearner.confparser2;

import org.dllearner.cli.ConfFileOption;
import org.dllearner.configuration.IConfiguration;
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

    public ConfParserConfiguration(Resource source){
        try {
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
            String fullName = option.getFullName();
            String stringValue = option.getValue().toString();
            try {
                props.setProperty(fullName, stringValue);
            } catch (Exception e) {
                throw new RuntimeException("Problem with property name: " + fullName + " and value " + stringValue,e);
            }
        }
        return props;
    }

    @Override
    public Set<String> getPositiveExamples() {
        return parser.getPositiveExamples();
    }

    @Override
    public Set<String> getNegativeExamples() {
        return parser.getNegativeExamples();
    }
}
