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
package org.dllearner.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.dllearner.confparser2.ConfParser;
import org.dllearner.confparser2.ParseException;

/**
 * 
 * New commandline interface.
 * 
 * @author Jens Lehmann
 *
 */
public class CLI {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		ConfParser parser = ConfParser.parseFile(new File("../test/newconf/test1.conf"));
		List<ConfFileOption> options = parser.getConfOptions();
		for(ConfFileOption option : options) {
			System.out.println(option);
		}
		
		System.out.println("positive examples: " + parser.getPositiveExamples());
		System.out.println("negative examples: " + parser.getNegativeExamples());
	}

}
