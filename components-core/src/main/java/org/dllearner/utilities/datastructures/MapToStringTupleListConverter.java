package org.dllearner.utilities.datastructures;

import org.dllearner.utilities.datastructures.StringTuple;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/28/11
 * Time: 6:37 AM
 *
 * Convert Maps of String to String Tuple List
 */
public class MapToStringTupleListConverter implements Converter<Map<String,String>,List<StringTuple>> {

    @Override
    public List<StringTuple> convert(Map<String,String> source) {
        List<StringTuple> result = new ArrayList<StringTuple>();

        Set<String> keys = source.keySet();

        for (String key : keys) {
            String value = source.get(key);
            result.add(new StringTuple(key, value));
        }

        return result;
    }
}
