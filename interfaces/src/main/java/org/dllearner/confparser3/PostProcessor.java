/**
 * Copyright (C) 2007, Jens Lehmann
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.cli.ConfFileOption2;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.StringTuple;

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
			List<StringTuple> prefixes = (List<StringTuple>) prefixOption.getValueObject();
			
			// loop through all options and replaces prefixes
			for(ConfFileOption2 option : confOptions) {
				String value = option.getPropertyValue();
				for(StringTuple prefix : prefixes) {
					// we only replace the prefix if it occurs directly after a quote
					value = value.replaceAll("\"" + prefix.a + ":", "\"" + prefix.b);
				}
				option.setPropertyValue(value);
			}
		}
	}
}
