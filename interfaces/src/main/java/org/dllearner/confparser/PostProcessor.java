/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.confparser;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import joptsimple.internal.Strings;
import org.apache.commons.collections15.CollectionUtils;
import org.dllearner.cli.ConfFileOption;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs post processing of conf files based on special parsing directives.
 * 
 * @author Jens Lehmann
 *
 */
public class PostProcessor {

	List<ConfFileOption> confOptions;
	Map<String, ConfFileOption> directives;
	
	// a set of properties which contain URIs
//	static final Set<String> uriProperties = Sets.newHashSet(
//			"positiveExamples", 
//			"negativeExamples", 
//			"startClass", 
//			"classToDescribe",
//			"entityToDescribe",
//			"propertyToDescribe"
//			);

	public PostProcessor(List<ConfFileOption> confOptions, Map<String, ConfFileOption> directives) {
		this.confOptions = confOptions;
		this.directives = directives;
	}
	
	private static String replaceAllMap (String pre, Map<String,String> repMap, String post, String in) {
		List<String> keys = new ArrayList<>(repMap.keySet());
		Collections.sort(keys, (o1, o2) -> o1.length() - o2.length());
		CollectionUtils.transform(keys, input -> Pattern.quote(input));
		Matcher m = Pattern.compile(pre + "(" + Strings.join(keys, "|") + ")" + post).matcher(in);
		m.reset();
		if (m.find()) {
			StringBuffer sb = new StringBuffer();
			do {
				m.appendReplacement(sb, repMap.get(m.group(1)));
			} while (m.find());
			return m.appendTail(sb).toString();
		}
		return in;
		}
	
	/**
	 * Applies all special directives by modifying the conf options.
	 */
	public void applyAll() {
		//apply base URI directive
		
		
		// apply prefix directive
		ConfFileOption prefixOption = directives.get("prefixes");
		Map<String,String> prefixes = new TreeMap<>();
		
		prefixes.put("owl", OWL.NS);
		prefixes.put("rdfs", RDFS.getURI());
		prefixes.put("rdf", RDF.getURI());
		
		if(prefixOption != null) {
			prefixes.putAll((Map<String,String>) prefixOption.getValueObject());
		}
			 
		// loop through all options and replaces prefixes
		for(ConfFileOption option : confOptions) {
			Object valueObject = option.getValue();
//			if(uriProperties.contains(option.getPropertyName())){
//				System.out.println(option);
//			}
			

			if(valueObject instanceof String){
				valueObject = replaceAllMap("", prefixes, ":", (String) valueObject);
			} else if(valueObject instanceof Map) {
				valueObject = processStringMap(prefixes, (Map)valueObject);
			} else if(valueObject instanceof Collection){
				processStringCollection(prefixes, (Collection<?>) valueObject);
			} else if(valueObject instanceof Boolean || valueObject instanceof Integer || valueObject instanceof Double || valueObject instanceof Long) {
				// nothing needs to be done for booleans
			} else {
				throw new Error("Unknown conf option type " + valueObject.getClass());
			}

			option.setValueObject(valueObject);
		}
    }

    private Map processStringMap(Map<String, String> prefixes, Map inputMap) {

        Map newMap = new HashMap();

        /** This does the values */
        for (Object keyObject : inputMap.keySet()) {
            Object key = keyObject;
            Object value = inputMap.get(key);

            if (keyObject instanceof String) {
                // replace prefixes in the key
            	key = replaceAllMap("", prefixes, ":", (String) keyObject);
                // if the value is a string, we also replace prefixes there
                if (value instanceof String) {
                	value = replaceAllMap("", prefixes, ":", (String) value);
                }
            }
            newMap.put(key, value);
        }

       return newMap;

    }

    @SuppressWarnings("unchecked")
	private void processStringCollection(final Map<String, String> prefixes, Collection valueObject) {
        CollectionUtils.transform(valueObject, input -> {
	        if (input instanceof String) {
		        return replaceAllMap("", prefixes, ":", (String) input);
	        }
	        return input;
        });
    }
}
