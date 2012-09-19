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

import java.io.File;
import java.io.FileNotFoundException;

import org.dllearner.cli.ConfFileOption2;
import org.junit.Test;

/**
 * Conf parser tests.
 * 
 * @author Jens Lehmann
 *
 */
public class ParseTest {

	@Test
	public void test() throws FileNotFoundException, ParseException {
		ConfParser parser = ConfParser.parseFile(new File("../examples/family/father.conf"));
		for(ConfFileOption2 option : parser.getConfOptions()) {
			System.out.print(option.getBeanName() + "." + option.getPropertyName() + " = " + option.getValue().toString());
			if(option.isBeanRef()) {
				System.out.println("    (bean reference)");
			} else {
				System.out.println();
			}	
		}
	}
	
}
