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
package org.dllearner.confparser3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dllearner.cli.ConfFileOption2;

/**
 * Performs post processing of conf files based on special parsing directives.
 * 
 * @author Jens Lehmann
 *
 */
public class PostProcessor {

	List<ConfFileOption2> confOptions;
	Map<String, ConfFileOption2> directives;
	
	public PostProcessor(List<ConfFileOption2> confOptions, Map<String, ConfFileOption2> directives) {
		this.confOptions = confOptions;
		this.directives = directives;
	}
	
	/**
	 * Applies all special directives by modifying the conf options.
	 */
	public void applyAll() {
		// apply prefix directive
		ConfFileOption2 prefixOption = directives.get("prefixes");
		if(prefixOption != null) {
			Map<String,String> prefixes = (Map<String,String>) prefixOption.getValueObject();
			
			// loop through all options and replaces prefixes
			for(ConfFileOption2 option : confOptions) {
                Object valueObject = option.getValue();

                if(valueObject instanceof String){
                    for (String prefix : prefixes.keySet()) {
                        // we only replace the prefix if it occurs directly after a quote
                        valueObject = ((String) valueObject).replaceAll(prefix + ":", prefixes.get(prefix));
                    }

                } else if(valueObject instanceof Map) {
                	valueObject = processStringMap(prefixes, (Map)valueObject);
                } else if(valueObject instanceof Collection){
                    processStringCollection(prefixes, (Collection<?>) valueObject);
                } else if(valueObject instanceof Boolean || valueObject instanceof Integer || valueObject instanceof Double) {
                	// nothing needs to be done for booleans
                } else {
                	throw new Error("Unknown conf option type " + valueObject.getClass());
                }

				option.setValueObject(valueObject);
			}
        }
    }


    private Map processStringMap(Map<String, String> prefixes, Map inputMap) {

        Map newMap = new HashMap();

        /** This does the values */
        for (Object keyObject : inputMap.keySet()) {
            Object key = keyObject;
            Object value = inputMap.get(key);

            if (keyObject instanceof String) {
                String keyString = (String) keyObject;
                // replace prefixes in the key
                for (String prefix : prefixes.keySet()) {
                	key = keyString.replaceAll(prefix + ":", prefixes.get(prefix));
                }
                // if the value is a string, we also replace prefixes there
                if (value instanceof String) {
                    String valueString = (String) value;
                    for (String prefix : prefixes.keySet()) {
                        value = valueString.replaceAll(prefix + ":", prefixes.get(prefix));    
                    }
                }
            }
            newMap.put(key, value);
        }

       return newMap;

    }

    private void processStringCollection(Map<String, String> prefixes, Collection valueObject) {
        Map<String, String> oldNewStringValues = new HashMap<String, String>();
        Iterator itr = valueObject.iterator();
        while (itr.hasNext()) {
            for (String prefix : prefixes.keySet()) {
                Object nextObject = itr.next();
                if (nextObject instanceof String) {
                    String oldValue = (String) nextObject;
                    String newValue = oldValue.replaceAll(prefix + ":", prefixes.get(prefix));
                    oldNewStringValues.put(oldValue, newValue);
                }
            }
        }

        Collection<String> oldValues = oldNewStringValues.keySet();
        Collection<String> newValues = oldNewStringValues.values();
        valueObject.removeAll(oldValues);
        valueObject.addAll(newValues);
    }
}
