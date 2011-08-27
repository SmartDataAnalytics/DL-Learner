package org.dllearner.confparser3;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/27/11
 * Time: 1:41 PM
 *
 * Convert our string structure to a Map
 */
public class MapEditor extends PropertyEditorSupport{


    @Override
    public void setAsText(String text) throws IllegalArgumentException {

        StringTokenizer tokenizer = new StringTokenizer(text,"[(,\") ]");

        if((tokenizer.countTokens() % 2) != 0){
            throw new RuntimeException("Expected an even number of tokens, check your map syntax: " + text);
        }

        Map<String, String> result = new HashMap<String, String>();

        while (tokenizer.hasMoreTokens()){
            String key = tokenizer.nextToken();
            String value = tokenizer.nextToken();
            result.put(key,value);
        }

        setValue(result);
    }
}
