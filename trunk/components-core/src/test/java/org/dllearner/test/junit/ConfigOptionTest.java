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
 */
package org.dllearner.test.junit;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.config.ConfigHelper;
import org.dllearner.core.config.ConfigOption;
import org.junit.Test;

/**
 * 
 * @author Jens Lehmann
 *
 */
public class ConfigOptionTest {

	@Test
	public void testConfigOption() {
		// now outdated, because property editors do not need to be specified for each option
		
//		List<ConfigOption> configOptions = ConfigHelper.getConfigOptions(CELOE.class);
//		assertFalse(configOptions.isEmpty());
//		
//		CELOE celoe = new CELOE();
//		celoe.setMaxExecutionTimeInSeconds(10);
//		Map<ConfigOption,Object> optionValues = ConfigHelper.getConfigOptionValues(celoe);
//		boolean found = false;
//		for(Entry<ConfigOption,Object> entry : optionValues.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue());
//			if(entry.getKey().name().equals("maxExecutionTimeInSeconds")) {
//				found = true;
//				assertTrue(Integer.valueOf(entry.getValue().toString())==10);
//			}
//		}
//		assertTrue(found);
		
	}
	
}
